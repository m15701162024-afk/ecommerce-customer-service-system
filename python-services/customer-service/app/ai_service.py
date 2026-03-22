import json
import re
from typing import Optional, Dict, Any, List, Tuple
from dataclasses import dataclass
from enum import Enum
from loguru import logger
import httpx


class IntentType(Enum):
    PRODUCT_INQUIRY = "product_inquiry"
    ORDER_QUERY = "order_query"
    SHIPPING_QUERY = "shipping_query"
    REFUND_REQUEST = "refund_request"
    PRICE_INQUIRY = "price_inquiry"
    STOCK_INQUIRY = "stock_inquiry"
    COUPON_INQUIRY = "coupon_inquiry"
    GENERAL_GREETING = "general_greeting"
    COMPLAINT = "complaint"
    OTHER = "other"


@dataclass
class IntentResult:
    intent: IntentType
    confidence: float
    entities: Dict[str, Any]
    raw_text: str


@dataclass
class ChatContext:
    session_id: str
    buyer_id: str
    platform: str
    shop_id: int
    history: List[Dict[str, str]]
    order_info: Optional[Dict[str, Any]] = None


class IntentClassifier:
    
    INTENT_PATTERNS = {
        IntentType.PRODUCT_INQUIRY: [
            r"这个商品", r"有货吗", r"质量怎么样", r"是正品吗",
            r"什么材质", r"多大", r"多重", r"颜色", r"尺寸"
        ],
        IntentType.ORDER_QUERY: [
            r"订单", r"下单", r"我的单子", r"买了", r"付款",
            r"订单号", r"订单状态", r"查订单"
        ],
        IntentType.SHIPPING_QUERY: [
            r"发货", r"快递", r"物流", r"什么时候到",
            r"运费", r"包邮", r"顺丰", r"到货"
        ],
        IntentType.REFUND_REQUEST: [
            r"退款", r"退货", r"退换", r"不要了",
            r"取消订单", r"申请退款", r"七天无理由"
        ],
        IntentType.PRICE_INQUIRY: [
            r"多少钱", r"价格", r"优惠", r"打折",
            r"能便宜", r"最低价", r"团购"
        ],
        IntentType.STOCK_INQUIRY: [
            r"有货吗", r"库存", r"现货", r"什么时候有货",
            r"缺货", r"断货"
        ],
        IntentType.COUPON_INQUIRY: [
            r"优惠券", r"满减", r"折扣", r"活动",
            r"领券", r"用券"
        ],
        IntentType.GENERAL_GREETING: [
            r"你好", r"在吗", r"亲", r"老板",
            r"客服", r"有人吗"
        ],
        IntentType.COMPLAINT: [
            r"投诉", r"差评", r"骗子", r"假货",
            r"质量差", r"欺骗", r"举报"
        ]
    }
    
    ENTITY_PATTERNS = {
        "order_id": r"[A-Z]{2}\d{12,20}",
        "phone": r"1[3-9]\d{9}",
        "product_name": r"【(.+?)】",
        "amount": r"(\d+(?:\.\d{1,2})?)\s*[元块]",
    }
    
    def classify(self, text: str) -> IntentResult:
        scores: Dict[IntentType, float] = {}
        
        text_lower = text.lower()
        
        for intent_type, patterns in self.INTENT_PATTERNS.items():
            score = 0.0
            for pattern in patterns:
                if re.search(pattern, text_lower):
                    score += 1.0
            scores[intent_type] = score / max(len(patterns), 1)
        
        best_intent = max(scores, key=scores.get)
        best_score = scores[best_intent]
        
        if best_score < 0.1:
            best_intent = IntentType.OTHER
            best_score = 0.5
        
        entities = self._extract_entities(text)
        
        return IntentResult(
            intent=best_intent,
            confidence=min(best_score * 1.5, 1.0),
            entities=entities,
            raw_text=text
        )
    
    def _extract_entities(self, text: str) -> Dict[str, Any]:
        entities = {}
        
        for entity_name, pattern in self.ENTITY_PATTERNS.items():
            match = re.search(pattern, text)
            if match:
                entities[entity_name] = match.group(1) if match.lastindex else match.group(0)
        
        return entities


class KnowledgeBaseMatcher:
    
    def __init__(self):
        self.knowledge_items: List[Dict[str, Any]] = []
    
    def load_knowledge(self, items: List[Dict[str, Any]]):
        self.knowledge_items = items
    
    def add_item(self, item: Dict[str, Any]):
        self.knowledge_items.append(item)
    
    def match(self, query: str, threshold: float = 0.3) -> Optional[Dict[str, Any]]:
        query_lower = query.lower()
        best_match = None
        best_score = 0.0
        
        for item in self.knowledge_items:
            score = self._calculate_similarity(query_lower, item)
            if score > best_score and score >= threshold:
                best_score = score
                best_match = {**item, "score": score}
        
        return best_match
    
    def _calculate_similarity(self, query: str, item: Dict[str, Any]) -> float:
        question = item.get("question", "").lower()
        keywords = item.get("keywords", [])
        if isinstance(keywords, str):
            keywords = keywords.split(",")
        
        score = 0.0
        
        if query in question or question in query:
            score += 0.5
        
        for keyword in keywords:
            keyword = keyword.strip().lower()
            if keyword in query:
                score += 0.2
        
        return min(score, 1.0)


