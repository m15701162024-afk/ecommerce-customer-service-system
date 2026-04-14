<template>
  <el-dialog
    :model-value="visible"
    @update:model-value="handleVisibleChange"
    title="退款处理"
    width="800px"
    :close-on-click-modal="false"
    class="refund-process-dialog"
  >
    <div v-if="refundData" class="refund-content">
      <!-- 订单信息 -->
      <el-card class="info-card" shadow="never">
        <template #header>
          <div class="card-header">
            <el-icon><Document /></el-icon>
            <span>订单信息</span>
          </div>
        </template>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="订单编号" :span="2">
            <span class="order-no">{{ refundData.orderNo }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="商品信息" :span="2">
            <div class="product-info">
              <el-image
                v-if="refundData.productImage"
                :src="refundData.productImage"
                :preview-src-list="[refundData.productImage]"
                fit="cover"
                class="product-image"
              />
              <div class="product-detail">
                <div class="product-name">{{ refundData.productName || '-' }}</div>
                <div class="product-sku" v-if="refundData.productSku">
                  规格: {{ refundData.productSku }}
                </div>
              </div>
            </div>
          </el-descriptions-item>
          <el-descriptions-item label="订单金额">
            <span class="text-price">¥{{ refundData.orderAmount || '0.00' }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="订单状态">
            <el-tag :type="orderStatusType(refundData.orderStatus)">
              {{ orderStatusText(refundData.orderStatus) }}
            </el-tag>
          </el-descriptions-item>
        </el-descriptions>
      </el-card>

      <!-- 退款申请信息 -->
      <el-card class="info-card" shadow="never">
        <template #header>
          <div class="card-header">
            <el-icon><Money /></el-icon>
            <span>退款申请信息</span>
          </div>
        </template>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="售后单号">{{ refundData.afterSaleNo }}</el-descriptions-item>
          <el-descriptions-item label="申请时间">{{ formatTime(refundData.createdAt) }}</el-descriptions-item>
          <el-descriptions-item label="退款类型">
            <el-tag :type="typeColor(refundData.type)">{{ typeText(refundData.type) }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="申请状态">
            <el-tag :type="statusColor(refundData.status)">{{ statusText(refundData.status) }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="退款原因" :span="2">{{ refundData.reason }}</el-descriptions-item>
          <el-descriptions-item label="详细说明" :span="2" v-if="refundData.description">
            <div class="description-text">{{ refundData.description }}</div>
          </el-descriptions-item>
          <el-descriptions-item label="退款金额" :span="2">
            <div class="refund-amount-display">
              <span class="original-amount">申请金额: <span class="text-price">¥{{ refundData.refundAmount || '0.00' }}</span></span>
            </div>
          </el-descriptions-item>
        </el-descriptions>
      </el-card>

      <!-- 买家信息 -->
      <el-card class="info-card" shadow="never">
        <template #header>
          <div class="card-header">
            <el-icon><User /></el-icon>
            <span>买家信息</span>
          </div>
        </template>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="买家账号">{{ refundData.buyerName }}</el-descriptions-item>
          <el-descriptions-item label="联系电话">{{ refundData.buyerPhone || '-' }}</el-descriptions-item>
          <el-descriptions-item label="买家ID">{{ refundData.buyerId || '-' }}</el-descriptions-item>
          <el-descriptions-item label="平台">{{ refundData.platform || '-' }}</el-descriptions-item>
        </el-descriptions>
      </el-card>

      <!-- 处理表单 -->
      <el-card class="info-card" shadow="never">
        <template #header>
          <div class="card-header">
            <el-icon><EditPen /></el-icon>
            <span>审核处理</span>
          </div>
        </template>
        <el-form
          ref="formRef"
          :model="form"
          :rules="rules"
          label-position="top"
          class="process-form"
        >
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="实际退款金额" prop="actualRefundAmount">
                <el-input-number
                  v-model="form.actualRefundAmount"
                  :min="0"
                  :max="maxRefundAmount"
                  :precision="2"
                  :step="1"
                  controls-position="right"
                  style="width: 100%"
                  size="large"
                >
                  <template #prefix>¥</template>
                </el-input-number>
                <div class="amount-hint">
                  最大可退: ¥{{ maxRefundAmount }} | 订单金额: ¥{{ refundData.orderAmount || '0.00' }}
                </div>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="审核备注" prop="remark">
                <el-input
                  v-model="form.remark"
                  type="textarea"
                  :rows="3"
                  placeholder="请输入审核备注（必填）"
                  maxlength="500"
                  show-word-limit
                />
              </el-form-item>
            </el-col>
          </el-row>
        </el-form>
      </el-card>
    </div>

    <!-- 底部操作按钮 -->
    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleClose" plain>取消</el-button>
        <el-button
          type="danger"
          @click="handleReject"
          :loading="submitting"
          :disabled="!form.remark?.trim()"
        >
          <el-icon><CircleClose /></el-icon>
          拒绝退款
        </el-button>
        <el-button
          type="success"
          @click="handleApprove"
          :loading="submitting"
          :disabled="!form.remark?.trim()"
        >
          <el-icon><CircleCheck /></el-icon>
          同意退款
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, computed, watch, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Document,
  Money,
  User,
  EditPen,
  CircleCheck,
  CircleClose
} from '@element-plus/icons-vue'

const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  },
  refundData: {
    type: Object,
    default: () => null
  }
})

