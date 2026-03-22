from fastapi import APIRouter, Depends, HTTPException
from pydantic import BaseModel
from typing import Optional, List
from app.schemas.base import Response
import time

router = APIRouter()


class ChatMessage(BaseModel):
    session_id: str
    message: str
    buyer_id: str
    platform: str


class ChatResponse(BaseModel):
    session_id: str
    reply: str
    intent: Optional[str] = None
    confidence: Optional[float] = None


@router.post("/send", response_model=Response[ChatResponse])
async def send_message(msg: ChatMessage):
    reply = await process_message(msg)
    return Response(
        data=ChatResponse(
            session_id=msg.session_id,
            reply=reply,
            intent="inquiry",
            confidence=0.95
        ),
        timestamp=int(time.time())
    )


@router.get("/sessions/{session_id}/history", response_model=Response)
async def get_chat_history(session_id: str, limit: int = 50):
    return Response(
        data={"session_id": session_id, "messages": []},
        timestamp=int(time.time())
    )


async def process_message(msg: ChatMessage) -> str:
    return "您好，请问有什么可以帮助您的？"