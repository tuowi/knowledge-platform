# context.py
import contextvars

# 定义一个上下文变量，默认值为 None
_request_token_var = contextvars.ContextVar("request_token", default=None)

def set_request_token(token: str) -> None:
    """在请求开始时设置当前请求的 token"""
    _request_token_var.set(token)

def get_request_token() -> str | None:
    """在工具中获取当前请求的 token"""
    return _request_token_var.get()