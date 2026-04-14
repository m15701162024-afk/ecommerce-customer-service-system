"""
聊天接口路由
实现真实的消息处理逻辑
"""
from fastapi import APIRouter, Depends, HTTPException, Query
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy import select, func, and_, desc
from typing import Optional, List
from datetime import datetime
import time
import json
import uuid
from loguru import logger

from app.database import get_db
from app.models import ChatSession, ChatMessage, Knowledge, SessionStatus, MessageRole
from app.schemas.base import (
    Response, ChatMessageCreate, ChatReplyData, 
    ChatHistoryData, ChatMessageData, ChatSessionData,
    SessionCreate, SessionClose, TransferRequest
)
from app.config import get_settings
from app.ai_service import CustomerServiceEngine, ChatContext

router = APIRouter()
settings = get_settings()

# 初始化AI引擎
ai_engine = CustomerServiceEngine(
    ai_api_key=settings.openai_api_key,
    ai_base_url=settings.openai_base_url
)


async def get_or_create_session(
    db: AsyncSession,
    session_id: str,
    buyer_id: str,
    platform: str,
    buyer_name: Optional[str] = None,
    shop_id: Optional[int] = None
) -> ChatSession:
    """获取或创建会话"""
    result = await db.execute(
        select(ChatSession).where(ChatSession.session_id == session_id)
    )
    session = result.scalar_one_or_none()
    
    if not session:
        # 创建新会话
        session = ChatSession(
            session_id=session_id,
            buyer_id=buyer_id,
            platform=platform,
            buyer_name=buyer_name,
            shop_id=shop_id,
            status=SessionStatus.ACTIVE,
            message_count=0
        )
        db.add(session)
        await db.flush()
        logger.info(f"创建新会话: {session_id}")
    
    return session


async def save_message(
    db: AsyncSession,
    session_id: str,
    role: MessageRole,
    content: str,
    intent: Optional[str] = None,
    confidence: Optional[float] = None,
    entities: Optional[dict] = None,
    need_human: bool = False,
    knowledge_id: Optional[int] = None
) -> ChatMessage:
    """保存消息到数据库"""
    message = ChatMessage(
        session_id=session_id,
        role=role,
        content=content,
        intent=intent,
        confidence=int(confidence * 100) if confidence else None,
        entities=json.dumps(entities, ensure_ascii=False) if entities else None,
        need_human=need_human,
        knowledge_id=knowledge_id
    )
    db.add(message)
    return message


async def load_knowledge_for_context(db: AsyncSession, shop_id: Optional[int] = None) -> List[dict]:
    """加载知识库用于上下文"""
    query = select(Knowledge).where(Knowledge.is_active == True).order_by(Knowledge.priority.desc())
    if shop_id:
        query = query.where(
            (Knowledge.shop_id == shop_id) | (Knowledge.shop_id.is_(None))
        )
    
    result = await db.execute(query.limit(100))
    knowledge_items = result.scalars().all()
    return [item.to_dict() for item in knowledge_items]


async def get_chat_history(db: AsyncSession, session_id: str, limit: int = 10) -> List[dict]:
    """获取聊天历史"""
    result = await db.execute(
        select(ChatMessage)
        .where(ChatMessage.session_id == session_id)
        .order_by(desc(ChatMessage.created_at))
        .limit(limit)
    )
    messages = result.scalars().all()
    
    # 按时间正序返回
    history = []
    for msg in reversed(messages):
        history.append({
            "role": msg.role.value if isinstance(msg.role, type(msg.role)) else msg.role,
            "content": msg.content
        })
    return history


