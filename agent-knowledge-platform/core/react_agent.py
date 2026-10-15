from langchain.agents import create_agent

from tools.team_tool import team_info_query
from model.factory import chat_model
from utils.base_api_tool1 import BaseApiTool1
from utils.prompt_loader import load_system_prompts
from tools.document_tool import document_info_query
from tools.rag_summary_tool import rag_summarize

from langgraph.checkpoint.sqlite.aio import AsyncSqliteSaver
from utils.path_tool import get_abs_path
import aiosqlite
from langgraph.checkpoint.sqlite import SqliteSaver  # 使用同步版本


class ReactAgent(object):
    def __init__(self, db_path: str = get_abs_path("conversations.db")):
        self.db_path = db_path
        self._saver_context = None
        self.agent = None
        self.checkpointer = None

    async def _ensure_checkpointer(self):
        """异步确保 checkpointer 已初始化"""
        if self.checkpointer is None:
            # 异步创建连接
            self._saver_context = await aiosqlite.connect(self.db_path)
            self.checkpointer = AsyncSqliteSaver(self._saver_context)

            # 初始化数据库表
            await self.checkpointer.setup()

            self.agent = create_agent(
                model=chat_model,
                system_prompt=load_system_prompts(),
                tools=[team_info_query, document_info_query, rag_summarize],
                checkpointer=self.checkpointer,
            )
        return self.agent

    async def execute_stream(self, query: str, session_id: str = "default_session", token: str = None):
        """异步流式执行"""
        # 确保 agent 已初始化（异步）
        if self.agent is None:
            await self._ensure_checkpointer()

        config = {"configurable": {"thread_id": session_id}}

        async for chunk in self.agent.astream(
                {"messages": [{"role": "user", "content": query}]},
                stream_mode="values",
                config=config,
                context={"report": False, "token": token}
        ):
            if "messages" in chunk and chunk["messages"]:
                latest_message = chunk["messages"][-1]
                if hasattr(latest_message, 'content') and latest_message.content:
                    yield latest_message.content.strip() + "\n"

    async def close(self):
        """异步关闭数据库连接"""
        if self._saver_context:
            await self._saver_context.close()
        if self.checkpointer and hasattr(self.checkpointer, 'close'):
            await self.checkpointer.close()
