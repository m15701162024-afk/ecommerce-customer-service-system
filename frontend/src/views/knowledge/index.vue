<template>
  <div class="knowledge-page">
    <el-row :gutter="20">
      <el-col :span="6">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>知识分类</span>
              <el-button type="primary" link @click="handleAddCategory">添加</el-button>
            </div>
          </template>
          <el-menu :default-active="activeCategory" @select="handleSelectCategory">
            <el-menu-item index="all">
              <el-icon><Folder /></el-icon>
              <span>全部 ({{ totalCount }})</span>
            </el-menu-item>
            <el-menu-item v-for="cat in categories" :key="cat.id" :index="cat.id">
              <el-icon><FolderOpened /></el-icon>
              <span>{{ cat.name }} ({{ cat.count }})</span>
            </el-menu-item>
          </el-menu>
        </el-card>
      </el-col>
      
      <el-col :span="18">
        <el-card>
          <template #header>
            <div class="card-header">
              <el-tabs v-model="activeTab" @tab-change="handleTabChange">
                <el-tab-pane label="知识库" name="list">
                  <template #label>
                    <span>知识库 ({{ totalCount }})</span>
                  </template>
                </el-tab-pane>
                <el-tab-pane label="待审核" name="pending">
                  <template #label>
                    <span>待审核 <el-badge v-if="pendingCount > 0" :value="pendingCount" type="warning" /></span>
                  </template>
                </el-tab-pane>
              </el-tabs>
              <el-button v-if="activeTab === 'list'" type="primary" @click="handleAddKnowledge">添加知识</el-button>
            </div>
          </template>
          
          <!-- 状态筛选 -->
          <div class="filter-bar" v-if="activeTab === 'list'">
            <el-select v-model="auditStatusFilter" placeholder="审核状态" clearable @change="fetchKnowledgeList" style="width: 150px; margin-right: 10px;">
              <el-option label="待审核" value="pending" />
              <el-option label="已通过" value="approved" />
              <el-option label="已拒绝" value="rejected" />
            </el-select>
          </div>
          
          <!-- 知识库列表 -->
          <el-table v-if="activeTab === 'list'" :data="knowledgeList" v-loading="loading" style="width: 100%">
            <el-table-column prop="category" label="分类" width="120" />
            <el-table-column prop="question" label="问题" show-overflow-tooltip />
            <el-table-column prop="answer" label="答案" show-overflow-tooltip />
            <el-table-column prop="hitCount" label="命中次数" width="100" />
            <el-table-column prop="status" label="状态" width="80">
              <template #default="{ row }">
                <el-tag :type="row.status === 1 ? 'success' : 'info'">
                  {{ row.status === 1 ? '启用' : '禁用' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="audit_status" label="审核状态" width="100">
              <template #default="{ row }">
                <el-tag :type="getAuditStatusType(row.audit_status)">
                  {{ getAuditStatusText(row.audit_status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="150">
              <template #default="{ row }">
                <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
                <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
          
          <!-- 待审核列表 -->
          <el-table v-if="activeTab === 'pending'" :data="pendingList" v-loading="pendingLoading" style="width: 100%">
            <el-table-column prop="category" label="分类" width="120" />
            <el-table-column prop="question" label="问题" show-overflow-tooltip />
            <el-table-column prop="answer" label="答案" show-overflow-tooltip />
            <el-table-column prop="created_at" label="创建时间" width="160" />
            <el-table-column prop="created_by" label="创建人" width="100" />
            <el-table-column label="操作" width="200">
              <template #default="{ row }">
                <el-button type="success" link @click="handleAudit(row, 'approved')">通过</el-button>
                <el-button type="danger" link @click="openRejectDialog(row)">拒绝</el-button>
                <el-button type="info" link @click="viewAuditHistory(row)">历史</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>
    
    <!-- 添加/编辑知识弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogType === 'add' ? '添加知识' : '编辑知识'"
      width="600px"
    >
      <el-form :model="knowledgeForm" label-width="80px">
        <el-form-item label="分类">
          <el-select v-model="knowledgeForm.category" placeholder="请选择分类">
            <el-option v-for="cat in categories" :key="cat.id" :label="cat.name" :value="cat.name" />
          </el-select>
        </el-form-item>
        <el-form-item label="问题">
          <el-input v-model="knowledgeForm.question" type="textarea" rows="2" placeholder="请输入问题" />
        </el-form-item>
        <el-form-item label="答案">
          <el-input v-model="knowledgeForm.answer" type="textarea" rows="4" placeholder="请输入答案" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="knowledgeForm.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave" :loading="loading">确定</el-button>
      </template>
    </el-dialog>
    
    <!-- 拒绝原因弹窗 -->
    <el-dialog
      v-model="rejectDialogVisible"
      title="拒绝原因"
      width="500px"
    >
      <el-form label-width="80px">
        <el-form-item label="拒绝原因">
          <el-input
            v-model="rejectComment"
            type="textarea"
            rows="4"
            placeholder="请输入拒绝原因"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="rejectDialogVisible = false">取消</el-button>
        <el-button type="danger" @click="handleAudit(currentRejectItem, 'rejected')" :loading="rejectLoading">确认拒绝</el-button>
      </template>
    </el-dialog>
    
    <!-- 审核历史弹窗 -->
    <el-dialog
      v-model="historyDialogVisible"
      title="审核历史"
      width="600px"
    >
      <el-table :data="auditHistory" v-loading="historyLoading">
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getAuditStatusType(row.status)">
              {{ getAuditStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="comment" label="审核意见" show-overflow-tooltip />
        <el-table-column prop="auditor" label="审核人" width="100" />
        <el-table-column prop="audited_at" label="审核时间" width="160" />
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  getKnowledgeList, 
  createKnowledge, 
  updateKnowledge, 
  deleteKnowledge,
  getPendingKnowledge,
  auditKnowledge,
  getAuditHistory
} from '@/api/chat'

const activeCategory = ref('all')
const activeTab = ref('list')
const totalCount = ref(0)
const pendingCount = ref(0)
const loading = ref(false)
const pendingLoading = ref(false)
const rejectLoading = ref(false)
const historyLoading = ref(false)
const knowledgeList = ref([])
const pendingList = ref([])
const categories = ref([])
const auditStatusFilter = ref('')

const getAuditStatusType = (status) => {
  const types = {
    pending: 'warning',
    approved: 'success',
    rejected: 'danger'
  }
  return types[status] || 'info'
}

const getAuditStatusText = (status) => {
  const texts = {
    pending: '待审核',
    approved: '已通过',
    rejected: '已拒绝'
  }
  return texts[status] || '未知'
}

// 获取知识列表
const fetchKnowledgeList = async () => {
  loading.value = true
  try {
    const params = { 
      category: activeCategory.value === 'all' ? '' : activeCategory.value 
    }
    if (auditStatusFilter.value) {
      params.audit_status = auditStatusFilter.value
    }
    const res = await getKnowledgeList(params)
    knowledgeList.value = res.data?.list || []
    totalCount.value = res.data?.total || 0
    updateCategoryCounts()
  } catch (error) {
    ElMessage.error('获取知识列表失败')
  } finally {
    loading.value = false
  }
}

// 获取待审核列表
const fetchPendingList = async () => {
  pendingLoading.value = true
  try {
    const res = await getPendingKnowledge({ 
      category: activeCategory.value === 'all' ? '' : activeCategory.value 
    })
    pendingList.value = res.data?.list || []
    pendingCount.value = res.data?.total || 0
  } catch (error) {
    ElMessage.error('获取待审核列表失败')
  } finally {
    pendingLoading.value = false
  }
}

// 更新分类计数
const updateCategoryCounts = () => {
  categories.value = categories.value.map(cat => ({
    ...cat,
    count: knowledgeList.value.filter(k => k.category === cat.name).length
  }))
}

const handleTabChange = (tab) => {
  if (tab === 'list') {
    fetchKnowledgeList()
  } else if (tab === 'pending') {
    fetchPendingList()
  }
}

onMounted(() => {
  fetchKnowledgeList()
})

const handleSelectCategory = (id) => {
  activeCategory.value = id
  if (activeTab.value === 'list') {
    fetchKnowledgeList()
  } else {
    fetchPendingList()
  }
}

const handleAddCategory = () => {
  ElMessage.info('添加分类功能待实现')
}

const dialogVisible = ref(false)
const dialogType = ref('add')
const currentKnowledge = ref(null)
const knowledgeForm = ref({
  category: '',
  question: '',
  answer: '',
  status: 1
})

const handleAddKnowledge = () => {
  dialogType.value = 'add'
  knowledgeForm.value = {
    category: categories.value[0]?.name || '',
    question: '',
    answer: '',
    status: 1
  }
  currentKnowledge.value = null
  dialogVisible.value = true
}

const handleEdit = (row) => {
  dialogType.value = 'edit'
  knowledgeForm.value = { ...row }
  currentKnowledge.value = row
  dialogVisible.value = true
}

const handleSave = async () => {
  try {
    loading.value = true
    if (dialogType.value === 'add') {
      await createKnowledge(knowledgeForm.value)
      ElMessage.success('添加成功')
    } else {
      await updateKnowledge(currentKnowledge.value.id, knowledgeForm.value)
      ElMessage.success('更新成功')
    }
    dialogVisible.value = false
    fetchKnowledgeList()
  } catch (error) {
    ElMessage.error(dialogType.value === 'add' ? '添加失败' : '更新失败')
  } finally {
    loading.value = false
  }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确认删除该知识条目?', '提示', { type: 'warning' })
    loading.value = true
    await deleteKnowledge(row.id)
    ElMessage.success('删除成功')
    fetchKnowledgeList()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  } finally {
    loading.value = false
  }
}

// 审核相关
const rejectDialogVisible = ref(false)
const rejectComment = ref('')
const currentRejectItem = ref(null)

const openRejectDialog = (row) => {
  currentRejectItem.value = row
  rejectComment.value = ''
  rejectDialogVisible.value = true
}

const handleAudit = async (row, status) => {
  const isReject = status === 'rejected'
  
  if (isReject && !rejectDialogVisible.value) {
    openRejectDialog(row)
    return
  }
  
  const action = status === 'approved' ? '通过' : '拒绝'
  
  try {
    if (status === 'approved') {
      await ElMessageBox.confirm(`确认通过该知识条目?`, '提示', { type: 'info' })
    }
    
    const loadingRef = isReject ? rejectLoading : loading
    loadingRef.value = true
    
    await auditKnowledge(row.id, {
      status,
      comment: isReject ? rejectComment.value : ''
    })
    
    ElMessage.success(`审核${action}成功`)
    rejectDialogVisible.value = false
    fetchPendingList()
    
    if (activeTab.value === 'list') {
      fetchKnowledgeList()
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(`审核${action}失败`)
    }
  } finally {
    const loadingRef = isReject ? rejectLoading : loading
    loadingRef.value = false
  }
}

const historyDialogVisible = ref(false)
const auditHistory = ref([])

const viewAuditHistory = async (row) => {
  historyDialogVisible.value = true
  historyLoading.value = true
  
  try {
    const res = await getAuditHistory(row.id)
    auditHistory.value = res.data?.list || []
  } catch (error) {
    ElMessage.error('获取审核历史失败')
  } finally {
    historyLoading.value = false
  }
}
</script>

<style lang="scss" scoped>
.knowledge-page {
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    
    :deep(.el-tabs) {
      flex: 1;
    }
  }
  
  .filter-bar {
    margin-bottom: 16px;
  }
}
</style>