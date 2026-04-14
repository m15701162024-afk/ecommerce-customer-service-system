<template>
  <el-dialog
    :model-value="visible"
    @update:model-value="handleVisibleChange"
    title="选择商品"
    width="800px"
    :close-on-click-modal="false"
    @close="handleClose"
    class="product-selector-dialog"
  >
    <div class="product-selector-content">
      <!-- 搜索栏 -->
      <div class="search-bar">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索商品名称"
          clearable
          @keyup.enter="handleSearch"
          style="width: 300px"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        <el-button type="primary" @click="handleSearch">搜索</el-button>
        <el-button @click="resetSearch">重置</el-button>
      </div>

      <!-- 商品列表 -->
      <div class="product-list-wrapper">
        <el-table
          :data="productList"
          v-loading="loading"
          @selection-change="handleSelectionChange"
          row-key="id"
          ref="tableRef"
          style="width: 100%"
          height="400px"
        >
          <el-table-column type="selection" width="55" reserve-selection />
          <el-table-column label="商品图片" width="80">
            <template #default="{ row }">
              <el-image
                :src="row.image || defaultImage"
                style="width: 50px; height: 50px"
                fit="cover"
                class="product-image"
              >
                <template #error>
                  <div class="image-placeholder">
                    <el-icon><Picture /></el-icon>
                  </div>
                </template>
              </el-image>
            </template>
          </el-table-column>
          <el-table-column prop="name" label="商品名称" show-overflow-tooltip min-width="180" />
          <el-table-column prop="price" label="售价" width="120">
            <template #default="{ row }">
              <span class="price">¥{{ row.price.toFixed(2) }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="stock" label="库存" width="100">
            <template #default="{ row }">
              <el-tag :type="row.stock > 0 ? 'success' : 'danger'" size="small">
                {{ row.stock }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="row.status === 'active' ? 'success' : 'info'" size="small">
                {{ row.status === 'active' ? '上架' : '下架' }}
              </el-tag>
            </template>
          </el-table-column>
        </el-table>

        <!-- 空状态 -->
        <el-empty v-if="!loading && productList.length === 0" description="暂无商品数据" :image-size="100" />
      </div>

      <!-- 分页 -->
      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.pageSize"
          :total="pagination.total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next"
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
        />
      </div>

      <!-- 已选商品展示 -->
      <div class="selected-info" v-if="selectedProducts.length > 0">
        <span class="selected-label">已选择：</span>
        <el-tag
          v-for="product in selectedProducts.slice(0, 5)"
          :key="product.id"
          closable
          size="small"
          @close="handleRemoveSelection(product.id)"
          class="selected-tag"
        >
          {{ product.name }}
        </el-tag>
        <el-tag v-if="selectedProducts.length > 5" size="small" type="info">
          +{{ selectedProducts.length - 5 }} 更多
        </el-tag>
      </div>
    </div>

    <template #footer>
      <div class="dialog-footer">
        <span class="selected-count">
          已选择 <strong>{{ selectedProducts.length }}</strong> 个商品
        </span>
        <div class="footer-actions">
          <el-button @click="handleClose">取消</el-button>
          <el-button type="primary" @click="handleConfirm">
            确定
          </el-button>
        </div>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, computed, watch, nextTick } from 'vue'
import { Search, Picture } from '@element-plus/icons-vue'

const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  },
  selected: {
    type: Array,
    default: () => []
  }
})

const emit = defineEmits(['update:visible', 'update:selected', 'confirm'])

// 表格引用
const tableRef = ref(null)

// 加载状态
const loading = ref(false)

// 搜索关键字
const searchKeyword = ref('')

// 已选择的商品ID
const selectedIds = ref([])

// 分页信息
const pagination = reactive({
  page: 1,
  pageSize: 10,
  total: 0
})

// 默认图片
const defaultImage = ''

// 模拟商品数据
const allProducts = [
  { id: 1, name: 'iPhone 15 Pro Max 256GB', price: 9999, stock: 50, status: 'active', image: '' },
  { id: 2, name: 'iPhone 15 Pro 128GB', price: 7999, stock: 30, status: 'active', image: '' },
  { id: 3, name: 'MacBook Air M3 13英寸', price: 8999, stock: 20, status: 'active', image: '' },
  { id: 4, name: 'MacBook Pro M3 14英寸', price: 12999, stock: 15, status: 'active', image: '' },
  { id: 5, name: 'AirPods Pro 第二代', price: 1899, stock: 100, status: 'active', image: '' },
  { id: 6, name: 'iPad Pro 12.9英寸 M2', price: 8999, stock: 25, status: 'active', image: '' },
  { id: 7, name: 'Apple Watch Ultra 2', price: 6299, stock: 40, status: 'active', image: '' },
  { id: 8, name: 'Apple Watch Series 9', price: 2999, stock: 60, status: 'active', image: '' },
  { id: 9, name: 'AirPods Max 银色', price: 4399, stock: 10, status: 'inactive', image: '' },
  { id: 10, name: 'HomePod 第二代', price: 2299, stock: 0, status: 'active', image: '' },
  { id: 11, name: 'Apple TV 4K', price: 1299, stock: 35, status: 'active', image: '' },
  { id: 12, name: 'Magic Keyboard', price: 2399, stock: 45, status: 'active', image: '' },
  { id: 13, name: 'Magic Mouse', price: 699, stock: 80, status: 'active', image: '' },
  { id: 14, name: 'AirTag 四件装', price: 779, stock: 200, status: 'active', image: '' },
  { id: 15, name: 'MagSafe 外接电池', price: 749, stock: 30, status: 'active', image: '' }
]

