import asyncio
import json
import hashlib
import time
from typing import Optional, Dict, Any
from loguru import logger
import websockets
import httpx
from pydantic import BaseModel


class QianniuMessage(BaseModel):
    seller_id: str
    buyer_id: str
    message_id: str
    content: str
    message_type: str = "text"
    timestamp: int


class QianniuPluginClient:
    
    def __init__(
        self,
        app_key: str,
        app_secret: str,
        ws_url: str = "wss://wss.qianniu.taobao.com/ws"
    ):
        self.app_key = app_key
        self.app_secret = app_secret
        self.ws_url = ws_url
        self.websocket: Optional[websockets.WebSocketClientProtocol] = None
        self.session_key: Optional[str] = None
        self.seller_id: Optional[str] = None
        self.message_callback = None
        self.running = False
        
    def generate_sign(self, params: Dict[str, Any]) -> str:
        sorted_params = sorted(params.items())
        sign_str = self.app_secret
        for key, value in sorted_params:
            if value is not None and key != "sign":
                sign_str += f"{key}{value}"
        sign_str += self.app_secret
        
        md5 = hashlib.md5()
        md5.update(sign_str.encode("utf-8"))
        return md5.hexdigest().upper()
    
    async def connect(self, session_key: str, seller_id: str):
        self.session_key = session_key
        self.seller_id = seller_id
        
        params = {
            "app_key": self.app_key,
            "timestamp": str(int(time.time() * 1000)),
            "session": session_key,
            "seller_id": seller_id
        }
        params["sign"] = self.generate_sign(params)
        
        ws_url = f"{self.ws_url}?{'&'.join(f'{k}={v}' for k, v in params.items())}"
        
        logger.info(f"正在连接千牛WebSocket: {ws_url}")
        
        self.websocket = await websockets.connect(ws_url)
        self.running = True
        
        logger.info("千牛WebSocket连接成功")
        
        asyncio.create_task(self._receive_loop())
    
    async def _receive_loop(self):
        try:
            async for message in self.websocket:
                await self._handle_message(message)
        except websockets.ConnectionClosed:
            logger.warning("千牛WebSocket连接已关闭")
            self.running = False
        except Exception as e:
            logger.error(f"千牛消息接收异常: {e}", exc_info=True)
            self.running = False
    
    async def _handle_message(self, raw_message: str):
        try:
            data = json.loads(raw_message)
            message_type = data.get("type")
            
            if message_type == "chat":
                await self._handle_chat_message(data)
            elif message_type == "order":
                await self._handle_order_message(data)
            elif message_type == "heartbeat":
                await self._send_heartbeat_response()
            else:
                logger.debug(f"收到未知类型消息: {message_type}")
                
        except json.JSONDecodeError:
            logger.warning(f"无效的JSON消息: {raw_message}")
    
    async def _handle_chat_message(self, data: Dict[str, Any]):
        msg = QianniuMessage(
            seller_id=data.get("seller_id", self.seller_id),
            buyer_id=data.get("buyer_id"),
            message_id=data.get("message_id"),
            content=data.get("content"),
            message_type=data.get("message_type", "text"),
            timestamp=data.get("timestamp", int(time.time() * 1000))
        )
        
        logger.info(f"收到千牛消息: buyer={msg.buyer_id}, content={msg.content[:50]}...")
        
        if self.message_callback:
            await self.message_callback(msg)
    
    async def _handle_order_message(self, data: Dict[str, Any]):
        logger.info(f"收到千牛订单消息: {data}")
    
    async def _send_heartbeat_response(self):
        if self.websocket:
            await self.websocket.send(json.dumps({"type": "heartbeat_ack"}))
    
    async def send_message(self, buyer_id: str, content: str, message_type: str = "text"):
        if not self.websocket or not self.running:
            logger.warning("WebSocket未连接，无法发送消息")
            return False
        
        message = {
            "type": "chat",
            "seller_id": self.seller_id,
            "buyer_id": buyer_id,
            "content": content,
            "message_type": message_type,
            "timestamp": int(time.time() * 1000)
        }
        
        try:
            await self.websocket.send(json.dumps(message))
            logger.info(f"发送消息成功: buyer={buyer_id}")
            return True
        except Exception as e:
            logger.error(f"发送消息失败: {e}")
            return False
    
    async def close(self):
        self.running = False
        if self.websocket:
            await self.websocket.close()
            logger.info("千牛WebSocket连接已关闭")


class QianniuService:
    
    def __init__(self, app_key: str, app_secret: str):
        self.app_key = app_key
        self.app_secret = app_secret
        self.clients: Dict[str, QianniuPluginClient] = {}
        
    async def register_shop(self, shop_id: str, session_key: str, seller_id: str, message_callback=None):
        if shop_id in self.clients:
            logger.warning(f"店铺已注册: {shop_id}")
            return
        
        client = QianniuPluginClient(self.app_key, self.app_secret)
        client.message_callback = message_callback
        await client.connect(session_key, seller_id)
        self.clients[shop_id] = client
        
        logger.info(f"店铺注册成功: {shop_id}")
    
    async def unregister_shop(self, shop_id: str):
        if shop_id in self.clients:
            await self.clients[shop_id].close()
            del self.clients[shop_id]
            logger.info(f"店铺注销成功: {shop_id}")
    
    async def send_message(self, shop_id: str, buyer_id: str, content: str) -> bool:
        if shop_id not in self.clients:
            logger.warning(f"店铺未注册: {shop_id}")
            return False
        
        return await self.clients[shop_id].send_message(buyer_id, content)
    
    async def close_all(self):
        for shop_id, client in self.clients.items():
            await client.close()
        self.clients.clear()