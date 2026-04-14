const express = require('express');
const cors = require('cors');
const bodyParser = require('body-parser');

const app = express();
const PORT = 8080;

app.use(cors());
app.use(bodyParser.json());

// 模拟用户数据
const users = new Map();
users.set('admin', { id: 1, username: 'admin', password: 'admin123', realName: '管理员', roles: ['ADMIN'] });
users.set('test', { id: 2, username: 'test', password: 'test123', realName: '测试用户', roles: ['USER'] });

// 模拟Token存储
const tokens = new Map();
let currentUserId = 3;

// 生成Token
function generateToken() {
  return 'mock_token_' + Date.now() + '_' + Math.random().toString(36).substr(2, 9);
}

// 认证中间件
function authMiddleware(req, res, next) {
  const authHeader = req.headers.authorization;
  if (!authHeader || !authHeader.startsWith('Bearer ')) {
    return res.status(401).json({ code: 401, message: '未授权' });
  }
  const token = authHeader.substring(7);
  const userId = tokens.get(token);
  if (!userId) {
    return res.status(401).json({ code: 401, message: 'Token无效' });
  }
  req.userId = userId;
  next();
}

// ==================== 认证API ====================

// 登录
app.post('/api/v1/auth/login', (req, res) => {
  const { username, password } = req.body;
  const user = users.get(username);
  
  if (!user || user.password !== password) {
    return res.status(401).json({ code: 401, message: '用户名或密码错误' });
  }
  
  const token = generateToken();
  tokens.set(token, user.id);
  
  res.json({
    code: 200,
    data: {
      token,
      user: {
        id: user.id,
        username: user.username,
        realName: user.realName,
        roles: user.roles
      }
    }
  });
});

// 注册
app.post('/api/v1/auth/register', (req, res) => {
  const { username, password, phone } = req.body;
  
  if (users.has(username)) {
    return res.status(400).json({ code: 400, message: '用户名已存在' });
  }
  
  const newUser = {
    id: currentUserId++,
    username,
    password,
    realName: username,
    phone,
    roles: ['USER']
  };
  users.set(username, newUser);
  
  const token = generateToken();
  tokens.set(token, newUser.id);
  
  res.json({
    code: 200,
    data: {
      token,
      user: {
        id: newUser.id,
        username: newUser.username,
        realName: newUser.realName,
        roles: newUser.roles
      }
    }
  });
});

// 获取用户信息
app.get('/api/v1/user/info', authMiddleware, (req, res) => {
  let userInfo = null;
  for (const user of users.values()) {
    if (user.id === req.userId) {
      userInfo = user;
      break;
    }
  }
  
  if (!userInfo) {
    return res.status(404).json({ code: 404, message: '用户不存在' });
  }
  
  res.json({
    code: 200,
    data: {
      id: userInfo.id,
      username: userInfo.username,
      realName: userInfo.realName,
      phone: userInfo.phone,
      roles: userInfo.roles
    }
  });
});

// 验证Token
app.get('/api/v1/auth/validate', authMiddleware, (req, res) => {
  res.json({ code: 200, data: { valid: true } });
});

// 登出
app.post('/api/v1/auth/logout', authMiddleware, (req, res) => {
  const authHeader = req.headers.authorization;
  const token = authHeader.substring(7);
  tokens.delete(token);
  res.json({ code: 200, message: '登出成功' });
});

// ==================== 订单API ====================

app.get('/api/v1/orders', authMiddleware, (req, res) => {
  const mockOrders = [];
  const statuses = ['pending', 'paid', 'shipped', 'completed'];
  const platforms = ['抖音', '淘宝', '小红书'];
  
  for (let i = 1; i <= 20; i++) {
    mockOrders.push({
      id: i,
      orderNo: 'ORD' + Date.now() + i.toString().padStart(4, '0'),
      platform: platforms[i % 3],
      buyerName: '买家' + i,
      productName: '商品' + i,
      amount: (Math.random() * 1000 + 50).toFixed(2),
      status: statuses[i % 4],
      statusText: ['待支付', '已支付', '已发货', '已完成'][i % 4],
      createdAt: new Date(Date.now() - i * 3600000).toISOString()
    });
  }
  
  res.json({
    code: 200,
    data: {
      list: mockOrders,
      total: 100,
      toShipCount: 5,
      refundCount: 2
    }
  });
});

