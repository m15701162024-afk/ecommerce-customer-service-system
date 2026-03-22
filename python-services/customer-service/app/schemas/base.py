from pydantic import BaseModel
from typing import Optional, Generic, TypeVar, List

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