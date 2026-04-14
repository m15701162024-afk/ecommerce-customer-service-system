<template>
  <el-dialog
    :model-value="visible"
    @update:model-value="handleVisibleChange"
    title="库存调整"
    width="640px"
    :close-on-click-modal="false"
    @close="handleClose"
    class="inventory-adjust-dialog"
  >
    <div class="dialog-content">
      <!-- 商品信息展示卡片 -->
      <div class="product-info-card">
        <div class="product-header">
          <el-avatar 
            :src="productData?.image || defaultProductImage" 
            :size="80" 
            shape="square"
            class="product-image"
          />
          <div class="product-meta">
            <h3 class="product-name">{{ productData?.name || '-' }}</h3>
            <p class="product-sku">SKU: {{ productData?.sku || '-' }}</p>
          </div>
        </div>
        
        <!-- 库存概览 -->
        <div class="stock-overview">
          <div class="stock-item">
            <div class="stock-value current">{{ currentStock }}</div>
            <div class="stock-label">
              <el-icon><Box /></el-icon>
              <span>当前库存</span>
            </div>
          </div>
          <div class="stock-divider" />
          <div class="stock-item">
            <div class="stock-value available">{{ availableStock }}</div>
            <div class="stock-label">
              <el-icon><Check /></el-icon>
              <span>可用库存</span>
            </div>
          </div>
          <div class="stock-divider" />
          <div class="stock-item">
            <div class="stock-value reserved">{{ reservedStock }}</div>
            <div class="stock-label">
              <el-icon><Lock /></el-icon>
              <span>预占库存</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 调整表单 -->
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="100px"
        class="adjust-form"
      >
        <!-- 调整类型 -->
        <el-form-item label="调整类型" prop="adjustType">
          <el-radio-group v-model="formData.adjustType" size="large">
            <el-radio-button 
              v-for="type in adjustTypes" 
              :key="type.value" 
              :label="type.value"
            >
              <el-icon class="radio-icon">
                <component :is="type.icon" />
              </el-icon>
              {{ type.label }}
            </el-radio-button>
          </el-radio-group>
        </el-form-item>

        <!-- 调整数量 -->
        <el-form-item label="调整数量" prop="adjustQuantity" required>
          <div class="quantity-input-wrapper">
            <el-input-number
              v-model="formData.adjustQuantity"
              :min="adjustMin"
              :max="adjustMax"
              :precision="0"
              controls-position="right"
              size="large"
              class="quantity-input"
              @change="handleQuantityChange"
            />
            <span class="quantity-hint">
              {{ quantityHint }}
            </span>
          </div>
        </el-form-item>

        <!-- 调整后库存预览 -->
        <el-form-item label="调整后库存">
          <div class="preview-section">
            <div class="preview-arrow">
              <el-icon><ArrowRight /></el-icon>
            </div>
            <div class="preview-card" :class="previewCardClass">
              <div class="preview-value">
                <el-icon><TrendCharts /></el-icon>
                <span class="value">{{ adjustedStock }}</span>
                <span class="unit">件</span>
              </div>
              <div class="preview-change" :class="changeTypeClass">
                <el-icon>
                  <component :is="changeTypeIcon" />
                </el-icon>
                <span>{{ changeText }}</span>
              </div>
            </div>
          </div>
        </el-form-item>

        <!-- 调整原因 -->
        <el-form-item label="调整原因" prop="reason" required>
          <el-select
            v-model="formData.reason"
            placeholder="请选择调整原因"
            clearable
            size="large"
            style="width: 100%"
          >
            <el-option-group
              v-for="group in reasonGroups"
              :key="group.label"
              :label="group.label"
            >
              <el-option
                v-for="item in group.options"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              >
                <el-icon class="reason-icon">
                  <component :is="item.icon" />
                </el-icon>
                <span>{{ item.label }}</span>
              </el-option>
            </el-option-group>
          </el-select>
        </el-form-item>

        <!-- 备注说明 -->
        <el-form-item label="备注说明" prop="remark" required>
          <el-input
            v-model="formData.remark"
            type="textarea"
            :rows="3"
            placeholder="请填写调整原因、操作人、关联单据号等详细信息（必填）"
            maxlength="500"
            show-word-limit
            resize="none"
            size="large"
          />
        </el-form-item>

        <!-- 操作提示 -->
        <div class="operation-tips">
          <el-alert
            :title="tipTitle"
            :type="tipType"
            :closable="false"
            show-icon
          >
            <template #default>
              <div class="tip-content">
                <p>• 库存调整后将实时同步至各销售渠道</p>
                <p>• 减少库存时，调整数量不能超过可用库存</p>
                <p>• 请详细填写备注信息以便后续追溯</p>
              </div>
            </template>
          </el-alert>
        </div>
      </el-form>
    </div>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleClose" size="large">取消</el-button>
        <el-button 
          type="primary" 
          @click="handleSubmit" 
          :loading="submitLoading"
          :disabled="!isValid"
          size="large"
        >
          <el-icon><Check /></el-icon>
          <span>确认调整</span>
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import {
  Box,
  Check,
  Lock,
  ArrowRight,
  TrendCharts,
  ArrowUp,
  ArrowDown,
  Minus,
  ShoppingCart,
  RefreshRight,
  Warning,
  Document,
  CircleClose,
  MoreFilled,
  TopRight,
  BottomRight
} from '@element-plus/icons-vue'