// ==================== 商品API ====================

app.get('/api/v1/products', authMiddleware, (req, res) => {
  const products = [];
  const categories = ['手机通讯', '数码产品', '服装鞋帽'];
  const brands = ['Apple', '华为', '小米', 'OPPO', 'vivo', '三星'];
  
  for (let i = 1; i <= 50; i++) {
    const isActive = i % 3 === 0;
    products.push({
      id: i,
      name: `商品${i} - ${categories[i % 3]}系列`,
      sku: `SKU-${10000 + i}`,
      price: (Math.random() * 500 + 50).toFixed(2),
      stock: Math.floor(Math.random() * 150),
      status: isActive ? 'active' : 'inactive',
      categoryId: (i % 3) + 1,
      brandId: (i % 6) + 1,
      brand: brands[i % 6],
      category: categories[i % 3],
      mainImage: '',
      description: `这是商品${i}的详细描述信息`,
      createdAt: new Date(Date.now() - i * 3600000).toISOString()
    });
  }
  res.json({ code: 200, data: { list: products, total: 50 } });
});

// ==================== 客服API ====================

app.get('/api/v1/chat/sessions', authMiddleware, (req, res) => {
  const sessions = [];
  const platforms = ['抖音', '淘宝', '小红书'];
  for (let i = 1; i <= 10; i++) {
    sessions.push({
      id: i,
      buyerName: '客户' + i,
      lastMessage: '您好，请问商品有货吗？',
      time: new Date(Date.now() - i * 600000).toLocaleTimeString('zh-CN'),
      unread: i % 3,
      platform: platforms[i % 3],
      avatar: ''
    });
  }
  res.json({ code: 200, data: sessions });
});

app.post('/api/v1/chat/send', authMiddleware, (req, res) => {
  res.json({
    code: 200,
    data: {
      reply: '感谢您的咨询，商品有现货，24小时内发货。'
    }
  });
});

// ==================== 采购API ====================

app.get('/api/v1/purchase/stats', authMiddleware, (req, res) => {
  res.json({
    code: 200,
    data: {
      todayAmount: 12345.67,
      pendingCount: 8,
      completedCount: 25
    }
  });
});

app.post('/api/v1/purchase', authMiddleware, (req, res) => {
  res.json({ code: 200, message: '采购订单创建成功', data: { id: Date.now() } });
});

// ==================== 仪表盘统计API ====================

app.get('/api/v1/orders/trend', authMiddleware, (req, res) => {
  const { type = 'week' } = req.query;
  const days = type === 'week' ? 7 : 30;
  const labels = [];
  const orderCounts = [];
  const salesAmounts = [];
  
  for (let i = 0; i < days; i++) {
    labels.push(type === 'week' ? ['周一', '周二', '周三', '周四', '周五', '周六', '周日'][i] : `${i + 1}日`);
    orderCounts.push(Math.floor(Math.random() * 100 + 20));
    salesAmounts.push(Math.floor(Math.random() * 10000 + 1000));
  }
  
  res.json({
    code: 200,
    data: { labels, orderCounts, salesAmounts }
  });
});

app.get('/api/v1/orders/platform-distribution', authMiddleware, (req, res) => {
  res.json({
    code: 200,
    data: [
      { name: '抖音', value: 45 },
      { name: '淘宝', value: 35 },
      { name: '小红书', value: 20 }
    ]
  });
});

// ==================== 监控API ====================