// 过滤后的商品列表
const productList = computed(() => {
  let filtered = allProducts
  
  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase()
    filtered = allProducts.filter(p => p.name.toLowerCase().includes(keyword))
  }
  
  pagination.total = filtered.length
  
  const start = (pagination.page - 1) * pagination.pageSize
  const end = start + pagination.pageSize
  
  return filtered.slice(start, end)
})

// 已选择的商品详情
const selectedProducts = computed(() => {
  return allProducts.filter(p => selectedIds.value.includes(p.id))
})

// 监听 visible 变化
watch(() => props.visible, async (val) => {
  if (val) {
    // 打开弹窗时，初始化选中状态
    selectedIds.value = [...props.selected]
    
    // 等待表格渲染完成后恢复选中状态
    await nextTick()
    
    // 重新加载数据并恢复选中
    const selectedRows = allProducts.filter(p => selectedIds.value.includes(p.id))
    selectedRows.forEach(row => {
      tableRef.value?.toggleRowSelection(row, true)
    })
  }
})

// 监听表格数据变化，恢复选中状态
watch(() => productList.value, async () => {
  await nextTick()
  const selectedRows = productList.value.filter(p => selectedIds.value.includes(p.id))
  selectedRows.forEach(row => {
    tableRef.value?.toggleRowSelection(row, true)
  })
}, { immediate: true })

// 处理选中变化
const handleSelectionChange = (selection) => {
  // 获取当前页选中的ID
  const currentPageSelectedIds = selection.map(item => item.id)
  
  // 获取当前页的所有ID
  const currentPageIds = productList.value.map(item => item.id)
  
  // 移除当前页中未选中的ID
  selectedIds.value = selectedIds.value.filter(id => !currentPageIds.includes(id))
  
  // 添加当前页新选中的ID
  selectedIds.value.push(...currentPageSelectedIds)
  
  // 去重
  selectedIds.value = [...new Set(selectedIds.value)]
}

// 移除选中
const handleRemoveSelection = (id) => {
  const index = selectedIds.value.indexOf(id)
  if (index > -1) {
    selectedIds.value.splice(index, 1)
    
    // 取消表格中的选中
    const row = productList.value.find(p => p.id === id)
    if (row) {
      tableRef.value?.toggleRowSelection(row, false)
    }
  }
}

// 搜索
const handleSearch = () => {
  pagination.page = 1
}

// 重置搜索
const resetSearch = () => {
  searchKeyword.value = ''
  pagination.page = 1
}

// 处理分页大小变化
const handleSizeChange = (size) => {
  pagination.pageSize = size
  pagination.page = 1
}

// 处理页码变化
const handlePageChange = (page) => {
  pagination.page = page
}

// 处理可见性变化
const handleVisibleChange = (val) => {
  emit('update:visible', val)
}

// 处理关闭
const handleClose = () => {
  emit('update:visible', false)
}

// 处理确认
const handleConfirm = () => {
  emit('update:selected', [...selectedIds.value])
  emit('confirm', [...selectedIds.value])
  emit('update:visible', false)
}
</script>

<style lang="scss" scoped>
.product-selector-dialog {
  :deep(.el-dialog__body) {
    padding: 0;
  }

  :deep(.el-dialog__header) {
    border-bottom: 1px solid #e4e7ed;
    margin-right: 0;
    padding: 16px 20px;
  }

  :deep(.el-dialog__footer) {
    border-top: 1px solid #e4e7ed;
    padding: 16px 20px;
  }
}

.product-selector-content {
  padding: 20px;

  .search-bar {
    display: flex;
    align-items: center;
    gap: 12px;
    margin-bottom: 16px;
  }

  .product-list-wrapper {
    margin-bottom: 16px;

    .product-image {
      border-radius: 4px;
      overflow: hidden;

      .image-placeholder {
        width: 100%;
        height: 100%;
        display: flex;
        align-items: center;
        justify-content: center;
        background-color: #f5f7fa;
        color: #c0c4cc;
        font-size: 20px;
      }
    }

    .price {
      color: #f56c6c;
      font-weight: 500;
    }
  }

  .pagination-wrapper {
    display: flex;
    justify-content: flex-end;
    margin-bottom: 16px;
  }

  .selected-info {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 12px;
    background-color: #f5f7fa;
    border-radius: 4px;
    flex-wrap: wrap;

    .selected-label {
      font-size: 13px;
      color: #606266;
      font-weight: 500;
    }

    .selected-tag {
      max-width: 150px;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }
  }
}

.dialog-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;

  .selected-count {
    font-size: 14px;
    color: #606266;

    strong {
      color: #409eff;
      font-weight: 600;
    }
  }

  .footer-actions {
    display: flex;
    gap: 12px;
  }
}
</style>
