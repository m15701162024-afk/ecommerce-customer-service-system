"""
数据库模型定义
包含: ChatSession, ChatMessage, Knowledge
"""
from sqlalchemy import Column, Integer, String, Text, DateTime, Boolean, Enum, ForeignKey, Index
from sqlalchemy.orm import relationship
from sqlalchemy.sql import func
from datetime import datetime
import enum

from app.database import Base


class SessionStatus(str, enum.Enum):
    """会话状态枚举"""
    ACTIVE = "active"        # 活跃中
    CLOSED = "closed"        # 已关闭
    PENDING = "pending"      # 等待人工
    TRANSFERRED = "transferred"  # 已转人工


class MessageRole(str, enum.Enum):
    """消息角色枚举"""
    USER = "user"            # 用户消息
    ASSISTANT = "assistant"  # AI回复
    SYSTEM = "system"        # 系统消息


class KnowledgeCategory(str, enum.Enum):
    """知识库分类枚举"""
    PRODUCT = "product"      # 产品相关
    ORDER = "order"          # 订单相关
    SHIPPING = "shipping"    # 物流相关
    REFUND = "refund"        # 退款相关
    COUPON = "coupon"        # 优惠相关
    FAQ = "faq"              # 常见问题
    OTHER = "other"          # 其他


class AuditStatus(str, enum.Enum):
    """审核状态枚举"""
    PENDING = "pending"      # 待审核
    APPROVED = "approved"    # 已通过
    REJECTED = "rejected"    # 已拒绝


class ChatSession(Base):
    """聊天会话表"""
    __tablename__ = "chat_sessions"
    
    id = Column(Integer, primary_key=True, autoincrement=True, comment="会话ID")
    session_id = Column(String(64), unique=True, nullable=False, index=True, comment="会话唯一标识")
    buyer_id = Column(String(64), nullable=False, index=True, comment="买家ID")
    platform = Column(String(32), nullable=False, comment="平台来源: taobao/jd/pdd等")
    shop_id = Column(Integer, nullable=True, comment="店铺ID")
    
    status = Column(
        Enum(SessionStatus), 
        default=SessionStatus.ACTIVE, 
        comment="会话状态"
    )
    
    # 客户信息
    buyer_name = Column(String(128), nullable=True, comment="买家昵称")
    buyer_avatar = Column(String(512), nullable=True, comment="买家头像")
    
    # 会话统计
    message_count = Column(Integer, default=0, comment="消息数量")
    
    # 最后活跃时间
    last_message_at = Column(DateTime, nullable=True, comment="最后消息时间")
    last_message_preview = Column(String(500), nullable=True, comment="最后消息预览")
    
    # 人工客服相关
    human_agent_id = Column(Integer, nullable=True, comment="人工客服ID")
    transferred_at = Column(DateTime, nullable=True, comment="转人工时间")
    
    # 评价相关
    rating = Column(Integer, nullable=True, comment="会话评分 1-5")
    feedback = Column(Text, nullable=True, comment="用户反馈")
    
    # 时间戳
    created_at = Column(DateTime, default=func.now(), comment="创建时间")
    updated_at = Column(DateTime, default=func.now(), onupdate=func.now(), comment="更新时间")
    closed_at = Column(DateTime, nullable=True, comment="关闭时间")
    
    # 关联消息
    messages = relationship("ChatMessage", back_populates="session", cascade="all, delete-orphan")
    
    __table_args__ = (
        Index("idx_buyer_platform", "buyer_id", "platform"),
        Index("idx_status_created", "status", "created_at"),
    )
    
    def __repr__(self):
        return f"<ChatSession(session_id={self.session_id}, buyer_id={self.buyer_id}, status={self.status})>"