app.get('/api/v1/monitor/health', authMiddleware, (req, res) => {
  res.json({
    code: 200,
    data: [
      { name: 'API Gateway', healthy: true, cpu: 23, memory: 45 },
      { name: 'Order Service', healthy: true, cpu: 34, memory: 56 },
      { name: 'Auth Service', healthy: true, cpu: 12, memory: 34 },
      { name: 'Product Service', healthy: true, cpu: 45, memory: 67 },
      { name: 'Chat Service', healthy: true, cpu: 23, memory: 45 },
      { name: 'Database', healthy: true, cpu: 34, memory: 78 }
    ]
  });
});

app.get('/api/v1/monitor/logs', authMiddleware, (req, res) => {
  const logs = [];
  const levels = ['INFO', 'WARN', 'ERROR'];
  const services = ['API Gateway', 'Order Service', 'Auth Service'];
  
  for (let i = 0; i < 20; i++) {
    logs.push({
      time: new Date(Date.now() - i * 60000).toISOString().replace('T', ' ').substr(0, 19),
      level: levels[i % 3],
      service: services[i % 3],
      message: i % 3 === 0 ? 'Request processed successfully' : i % 3 === 1 ? 'High latency detected' : 'Connection timeout'
    });
  }
  res.json({ code: 200, data: logs });
});

// ==================== 平台API ====================

app.get('/api/v1/platforms', authMiddleware, (req, res) => {
  res.json({
    code: 200,
    data: [
      { code: 'douyin', name: '抖音', connected: false, stats: { orders: 0, messages: 0 } },
      { code: 'taobao', name: '淘宝', connected: false, stats: { orders: 0, messages: 0 } },
      { code: 'xiaohongshu', name: '小红书', connected: false, stats: { orders: 0, messages: 0 } },
      { code: 'a1688', name: '1688', connected: false, stats: { orders: 0, messages: 0 } }
    ]
  });
});

// ==================== OAuth API ====================

app.get('/api/v1/oauth/:platform/authorize', authMiddleware, (req, res) => {
  const { platform } = req.params;
  const mockUrl = `https://example.com/oauth/${platform}?client_id=mock&redirect_uri=http://localhost:3000/oauth/callback/${platform}`;
  res.json({ code: 200, data: { authorizeUrl: mockUrl } });
});

app.post('/api/v1/oauth/:platform/bind', authMiddleware, (req, res) => {
  res.json({ code: 200, message: '绑定成功' });
});

app.get('/api/v1/oauth/status', authMiddleware, (req, res) => {
  res.json({
    code: 200,
    data: {
      douyin: false,
      xiaohongshu: false,
      a1688: false
    }
  });
});

// ==================== 店铺API ====================

app.get('/api/v1/shops', authMiddleware, (req, res) => {
  res.json({ code: 200, data: [] });
});

// ==================== 其他API ====================

app.get('/api/v1/customers', authMiddleware, (req, res) => {
  const customers = [];
  for (let i = 1; i <= 15; i++) {
    customers.push({
      id: i,
      name: '客户' + i,
      phone: '138' + Math.floor(Math.random() * 100000000).toString().padStart(8, '0'),
      level: ['普通', 'VIP', 'SVIP'][i % 3],
      totalOrders: Math.floor(Math.random() * 50),
      totalAmount: (Math.random() * 10000).toFixed(2)
    });
  }
  res.json({ code: 200, data: { list: customers, total: 50 } });
});

app.get('/api/v1/work-orders', authMiddleware, (req, res) => {
  res.json({ code: 200, data: { list: [], total: 0 } });
});

app.get('/api/v1/after-sales', authMiddleware, (req, res) => {
  res.json({ code: 200, data: { list: [], total: 0 } });
});

app.get('/api/v1/knowledge', authMiddleware, (req, res) => {
  res.json({ code: 200, data: { list: [], total: 0 } });
});

