"""
文档查询工具
方法：+execute()
作用：查询文档详情、权限、发布状态、内容信息
实现：传入文档 ID → 请求后端文档接口 → 整理文本结果
"""
from langchain.tools import tool

from model.factory import chat_model
from utils.base_api_tool import BaseApiTool
from utils.base_api_tool1 import BaseApiTool1
from utils.config_handler import tools_conf


@tool
def document_info_query(document_id: int) -> str:
    """
    根据文档ID查询指定文档的完整信息。

    该工具用于获取文档的详细元数据（如文档名称、作者、所属团队、状态、可见范围、统计数据等）以及文档的实际内容。支持从本地文件、URL或对象存储中读取文档内容，适用于需要展示文档详情或进行文档内容分析的场景。

    使用场景：
    - 用户询问某篇文档的具体信息或内容时
    - 需要统计文档的查看、点赞、收藏数据时
    - 对文档内容进行摘要、总结或进一步分析时

    :param document_id: 需要查询的文档ID，通常是数字格式的唯一标识符
    :return: 返回包含文档元数据和文档内容的格式化文本信息，如果文档不存在则返回相应提示
    """
    try:
        # 请求Java后端接口获取原始数据
        api = BaseApiTool()
        resp_data = api._get(
            url=tools_conf['java_document_api_url'],
            params={"id": document_id},
            timeout=5
        )

        # 接口状态判断
        if resp_data.get("code") != 1:
            return f"文档信息查询失败：{resp_data.get('msg', '未知错误')}"

        document_data = resp_data.get("data", {})

        # 提取所有字段
        fields = {
            "title": "",
            "content": "",
            "statusName": "",
            "auditStatusName": "",
            "visibilityName": "",
            "authorId": "",
            "teamId": "",
            "viewCount": 0,
            "likeCount": 0,
            "favoriteCount": 0,
            "teamName": "",
            "authorName": "",
            "createdAt": "",
            "updatedAt": ""
        }

        doc_info = {key: document_data.get(key, default) for key, default in fields.items()}

        # 使用时：
        # doc_info["title"], doc_info["authorName"] 等

        result_text = (f"文档名称：{doc_info['title']}\n"
                       f"作者：{doc_info['authorName']}\n"
                       f"所属团队：{doc_info['teamName']}\n"
                       f"文档状态：{doc_info['statusName']}\n"
                       f"审核状态：{doc_info['auditStatusName']}\n"
                       f"可见范围：{doc_info['visibilityName']}\n"
                       f"查看次数：{doc_info['viewCount']}\n"
                       f"点赞次数：{doc_info['likeCount']}\n"
                       f"收藏次数：{doc_info['favoriteCount']}\n"
                       f"创建时间：{doc_info['createdAt']}\n"
                       f"更新时间：{doc_info['updatedAt']}\n"
                       f"文档地址：{doc_info['content']}\n"
                       f"【{doc_info['title']}】文档信息详情：\n")

        doc_path = doc_info['content']  # 例如："/uploads/docs/xxx.pdf" 或 "./docs/example.txt"

        # 读取文本文件
        if doc_path.endswith('.txt') or doc_path.endswith('.md'):
            try:
                with open(doc_path, 'r', encoding='utf-8') as f:
                    doc_content = f.read()
                    result_text = result_text + doc_content[:500]
                    print(f"文档内容：{doc_content[:500]}...")  # 只显示前500字
            except FileNotFoundError:
                print(f"文件不存在：{doc_path}")
            except Exception as e:
                print(f"读取文件失败：{e}")

        return result_text.strip()

    except Exception as e:
        return f"调用文档查询接口异常：{str(e)}"


if __name__ == "__main__":
    # 设置 token
    user_token = "eyJhbGciOiJIUzI1NiJ9.eyJpZGVudGl0eSI6MCwidXNlcklkIjoxLCJ1c2VybmFtZSI6InpoYW5nc2FuIiwiZXhwIjoxNzc5MDE4MDI0fQ.6RWmBRyC1svZP-49ys8I94SW7Pu8aQqM6d4bYsdzF_c"
    BaseApiTool1.set_token(user_token)

    # 绑定工具到模型
    llm_with_tools = chat_model.bind_tools([document_info_query])

    # 用户问题
    user_query = "帮我查一下文档ID为1的文档数据信息"

    # 调用模型
    response = llm_with_tools.invoke(user_query)

    print(response)

    # 处理工具调用
    if response.tool_calls:
        for tool_call in response.tool_calls:
            if tool_call["name"] == "document_info_query":
                # 执行工具
                result = document_info_query.invoke(tool_call["args"])
                print(f"查询结果：\n{result}")
    else:
        print(response.content)
