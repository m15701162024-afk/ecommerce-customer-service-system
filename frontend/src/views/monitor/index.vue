<template>
  <div class="monitor-page">
    <el-row :gutter="20">
      <el-col :span="8" v-for="item in serviceStatus" :key="item.name">
        <el-card class="status-card" :class="{ healthy: item.healthy, unhealthy: !item.healthy }">
          <div class="service-info">
            <el-icon :size="32" :class="item.healthy ? 'success-icon' : 'error-icon'">
              <component :is="item.healthy ? 'CircleCheckFilled' : 'CircleCloseFilled'" />
            </el-icon>
            <div class="service-detail">
              <div class="service-name">{{ item.name }}</div>
              <div class="service-status">{{ item.healthy ? '运行正常' : '异常' }}</div>
            </div>
          </div>
          <div class="service-metrics">
            <div class="metric">
              <span class="label">CPU</span>
              <span class="value">{{ item.cpu }}%</span>
            </div>
            <div class="metric">
              <span class="label">内存</span>
              <span class="value">{{ item.memory }}%</span>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
    
    <el-row :gutter="20" class="mt-20">
      <el-col :span="12">
        <el-card>
          <template #header><span>请求量趋势</span></template>
          <div ref="requestChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card>
          <template #header><span>响应时间分布</span></template>
          <div ref="responseChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
    </el-row>
    
    <el-card class="mt-20">
      <template #header><span>系统日志</span></template>
      <el-table :data="logs" style="width: 100%" max-height="300">
        <el-table-column prop="time" label="时间" width="180" />
        <el-table-column prop="level" label="级别" width="80">
          <template #default="{ row }">
            <el-tag :type="getLogLevelType(row.level)" size="small">{{ row.level }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="service" label="服务" width="120" />
        <el-table-column prop="message" label="消息" show-overflow-tooltip />
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import * as echarts from 'echarts'

const requestChartRef = ref(null)
const responseChartRef = ref(null)

const serviceStatus = ref([
  { name: 'API Gateway', healthy: true, cpu: 23, memory: 45 },
  { name: 'Order Service', healthy: true, cpu: 34, memory: 56 },
  { name: 'Purchase Service', healthy: true, cpu: 12, memory: 34 },
  { name: 'Customer Service', healthy: true, cpu: 45, memory: 67 },
  { name: 'Redis Cluster', healthy: true, cpu: 8, memory: 23 },
  { name: 'Kafka', healthy: true, cpu: 15, memory: 34 }
])

const logs = ref([
  { time: '2024-01-15 14:30:25', level: 'INFO', service: 'API Gateway', message: 'Request processed successfully' },
  { time: '2024-01-15 14:30:24', level: 'WARN', service: 'Order Service', message: 'Order timeout, retrying...' },
  { time: '2024-01-15 14:30:23', level: 'INFO', service: 'Purchase Service', message: 'Purchase order created: PO202401150001' },
  { time: '2024-01-15 14:30:22', level: 'ERROR', service: 'Customer Service', message: 'AI service response timeout' },
  { time: '2024-01-15 14:30:21', level: 'INFO', service: 'Redis Cluster', message: 'Cache hit ratio: 85.6%' }
])

const getLogLevelType = (level) => {
  const types = { INFO: 'info', WARN: 'warning', ERROR: 'danger' }
  return types[level] || 'info'
}

onMounted(() => {
  initRequestChart()
  initResponseChart()
})

const initRequestChart = () => {
  const chart = echarts.init(requestChartRef.value)
  chart.setOption({
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: ['14:00', '14:05', '14:10', '14:15', '14:20', '14:25', '14:30'] },
    yAxis: { type: 'value' },
    series: [{ data: [120, 200, 150, 80, 70, 110, 130], type: 'line', smooth: true, areaStyle: {} }]
  })
}

const initResponseChart = () => {
  const chart = echarts.init(responseChartRef.value)
  chart.setOption({
    tooltip: { trigger: 'item' },
    series: [{
      type: 'pie',
      radius: ['40%', '70%'],
      data: [
        { value: 1048, name: '<100ms' },
        { value: 735, name: '100-500ms' },
        { value: 580, name: '500ms-1s' },
        { value: 484, name: '>1s' }
      ]
    }]
  })
}
</script>

<style lang="scss" scoped>
.monitor-page {
  .status-card {
    &.healthy { border-left: 4px solid #67c23a; }
    &.unhealthy { border-left: 4px solid #f56c6c; }
    
    .service-info {
      display: flex;
      align-items: center;
      gap: 15px;
      
      .success-icon { color: #67c23a; }
      .error-icon { color: #f56c6c; }
      
      .service-detail {
        .service-name { font-weight: bold; font-size: 16px; }
        .service-status { color: #909399; font-size: 12px; }
      }
    }
    
    .service-metrics {
      display: flex;
      gap: 20px;
      margin-top: 15px;
      
      .metric {
        .label { color: #909399; font-size: 12px; }
        .value { font-weight: bold; margin-left: 8px; }
      }
    }
  }
  
  .chart-container { height: 250px; }
  .mt-20 { margin-top: 20px; }
}
</style>