// ==================== 订单详情 ====================
app.get('/api/v1/orders/:orderNo', authMiddleware, (req, res) => {
  const { orderNo } = req.params;
  res.json({
    code: 200,
    data: {
      orderNo,
      platform: '抖音',
      buyerName: '买家用户',
      buyerPhone: '138****8888',
      buyerAddress: '浙江省杭州市西湖区xxx路xxx号',
      totalAmount: 299.00,
      payAmount: 279.00,
      status: 'paid',
      statusText: '已支付',
      createdAt: new Date().toISOString(),
      items: [
        { id: 1, productName: '商品A', quantity: 2, price: 139.50, total: 279.00 }
      ],
      logistics: { company: '顺丰速运', trackingNo: 'SF1234567890', status: '运输中' }
    }
  });
});

app.put('/api/v1/orders/:orderNo/status', authMiddleware, (req, res) => {
  res.json({ code: 200, message: '状态更新成功' });
});

// ==================== 采购详情 ====================
app.get('/api/v1/purchase/:orderNo', authMiddleware, (req, res) => {
  res.json({
    code: 200,
    data: {
      orderNo: req.params.orderNo,
      supplierName: '供应商A',
      totalAmount: 199.00,
      status: 'pending',
      items: [{ productName: '商品A', quantity: 2, price: 99.50 }]
    }
  });
});

app.post('/api/v1/purchase/:id/confirm', authMiddleware, (req, res) => {
  res.json({ code: 200, message: '确认成功' });
});

app.post('/api/v1/purchase/:id/reject', authMiddleware, (req, res) => {
  res.json({ code: 200, message: '拒绝成功' });
});

// ==================== 售后操作 ====================
app.post('/api/v1/after-sales/:id/approve', authMiddleware, (req, res) => {
  res.json({ code: 200, message: '审批通过' });
});

app.post('/api/v1/after-sales/:id/reject', authMiddleware, (req, res) => {
  res.json({ code: 200, message: '已拒绝' });
});

app.post('/api/v1/after-sales/:id/refund', authMiddleware, (req, res) => {
  res.json({ code: 200, message: '退款成功' });
});

// ==================== 工单操作 ====================
app.post('/api/v1/work-orders', authMiddleware, (req, res) => {
  res.json({ code: 200, message: '工单创建成功', data: { id: Date.now() } });
});

app.post('/api/v1/work-orders/assign', authMiddleware, (req, res) => {
  res.json({ code: 200, message: '分配成功' });
});

app.post('/api/v1/work-orders/resolve', authMiddleware, (req, res) => {
  res.json({ code: 200, message: '已解决' });
});

// ==================== 商品分类API ====================

app.get('/api/v1/categories', (req, res) => {
  res.json({
    code: 200,
    data: [
      { 
        id: 1, 
        name: '手机通讯', 
        value: 1,
        children: [
          { id: 11, name: '手机', value: 11 },
          { id: 12, name: '手机配件', value: 12 },
          { id: 13, name: '手机壳', value: 13 },
          { id: 14, name: '充电器', value: 14 }
        ]
      },
      { 
        id: 2, 
        name: '数码产品', 
        value: 2,
        children: [
          { id: 21, name: '相机', value: 21 },
          { id: 22, name: '智能设备', value: 22 },
          { id: 23, name: '平板电脑', value: 23 }
        ]
      },
      { 
        id: 3, 
        name: '服装鞋帽', 
        value: 3,
        children: [
          { id: 31, name: '男装', value: 31 },
          { id: 32, name: '女装', value: 32 },
          { id: 33, name: '童装', value: 33 }
        ]
      }
    ]
  });
});

// ==================== 品牌API ====================

app.get('/api/v1/brands', (req, res) => {
  res.json({
    code: 200,
    data: [
      { id: 1, name: 'Apple' },
      { id: 2, name: '华为' },
      { id: 3, name: '小米' },
      { id: 4, name: 'OPPO' },
      { id: 5, name: 'vivo' },
      { id: 6, name: '三星' },
      { id: 7, name: 'Nike' },
      { id: 8, name: 'Adidas' }
    ]
  });
});

// ==================== Dashboard统计API ====================

app.get('/api/v1/dashboard/stats', authMiddleware, (req, res) => {
  res.json({
    code: 200,
    data: {
      todayOrders: 156,
      todaySales: 45678.90,
      pendingChat: 12,
      pendingPurchase: 8
    }
  });
});