const emit = defineEmits(['update:visible', 'approve', 'reject'])

// 表单引用
const formRef = ref(null)

// 提交状态
const submitting = ref(false)

// 表单数据
const form = ref({
  actualRefundAmount: 0,
  remark: ''
})

// 最大退款金额
const maxRefundAmount = computed(() => {
  if (!props.refundData) return 0
  return parseFloat(props.refundData.orderAmount || props.refundData.refundAmount || 0)
})

// 表单验证规则
const rules = {
  actualRefundAmount: [
    { required: true, message: '请输入退款金额', trigger: 'blur' }
  ],
  remark: [
    { required: true, message: '请输入审核备注', trigger: 'blur' },
    { min: 1, max: 500, message: '备注长度在 1 到 500 个字符', trigger: 'blur' }
  ]
}

// 监听数据变化，初始化表单
watch(() => props.refundData, (newVal) => {
  if (newVal) {
    nextTick(() => {
      form.value = {
        actualRefundAmount: parseFloat(newVal.refundAmount || 0),
        remark: ''
      }
      // 重置表单验证
      formRef.value?.resetFields()
    })
  }
}, { immediate: true })

// 监听visible变化
const handleVisibleChange = (val) => {
  emit('update:visible', val)
}

// 关闭弹窗
const handleClose = () => {
  emit('update:visible', false)
}

// 格式化时间
const formatTime = (time) => {
  if (!time) return '-'
  return new Date(time).toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  })
}

// 类型相关
const typeText = (type) => {
  const map = {
    REFUND_ONLY: '仅退款',
    RETURN_REFUND: '退货退款',
    EXCHANGE: '换货'
  }
  return map[type] || type
}

const typeColor = (type) => {
  const map = {
    REFUND_ONLY: 'warning',
    RETURN_REFUND: 'danger',
    EXCHANGE: 'info'
  }
  return map[type] || ''
}

// 状态相关
const statusText = (status) => {
  const map = {
    PENDING: '待审核',
    WAITING_RETURN: '待退货',
    WAITING_REFUND: '待退款',
    COMPLETED: '已完成',
    REJECTED: '已拒绝'
  }
  return map[status] || status
}

const statusColor = (status) => {
  const map = {
    PENDING: 'warning',
    WAITING_RETURN: 'info',
    WAITING_REFUND: 'primary',
    COMPLETED: 'success',
    REJECTED: 'danger'
  }
  return map[status] || ''
}

// 订单状态相关
const orderStatusText = (status) => {
  const map = {
    PENDING_PAY: '待付款',
    PAID: '已付款',
    SHIPPED: '已发货',
    DELIVERED: '已送达',
    COMPLETED: '已完成',
    CANCELLED: '已取消'
  }
  return map[status] || status || '-'
}

const orderStatusType = (status) => {
  const map = {
    PENDING_PAY: 'warning',
    PAID: 'success',
    SHIPPED: 'primary',
    DELIVERED: 'info',
    COMPLETED: 'success',
    CANCELLED: 'info'
  }
  return map[status] || ''
}

