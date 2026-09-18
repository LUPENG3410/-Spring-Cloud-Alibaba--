"""RAG 知识库 —— 基于 LangChain Retriever 的 FAQ 检索与相似问复用。

对应高并发优化方案第 5.2 节第 1、2 点：
  1. 高频问题缓存：命中 FAQ 直接返回标准答案，不再调用 LLM；
  2. 语义相似问复用：向量检索近邻问题，复用已有回答。

并支持把对话中学到的新问答持久化到本地，形成自增的知识库。
"""
import asyncio
import json
import os
from dataclasses import dataclass
from typing import Optional

import numpy as np
from langchain_core.documents import Document
from langchain_core.retrievers import BaseRetriever
from pydantic import PrivateAttr

import config
from cache import TTLCache
from embeddings import embedding_provider


@dataclass
class Hit:
    question: str
    answer: str
    score: float
    doc: Document


def _cosine_sim(query: np.ndarray, matrix: np.ndarray) -> np.ndarray:
    """query 与 matrix 每一行（均已归一化）的余弦相似度。"""
    denom = np.linalg.norm(matrix, axis=1) * max(float(np.linalg.norm(query)), 1e-9) + 1e-9
    return matrix @ query / denom


class FAQRetriever(BaseRetriever):
    """把内存 FAQ 向量库包装为 LangChain Retriever。"""

    _store: "RAGStore" = PrivateAttr()

    def __init__(self, store: "RAGStore"):
        super().__init__()
        self._store = store

    def _get_relevant_documents(self, query, *, run_manager=None):
        return [h.doc for h in self._store.search_sync(query)]

    async def _aget_relevant_documents(self, query, *, run_manager=None):
        return [h.doc for h in await self._store.search(query)]


class RAGStore:
    """向量检索核心：维护 FAQ 文档与向量。"""

    def __init__(self):
        self._docs: list[Document] = []
        self._vectors: Optional[np.ndarray] = None
        self._learned: list[dict] = []
        self._built = False
        self._lock = asyncio.Lock()
        self._load()

    def _load(self):
        from faq_data import SEED_FAQS

        seed = list(SEED_FAQS)
        extra: list[dict] = []
        if config.FAQ_KB_PATH and os.path.exists(config.FAQ_KB_PATH):
            try:
                with open(config.FAQ_KB_PATH, "r", encoding="utf-8") as f:
                    loaded = json.load(f)
                extra = list(loaded) if isinstance(loaded, list) else []
            except Exception:
                extra = []
        for item in seed + extra:
            self._docs.append(self._to_doc(item))

        if os.path.exists(config.FAQ_LEARNED_PATH):
            try:
                with open(config.FAQ_LEARNED_PATH, "r", encoding="utf-8") as f:
                    self._learned = json.load(f)
                for item in self._learned:
                    self._docs.append(self._to_doc(item))
            except Exception:
                self._learned = []

    @staticmethod
    def _to_doc(item: dict) -> Document:
        return Document(
            page_content=item["answer"],
            metadata={"question": item["question"], "source": item.get("source", "faq")},
        )

    async def ensure_built(self):
        if self._built:
            return
        async with self._lock:
            if self._built:
                return
            if self._docs:
                vecs = await embedding_provider.embed_documents(
                    [d.metadata["question"] for d in self._docs]
                )
                self._vectors = np.asarray(vecs, dtype=np.float32)
            else:
                self._vectors = np.zeros((0, config.EMBEDDING_DIM), dtype=np.float32)
            self._built = True

    async def search(self, query: str, top_k: int | None = None) -> list[Hit]:
        await self.ensure_built()
        top_k = top_k or config.RAG_TOP_K
        if self._vectors is None or self._vectors.shape[0] == 0:
            return []
        q = np.asarray(await embedding_provider.embed_query(query), dtype=np.float32)
        return self._rank(q, top_k)

    def search_sync(self, query: str, top_k: int | None = None) -> list[Hit]:
        """同步检索（哈希嵌入），供 LangChain Retriever 的同步 invoke 使用。"""
        top_k = top_k or config.RAG_TOP_K
        if self._vectors is None or self._vectors.shape[0] == 0:
            return []
        if self._vectors.shape[1] != config.EMBEDDING_DIM:
            return []
        q = np.asarray(
            embedding_provider.hash_embed(query, config.EMBEDDING_DIM), dtype=np.float32
        )
        return self._rank(q, top_k)

    def _rank(self, q: np.ndarray, top_k: int) -> list[Hit]:
        scores = _cosine_sim(q, self._vectors)
        idxs = np.argsort(-scores)[:top_k]
        hits = []
        for i in idxs:
            d = self._docs[int(i)]
            hits.append(Hit(d.metadata["question"], d.page_content, float(scores[i]), d))
        return hits

    async def remember(self, question: str, answer: str, force: bool = False) -> bool:
        question = question.strip()
        answer = answer.strip()
        if not question or not answer:
            return False
        if not force:
            # 太短的问答跳过，并去重（相似度达标视为已存在）
            if len(question) < 4 or len(answer) < 20:
                return False
            hits = await self.search(question, top_k=1)
            if hits and hits[0].score >= config.RAG_SIMILARITY_THRESHOLD:
                return False

        doc = self._to_doc({"question": question, "answer": answer, "source": "learned"})
        vec = np.asarray([await embedding_provider.embed_query(question)], dtype=np.float32)
        async with self._lock:
            self._docs.append(doc)
            if self._vectors is None or self._vectors.shape[0] == 0:
                self._vectors = vec
            else:
                self._vectors = np.vstack([self._vectors, vec])
            self._learned.append({"question": question, "answer": answer})
            if len(self._learned) > config.FAQ_MAX_LEARNED:
                self._learned = self._learned[-config.FAQ_MAX_LEARNED:]
            self._persist_learned()
        return True

    def _persist_learned(self):
        path = config.FAQ_LEARNED_PATH
        if not path:
            return
        try:
            directory = os.path.dirname(path) or "."
            os.makedirs(directory, exist_ok=True)
            with open(path, "w", encoding="utf-8") as f:
                json.dump(self._learned, f, ensure_ascii=False, indent=2)
        except Exception:
            pass

    @property
    def learned(self) -> list[dict]:
        return self._learned

    @property
    def docs(self) -> list[Document]:
        return self._docs


