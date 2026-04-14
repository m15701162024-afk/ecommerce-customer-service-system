"""
知识库接口路由
实现真实的CRUD操作
"""
from fastapi import APIRouter, Depends, HTTPException, Query
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy import select, func, and_, or_, desc
from typing import Optional, List
import time
from datetime import datetime
from loguru import logger

from app.database import get_db
from app.models import Knowledge, KnowledgeCategory, AuditStatus
from app.schemas.base import (
    Response, KnowledgeCreate, KnowledgeUpdate, 
    KnowledgeData, KnowledgeListData,
    SubmitAuditRequest, AuditRequest, KnowledgeAuditData, PendingAuditListData
)

router = APIRouter()


@router.get("/list", response_model=Response[KnowledgeListData])
async def list_knowledge(
    category: Optional[str] = Query(None, description="分类过滤"),
    keyword: Optional[str] = Query(None, description="关键词搜索"),
    shop_id: Optional[int] = Query(None, description="店铺ID过滤"),
    is_active: Optional[bool] = Query(None, description="是否启用"),
    page: int = Query(default=1, ge=1, description="页码"),
    size: int = Query(default=20, ge=1, le=100, description="每页数量"),
    db: AsyncSession = Depends(get_db)
):
    """
    获取知识库列表
    支持分类、关键词搜索和分页
    """
    try:
        # 构建查询条件
        conditions = []
        
        if category:
            try:
                conditions.append(Knowledge.category == KnowledgeCategory(category))
            except ValueError:
                pass
        
        if keyword:
            keyword_filter = or_(
                Knowledge.question.contains(keyword),
                Knowledge.answer.contains(keyword),
                Knowledge.keywords.contains(keyword)
            )
            conditions.append(keyword_filter)
        
        if shop_id is not None:
            conditions.append(Knowledge.shop_id == shop_id)
        
        if is_active is not None:
            conditions.append(Knowledge.is_active == is_active)
        
        # 查询总数
        count_query = select(func.count(Knowledge.id))
        if conditions:
            count_query = count_query.where(and_(*conditions))
        count_result = await db.execute(count_query)
        total = count_result.scalar()
        
        # 分页查询
        offset = (page - 1) * size
        query = select(Knowledge)
        if conditions:
            query = query.where(and_(*conditions))
        query = query.order_by(desc(Knowledge.priority), desc(Knowledge.created_at)).offset(offset).limit(size)
        
        result = await db.execute(query)
        knowledge_items = result.scalars().all()
        
        # 转换为响应格式
        items = [
            KnowledgeData(
                id=item.id,
                category=item.category.value if hasattr(item.category, 'value') else item.category,
                question=item.question,
                answer=item.answer,
                keywords=item.keywords.split(",") if item.keywords else [],
                title=item.title,
                tags=item.tags.split(",") if item.tags else [],
                use_count=item.use_count or 0,
                helpful_count=item.helpful_count or 0,
                is_active=item.is_active,
                priority=item.priority or 0,
                audit_status=item.audit_status.value if hasattr(item.audit_status, 'value') else (item.audit_status or 'pending'),
                audit_comment=item.audit_comment,
                auditor_id=item.auditor_id,
                audited_at=item.audited_at,
                created_at=item.created_at,
                updated_at=item.updated_at
            )
            for item in knowledge_items
        ]
        
        return Response(
            data=KnowledgeListData(
                items=items,
                total=total,
                page=page,
                size=size
            ),
            timestamp=int(time.time())
        )
        
    except Exception as e:
        logger.error(f"获取知识库列表失败: {e}", exc_info=True)
        raise HTTPException(status_code=500, detail=f"获取知识库列表失败: {str(e)}")


@router.get("/{item_id}", response_model=Response[KnowledgeData])
async def get_knowledge(
    item_id: int,
    db: AsyncSession = Depends(get_db)
):
    """
    获取单个知识条目详情
    """
    try:
        result = await db.execute(
            select(Knowledge).where(Knowledge.id == item_id)
        )
        item = result.scalar_one_or_none()
        
        if not item:
            raise HTTPException(status_code=404, detail="知识条目不存在")
        
        return Response(
            data=KnowledgeData(
                id=item.id,
                category=item.category.value if hasattr(item.category, 'value') else item.category,
                question=item.question,
                answer=item.answer,
                keywords=item.keywords.split(",") if item.keywords else [],
                title=item.title,
                tags=item.tags.split(",") if item.tags else [],
                use_count=item.use_count or 0,
                helpful_count=item.helpful_count or 0,
                is_active=item.is_active,
                priority=item.priority or 0,
                audit_status=item.audit_status.value if hasattr(item.audit_status, 'value') else (item.audit_status or 'pending'),
                audit_comment=item.audit_comment,
                auditor_id=item.auditor_id,
                audited_at=item.audited_at,
                created_at=item.created_at,
                updated_at=item.updated_at
            ),
            timestamp=int(time.time())
        )
        
    except HTTPException:
        raise
    except Exception as e:
        logger.error(f"获取知识条目失败: {e}", exc_info=True)
        raise HTTPException(status_code=500, detail=f"获取知识条目失败: {str(e)}")


