from pydantic_settings import BaseSettings
from functools import lru_cache
from typing import Optional


class Settings(BaseSettings):
    app_name: str = "ecommerce-customer-service"
    app_env: str = "development"
    debug: bool = True
    
    database_url: str = "mysql+aiomysql://root:@localhost:4000/ecommerce"
    redis_url: str = "redis://localhost:7001"
    
    kafka_servers: str = "localhost:9092"
    rabbitmq_url: str = "amqp://admin:admin123@localhost:5672/ecommerce"
    
    jwt_secret: str = "ecommerce-customer-service-secret-key-2024"
    jwt_expiration: int = 86400
    
    openai_api_key: Optional[str] = None
    openai_base_url: Optional[str] = None
    
    log_level: str = "INFO"
    
    class Config:
        env_file = ".env"
        env_file_encoding = "utf-8"


@lru_cache
def get_settings() -> Settings:
    return Settings()