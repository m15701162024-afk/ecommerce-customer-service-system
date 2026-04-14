<template>
  <div class="order-detail-page">
    <el-card v-loading="loading">
      <template #header>
        <div class="card-header">
          <span>订单详情</span>
          <el-button @click="goBack">返回</el-button>
        </div>
      </template>

      <el-tabs v-model="activeTab">
        <el-tab-pane label="基本信息" name="base">
          <el-descriptions :column="2" border>
            <el-descriptions-item label="订单号">{{ orderDetail.orderNo }}</el-descriptions-item>
            <el-descriptions-item label="平台">
              <el-tag :type="getPlatformType(orderDetail.platform)">{{ orderDetail.platform }}</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="买家">{{ orderDetail.buyerName }}</el-descriptions-item>
            <el-descriptions-item label="买家电话">{{ orderDetail.buyerPhone || '-' }}</el-descriptions-item>
            <el-descriptions-item label="商品金额">¥{{ orderDetail.amount?.toFixed(2) }}</el-descriptions-item>
            <el-descriptions-item label="订单状态">
              <el-tag :type="getStatusType(orderDetail.status)">{{ orderDetail.statusText }}</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="下单时间">{{ orderDetail.createdAt }}</el-descriptions-item>
            <el-descriptions-item label="付款时间">{{ orderDetail.payTime || '-' }}</el-descriptions-item>
            <el-descriptions-item label="收货地址" :span="2">{{ orderDetail.address || '-' }}</el-descriptions-item>
            <el-descriptions-item label="备注" :span="2">{{ orderDetail.remark || '-' }}</el-descriptions-item>
          </el-descriptions>
        </el-tab-pane>

        <el-tab-pane label="商品信息" name="products">
          <el-table :data="orderDetail.items || []" style="width: 100%">
            <el-table-column prop="productName" label="商品名称" />
            <el-table-column prop="productImage" label="商品图片" width="100">
              <template #default="{ row }">
                <el-image 
                  v-if="row.productImage" 
                  :src="row.productImage" 
                  style="width: 60px; height: 60px" 
                  fit="cover"
                />
                <span v-else>-</span>
              </template>
            </el-table-column>
            <el-table-column prop="sku" label="SKU" />
            <el-table-column prop="quantity" label="数量" width="100" />
            <el-table-column prop="price" label="单价" width="100">
              <template #default="{ row }">¥{{ row.price?.toFixed(2) }}</template>
            </el-table-column>
            <el-table-column prop="subtotal" label="小计" width="100">
              <template #default="{ row }">¥{{ row.subtotal?.toFixed(2) }}</template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <el-tab-pane label="物流信息" name="logistics">
          <el-descriptions :column="1" border>
            <el-descriptions-item label="物流公司">{{ orderDetail.logistics?.company || '-' }}</el-descriptions-item>
            <el-descriptions-item label="快递单号">{{ orderDetail.logistics?.trackingNo || '-' }}</el-descriptions-item>
            <el-descriptions-item label="发货时间">{{ orderDetail.logistics?.shipTime || '-' }}</el-descriptions-item>
            <el-descriptions-item label="物流轨迹">
              <div v-if="orderDetail.logistics?.tracks?.length">
                <el-timeline>
                  <el-timeline-item
                    v-for="(track, index) in orderDetail.logistics.tracks"
                    :key="index"
                    :timestamp="track.time"
                    :type="index === 0 ? 'primary' : ''"
                  >
                    {{ track.content }}
                  </el-timeline-item>
                </el-timeline>
              </div>
              <span v-else>暂无物流信息</span>
            </el-descriptions-item>
          </el-descriptions>
        </el-tab-pane>
      </el-tabs>

      <div class="action-buttons" v-if="orderDetail.status">
        <el-button 
          type="warning" 
          @click="handlePurchase" 
          v-if="orderDetail.status === 'to_purchase'"
        >
          创建采购
        </el-button>
        <el-button 
          type="success" 
          @click="handleShip" 
          v-if="orderDetail.status === 'to_ship'"
        >
          发货
        </el-button>
        <el-button 
          type="danger" 
          @click="handleRefund" 
          v-if="['to_purchase', 'to_ship', 'shipped'].includes(orderDetail.status)"
        >
          退款
        </el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getOrderDetail, updateOrderStatus } from '@/api/order'
import { createPurchase } from '@/api/purchase'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const activeTab = ref('base')
const orderDetail = ref({})

// 获取订单详情
const fetchOrderDetail = async () => {
  loading.value = true
  try {
    const orderNo = route.params.orderNo
    const res = await getOrderDetail(orderNo)
    orderDetail.value = res.data || {}
  } catch (error) {
    ElMessage.error('获取订单详情失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchOrderDetail()
})

const getPlatformType = (platform) => {
  const types = { '抖音': 'danger', '淘宝': 'warning', '小红书': 'success' }
  return types[platform] || 'info'
}

const getStatusType = (status) => {
  const types = { pending: 'info', to_purchase: 'warning', to_ship: '', shipped: 'primary', completed: 'success', cancelled: 'danger' }
  return types[status] || 'info'
}

const goBack = () => {
  router.back()
}

const handlePurchase = async () => {
  try {
    await ElMessageBox.confirm('确认创建采购订单?', '提示', { type: 'warning' })
    loading.value = true
    await createPurchase({ orderNo: orderDetail.value.orderNo })
    ElMessage.success('采购订单创建成功')
    fetchOrderDetail()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('采购失败')
    }
  } finally {
    loading.value = false
  }
}

const handleShip = async () => {
  try {
    await ElMessageBox.confirm('确认发货?', '提示', { type: 'warning' })
    loading.value = true
    await updateOrderStatus(orderDetail.value.orderNo, 'shipped')
    ElMessage.success('发货成功')
    fetchOrderDetail()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('发货失败')
    }
  } finally {
    loading.value = false
  }
}

const handleRefund = async () => {
  try {
    const { value: reason } = await ElMessageBox.prompt('请输入退款原因', '退款确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputPattern: /\S+/,
      inputErrorMessage: '请输入退款原因'
    })
    loading.value = true
    await updateOrderStatus(orderDetail.value.orderNo, 'refunded')
    ElMessage.success('退款成功')
    fetchOrderDetail()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('退款失败')
    }
  } finally {
    loading.value = false
  }
}
</script>

<style lang="scss" scoped>
.order-detail-page {
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .action-buttons {
    margin-top: 20px;
    padding-top: 20px;
    border-top: 1px solid #eee;
    display: flex;
    gap: 10px;
  }
}
</style>