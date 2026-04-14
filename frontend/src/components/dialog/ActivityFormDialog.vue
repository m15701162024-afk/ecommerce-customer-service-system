<template>
  <el-dialog
    :model-value="visible"
    @update:model-value="handleVisibleChange"
    :title="dialogTitle"
    width="700px"
    :close-on-click-modal="false"
    @close="handleClose"
    class="activity-form-dialog"
  >
    <el-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      label-width="100px"
      class="activity-form"
    >
      <!-- 活动名称 -->
      <el-form-item label="活动名称" prop="name">
        <el-input
          v-model="formData.name"
          placeholder="请输入活动名称"
          maxlength="50"
          show-word-limit
          clearable
        />
      </el-form-item>

      <!-- 活动类型 -->
      <el-form-item label="活动类型" prop="type">
        <el-select
          v-model="formData.type"
          placeholder="请选择活动类型"
          clearable
          @change="handleTypeChange"
          style="width: 100%"
        >
          <el-option
            v-for="item in activityTypes"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
      </el-form-item>

      <!-- 活动时间范围 -->
      <el-form-item label="活动时间" prop="timeRange">
        <el-date-picker
          v-model="formData.timeRange"
          type="datetimerange"
          range-separator="至"
          start-placeholder="开始时间"
          end-placeholder="结束时间"
          format="YYYY-MM-DD HH:mm"
          value-format="YYYY-MM-DD HH:mm:ss"
          :default-time="defaultTimeRange"
          style="width: 100%"
        />
      </el-form-item>

      <!-- 活动规则 - 动态配置 -->
      <el-form-item label="活动规则" prop="rules" class="rules-section">
        <div class="rules-container">
          <!-- 限时折扣 -->
          <template v-if="formData.type === 'discount'">
            <el-row :gutter="16">
              <el-col :span="12">
                <el-form-item label="折扣比例" prop="rules.discountRate" label-width="80px">
                  <el-input-number
                    v-model="formData.rules.discountRate"
                    :min="1"
                    :max="99"
                    :precision="0"
                    controls-position="right"
                    style="width: 100%"
                  >
                    <template #append>%</template>
                  </el-input-number>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="限购数量" prop="rules.limitPerUser" label-width="80px">
                  <el-input-number
                    v-model="formData.rules.limitPerUser"
                    :min="1"
                    :max="999"
                    :precision="0"
                    controls-position="right"
                    style="width: 100%"
                    placeholder="每人限购"
                  />
                </el-form-item>
              </el-col>
            </el-row>
          </template>

          <!-- 满减优惠 -->
          <template v-if="formData.type === 'fullReduction'">
            <el-row :gutter="16" align="middle" class="full-reduction-row">
              <el-col :span="10">
                <el-form-item label="满" prop="rules.fullAmount" label-width="40px" class="inline-form-item">
                  <el-input-number
                    v-model="formData.rules.fullAmount"
                    :min="1"
                    :precision="2"
                    controls-position="right"
                    style="width: 100%"
                  >
                    <template #append>元</template>
                  </el-input-number>
                </el-form-item>
              </el-col>
              <el-col :span="4" class="text-center">
                <span class="reduction-text">减</span>
              </el-col>
              <el-col :span="10">
                <el-form-item prop="rules.reduceAmount" label-width="0" class="inline-form-item">
                  <el-input-number
                    v-model="formData.rules.reduceAmount"
                    :min="0.01"
                    :precision="2"
                    controls-position="right"
                    style="width: 100%"
                  >
                    <template #append>元</template>
                  </el-input-number>
                </el-form-item>
              </el-col>
            </el-row>
          </template>

          <!-- 买赠活动 -->
          <template v-if="formData.type === 'buyAndGive'">
            <el-row :gutter="16" align="middle" class="buy-give-row">
              <el-col :span="10">
                <el-form-item label="买" prop="rules.buyQuantity" label-width="40px" class="inline-form-item">
                  <el-input-number
                    v-model="formData.rules.buyQuantity"
                    :min="1"
                    :precision="0"
                    controls-position="right"
                    style="width: 100%"
                  >
                    <template #append>件</template>
                  </el-input-number>
                </el-form-item>
              </el-col>
              <el-col :span="4" class="text-center">
                <span class="give-text">赠</span>
              </el-col>
              <el-col :span="10">
                <el-form-item prop="rules.giveQuantity" label-width="0" class="inline-form-item">
                  <el-input-number
                    v-model="formData.rules.giveQuantity"
                    :min="1"
                    :precision="0"
                    controls-position="right"
                    style="width: 100%"
                  >
                    <template #append>件</template>
                  </el-input-number>
                </el-form-item>
              </el-col>
            </el-row>
          </template>

          <!-- 秒杀活动 -->
          <template v-if="formData.type === 'flashSale'">
            <el-row :gutter="16">
              <el-col :span="12">
                <el-form-item label="秒杀价" prop="rules.flashPrice" label-width="70px">
                  <el-input-number
                    v-model="formData.rules.flashPrice"
                    :min="0.01"
                    :precision="2"
                    controls-position="right"
                    style="width: 100%"
                  >
                    <template #append>元</template>
                  </el-input-number>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="库存限制" prop="rules.stockLimit" label-width="70px">
                  <el-input-number
                    v-model="formData.rules.stockLimit"
                    :min="1"
                    :max="99999"
                    :precision="0"
                    controls-position="right"
                    style="width: 100%"
                    placeholder="秒杀库存"
                  />
                </el-form-item>
              </el-col>
            </el-row>
          </template>

          <!-- 未选择活动类型时 -->
          <el-empty v-if="!formData.type" description="请先选择活动类型" :image-size="60" />
        </div>
      </el-form-item>

      <!-- 关联商品 -->
      <el-form-item label="关联商品" prop="productIds">
        <div class="product-select-section">
          <div class="product-select-header">
            <el-button type="primary" link @click="showProductSelector = true">
              <el-icon><Plus /></el-icon>
              选择商品
            </el-button>
            <span class="product-count" v-if="selectedProducts.length > 0">
              已选择 {{ selectedProducts.length }} 个商品
            </span>
          </div>
          <div class="selected-products" v-if="selectedProducts.length > 0">
            <el-tag
              v-for="product in selectedProducts"
              :key="product.id"
              closable
              @close="removeProduct(product.id)"
              class="product-tag"
            >
              {{ product.name }}
            </el-tag>
          </div>
          <el-empty v-else description="暂未选择商品" :image-size="60" />
        </div>
      </el-form-item>

      <!-- 活动状态 -->
      <el-form-item label="活动状态" prop="status">
        <el-switch
          v-model="formData.status"
          :active-value="1"
          :inactive-value="0"
          active-text="启用"
          inactive-text="禁用"
          inline-prompt
        />
      </el-form-item>

      <!-- 活动描述 -->
      <el-form-item label="活动描述" prop="description">
        <el-input
          v-model="formData.description"
          type="textarea"
          :rows="3"
          placeholder="请输入活动描述（选填）"
          maxlength="500"
          show-word-limit
          resize="none"
        />
      </el-form-item>
    </el-form>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleClose">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitLoading">
          确定
        </el-button>
      </div>
    </template>

    <!-- 商品选择器弹窗 -->
    <ProductSelector
      v-model:visible="showProductSelector"
      v-model:selected="formData.productIds"
      @confirm="handleProductSelect"
    />
  </el-dialog>
