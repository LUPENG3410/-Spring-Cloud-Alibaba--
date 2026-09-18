# 畅行租车 AI Agent（LangChain + RAG）

智能客服 AI Agent 服务，基于 FastAPI + LangChain + RAG，并落地了《高并发优化方案》第 5 节的 AI 客服并发优化。

## 技术栈

- **FastAPI** - Web 框架
- **LangChain** - `ChatOpenAI` 编排 LLM、`ChatPromptTemplate` / `Message` 管理对话、`BaseRetriever` 承载 RAG 检索
- **RAG** - 向量化 FAQ 知识库，实现高频问题缓存与语义相似问复用
- **SSE** - Server-Sent Events 流式响应（带连接数限流）
- **Pydantic** - 数据验证

## 相对原版的关键改进（对应文档第 5 节）

| 痛点 | 优化 |
|------|------|
| 每次问询都直连 LLM，延迟高、成本高 | 高频问题缓存 + 相似问复用（RAG 命中直接返回） |
| 相同问题重复调 LLM | 向量相似度达标复用已有回答 |
| 高峰可能打爆 LLM API | LLM 出口 `asyncio.Semaphore` 并发限流 + 排队上限快速失败 |
| SSE 长连接无限制 | 全局 + 单用户连接数限流，超出返回 429 |
| 长会话 token 膨胀 | 会话上下文瘦身，只取最近 N 条历史 |

## 架构

```
用户消息
  ├─ RAG FAQ 缓存/相似问（命中 → 直接返回，不走 LLM）
  └─ 意图识别 → 后端 API 查询（带短缓存）
        └─ RAG 检索补充上下文
              └─ LangChain ChatOpenAI 生成（流式/一次性，受并发限流）
```

## 快速启动

```bash
cd changxing-agent

python -m venv venv
venv\Scripts\activate     # Windows

pip install -r requirements.txt

cp .env.example .env      # 编辑 .env 填入 OPENAI_API_KEY 等

python main.py
```

服务默认运行在 `http://localhost:5000`。

> 要求 Python >= 3.10。

## 嵌入模式

默认 `EMBEDDING_MODE=hash`：本地确定性字符 n-gram 哈希嵌入，零依赖、可复现，适合先跑通流程。

如需更强语义，可在 `.env` 中设置 `EMBEDDING_MODE=ollama`，并确认 Ollama 已拉取嵌入模型（如 `nomic-embed-text`）。向量会自动折叠到 `EMBEDDING_DIM` 维，检索层维度一致。

## API 接口

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/health` | 健康检查 + LLM/SSE/RAG 运行状态 |
| GET | `/hot-cars` | 热门车型排行 |
| POST | `/chat` | 普通聊天（返回完整回复） |
| POST | `/chat/stream` | SSE 流式聊天（打字机效果） |
| GET | `/rag/faq` | 查看知识库条目 |
| POST | `/rag/faq` | 动态追加标准问答 |

### 请求体

```json
{
  "conversation_id": "conv_123",
  "content": "怎么取消订单",
  "car_id": 1,
  "car_name": "大众朗逸"
}
```

### SSE 流式响应格式

```
event: start
data: {"message_id": 1001}

event: delta
data: {"content": "您好"}

event: done
data: {"message_id": 1001}
```

## 关键配置（见 .env / config.py）

| 配置 | 默认 | 说明 |
|------|------|------|
| `EMBEDDING_MODE` | `hash` | 嵌入后端：`hash` / `ollama` |
| `RAG_SIMILARITY_THRESHOLD` | `0.80` | 相似度达标则复用已有回答、不调 LLM |
| `RAG_TOP_K` | `3` | 检索返回条数 |
| `FAQ_CACHE_TTL` | `3600` | 高频答案缓存 TTL（秒） |
| `DATA_CACHE_TTL` | `60` | 业务数据缓存 TTL（秒） |
| `LLM_MAX_CONCURRENCY` | `5` | LLM 调用并发上限 |
| `LLM_QUEUE_MAX` | `100` | LLM 排队上限，超出快速失败 |
| `MAX_HISTORY_MESSAGES` | `12` | 会话上下文保留的最近消息数 |
| `SSE_MAX_TOTAL` / `SSE_MAX_PER_USER` | `100` / `3` | SSE 连接数限制 |

## 知识库扩展

- 静态扩展：在 `FAQ_KB_PATH` 指向一个 JSON 文件（`[{question, answer}, ...]`）。
- 动态扩展：`POST /rag/faq` 追加标准问答。
- 自动学习：每次 LLM 生成后，符合条件（长度、相似度去重）的问答会写入 `FAQ_LEARNED_PATH`（默认 `data/faq_learned.json`），重启后自动加载。