// 默认商品图片
const defaultProductImage = 'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iODAiIGhlaWdodD0iODAiIHZpZXdCb3g9IjAgMCA4MCA4MCIgZmlsbD0ibm9uZSIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj4KPHJlY3Qgd2lkdGg9IjgwIiBoZWlnaHQ9IjgwIiBmaWxsPSIjRjVGN0ZBIi8+CjxwYXRoIGQ9Ik00MCAyNUMzNS41OCAyNSAzMiAyOC41OCAzMiAzM0MzMiAzNy40MiAzNS41OCA0MSA0MCA0MUM0NC40MiA0MSA0OCAzNy40MiA0OCAzM0M0OCAyOC41OCA0NC40MiAyNSA0MCAyNVoiIGZpbGw9IiNDMEM0Q0MiLz4KPHBhdGggZD0iTTQwIDQ0QzMyLjI2OCA0NCAyNiA1MC4yNjggMjYgNThWNTlINTRWNThDNzQgNTAuMjY4IDQ3LjczMiA0NCA0MCA0NFoiIGZpbGw9IiNDMEM0Q0MiLz4KPC9zdmc+'

// Props 定义
const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  },
  productData: {
    type: Object,
    default: () => ({
      id: null,
      name: '',
      sku: '',
      image: '',
      currentStock: 0,
      availableStock: 0,
      reservedStock: 0
    })
  }
})

// Emits 定义
const emit = defineEmits(['update:visible', 'submit'])

// 表单引用
const formRef = ref(null)
const submitLoading = ref(false)

// 调整类型选项
const adjustTypes = [
  { label: '入库', value: 'inbound', icon: TopRight },
  { label: '出库', value: 'outbound', icon: BottomRight },
  { label: '盘点调整', value: 'adjust', icon: RefreshRight }
]

// 调整原因分组选项
const reasonGroups = [
  {
    label: '入库相关',
    options: [
      { label: '采购入库', value: 'purchase_inbound', icon: ShoppingCart },
      { label: '退货入库', value: 'return_inbound', icon: RefreshRight },
      { label: '调拨入库', value: 'transfer_inbound', icon: ArrowRight }
    ]
  },
  {
    label: '出库相关',
    options: [
      { label: '销售出库', value: 'sales_outbound', icon: ShoppingCart },
      { label: '调拨出库', value: 'transfer_outbound', icon: ArrowRight },
      { label: '样品出库', value: 'sample_outbound', icon: Document }
    ]
  },
  {
    label: '调整相关',
    options: [
      { label: '盘点调整', value: 'inventory_adjust', icon: RefreshRight },
      { label: '损耗报损', value: 'loss_adjust', icon: Warning },
      { label: '其他原因', value: 'other', icon: MoreFilled }
    ]
  }
]

// 表单数据
const formData = reactive({
  adjustType: 'inbound',
  adjustQuantity: 1,
  reason: '',
  remark: ''
})

