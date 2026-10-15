# base_api_tool.py
from typing import Dict, Optional
import requests
from abc import ABC


class BaseApiTool1(ABC):
    """所有 API 调用工具的基础类"""

    _current_token: Optional[str] = None

    @classmethod
    def set_token(cls, token: str):
        cls._current_token = token

    @classmethod
    def get_token(cls) -> Optional[str]:
        return cls._current_token

    @classmethod
    def clear_token(cls):
        cls._current_token = None

    def _request(
            self,
            method: str,
            url: str,
            params: Optional[Dict] = None,
            data: Optional[Dict] = None,
            timeout: int = 10
    ) -> Dict:
        """统一请求方法，自动添加 token"""

        headers = {"Content-Type": "application/json"}

        token = self.get_token()
        if token:
            headers["Authentication"] = token  # 使用正确的 header 名称

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
        return response.json()  # ← 直接返回字典

    def _get(self, url: str, params: Optional[Dict] = None, timeout: int = 10) -> Dict:
        """GET 请求快捷方法"""
        return self._request("GET", url, params=params, timeout=timeout)

    def _post(self, url: str, data: Optional[Dict] = None, timeout: int = 10) -> Dict:
        """POST 请求快捷方法"""
        return self._request("POST", url, data=data, timeout=timeout)