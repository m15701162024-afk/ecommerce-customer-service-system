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
import { ElMessage } from 'element-plus'
import { getServiceHealth, getSystemLogs, getRequestTrend, getResponseDistribution } from '@/api/monitor'

const requestChartRef = ref(null)
const responseChartRef = ref(null)
const loading = ref(false)

const serviceStatus = ref([])
const logs = ref([])

const getLogLevelType = (level) => {
  const types = { INFO: 'info', WARN: 'warning', ERROR: 'danger' }
  return types[level] || 'info'
}

const fetchServiceStatus = async () => {
  try {
    const res = await getServiceHealth()
    serviceStatus.value = res.data?.services || []
  } catch (error) {
    serviceStatus.value = []
  }
}

const fetchLogs = async () => {
  try {
    const res = await getSystemLogs({ page: 1, size: 50 })
    logs.value = res.data?.list || []
  } catch (error) {
    logs.value = []
  }
}

onMounted(async () => {
  loading.value = true
  try {
    await fetchServiceStatus()
    await fetchLogs()
    await initRequestChart()
    await initResponseChart()
  } finally {
    loading.value = false
  }
})

const initRequestChart = async () => {
  const chart = echarts.init(requestChartRef.value)
  
  try {
    const res = await getRequestTrend()
    const times = res.data?.times || []
    const counts = res.data?.counts || []
    
    if (times.length === 0) {
      chart.setOption({
        title: { text: '暂无数据', left: 'center', top: 'center', textStyle: { color: '#999', fontSize: 14 } },
        xAxis: { type: 'category', data: [] },
        yAxis: { type: 'value' },
        series: []
      }, true)
      return
    }
    
    chart.setOption({
      tooltip: { trigger: 'axis' },
      xAxis: { type: 'category', data: times },
      yAxis: { type: 'value' },
      series: [{ data: counts, type: 'line', smooth: true, areaStyle: {} }]
    }, true)
  } catch (error) {
    chart.setOption({
      title: { text: '暂无数据', left: 'center', top: 'center', textStyle: { color: '#999', fontSize: 14 } },
      xAxis: { type: 'category', data: [] },
      yAxis: { type: 'value' },
      series: []
    }, true)
  }
}

const initResponseChart = async () => {
  const chart = echarts.init(responseChartRef.value)
  
  try {
    const res = await getResponseDistribution()
    const chartData = res.data || []
    
    if (chartData.length === 0) {
      chart.setOption({
        title: { text: '暂无数据', left: 'center', top: 'center', textStyle: { color: '#999', fontSize: 14 } },
        series: []
      }, true)
      return
    }
    
    chart.setOption({
      tooltip: { trigger: 'item' },
      series: [{
        type: 'pie',
        radius: ['40%', '70%'],
        data: chartData
      }]
    }, true)
  } catch (error) {
    chart.setOption({
      title: { text: '暂无数据', left: 'center', top: 'center', textStyle: { color: '#999', fontSize: 14 } },
      series: []
    }, true)
  }
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