class ChatMessage(Base):
    """聊天消息表"""
    __tablename__ = "chat_messages"
    
    id = Column(Integer, primary_key=True, autoincrement=True, comment="消息ID")
    session_id = Column(String(64), ForeignKey("chat_sessions.session_id"), nullable=False, index=True, comment="会话ID")
    
    # 消息内容
    role = Column(Enum(MessageRole), nullable=False, comment="消息角色: user/assistant/system")
    content = Column(Text, nullable=False, comment="消息内容")
    
    # AI分析结果
    intent = Column(String(64), nullable=True, comment="识别意图")
    confidence = Column(Integer, nullable=True, comment="置信度(百分比)")
    entities = Column(Text, nullable=True, comment="提取的实体(JSON)")
    
    # 消息元数据
    message_type = Column(String(32), default="text", comment="消息类型: text/image/product")
    attachment_url = Column(String(512), nullable=True, comment="附件URL")
    
    # 关联的知识库条目
    knowledge_id = Column(Integer, nullable=True, comment="匹配的知识库ID")
    
    # 是否需要人工
    need_human = Column(Boolean, default=False, comment="是否需要人工处理")
    
    # 时间戳
    created_at = Column(DateTime, default=func.now(), index=True, comment="创建时间")
    
    # 关联会话
    session = relationship("ChatSession", back_populates="messages")
    
    __table_args__ = (
        Index("idx_session_created", "session_id", "created_at"),
    )
    
    def __repr__(self):
        return f"<ChatMessage(id={self.id}, session_id={self.session_id}, role={self.role})>"


class Knowledge(Base):
    """知识库表"""
    __tablename__ = "knowledge"
    
    id = Column(Integer, primary_key=True, autoincrement=True, comment="知识ID")
    
    # 分类和内容
    category = Column(Enum(KnowledgeCategory), nullable=False, index=True, comment="知识分类")
    question = Column(String(500), nullable=False, comment="问题/关键词")
    answer = Column(Text, nullable=False, comment="答案内容")
    
    # 关键词(用于匹配)
    keywords = Column(String(1000), nullable=True, comment="关键词列表,逗号分隔")
    
    # 元数据
    title = Column(String(200), nullable=True, comment="标题")
    tags = Column(String(500), nullable=True, comment="标签,逗号分隔")
    
    # 关联信息
    product_id = Column(Integer, nullable=True, comment="关联商品ID")
    shop_id = Column(Integer, nullable=True, comment="所属店铺ID")
    
    # 统计
    use_count = Column(Integer, default=0, comment="使用次数")
    helpful_count = Column(Integer, default=0, comment="有帮助次数")
    
    # 状态
    is_active = Column(Boolean, default=True, index=True, comment="是否启用")
    
    # 优先级(数字越大优先级越高)
    priority = Column(Integer, default=0, comment="优先级")
    
    # 审核相关
    audit_status = Column(
        Enum(AuditStatus), 
        default=AuditStatus.PENDING, 
        index=True,
        comment="审核状态"
    )
    audit_comment = Column(Text, nullable=True, comment="审核意见")
    auditor_id = Column(Integer, nullable=True, comment="审核人ID")
    audited_at = Column(DateTime, nullable=True, comment="审核时间")
    
    # 时间戳
    created_at = Column(DateTime, default=func.now(), comment="创建时间")
    updated_at = Column(DateTime, default=func.now(), onupdate=func.now(), comment="更新时间")
    created_by = Column(Integer, nullable=True, comment="创建人ID")
    
    __table_args__ = (
        Index("idx_category_active", "category", "is_active"),
        Index("idx_shop_active", "shop_id", "is_active"),
    )
    
    def __repr__(self):
        return f"<Knowledge(id={self.id}, category={self.category}, question={self.question[:30]}...)>"
    
    def to_dict(self) -> dict:
        """转换为字典(用于AI服务)"""
        return {
            "id": self.id,
            "category": self.category.value if self.category else None,
            "question": self.question,
            "answer": self.answer,
            "keywords": self.keywords.split(",") if self.keywords else []
        }