</template>

<script setup>
import { ref, reactive, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import ProductSelector from './ProductSelector.vue'

const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  },
  activityData: {
    type: Object,
    default: () => null
  },
  mode: {
    type: String,
    default: 'add',
    validator: (value) => ['add', 'edit'].includes(value)
  }
})

const emit = defineEmits(['update:visible', 'submit'])

// 表单引用
const formRef = ref(null)
const submitLoading = ref(false)
const showProductSelector = ref(false)

// 活动类型选项
const activityTypes = [
  { label: '限时折扣', value: 'discount' },
  { label: '满减优惠', value: 'fullReduction' },
  { label: '买赠活动', value: 'buyAndGive' },
  { label: '秒杀活动', value: 'flashSale' }
]

// 默认时间范围（当天的0点和23:59:59）
const defaultTimeRange = [
  new Date(2000, 0, 1, 0, 0, 0),
  new Date(2000, 0, 1, 23, 59, 59)
]

// 表单数据
const formData = reactive({
  name: '',
  type: '',
  timeRange: [],
  rules: {
    // 限时折扣
    discountRate: 10,
    limitPerUser: 1,
    // 满减优惠
    fullAmount: undefined,
    reduceAmount: undefined,
    // 买赠活动
    buyQuantity: 1,
    giveQuantity: 1,
    // 秒杀活动
    flashPrice: undefined,
    stockLimit: undefined
  },
  productIds: [],
  status: 1,
  description: ''
})

