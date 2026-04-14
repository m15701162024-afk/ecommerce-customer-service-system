from pydantic import BaseModel, Field
from typing import Optional, Generic, TypeVar, List
from datetime import datetime
from enum import Enum

T = TypeVar("T")


class Response(BaseModel, Generic[T]):
    code: int = 200
    message: str = "success"
    data: Optional[T] = None
    timestamp: int
    
    class Config:
        from_attributes = True


class PageData(BaseModel, Generic[T]):
    list: List[T]
    total: int
    page_num: int
    page_size: int
    pages: int


class ErrorResponse(BaseModel):
    code: int
    message: str
    detail: Optional[str] = None


# ============ Chat Schemas ============

class ChatMessageCreate(BaseModel):
    """发送消息请求"""
    session_id: str = Field(..., description="会话ID")
    message: str = Field(..., description="用户消息")
    buyer_id: str = Field(..., description="买家ID")
    platform: str = Field(default="taobao", description="平台来源")
    buyer_name: Optional[str] = Field(None, description="买家昵称")
    shop_id: Optional[int] = Field(None, description="店铺ID")


class ChatReplyData(BaseModel):
    """聊天回复数据"""
    session_id: str
    reply: str
    intent: Optional[str] = None
    confidence: Optional[float] = None
    need_human: bool = False


class ChatSessionData(BaseModel):
    """会话信息"""
    session_id: str
    buyer_id: str
    platform: str
    status: str
    message_count: int
    created_at: datetime
    last_message_at: Optional[datetime] = None
    
    class Config:
        from_attributes = True


class ChatMessageData(BaseModel):
    """消息数据"""
    id: int
    session_id: str
    role: str
    content: str
    intent: Optional[str] = None
    confidence: Optional[float] = None
    created_at: datetime
    
    class Config:
        from_attributes = True


class ChatHistoryData(BaseModel):
    """聊天历史数据"""
    session_id: str
    status: str
    messages: List[ChatMessageData]
    total: int


# ============ Knowledge Schemas ============

class KnowledgeCreate(BaseModel):
    """创建知识条目"""
    category: str = Field(..., description="分类: product/order/shipping/refund/coupon/faq/other")
    question: str = Field(..., description="问题/关键词")
    answer: str = Field(..., description="答案内容")
    keywords: Optional[List[str]] = Field(None, description="关键词列表")
    title: Optional[str] = Field(None, description="标题")
    tags: Optional[List[str]] = Field(None, description="标签")
    product_id: Optional[int] = Field(None, description="关联商品ID")
    shop_id: Optional[int] = Field(None, description="店铺ID")
    priority: int = Field(default=0, description="优先级")


class KnowledgeUpdate(BaseModel):
    """更新知识条目"""
    category: Optional[str] = None
    question: Optional[str] = None
    answer: Optional[str] = None
    keywords: Optional[List[str]] = None
    title: Optional[str] = None
    tags: Optional[List[str]] = None
    product_id: Optional[int] = None
    shop_id: Optional[int] = None
    priority: Optional[int] = None
    is_active: Optional[bool] = None


class KnowledgeData(BaseModel):
    """知识条目数据"""
    id: int
    category: str
    question: str
    answer: str
    keywords: Optional[List[str]] = None
    title: Optional[str] = None
    tags: Optional[List[str]] = None
    use_count: int = 0
    helpful_count: int = 0
    is_active: bool = True
    priority: int = 0
    audit_status: str = "pending"
    audit_comment: Optional[str] = None
    auditor_id: Optional[int] = None
    audited_at: Optional[datetime] = None
    created_at: datetime
    updated_at: Optional[datetime] = None
    
    class Config:
        from_attributes = True


class KnowledgeListData(BaseModel):
    """知识库列表数据"""
    items: List[KnowledgeData]
    total: int
    page: int
    size: int


class AuditStatusEnum(str, Enum):
    """审核状态枚举"""
    PENDING = "pending"
    APPROVED = "approved"
    REJECTED = "rejected"


class SubmitAuditRequest(BaseModel):
    """提交审核请求"""
    pass


class AuditRequest(BaseModel):
    """审核操作请求"""
    status: AuditStatusEnum = Field(..., description="审核状态: approved/rejected")
    comment: Optional[str] = Field(None, description="审核意见")
    auditor_id: int = Field(..., description="审核人ID")


class KnowledgeAuditData(BaseModel):
    """知识条目审核数据"""
    id: int
    category: str
    question: str
    answer: str
    keywords: Optional[List[str]] = None
    title: Optional[str] = None
    tags: Optional[List[str]] = None
    audit_status: str
    audit_comment: Optional[str] = None
    auditor_id: Optional[int] = None
    audited_at: Optional[datetime] = None
    created_at: datetime
    created_by: Optional[int] = None
    
    class Config:
        from_attributes = True


class PendingAuditListData(BaseModel):
    """待审核知识列表数据"""
    items: List[KnowledgeAuditData]
    total: int
    page: int
    size: int


# ============ Session Schemas ============

class SessionCreate(BaseModel):
    """创建会话"""
    buyer_id: str = Field(..., description="买家ID")
    platform: str = Field(default="taobao", description="平台")
    buyer_name: Optional[str] = Field(None, description="买家昵称")
    shop_id: Optional[int] = Field(None, description="店铺ID")


class SessionClose(BaseModel):
    """关闭会话"""
    rating: Optional[int] = Field(None, ge=1, le=5, description="评分1-5")
    feedback: Optional[str] = Field(None, description="用户反馈")


class TransferRequest(BaseModel):
    """转人工请求"""
    reason: Optional[str] = Field(None, description="转人工原因")