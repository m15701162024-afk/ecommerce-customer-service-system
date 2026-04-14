<template>
  <div class="dashboard">
    <el-row :gutter="20">
      <el-col :span="6" v-for="item in statsCards" :key="item.title">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" :style="{ background: item.color }">
              <el-icon :size="28"><component :is="item.icon" /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ item.value }}</div>
              <div class="stat-title">{{ item.title }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="mt-20">
      <el-col :span="16">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>订单趋势</span>
              <el-radio-group v-model="chartType" size="small">
                <el-radio-button label="week">近7天</el-radio-button>
                <el-radio-button label="month">近30天</el-radio-button>
              </el-radio-group>
            </div>
          </template>
          <div ref="orderChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card>
          <template #header>
            <span>平台订单分布</span>
          </template>
          <div ref="platformChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="mt-20">
      <el-col :span="12">
        <el-card>
          <template #header>
            <span>待处理事项</span>
          </template>
          <el-table :data="pendingTasks" style="width: 100%">
            <el-table-column prop="type" label="类型" width="120" />
            <el-table-column prop="count" label="数量" width="100">
              <template #default="{ row }">
                <el-tag :type="row.count > 10 ? 'danger' : 'warning'">{{ row.count }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="desc" label="说明" />
            <el-table-column label="操作" width="100">
              <template #default="{ row }">
                <el-button type="primary" link @click="handleTaskAction(row)">去处理</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card>
          <template #header>
            <span>最新订单</span>
          </template>
          <el-table :data="latestOrders" style="width: 100%">
            <el-table-column prop="orderNo" label="订单号" width="180" />
            <el-table-column prop="platform" label="平台" width="100">
              <template #default="{ row }">
                <el-tag :type="getPlatformType(row.platform)">{{ row.platform }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="amount" label="金额" width="100">
              <template #default="{ row }">¥{{ row.amount }}</template>
            </el-table-column>
            <el-table-column prop="status" label="状态">
              <template #default="{ row }">
                <el-tag :type="getStatusType(row.status)">{{ row.status }}</el-tag>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import * as echarts from 'echarts'
import { ElMessage } from 'element-plus'
import { getOrderList, getOrderTrend, getPlatformDistribution } from '@/api/order'
import { getPurchaseStats } from '@/api/purchase'
import { getChatSessions } from '@/api/chat'

const chartType = ref('week')
const loading = ref(false)

const statsCards = ref([
  { title: '今日订单', value: '0', icon: 'List', color: '#409eff' },
  { title: '今日销售额', value: '¥0', icon: 'Money', color: '#67c23a' },
  { title: '待处理客服', value: '0', icon: 'Service', color: '#e6a23c' },
  { title: '待采购订单', value: '0', icon: 'ShoppingCart', color: '#f56c6c' }
])

const pendingTasks = ref([])
const latestOrders = ref([])

const orderChartRef = ref(null)
const platformChartRef = ref(null)

// 获取仪表盘统计数据
const fetchDashboardStats = async () => {
  loading.value = true
  try {
    // 获取今日订单数
    const orderRes = await getOrderList({ page: 1, size: 1, date: 'today' })
    const todayOrderCount = orderRes.data?.total || 0
    
    // 获取采购统计
    const purchaseRes = await getPurchaseStats()
    const todaySales = purchaseRes.data?.todayAmount || 0
    const pendingPurchase = purchaseRes.data?.pendingCount || 0
    
    // 获取客服消息数
    const chatRes = await getChatSessions({ unread: true })
    const pendingChat = chatRes.data?.length || 0
    
    // 更新统计卡片
    statsCards.value[0].value = todayOrderCount.toString()
    statsCards.value[1].value = `¥${todaySales.toFixed(2)}`
    statsCards.value[2].value = pendingChat.toString()
    statsCards.value[3].value = pendingPurchase.toString()
    
    // 更新待处理任务
    pendingTasks.value = [
      { type: '待发货', count: orderRes.data?.toShipCount || 0, desc: '订单已付款，等待发货' },
      { type: '待采购', count: pendingPurchase, desc: '订单需要采购货源' },
      { type: '客服消息', count: pendingChat, desc: '未回复的客户消息' },
      { type: '退款申请', count: orderRes.data?.refundCount || 0, desc: '待处理的退款请求' }
    ]
    
    // 获取最新订单
    const latestRes = await getOrderList({ page: 1, size: 5 })
    latestOrders.value = (latestRes.data?.list || []).map(order => ({
      orderNo: order.orderNo,
      platform: order.platform,
      amount: order.amount,
      status: order.statusText || order.status
    }))
  } catch (error) {
    ElMessage.error('获取统计数据失败')
  } finally {
    loading.value = false
  }
}

const getPlatformType = (platform) => {
  const types = { '抖音': 'danger', '淘宝': 'warning', '小红书': 'success' }
  return types[platform] || 'info'
}

const getStatusType = (status) => {
  const types = { '待支付': 'info', '待采购': 'warning', '待发货': '', '已发货': 'success', '已完成': 'success' }
  return types[status] || 'info'
}

const handleTaskAction = (row) => {
  const routeMap = {
    '待发货': '/orders?status=to_ship',
    '待采购': '/purchase?status=pending',
    '客服消息': '/customer-service',
    '退款申请': '/after-sale?status=PENDING'
  }
  const route = routeMap[row.type]
  if (route) {
    window.location.href = route
  }
}

onMounted(() => {
  fetchDashboardStats()
  initOrderChart()
  initPlatformChart()
})

watch(chartType, () => {
  initOrderChart()
})

const initOrderChart = async () => {
  const chart = echarts.init(orderChartRef.value)
  
  try {
    const params = { range: chartType.value }
    const res = await getOrderTrend(params)
    
    const xAxisData = res.data?.dates || []
    const orderCounts = res.data?.orderCounts || []
    const salesAmounts = res.data?.salesAmounts || []
    
    if (xAxisData.length === 0) {
      chart.setOption({
        title: { text: '暂无数据', left: 'center', top: 'center', textStyle: { color: '#999', fontSize: 14 } },
        xAxis: { type: 'category', data: [] },
        yAxis: [{ type: 'value', name: '订单数' }, { type: 'value', name: '销售额(元)' }],
        series: []
      }, true)
      return
    }
    
    const option = {
      tooltip: { trigger: 'axis' },
      legend: { data: ['订单数', '销售额'] },
      xAxis: { type: 'category', data: xAxisData },
      yAxis: [{ type: 'value', name: '订单数' }, { type: 'value', name: '销售额(元)' }],
      series: [
        { name: '订单数', type: 'bar', data: orderCounts },
        { name: '销售额', type: 'line', yAxisIndex: 1, data: salesAmounts }
      ]
    }
    chart.setOption(option, true)
  } catch (error) {
    chart.setOption({
      title: { text: '暂无数据', left: 'center', top: 'center', textStyle: { color: '#999', fontSize: 14 } },
      xAxis: { type: 'category', data: [] },
      yAxis: [{ type: 'value', name: '订单数' }, { type: 'value', name: '销售额(元)' }],
      series: []
    }, true)
  }
}

const initPlatformChart = async () => {
  const chart = echarts.init(platformChartRef.value)
  
  try {
    const res = await getPlatformDistribution()
    const chartData = res.data || []
    
    if (chartData.length === 0) {
      chart.setOption({
        title: { text: '暂无数据', left: 'center', top: 'center', textStyle: { color: '#999', fontSize: 14 } },
        series: []
      }, true)
      return
    }
    
    const option = {
      tooltip: { trigger: 'item' },
      legend: { bottom: 0 },
      series: [{
        type: 'pie',
        radius: ['40%', '70%'],
        avoidLabelOverlap: false,
        itemStyle: { borderRadius: 10, borderColor: '#fff', borderWidth: 2 },
        label: { show: false },
        emphasis: { label: { show: true, fontSize: 14, fontWeight: 'bold' } },
        data: chartData
      }]
    }
    chart.setOption(option, true)
  } catch (error) {
    chart.setOption({
      title: { text: '暂无数据', left: 'center', top: 'center', textStyle: { color: '#999', fontSize: 14 } },
      series: []
    }, true)
  }
}
</script>

<style lang="scss" scoped>
.dashboard {
  .stat-card {
    .stat-content {
      display: flex;
      align-items: center;
      gap: 16px;
      
      .stat-icon {
        width: 56px;
        height: 56px;
        border-radius: 8px;
        display: flex;
        align-items: center;
        justify-content: center;
        color: #fff;
      }
      
      .stat-info {
        .stat-value {
          font-size: 24px;
          font-weight: bold;
          color: #303133;
        }
        .stat-title {
          font-size: 14px;
          color: #909399;
          margin-top: 4px;
        }
      }
    }
  }
  
  .chart-container {
    height: 300px;
  }
  
  .mt-20 {
    margin-top: 20px;
  }
  
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
}
</style>