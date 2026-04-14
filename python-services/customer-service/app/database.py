"""
数据库连接配置
使用SQLAlchemy异步连接MySQL
"""
from sqlalchemy.ext.asyncio import AsyncSession, create_async_engine, async_sessionmaker
from sqlalchemy.orm import DeclarativeBase
from sqlalchemy import text
from loguru import logger
from typing import AsyncGenerator

from app.config import get_settings

settings = get_settings()

# 创建异步引擎
engine = create_async_engine(
    settings.database_url,
    echo=settings.debug,
    pool_size=10,
    max_overflow=20,
    pool_pre_ping=True,
    pool_recycle=3600
)

# 创建异步会话工厂
async_session_maker = async_sessionmaker(
    engine,
    class_=AsyncSession,
    expire_on_commit=False,
    autocommit=False,
    autoflush=False
)


class Base(DeclarativeBase):
    """SQLAlchemy基类"""
    pass


async def get_db() -> AsyncGenerator[AsyncSession, None]:
    """
    获取数据库会话的依赖函数
    用于FastAPI的Depends注入
    """
    async with async_session_maker() as session:
        try:
            yield session
            await session.commit()
        except Exception as e:
            await session.rollback()
            logger.error(f"数据库会话异常: {e}")
            raise
        finally:
            await session.close()


async def init_db():
    """
    初始化数据库
    创建所有表
    """
    try:
        async with engine.begin() as conn:
            # 导入所有模型以确保它们被注册
            from app.models import ChatSession, ChatMessage, Knowledge  # noqa
            
            await conn.run_sync(Base.metadata.create_all)
            logger.info("数据库表初始化完成")
    except Exception as e:
        logger.error(f"数据库初始化失败: {e}")
        raise


async def check_db_connection() -> bool:
    """
    检查数据库连接是否正常
    """
    try:
        async with engine.connect() as conn:
            await conn.execute(text("SELECT 1"))
        return True
    except Exception as e:
        logger.error(f"数据库连接检查失败: {e}")
        return False


async def close_db():
    """
    关闭数据库连接池
    """
    try:
        await engine.dispose()
        logger.info("数据库连接池已关闭")
    except Exception as e:
        logger.error(f"关闭数据库连接池失败: {e}")