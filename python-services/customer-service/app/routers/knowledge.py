from fastapi import APIRouter, HTTPException
from pydantic import BaseModel
from typing import Optional, List
from app.schemas.base import Response
import time

router = APIRouter()


class KnowledgeItem(BaseModel):
    id: Optional[int] = None
    category: str
    question: str
    answer: str
    keywords: Optional[List[str]] = None


@router.get("/list", response_model=Response)
async def list_knowledge(category: Optional[str] = None, page: int = 1, size: int = 20):
    return Response(
        data={"items": [], "total": 0, "page": page, "size": size},
        timestamp=int(time.time())
    )


@router.post("/create", response_model=Response)
async def create_knowledge(item: KnowledgeItem):
    return Response(
        message="创建成功",
        data={"id": 1},
        timestamp=int(time.time())
    )


@router.put("/{item_id}", response_model=Response)
async def update_knowledge(item_id: int, item: KnowledgeItem):
    return Response(
        message="更新成功",
        timestamp=int(time.time())
    )


@router.delete("/{item_id}", response_model=Response)
async def delete_knowledge(item_id: int):
    return Response(
        message="删除成功",
        timestamp=int(time.time())
    )