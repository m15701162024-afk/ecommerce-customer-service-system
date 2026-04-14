"""
采购服务业务逻辑验证脚本
验证金额规则:
- 小于1000元: 自动支付
- 1000-10000元: 需人工确认
- 大于10000元: 需人工确认并预警
"""

from decimal import Decimal
from dataclasses import dataclass
from enum import Enum
from datetime import datetime
from typing import Optional

class OrderStatus(Enum):
    PENDING = "PENDING"
    AUTO_PAID = "AUTO_PAID"
    CONFIRMING = "CONFIRMING"
    WARNING = "WARNING"
    COMPLETED = "COMPLETED"
    CANCELLED = "CANCELLED"
    REJECTED = "REJECTED"

@dataclass
class PurchaseOrder:
    id: int
    order_no: str
    supplier_name: str
    product_name: str
    quantity: int
    unit_price: Decimal
    total_amount: Optional[Decimal] = None
    status: Optional[OrderStatus] = None
    remark: Optional[str] = None
    
    def __post_init__(self):
        if self.total_amount is None:
            self.total_amount = self.unit_price * self.quantity

class PurchaseService:
    AUTO_PAY_THRESHOLD = Decimal("1000")
    WARNING_THRESHOLD = Decimal("10000")
    
    def determine_status(self, amount: Decimal) -> OrderStatus:
        if amount < self.AUTO_PAY_THRESHOLD:
            return OrderStatus.AUTO_PAID
        elif amount <= self.WARNING_THRESHOLD:
            return OrderStatus.CONFIRMING
        else:
            return OrderStatus.WARNING
    
    def create_order(self, order: PurchaseOrder) -> PurchaseOrder:
        order.total_amount = order.unit_price * order.quantity
        order.status = self.determine_status(order.total_amount)
        return order
    
    def confirm_order(self, order: PurchaseOrder) -> PurchaseOrder:
        if order.status not in [OrderStatus.CONFIRMING, OrderStatus.WARNING]:
            raise ValueError(f"Cannot confirm order with status {order.status}")
        order.status = OrderStatus.COMPLETED
        return order
    
    def reject_order(self, order: PurchaseOrder, reason: str) -> PurchaseOrder:
        if order.status in [OrderStatus.COMPLETED, OrderStatus.CANCELLED]:
            raise ValueError(f"Cannot reject order with status {order.status}")
        order.status = OrderStatus.REJECTED
        order.remark = reason
        return order
    
    def cancel_order(self, order: PurchaseOrder) -> PurchaseOrder:
        if order.status == OrderStatus.COMPLETED:
            raise ValueError("Cannot cancel completed order")
        order.status = OrderStatus.CANCELLED
        return order

def run_tests():
    service = PurchaseService()
    tests_passed = 0
    tests_failed = 0
    
    print("=" * 60)
    print("采购服务业务逻辑测试")
    print("=" * 60)
    
    # Test 1: 金额小于1000自动支付
    print("\n[测试1] 金额小于1000元 - 应自动支付")
    order1 = PurchaseOrder(1, "PO001", "供应商A", "商品A", 10, Decimal("50"))
    result = service.create_order(order1)
    expected = OrderStatus.AUTO_PAID
    if result.status == expected:
        print(f"  ✅ 通过: 金额={result.total_amount}, 状态={result.status.value}")
        tests_passed += 1
    else:
        print(f"  ❌ 失败: 期望={expected.value}, 实际={result.status.value}")
        tests_failed += 1
    
    # Test 2: 金额1000-10000需确认
    print("\n[测试2] 金额1000-10000元 - 需人工确认")
    order2 = PurchaseOrder(2, "PO002", "供应商B", "商品B", 20, Decimal("100"))
    result = service.create_order(order2)
    expected = OrderStatus.CONFIRMING
    if result.status == expected:
        print(f"  ✅ 通过: 金额={result.total_amount}, 状态={result.status.value}")
        tests_passed += 1
    else:
        print(f"  ❌ 失败: 期望={expected.value}, 实际={result.status.value}")
        tests_failed += 1
    
    # Test 3: 金额超过10000预警
    print("\n[测试3] 金额超过10000元 - 预警状态")
    order3 = PurchaseOrder(3, "PO003", "供应商C", "商品C", 60, Decimal("200"))
    result = service.create_order(order3)
    expected = OrderStatus.WARNING
    if result.status == expected:
        print(f"  ✅ 通过: 金额={result.total_amount}, 状态={result.status.value}")
        tests_passed += 1
    else:
        print(f"  ❌ 失败: 期望={expected.value}, 实际={result.status.value}")
        tests_failed += 1
    
    # Test 4: 确认订单
    print("\n[测试4] 确认订单")
    order4 = PurchaseOrder(4, "PO004", "供应商D", "商品D", 15, Decimal("100"))
    service.create_order(order4)
    result = service.confirm_order(order4)
    expected = OrderStatus.COMPLETED
    if result.status == expected:
        print(f"  ✅ 通过: 确认后状态={result.status.value}")
        tests_passed += 1
    else:
        print(f"  ❌ 失败: 期望={expected.value}, 实际={result.status.value}")
        tests_failed += 1
    
    # Test 5: 确认已完成订单应报错
    print("\n[测试5] 确认已完成订单 - 应报错")
    try:
        service.confirm_order(order4)
        print(f"  ❌ 失败: 应该抛出异常但没有")
        tests_failed += 1
    except ValueError as e:
        print(f"  ✅ 通过: 正确抛出异常 - {e}")
        tests_passed += 1
    
    # Test 6: 拒绝订单
    print("\n[测试6] 拒绝订单")
    order6 = PurchaseOrder(6, "PO006", "供应商F", "商品F", 10, Decimal("150"))
    service.create_order(order6)
    result = service.reject_order(order6, "测试拒绝原因")
    expected_status = OrderStatus.REJECTED
    if result.status == expected_status and result.remark == "测试拒绝原因":
        print(f"  ✅ 通过: 状态={result.status.value}, 原因={result.remark}")
        tests_passed += 1
    else:
        print(f"  ❌ 失败")
        tests_failed += 1
    
    # Test 7: 取消订单
    print("\n[测试7] 取消订单")
    order7 = PurchaseOrder(7, "PO007", "供应商G", "商品G", 5, Decimal("100"))
    service.create_order(order7)
    result = service.cancel_order(order7)
    expected = OrderStatus.CANCELLED
    if result.status == expected:
        print(f"  ✅ 通过: 取消后状态={result.status.value}")
        tests_passed += 1
    else:
        print(f"  ❌ 失败: 期望={expected.value}, 实际={result.status.value}")
        tests_failed += 1
    
    # Test 8: 取消已完成订单应报错
    print("\n[测试8] 取消已完成订单 - 应报错")
    completed_order = PurchaseOrder(8, "PO008", "供应商H", "商品H", 5, Decimal("50"))
    service.create_order(completed_order)
    completed_order.status = OrderStatus.COMPLETED
    try:
        service.cancel_order(completed_order)
        print(f"  ❌ 失败: 应该抛出异常但没有")
        tests_failed += 1
    except ValueError as e:
        print(f"  ✅ 通过: 正确抛出异常 - {e}")
        tests_passed += 1
    
    # Summary
    print("\n" + "=" * 60)
    print(f"测试结果: 通过={tests_passed}, 失败={tests_failed}, 总计={tests_passed + tests_failed}")
    print("=" * 60)
    
    return tests_failed == 0

if __name__ == "__main__":
    success = run_tests()
    exit(0 if success else 1)