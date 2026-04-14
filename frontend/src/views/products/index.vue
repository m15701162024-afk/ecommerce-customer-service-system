<template>
  <div class="products-page">
    <el-card>
      <!-- Header with search and actions -->
      <template #header>
        <div class="card-header">
          <span>商品列表</span>
          <div class="header-actions">
            <el-input v-model="searchKeyword" placeholder="搜索商品" style="width: 200px" clearable @keyup.enter="handleSearch" />
            <el-button type="primary" @click="handleSearch">搜索</el-button>
            <el-button type="success" @click="handleImport">
              <el-icon><Upload /></el-icon>
              导入
            </el-button>
            <el-button type="warning" @click="handleExport">
              <el-icon><Download /></el-icon>
              导出
            </el-button>
            <el-button type="primary" @click="handleAdd">添加商品</el-button>
          </div>
        </div>
      </template>

      <!-- Filter Panel -->
      <div class="filter-panel">
        <el-row :gutter="16">
          <el-col :span="6">
            <el-form-item label="所属平台">
              <el-select v-model="filterForm.platform" placeholder="选择平台" clearable style="width: 100%">
                <el-option label="抖音" value="douyin" />
                <el-option label="淘宝" value="taobao" />
                <el-option label="小红书" value="xiaohongshu" />
                <el-option label="京东" value="jd" />
                <el-option label="拼多多" value="pdd" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="商品状态">
              <el-select v-model="filterForm.status" placeholder="选择状态" clearable style="width: 100%">
                <el-option label="上架" value="active" />
                <el-option label="下架" value="inactive" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="价格区间">
              <div class="price-range">
                <el-input-number v-model="filterForm.minPrice" :min="0" :precision="2" placeholder="最低价" style="width: 48%" />
                <span class="price-separator">-</span>
                <el-input-number v-model="filterForm.maxPrice" :min="0" :precision="2" placeholder="最高价" style="width: 48%" />
              </div>
            </el-form-item>
          </el-col>
          <el-col :span="6" class="filter-actions">
            <el-button type="primary" @click="handleFilter">
              <el-icon><Search /></el-icon>
              筛选
            </el-button>
            <el-button @click="handleResetFilter">
              <el-icon><RefreshRight /></el-icon>
              重置
            </el-button>
          </el-col>
        </el-row>
      </div>

      <!-- Batch Operations -->
      <div class="batch-operations" v-if="selectedProducts.length > 0">
        <span class="batch-info">已选择 {{ selectedProducts.length }} 项</span>
        <el-button type="danger" size="small" @click="handleBatchDelete">
          <el-icon><Delete /></el-icon>
          批量删除
        </el-button>
        <el-button type="primary" size="small" @click="handleBatchUpdateStatus('active')">
          <el-icon><Check /></el-icon>
          批量上架
        </el-button>
        <el-button type="info" size="small" @click="handleBatchUpdateStatus('inactive')">
          <el-icon><Close /></el-icon>
          批量下架
        </el-button>
      </div>

      <!-- Product Table -->
      <el-table
        :data="productList"
        v-loading="loading"
        style="width: 100%"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="image" label="图片" width="80">
          <template #default="{ row }">
            <el-image :src="row.image || row.mainImage" style="width: 50px; height: 50px" fit="cover" />
          </template>
        </el-table-column>
        <el-table-column prop="name" label="商品名称" show-overflow-tooltip />
        <el-table-column prop="platform" label="平台" width="100">
          <template #default="{ row }">
            <el-tag>{{ getPlatformLabel(row.platform) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="price" label="售价" width="100">
          <template #default="{ row }">¥{{ row.price }}</template>
        </el-table-column>
        <el-table-column prop="sourcePrice" label="采购价" width="100">
          <template #default="{ row }">¥{{ row.sourcePrice }}</template>
        </el-table-column>
        <el-table-column prop="stock" label="库存" width="80">
          <template #default="{ row }">
            <span :class="{ 'low-stock': row.stock < 10 }">{{ row.stock }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 'active' ? 'success' : 'info'">
              {{ row.status === 'active' ? '上架' : '下架' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="sourceSupplier" label="货源" width="120" show-overflow-tooltip />
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-button type="primary" link @click="handleViewSkus(row)">SKU管理</el-button>
            <el-button type="primary" link @click="handleMatchSource(row)">匹配货源</el-button>
            <el-button type="warning" link @click="handleAdjustInventory(row)">调整库存</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- Pagination -->
      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
        />
      </div>

      <!-- Product Form Dialog -->
      <ProductFormDialog
        v-model:visible="dialogVisible"
        :mode="dialogType"
        :product-data="currentProduct"
        @submit="handleDialogSubmit"
      />

      <!-- Inventory Adjust Dialog -->
      <InventoryAdjustDialog
        v-model:visible="inventoryDialogVisible"
        :product-data="currentProduct"
        @submit="handleInventorySubmit"
      />

      <!-- Batch Import Dialog -->
      <BatchImportDialog
        v-model:visible="importDialogVisible"
        import-type="product"
        @success="handleImportSuccess"
      />
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Upload,
  Download,
  Search,
  RefreshRight,
  Delete,
  Check,
  Close
} from '@element-plus/icons-vue'
import {
  getProductList,
  matchSourceProduct,
  createProduct,
  updateProduct,
  deleteProduct,
  exportProducts,
  importProducts,
  downloadTemplate
} from '@/api/product'
import ProductFormDialog from '@/components/dialog/ProductFormDialog.vue'
import InventoryAdjustDialog from '@/components/dialog/InventoryAdjustDialog.vue'
import BatchImportDialog from '@/components/dialog/BatchImportDialog.vue'

// Search and Loading State
const searchKeyword = ref('')
const loading = ref(false)
const submitLoading = ref(false)

// Product List Data
const productList = ref([])
const selectedProducts = ref([])

// Dialog Visibility State
const dialogVisible = ref(false)
const dialogType = ref('add')
const currentProduct = ref(null)
const inventoryDialogVisible = ref(false)
const importDialogVisible = ref(false)

// Pagination State
const pagination = reactive({
  page: 1,
  pageSize: 10,
  total: 0
})

// Filter Form State
const filterForm = reactive({
  platform: '',
  status: '',
  minPrice: null,
  maxPrice: null
})

// Platform Labels Mapping
const platformLabels = {
  douyin: '抖音',
  taobao: '淘宝',
  xiaohongshu: '小红书',
  jd: '京东',
  pdd: '拼多多'
}

const getPlatformLabel = (platform) => {
  return platformLabels[platform] || platform
}

// Fetch Product List with Pagination and Filters
const fetchProductList = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.page,
      pageSize: pagination.pageSize,
      keyword: searchKeyword.value,
      ...buildFilterParams()
    }
    const res = await getProductList(params)
    productList.value = res.data?.list || []
    pagination.total = res.data?.total || 0
  } catch (error) {
    ElMessage.error('获取商品列表失败')
  } finally {
    loading.value = false
  }
}

// Build Filter Parameters
const buildFilterParams = () => {
  const params = {}
  if (filterForm.platform) params.platform = filterForm.platform
  if (filterForm.status) params.status = filterForm.status
  if (filterForm.minPrice !== null && filterForm.minPrice !== undefined) {
    params.minPrice = filterForm.minPrice
  }
  if (filterForm.maxPrice !== null && filterForm.maxPrice !== undefined) {
    params.maxPrice = filterForm.maxPrice
  }
  return params
}

// Handle Filter
const handleFilter = () => {
  pagination.page = 1
  fetchProductList()
}

// Reset Filter
const handleResetFilter = () => {
  filterForm.platform = ''
  filterForm.status = ''
  filterForm.minPrice = null
  filterForm.maxPrice = null
  searchKeyword.value = ''
  pagination.page = 1
  fetchProductList()
}

// Handle Search
const handleSearch = () => {
  pagination.page = 1
  fetchProductList()
}

// Handle Size Change
const handleSizeChange = (size) => {
  pagination.pageSize = size
  pagination.page = 1
  fetchProductList()
}

// Handle Page Change
const handlePageChange = (page) => {
  pagination.page = page
  fetchProductList()
}

// Handle Selection Change
const handleSelectionChange = (selection) => {
  selectedProducts.value = selection
}

// Handle Add Product
const handleAdd = () => {
  dialogType.value = 'add'
  currentProduct.value = null
  dialogVisible.value = true
}

// Handle Edit Product
const handleEdit = (row) => {
  dialogType.value = 'edit'
  currentProduct.value = row
  dialogVisible.value = true
}

// Handle View SKUs
const handleViewSkus = (row) => {
  dialogType.value = 'edit'
  currentProduct.value = row
  dialogVisible.value = true
}

// Handle Dialog Submit
const handleDialogSubmit = async (formData) => {
  submitLoading.value = true
  try {
    if (dialogType.value === 'add') {
      await createProduct(formData)
      ElMessage.success('商品添加成功')
    } else {
      await updateProduct(formData.id, formData)
      ElMessage.success('商品更新成功')
    }
    dialogVisible.value = false
    fetchProductList()
  } catch (error) {
    ElMessage.error(dialogType.value === 'add' ? '添加商品失败' : '更新商品失败')
  } finally {
    submitLoading.value = false
  }
}

// Handle Match Source
const handleMatchSource = async (row) => {
  try {
    loading.value = true
    await matchSourceProduct(row.id)
    ElMessage.success('货源匹配成功')
    fetchProductList()
  } catch (error) {
    ElMessage.error('货源匹配失败')
  } finally {
    loading.value = false
  }
}

// Handle Adjust Inventory
const handleAdjustInventory = (row) => {
  currentProduct.value = {
    id: row.id,
    name: row.name,
    sku: row.sku || '-',
    image: row.image || row.mainImage,
    currentStock: row.stock || 0,
    availableStock: row.stock || 0,
    reservedStock: 0
  }
  inventoryDialogVisible.value = true
}

// Handle Inventory Submit
const handleInventorySubmit = async (submitData) => {
  try {
    // TODO: Call inventory adjustment API
    ElMessage.success('库存调整成功')
    fetchProductList()
  } catch (error) {
    ElMessage.error('库存调整失败')
  }
}

// Handle Import
const handleImport = () => {
  importDialogVisible.value = true
}

// Handle Import Success
const handleImportSuccess = (result) => {
  ElMessage.success(`成功导入 ${result.count} 条商品数据`)
  fetchProductList()
}

// Handle Export
const handleExport = async () => {
  try {
    const params = {
      keyword: searchKeyword.value,
      ...buildFilterParams()
    }
    const res = await exportProducts(params)

    // Create blob and download
    const blob = new Blob([res.data], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })
    const link = document.createElement('a')
    link.href = URL.createObjectURL(blob)
    link.download = `商品列表_${new Date().toISOString().slice(0, 10)}.xlsx`
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)

    ElMessage.success('导出成功')
  } catch (error) {
    ElMessage.error('导出失败')
  }
}