@router.post("/send", response_model=Response[ChatReplyData])
async def send_message(
    msg: ChatMessageCreate,
    db: AsyncSession = Depends(get_db)
):
    """
    发送消息
    - 获取/创建会话
    - 保存用户消息
    - AI处理并回复
    - 保存回复消息
    """
    try:
        # 获取或创建会话
        session = await get_or_create_session(
            db=db,
            session_id=msg.session_id,
            buyer_id=msg.buyer_id,
            platform=msg.platform,
            buyer_name=msg.buyer_name,
            shop_id=msg.shop_id
        )
        
        # 检查会话状态
        if session.status == SessionStatus.CLOSED:
            # 重新激活会话
            session.status = SessionStatus.ACTIVE
            session.closed_at = None
        
        # 保存用户消息
        await save_message(
            db=db,
            session_id=msg.session_id,
            role=MessageRole.USER,
            content=msg.message
        )
        
        # 加载知识库
        knowledge_items = await load_knowledge_for_context(db, msg.shop_id)
        ai_engine.load_knowledge(knowledge_items)
        
        # 获取聊天历史作为上下文
        history = await get_chat_history(db, msg.session_id, limit=10)
        
        # 构建上下文
        context = ChatContext(
            session_id=msg.session_id,
            buyer_id=msg.buyer_id,
            platform=msg.platform,
            shop_id=msg.shop_id or 0,
            history=history
        )
        
        # AI处理消息
        result = await ai_engine.process_message(msg.message, context)
        
        reply = result.get("response", "您好，请问有什么可以帮助您的？")
        intent = result.get("intent", "unknown")
        confidence = result.get("confidence", 0.5)
        entities = result.get("entities", {})
        need_human = result.get("need_human", False)
        
        # 保存AI回复
        await save_message(
            db=db,
            session_id=msg.session_id,
            role=MessageRole.ASSISTANT,
            content=reply,
            intent=intent,
            confidence=confidence,
            entities=entities,
            need_human=need_human
        )
        
        # 更新会话统计
        session.message_count += 2  # 用户消息 + AI回复
        session.last_message_at = datetime.now()
        session.last_message_preview = msg.message[:500] if len(msg.message) > 500 else msg.message
        
        if need_human and session.status != SessionStatus.TRANSFERRED:
            session.status = SessionStatus.PENDING
        
        await db.commit()
        
        return Response(
            data=ChatReplyData(
                session_id=msg.session_id,
                reply=reply,
                intent=intent,
                confidence=confidence,
                need_human=need_human
            ),
            timestamp=int(time.time())
        )
        
    except Exception as e:
        logger.error(f"处理消息失败: {e}", exc_info=True)
        await db.rollback()
        raise HTTPException(status_code=500, detail=f"处理消息失败: {str(e)}")


@router.get("/sessions/{session_id}/history", response_model=Response[ChatHistoryData])
async def get_chat_history_api(
    session_id: str,
    limit: int = Query(default=50, ge=1, le=200),
    db: AsyncSession = Depends(get_db)
):
    """
    获取聊天历史
    返回指定会话的消息历史
    """
    try:
        # 获取会话信息
        session_result = await db.execute(
            select(ChatSession).where(ChatSession.session_id == session_id)
        )
        session = session_result.scalar_one_or_none()
        
        if not session:
            raise HTTPException(status_code=404, detail="会话不存在")
        
        # 获取消息列表
        result = await db.execute(
            select(ChatMessage)
            .where(ChatMessage.session_id == session_id)
            .order_by(ChatMessage.created_at.asc())
            .limit(limit)
        )
        messages = result.scalars().all()
        
        # 转换为响应格式
        message_data = [
            ChatMessageData(
                id=msg.id,
                session_id=msg.session_id,
                role=msg.role.value if hasattr(msg.role, 'value') else msg.role,
                content=msg.content,
                intent=msg.intent,
                confidence=msg.confidence / 100 if msg.confidence else None,
                created_at=msg.created_at
            )
            for msg in messages
        ]
        
        return Response(
            data=ChatHistoryData(
                session_id=session_id,
                status=session.status.value if hasattr(session.status, 'value') else session.status,
                messages=message_data,
                total=len(message_data)
            ),
            timestamp=int(time.time())
        )
        
    except HTTPException:
        raise
    except Exception as e:
        logger.error(f"获取聊天历史失败: {e}", exc_info=True)
        raise HTTPException(status_code=500, detail=f"获取聊天历史失败: {str(e)}")


@router.post("/sessions/create", response_model=Response[ChatSessionData])
async def create_session(
    data: SessionCreate,
    db: AsyncSession = Depends(get_db)
):
    """
    创建新会话
    返回会话信息
    """
    try:
        # 生成唯一会话ID
        session_id = f"CS{int(time.time())}{uuid.uuid4().hex[:8].upper()}"
        
        session = ChatSession(
            session_id=session_id,
            buyer_id=data.buyer_id,
            platform=data.platform,
            buyer_name=data.buyer_name,
            shop_id=data.shop_id,
            status=SessionStatus.ACTIVE,
            message_count=0
        )
        
        db.add(session)
        await db.commit()
        await db.refresh(session)
        
        logger.info(f"创建会话成功: {session_id}")
        
        return Response(
            data=ChatSessionData(
                session_id=session.session_id,
                buyer_id=session.buyer_id,
                platform=session.platform,
                status=session.status.value,
                message_count=session.message_count,
                created_at=session.created_at,
                last_message_at=session.last_message_at
            ),
            timestamp=int(time.time())
        )
        
    except Exception as e:
        logger.error(f"创建会话失败: {e}", exc_info=True)
        await db.rollback()
        raise HTTPException(status_code=500, detail=f"创建会话失败: {str(e)}")