class AIResponder:
    
    SYSTEM_PROMPT = """你是一个专业的电商客服助手。你的职责是:
1. 友好、专业地回答客户问题
2. 对于产品咨询，详细介绍商品特点
3. 对于订单查询，引导客户提供订单号
4. 对于售后问题，提供解决方案
5. 如果无法处理，建议转人工客服

回复要求：
- 简洁友好，不超过100字
- 使用礼貌用语
- 不要编造信息
"""
    
    INTENT_RESPONSES = {
        IntentType.GENERAL_GREETING: [
            "您好！很高兴为您服务，请问有什么可以帮您的？",
            "亲，您好！有什么需要帮助的吗？",
            "您好，欢迎光临！请问有什么需要咨询的吗？"
        ],
        IntentType.PRODUCT_INQUIRY: [
            "这款商品是我们的热销款，品质优良。您具体想了解哪方面呢？",
            "这款商品很受欢迎！我可以为您详细介绍，您想了解什么呢？"
        ],
        IntentType.ORDER_QUERY: [
            "好的，请提供您的订单号，我帮您查询订单状态。",
            "请您提供订单号，我来帮您查一下订单情况。"
        ],
        IntentType.SHIPPING_QUERY: [
            "我们一般24小时内发货，默认发顺丰快递。您是想查询物流信息吗？",
            "订单付款后24小时内发出，您想查询具体订单的物流吗？"
        ],
        IntentType.REFUND_REQUEST: [
            "好的，请问是什么原因需要退款呢？我来帮您处理。",
            "抱歉给您带来不便，请问具体是什么问题？我来为您处理售后。"
        ],
        IntentType.PRICE_INQUIRY: [
            "目前商品价格已是最优惠价了，您可以放心购买~",
            "现在的价格很划算哦，而且下单还有优惠活动！"
        ],
        IntentType.STOCK_INQUIRY: [
            "这款商品目前有现货，下单后24小时内发货~",
            "亲，这款有货的，您可以直接下单~"
        ],
        IntentType.COUPON_INQUIRY: [
            "目前店铺有满减活动，下单时自动抵扣哦~",
            "您可以关注店铺，领取优惠券后再下单更划算！"
        ],
        IntentType.COMPLAINT: [
            "非常抱歉给您带来不好的体验，请您详细说明情况，我来帮您处理。",
            "抱歉！请您具体说明问题，我会尽快为您解决。"
        ]
    }
    
    NEED_HUMAN_INTENTS = {
        IntentType.COMPLAINT,
        IntentType.REFUND_REQUEST
    }
    
    def __init__(self, api_key: str = None, base_url: str = None):
        self.api_key = api_key
        self.base_url = base_url or "https://api.openai.com/v1"
        self.client = httpx.AsyncClient(timeout=30.0)
        self.knowledge_matcher = KnowledgeBaseMatcher()
    
    async def generate_response(
        self,
        user_message: str,
        intent: IntentResult,
        context: Optional[ChatContext] = None
    ) -> Tuple[str, bool]:
        kb_match = self.knowledge_matcher.match(user_message)
        if kb_match:
            return kb_match.get("answer", ""), False
        
        if intent.intent in self.INTENT_RESPONSES:
            import random
            responses = self.INTENT_RESPONSES[intent.intent]
            response = random.choice(responses)
            
            need_human = intent.intent in self.NEED_HUMAN_INTENTS
            if intent.confidence < 0.5:
                need_human = True
            
            return response, need_human
        
        if self.api_key:
            return await self._generate_ai_response(user_message, context)
        
        return "您好，这个问题我需要转给人工客服处理，请稍等~", True
    
    async def _generate_ai_response(
        self,
        user_message: str,
        context: Optional[ChatContext] = None
    ) -> Tuple[str, bool]:
        messages = [
            {"role": "system", "content": self.SYSTEM_PROMPT}
        ]
        
        if context and context.history:
            for msg in context.history[-5:]:
                messages.append({"role": msg.get("role", "user"), "content": msg.get("content", "")})
        
        messages.append({"role": "user", "content": user_message})
        
        try:
            response = await self.client.post(
                f"{self.base_url}/chat/completions",
                headers={
                    "Authorization": f"Bearer {self.api_key}",
                    "Content-Type": "application/json"
                },
                json={
                    "model": "gpt-3.5-turbo",
                    "messages": messages,
                    "max_tokens": 150,
                    "temperature": 0.7
                }
            )
            
            if response.status_code == 200:
                data = response.json()
                reply = data["choices"][0]["message"]["content"]
                return reply.strip(), False
            else:
                logger.error(f"AI API错误: {response.status_code}")
                return "抱歉，我暂时无法回答这个问题，稍后为您转人工客服~", True
                
        except Exception as e:
            logger.error(f"AI响应生成异常: {e}")
            return "抱歉，系统暂时繁忙，请稍后再试~", False
    
    async def close(self):
        await self.client.aclose()


class CustomerServiceEngine:
    
    def __init__(self, ai_api_key: str = None, ai_base_url: str = None):
        self.intent_classifier = IntentClassifier()
        self.ai_responder = AIResponder(ai_api_key, ai_base_url)
    
    async def process_message(
        self,
        message: str,
        context: Optional[ChatContext] = None
    ) -> Dict[str, Any]:
        intent_result = self.intent_classifier.classify(message)
        
        response, need_human = await self.ai_responder.generate_response(
            message, intent_result, context
        )
        
        return {
            "response": response,
            "intent": intent_result.intent.value,
            "confidence": intent_result.confidence,
            "entities": intent_result.entities,
            "need_human": need_human
        }
    
    def load_knowledge(self, items: List[Dict[str, Any]]):
        self.ai_responder.knowledge_matcher.load_knowledge(items)
    
    async def close(self):
        await self.ai_responder.close()