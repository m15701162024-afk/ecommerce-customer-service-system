<template>
  <el-dialog
    v-model="dialogVisible"
    :title="dialogTitle"
    width="700px"
    :close-on-click-modal="false"
    destroy-on-close
    class="coupon-form-dialog"
    @close="handleClose"
  >
    <el-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      label-width="120px"
      class="coupon-form"
    >
      <!-- 基本信息 -->
      <div class="form-section">
        <div class="section-title">基本信息</div>
        
        <el-form-item label="优惠券名称" prop="name">
          <el-input
            v-model="formData.name"
            placeholder="请输入优惠券名称"
            maxlength="50"
            show-word-limit
          />
        </el-form-item>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="优惠券类型" prop="type">
              <el-select
                v-model="formData.type"
                placeholder="请选择类型"
                style="width: 100%"
                @change="handleTypeChange"
              >
                <el-option
                  v-for="item in couponTypeOptions"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="优惠券状态" prop="status">
              <el-radio-group v-model="formData.status">
                <el-radio :label="1">启用</el-radio>
                <el-radio :label="0">禁用</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>

        <!-- 优惠面值 - 动态显示 -->
        <el-form-item :label="valueLabel" prop="value" v-if="formData.type">
          <el-input-number
            v-model="formData.value"
            :min="valueMin"
            :max="valueMax"
            :precision="valuePrecision"
            :step="valueStep"
            style="width: 200px"
            :placeholder="valuePlaceholder"
          />
          <span class="unit-text">{{ valueUnit }}</span>
          <span class="hint-text" v-if="formData.type === 'discount'">（例如：0.85 表示 8.5 折）</span>
        </el-form-item>
      </div>

      <!-- 使用规则 -->
      <div class="form-section">
        <div class="section-title">使用规则</div>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="使用门槛" prop="minOrderAmount">
              <el-input-number
                v-model="formData.minOrderAmount"
                :min="0"
                :precision="2"
                :step="1"
                style="width: 150px"
                placeholder="满多少可用"
              />
              <span class="unit-text">元</span>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="适用范围" prop="scope">
              <el-select
                v-model="formData.scope"
                placeholder="请选择适用范围"
                style="width: 100%"
              >
                <el-option label="全场通用" value="all" />
                <el-option label="指定商品" value="product" />
                <el-option label="指定分类" value="category" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
      </div>

      <!-- 发放规则 -->
      <div class="form-section">
        <div class="section-title">发放规则</div>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="发放数量" prop="totalQuantity">
              <el-input-number
                v-model="formData.totalQuantity"
                :min="1"
                :max="999999"
                :step="1"
                style="width: 150px"
                placeholder="总数量"
              />
              <span class="unit-text">张</span>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="每人限领" prop="limitPerUser">
              <el-input-number
                v-model="formData.limitPerUser"
                :min="1"
                :max="999"
                :step="1"
                style="width: 150px"
                placeholder="每人限领"
              />
              <span class="unit-text">张</span>
            </el-form-item>
          </el-col>
        </el-row>
      </div>

      <!-- 有效期设置 -->
      <div class="form-section">
        <div class="section-title">有效期设置</div>
        
        <el-form-item label="有效期类型" prop="validityType">
          <el-radio-group v-model="formData.validityType" @change="handleValidityTypeChange">
            <el-radio label="fixed">固定时间</el-radio>
            <el-radio label="relative">相对时间</el-radio>
          </el-radio-group>
        </el-form-item>

        <!-- 固定时间 -->
        <el-form-item
          v-if="formData.validityType === 'fixed'"
          label="有效时间"
          prop="validityTimeRange"
        >
          <el-date-picker
            v-model="formData.validityTimeRange"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            value-format="YYYY-MM-DD HH:mm:ss"
            style="width: 100%"
          />
        </el-form-item>

        <!-- 相对时间 -->
        <el-form-item
          v-if="formData.validityType === 'relative'"
          label="领取后有效"
          prop="validityDays"
        >
          <el-input-number
            v-model="formData.validityDays"
            :min="1"
            :max="365"
            :step="1"
            style="width: 150px"
            placeholder="天数"
          />
          <span class="unit-text">天</span>
        </el-form-item>
      </div>

      <!-- 使用说明 -->
      <div class="form-section">
        <div class="section-title">其他设置</div>
        
        <el-form-item label="使用说明" prop="description">
          <el-input
            v-model="formData.description"
            type="textarea"
            :rows="4"
            placeholder="请输入优惠券使用说明，如：不可与其他优惠叠加使用等"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
      </div>
    </el-form>

    <template #footer>
      <span class="dialog-footer">
        <el-button @click="handleClose" :disabled="submitting">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">
          {{ submitButtonText }}
        </el-button>
      </span>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, computed, watch } from 'vue'