@router.post("/create", response_model=Response)
async def create_knowledge(
    item: KnowledgeCreate,
    db: AsyncSession = Depends(get_db)
):
    """
    创建知识条目
    """
    try:
        # 验证分类
        try:
            category_enum = KnowledgeCategory(item.category)
        except ValueError:
            raise HTTPException(status_code=400, detail=f"无效的分类: {item.category}")
        
        # 创建知识条目
        knowledge = Knowledge(
            category=category_enum,
            question=item.question,
            answer=item.answer,
            keywords=",".join(item.keywords) if item.keywords else None,
            title=item.title,
            tags=",".join(item.tags) if item.tags else None,
            product_id=item.product_id,
            shop_id=item.shop_id,
            priority=item.priority,
            is_active=True,
            use_count=0,
            helpful_count=0
        )
        
        db.add(knowledge)
        await db.commit()
        await db.refresh(knowledge)
        
        logger.info(f"创建知识条目成功: {knowledge.id} - {knowledge.question[:30]}")
        
        return Response(
            message="创建成功",
            data={"id": knowledge.id},
            timestamp=int(time.time())
        )
        
    except HTTPException:
        raise
    except Exception as e:
        logger.error(f"创建知识条目失败: {e}", exc_info=True)
        await db.rollback()
        raise HTTPException(status_code=500, detail=f"创建知识条目失败: {str(e)}")


@router.put("/{item_id}", response_model=Response)
async def update_knowledge(
    item_id: int,
    item: KnowledgeUpdate,
    db: AsyncSession = Depends(get_db)
):
    """
    更新知识条目
    """
    try:
        result = await db.execute(
            select(Knowledge).where(Knowledge.id == item_id)
        )
        knowledge = result.scalar_one_or_none()
        
        if not knowledge:
            raise HTTPException(status_code=404, detail="知识条目不存在")
        
        # 更新字段
        update_data = item.model_dump(exclude_unset=True)
        
        for field, value in update_data.items():
            if field == "category" and value:
                try:
                    value = KnowledgeCategory(value)
                except ValueError:
                    raise HTTPException(status_code=400, detail=f"无效的分类: {value}")
            elif field in ("keywords", "tags") and isinstance(value, list):
                value = ",".join(value) if value else None
            
            setattr(knowledge, field, value)
        
        await db.commit()
        
        logger.info(f"更新知识条目成功: {item_id}")
        
        return Response(
            message="更新成功",
            data={"id": item_id},
            timestamp=int(time.time())
        )
        
    except HTTPException:
        raise
    except Exception as e:
        logger.error(f"更新知识条目失败: {e}", exc_info=True)
        await db.rollback()
        raise HTTPException(status_code=500, detail=f"更新知识条目失败: {str(e)}")


@router.delete("/{item_id}", response_model=Response)
async def delete_knowledge(
    item_id: int,
    db: AsyncSession = Depends(get_db)
):
    """
    删除知识条目
    软删除：将is_active设置为False
    """
    try:
        result = await db.execute(
            select(Knowledge).where(Knowledge.id == item_id)
        )
        knowledge = result.scalar_one_or_none()
        
        if not knowledge:
            raise HTTPException(status_code=404, detail="知识条目不存在")
        
        # 软删除
        knowledge.is_active = False
        await db.commit()
        
        logger.info(f"删除知识条目成功(软删除): {item_id}")
        
        return Response(
            message="删除成功",
            data={"id": item_id},
            timestamp=int(time.time())
        )
        
    except HTTPException:
        raise
    except Exception as e:
        logger.error(f"删除知识条目失败: {e}", exc_info=True)
        await db.rollback()
        raise HTTPException(status_code=500, detail=f"删除知识条目失败: {str(e)}")


