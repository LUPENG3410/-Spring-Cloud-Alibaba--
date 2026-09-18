"""
数据模型定义 - 使用 Pydantic 定义 Agent API 的请求/响应数据结构
"""
from pydantic import BaseModel


class ChatRequest(BaseModel):
    """
    聊天请求模型 - 前端发送给 Agent 的消息格式

    字段说明：
        conversation_id: 对话会话 ID，用于关联同一轮对话的上下文历史
        content:         用户发送的文本消息内容
        car_id:          可选，当前用户正在查看的车辆 ID（用于上下文感知）
        car_name:        可选，当前用户正在查看的车辆名称（用于上下文感知）
        token:           可选，用户的登录 JWT Token（查询订单/个人信息时需要）
    """
    conversation_id: str
    content: str
    car_id: int | None = None
    car_name: str | None = None
    token: str | None = None


class ChatDelta(BaseModel):
    """
    流式响应增量模型 - SSE 推送的单个数据块格式

    字段说明：
        type:       消息类型标识（start/delta/done/error）
        content:    当前增量的文本内容（type="delta" 时有值）
        message_id: 消息 ID（用于前端关联消息记录）
        error:      错误信息（type="error" 时有值）
    """
    type: str
    content: str = ""
    message_id: int | None = None
    error: str | None = None


class FAQItem(BaseModel):
    """知识库条目 - 用于 /rag/faq 接口动态追加标准问答。"""
    question: str
    answer: str