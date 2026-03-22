import asyncio
import json
import hashlib
import time
from typing import Optional, Dict, Any, List
from loguru import logger
import httpx
from pydantic import BaseModel


class A1688Config(BaseModel):
    app_key: str
    app_secret: str
    access_token: Optional[str] = None
    api_url: str = "https://gw.open.1688.com/openapi"


class A1688Product(BaseModel):
    product_id: str
    title: str
    price: float
    original_price: Optional[float] = None
    image_url: Optional[str] = None
    supplier_id: str
    supplier_name: Optional[str] = None
    stock: int = 0
    min_order_quantity: int = 1


class A1688OrderResult(BaseModel):
    success: bool
    order_id: Optional[str] = None
    error_message: Optional[str] = None
    payment_url: Optional[str] = None


class A1688Client:
    
    def __init__(self, config: A1688Config):
        self.config = config
        self.client = httpx.AsyncClient(timeout=30.0)
    
    def _generate_sign(self, params: Dict[str, Any]) -> str:
        sorted_params = sorted(params.items())
        sign_str = self.config.app_secret
        for key, value in sorted_params:
            if value is not None and key != "sign":
                sign_str += f"{key}{value}"
        sign_str += self.config.app_secret
        
        md5 = hashlib.md5()
        md5.update(sign_str.encode("utf-8"))
        return md5.hexdigest().upper()
    
    def _build_params(self, method: str, extra_params: Dict[str, Any] = None) -> Dict[str, Any]:
        params = {
            "app_key": self.config.app_key,
            "method": method,
            "timestamp": str(int(time.time() * 1000)),
            "format": "json",
            "v": "2.0",
            "sign_method": "md5"
        }
        
        if self.config.access_token:
            params["access_token"] = self.config.access_token
        
        if extra_params:
            params.update(extra_params)
        
        params["sign"] = self._generate_sign(params)
        return params
    
    async def search_products(
        self,
        keywords: str,
        page: int = 1,
        page_size: int = 20,
        price_range: tuple = None
    ) -> List[A1688Product]:
        method = "alibaba.product.search"
        
        extra_params = {
            "keywords": keywords,
            "page_no": page,
            "page_size": page_size
        }
        
        if price_range:
            extra_params["price_start"] = price_range[0]
            extra_params["price_end"] = price_range[1]
        
        params = self._build_params(method, extra_params)
        
        try:
            url = f"{self.config.api_url}/param2/1/com.alibaba.product/{method}"
            response = await self.client.post(url, json=params)
            data = response.json()
            
            if data.get("result_success"):
                products = []
                for item in data.get("result", {}).get("product_info_list", []):
                    products.append(A1688Product(
                        product_id=item.get("product_id"),
                        title=item.get("subject"),
                        price=float(item.get("price", 0)),
                        image_url=item.get("image_url"),
                        supplier_id=item.get("company_id"),
                        stock=item.get("stock", 0)
                    ))
                return products
            else:
                logger.error(f"1688商品搜索失败: {data}")
                return []
                
        except Exception as e:
            logger.error(f"1688商品搜索异常: {e}")
            return []
    
    async def get_product_detail(self, product_id: str) -> Optional[A1688Product]:
        method = "alibaba.product.get"
        
        params = self._build_params(method, {"product_id": product_id})
        
        try:
            url = f"{self.config.api_url}/param2/1/com.alibaba.product/{method}"
            response = await self.client.post(url, json=params)
            data = response.json()
            
            if data.get("result_success"):
                item = data.get("result", {}).get("product_info", {})
                return A1688Product(
                    product_id=item.get("product_id"),
                    title=item.get("subject"),
                    price=float(item.get("price", 0)),
                    original_price=float(item.get("original_price", 0)) if item.get("original_price") else None,
                    image_url=item.get("image_url"),
                    supplier_id=item.get("company_id"),
                    supplier_name=item.get("company_name"),
                    stock=item.get("stock", 0),
                    min_order_quantity=item.get("min_order_quantity", 1)
                )
            return None
            
        except Exception as e:
            logger.error(f"1688商品详情获取异常: {e}")
            return None
    
    async def create_order(
        self,
        product_id: str,
        sku_id: str,
        quantity: int,
        address_info: Dict[str, str],
        auto_pay: bool = False
    ) -> A1688OrderResult:
        method = "alibaba.trade.fastCreateOrder"
        
        extra_params = {
            "product_id": product_id,
            "sku_id": sku_id,
            "quantity": quantity,
            "address_info": json.dumps(address_info),
            "auto_pay": "true" if auto_pay else "false"
        }
        
        params = self._build_params(method, extra_params)
        
        try:
            url = f"{self.config.api_url}/param2/1/com.alibaba.trade/{method}"
            response = await self.client.post(url, json=params)
            data = response.json()
            
            if data.get("result_success"):
                result_data = data.get("result", {})
                return A1688OrderResult(
                    success=True,
                    order_id=result_data.get("order_id"),
                    payment_url=result_data.get("pay_url")
                )
            else:
                error_msg = data.get("error_message", "未知错误")
                logger.error(f"1688下单失败: {error_msg}")
                return A1688OrderResult(
                    success=False,
                    error_message=error_msg
                )
                
        except Exception as e:
            logger.error(f"1688下单异常: {e}")
            return A1688OrderResult(
                success=False,
                error_message=str(e)
            )
    
    async def get_order_detail(self, order_id: str) -> Optional[Dict[str, Any]]:
        method = "alibaba.trade.getBuyerOrderView"
        
        params = self._build_params(method, {"order_id": order_id})
        
        try:
            url = f"{self.config.api_url}/param2/1/com.alibaba.trade/{method}"
            response = await self.client.post(url, json=params)
            data = response.json()
            
            if data.get("result_success"):
                return data.get("result", {}).get("order_info")
            return None
            
        except Exception as e:
            logger.error(f"1688订单详情获取异常: {e}")
            return None
    
    async def get_payment_url(self, order_id: str) -> Optional[str]:
        method = "alibaba.alipay.url.get"
        
        params = self._build_params(method, {"order_id": order_id})
        
        try:
            url = f"{self.config.api_url}/param2/1/com.alibaba.trade/{method}"
            response = await self.client.post(url, json=params)
            data = response.json()
            
            if data.get("result_success"):
                return data.get("result", {}).get("pay_url")
            return None
            
        except Exception as e:
            logger.error(f"1688支付链接获取异常: {e}")
            return None
    
    async def close(self):
        await self.client.aclose()


