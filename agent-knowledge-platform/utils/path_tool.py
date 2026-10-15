# 路径工具
"""
为整个工程提供统一的绝对路径
"""
import os


def get_project_root() -> str:
    """
    获取工程所在的根目录
    :return: 字符串根目录
    """
    # 当前文件夹的绝对路径
    current_dir = os.path.dirname(__file__)
    # 获取当前工程的根目录
    project_root = os.path.dirname(current_dir)

    return project_root


def get_abs_path(relative_path: str) -> str:
    """
    传递相对路径：得到绝对路径
    :param relative_path:
    :return:
    """
    project_root = get_project_root()
    return os.path.join(project_root, relative_path)


def get_previous_path(abs_path) -> str:
    """
    传递绝对路径，得到绝对路径的目录
    :param abs_path:
    :return:
    """
    current_dir = os.path.dirname(__file__)
    return current_dir


if __name__ == '__main__':
    print(get_abs_path(relative_path="utils/path_tool.py"))
    print(get_previous_path(abs_path=get_abs_path(relative_path="utils/path_tool.py")))
