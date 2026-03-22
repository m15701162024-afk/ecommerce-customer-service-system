from fastapi import APIRouter
from app.schemas.base import Response
import time

router = APIRouter()


@router.get("/health", response_model=Response)
async def health_check():
    return Response(
        message="服务正常",
        timestamp=int(time.time())
    )


@router.get("/ready", response_model=Response)
async def readiness_check():
    return Response(
        message="服务就绪",
        timestamp=int(time.time())
    )