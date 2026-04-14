<template>
  <div class="purchase-detail-page">
    <el-card v-loading="loading">
      <template #header>
        <div class="card-header">
          <span>采购详情</span>
          <el-button @click="goBack">返回</el-button>
        </div>
      </template>

      <el-tabs v-model="activeTab">
        <el-tab-pane label="基本信息" name="base">
          <el-descriptions :column="2" border>
            <el-descriptions-item label="采购单号">{{ purchaseDetail.purchaseNo }}</el-descriptions-item>
            <el-descriptions-item label="销售订单号">{{ purchaseDetail.orderNo }}</el-descriptions-item>
            <el-descriptions-item label="状态">
              <el-tag :type="getStatusType(purchaseDetail.status)">{{ purchaseDetail.statusText }}</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="支付方式">
              <el-tag v-if="purchaseDetail.paymentType === 'auto'" type="success">自动</el-tag>
              <el-tag v-else type="warning">人工</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="采购价">¥{{ purchaseDetail.sourcePrice?.toFixed(2) }}</el-descriptions-item>
            <el-descriptions-item label="售价">¥{{ purchaseDetail.sellPrice?.toFixed(2) }}</el-descriptions-item>
            <el-descriptions-item label="利润">
              <span :class="{ 'profit-positive': purchaseDetail.profit > 0, 'profit-negative': purchaseDetail.profit < 0 }">
                ¥{{ purchaseDetail.profit?.toFixed(2) }}
              </span>
            </el-descriptions-item>
            <el-descriptions-item label="创建时间">{{ purchaseDetail.createdAt }}</el-descriptions-item>
            <el-descriptions-item label="采购时间" v-if="purchaseDetail.purchaseTime">{{ purchaseDetail.purchaseTime }}</el-descriptions-item>
            <el-descriptions-item label="失败原因" v-if="purchaseDetail.failReason">{{ purchaseDetail.failReason }}</el-descriptions-item>
          </el-descriptions>
        </el-tab-pane>

        <el-tab-pane label="商品信息" name="product">
          <el-descriptions :column="1" border>
            <el-descriptions-item label="商品名称">{{ purchaseDetail.productName }}</el-descriptions-item>
            <el-descriptions-item label="商品图片">
              <el-image 
                v-if="purchaseDetail.productImage" 
                :src="purchaseDetail.productImage" 
                style="width: 80px; height: 80px" 
                fit="cover"
              />
              <span v-else>-</span>
            </el-descriptions-item>
            <el-descriptions-item label="SKU">{{ purchaseDetail.sku || '-' }}</el-descriptions-item>
            <el-descriptions-item label="数量">{{ purchaseDetail.quantity || 1 }}</el-descriptions-item>
          </el-descriptions>
        </el-tab-pane>

        <el-tab-pane label="供应商信息" name="supplier">
          <el-descriptions :column="1" border>
            <el-descriptions-item label="供应商">{{ purchaseDetail.supplier || '-' }}</el-descriptions-item>
            <el-descriptions-item label="供应商链接" v-if="purchaseDetail.supplierLink">
              <el-link :href="purchaseDetail.supplierLink" type="primary" target="_blank">
                查看链接
              </el-link>
            </el-descriptions-item>
            <el-descriptions-item label="联系方式">{{ purchaseDetail.supplierContact || '-' }}</el-descriptions-item>
            <el-descriptions-item label="备注">{{ purchaseDetail.supplierRemark || '-' }}</el-descriptions-item>
          </el-descriptions>
        </el-tab-pane>

        <el-tab-pane label="采购日志" name="logs">
          <el-timeline>
            <el-timeline-item
              v-for="(log, index) in purchaseDetail.logs || []"
              :key="index"
              :timestamp="log.time"
              :type="index === 0 ? 'primary' : ''"
            >
              {{ log.content }}
            </el-timeline-item>
            <el-timeline-item v-if="!purchaseDetail.logs?.length" timestamp="">
              暂无日志
            </el-timeline-item>
          </el-timeline>
        </el-tab-pane>
      </el-tabs>

      <div class="action-buttons" v-if="purchaseDetail.status">
        <el-button 
          type="primary" 
          @click="handlePurchase" 
          v-if="purchaseDetail.status === 'pending'"
        >
          立即采购
        </el-button>
        <el-button 
          type="success" 
          @click="handleConfirm" 
          v-if="purchaseDetail.status === 'processing'"
        >
          确认完成
        </el-button>
        <el-button 
          type="danger" 
          @click="handleCancel" 
          v-if="['pending', 'processing'].includes(purchaseDetail.status)"
        >
          取消采购
        </el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getPurchaseDetail, createPurchase, confirmPurchase } from '@/api/purchase'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const activeTab = ref('base')
const purchaseDetail = ref({})

const fetchPurchaseDetail = async () => {
  loading.value = true
  try {
    const orderNo = route.params.orderNo
    const res = await getPurchaseDetail(orderNo)
    purchaseDetail.value = res.data || {}
  } catch (error) {
    ElMessage.error('获取采购详情失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchPurchaseDetail()
})

const getStatusType = (status) => {
  const types = { pending: 'warning', processing: 'primary', completed: 'success', failed: 'danger' }
  return types[status] || 'info'
}

const goBack = () => {
  router.back()
}

const handlePurchase = async () => {
  try {
    await ElMessageBox.confirm('确认执行采购?', '提示', { type: 'warning' })
    loading.value = true
    await createPurchase({ orderNo: purchaseDetail.value.orderNo })
    ElMessage.success('采购成功')
    fetchPurchaseDetail()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('采购失败')
    }
  } finally {
    loading.value = false
  }
}

const handleConfirm = async () => {
  try {
    await ElMessageBox.confirm('确认采购完成?', '提示', { type: 'warning' })
    loading.value = true
    await confirmPurchase(purchaseDetail.value.purchaseNo || purchaseDetail.value.orderNo)
    ElMessage.success('确认成功')
    fetchPurchaseDetail()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('确认失败')
    }
  } finally {
    loading.value = false
  }
}

const handleCancel = async () => {
  try {
    const { value: reason } = await ElMessageBox.prompt('请输入取消原因', '取消采购', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputPattern: /\S+/,
      inputErrorMessage: '请输入取消原因'
    })
    ElMessage.success('已取消采购')
    router.push('/purchase')
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('操作失败')
    }
  }
}
</script>

<style lang="scss" scoped>
.purchase-detail-page {
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .profit-positive {
    color: #67c23a;
    font-weight: 500;
  }
  
  .profit-negative {
    color: #f56c6c;
    font-weight: 500;
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