// 计算属性：当前库存数据
const currentStock = computed(() => props.productData?.currentStock || 0)
const availableStock = computed(() => props.productData?.availableStock || 0)
const reservedStock = computed(() => props.productData?.reservedStock || 0)

// 计算属性：调整后库存
const adjustedStock = computed(() => {
  const quantity = Number(formData.adjustQuantity) || 0
  if (formData.adjustType === 'outbound') {
    return Math.max(0, currentStock.value - quantity)
  }
  return currentStock.value + quantity
})

// 计算属性：数量变化相关
const quantityChange = computed(() => {
  const quantity = Number(formData.adjustQuantity) || 0
  if (formData.adjustType === 'outbound') {
    return -quantity
  }
  return quantity
})

const changeTypeClass = computed(() => {
  if (quantityChange.value > 0) return 'increase'
  if (quantityChange.value < 0) return 'decrease'
  return 'neutral'
})

const changeTypeIcon = computed(() => {
  if (quantityChange.value > 0) return ArrowUp
  if (quantityChange.value < 0) return ArrowDown
  return Minus
})

const changeText = computed(() => {
  const change = quantityChange.value
  if (change > 0) return `+${change} 件`
  if (change < 0) return `${change} 件`
  return '无变化'
})

// 计算属性：预览卡片样式
const previewCardClass = computed(() => {
  if (formData.adjustType === 'outbound') return 'outbound'
  if (formData.adjustType === 'adjust') return 'adjust'
  return 'inbound'
})

// 计算属性：数量输入限制
const adjustMin = computed(() => {
  return formData.adjustType === 'outbound' ? 1 : 1
})

const adjustMax = computed(() => {
  if (formData.adjustType === 'outbound') {
    return availableStock.value
  }
  return 999999
})

// 计算属性：数量提示文本
const quantityHint = computed(() => {
  if (formData.adjustType === 'outbound') {
    return `最多可出库 ${availableStock.value} 件`
  }
  if (formData.adjustType === 'adjust') {
    return '可输入负数表示减少'
  }
  return '正数表示增加库存'
})

// 计算属性：提示信息
const tipTitle = computed(() => {
  switch (formData.adjustType) {
    case 'inbound':
      return '入库操作提示'
    case 'outbound':
      return '出库操作提示'
    case 'adjust':
      return '盘点调整提示'
    default:
      return '操作提示'
  }
})

const tipType = computed(() => {
  switch (formData.adjustType) {
    case 'inbound':
      return 'success'
    case 'outbound':
      return 'warning'
    case 'adjust':
      return 'info'
    default:
      return 'info'
  }
})

// 计算属性：表单是否有效
const isValid = computed(() => {
  const quantity = Number(formData.adjustQuantity)
  if (!quantity || quantity === 0) return false
  if (!formData.reason) return false
  if (!formData.remark.trim()) return false
  
  if (formData.adjustType === 'outbound' && quantity > availableStock.value) {
    return false
  }
  
  return true
})

// 表单验证规则
const formRules = {
  adjustType: [
    { required: true, message: '请选择调整类型', trigger: 'change' }
  ],
  adjustQuantity: [
    { required: true, message: '请输入调整数量', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        const num = Number(value)
        if (!num || num === 0) {
          callback(new Error('调整数量不能为0'))
          return
        }
        if (num < 0) {
          callback(new Error('调整数量必须大于0'))
          return
        }
        if (formData.adjustType === 'outbound' && num > availableStock.value) {
          callback(new Error(`调整数量不能超过可用库存 ${availableStock.value} 件`))
          return
        }
        callback()
      },
      trigger: 'change'
    }
  ],
  reason: [
    { required: true, message: '请选择调整原因', trigger: 'change' }
  ],
  remark: [
    { required: true, message: '请填写备注说明', trigger: 'blur' },
    { min: 5, message: '备注说明至少5个字符', trigger: 'blur' }
  ]
}

// 监听 visible 变化
watch(() => props.visible, (val) => {
  if (val) {
    // 打开弹窗时重置表单
    resetForm()
  }
})

// 监听调整类型变化
watch(() => formData.adjustType, () => {
  // 重置调整数量
  formData.adjustQuantity = 1
  // 清除验证
  formRef.value?.clearValidate('adjustQuantity')
})

