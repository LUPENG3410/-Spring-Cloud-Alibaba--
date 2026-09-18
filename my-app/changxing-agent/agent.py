"""
Agent 核心服务 - 基于 LangChain + RAG 的智能客服编排

对应高并发优化方案第 5 节（AI 客服并发优化），落地要点：
    1. 高频问题缓存 + 语义相似问复用（RAG），命中直接返回、不调用 LLM；
    2. LLM 出口并发限制 + 排队上限（asyncio.Semaphore）；
    3. 会话上下文瘦身（只保留最近 N 条消息）；
    4. SSE 长连接数限制（见 main.py）。

技术栈：LangChain 的 ChatOpenAI / Message / ChatPromptTemplate 负责 LLM 编排，
        RAG 用向量检索（embedding 见 embeddings.py，检索见 rag.py）实现。
"""
import json
from typing import AsyncGenerator, Optional

import httpx
from langchain_core.messages import AIMessage, BaseMessage, HumanMessage
from langchain_core.prompts import ChatPromptTemplate, MessagesPlaceholder
from langchain_openai import ChatOpenAI

import config
from cache import TTLCache
from limits import LLMLimiter
from models import ChatRequest
from rag import Hit, rag_service
from tools import DataService

# 需要鉴权的个性化数据不做短缓存（避免串号/信息过期）
NO_CACHE_INTENTS = {"user_bookings", "user_profile"}


