#!/bin/bash

# ============================================
# 电商客服系统 - API端点测试脚本
# ============================================

BASE_URL="http://localhost:8080/api/v1"
AUTH_URL="${BASE_URL}/auth"
ORDER_URL="${BASE_URL}/orders"
PURCHASE_URL="${BASE_URL}/purchase"
PRODUCT_URL="${BASE_URL}/products"
CHAT_URL="${BASE_URL}/chat"

# 颜色输出
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 测试结果统计
TOTAL_TESTS=0
PASSED_TESTS=0
FAILED_TESTS=0

# 打印测试结果
print_result() {
    local test_name="$1"
    local response="$2"
    local expected_code="$3"
    
    TOTAL_TESTS=$((TOTAL_TESTS + 1))
    
    # 提取HTTP状态码
    http_code=$(echo "$response" | tail -n 1)
    
    if [ "$http_code" = "$expected_code" ]; then
        echo -e "${GREEN}✓${NC} $test_name (HTTP $http_code)"
        PASSED_TESTS=$((PASSED_TESTS + 1))
    else
        echo -e "${RED}✗${NC} $test_name (期望: $expected_code, 实际: $http_code)"
        FAILED_TESTS=$((FAILED_TESTS + 1))
    fi
}

# 打印分隔线
print_separator() {
    echo "========================================"
}

echo ""
echo "========================================"
echo "电商客服系统 - API端点测试"
echo "========================================"
echo ""

# 检查服务是否运行
echo ">>> 检查服务状态..."
if curl -s -o /dev/null -w "%{http_code}" "${BASE_URL}/../health" | grep -q "200\|404"; then
    echo -e "${GREEN}✓${NC} API网关运行中"
else
    echo -e "${YELLOW}!${NC} API网关未运行，部分测试可能失败"
fi
echo ""

# ============================================
# 认证服务测试
# ============================================
print_separator
echo "认证服务测试"
print_separator

# 测试1: 用户登录
echo ""
echo ">>> 测试用户登录..."
RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "${AUTH_URL}/login" \
    -H "Content-Type: application/json" \
    -d '{"username":"admin","password":"admin123"}')
print_result "用户登录" "$RESPONSE" "200"

# 提取Token
TOKEN=$(echo "$RESPONSE" | head -n -1 | grep -o '"token":"[^"]*' | sed 's/"token":"//')
if [ -n "$TOKEN" ]; then
    echo "  Token获取成功"
else
    echo "  Token获取失败，使用默认值"
    TOKEN="test-token"
fi

# 测试2: 用户注册
echo ""
echo ">>> 测试用户注册..."
RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "${AUTH_URL}/register" \
    -H "Content-Type: application/json" \
    -d "{\"username\":\"testuser_$(date +%s)\",\"password\":\"test123\",\"email\":\"test@example.com\"}")
print_result "用户注册" "$RESPONSE" "200"

# 测试3: Token验证
echo ""
echo ">>> 测试Token验证..."
RESPONSE=$(curl -s -w "\n%{http_code}" -X GET "${AUTH_URL}/validate" \
    -H "Authorization: Bearer ${TOKEN}")
print_result "Token验证" "$RESPONSE" "200"

# ============================================
# 订单服务测试
# ============================================
echo ""
print_separator
echo "订单服务测试"
print_separator

# 测试4: 获取订单列表
echo ""
echo ">>> 测试获取订单列表..."
RESPONSE=$(curl -s -w "\n%{http_code}" -X GET "${ORDER_URL}/list?page=1&size=10" \
    -H "Authorization: Bearer ${TOKEN}")
print_result "获取订单列表" "$RESPONSE" "200"

# 测试5: 订单同步
echo ""
echo ">>> 测试订单同步..."
RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "${ORDER_URL}/sync" \
    -H "Authorization: Bearer ${TOKEN}" \
    -H "Content-Type: application/json" \
    -d '{"platform":"DOUYIN"}')
print_result "订单同步" "$RESPONSE" "200"

# ============================================
# 采购服务测试
# ============================================
echo ""
print_separator
echo "采购服务测试"
print_separator

# 测试6: 获取采购列表
echo ""
echo ">>> 测试获取采购列表..."
RESPONSE=$(curl -s -w "\n%{http_code}" -X GET "${PURCHASE_URL}/list" \
    -H "Authorization: Bearer ${TOKEN}")
print_result "获取采购列表" "$RESPONSE" "200"

