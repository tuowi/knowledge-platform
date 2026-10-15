from fastapi import FastAPI, HTTPException, Header

from pydantic import BaseModel
from fastapi.responses import StreamingResponse

from utils.context import set_request_token
from core.react_agent import ReactAgent
from contextlib import asynccontextmanager
from utils.path_tool import get_abs_path, get_previous_path
from database_operation.delete_session import delete_thread_memory
from database_operation.get_session import get_thread_memory
from rag.vector_store import VectorStoreService

import uvicorn
from utils.logger_handler import logger


class ChatRequest(BaseModel):
    session_id: str
    query: str


agent_instance = None


@asynccontextmanager
async def lifespan(app: FastAPI):
    global agent_instance
    # 异步创建
    agent_instance = ReactAgent(db_path=get_abs_path("conversations.db"))
    await agent_instance._ensure_checkpointer()  # 异步初始化
    print("✅ Agent 已启动")
    yield
    if agent_instance:
        await agent_instance.close()  # 异步关闭
        print("✅ Agent 已关闭")


app = FastAPI(lifespan=lifespan)


# 接收问题，回答问题
@app.post("/chat/stream")
async def chat_stream(
        request_data: ChatRequest,
        authentication: str = Header(..., alias="Authentication")
):
    try:
        set_request_token(authentication)
        return StreamingResponse(
            agent_instance.execute_stream(
                query=request_data.query,
                session_id=request_data.session_id,
                token=authentication  # 传递 token
            ),
            media_type="text/plain"
        )
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))


# 删除会话记忆
@app.delete("/memory/{thread_id}")
async def clear_memory(thread_id: str):
    try:
        # 使用与 Agent 相同的数据库路径
        db_path = get_abs_path("conversations.db")

        # 删除数据库中的记忆
        await delete_thread_memory(db_path, thread_id)

        # 可选：如果 Agent 有内部缓存，通知它清除该会话的缓存
        if agent_instance and hasattr(agent_instance, 'invalidate_cache'):
            await agent_instance.invalidate_cache(thread_id)

        return {
            "status": "success",
            "message": f"Thread {thread_id} memory cleared",
            "db_path": db_path
        }
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))


# 获取会话记忆
@app.get("/memory/{thread_id}")
async def get_memory(
        thread_id: str,
):
    """获取指定会话的所有记忆，返回格式化的消息列表"""
    try:
        # 使用与 Agent 相同的数据库路径
        db_path = get_abs_path("conversations.db")

        # 获取记忆历史（返回的是 CheckpointTuple 对象列表）
        history = await get_thread_memory(db_path, thread_id)

        # 格式化返回
        if history:
            # 获取最新状态的 checkpoint（字典格式）
            latest_checkpoint = history[0].checkpoint  # 这是字典
            latest_state = latest_checkpoint.get('channel_values', {})
            messages = latest_state.get('messages', [])

            # 格式化消息为你需要的格式
            formatted_messages = []
            for msg in messages:
                # 确定角色
                if msg.type == "human":
                    role = "user"
                elif msg.type == "ai":
                    role = "assistant"
                else:
                    role = "system"

                # 构建消息对象
                message_obj = {
                    "role": role,
                    "content": msg.content
                }

                formatted_messages.append(message_obj)

            return {
                "thread_id": thread_id,
                "total_messages": len(formatted_messages),
                "messages": formatted_messages
            }
        else:
            return {
                "thread_id": thread_id,
                "total_messages": 0,
                "messages": [],
                "message": "No memory found for this thread"
            }
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))


# 归档文档进入向量数据库
@app.post("/document/archive")
async def archive_document(
        document_path: str
):
    try:
        vs = VectorStoreService()
        document_dir = get_previous_path(document_path)
        vs.load_document_archive(document_dir)  # 同步方法
        return {
            "status": "success",
        }
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@app.get("/logger/test")
async def test_logger(a:str):
    logger.info(f"{a}已经执行")
    return {"status": "success"}


if __name__ == "__main__":
    uvicorn.run(
        "main:app",
        reload=True,
        log_level="info"  # 确保这个不是 "warning" 或更高
    )