@router.post("/{item_id}/hard-delete", response_model=Response)
async def hard_delete_knowledge(
    item_id: int,
    db: AsyncSession = Depends(get_db)
):
    """
    硬删除知识条目（谨慎使用）
    """
    try:
        result = await db.execute(
            select(Knowledge).where(Knowledge.id == item_id)
        )
        knowledge = result.scalar_one_or_none()
        
        if not knowledge:
            raise HTTPException(status_code=404, detail="知识条目不存在")
        
        await db.delete(knowledge)
        await db.commit()
        
        logger.info(f"硬删除知识条目成功: {item_id}")
        
        return Response(
            message="已永久删除",
            data={"id": item_id},
            timestamp=int(time.time())
        )
        
    except HTTPException:
        raise
    except Exception as e:
        logger.error(f"硬删除知识条目失败: {e}", exc_info=True)
        await db.rollback()
        raise HTTPException(status_code=500, detail=f"删除知识条目失败: {str(e)}")


@router.post("/{item_id}/helpful", response_model=Response)
async def mark_helpful(
    item_id: int,
    db: AsyncSession = Depends(get_db)
):
    """
    标记知识条目有帮助
    """
    try:
        result = await db.execute(
            select(Knowledge).where(Knowledge.id == item_id)
        )
        knowledge = result.scalar_one_or_none()
        
        if not knowledge:
            raise HTTPException(status_code=404, detail="知识条目不存在")
        
        knowledge.helpful_count = (knowledge.helpful_count or 0) + 1
        await db.commit()
        
        return Response(
            message="感谢您的反馈",
            data={"id": item_id, "helpful_count": knowledge.helpful_count},
            timestamp=int(time.time())
        )
        
    except HTTPException:
        raise
    except Exception as e:
        logger.error(f"标记有帮助失败: {e}", exc_info=True)
        await db.rollback()
        raise HTTPException(status_code=500, detail=f"操作失败: {str(e)}")


@router.get("/search/query", response_model=Response)
async def search_knowledge(
    q: str = Query(..., min_length=1, description="搜索关键词"),
    limit: int = Query(default=10, ge=1, le=50, description="返回数量"),
    db: AsyncSession = Depends(get_db)
):
    """
    搜索知识库
    用于AI匹配知识条目
    """
    try:
        # 模糊搜索问题和关键词
        result = await db.execute(
            select(Knowledge)
            .where(
                and_(
                    Knowledge.is_active == True,
                    or_(
                        Knowledge.question.contains(q),
                        Knowledge.keywords.contains(q),
                        Knowledge.answer.contains(q)
                    )
                )
            )
            .order_by(desc(Knowledge.priority), desc(Knowledge.use_count))
            .limit(limit)
        )
        items = result.scalars().all()
        
        # 返回简化格式（供AI使用）
        knowledge_list = [
            {
                "id": item.id,
                "category": item.category.value if hasattr(item.category, 'value') else item.category,
                "question": item.question,
                "answer": item.answer,
                "keywords": item.keywords.split(",") if item.keywords else []
            }
            for item in items
        ]
        
        return Response(
            data={"items": knowledge_list, "total": len(knowledge_list)},
            timestamp=int(time.time())
        )
        
    except Exception as e:
        logger.error(f"搜索知识库失败: {e}", exc_info=True)
        raise HTTPException(status_code=500, detail=f"搜索失败: {str(e)}")


@router.get("/categories/list", response_model=Response)
async def list_categories():
    """
    获取所有知识库分类
    """
    categories = [
        {"value": "product", "label": "产品相关"},
        {"value": "order", "label": "订单相关"},
        {"value": "shipping", "label": "物流相关"},
        {"value": "refund", "label": "退款相关"},
        {"value": "coupon", "label": "优惠相关"},
        {"value": "faq", "label": "常见问题"},
        {"value": "other", "label": "其他"}
    ]
    
    return Response(
        data={"categories": categories},
        timestamp=int(time.time())
    )


