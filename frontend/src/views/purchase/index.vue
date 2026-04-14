<template>
  <div class="purchase-page">
    <el-row :gutter="20">
      <el-col :span="24">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>采购订单</span>
              <div class="header-actions">
                <el-select v-model="filters.status" placeholder="状态" clearable>
                  <el-option label="待采购" value="pending" />
                  <el-option label="采购中" value="processing" />
                  <el-option label="已采购" value="completed" />
                  <el-option label="采购失败" value="failed" />
                </el-select>
                <el-button type="primary" @click="handleAutoPurchase">一键采购</el-button>
              </div>
            </div>
          </template>
          
          <el-table :data="purchaseList" v-loading="loading" style="width: 100%">
            <el-table-column type="selection" width="50" />
            <el-table-column prop="orderNo" label="销售订单号" width="180" />
            <el-table-column prop="productName" label="商品名称" show-overflow-tooltip />
            <el-table-column prop="supplier" label="供应商" width="150" />
            <el-table-column prop="sourcePrice" label="采购价" width="100">
              <template #default="{ row }">¥{{ row.sourcePrice.toFixed(2) }}</template>
            </el-table-column>
            <el-table-column prop="sellPrice" label="售价" width="100">
              <template #default="{ row }">¥{{ row.sellPrice.toFixed(2) }}</template>
            </el-table-column>
            <el-table-column prop="profit" label="利润" width="100">
              <template #default="{ row }">
                <span :class="{ 'profit-positive': row.profit > 0, 'profit-negative': row.profit < 0 }">
                  ¥{{ row.profit.toFixed(2) }}
                </span>
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="getStatusType(row.status)">{{ row.statusText }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="paymentType" label="支付方式" width="100">
              <template #default="{ row }">
                <el-tag v-if="row.paymentType === 'auto'" type="success">自动</el-tag>
                <el-tag v-else type="warning">人工</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="150">
              <template #default="{ row }">
                <el-button type="primary" link @click="handlePurchase(row)" v-if="row.status === 'pending'">立即采购</el-button>
                <el-button type="primary" link @click="handleDetail(row)">详情</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>
    
    <el-row :gutter="20" class="mt-20">
      <el-col :span="12">
        <el-card>
          <template #header>
            <span>采购统计</span>
          </template>
          <div class="stats-grid">
            <div class="stat-item">
              <div class="stat-value">¥{{ purchaseStats.todayAmount?.toFixed(2) || '0.00' }}</div>
              <div class="stat-label">今日采购额</div>
            </div>
            <div class="stat-item">
              <div class="stat-value">¥{{ purchaseStats.todayProfit?.toFixed(2) || '0.00' }}</div>
              <div class="stat-label">今日利润</div>
            </div>
            <div class="stat-item">
              <div class="stat-value">{{ purchaseStats.profitRate?.toFixed(1) || '0.0' }}%</div>
              <div class="stat-label">利润率</div>
            </div>
            <div class="stat-item">
              <div class="stat-value">{{ purchaseStats.orderCount || 0 }}</div>
              <div class="stat-label">采购订单数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="12">
        <el-card>
          <template #header>
            <span>待人工确认</span>
          </template>
          <el-table :data="manualConfirmList" style="width: 100%" max-height="200">
            <el-table-column prop="orderNo" label="订单号" width="150" />
            <el-table-column prop="amount" label="金额" width="100">
              <template #default="{ row }">¥{{ row.amount.toFixed(2) }}</template>
            </el-table-column>
            <el-table-column prop="reason" label="原因" />
            <el-table-column label="操作" width="120">
              <template #default="{ row }">
                <el-button type="success" size="small" @click="handleConfirm(row)">确认</el-button>
                <el-button type="danger" size="small" @click="handleReject(row)">拒绝</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getPurchaseList, createPurchase, confirmPurchase, rejectPurchase, getPurchaseStats, getManualConfirmList } from '@/api/purchase'

const router = useRouter()

const filters = reactive({
  status: ''
})