class RAGService:
    """对外门面：缓存命中 / 知识上下文检索 / 学习记忆。"""

    def __init__(self):
        self.store = RAGStore()
        self.retriever = FAQRetriever(self.store)
        self._answer_cache = TTLCache(max_size=512, ttl=config.FAQ_CACHE_TTL)

    async def ensure_built(self):
        await self.store.ensure_built()

    async def answer_from_cache(self, question: str) -> Optional[str]:
        """高频问题缓存 + 相似问复用：命中则直接返回答案，不调用 LLM。"""
        key = question.strip()
        if not key:
            return None
        hit = self._answer_cache.get(key)
        if hit:
            return hit
        hits = await self.store.search(key, top_k=1)
        if hits and hits[0].score >= config.RAG_SIMILARITY_THRESHOLD:
            self._answer_cache.set(key, hits[0].answer)
            return hits[0].answer
        return None

    async def retrieve_context(self, question: str) -> list[Hit]:
        """检索相关 FAQ 作为上下文注入 Prompt（未命中标准答案时）。"""
        hits = await self.store.search(question, top_k=config.RAG_TOP_K)
        return [h for h in hits if h.score >= config.RAG_CONTEXT_THRESHOLD]

    async def remember(self, question: str, answer: str, force: bool = False) -> bool:
        return await self.store.remember(question, answer, force=force)

    async def all_items(self) -> list[dict]:
        await self.store.ensure_built()
        return [
            {"question": d.metadata["question"], "answer": d.page_content,
             "source": d.metadata.get("source", "faq")}
            for d in self.store.docs
        ]

    def persist(self):
        self.store._persist_learned()

    def stats(self) -> dict:
        return {"documents": len(self.store.docs), "learned": len(self.store.learned)}


rag_service = RAGService()