app.get('/api/v1/dashboard/order-trend', authMiddleware, (req, res) => {
  const { days = 7 } = req.query;
  const labels = [];
  const values = [];
  
  for (let i = 0; i < days; i++) {
    labels.push(['周一', '周二', '周三', '周四', '周五', '周六', '周日'][i] || `${i + 1}日`);
    values.push(Math.floor(Math.random() * 100 + 50));
  }
  
  res.json({
    code: 200,
    data: { labels, values }
  });
});

app.get('/api/v1/dashboard/platform-distribution', authMiddleware, (req, res) => {
  res.json({
    code: 200,
    data: [
      { name: '抖音', value: 45 },
      { name: '淘宝', value: 35 },
      { name: '小红书', value: 20 }
    ]
  });
});

app.get('/api/v1/dashboard/pending-tasks', authMiddleware, (req, res) => {
  res.json({
    code: 200,
    data: [
      { id: 1, type: 'order', title: '待发货订单', count: 5, urgency: 'high' },
      { id: 2, type: 'refund', title: '待处理退款', count: 3, urgency: 'medium' },
      { id: 3, type: 'purchase', title: '待采购商品', count: 8, urgency: 'low' }
    ]
  });
});

app.get('/api/v1/dashboard/recent-orders', authMiddleware, (req, res) => {
  const { limit = 5 } = req.query;
  const orders = [];
  const platforms = ['抖音', '淘宝', '小红书'];
  
  for (let i = 1; i <= limit; i++) {
    orders.push({
      id: i,
      orderNo: 'ORD' + Date.now() + i.toString().padStart(4, '0'),
      platform: platforms[i % 3],
      buyerName: '买家' + i,
      productName: '商品' + i,
      amount: (Math.random() * 500 + 50).toFixed(2),
      status: ['pending', 'paid', 'shipped'][i % 3],
      createdAt: new Date(Date.now() - i * 3600000).toISOString()
    });
  }
  
  res.json({
    code: 200,
    data: orders
  });
});

// ==================== 商品详情API ====================

app.get('/api/v1/products/:id', authMiddleware, (req, res) => {
  const { id } = req.params;
  res.json({
    code: 200,
    data: {
      id,
      name: '商品' + id,
      sku: 'SKU-' + id,
      price: (Math.random() * 200 + 50).toFixed(2),
      stock: Math.floor(Math.random() * 100),
      status: 'active',
      categoryId: 1,
      brandId: 1,
      mainImage: '',
      images: [],
      description: '这是一个商品描述',
      createdAt: new Date().toISOString()
    }
  });
});

// ==================== 商品操作 ====================
app.post('/api/v1/products', authMiddleware, (req, res) => {
  res.json({ code: 200, message: '商品创建成功', data: { id: Date.now() } });
});

app.put('/api/v1/products/:id', authMiddleware, (req, res) => {
  res.json({ code: 200, message: '商品更新成功' });
});

app.delete('/api/v1/products/:id', authMiddleware, (req, res) => {
  res.json({ code: 200, message: '商品删除成功' });
});

// ==================== 平台操作 ====================
app.post('/api/v1/platforms/:platform/connect', authMiddleware, (req, res) => {
  res.json({ code: 200, message: '连接成功' });
});

app.post('/api/v1/platforms/:platform/sync', authMiddleware, (req, res) => {
  res.json({ code: 200, message: '同步成功' });
});

app.put('/api/v1/platforms/:platform/config', authMiddleware, (req, res) => {
  res.json({ code: 200, message: '配置保存成功' });
});

// ==================== 客服聊天API ====================

app.get('/api/v1/chat/sessions', authMiddleware, (req, res) => {
  const sessions = [];
  const platforms = ['抖音', '淘宝', '小红书'];
  for (let i = 1; i <= 10; i++) {
    sessions.push({
      id: i,
      buyerName: '客户' + i,
      lastMessage: '您好，请问商品有货吗？',
      time: new Date(Date.now() - i * 600000).toLocaleTimeString('zh-CN'),
      unread: i % 3,
      platform: platforms[i % 3],
      avatar: '',
      status: i % 2 === 0 ? 'online' : 'offline'
    });
  }
  res.json({ code: 200, data: sessions });
});

