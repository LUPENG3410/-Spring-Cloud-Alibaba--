"""
畅行租车 AI Agent - FastAPI 主入口

提供以下 API 接口：
    GET  /health      - 健康检查（含 LLM/SSE/RAG 运行状态）
    GET  /hot-cars    - 获取热门车型排行（前端首页使用）
    POST /chat        - 普通聊天接口（一次性返回完整回复）
    POST /chat/stream - 流式聊天接口（SSE 逐字推送，带连接数限流）
    GET  /rag/faq     - 查看知识库条目
    POST /rag/faq     - 向知识库动态追加标准问答
"""
import json
from contextlib import asynccontextmanager

from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from fastapi.responses import JSONResponse
from sse_starlette.sse import EventSourceResponse

import config
from agent import agent_service
from limits import RateLimitedError, SSEConnectionLimiter
from models import ChatRequest, FAQItem
from rag import rag_service

# SSE 连接数限流器（全局 + 单用户）
sse_limiter = SSEConnectionLimiter(config.SSE_MAX_TOTAL, config.SSE_MAX_PER_USER)


@asynccontextmanager
async def lifespan(app: FastAPI):
    """启动时预热 RAG 知识库，关闭时优雅释放资源。"""
    print(f"畅行租车 AI Agent 启动于 http://localhost:{config.AGENT_PORT}")
    await rag_service.ensure_built()
    print(f"RAG 知识库已就绪，共 {rag_service.stats()['documents']} 条")
    yield
    await agent_service.http.aclose()
    await agent_service.data_service.close()
    rag_service.persist()


# 创建 FastAPI 应用实例
app = FastAPI(
    title="畅行租车 AI Agent",
    description="智能客服 AI Agent 服务（LangChain + RAG）",
    version="2.0.0",
    lifespan=lifespan,
)

# 配置 CORS 跨域中间件，允许前端页面直接调用 Agent API
app.add_middleware(
    CORSMiddleware,
    allow_origins=config.CORS_ORIGINS,
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)


@app.get("/health")
async def health():
    """健康检查 + 运行状态（LLM 并发、SSE 连接、RAG 规模）。"""
    return {
        "status": "ok",
        "service": "changxing-agent",
        "llm": {
            "active": agent_service.llm_limiter.active,
            "waiting": agent_service.llm_limiter.waiting,
        },
        "sse": {"total": sse_limiter.total},
        "rag": rag_service.stats(),
    }


@app.get("/hot-cars")
async def hot_cars():
    """代理转发后端 /cars/hot，供前端首页热门车型区块使用。"""
    try:
        import httpx

        async with httpx.AsyncClient(
            base_url=config.BACKEND_API_URL, timeout=10.0
        ) as client:
            resp = await client.get("/cars/hot")
            if resp.status_code == 200:
                return resp.json()
            return {"code": 500, "data": []}
    except Exception as e:
        return {"code": 500, "data": [], "message": str(e)}


@app.post("/chat")
async def chat(req: ChatRequest):
    """普通聊天接口 - 一次性返回完整回复。"""
    reply = await agent_service.chat(req)
    return {"code": 200, "data": {"content": reply}}


@app.post("/chat/stream")
async def chat_stream(req: ChatRequest):
    """流式聊天接口 - SSE 逐字推送，超出连接数上限时快速失败返回 429。"""
    user_key = req.conversation_id
    accepted = await sse_limiter.try_acquire(user_key)
    if not accepted:
        return JSONResponse(
            status_code=429,
            content={"code": 429, "data": {"message": "当前连接数已达上限，请稍后重试"}},
        )

    async def event_generator():
        try:
            yield {"event": "start", "data": json.dumps({"message_id": 0})}
            async for delta in agent_service.chat_stream(req):
                yield {"event": "delta", "data": json.dumps({"content": delta})}
            yield {"event": "done", "data": json.dumps({"message_id": 0})}
        except RateLimitedError as e:
            yield {"event": "error", "data": json.dumps({"message": str(e)})}
        except Exception as e:
            yield {"event": "error", "data": json.dumps({"message": str(e)})}
        finally:
            await sse_limiter.release(user_key)

    return EventSourceResponse(event_generator())


@app.get("/rag/faq")
async def list_faq():
    """查看知识库全部条目。"""
    items = await rag_service.all_items()
    return {"code": 200, "data": {"total": len(items), "items": items}}


@app.post("/rag/faq")
async def add_faq(item: FAQItem):
    """向知识库动态追加标准问答（运营可无代码扩容 FAQ）。"""
    await rag_service.remember(item.question, item.answer, force=True)
    return {"code": 200, "data": {"message": "已加入知识库"}}


if __name__ == "__main__":
    import uvicorn

    uvicorn.run("main:app", host="0.0.0.0", port=config.AGENT_PORT, reload=True)