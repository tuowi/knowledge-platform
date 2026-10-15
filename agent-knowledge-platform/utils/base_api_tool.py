# base_api_tool.py
from typing import Dict, Optional
import requests
from .context import get_request_token

class BaseApiTool:
    # 不再需要类变量 _current_token

    def _request(self, method: str, url: str,
                 params: Optional[Dict] = None,
                 data: Optional[Dict] = None,
                 timeout: int = 10) -> Dict:
        headers = {"Content-Type": "application/json"}
        # 从当前请求上下文中获取 token
        token = get_request_token()
        if token:
            headers["Authentication"] = token   # 根据你的后端实际 header 名称调整

        response = requests.request(
            method=method,
            url=url,
            headers=headers,
            params=params,
            json=data,
            timeout=timeout
        )
        if response.status_code == 401:
            raise Exception("认证失败，请重新登录")
        response.raise_for_status()
        return response.json()

    def _get(self, url: str, params: Optional[Dict] = None, timeout: int = 10) -> Dict:
        return self._request("GET", url, params=params, timeout=timeout)

    def _post(self, url: str, data: Optional[Dict] = None, timeout: int = 10) -> Dict:
        return self._request("POST", url, data=data, timeout=timeout)