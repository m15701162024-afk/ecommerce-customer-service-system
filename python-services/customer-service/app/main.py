from fastapi import FastAPI, Request
from fastapi.middleware.cors import CORSMiddleware
from fastapi.responses import JSONResponse
from fastapi.exceptions import RequestValidationError
from loguru import logger
from contextlib import asynccontextmanager
import time
import sys

from app.config import get_settings
from app.database import init_db, close_db, check_db_connection
from app.routers import health, chat, knowledge

settings = get_settings()

# 配置日志
logger.remove()
logger.add(
    sys.stdout,
    level=settings.log_level,
    format="<green>{time:YYYY-MM-DD HH:mm:ss}</green> | <level>{level: <8}</level> | <cyan>{name}</cyan>:<cyan>{function}</cyan>:<cyan>{line}</cyan> - <level>{message}</level>"
)


@asynccontextmanager
async def lifespan(app: FastAPI):
    """
    应用生命周期管理
    启动时初始化数据库，关闭时清理资源
    """
    # 启动逻辑
    logger.info(f"Starting {settings.app_name} in {settings.app_env} mode")
    
    # 检查数据库连接
    db_connected = await check_db_connection()
    if db_connected:
        logger.info("数据库连接成功")
        # 初始化数据库表
        await init_db()
    else:
        logger.warning("数据库连接失败，服务将启动但数据库功能不可用")
    
    yield
    
    # 关闭逻辑
    logger.info(f"Shutting down {settings.app_name}")
    await close_db()
    logger.info("服务已停止")


app = FastAPI(
    title="智能客服服务",
    description="AI-powered customer service for e-commerce platforms",
    version="1.0.0",
    docs_url="/docs",
    redoc_url="/redoc",
    lifespan=lifespan
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)


@app.middleware("http")
async def log_requests(request: Request, call_next):
    start_time = time.time()
    response = await call_next(request)
    duration = time.time() - start_time
    logger.info(f"{request.method} {request.url.path} - {response.status_code} - {duration:.3f}s")
    return response


@app.exception_handler(RequestValidationError)
async def validation_exception_handler(request: Request, exc: RequestValidationError):
    return JSONResponse(
        status_code=400,
        content={"code": 400, "message": "参数校验失败", "detail": exc.errors()}
    )


@app.exception_handler(Exception)
async def global_exception_handler(request: Request, exc: Exception):
    logger.error(f"Unhandled exception: {exc}", exc_info=True)
    return JSONResponse(
        status_code=500,
        content={"code": 500, "message": "服务器内部错误"}
    )


# 注册路由
app.include_router(health.router, prefix="/api/v1", tags=["健康检查"])
app.include_router(chat.router, prefix="/api/v1/chat", tags=["客服对话"])
app.include_router(knowledge.router, prefix="/api/v1/knowledge", tags=["知识库"])


# 健康检查端点
@app.get("/health", tags=["健康检查"])
async def health_check():
    """
    服务健康检查
    返回服务状态和数据库连接状态
    """
    db_status = "connected" if await check_db_connection() else "disconnected"
    return {
        "status": "healthy",
        "service": settings.app_name,
        "version": "1.0.0",
        "database": db_status,
        "environment": settings.app_env
    }


# 根路径
@app.get("/", tags=["根路径"])
async def root():
    """
    服务根路径
    返回服务基本信息
    """
    return {
        "service": "智能客服服务",
        "version": "1.0.0",
        "docs": "/docs",
        "health": "/health"
    }


if __name__ == "__main__":
    import uvicorn
    uvicorn.run(
        "app.main:app",
        host="0.0.0.0",
        port=8000,
        reload=settings.debug,
        log_level=settings.log_level.lower()
    )