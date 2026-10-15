import aiosqlite
from langgraph.checkpoint.sqlite.aio import AsyncSqliteSaver


async def delete_thread_memory(db_path: str , thread_id: str):
    """
    删除指定 SQLite 数据库中的特定线程的所有检查点记忆。

    Args:
        db_path: SQLite 数据库文件路径（例如 "checkpoints.db"）
        thread_id: 要清除的线程 ID
    """
    # 异步连接数据库
    async with aiosqlite.connect(db_path) as conn:
        # 创建异步检查点保存器实例
        saver = AsyncSqliteSaver(conn)
        # 初始化内部表结构（如果尚未创建）
        await saver.setup()
        # 删除该线程的所有记忆
        await saver.adelete_thread(thread_id=thread_id)
