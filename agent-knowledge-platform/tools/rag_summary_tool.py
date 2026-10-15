from langchain.tools import tool

from rag.rag_service import RagSummarizeService


@tool
def rag_summarize(query: str) -> str:
    """从企业知识库（向量数据库）检索与问题相关的内容并生成总结。

    使用场景：
    - 用户询问公司制度、流程、规范、知识、方法（如"团队如何协作"、"报销流程"、"考勤规定"）
    - 用户希望基于知识库中的已有内容来回答问题
    - 用户提到"根据知识库"、"系统中"、"资料中"等关键词

    不使用场景：
    - 用户需要查询具体团队信息（应使用 team_info_query）
    - 用户需要查看具体文档详情（应使用 document_info_query）

    Args:
        query: 核心关键词或问题，2-5个词，如"团队协作规范"、"报销流程"

    Returns:
        检索到的知识总结文本；若知识库中无相关内容则返回空字符串。
    """
    return RagSummarizeService().rag_summarize(query)