// 重置表单
const resetForm = () => {
  formData.adjustType = 'inbound'
  formData.adjustQuantity = 1
  formData.reason = ''
  formData.remark = ''
  
  // 重置验证
  formRef.value?.resetFields()
}

// 处理数量变化
const handleQuantityChange = (val) => {
  // 确保数量至少为1
  if (!val || val < 1) {
    formData.adjustQuantity = 1
  }
}

// 处理可见性变化
const handleVisibleChange = (val) => {
  emit('update:visible', val)
}

// 处理关闭
const handleClose = () => {
  formRef.value?.resetFields()
  resetForm()
  emit('update:visible', false)
}

// 处理提交
const handleSubmit = async () => {
  try {
    await formRef.value.validate()
  } catch {
    ElMessage.warning('请完善表单信息')
    return
  }

  // 再次验证出库数量
  if (formData.adjustType === 'outbound' && formData.adjustQuantity > availableStock.value) {
    ElMessage.error(`调整数量不能超过可用库存 ${availableStock.value} 件`)
    return
  }

  submitLoading.value = true

  try {
    // 构建提交数据
    const submitData = {
      productId: props.productData?.id,
      productName: props.productData?.name,
      productSku: props.productData?.sku,
      // 原始库存
      originalStock: currentStock.value,
      originalAvailable: availableStock.value,
      originalReserved: reservedStock.value,
      // 调整信息
      adjustType: formData.adjustType,
      adjustQuantity: formData.adjustType === 'outbound' 
        ? -formData.adjustQuantity 
        : formData.adjustQuantity,
      // 调整后库存
      adjustedStock: adjustedStock.value,
      // 调整原因和备注
      reason: formData.reason,
      reasonLabel: getReasonLabel(formData.reason),
      remark: formData.remark.trim(),
      // 操作时间
      operateTime: new Date().toISOString()
    }

    emit('submit', submitData)
    
    // 提交成功后关闭弹窗
    handleClose()
  } finally {
    submitLoading.value = false
  }
}

// 获取原因标签
const getReasonLabel = (value) => {
  for (const group of reasonGroups) {
    const option = group.options.find(opt => opt.value === value)
    if (option) return option.label
  }
  return value
}
</script>

<style lang="scss" scoped>
.inventory-adjust-dialog {
  :deep(.el-dialog__header) {
    margin-right: 0;
    padding: 20px 24px;
    border-bottom: 1px solid #e4e7ed;

    .el-dialog__title {
      font-size: 18px;
      font-weight: 600;
      color: #303133;
    }
  }

  :deep(.el-dialog__body) {
    padding: 24px;
  }

  :deep(.el-dialog__footer) {
    padding: 16px 24px;
    border-top: 1px solid #e4e7ed;
  }
}