app.get('/api/v1/chat/messages/:sessionId', authMiddleware, (req, res) => {
  const messages = [];
  for (let i = 0; i < 10; i++) {
    messages.push({
      id: i,
      type: i % 2 === 0 ? 'buyer' : 'service',
      content: i % 2 === 0 ? '请问这个商品有货吗？' : '有的，现在有现货，24小时内发货',
      time: new Date(Date.now() - (10 - i) * 60000).toISOString()
    });
  }
  res.json({ code: 200, data: messages });
});

app.post('/api/v1/chat/send', authMiddleware, (req, res) => {
  res.json({
    code: 200,
    data: {
      reply: '感谢您的咨询，商品有现货，24小时内发货。'
    }
  });
});

// ==================== 库存管理API ====================

app.get('/api/v1/inventory', authMiddleware, (req, res) => {
  const inventory = [];
  const warehouses = ['北京仓', '上海仓', '广州仓', '深圳仓'];
  for (let i = 1; i <= 20; i++) {
    inventory.push({
      id: i,
      skuId: 'SKU' + (10000 + i),
      productName: '商品' + i,
      spec: i % 2 === 0 ? '黑色/L' : '白色/M',
      warehouse: warehouses[i % 4],
      availableStock: Math.floor(Math.random() * 500),
      reservedStock: Math.floor(Math.random() * 50),
      inTransitStock: Math.floor(Math.random() * 100)
    });
  }
  res.json({ code: 200, data: { list: inventory, total: 50 } });
});

app.get('/api/v1/inventory/alerts', authMiddleware, (req, res) => {
  const alerts = [];
  for (let i = 1; i <= 5; i++) {
    alerts.push({
      id: i,
      skuId: 'SKU' + (10000 + i),
      productName: '商品' + i,
      currentStock: Math.floor(Math.random() * 10),
      alertThreshold: 20,
      level: 'low',
      createdAt: new Date().toISOString()
    });
  }
  res.json({ code: 200, data: alerts });
});

// ==================== 知识库API ====================

app.get('/api/v1/knowledge', authMiddleware, (req, res) => {
  const knowledge = [
    { id: 1, category: '订单问题', title: '如何查询订单状态', content: '您可以在"我的订单"中查看订单状态...', keywords: '订单,查询,状态', updatedAt: '2024-01-15' },
    { id: 2, category: '物流问题', title: '发货时间说明', content: '一般情况下24小时内发货...', keywords: '发货,时间,物流', updatedAt: '2024-01-14' },
    { id: 3, category: '售后问题', title: '退换货政策', content: '支持7天无理由退换货...', keywords: '退货,换货,售后', updatedAt: '2024-01-13' },
    { id: 4, category: '支付问题', title: '支持的支付方式', content: '支持微信、支付宝、银行卡等...', keywords: '支付,方式', updatedAt: '2024-01-12' },
    { id: 5, category: '商品问题', title: '商品真伪查询', content: '所有商品均为正品，支持专柜验货...', keywords: '真伪,正品', updatedAt: '2024-01-11' }
  ];
  res.json({ code: 200, data: { list: knowledge, total: 5 } });
});

app.post('/api/v1/knowledge', authMiddleware, (req, res) => {
  res.json({ code: 200, message: '知识添加成功', data: { id: Date.now() } });
});

// ==================== 营销活动API ====================

app.get('/api/v1/activities', authMiddleware, (req, res) => {
  const activities = [
    { id: 1, code: 'ACT001', name: '春节大促销', type: '折扣', startTime: '2024-02-01', endTime: '2024-02-15', status: '进行中' },
    { id: 2, code: 'ACT002', name: '新用户专享', type: '优惠券', startTime: '2024-01-01', endTime: '2024-12-31', status: '进行中' },
    { id: 3, code: 'ACT003', name: '满减活动', type: '满减', startTime: '2024-02-10', endTime: '2024-02-20', status: '待开始' }
  ];
  res.json({ code: 200, data: { list: activities, total: 3 } });
});