@router.post("/sessions/{session_id}/close", response_model=Response)
async def close_session(
    session_id: str,
    data: SessionClose,
    db: AsyncSession = Depends(get_db)
):
    """
    关闭会话
    可选评分和反馈
    """
    try:
        result = await db.execute(
            select(ChatSession).where(ChatSession.session_id == session_id)
        )
        session = result.scalar_one_or_none()
        
        if not session:
            raise HTTPException(status_code=404, detail="会话不存在")
        
        session.status = SessionStatus.CLOSED
        session.closed_at = datetime.now()
        
        if data.rating:
            session.rating = data.rating
        if data.feedback:
            session.feedback = data.feedback
        
        await db.commit()
        
        logger.info(f"关闭会话: {session_id}, 评分: {data.rating}")
        
        return Response(
            message="会话已关闭",
            data={"session_id": session_id, "status": "closed"},
            timestamp=int(time.time())
        )
        
    except HTTPException:
        raise
    except Exception as e:
        logger.error(f"关闭会话失败: {e}", exc_info=True)
        await db.rollback()
        raise HTTPException(status_code=500, detail=f"关闭会话失败: {str(e)}")


@router.get("/sessions/buyer/{buyer_id}", response_model=Response)
async def get_buyer_sessions(
    buyer_id: str,
    platform: Optional[str] = None,
    status: Optional[str] = None,
    page: int = Query(default=1, ge=1),
    size: int = Query(default=20, ge=1, le=100),
    db: AsyncSession = Depends(get_db)
):
    """
    获取买家的会话列表
    """
    try:
        # 构建查询条件
        conditions = [ChatSession.buyer_id == buyer_id]
        if platform:
            conditions.append(ChatSession.platform == platform)
        if status:
            conditions.append(ChatSession.status == SessionStatus(status))
        
        # 查询总数
        count_result = await db.execute(
            select(func.count(ChatSession.id)).where(and_(*conditions))
        )
        total = count_result.scalar()
        
        # 分页查询
        offset = (page - 1) * size
        result = await db.execute(
            select(ChatSession)
            .where(and_(*conditions))
            .order_by(desc(ChatSession.created_at))
            .offset(offset)
            .limit(size)
        )
        sessions = result.scalars().all()
        
        # 转换为响应格式
        session_list = [
            {
                "session_id": s.session_id,
                "platform": s.platform,
                "status": s.status.value if hasattr(s.status, 'value') else s.status,
                "message_count": s.message_count,
                "created_at": s.created_at.isoformat() if s.created_at else None,
                "last_message_at": s.last_message_at.isoformat() if s.last_message_at else None,
                "last_message_preview": s.last_message_preview
            }
            for s in sessions
        ]
        
        return Response(
            data={
                "items": session_list,
                "total": total,
                "page": page,
                "size": size,
                "pages": (total + size - 1) // size
            },
            timestamp=int(time.time())
        )
        
    except Exception as e:
        logger.error(f"获取买家会话列表失败: {e}", exc_info=True)
        raise HTTPException(status_code=500, detail=f"获取会话列表失败: {str(e)}")


@router.post("/sessions/{session_id}/transfer", response_model=Response)
async def transfer_to_human(
    session_id: str,
    data: TransferRequest,
    db: AsyncSession = Depends(get_db)
):
    """
    转人工客服
    将会话状态更新为等待人工处理
    """
    try:
        result = await db.execute(
            select(ChatSession).where(ChatSession.session_id == session_id)
        )
        session = result.scalar_one_or_none()
        
        if not session:
            raise HTTPException(status_code=404, detail="会话不存在")
        
        if session.status == SessionStatus.CLOSED:
            raise HTTPException(status_code=400, detail="会话已关闭，无法转人工")
        
        if session.status == SessionStatus.TRANSFERRED:
            return Response(
                message="会话已转人工处理",
                data={"session_id": session_id, "status": "transferred"},
                timestamp=int(time.time())
            )
        
        session.status = SessionStatus.TRANSFERRED
        session.transferred_at = datetime.now()
        
        transfer_message = ChatMessage(
            session_id=session_id,
            role=MessageRole.SYSTEM,
            content=f"系统消息：已转人工客服处理，原因：{data.reason or '用户请求'}",
            intent="transfer_to_human"
        )
        db.add(transfer_message)
        
        session.message_count += 1
        session.last_message_at = datetime.now()
        session.last_message_preview = "已转人工客服处理"
        
        await db.commit()
        
        logger.info(f"会话 {session_id} 已转人工客服处理")
        
        return Response(
            message="已转人工客服处理",
            data={
                "session_id": session_id,
                "status": "transferred",
                "transferred_at": session.transferred_at.isoformat() if session.transferred_at else None
            },
            timestamp=int(time.time())
        )
        
    except HTTPException:
        raise
    except Exception as e:
        logger.error(f"转人工失败: {e}", exc_info=True)
        await db.rollback()
        raise HTTPException(status_code=500, detail=f"转人工失败: {str(e)}")