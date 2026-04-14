# 500错误修复指南

## 问题原因
API网关的白名单路径配置错误，导致登录接口被拦截需要认证。

## 快速解决方案

### 方案1: 修复白名单配置（推荐）

编辑文件 `java-services/api-gateway/src/main/java/com/ecommerce/gateway/filter/AuthGlobalFilter.java`

将第30-38行的 WHITE_LIST 改为：
```java
private static final List<String> WHITE_LIST = List.of(
    "/api/v1/auth/login",
    "/api/v1/auth/register",
    "/api/v1/auth/send-sms",
    "/api/v1/auth/health",
    "/actuator",
    "/health",
    "/api/v1/platform/*/callback",
    "/api/v1/platform/*/verify"
);
```

然后重新编译并启动服务。

### 方案2: 临时禁用认证过滤器

在 `application-dev.yml` 中添加：
```yaml
spring:
  cloud:
    gateway:
      default-filters:
        - name: AuthGlobalFilter
          enabled: false
```

### 方案3: 使用IDE重新编译

1. 用 IntelliJ IDEA 打开项目
2. 右键 api-gateway 模块 -> Rebuild Module
3. 运行 GatewayApplication

### 方案4: 配置JAVA_HOME

```bash
# 检查Java安装位置
find /Library/Java -name "java" 2>/dev/null
find /opt -name "java" 2>/dev/null

# 设置JAVA_HOME
export JAVA_HOME=$(/usr/libexec/java_home 2>/dev/null || echo "/Library/Java/JavaVirtualMachines/xxx/Contents/Home")
export PATH=$JAVA_HOME/bin:$PATH

# 验证
java -version
mvn -version

# 重新编译
cd java-services
mvn clean package -DskipTests
```

## 重启服务

```bash
# 进入项目目录
cd ~/Desktop/ecommerce-customer-service-system/java-services

# 启动所有服务（后台运行）
nohup java -jar api-gateway/target/api-gateway-1.0.0-SNAPSHOT.jar --spring.profiles.active=dev --server.port=8080 > logs/api-gateway.log 2>&1 &
nohup java -jar auth-service/target/auth-service-1.0.0.jar --spring.profiles.active=dev --server.port=8083 > logs/auth-service.log 2>&1 &
nohup java -jar order-service/target/order-service-1.0.0-SNAPSHOT.jar --spring.profiles.active=dev --server.port=8084 > logs/order-service.log 2>&1 &
nohup java -jar product-service/target/product-service-1.0.0-SNAPSHOT.jar --spring.profiles.active=dev --server.port=8086 > logs/product-service.log 2>&1 &
```

## 验证修复

```bash
# 测试登录接口（应该返回401未找到用户，而不是401 Missing token）
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'
```