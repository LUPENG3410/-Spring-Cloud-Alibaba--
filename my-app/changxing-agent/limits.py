"""并发与连接数限流 —— 对应高并发优化方案第 5.2 节第 3、4 点。"""
import asyncio


class RateLimitedError(Exception):
    """限流触发的业务异常，用于快速失败。"""


class LLMLimiter:
    """LLM 出口并发限制 + 排队上限。

    - max_concurrency：同时进行的 LLM 调用数（asyncio.Semaphore 限制）；
    - queue_max：允许排队的等待者上限，超过则直接抛 RateLimitedError 快速失败，
      避免高峰把 LLM API 打爆或无限制堆积。
    """

    def __init__(self, max_concurrency: int, queue_max: int):
        self.max_concurrency = max(1, max_concurrency)
        self.queue_max = max(0, queue_max)
        self._sem = asyncio.Semaphore(self.max_concurrency)
        self._waiting = 0
        self._active = 0
        self._lock = asyncio.Lock()

    async def __aenter__(self):
        async with self._lock:
            if self._waiting >= self.queue_max:
                raise RateLimitedError("AI 服务繁忙，请稍后重试")
            self._waiting += 1
        try:
            await self._sem.acquire()
        finally:
            async with self._lock:
                self._waiting -= 1
        self._active += 1
        return self

    async def __aexit__(self, exc_type, exc, tb):
        self._sem.release()
        self._active -= 1
        return False

    @property
    def active(self) -> int:
        return self._active

    @property
    def waiting(self) -> int:
        return self._waiting


class SSEConnectionLimiter:
    """限制 SSE 长连接的总数以及单用户连接数，防止突发长连接占满资源。"""

    def __init__(self, max_total: int, max_per_user: int):
        self.max_total = max(1, max_total)
        self.max_per_user = max(1, max_per_user)
        self._total = 0
        self._per_user: dict[str, int] = {}
        self._lock = asyncio.Lock()

    async def try_acquire(self, user_key: str) -> bool:
        async with self._lock:
            if self._total >= self.max_total:
                return False
            if self._per_user.get(user_key, 0) >= self.max_per_user:
                return False
            self._total += 1
            self._per_user[user_key] = self._per_user.get(user_key, 0) + 1
            return True

    async def release(self, user_key: str):
        async with self._lock:
            self._total = max(0, self._total - 1)
            cnt = self._per_user.get(user_key, 0) - 1
            if cnt <= 0:
                self._per_user.pop(user_key, None)
            else:
                self._per_user[user_key] = cnt

    @property
    def total(self) -> int:
        return self._total