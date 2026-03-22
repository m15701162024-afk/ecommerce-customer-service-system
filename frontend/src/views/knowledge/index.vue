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
              <span>知识条目</span>
              <el-button type="primary" @click="handleAddKnowledge">添加知识</el-button>
            </div>
          </template>
          
          <el-table :data="knowledgeList" style="width: 100%">
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
            <el-table-column label="操作" width="150">
              <template #default="{ row }">
                <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
                <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const activeCategory = ref('all')
const totalCount = ref(156)

const categories = ref([
  { id: 'product', name: '商品咨询', count: 45 },
  { id: 'order', name: '订单问题', count: 38 },
  { id: 'shipping', name: '物流配送', count: 28 },
  { id: 'refund', name: '退换货', count: 25 },
  { id: 'other', name: '其他', count: 20 }
])

const knowledgeList = ref([
  { id: 1, category: '商品咨询', question: '这个商品有货吗？', answer: '您好，该商品目前有现货，下单后24小时内发货。', hitCount: 1234, status: 1 },
  { id: 2, category: '订单问题', question: '我的订单什么时候发货？', answer: '您好，订单付款后24小时内发货，您可以在订单详情查看物流信息。', hitCount: 856, status: 1 },
  { id: 3, category: '物流配送', question: '发什么快递？', answer: '我们默认发顺丰快递，部分地区可能发其他快递，具体以实际为准。', hitCount: 654, status: 1 }
])

const handleSelectCategory = (id) => { activeCategory.value = id }
const handleAddCategory = () => console.log('添加分类')
const handleAddKnowledge = () => console.log('添加知识')
const handleEdit = (row) => console.log('编辑:', row)
const handleDelete = (row) => console.log('删除:', row)
</script>

<style lang="scss" scoped>
.knowledge-page .card-header { display: flex; justify-content: space-between; align-items: center; }
</style>