<template>
  <div class="after-sale-page">
    <div class="page-header">
      <h2>售后管理</h2>
    </div>

    <!-- 统计卡片 -->
    <el-row :gutter="16" class="stat-cards">
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-item">
            <div class="stat-value">{{ statistics.pending || 0 }}</div>
            <div class="stat-label">待处理</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-item">
            <div class="stat-value">{{ statistics.processing || 0 }}</div>
            <div class="stat-label">处理中</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-item success">
            <div class="stat-value">{{ statistics.completed || 0 }}</div>
            <div class="stat-label">已完成</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-item warning">
            <div class="stat-value">¥{{ statistics.refundAmount || '0.00' }}</div>
            <div class="stat-label">退款金额</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 筛选 -->
    <el-card class="filter-card">
      <el-form :inline="true" :model="filterForm">
        <el-form-item label="售后单号">
          <el-input v-model="filterForm.afterSaleNo" placeholder="请输入" clearable />
        </el-form-item>
        <el-form-item label="类型">
          <el-select v-model="filterForm.type" placeholder="全部类型" clearable>
            <el-option label="仅退款" value="REFUND_ONLY" />
            <el-option label="退货退款" value="RETURN_REFUND" />
            <el-option label="换货" value="EXCHANGE" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="filterForm.status" placeholder="全部状态" clearable>
            <el-option label="待审核" value="PENDING" />
            <el-option label="待退货" value="WAITING_RETURN" />
            <el-option label="待退款" value="WAITING_REFUND" />
            <el-option label="已完成" value="COMPLETED" />
            <el-option label="已拒绝" value="REJECTED" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="fetchList">查询</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 列表 -->
    <el-card>
      <el-table :data="afterSaleList" v-loading="loading" style="width: 100%">
        <el-table-column prop="afterSaleNo" label="售后单号" width="180" />
        <el-table-column prop="orderNo" label="订单编号" width="180" />
        <el-table-column prop="type" label="类型" width="100">
          <template #default="{ row }">
            <el-tag :type="typeColor(row.type)" size="small">{{ typeText(row.type) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="reason" label="原因" min-width="150" show-overflow-tooltip />
        <el-table-column prop="refundAmount" label="退款金额" width="100">
          <template #default="{ row }">
            <span class="text-price">¥{{ row.refundAmount || '0.00' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusColor(row.status)">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="buyerName" label="买家" width="100" />
        <el-table-column prop="createdAt" label="申请时间" width="160">
          <template #default="{ row }">
            {{ formatTime(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="240" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleDetail(row)">详情</el-button>
            <el-button v-if="row.status === 'PENDING'" type="success" link @click="handleApprove(row)">通过</el-button>
            <el-button v-if="row.status === 'PENDING'" type="danger" link @click="handleReject(row)">拒绝</el-button>
            <el-button v-if="row.status === 'WAITING_REFUND' || row.status === 'PENDING'" type="warning" link @click="handleProcessRefund(row)">处理退款</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.size"
        :total="pagination.total"
        layout="total, sizes, prev, pager, next"
        @size-change="fetchList"
        @current-change="fetchList"
        style="margin-top: 16px; justify-content: flex-end;"
      />
    </el-card>

    <!-- 详情对话框 -->
    <el-dialog v-model="detailVisible" title="售后详情" width="700px">
      <el-descriptions :column="2" border v-if="currentDetail">
        <el-descriptions-item label="售后单号">{{ currentDetail.afterSaleNo }}</el-descriptions-item>
        <el-descriptions-item label="订单编号">{{ currentDetail.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="类型">{{ typeText(currentDetail.type) }}</el-descriptions-item>
        <el-descriptions-item label="状态">{{ statusText(currentDetail.status) }}</el-descriptions-item>
        <el-descriptions-item label="退款金额">¥{{ currentDetail.refundAmount }}</el-descriptions-item>
        <el-descriptions-item label="买家">{{ currentDetail.buyerName }}</el-descriptions-item>
        <el-descriptions-item label="申请原因" :span="2">{{ currentDetail.reason }}</el-descriptions-item>
        <el-descriptions-item label="详细描述" :span="2">{{ currentDetail.description || '无' }}</el-descriptions-item>
        <el-descriptions-item label="退货物流" v-if="currentDetail.returnLogisticsNo">
          {{ currentDetail.returnLogisticsCompany }} {{ currentDetail.returnLogisticsNo }}
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>
    <!-- 退款处理对话框 -->
    <RefundProcessDialog
      v-model:visible="refundDialogVisible"
      :refund-data="currentRefund"
      @approve="handleRefundApprove"
      @reject="handleRefundReject"
    />
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import RefundProcessDialog from '@/components/dialog/RefundProcessDialog.vue'
import {
  getAfterSaleList, getAfterSaleDetail, approveAfterSale, rejectAfterSale, refundAfterSale,
  getAfterSaleStatistics
} from '@/api/extension'

const loading = ref(false)
const afterSaleList = ref([])
const detailVisible = ref(false)
const currentDetail = ref(null)

// RefundProcessDialog related
const refundDialogVisible = ref(false)
const currentRefund = ref(null)

const filterForm = reactive({
  afterSaleNo: '',
  type: '',
  status: ''
})

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

const statistics = reactive({
  pending: 0,
  processing: 0,
  completed: 0,
  refundAmount: '0.00'
})

const fetchList = async () => {
  loading.value = true
  try {
    const res = await getAfterSaleList({ ...filterForm, page: pagination.page, size: pagination.size })
    afterSaleList.value = res.data?.list || []
    pagination.total = res.data?.total || 0
  } catch (error) {
    ElMessage.error('获取列表失败')
  } finally {
    loading.value = false
  }
}

const fetchStatistics = async () => {
  try {
    const res = await getAfterSaleStatistics()
    Object.assign(statistics, res.data || {})
  } catch (error) {
    console.error('获取统计失败', error)
  }
}

const handleDetail = async (row) => {
  try {
    const res = await getAfterSaleDetail(row.id)
    currentDetail.value = res.data
    detailVisible.value = true
  } catch (error) {
    ElMessage.error('获取详情失败')
  }
}

// 处理打开退款弹窗
const handleProcessRefund = (row) => {
  // 将行数据转换为 RefundProcessDialog 需要的格式
  currentRefund.value = {
    id: row.id,
    afterSaleNo: row.afterSaleNo,
    orderNo: row.orderNo,
    productName: row.productName,
    productImage: row.productImage,
    productSku: row.productSku,
    orderAmount: row.orderAmount || row.refundAmount,
    orderStatus: row.orderStatus || 'PAID',
    type: row.type,
    status: row.status,
    reason: row.reason,
    description: row.description,
    refundAmount: row.refundAmount,
    buyerName: row.buyerName,
    buyerPhone: row.buyerPhone,
    buyerId: row.buyerId,
    platform: row.platform || '淘宝',
    createdAt: row.createdAt
  }
  refundDialogVisible.value = true
}

// 处理同意退款
const handleRefundApprove = async (data) => {
  try {
    await approveAfterSale(data.id, {
      actualRefundAmount: data.actualRefundAmount,
      remark: data.remark
    })
    ElMessage.success('退款申请已通过')
    refundDialogVisible.value = false
    fetchList()
    fetchStatistics()
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

// 处理拒绝退款
const handleRefundReject = async (data) => {
  try {
    await rejectAfterSale(data.id, {
      reason: data.remark
    })
    ElMessage.success('退款申请已拒绝')
    refundDialogVisible.value = false
    fetchList()
    fetchStatistics()
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const handleApprove = async (row) => {
  try {
    await ElMessageBox.confirm('确认通过该售后申请?', '提示', { type: 'warning' })
    await approveAfterSale(row.id, {})
    ElMessage.success('已通过')
    fetchList()
    fetchStatistics()
  } catch (error) {
    if (error !== 'cancel') ElMessage.error('操作失败')
  }
}

const handleReject = async (row) => {
  try {
    const { value } = await ElMessageBox.prompt('请输入拒绝原因', '拒绝申请', {
      inputPattern: /\S+/,
      inputErrorMessage: '请输入拒绝原因'
    })
    await rejectAfterSale(row.id, { reason: value })
    ElMessage.success('已拒绝')
    fetchList()
    fetchStatistics()
  } catch (error) {
    if (error !== 'cancel') ElMessage.error('操作失败')
  }
}

const handleRefund = async (row) => {
  try {
    await ElMessageBox.confirm(`确认退款 ¥${row.refundAmount}?`, '退款确认', { type: 'warning' })
    await refundAfterSale(row.id, {})
    ElMessage.success('退款成功')
    fetchList()
    fetchStatistics()
  } catch (error) {
    if (error !== 'cancel') ElMessage.error('退款失败')
  }
}

const typeText = (type) => ({ REFUND_ONLY: '仅退款', RETURN_REFUND: '退货退款', EXCHANGE: '换货' }[type] || type)
const typeColor = (type) => ({ REFUND_ONLY: 'warning', RETURN_REFUND: 'danger', EXCHANGE: 'info' }[type] || '')
const statusText = (s) => ({ PENDING: '待审核', WAITING_RETURN: '待退货', WAITING_REFUND: '待退款', COMPLETED: '已完成', REJECTED: '已拒绝' }[s] || s)
const statusColor = (s) => ({ PENDING: 'warning', WAITING_RETURN: 'info', WAITING_REFUND: 'primary', COMPLETED: 'success', REJECTED: 'danger' }[s] || '')
const formatTime = (t) => t ? new Date(t).toLocaleString('zh-CN') : '-'

onMounted(() => {
  fetchList()
  fetchStatistics()
})
</script>

<style scoped>
.after-sale-page { padding: 20px; }
.page-header { display: flex; justify-content: space-between; margin-bottom: 20px; }
.filter-card { margin-bottom: 20px; }
.stat-cards { margin-bottom: 20px; }
.stat-item { text-align: center; padding: 10px 0; }
.stat-value { font-size: 28px; font-weight: bold; color: #409EFF; }
.stat-item.success .stat-value { color: #67C23A; }
.stat-item.warning .stat-value { color: #E6A23C; }
.stat-label { color: #909399; margin-top: 8px; }
.text-price { color: #F56C6C; font-weight: bold; }
</style>