# 测试7: 创建采购订单
echo ""
echo ">>> 测试创建采购订单..."
RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "${PURCHASE_URL}/create" \
    -H "Authorization: Bearer ${TOKEN}" \
    -H "Content-Type: application/json" \
    -d '{"supplierName":"测试供应商","productName":"测试商品","quantity":10,"unitPrice":50.00}')
print_result "创建采购订单" "$RESPONSE" "200"

# 测试8: 获取采购统计
echo ""
echo ">>> 测试获取采购统计..."
RESPONSE=$(curl -s -w "\n%{http_code}" -X GET "${PURCHASE_URL}/stats" \
    -H "Authorization: Bearer ${TOKEN}")
print_result "获取采购统计" "$RESPONSE" "200"

# 测试9: 获取待人工确认列表
echo ""
echo ">>> 测试获取待人工确认列表..."
RESPONSE=$(curl -s -w "\n%{http_code}" -X GET "${PURCHASE_URL}/manual-confirm-list" \
    -H "Authorization: Bearer ${TOKEN}")
print_result "获取待人工确认列表" "$RESPONSE" "200"

# ============================================
# 商品服务测试
# ============================================
echo ""
print_separator
echo "商品服务测试"
print_separator

# 测试10: 获取商品列表
echo ""
echo ">>> 测试获取商品列表..."
RESPONSE=$(curl -s -w "\n%{http_code}" -X GET "${PRODUCT_URL}/list?page=1&size=10" \
    -H "Authorization: Bearer ${TOKEN}")
print_result "获取商品列表" "$RESPONSE" "200"

# 测试11: 创建商品
echo ""
echo ">>> 测试创建商品..."
RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "${PRODUCT_URL}" \
    -H "Authorization: Bearer ${TOKEN}" \
    -H "Content-Type: application/json" \
    -d '{"name":"测试商品","platform":"DOUYIN","price":99.00,"stock":100}')
print_result "创建商品" "$RESPONSE" "200"

# ============================================
# 客服服务测试
# ============================================
echo ""
print_separator
echo "客服服务测试"
print_separator

# 测试12: 获取会话列表
echo ""
echo ">>> 测试获取会话列表..."
RESPONSE=$(curl -s -w "\n%{http_code}" -X GET "${CHAT_URL}/sessions" \
    -H "Authorization: Bearer ${TOKEN}")
print_result "获取会话列表" "$RESPONSE" "200"

# 测试13: 发送消息
echo ""
echo ">>> 测试发送消息..."
RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "${CHAT_URL}/send" \
    -H "Authorization: Bearer ${TOKEN}" \
    -H "Content-Type: application/json" \
    -d '{"session_id":"test-session-001","message":"你好，有什么可以帮助我的？","buyer_id":"buyer-001"}')
print_result "发送消息" "$RESPONSE" "200"

# ============================================
# 平台服务测试
# ============================================
echo ""
print_separator
echo "平台服务测试"
print_separator

# 测试14: 抖音平台健康检查
echo ""
echo ">>> 测试抖音平台健康检查..."
RESPONSE=$(curl -s -w "\n%{http_code}" -X GET "${BASE_URL}/platform/douyin/callback/verify?signature=test&timestamp=123&nonce=456&echostr=hello")
print_result "抖音平台验证" "$RESPONSE" "200"

# 测试15: 小红书平台健康检查
echo ""
echo ">>> 测试小红书平台健康检查..."
RESPONSE=$(curl -s -w "\n%{http_code}" -X GET "${BASE_URL}/platform/xiaohongshu/callback/verify?signature=test&timestamp=123&nonce=456&echostr=hello")
print_result "小红书平台验证" "$RESPONSE" "200"

# 测试16: 1688平台健康检查
echo ""
echo ">>> 测试1688平台健康检查..."
RESPONSE=$(curl -s -w "\n%{http_code}" -X GET "${BASE_URL}/platform/1688/callback/verify?signature=test&timestamp=123&nonce=456&echostr=hello")
print_result "1688平台验证" "$RESPONSE" "200"

# ============================================
# 测试结果汇总
# ============================================
echo ""
print_separator
echo "测试结果汇总"
print_separator
echo ""
echo -e "总计测试: ${TOTAL_TESTS}"
echo -e "${GREEN}通过: ${PASSED_TESTS}${NC}"
echo -e "${RED}失败: ${FAILED_TESTS}${NC}"
echo ""

if [ $FAILED_TESTS -eq 0 ]; then
    echo -e "${GREEN}所有测试通过!${NC}"
    exit 0
else
    echo -e "${RED}存在失败的测试，请检查日志。${NC}"
    exit 1
fi