class A1688Service:
    
    AUTO_PAY_THRESHOLD = 1000.0
    MANUAL_CONFIRM_THRESHOLD = 10000.0
    
    def __init__(self, config: A1688Config):
        self.client = A1688Client(config)
        self.pending_manual_orders: Dict[str, Dict[str, Any]] = {}
    
    async def find_source_product(
        self,
        product_name: str,
        target_price: float = None,
        keywords: str = None
    ) -> Optional[A1688Product]:
        search_keywords = keywords or product_name
        products = await self.client.search_products(
            keywords=search_keywords,
            page_size=20,
            price_range=(0, target_price * 1.2) if target_price else None
        )
        
        if not products:
            return None
        
        if target_price:
            products.sort(key=lambda p: abs(p.price - target_price))
        
        return products[0]
    
    async def create_purchase_order(
        self,
        order_id: str,
        product_id: str,
        sku_id: str,
        quantity: int,
        total_amount: float,
        address_info: Dict[str, str]
    ) -> Dict[str, Any]:
        need_manual_confirm = total_amount > self.AUTO_PAY_THRESHOLD
        
        auto_pay = total_amount <= self.AUTO_PAY_THRESHOLD
        
        result = await self.client.create_order(
            product_id=product_id,
            sku_id=sku_id,
            quantity=quantity,
            address_info=address_info,
            auto_pay=auto_pay
        )
        
        response = {
            "success": result.success,
            "source_order_id": result.order_id,
            "payment_url": result.payment_url,
            "need_manual_confirm": need_manual_confirm,
            "auto_paid": auto_pay and result.success
        }
        
        if result.success and need_manual_confirm:
            self.pending_manual_orders[result.order_id] = {
                "order_id": order_id,
                "source_order_id": result.order_id,
                "total_amount": total_amount,
                "payment_url": result.payment_url
            }
        
        if not result.success:
            response["error"] = result.error_message
        
        return response
    
    async def confirm_manual_order(self, source_order_id: str, confirm: bool) -> bool:
        if source_order_id not in self.pending_manual_orders:
            return False
        
        order_info = self.pending_manual_orders[source_order_id]
        
        if confirm:
            payment_url = await self.client.get_payment_url(source_order_id)
            if payment_url:
                order_info["payment_url"] = payment_url
                return True
        else:
            del self.pending_manual_orders[source_order_id]
            return True
        
        return False
    
    async def check_order_status(self, source_order_id: str) -> Optional[str]:
        order_detail = await self.client.get_order_detail(source_order_id)
        if order_detail:
            return order_detail.get("status")
        return None
    
    async def close(self):
        await self.client.close()