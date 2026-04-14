<template>
  <div class="customer-page">
    <div class="page-header">
      <h2>客户管理</h2>
    </div>

    <!-- 筛选 -->
    <el-card class="filter-card">
      <el-form :inline="true" :model="filterForm">
        <el-form-item label="关键词">
          <el-input v-model="filterForm.keyword" placeholder="昵称/手机/买家ID" clearable />
        </el-form-item>
        <el-form-item label="客户等级">
          <el-select v-model="filterForm.level" placeholder="全部等级" clearable>
            <el-option label="普通客户" value="NORMAL" />
            <el-option label="银卡会员" value="SILVER" />
            <el-option label="金卡会员" value="GOLD" />
            <el-option label="白金会员" value="PLATINUM" />
            <el-option label="钻石会员" value="DIAMOND" />
          </el-select>
        </el-form-item>
        <el-form-item label="标签">
          <el-select v-model="filterForm.tagId" placeholder="全部标签" clearable>
            <el-option v-for="tag in tags" :key="tag.id" :label="tag.name" :value="tag.id" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="fetchList">查询</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 统计 -->
    <el-row :gutter="16" class="stat-cards">
      <el-col :span="4">
        <el-card shadow="hover">
          <div class="stat-item">
            <div class="stat-value">{{ statistics.total || 0 }}</div>
            <div class="stat-label">总客户数</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card shadow="hover">
          <div class="stat-item">
            <div class="stat-value">{{ statistics.newThisMonth || 0 }}</div>
            <div class="stat-label">本月新增</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card shadow="hover">
          <div class="stat-item">
            <div class="stat-value">{{ statistics.active || 0 }}</div>
            <div class="stat-label">活跃客户</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card shadow="hover">
          <div class="stat-item success">
            <div class="stat-value">{{ statistics.vip || 0 }}</div>
            <div class="stat-label">VIP客户</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card shadow="hover">
          <div class="stat-item warning">
            <div class="stat-value">{{ statistics.repeat || 0 }}</div>
            <div class="stat-label">复购客户</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card shadow="hover">
          <div class="stat-item">
            <div class="stat-value">¥{{ statistics.avgAmount || '0.00' }}</div>
            <div class="stat-label">平均客单价</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 列表 -->
    <el-card>
      <el-table :data="customerList" v-loading="loading" style="width: 100%">
        <el-table-column prop="nickname" label="客户昵称" width="150">
          <template #default="{ row }">
            <div class="customer-info">
              <el-avatar :src="row.avatar" :size="32">{{ row.nickname?.charAt(0) }}</el-avatar>
              <span>{{ row.nickname }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="customerLevel" label="等级" width="100">
          <template #default="{ row }">
            <el-tag :type="levelColor(row.customerLevel)" size="small">{{ levelText(row.customerLevel) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="tags" label="标签" width="200">
          <template #default="{ row }">
            <el-tag v-for="tag in (row.tags || []).slice(0, 3)" :key="tag.id" size="small" 
                    :color="tag.color" style="margin-right: 4px;">{{ tag.name }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="totalOrders" label="订单数" width="80" />
        <el-table-column prop="totalAmount" label="消费金额" width="120">
          <template #default="{ row }">
            <span class="text-price">¥{{ row.totalAmount || '0.00' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="avgOrderAmount" label="客单价" width="100">
          <template #default="{ row }">
            ¥{{ row.avgOrderAmount || '0.00' }}
          </template>
        </el-table-column>
        <el-table-column prop="lastOrderAt" label="最后下单" width="160">
          <template #default="{ row }">
            {{ formatTime(row.lastOrderAt) }}
          </template>
        </el-table-column>
        <el-table-column prop="phone" label="手机号" width="120" />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleDetail(row)">详情</el-button>
            <el-button type="primary" link @click="handleEditTags(row)">标签</el-button>
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

    <!-- 标签编辑 -->
    <el-dialog v-model="tagDialogVisible" title="编辑客户标签" width="500px">
      <div class="tag-editor">
        <div class="current-tags">
          <span>当前标签：</span>
          <el-tag v-for="tag in currentCustomerTags" :key="tag.id" closable @close="removeTag(tag.id)">
            {{ tag.name }}
          </el-tag>
          <span v-if="!currentCustomerTags.length">暂无标签</span>
        </div>
        <el-divider />
        <div class="add-tags">
          <span>添加标签：</span>
          <el-tag v-for="tag in availableTags" :key="tag.id" 
                  :color="tag.color" style="cursor: pointer; margin: 4px;"
                  @click="addTag(tag.id)">
            + {{ tag.name }}
          </el-tag>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getCustomerList, getCustomerTags, addCustomerTag, removeCustomerTag, getCustomerStatistics } from '@/api/extension'

const loading = ref(false)
const customerList = ref([])
const tags = ref([])
const tagDialogVisible = ref(false)
const currentCustomerId = ref(null)
const currentCustomerTags = ref([])

const filterForm = reactive({ keyword: '', level: '', tagId: '' })
const pagination = reactive({ page: 1, size: 10, total: 0 })
const statistics = reactive({ total: 0, newThisMonth: 0, active: 0, vip: 0, repeat: 0, avgAmount: '0.00' })

const availableTags = computed(() => {
  const currentIds = currentCustomerTags.value.map(t => t.id)
  return tags.value.filter(t => !currentIds.includes(t.id))
})

const fetchList = async () => {
  loading.value = true
  try {
    const res = await getCustomerList({ ...filterForm, page: pagination.page, size: pagination.size })
    customerList.value = res.data?.list || []
    pagination.total = res.data?.total || 0
  } catch (error) {
    ElMessage.error('获取客户列表失败')
  } finally {
    loading.value = false
  }
}

const fetchTags = async () => {
  try {
    const res = await getCustomerTags()
    tags.value = res.data || []
  } catch (error) {
    console.error('获取标签失败', error)
  }
}

const fetchStatistics = async () => {
  try {
    const res = await getCustomerStatistics()
    Object.assign(statistics, res.data || {})
  } catch (error) {
    console.error('获取统计失败', error)
  }
}

const handleDetail = (row) => {
  console.log('查看详情', row)
}

const handleEditTags = (row) => {
  currentCustomerId.value = row.id
  currentCustomerTags.value = row.tags || []
  tagDialogVisible.value = true
}

const addTag = async (tagId) => {
  try {
    await addCustomerTag(currentCustomerId.value, tagId)
    const tag = tags.value.find(t => t.id === tagId)
    if (tag) currentCustomerTags.value.push(tag)
    ElMessage.success('添加成功')
    fetchList()
  } catch (error) {
    ElMessage.error('添加失败')
  }
}

const removeTag = async (tagId) => {
  try {
    await removeCustomerTag(currentCustomerId.value, tagId)
    currentCustomerTags.value = currentCustomerTags.value.filter(t => t.id !== tagId)
    ElMessage.success('移除成功')
    fetchList()
  } catch (error) {
    ElMessage.error('移除失败')
  }
}

const levelText = (l) => ({ NORMAL: '普通', SILVER: '银卡', GOLD: '金卡', PLATINUM: '白金', DIAMOND: '钻石' }[l] || l)
const levelColor = (l) => ({ NORMAL: 'info', SILVER: '', GOLD: 'warning', PLATINUM: '', DIAMOND: 'success' }[l] || 'info')
const formatTime = (t) => t ? new Date(t).toLocaleString('zh-CN') : '-'

onMounted(() => {
  fetchList()
  fetchTags()
  fetchStatistics()
})
</script>

<style scoped>
.customer-page { padding: 20px; }
.page-header { margin-bottom: 20px; }
.filter-card { margin-bottom: 20px; }
.stat-cards { margin-bottom: 20px; }
.stat-item { text-align: center; padding: 10px 0; }
.stat-value { font-size: 24px; font-weight: bold; color: #409EFF; }
.stat-item.success .stat-value { color: #67C23A; }
.stat-item.warning .stat-value { color: #E6A23C; }
.stat-label { color: #909399; margin-top: 8px; font-size: 12px; }
.customer-info { display: flex; align-items: center; gap: 8px; }
.text-price { color: #F56C6C; font-weight: bold; }
.tag-editor { padding: 10px 0; }
.current-tags { margin-bottom: 10px; }
</style>