// 处理同意退款
const handleApprove = async () => {
  if (!form.value.remark?.trim()) {
    ElMessage.warning('请输入审核备注')
    return
  }

  try {
    await formRef.value?.validate()

    // 检查退款金额
    if (form.value.actualRefundAmount <= 0) {
      ElMessage.warning('退款金额必须大于0')
      return
    }

    if (form.value.actualRefundAmount > maxRefundAmount.value) {
      ElMessage.warning('退款金额不能超过订单金额')
      return
    }

    // 确认操作
    await ElMessageBox.confirm(
      `确认同意退款申请？\n退款金额: ¥${form.value.actualRefundAmount.toFixed(2)}`,
      '确认通过',
      {
        confirmButtonText: '确认通过',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    submitting.value = true

    emit('approve', {
      id: props.refundData.id,
      afterSaleNo: props.refundData.afterSaleNo,
      actualRefundAmount: form.value.actualRefundAmount,
      remark: form.value.remark.trim()
    })
  } catch (error) {
    if (error !== 'cancel') {
      console.error('审核通过失败:', error)
    }
  } finally {
    submitting.value = false
  }
}

// 处理拒绝退款
const handleReject = async () => {
  if (!form.value.remark?.trim()) {
    ElMessage.warning('请输入拒绝原因（备注）')
    return
  }

  try {
    await formRef.value?.validate()

    // 确认操作
    await ElMessageBox.confirm(
      '确认拒绝该退款申请？',
      '确认拒绝',
      {
        confirmButtonText: '确认拒绝',
        cancelButtonText: '取消',
        type: 'error'
      }
    )

    submitting.value = true

    emit('reject', {
      id: props.refundData.id,
      afterSaleNo: props.refundData.afterSaleNo,
      remark: form.value.remark.trim()
    })
  } catch (error) {
    if (error !== 'cancel') {
      console.error('审核拒绝失败:', error)
    }
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.refund-process-dialog :deep(.el-dialog__body) {
  padding: 0;
  max-height: 70vh;
  overflow-y: auto;
}

.refund-content {
  padding: 20px;
}

.info-card {
  margin-bottom: 16px;
}

.info-card:last-child {
  margin-bottom: 0;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
  font-size: 15px;
  color: #303133;
}

.card-header :deep(.el-icon) {
  font-size: 18px;
  color: #409EFF;
}

.order-no {
  font-family: 'Courier New', monospace;
  font-weight: 600;
  color: #409EFF;
  background-color: #ecf5ff;
  padding: 2px 8px;
  border-radius: 4px;
}

.product-info {
  display: flex;
  align-items: flex-start;
  gap: 12px;
}

.product-image {
  width: 80px;
  height: 80px;
  border-radius: 8px;
  border: 1px solid #ebeef5;
  flex-shrink: 0;
}

.product-detail {
  flex: 1;
  min-width: 0;
}

.product-name {
  font-size: 14px;
  color: #303133;
  line-height: 1.5;
  margin-bottom: 4px;
  word-break: break-all;
}

.product-sku {
  font-size: 12px;
  color: #909399;
}

.text-price {
  color: #f56c6c;
  font-weight: 600;
  font-size: 16px;
}

.description-text {
  white-space: pre-wrap;
  line-height: 1.6;
  color: #606266;
}

.refund-amount-display {
  display: flex;
  align-items: center;
  gap: 16px;
}

.original-amount {
  font-size: 14px;
  color: #606266;
}

.process-form {
  padding: 8px 0;
}

.amount-hint {
  margin-top: 8px;
  font-size: 12px;
  color: #909399;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  gap: 12px;
  padding: 16px 20px;
  border-top: 1px solid #ebeef5;
}

.dialog-footer .el-button {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  min-width: 100px;
}

/* Element Plus 组件样式覆盖 */
:deep(.el-descriptions__cell) {
  padding: 12px 16px;
}

:deep(.el-descriptions__label) {
  font-weight: 500;
  color: #606266;
  background-color: #f5f7fa;
  min-width: 100px;
}

:deep(.el-card__header) {
  padding: 12px 16px;
  border-bottom: 1px solid #ebeef5;
  background-color: #fafafa;
}

:deep(.el-card__body) {
  padding: 16px;
}

:deep(.el-input-number .el-input__wrapper) {
  padding-left: 8px;
}

:deep(.el-input-number__prefix) {
  color: #f56c6c;
  font-weight: 600;
  margin-right: 4px;
}

/* 响应式适配 */
@media (max-width: 768px) {
  .refund-content {
    padding: 12px;
  }

  .product-info {
    flex-direction: column;
    align-items: flex-start;
  }

  .product-image {
    width: 60px;
    height: 60px;
  }

  .refund-amount-display {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }

  .dialog-footer {
    flex-direction: column-reverse;
    gap: 8px;
  }

  .dialog-footer .el-button {
    width: 100%;
  }
}
</style>