const loading = ref(false)
const purchaseList = ref([])
const manualConfirmList = ref([])
const purchaseStats = ref({
  todayAmount: 0,
  todayProfit: 0,
  profitRate: 0,
  orderCount: 0
})

// 获取采购列表
const fetchPurchaseList = async () => {
  loading.value = true
  try {
    const res = await getPurchaseList({ status: filters.status })
    purchaseList.value = res.data?.list || []
  } catch (error) {
    ElMessage.error('获取采购列表失败')
  } finally {
    loading.value = false
  }
}

// 获取待确认列表
const fetchManualConfirmList = async () => {
  try {
    const res = await getManualConfirmList()
    manualConfirmList.value = res.data || []
  } catch (error) {
    manualConfirmList.value = []
  }
}

// 获取采购统计
const fetchPurchaseStats = async () => {
  try {
    const res = await getPurchaseStats()
    purchaseStats.value = res.data || {}
  } catch (error) {
    purchaseStats.value = { todayAmount: 0, todayProfit: 0, profitRate: 0, orderCount: 0 }
  }
}

onMounted(() => {
  fetchPurchaseList()
  fetchManualConfirmList()
  fetchPurchaseStats()
})

const getStatusType = (status) => {
  const types = { pending: 'warning', processing: 'primary', completed: 'success', failed: 'danger' }
  return types[status] || 'info'
}

const handleAutoPurchase = async () => {
  try {
    await ElMessageBox.confirm('确认一键采购所有待采购订单?', '提示', { type: 'warning' })
    loading.value = true
    // 批量创建采购订单
    const pendingOrders = purchaseList.value.filter(item => item.status === 'pending')
    for (const order of pendingOrders) {
      await createPurchase({ orderNo: order.orderNo })
    }
    ElMessage.success('批量采购成功')
    fetchPurchaseList()
    fetchPurchaseStats()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('采购失败')
    }
  } finally {
    loading.value = false
  }
}

const handlePurchase = async (row) => {
  try {
    loading.value = true
    await createPurchase({ orderNo: row.orderNo })
    ElMessage.success('采购订单创建成功')
    fetchPurchaseList()
    fetchPurchaseStats()
  } catch (error) {
    ElMessage.error('采购失败')
  } finally {
    loading.value = false
  }
}

const handleDetail = (row) => {
  router.push(`/purchase/detail/${row.orderNo}`)
}

const handleConfirm = async (row) => {
  try {
    await ElMessageBox.confirm('确认通过该采购?', '提示', { type: 'warning' })
    loading.value = true
    await confirmPurchase(row.orderNo)
    ElMessage.success('确认成功')
    fetchPurchaseList()
    fetchManualConfirmList()
    fetchPurchaseStats()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('确认失败')
    }
  } finally {
    loading.value = false
  }
}

const handleReject = async (row) => {
  try {
    const { value: reason } = await ElMessageBox.prompt('请输入拒绝原因', '拒绝采购', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputPattern: /\S+/,
      inputErrorMessage: '请输入拒绝原因'
    })
    loading.value = true
    await rejectPurchase(row.id || row.orderNo, { reason })
    ElMessage.success('已拒绝该采购')
    fetchManualConfirmList()
    fetchPurchaseList()
    fetchPurchaseStats()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('操作失败')
    }
  } finally {
    loading.value = false
  }
}
</script>

<style lang="scss" scoped>
.purchase-page {
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    
    .header-actions {
      display: flex;
      gap: 10px;
    }
  }
  
  .profit-positive {
    color: #67c23a;
    font-weight: 500;
  }
  
  .profit-negative {
    color: #f56c6c;
    font-weight: 500;
  }
  
  .stats-grid {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 20px;
    
    .stat-item {
      text-align: center;
      padding: 15px;
      background: #f5f7fa;
      border-radius: 8px;
      
      .stat-value {
        font-size: 24px;
        font-weight: bold;
        color: #303133;
      }
      
      .stat-label {
        font-size: 14px;
        color: #909399;
        margin-top: 5px;
      }
    }
  }
  
  .mt-20 {
    margin-top: 20px;
  }
}
</style>