const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  },
  couponData: {
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

// 优惠券类型选项
const couponTypeOptions = [
  { value: 'amount', label: '满减券', unit: '元', placeholder: '减免金额', min: 0.01, max: 99999, precision: 2, step: 1 },
  { value: 'discount', label: '折扣券', unit: '折', placeholder: '折扣比例', min: 0.01, max: 1, precision: 2, step: 0.05 },
  { value: 'cash', label: '现金券', unit: '元', placeholder: '现金金额', min: 0.01, max: 99999, precision: 2, step: 1 },
  { value: 'exchange', label: '兑换券', unit: '', placeholder: '兑换商品', min: 1, max: 99999, precision: 0, step: 1 }
]

// 弹窗可见性
const dialogVisible = computed({
  get: () => props.visible,
  set: (val) => emit('update:visible', val)
})

// 弹窗标题
const dialogTitle = computed(() => {
  return props.mode === 'add' ? '新增优惠券' : '编辑优惠券'
})

// 提交按钮文本
const submitButtonText = computed(() => {
  return props.mode === 'add' ? '创建' : '保存'
})

// 表单引用
const formRef = ref(null)

// 提交状态
const submitting = ref(false)

// 表单数据
const formData = reactive({
  name: '',
  type: '',
  value: undefined,
  minOrderAmount: 0,
  totalQuantity: 100,
  limitPerUser: 1,
  validityType: 'fixed',
  validityTimeRange: [],
  validityDays: 7,
  scope: 'all',
  status: 1,
  description: ''
})

// 动态计算优惠面值的标签
const valueLabel = computed(() => {
  const typeMap = {
    amount: '减免金额',
    discount: '折扣比例',
    cash: '现金金额',
    exchange: '兑换商品'
  }
  return typeMap[formData.type] || '优惠面值'
})

// 动态计算优惠面值的单位
const valueUnit = computed(() => {
  const option = couponTypeOptions.find(item => item.value === formData.type)
  return option?.unit || ''
})

// 动态计算优惠面值的 placeholder
const valuePlaceholder = computed(() => {
  const option = couponTypeOptions.find(item => item.value === formData.type)
  return option?.placeholder || '请输入'
})

// 动态计算输入限制
const valueMin = computed(() => {
  const option = couponTypeOptions.find(item => item.value === formData.type)
  return option?.min || 0
})

const valueMax = computed(() => {
  const option = couponTypeOptions.find(item => item.value === formData.type)
  return option?.max || 999999
})

const valuePrecision = computed(() => {
  const option = couponTypeOptions.find(item => item.value === formData.type)
  return option?.precision ?? 2
})

const valueStep = computed(() => {
  const option = couponTypeOptions.find(item => item.value === formData.type)
  return option?.step || 1
})

// 表单验证规则
const formRules = {
  name: [
    { required: true, message: '请输入优惠券名称', trigger: 'blur' },
    { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  type: [
    { required: true, message: '请选择优惠券类型', trigger: 'change' }
  ],
  value: [
    { required: true, message: '请输入优惠面值', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value === undefined || value === null) {
          callback(new Error('请输入优惠面值'))
        } else if (formData.type === 'discount' && (value <= 0 || value > 1)) {
          callback(new Error('折扣比例应在 0.01 到 1 之间'))
        } else if (value <= 0) {
          callback(new Error('优惠面值必须大于 0'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ],
  minOrderAmount: [
    { required: true, message: '请输入使用门槛', trigger: 'blur' }
  ],
  totalQuantity: [
    { required: true, message: '请输入发放数量', trigger: 'blur' }
  ],
  limitPerUser: [
    { required: true, message: '请输入每人限领数量', trigger: 'blur' }
  ],
  validityType: [
    { required: true, message: '请选择有效期类型', trigger: 'change' }
  ],
  validityTimeRange: [
    {
      required: true,
      validator: (rule, value, callback) => {
        if (formData.validityType === 'fixed') {
          if (!value || value.length !== 2 || !value[0] || !value[1]) {
            callback(new Error('请选择有效时间范围'))
          } else {
            callback()
          }
        } else {
          callback()
        }
      },
      trigger: 'change'
    }
  ],
  validityDays: [
    {
      required: true,
      validator: (rule, value, callback) => {
        if (formData.validityType === 'relative') {
          if (!value || value <= 0) {
            callback(new Error('请输入有效天数'))
          } else {
            callback()
          }
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ],
  scope: [
    { required: true, message: '请选择适用范围', trigger: 'change' }
  ],
  status: [
    { required: true, message: '请选择状态', trigger: 'change' }
  ]
}

// 监听 couponData 变化，用于编辑模式
watch(
  () => props.couponData,
  (newVal) => {
    if (newVal && props.mode === 'edit') {
      // 编辑模式：填充数据
      Object.assign(formData, {
        name: newVal.name || '',
        type: newVal.type || '',
        value: newVal.value,
        minOrderAmount: newVal.minOrderAmount || 0,
        totalQuantity: newVal.totalQuantity || 100,
        limitPerUser: newVal.limitPerUser || 1,
        validityType: newVal.validityType || 'fixed',
        validityTimeRange: newVal.validityType === 'fixed' 
          ? [newVal.startTime, newVal.endTime] 
          : [],
        validityDays: newVal.validityDays || 7,
        scope: newVal.scope || 'all',
        status: newVal.status ?? 1,
        description: newVal.description || ''
      })
    } else {
      // 新增模式：重置表单
      resetForm()
    }
  },
  { immediate: true, deep: true }
)

// 处理优惠券类型变化
const handleTypeChange = () => {
  formData.value = undefined
}

// 处理有效期类型变化
const handleValidityTypeChange = () => {
  if (formData.validityType === 'fixed') {
    formData.validityTimeRange = []
    formData.validityDays = 7
  } else {
    formData.validityTimeRange = []
    formData.validityDays = 7
  }
}

// 重置表单
const resetForm = () => {
  Object.assign(formData, {
    name: '',
    type: '',
    value: undefined,
    minOrderAmount: 0,
    totalQuantity: 100,
    limitPerUser: 1,
    validityType: 'fixed',
    validityTimeRange: [],
    validityDays: 7,
    scope: 'all',
    status: 1,
    description: ''
  })
  formRef.value?.resetFields()
}

// 处理关闭
const handleClose = () => {
  resetForm()
  dialogVisible.value = false
}

// 处理提交
const handleSubmit = async () => {
  try {
    await formRef.value.validate()
  } catch {
    return
  }

  submitting.value = true

  try {
    // 构造提交数据
    const submitData = {
      name: formData.name,
      type: formData.type,
      value: formData.value,
      minOrderAmount: formData.minOrderAmount,
      totalQuantity: formData.totalQuantity,
      limitPerUser: formData.limitPerUser,
      validityType: formData.validityType,
      scope: formData.scope,
      status: formData.status,
      description: formData.description
    }

    if (formData.validityType === 'fixed') {
      submitData.startTime = formData.validityTimeRange[0]
      submitData.endTime = formData.validityTimeRange[1]
    } else {
      submitData.validityDays = formData.validityDays
    }

    // 编辑模式添加 id
    if (props.mode === 'edit' && props.couponData?.id) {
      submitData.id = props.couponData.id
    }

    emit('submit', submitData)
  } finally {
    submitting.value = false
  }
}

// 暴露方法给父组件
defineExpose({
  resetForm,
  getFormData: () => ({ ...formData })
})
</script>

<style lang="scss" scoped>
.coupon-form-dialog {
  :deep(.el-dialog__body) {
    padding: 20px 30px 10px;
    max-height: 65vh;
    overflow-y: auto;
  }

  :deep(.el-dialog__footer) {
    padding: 15px 30px 20px;
  }
}

.coupon-form {
  .form-section {
    margin-bottom: 24px;

    &:last-child {
      margin-bottom: 0;
    }

    .section-title {
      font-size: 14px;
      font-weight: 600;
      color: #303133;
      margin-bottom: 16px;
      padding-left: 10px;
      border-left: 3px solid #409eff;
    }
  }

  .unit-text {
    margin-left: 8px;
    color: #606266;
    font-size: 14px;
  }

  .hint-text {
    margin-left: 8px;
    color: #909399;
    font-size: 12px;
  }

  :deep(.el-form-item) {
    margin-bottom: 18px;
  }

  :deep(.el-input__wrapper) {
    box-shadow: 0 0 0 1px #dcdfe6 inset;

    &:hover {
      box-shadow: 0 0 0 1px #c0c4cc inset;
    }

    &.is-focus {
      box-shadow: 0 0 0 1px #409eff inset;
    }
  }

  :deep(.el-radio-group) {
    .el-radio {
      margin-right: 20px;
    }
  }
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}
</style>
