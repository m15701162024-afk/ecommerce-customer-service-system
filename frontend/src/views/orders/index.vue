<template>
  <div class="orders-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>订单列表</span>
          <div class="header-actions">
            <el-select v-model="filters.platform" placeholder="平台" clearable style="width: 120px">
              <el-option label="抖音" value="douyin" />
              <el-option label="淘宝" value="taobao" />
              <el-option label="小红书" value="xiaohongshu" />
            </el-select>
            <el-select v-model="filters.status" placeholder="状态" clearable style="width: 120px">
              <el-option label="待支付" value="pending" />
              <el-option label="待采购" value="to_purchase" />
              <el-option label="待发货" value="to_ship" />
              <el-option label="已发货" value="shipped" />
              <el-option label="已完成" value="completed" />
            </el-select>
            <el-input v-model="filters.keyword" placeholder="搜索订单号" style="width: 200px" clearable />
            <el-button type="primary" @click="handleSearch">搜索</el-button>
          </div>
        </div>
      </template>
      
      <el-table :data="orderList" v-loading="loading" style="width: 100%">
        <el-table-column prop="orderNo" label="订单号" width="180" />
        <el-table-column prop="platform" label="平台" width="100">
          <template #default="{ row }">
            <el-tag :type="getPlatformType(row.platform)">{{ row.platform }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="buyerName" label="买家" width="120" />
        <el-table-column prop="productName" label="商品" show-overflow-tooltip />
        <el-table-column prop="amount" label="金额" width="100">
          <template #default="{ row }">¥{{ row.amount.toFixed(2) }}</template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">{{ row.statusText }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="下单时间" width="180" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleDetail(row)">详情</el-button>
            <el-button type="warning" link @click="handlePurchase(row)" v-if="row.status === 'to_purchase'">采购</el-button>
            <el-button type="success" link @click="handleShip(row)" v-if="row.status === 'to_ship'">发货</el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :total="pagination.total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'

const loading = ref(false)
const filters = reactive({
  platform: '',
  status: '',
  keyword: ''
})

const pagination = reactive({
  page: 1,
  size: 10,
  total: 100
})

const orderList = ref([
  { orderNo: 'DD202401150001', platform: '抖音', buyerName: '张三', productName: 'iPhone 15 Pro Max 256GB', amount: 8999, status: 'to_purchase', statusText: '待采购', createdAt: '2024-01-15 10:30:00' },
  { orderNo: 'DD202401150002', platform: '淘宝', buyerName: '李四', productName: 'MacBook Air M3', amount: 8499, status: 'to_ship', statusText: '待发货', createdAt: '2024-01-15 11:20:00' },
  { orderNo: 'DD202401150003', platform: '小红书', buyerName: '王五', productName: 'AirPods Pro 2', amount: 1899, status: 'shipped', statusText: '已发货', createdAt: '2024-01-15 09:15:00' },
  { orderNo: 'DD202401150004', platform: '抖音', buyerName: '赵六', productName: 'iPad Pro 12.9', amount: 10999, status: 'completed', statusText: '已完成', createdAt: '2024-01-14 15:45:00' },
  { orderNo: 'DD202401150005', platform: '淘宝', buyerName: '钱七', productName: 'Apple Watch Ultra 2', amount: 6499, status: 'pending', statusText: '待支付', createdAt: '2024-01-15 14:00:00' }
])

const getPlatformType = (platform) => {
  const types = { '抖音': 'danger', '淘宝': 'warning', '小红书': 'success' }
  return types[platform] || 'info'
}

const getStatusType = (status) => {
  const types = { pending: 'info', to_purchase: 'warning', to_ship: '', shipped: 'primary', completed: 'success' }
  return types[status] || 'info'
}

const handleSearch = () => {
  console.log('搜索:', filters)
}

const handleDetail = (row) => {
  console.log('查看详情:', row)
}

const handlePurchase = (row) => {
  console.log('采购:', row)
}

const handleShip = (row) => {
  console.log('发货:', row)
}
</script>

<style lang="scss" scoped>
.orders-page {
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    
    .header-actions {
      display: flex;
      gap: 10px;
    }
  }
  
  .pagination-wrapper {
    margin-top: 20px;
    display: flex;
    justify-content: flex-end;
  }
}
</style>