app.get('/api/v1/coupons', authMiddleware, (req, res) => {
  const coupons = [
    { id: 1, name: '新用户立减20元', type: '满减', value: 20, condition: '满100可用', used: 156, total: 1000, status: '有效' },
    { id: 2, name: '全品类9折券', type: '折扣', value: 0.9, condition: '无门槛', used: 89, total: 500, status: '有效' },
    { id: 3, name: '会员专享50元券', type: '满减', value: 50, condition: '满200可用', used: 45, total: 200, status: '有效' }
  ];
  res.json({ code: 200, data: { list: coupons, total: 3 } });
});

// ==================== 店铺绑定API ====================

app.post('/api/v1/shops', authMiddleware, (req, res) => {
  res.json({ code: 200, message: '店铺绑定成功', data: { id: Date.now() } });
});

app.get('/api/v1/shops', authMiddleware, (req, res) => {
  res.json({ 
    code: 200, 
    data: [
      { id: 1, platform: '抖音', name: '测试抖音店铺', shopId: 'SHOP123456', status: 'active', orders: 156, revenue: 45678.90 },
      { id: 2, platform: '淘宝', name: '测试淘宝店铺', shopId: 'SHOP789012', status: 'active', orders: 89, revenue: 23456.78 }
    ]
  });
});

// ==================== 风控API ====================

app.get('/api/v1/risk/orders', authMiddleware, (req, res) => {
  const riskyOrders = [
    { id: 1, orderNo: 'ORD20240115001', riskLevel: 'high', riskType: '异常地址', description: '收货地址与常用地址不符', detectedAt: '2024-01-15 10:30:00' },
    { id: 2, orderNo: 'ORD20240115002', riskLevel: 'medium', riskType: '频繁退货', description: '用户近期退货率过高', detectedAt: '2024-01-15 11:20:00' }
  ];
  res.json({ code: 200, data: { list: riskyOrders, total: 2 } });
});

app.get('/api/v1/risk/stats', authMiddleware, (req, res) => {
  res.json({
    code: 200,
    data: {
      highRisk: 3,
      mediumRisk: 8,
      flaggedAccounts: 5,
      autoBlocked: 12
    }
  });
});

// ==================== 供应商API ====================

app.get('/api/v1/suppliers', authMiddleware, (req, res) => {
  const suppliers = [
    { id: 1, name: '广州服饰批发', contact: '张经理', phone: '13800138000', address: '广州市白云区', products: 156, rating: 4.8 },
    { id: 2, name: '义乌小商品城', contact: '李总', phone: '13900139000', address: '义乌市稠州路', products: 89, rating: 4.6 },
    { id: 3, name: '深圳数码批发', contact: '王经理', phone: '13700137000', address: '深圳市华强北', products: 234, rating: 4.9 }
  ];
  res.json({ code: 200, data: { list: suppliers, total: 3 } });
});

// ==================== 工单API ====================

app.get('/api/v1/tickets', authMiddleware, (req, res) => {
  const tickets = [
    { id: 1, title: '客户投诉处理', type: '投诉', priority: '高', status: '待处理', assignee: '客服小王', createdAt: '2024-01-15' },
    { id: 2, title: '售后退款申请', type: '退款', priority: '中', status: '处理中', assignee: '客服小李', createdAt: '2024-01-14' }
  ];
  res.json({ code: 200, data: { list: tickets, total: 2 } });
});

// 健康检查
app.get('/actuator/health', (req, res) => {
  res.json({ status: 'UP' });
});

// 启动服务器
app.listen(PORT, () => {
  console.log(`Mock API Server running on http://localhost:${PORT}`);
  console.log('Available users: admin/admin123, test/test123');
});