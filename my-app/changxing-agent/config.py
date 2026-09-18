"""
配置模块 - 从环境变量读取 Agent 服务的所有运行参数
"""
import os
from dotenv import load_dotenv

# 加载 .env 文件中的环境变量
load_dotenv()

# ===== Agent 服务配置 =====
# Agent HTTP 服务监听端口
AGENT_PORT = int(os.getenv("AGENT_PORT", "5000"))
# 后端微服务网关地址，Agent 通过此地址调用后端 API（车辆、订单、门店等）
BACKEND_API_URL = os.getenv("BACKEND_API_URL", "http://localhost:8080/api")

# ===== LLM 大模型配置 =====
# 大模型 API Key（本地 Ollama 默认填 "ollama"，云端 API 填真实 Key）
OPENAI_API_KEY = os.getenv("OPENAI_API_KEY", "ollama")
# 大模型 API 地址（本地 Ollama 地址 / 云端 OpenAI 兼容接口地址）
OPENAI_BASE_URL = os.getenv("OPENAI_BASE_URL", "http://localhost:11434/v1")
# 使用的模型名称（本地 Ollama 模型名 / 云端模型 ID）
OPENAI_MODEL = os.getenv("OPENAI_MODEL", "qwen:1.8b")
# 生成温度与最大生成长度
OPENAI_TEMPERATURE = float(os.getenv("OPENAI_TEMPERATURE", "0.7"))
OPENAI_MAX_TOKENS = int(os.getenv("OPENAI_MAX_TOKENS", "1024"))

# ===== 系统提示词 =====
# Agent 的 system prompt，定义了 AI 客服的角色和行为准则
# 会被注入到每次 LLM 对话的第一条 system 消息中
AGENT_SYSTEM_PROMPT = os.getenv(
    "AGENT_SYSTEM_PROMPT",
    "你是畅行租车的智能客服助手。你可以查询车辆信息、门店信息、用户评价等实时数据来回答用户问题。"
    "回答时请基于查询到的真实数据，不要编造信息。用友好、专业、简洁的语气回答。",
)

# ===== RAG 与知识库配置 =====
# 嵌入模式：hash（默认，本地确定性、零依赖）| ollama（本地语义嵌入，需 Ollama 与嵌入模型）
EMBEDDING_MODE = os.getenv("EMBEDDING_MODE", "hash")
# 向量维度（hash 嵌入的维度，ollama 向量会被折叠到该维度）
EMBEDDING_DIM = int(os.getenv("EMBEDDING_DIM", "512"))
# Ollama 嵌入服务地址与模型（EMBEDDING_MODE=ollama 时生效）
OLLAMA_EMBED_BASE_URL = os.getenv("OLLAMA_EMBED_BASE_URL", "http://localhost:11434")
OLLAMA_EMBED_MODEL = os.getenv("OLLAMA_EMBED_MODEL", "nomic-embed-text")
# 可选：外部 FAQ JSON 文件路径（[{question, answer}, ...]）
FAQ_KB_PATH = os.getenv("FAQ_KB_PATH", "")
# 学习到的问答持久化路径
FAQ_LEARNED_PATH = os.getenv("FAQ_LEARNED_PATH", "data/faq_learned.json")
# 检索返回条数
RAG_TOP_K = int(os.getenv("RAG_TOP_K", "3"))
# 相似度达到该阈值则直接复用已有回答（不调用 LLM）
RAG_SIMILARITY_THRESHOLD = float(os.getenv("RAG_SIMILARITY_THRESHOLD", "0.80"))
# 相似度低于复用阈值但高于此值的 FAQ，作为上下文注入 Prompt
RAG_CONTEXT_THRESHOLD = float(os.getenv("RAG_CONTEXT_THRESHOLD", "0.45"))
# 学习问答的最大保留条数
FAQ_MAX_LEARNED = int(os.getenv("FAQ_MAX_LEARNED", "1000"))

# ===== 缓存配置 =====
# 高频问题答案缓存 TTL（秒）
FAQ_CACHE_TTL = int(os.getenv("FAQ_CACHE_TTL", "3600"))
# 业务数据（车辆列表/门店等）缓存 TTL（秒）
DATA_CACHE_TTL = int(os.getenv("DATA_CACHE_TTL", "60"))
# 业务数据缓存最大条目数
DATA_CACHE_MAX = int(os.getenv("DATA_CACHE_MAX", "512"))

# ===== 并发与限流配置 =====
# LLM 出口并发上限
LLM_MAX_CONCURRENCY = int(os.getenv("LLM_MAX_CONCURRENCY", "5"))
# LLM 排队上限，超出则快速失败
LLM_QUEUE_MAX = int(os.getenv("LLM_QUEUE_MAX", "100"))
# 会话上下文瘦身：只保留最近 N 条历史消息
MAX_HISTORY_MESSAGES = int(os.getenv("MAX_HISTORY_MESSAGES", "12"))
# SSE 长连接全局上限 / 单用户上限
SSE_MAX_TOTAL = int(os.getenv("SSE_MAX_TOTAL", "100"))
SSE_MAX_PER_USER = int(os.getenv("SSE_MAX_PER_USER", "3"))

# ===== CORS 跨域配置 =====
# 允许的前端域名列表，多个用逗号分隔（用于本地开发和生产环境的跨域请求）
CORS_ORIGINS = [
    origin.strip()
    for origin in os.getenv("CORS_ORIGINS", "http://localhost:5173").split(",")
]