class AgentService:
    """基于 LangChain 编排的智能客服服务。"""

    _PROMPT = ChatPromptTemplate.from_messages(
        [
            ("system", "{system}"),
            MessagesPlaceholder(variable_name="history"),
            ("human", "{input}"),
        ]
    )

    def __init__(self):
        # LangChain 聊天模型 - 通过 OpenAI 兼容接口调用大语言模型
        self.llm = ChatOpenAI(
            api_key=config.OPENAI_API_KEY,
            base_url=config.OPENAI_BASE_URL,
            model=config.OPENAI_MODEL,
            temperature=config.OPENAI_TEMPERATURE,
            max_tokens=config.OPENAI_MAX_TOKENS,
            streaming=True,
        )
        # 后端 HTTP 客户端 - 调用 Spring Cloud 微服务网关
        self.http = httpx.AsyncClient(base_url=config.BACKEND_API_URL, timeout=10.0)
        # 数据查询服务 - 意图识别 + 调用后端公开 API 获取业务数据
        self.data_service = DataService(config.BACKEND_API_URL)
        # LLM 出口限流器（并发限制 + 排队上限）
        self.llm_limiter = LLMLimiter(config.LLM_MAX_CONCURRENCY, config.LLM_QUEUE_MAX)
        # 业务数据短缓存（车辆列表/门店等读多写少的数据）
        self.data_cache = TTLCache(max_size=config.DATA_CACHE_MAX, ttl=config.DATA_CACHE_TTL)

    # ================================================================
    # 数据准备
    # ================================================================

    async def _prepare(self, req: ChatRequest) -> tuple[list[BaseMessage], Optional[str]]:
        """准备一轮对话：返回 (消息列表, 命中的缓存答案)。

        命中缓存时消息列表为空、返回缓存的答案，调用方直接返回、不走 LLM。
        """
        question = req.content.strip()

        # 1) 高频问题缓存 / 相似问复用（RAG 命中直接返回，不调用 LLM）
        cached = await rag_service.answer_from_cache(question)
        if cached is not None:
            return [], cached

        # 2) 意图识别 + 数据查询（带短缓存）
        data_context = await self._data_context(req)

        # 3) RAG 知识上下文（未命中标准答案时，注入相关 FAQ 作为参考）
        rag_context = await rag_service.retrieve_context(question)

        # 4) 会话上下文瘦身：历史只取最近 N 条
        history = await self.get_conversation_history(req.conversation_id)
        history = history[-config.MAX_HISTORY_MESSAGES:]

        system_prompt = await self.build_system_prompt(req, data_context, rag_context)
        messages = self._PROMPT.format_messages(
            system=system_prompt, history=history, input=question
        )
        return messages, None

    async def _data_context(self, req: ChatRequest) -> str:
        """意图识别 + 数据查询，对读多写少的公共数据做短缓存。"""
        intent, params = self.data_service.detect_intent(req.content)
        if intent == "none":
            return ""
        token = req.token or ""
        if intent in NO_CACHE_INTENTS:
            return (await self.data_service.fetch_data(intent, params, token)) or ""

        cache_key = f"{intent}:{json.dumps(params, sort_keys=True, ensure_ascii=False)}"
        cached = self.data_cache.get(cache_key)
        if cached is not None:
            return cached
        data = (await self.data_service.fetch_data(intent, params, token)) or ""
        if data:
            self.data_cache.set(cache_key, data)
        return data

    async def get_conversation_history(self, conversation_id: str) -> list[BaseMessage]:
        """获取最近会话历史，转换为 LangChain 消息（失败时静默降级为空）。"""
        try:
            resp = await self.http.get(f"/service/conversations/{conversation_id}")
            data = resp.json().get("data", {})
            messages = data.get("messages", [])
            return [
                HumanMessage(content=m["content"])
                if m["sender"] == "user"
                else AIMessage(content=m["content"])
                for m in messages
                if m["sender"] in ("user", "agent")
            ]
        except Exception:
            return []

    async def build_system_prompt(
        self,
        req: ChatRequest,
        data_context: str = "",
        rag_context: list[Hit] | None = None,
    ) -> str:
        """组装 system 指令：角色设定 + 车辆上下文 + 业务数据 + RAG 知识。"""
        base = config.AGENT_SYSTEM_PROMPT
        if req.car_id and req.car_name:
            base += f"\n\n当前用户正在咨询车辆：{req.car_name}（ID: {req.car_id}）。"
        if data_context:
            base += (
                "\n\n以下是根据用户问题查询到的真实数据，请基于这些数据回答用户问题，不要编造信息：\n"
                f"{data_context}"
            )
        if rag_context:
            knowledge = "\n".join(
                f"- 问：{h.question}\n  答：{h.answer}" for h in rag_context
            )
            base += (
                "\n\n以下是与用户问题相关的常见问题解答（知识库），可参考其口径回答：\n"
                f"{knowledge}"
            )
        return base

    # ================================================================
    # 对话入口
    # ================================================================

    async def chat(self, req: ChatRequest) -> str:
        """普通聊天 - 一次性返回完整回复。"""
        messages, cached = await self._prepare(req)
        if cached is not None:
            return cached
        async with self.llm_limiter:
            resp = await self.llm.ainvoke(messages)
        answer = self._extract(resp)
        await rag_service.remember(req.content, answer)
        return answer

    async def chat_stream(self, req: ChatRequest) -> AsyncGenerator[str, None]:
        """流式聊天 - 逐 token 返回 LangChain 生成结果。

        命中缓存时一次性 yield 整段答案；否则走 LLM 流式生成，
        并在整个流式期间持有 LLM 限流信号量。
        """
        messages, cached = await self._prepare(req)
        if cached is not None:
            yield cached
            return

        collected: list[str] = []
        async with self.llm_limiter:
            async for chunk in self.llm.astream(messages):
                text = self._extract(chunk)
                if text:
                    collected.append(text)
                    yield text
        await rag_service.remember(req.content, "".join(collected))

    @staticmethod
    def _extract(message) -> str:
        """从 LangChain 消息中提取纯文本内容（兼容 str / 内容块列表）。"""
        content = getattr(message, "content", "")
        if isinstance(content, str):
            return content
        if isinstance(content, list):
            parts = []
            for block in content:
                if isinstance(block, str):
                    parts.append(block)
                elif isinstance(block, dict) and block.get("type") == "text":
                    parts.append(block.get("text", ""))
            return "".join(parts)
        return str(content)


# 模块级别单例，所有接口共享同一个 AgentService 实例
agent_service = AgentService()