.dialog-content {
  .product-info-card {
    background: linear-gradient(135deg, #f5f7fa 0%, #ffffff 100%);
    border-radius: 12px;
    padding: 20px;
    margin-bottom: 24px;
    border: 1px solid #e4e7ed;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);

    .product-header {
      display: flex;
      gap: 16px;
      margin-bottom: 20px;

      .product-image {
        border-radius: 8px;
        border: 2px solid #fff;
        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
      }

      .product-meta {
        flex: 1;
        display: flex;
        flex-direction: column;
        justify-content: center;

        .product-name {
          font-size: 16px;
          font-weight: 600;
          color: #303133;
          margin-bottom: 8px;
          line-height: 1.4;
        }

        .product-sku {
          font-size: 13px;
          color: #909399;
          font-family: 'SF Mono', Monaco, monospace;
        }
      }
    }

    .stock-overview {
      display: flex;
      align-items: center;
      justify-content: space-between;
      background: #fff;
      border-radius: 8px;
      padding: 16px;
      border: 1px solid #ebeef5;

      .stock-item {
        flex: 1;
        text-align: center;

        .stock-value {
          font-size: 28px;
          font-weight: 700;
          margin-bottom: 8px;
          font-family: 'SF Mono', Monaco, monospace;

          &.current {
            color: #409eff;
          }

          &.available {
            color: #67c23a;
          }

          &.reserved {
            color: #e6a23c;
          }
        }

        .stock-label {
          display: flex;
          align-items: center;
          justify-content: center;
          gap: 4px;
          font-size: 13px;
          color: #606266;

          .el-icon {
            font-size: 14px;
          }
        }
      }

      .stock-divider {
        width: 1px;
        height: 40px;
        background: #e4e7ed;
      }
    }
  }

  .adjust-form {
    .el-form-item {
      margin-bottom: 20px;
    }

    :deep(.el-form-item__label) {
      font-weight: 500;
      color: #606266;
      font-size: 14px;
    }

    // 调整类型单选按钮
    :deep(.el-radio-group) {
      display: flex;
      width: 100%;

      .el-radio-button {
        flex: 1;

        .el-radio-button__inner {
          width: 100%;
          display: flex;
          align-items: center;
          justify-content: center;
          gap: 6px;
          font-size: 14px;
          padding: 12px 20px;

          .radio-icon {
            font-size: 16px;
          }
        }

        &.is-active {
          .el-radio-button__inner {
            font-weight: 600;
          }
        }
      }
    }

    // 数量输入
    .quantity-input-wrapper {
      display: flex;
      flex-direction: column;
      gap: 8px;

      .quantity-input {
        width: 100%;

        :deep(.el-input__wrapper) {
          padding-left: 0;
        }

        :deep(.el-input__inner) {
          text-align: center;
          font-size: 18px;
          font-weight: 600;
        }
      }

      .quantity-hint {
        font-size: 12px;
        color: #909399;
        padding-left: 4px;
      }
    }

    // 预览区域
    .preview-section {
      display: flex;
      align-items: center;
      gap: 12px;

      .preview-arrow {
        color: #c0c4cc;
        font-size: 20px;
      }

      .preview-card {
        flex: 1;
        display: flex;
        align-items: center;
        justify-content: space-between;
        padding: 16px 20px;
        border-radius: 8px;
        border: 2px solid;
        transition: all 0.3s ease;

        &.inbound {
          background: linear-gradient(135deg, #f0f9ff 0%, #e6f7ff 100%);
          border-color: #91d5ff;
        }

        &.outbound {
          background: linear-gradient(135deg, #fff7e6 0%, #fff1d6 100%);
          border-color: #ffd591;
        }

        &.adjust {
          background: linear-gradient(135deg, #f6ffed 0%, #e6f7d6 100%);
          border-color: #b7eb8f;
        }

        .preview-value {
          display: flex;
          align-items: center;
          gap: 8px;

          .el-icon {
            font-size: 20px;
            color: #606266;
          }

          .value {
            font-size: 24px;
            font-weight: 700;
            color: #303133;
            font-family: 'SF Mono', Monaco, monospace;
          }

          .unit {
            font-size: 14px;
            color: #909399;
          }
        }

        .preview-change {
          display: flex;
          align-items: center;
          gap: 4px;
          font-size: 14px;
          font-weight: 500;
          padding: 4px 12px;
          border-radius: 20px;

          &.increase {
            color: #67c23a;
            background: rgba(103, 194, 58, 0.1);
          }

          &.decrease {
            color: #f56c6c;
            background: rgba(245, 108, 108, 0.1);
          }

          &.neutral {
            color: #909399;
            background: rgba(144, 147, 153, 0.1);
          }

          .el-icon {
            font-size: 14px;
          }
        }
      }
    }

    // 原因选择器
    :deep(.el-select-dropdown__item) {
      display: flex;
      align-items: center;
      gap: 8px;

      .reason-icon {
        font-size: 16px;
        color: #909399;
      }
    }

    // 操作提示
    .operation-tips {
      margin-top: 8px;

      :deep(.el-alert) {
        padding: 12px 16px;

        .el-alert__title {
          font-weight: 600;
          font-size: 14px;
        }
      }

      .tip-content {
        margin-top: 8px;
        font-size: 13px;
        line-height: 1.6;

        p {
          margin: 0;
          color: #606266;
        }
      }
    }
  }
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;

  .el-button {
    min-width: 100px;

    .el-icon {
      margin-right: 4px;
    }
  }
}
</style>
