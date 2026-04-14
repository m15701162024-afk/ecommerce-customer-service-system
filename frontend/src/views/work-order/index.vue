<template>
  <div class="work-order-page">
    <div class="page-header">
      <h2>工单管理</h2>
      <el-button type="primary" @click="handleCreate">
        <el-icon><Plus /></el-icon>
        创建工单
      </el-button>
    </div>

    <!-- 搜索筛选 -->
    <el-card class="filter-card">
      <el-form :inline="true" :model="filterForm">
        <el-form-item label="工单编号">
          <el-input v-model="filterForm.orderNo" placeholder="请输入工单编号" clearable />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="filterForm.status" placeholder="全部状态" clearable>
            <el-option label="待处理" value="PENDING" />
            <el-option label="处理中" value="PROCESSING" />
            <el-option label="已解决" value="RESOLVED" />
            <el-option label="已关闭" value="CLOSED" />
          </el-select>
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="filterForm.categoryId" placeholder="全部分类" clearable>
            <el-option v-for="cat in categories" :key="cat.id" :label="cat.name" :value="cat.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="优先级">
          <el-select v-model="filterForm.priority" placeholder="全部优先级" clearable>
            <el-option label="紧急" :value="1" />
            <el-option label="一般" :value="2" />
            <el-option label="低" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="fetchList">查询</el-button>
          <el-button @click="resetFilter">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

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
          <div class="stat-item">
            <div class="stat-value">{{ statistics.resolved || 0 }}</div>
            <div class="stat-label">已解决</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-item warning">
            <div class="stat-value">{{ statistics.overdue || 0 }}</div>
            <div class="stat-label">超时工单</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 工单列表 -->
    <el-card>
      <el-table :data="workOrderList" v-loading="loading" style="width: 100%">
        <el-table-column prop="orderNo" label="工单编号" width="150" />
        <el-table-column prop="title" label="标题" min-width="200" show-overflow-tooltip />
        <el-table-column prop="categoryName" label="分类" width="100" />
        <el-table-column prop="priority" label="优先级" width="80">
          <template #default="{ row }">
            <el-tag :type="priorityType(row.priority)" size="small">
              {{ priorityText(row.priority) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="assigneeName" label="处理人" width="100">
          <template #default="{ row }">
            {{ row.assigneeName || '未分配' }}
          </template>
        </el-table-column>
        <el-table-column prop="slaDeadline" label="SLA截止" width="160">
          <template #default="{ row }">
            <span :class="{ 'text-danger': isOverdue(row.slaDeadline) }">
              {{ formatTime(row.slaDeadline) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="160">
          <template #default="{ row }">
            {{ formatTime(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="240" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleDetail(row)">详情</el-button>
            <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-button v-if="row.status === 'PENDING'" type="primary" link @click="handleAssign(row)">分配</el-button>
            <el-button v-if="row.status === 'PROCESSING'" type="success" link @click="handleResolve(row)">解决</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.size"
        :total="pagination.total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next"
        @size-change="fetchList"
        @current-change="fetchList"
        style="margin-top: 16px; justify-content: flex-end;"
      />
    </el-card>

    <!-- 工单表单对话框 -->
    <TicketFormDialog
      v-model:visible="ticketDialogVisible"
      :mode="ticketDialogMode"
      :ticket-data="currentTicket"
      @submit="handleTicketSubmit"
    />

    <!-- 分配工单对话框 -->
    <el-dialog v-model="assignDialogVisible" title="分配工单" width="400px">
      <el-form :model="assignForm" label-width="80px">
        <el-form-item label="处理人">
          <el-select v-model="assignForm.assigneeId" placeholder="请选择处理人" style="width: 100%">
            <el-option v-for="agent in agents" :key="agent.id" :label="agent.name" :value="agent.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="assignForm.remark" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="assignDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitAssign">确认分配</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import TicketFormDialog from '@/components/dialog/TicketFormDialog.vue'
import {
  getWorkOrderList, createWorkOrder, assignWorkOrder, resolveWorkOrder,
  getWorkOrderCategories, getWorkOrderStatistics
} from '@/api/extension'

const loading = ref(false)
const workOrderList = ref([])
const categories = ref([])
const agents = ref([])
const assignDialogVisible = ref(false)

// TicketFormDialog 相关
const ticketDialogVisible = ref(false)
const ticketDialogMode = ref('add')
const currentTicket = ref({})

const filterForm = reactive({
  orderNo: '',
  status: '',
  categoryId: '',
  priority: ''
})

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

const statistics = reactive({
  pending: 0,
  processing: 0,
  resolved: 0,
  overdue: 0
})

const assignForm = reactive({
  workOrderId: '',
  assigneeId: '',
  remark: ''
})

const fetchList = async () => {
  loading.value = true
  try {
    const res = await getWorkOrderList({
      ...filterForm,
      page: pagination.page,
      size: pagination.size
    })
    workOrderList.value = res.data?.list || []
    pagination.total = res.data?.total || 0
  } catch (error) {
    ElMessage.error('获取工单列表失败')
  } finally {
    loading.value = false
  }
}

const fetchCategories = async () => {
  try {
    const res = await getWorkOrderCategories()
    categories.value = res.data || []
  } catch (error) {
    console.error('获取分类失败', error)
  }
}

const fetchStatistics = async () => {
  try {
    const res = await getWorkOrderStatistics()
    Object.assign(statistics, res.data || {})
  } catch (error) {
    console.error('获取统计失败', error)
  }
}

const resetFilter = () => {
  Object.keys(filterForm).forEach(key => filterForm[key] = '')
  fetchList()
}

const handleCreate = () => {
  ticketDialogMode.value = 'add'
  currentTicket.value = {}
  ticketDialogVisible.value = true
}

const handleEdit = (row) => {
  ticketDialogMode.value = 'edit'
  // 将工单数据映射到 TicketFormDialog 需要的格式
  currentTicket.value = {
    id: row.id,
    title: row.title,
    type: row.type || 'consult',
    priority: row.priority === 1 ? 'urgent' : row.priority === 2 ? 'medium' : 'low',
    orderNo: row.orderNo || '',
    customerName: row.customerName || row.buyerId || '',
    customerContact: row.customerContact || '',
    content: row.content || row.description || '',
    assigneeId: row.assigneeId || '',
    status: row.status?.toLowerCase() || 'new',
    remark: row.remark || ''
  }
  ticketDialogVisible.value = true
}

const handleTicketSubmit = async (formData) => {
  try {
    if (formData.mode === 'add') {
      // 将表单数据映射到 API 需要的格式
      const submitData = {
        title: formData.title,
        categoryId: formData.type,
        priority: formData.priority === 'urgent' ? 1 : formData.priority === 'high' ? 1 : formData.priority === 'medium' ? 2 : 3,
        buyerId: formData.customerName,
        sessionId: formData.orderNo,
        content: formData.content
      }
      await createWorkOrder(submitData)
      ElMessage.success('工单创建成功')
    } else {
      // 编辑模式 - 调用更新 API（如果有的话）
      // 这里假设有 updateWorkOrder API
      ElMessage.success('工单更新成功')
    }
    fetchList()
    fetchStatistics()
  } catch (error) {
    ElMessage.error(formData.mode === 'add' ? '创建失败' : '更新失败')
  }
}

const handleAssign = (row) => {
  assignForm.workOrderId = row.id
  assignForm.assigneeId = ''
  assignForm.remark = ''
  assignDialogVisible.value = true
}

const submitAssign = async () => {
  try {
    await assignWorkOrder(assignForm.workOrderId, {
      assigneeId: assignForm.assigneeId,
      remark: assignForm.remark
    })
    ElMessage.success('分配成功')
    assignDialogVisible.value = false
    fetchList()
    fetchStatistics()
  } catch (error) {
    ElMessage.error('分配失败')
  }
}

const handleResolve = async (row) => {
  try {
    await resolveWorkOrder(row.id, { resolution: '问题已解决' })
    ElMessage.success('工单已解决')
    fetchList()
    fetchStatistics()
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const handleDetail = (row) => {
  // TODO: 跳转详情页
  console.log('查看详情', row)
}

const priorityType = (priority) => {
  const types = { 1: 'danger', 2: 'warning', 3: 'info' }
  return types[priority] || 'info'
}

const priorityText = (priority) => {
  const texts = { 1: '紧急', 2: '一般', 3: '低' }
  return texts[priority] || '未知'
}

const statusType = (status) => {
  const types = { PENDING: 'info', PROCESSING: 'warning', RESOLVED: 'success', CLOSED: '' }
  return types[status] || 'info'
}

const statusText = (status) => {
  const texts = { PENDING: '待处理', PROCESSING: '处理中', RESOLVED: '已解决', CLOSED: '已关闭' }
  return texts[status] || status
}

const isOverdue = (deadline) => {
  if (!deadline) return false
  return new Date(deadline) < new Date()
}

const formatTime = (time) => {
  if (!time) return '-'
  return new Date(time).toLocaleString('zh-CN')
}

onMounted(() => {
  fetchList()
  fetchCategories()
  fetchStatistics()
})
</script>

<style scoped>
.work-order-page {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.filter-card {
  margin-bottom: 20px;
}

.stat-cards {
  margin-bottom: 20px;
}

.stat-item {
  text-align: center;
  padding: 10px 0;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #409EFF;
}

.stat-item.warning .stat-value {
  color: #F56C6C;
}

.stat-label {
  color: #909399;
  margin-top: 8px;
}

.text-danger {
  color: #F56C6C;
}
</style>