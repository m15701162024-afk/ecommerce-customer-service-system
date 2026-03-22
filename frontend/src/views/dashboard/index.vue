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
              <template #default>
                <el-button type="primary" link>去处理</el-button>
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
import { ref, onMounted } from 'vue'
import * as echarts from 'echarts'

const chartType = ref('week')

const statsCards = ref([
  { title: '今日订单', value: '1,234', icon: 'List', color: '#409eff' },
  { title: '今日销售额', value: '¥89,432', icon: 'Money', color: '#67c23a' },
  { title: '待处理客服', value: '56', icon: 'Service', color: '#e6a23c' },
  { title: '待采购订单', value: '23', icon: 'ShoppingCart', color: '#f56c6c' }
])

const pendingTasks = ref([
  { type: '待发货', count: 45, desc: '订单已付款，等待发货' },
  { type: '待采购', count: 23, desc: '订单需要采购货源' },
  { type: '客服消息', count: 56, desc: '未回复的客户消息' },
  { type: '退款申请', count: 8, desc: '待处理的退款请求' }
])

const latestOrders = ref([
  { orderNo: 'DD202401150001', platform: '抖音', amount: 299.00, status: '待发货' },
  { orderNo: 'DD202401150002', platform: '淘宝', amount: 158.00, status: '已发货' },
  { orderNo: 'DD202401150003', platform: '小红书', amount: 89.00, status: '待采购' },
  { orderNo: 'DD202401150004', platform: '抖音', amount: 459.00, status: '已完成' },
  { orderNo: 'DD202401150005', platform: '淘宝', amount: 128.00, status: '待支付' }
])

const orderChartRef = ref(null)
const platformChartRef = ref(null)

const getPlatformType = (platform) => {
  const types = { '抖音': 'danger', '淘宝': 'warning', '小红书': 'success' }
  return types[platform] || 'info'
}

const getStatusType = (status) => {
  const types = { '待支付': 'info', '待采购': 'warning', '待发货': '', '已发货': 'success', '已完成': 'success' }
  return types[status] || 'info'
}

onMounted(() => {
  initOrderChart()
  initPlatformChart()
})

const initOrderChart = () => {
  const chart = echarts.init(orderChartRef.value)
  const option = {
    tooltip: { trigger: 'axis' },
    legend: { data: ['订单数', '销售额'] },
    xAxis: {
      type: 'category',
      data: ['周一', '周二', '周三', '周四', '周五', '周六', '周日']
    },
    yAxis: [
      { type: 'value', name: '订单数' },
      { type: 'value', name: '销售额(元)' }
    ],
    series: [
      { name: '订单数', type: 'bar', data: [120, 200, 150, 80, 70, 110, 130] },
      { name: '销售额', type: 'line', yAxisIndex: 1, data: [2400, 4000, 3000, 1600, 1400, 2200, 2600] }
    ]
  }
  chart.setOption(option)
}

const initPlatformChart = () => {
  const chart = echarts.init(platformChartRef.value)
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
      data: [
        { value: 1048, name: '抖音' },
        { value: 735, name: '淘宝' },
        { value: 580, name: '小红书' }
      ]
    }]
  }
  chart.setOption(option)
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