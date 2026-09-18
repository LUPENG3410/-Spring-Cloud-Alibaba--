"""轻量 TTL + LRU 内存缓存，避免引入额外依赖。"""
import time
from collections import OrderedDict
from threading import Lock


class TTLCache:
    """带过期时间的 LRU 缓存。"""

    def __init__(self, max_size: int = 512, ttl: float = 60.0):
        self.max_size = max(1, max_size)
        self.ttl = ttl
        self._data: "OrderedDict[object, tuple]" = OrderedDict()
        self._lock = Lock()

    def get(self, key):
        with self._lock:
            self._evict()
            if key not in self._data:
                return None
            value, expire_at = self._data.pop(key)
            self._data[key] = (value, expire_at)  # 触达即移动到末尾，标记为最近使用
            return value

    def set(self, key, value, ttl: float | None = None):
        with self._lock:
            self._evict()
            expire_at = time.time() + (ttl if ttl is not None else self.ttl)
            self._data[key] = (value, expire_at)
            self._data.move_to_end(key)
            while len(self._data) > self.max_size:
                self._data.popitem(last=False)

    def _evict(self):
        now = time.time()
        expired = [k for k, (_, exp) in self._data.items() if exp <= now]
        for k in expired:
            del self._data[k]

    def __len__(self):
        with self._lock:
            return len(self._data)