// 已选择商品（模拟数据）
const selectedProducts = ref([])

// 模拟商品列表
const mockProducts = [
  { id: 1, name: 'iPhone 15 Pro', price: 7999, image: '' },
  { id: 2, name: 'MacBook Air M3', price: 8999, image: '' },
  { id: 3, name: 'AirPods Pro 2', price: 1899, image: '' },
  { id: 4, name: 'iPad Pro 12.9', price: 8999, image: '' },
  { id: 5, name: 'Apple Watch Ultra', price: 6299, image: '' }
]

// 对话框标题
const dialogTitle = computed(() => {
  return props.mode === 'add' ? '新增活动' : '编辑活动'
})

// 动态规则验证
const validateRules = (rule, value, callback) => {
  if (!formData.type) {
    callback(new Error('请先选择活动类型'))
    return
  }

  const type = formData.type
  const rules = formData.rules

  if (type === 'discount') {
    if (!rules.discountRate || rules.discountRate < 1 || rules.discountRate > 99) {
      callback(new Error('折扣比例必须在1-99之间'))
      return
    }
    if (!rules.limitPerUser || rules.limitPerUser < 1) {
      callback(new Error('限购数量必须大于0'))
      return
    }
  } else if (type === 'fullReduction') {
    if (!rules.fullAmount || rules.fullAmount <= 0) {
      callback(new Error('满减金额必须大于0'))
      return
    }
    if (!rules.reduceAmount || rules.reduceAmount <= 0) {
      callback(new Error('减免金额必须大于0'))
      return
    }
    if (rules.reduceAmount >= rules.fullAmount) {
      callback(new Error('减免金额必须小于满减金额'))
      return
    }
  } else if (type === 'buyAndGive') {
    if (!rules.buyQuantity || rules.buyQuantity < 1) {
      callback(new Error('购买数量必须大于0'))
      return
    }
    if (!rules.giveQuantity || rules.giveQuantity < 1) {
      callback(new Error('赠送数量必须大于0'))
      return
    }
  } else if (type === 'flashSale') {
    if (!rules.flashPrice || rules.flashPrice <= 0) {
      callback(new Error('秒杀价必须大于0'))
      return
    }
    if (!rules.stockLimit || rules.stockLimit < 1) {
      callback(new Error('库存限制必须大于0'))
      return
    }
  }

  callback()
}

// 表单验证规则
const formRules = {
  name: [
    { required: true, message: '请输入活动名称', trigger: 'blur' },
    { min: 2, max: 50, message: '活动名称长度2-50字符', trigger: 'blur' }
  ],
  type: [
    { required: true, message: '请选择活动类型', trigger: 'change' }
  ],
  timeRange: [
    { required: true, message: '请选择活动时间', trigger: 'change' },
    {
      validator: (rule, value, callback) => {
        if (!value || value.length !== 2) {
          callback(new Error('请选择完整的活动时间范围'))
        } else if (new Date(value[0]) >= new Date(value[1])) {
          callback(new Error('结束时间必须晚于开始时间'))
        } else {
          callback()
        }
      },
      trigger: 'change'
    }
  ],
  rules: [
    { required: true, validator: validateRules, trigger: 'change' }
  ],
  productIds: [
    {
      validator: (rule, value, callback) => {
        if (!value || value.length === 0) {
          callback(new Error('请至少选择一个关联商品'))
        } else {
          callback()
        }
      },
      trigger: 'change'
    }
  ]
}

// 监听 visible 变化
watch(() => props.visible, (val) => {
  if (val) {
    // 打开弹窗时，如果有数据则初始化表单
    if (props.activityData && props.mode === 'edit') {
      initFormData(props.activityData)
    } else {
      resetForm()
    }
  }
})

// 监听商品ID变化，更新已选商品列表
watch(() => formData.productIds, (ids) => {
  selectedProducts.value = mockProducts.filter(p => ids.includes(p.id))
}, { immediate: true })

