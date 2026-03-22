<template>
  <div class="products-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>商品列表</span>
          <div class="header-actions">
            <el-input v-model="searchKeyword" placeholder="搜索商品" style="width: 200px" clearable />
            <el-button type="primary" @click="handleAdd">添加商品</el-button>
          </div>
        </div>
      </template>
      
      <el-table :data="productList" style="width: 100%">
        <el-table-column prop="image" label="图片" width="80">
          <template #default="{ row }">
            <el-image :src="row.image" style="width: 50px; height: 50px" fit="cover" />
          </template>
        </el-table-column>
        <el-table-column prop="name" label="商品名称" show-overflow-tooltip />
        <el-table-column prop="platform" label="平台" width="100">
          <template #default="{ row }">
            <el-tag>{{ row.platform }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="price" label="售价" width="100">
          <template #default="{ row }">¥{{ row.price }}</template>
        </el-table-column>
        <el-table-column prop="sourcePrice" label="采购价" width="100">
          <template #default="{ row }">¥{{ row.sourcePrice }}</template>
        </el-table-column>
        <el-table-column prop="stock" label="库存" width="80" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 'active' ? 'success' : 'info'">
              {{ row.status === 'active' ? '上架' : '下架' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="sourceSupplier" label="货源" width="120" />
        <el-table-column label="操作" width="150">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-button type="primary" link @click="handleMatchSource(row)">匹配货源</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const searchKeyword = ref('')

const productList = ref([
  { id: 1, name: 'iPhone 15 Pro Max 256GB', image: 'https://via.placeholder.com/50', platform: '抖音', price: 8999, sourcePrice: 7500, stock: 100, status: 'active', sourceSupplier: '深圳数码批发' },
  { id: 2, name: 'MacBook Air M3', image: 'https://via.placeholder.com/50', platform: '淘宝', price: 8499, sourcePrice: 7200, stock: 50, status: 'active', sourceSupplier: '广州电子城' },
  { id: 3, name: 'AirPods Pro 2', image: 'https://via.placeholder.com/50', platform: '小红书', price: 1899, sourcePrice: 1400, stock: 200, status: 'active', sourceSupplier: '东莞耳机批发' }
])

const handleAdd = () => console.log('添加商品')
const handleEdit = (row) => console.log('编辑:', row)
const handleMatchSource = (row) => console.log('匹配货源:', row)
</script>

<style lang="scss" scoped>
.products-page .card-header { display: flex; justify-content: space-between; align-items: center; }
</style>