// Handle Batch Delete
const handleBatchDelete = async () => {
  try {
    await ElMessageBox.confirm(
      `确定要删除选中的 ${selectedProducts.value.length} 个商品吗？`,
      '批量删除',
      { type: 'warning' }
    )
    const deletePromises = selectedProducts.value.map(row => deleteProduct(row.id))
    await Promise.all(deletePromises)
    ElMessage.success('批量删除成功')
    selectedProducts.value = []
    fetchProductList()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('批量删除失败')
    }
  }
}

// Handle Batch Update Status
const handleBatchUpdateStatus = async (status) => {
  const statusText = status === 'active' ? '上架' : '下架'
  try {
    await ElMessageBox.confirm(
      `确定要将选中的 ${selectedProducts.value.length} 个商品${statusText}吗？`,
      `批量${statusText}`,
      { type: 'warning' }
    )
    const updatePromises = selectedProducts.value.map(row =>
      updateProduct(row.id, { ...row, status })
    )
    await Promise.all(updatePromises)
    ElMessage.success(`批量${statusText}成功`)
    selectedProducts.value = []
    fetchProductList()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(`批量${statusText}失败`)
    }
  }
}

// Initialize
onMounted(() => {
  fetchProductList()
})
</script>

<style lang="scss" scoped>
.products-page {
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;

    .header-actions {
      display: flex;
      gap: 10px;
      align-items: center;
    }
  }

  .filter-panel {
    padding: 16px;
    background-color: var(--el-fill-color-light);
    border-radius: 4px;
    margin-bottom: 16px;

    .el-form-item {
      margin-bottom: 0;
    }

    .price-range {
      display: flex;
      align-items: center;
      gap: 8px;

      .price-separator {
        color: var(--el-text-color-secondary);
      }
    }
  }

  .filter-actions {
    display: flex;
    align-items: center;
    gap: 8px;
  }

  .batch-operations {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 12px 16px;
    background-color: var(--el-fill-color-light);
    border-radius: 4px;
    margin-bottom: 16px;

    .batch-info {
      font-size: 14px;
      color: var(--el-text-color-primary);
      margin-right: 8px;
    }
  }

  .pagination-wrapper {
    display: flex;
    justify-content: flex-end;
    margin-top: 16px;
    padding-top: 16px;
    border-top: 1px solid var(--el-border-color-light);
  }

  .low-stock {
    color: var(--el-color-danger);
    font-weight: bold;
  }
}
</style>