@router.get("/pending", response_model=Response[PendingAuditListData])
async def list_pending_audit(
    category: Optional[str] = Query(None, description="分类过滤"),
    keyword: Optional[str] = Query(None, description="关键词搜索"),
    page: int = Query(default=1, ge=1, description="页码"),
    size: int = Query(default=20, ge=1, le=100, description="每页数量"),
    db: AsyncSession = Depends(get_db)
):
    """
    获取待审核知识列表
    """
    try:
        conditions = [Knowledge.audit_status == AuditStatus.PENDING]
        
        if category:
            try:
                conditions.append(Knowledge.category == KnowledgeCategory(category))
            except ValueError:
                pass
        
        if keyword:
            keyword_filter = or_(
                Knowledge.question.contains(keyword),
                Knowledge.answer.contains(keyword),
                Knowledge.keywords.contains(keyword)
            )
            conditions.append(keyword_filter)
        
        count_query = select(func.count(Knowledge.id)).where(and_(*conditions))
        count_result = await db.execute(count_query)
        total = count_result.scalar()
        
        offset = (page - 1) * size
        query = select(Knowledge).where(and_(*conditions)).order_by(desc(Knowledge.created_at)).offset(offset).limit(size)
        
        result = await db.execute(query)
        knowledge_items = result.scalars().all()
        
        items = [
            KnowledgeAuditData(
                id=item.id,
                category=item.category.value if hasattr(item.category, 'value') else item.category,
                question=item.question,
                answer=item.answer,
                keywords=item.keywords.split(",") if item.keywords else [],
                title=item.title,
                tags=item.tags.split(",") if item.tags else [],
                audit_status=item.audit_status.value if hasattr(item.audit_status, 'value') else item.audit_status,
                audit_comment=item.audit_comment,
                auditor_id=item.auditor_id,
                audited_at=item.audited_at,
                created_at=item.created_at,
                created_by=item.created_by
            )
            for item in knowledge_items
        ]
        
        return Response(
            data=PendingAuditListData(
                items=items,
                total=total,
                page=page,
                size=size
            ),
            timestamp=int(time.time())
        )
        
    except Exception as e:
        logger.error(f"获取待审核知识列表失败: {e}", exc_info=True)
        raise HTTPException(status_code=500, detail=f"获取待审核知识列表失败: {str(e)}")


@router.post("/{item_id}/submit-audit", response_model=Response)
async def submit_audit(
    item_id: int,
    db: AsyncSession = Depends(get_db)
):
    """
    提交审核
    将知识条目的审核状态设置为 pending
    """
    try:
        result = await db.execute(
            select(Knowledge).where(Knowledge.id == item_id)
        )
        knowledge = result.scalar_one_or_none()
        
        if not knowledge:
            raise HTTPException(status_code=404, detail="知识条目不存在")
        
        if knowledge.audit_status == AuditStatus.APPROVED:
            raise HTTPException(status_code=400, detail="该知识条目已审核通过，无需重新提交")
        
        knowledge.audit_status = AuditStatus.PENDING
        knowledge.audit_comment = None
        knowledge.auditor_id = None
        knowledge.audited_at = None
        
        await db.commit()
        
        logger.info(f"提交审核成功: {item_id}")
        
        return Response(
            message="已提交审核",
            data={"id": item_id, "audit_status": "pending"},
            timestamp=int(time.time())
        )
        
    except HTTPException:
        raise
    except Exception as e:
        logger.error(f"提交审核失败: {e}", exc_info=True)
        await db.rollback()
        raise HTTPException(status_code=500, detail=f"提交审核失败: {str(e)}")


@router.post("/{item_id}/audit", response_model=Response)
async def audit_knowledge(
    item_id: int,
    audit_data: AuditRequest,
    db: AsyncSession = Depends(get_db)
):
    """
    审核操作（通过/拒绝）
    """
    try:
        result = await db.execute(
            select(Knowledge).where(Knowledge.id == item_id)
        )
        knowledge = result.scalar_one_or_none()
        
        if not knowledge:
            raise HTTPException(status_code=404, detail="知识条目不存在")
        
        if knowledge.audit_status != AuditStatus.PENDING:
            raise HTTPException(status_code=400, detail=f"该知识条目当前状态为 {knowledge.audit_status.value}，无法审核")
        
        if audit_data.status == AuditStatus.PENDING:
            raise HTTPException(status_code=400, detail="审核状态不能设置为 pending")
        
        knowledge.audit_status = audit_data.status
        knowledge.audit_comment = audit_data.comment
        knowledge.auditor_id = audit_data.auditor_id
        knowledge.audited_at = datetime.now()
        
        if audit_data.status == AuditStatus.APPROVED:
            knowledge.is_active = True
        
        await db.commit()
        
        logger.info(f"审核操作成功: {item_id}, 状态: {audit_data.status.value}, 审核人: {audit_data.auditor_id}")
        
        return Response(
            message=f"审核完成: {audit_data.status.value}",
            data={
                "id": item_id,
                "audit_status": audit_data.status.value,
                "auditor_id": audit_data.auditor_id,
                "audited_at": knowledge.audited_at.isoformat() if knowledge.audited_at else None
            },
            timestamp=int(time.time())
        )
        
    except HTTPException:
        raise
    except Exception as e:
        logger.error(f"审核操作失败: {e}", exc_info=True)
        await db.rollback()
        raise HTTPException(status_code=500, detail=f"审核操作失败: {str(e)}")