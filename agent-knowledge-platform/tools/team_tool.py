"""
2.团队工具
"""
from langchain.tools import tool

from model.factory import chat_model
from utils.base_api_tool import BaseApiTool
from utils.config_handler import tools_conf
from utils.base_api_tool1 import BaseApiTool1


@tool
def team_info_query(team_id: int) -> str:
    """
    根据团队ID查询团队详细信息，返回值包括团队ID，团队名，团队队长ID，团队队长账号，团队成员人数，团队状态描述，团队创建时间，以及团队内成员列表。

    Args:
        team_id: 需要查询的团队唯一ID
    """
    try:
        # 请求Java后端接口获取原始数据
        api = BaseApiTool()
        resp_data = api._get(
            url=tools_conf['java_team_api_url'],
            params={"id": team_id},
            timeout=5
        )

        # 接口状态判断
        if resp_data.get("code") != 1:
            return f"团队信息查询失败：{resp_data.get('msg', '未知错误')}"

        team_data = resp_data.get("data", {})
        team_name = team_data.get("name", "未知团队")
        team_owner_name = team_data.get("ownerUserName", "未知")
        team_member_count = team_data.get("memberCount", "未知")
        team_status_name = team_data.get("statsName", "未知")
        team_created_at = team_data.get("createdAt", "未知")
        member_list = team_data.get("members", [])

        # 格式化拼接为自然语言
        result_text = (f"团队名称：{team_name}\n团队队长：{team_owner_name}\n团队人数：{team_member_count}\n"
                       f"团队状态：{team_status_name}\n团队创建时间：{team_created_at}\n【{team_name}】团队信息详情：\n")

        # 拼接成员+权限信息
        result_text += "团队成员及权限：\n"
        for member in member_list:
            username = member.get("username", "")
            role = member.get("role", "普通成员")
            result_text += f"- {username}，身份：{role}\n"

        return result_text.strip()

    except Exception as e:
        return f"调用团队查询接口异常：{str(e)}"


if __name__ == "__main__":
    # 设置 token
    user_token = "eyJhbGciOiJIUzI1NiJ9.eyJpZGVudGl0eSI6MSwidXNlcklkIjoyLCJ1c2VybmFtZSI6Imxpc2kiLCJleHAiOjE3Nzg5NDYzODJ9.CifNy9DSUhriXbD16vve4dGwj7wMIZ725nJJvi8DR1Y"
    BaseApiTool1.set_token(user_token)

    # 绑定工具到模型
    llm_with_tools = chat_model.bind_tools([team_info_query])

    # 用户问题
    user_query = "帮我查一下团队ID为1的团队信息，并且简单总结一下团队情况"

    # 调用模型
    response = llm_with_tools.invoke(user_query)

    print(response)

    # 处理工具调用
    if response.tool_calls:
        for tool_call in response.tool_calls:
            if tool_call["name"] == "team_info_query":
                # 执行工具
                result = team_info_query.invoke(tool_call["args"])
                print(f"查询结果：\n{result}")
    else:
        print(response.content)