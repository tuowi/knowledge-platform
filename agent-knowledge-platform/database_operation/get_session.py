# database_operation/get_session.py
import aiosqlite
from langgraph.checkpoint.sqlite.aio import AsyncSqliteSaver


async def get_thread_memory(db_path: str, thread_id: str):
    """获取指定线程的所有检查点记忆"""
    async with aiosqlite.connect(db_path) as conn:
        saver = AsyncSqliteSaver(conn)
        await saver.setup()

        config = {"configurable": {"thread_id": thread_id}}

        # 收集所有 CheckpointTuple 对象
        history = []
        async for checkpoint_tuple in saver.alist(config):
            history.append(checkpoint_tuple)  # 直接保存对象，不要转换

        return history