// 初始化表单数据
const initFormData = (data) => {
  formData.name = data.name || ''
  formData.type = data.type || ''
  formData.timeRange = data.timeRange || []
  
  // 合并规则数据
  Object.assign(formData.rules, {
    discountRate: 10,
    limitPerUser: 1,
    fullAmount: undefined,
    reduceAmount: undefined,
    buyQuantity: 1,
    giveQuantity: 1,
    flashPrice: undefined,
    stockLimit: undefined,
    ...data.rules
  })
  
  formData.productIds = data.productIds || []
  formData.status = data.status !== undefined ? data.status : 1
  formData.description = data.description || ''
}

// 重置表单
const resetForm = () => {
  formData.name = ''
  formData.type = ''
  formData.timeRange = []
  formData.rules = {
    discountRate: 10,
    limitPerUser: 1,
    fullAmount: undefined,
    reduceAmount: undefined,
    buyQuantity: 1,
    giveQuantity: 1,
    flashPrice: undefined,
    stockLimit: undefined
  }
  formData.productIds = []
  formData.status = 1
  formData.description = ''
  
  selectedProducts.value = []
}

// 处理活动类型变化
const handleTypeChange = () => {
  // 清空规则相关的验证
  formRef.value?.clearValidate('rules')
}

// 处理商品选择
const handleProductSelect = (selectedIds) => {
  formData.productIds = selectedIds
  // 手动触发验证
  formRef.value?.validateField('productIds')
}

// 移除商品
const removeProduct = (productId) => {
  const index = formData.productIds.indexOf(productId)
  if (index > -1) {
    formData.productIds.splice(index, 1)
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

  submitLoading.value = true

  try {
    // 构建提交数据
    const submitData = {
      name: formData.name,
      type: formData.type,
      startTime: formData.timeRange[0],
      endTime: formData.timeRange[1],
      rules: getRulesByType(formData.type),
      productIds: formData.productIds,
      status: formData.status,
      description: formData.description
    }

    // 如果是编辑模式，添加ID
    if (props.mode === 'edit' && props.activityData?.id) {
      submitData.id = props.activityData.id
    }

    emit('submit', submitData, props.mode)
  } finally {
    submitLoading.value = false
  }
}

// 根据活动类型获取对应的规则
const getRulesByType = (type) => {
  const rules = formData.rules
  switch (type) {
    case 'discount':
      return {
        discountRate: rules.discountRate,
        limitPerUser: rules.limitPerUser
      }
    case 'fullReduction':
      return {
        fullAmount: rules.fullAmount,
        reduceAmount: rules.reduceAmount
      }
    case 'buyAndGive':
      return {
        buyQuantity: rules.buyQuantity,
        giveQuantity: rules.giveQuantity
      }
    case 'flashSale':
      return {
        flashPrice: rules.flashPrice,
        stockLimit: rules.stockLimit
      }
    default:
      return {}
  }
}
</script>

<style lang="scss" scoped>
.activity-form-dialog {
  :deep(.el-dialog__body) {
    padding: 20px 24px;
  }
}

.activity-form {
  .rules-section {
    margin-bottom: 24px;
  }

  .rules-container {
    padding: 16px;
    background-color: #f5f7fa;
    border-radius: 8px;
    border: 1px solid #e4e7ed;

    .full-reduction-row,
    .buy-give-row {
      .reduction-text,
      .give-text {
        font-size: 16px;
        font-weight: 500;
        color: #f56c6c;
      }
    }

    .text-center {
      text-align: center;
    }

    :deep(.inline-form-item) {
      margin-bottom: 0;
    }
  }

  .product-select-section {
    .product-select-header {
      display: flex;
      align-items: center;
      gap: 12px;
      margin-bottom: 12px;

      .product-count {
        font-size: 13px;
        color: #909399;
      }
    }

    .selected-products {
      display: flex;
      flex-wrap: wrap;
      gap: 8px;
      padding: 12px;
      background-color: #f5f7fa;
      border-radius: 8px;
      min-height: 60px;

      .product-tag {
        max-width: 200px;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }
    }
  }

  :deep(.el-form-item__label) {
    font-weight: 500;
    color: #606266;
  }

  :deep(.el-input-number) {
    .el-input__wrapper {
      padding-left: 0;
    }
  }
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}
</style>
