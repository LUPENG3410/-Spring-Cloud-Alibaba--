"""向量嵌入模块 —— 为 RAG 提供文本向量化能力。

默认使用确定性的字符 n-gram 哈希嵌入（本地离线、零依赖、可复现），
可选切换为 Ollama 本地嵌入模型（EMBEDDING_MODE=ollama）以获得更强的语义表示。
无论哪种后端，最终向量都会规整到 config.EMBEDDING_DIM 维并做 L2 归一化，
保证检索层维度一致。
"""
import hashlib
import math
import re
from typing import Sequence

import config


def _normalize(vec: list[float]) -> list[float]:
    norm = math.sqrt(sum(x * x for x in vec))
    return [x / norm for x in vec] if norm > 0 else vec


def _fit_dim(vec: list[float], dim: int) -> list[float]:
    """把任意长度向量折叠到指定维度（模叠加 + 归一化）。"""
    if len(vec) == dim:
        return _normalize(vec)
    out = [0.0] * dim
    for i, x in enumerate(vec):
        out[i % dim] += x
    return _normalize(out)


def hash_embed(text: str, dim: int | None = None) -> list[float]:
    """基于字符 1/2/3-gram 特征哈希的确定性嵌入。"""
    dim = dim or config.EMBEDDING_DIM
    t = re.sub(r"\s+", "", text.lower())
    vec = [0.0] * dim
    if not t:
        return vec
    for n in (1, 2, 3):
        for i in range(len(t) - n + 1):
            gram = t[i:i + n]
            h = int(hashlib.md5(gram.encode("utf-8")).hexdigest(), 16)
            vec[h % dim] += 1.0
    return _normalize(vec)


class EmbeddingProvider:
    """统一嵌入接口，按 config.EMBEDDING_MODE 选择后端。"""

    def __init__(self):
        self.mode = config.EMBEDDING_MODE
        self.dim = config.EMBEDDING_DIM

    async def embed_query(self, text: str) -> list[float]:
        return await self._embed(text)

    async def embed_documents(self, texts: Sequence[str]) -> list[list[float]]:
        return [await self._embed(t) for t in texts]

    async def _embed(self, text: str) -> list[float]:
        if self.mode == "ollama":
            v = await self._ollama_embed(text)
            if v is not None:
                return _fit_dim(v, self.dim)
        return hash_embed(text, self.dim)

    async def _ollama_embed(self, text: str) -> list[float] | None:
        """调用 Ollama 原生 /api/embeddings，失败时返回 None 触发降级。"""
        import httpx

        base = config.OLLAMA_EMBED_BASE_URL.rstrip("/")
        native_base = re.sub(r"/v1$", "", base)
        url = f"{native_base}/api/embeddings"
        try:
            async with httpx.AsyncClient(timeout=30.0) as client:
                resp = await client.post(
                    url,
                    json={"model": config.OLLAMA_EMBED_MODEL, "prompt": text},
                )
                if resp.status_code != 200:
                    return None
                emb = resp.json().get("embedding")
                if not emb:
                    return None
                return [float(x) for x in emb]
        except Exception:
            return None


embedding_provider = EmbeddingProvider()