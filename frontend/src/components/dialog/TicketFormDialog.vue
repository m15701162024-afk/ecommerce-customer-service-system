<template>
  <el-dialog
    :model-value="visible"
    @update:model-value="handleVisibleChange"
    :title="dialogTitle"
    width="720px"
    :close-on-click-modal="false"
    destroy-on-close
  >
    <el-form
      ref="formRef"
      :model="form"
      :rules="rules"
      label-width="100px"
      class="ticket-form"
    >
      <!-- 基本信息 -->
      <el-divider content-position="left">基本信息</el-divider>
      
      <el-row :gutter="20">
        <el-col :span="24">
          <el-form-item label="工单标题" prop="title">
            <el-input
              v-model="form.title"
              placeholder="请输入工单标题"
              clearable
              maxlength="100"
              show-word-limit
            />
          </el-form-item>
        </el-col>
      </el-row>

      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="工单类型" prop="type">
            <el-select v-model="form.type" placeholder="请选择工单类型" style="width: 100%">
              <el-option
                v-for="item in ticketTypes"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              >
                <el-icon style="margin-right: 8px">
                  <component :is="item.icon" />
                </el-icon>
                {{ item.label }}
              </el-option>
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="优先级" prop="priority">
            <el-select v-model="form.priority" placeholder="请选择优先级" style="width: 100%">
              <el-option
                v-for="item in priorityOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              >
                <el-tag :type="item.tagType" size="small" style="margin-right: 8px">
                  {{ item.label }}
                </el-tag>
              </el-option>
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>

      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="关联订单">
            <el-input
              v-model="form.orderNo"
              placeholder="请输入关联订单号"
              clearable
            >
              <template #prefix>
                <el-icon><Document /></el-icon>
              </template>
              <template #append>
                <el-button @click="handleSearchOrder">
                  <el-icon><Search /></el-icon>
                </el-button>
              </template>
            </el-input>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="工单状态" prop="status" v-if="mode === 'edit'">
            <el-select v-model="form.status" placeholder="请选择状态" style="width: 100%">
              <el-option
                v-for="item in statusOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              >
                <el-tag :type="item.tagType" size="small">
                  {{ item.label }}
                </el-tag>
              </el-option>
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>

      <!-- 客户信息 -->
      <el-divider content-position="left">客户信息</el-divider>
      
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="客户姓名" prop="customerName">
            <el-input
              v-model="form.customerName"
              placeholder="请输入客户姓名"
              clearable
            >
              <template #prefix>
                <el-icon><User /></el-icon>
              </template>
            </el-input>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="联系方式" prop="customerContact">
            <el-input
              v-model="form.customerContact"
              placeholder="手机号/邮箱"
              clearable
            >
              <template #prefix>
                <el-icon><Phone /></el-icon>
              </template>
            </el-input>
          </el-form-item>
        </el-col>
      </el-row>

      <!-- 工单内容 -->
      <el-divider content-position="left">工单内容</el-divider>
      
      <el-form-item label="问题描述" prop="content">
        <el-input
          v-model="form.content"
          type="textarea"
          :rows="5"
          placeholder="请详细描述问题内容..."
          maxlength="2000"
          show-word-limit
        />
      </el-form-item>

      <!-- 处理信息 -->
      <el-divider content-position="left">处理信息</el-divider>
      
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="处理人" prop="assigneeId">
            <el-select
              v-model="form.assigneeId"
              placeholder="请选择处理人"
              style="width: 100%"
              clearable
            >
              <el-option
                v-for="agent in agents"
                :key="agent.id"
                :label="agent.name"
                :value="agent.id"
              >
                <div style="display: flex; align-items: center; gap: 8px">
                  <el-avatar :size="24" :src="agent.avatar">
                    {{ agent.name?.charAt(0) }}
                  </el-avatar>
                  <span>{{ agent.name }}</span>
                  <el-tag v-if="agent.status === 'online'" type="success" size="small">在线</el-tag>
                  <el-tag v-else type="info" size="small">离线</el-tag>
                </div>
              </el-option>
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>

      <el-form-item label="备注" prop="remark">
        <el-input
          v-model="form.remark"
          type="textarea"
          :rows="2"
          placeholder="内部备注信息..."
          maxlength="500"
          show-word-limit
        />
      </el-form-item>
    </el-form>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleCancel">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">
          {{ mode === 'add' ? '创建工单' : '保存修改' }}
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, computed, watch, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import {
  Document,
  Search,
  User,
  Phone,
  QuestionFilled,
  WarningFilled,
  InfoFilled,
  StarFilled
} from '@element-plus/icons-vue'

const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  },
  ticketData: {
    type: Object,
    default: () => ({})
  },
  mode: {
    type: String,
    default: 'add',
    validator: (value) => ['add', 'edit'].includes(value)
  }
})

const emit = defineEmits(['update:visible', 'submit'])

// Refs
const formRef = ref(null)
const submitting = ref(false)

// 客服列表（模拟数据）
const agents = ref([
  { id: 1, name: '张三', avatar: '', status: 'online' },
  { id: 2, name: '李四', avatar: '', status: 'online' },
  { id: 3, name: '王五', avatar: '', status: 'offline' },
  { id: 4, name: '赵六', avatar: '', status: 'online' }
])

// 工单类型选项
const ticketTypes = [
  { value: 'consult', label: '咨询', icon: 'QuestionFilled' },
  { value: 'complaint', label: '投诉', icon: 'WarningFilled' },
  { value: 'suggestion', label: '建议', icon: 'StarFilled' },
  { value: 'aftersale', label: '售后', icon: 'Document' },
  { value: 'other', label: '其他', icon: 'InfoFilled' }
]

// 优先级选项
const priorityOptions = [
  { value: 'low', label: '低', tagType: 'info' },
  { value: 'medium', label: '中', tagType: 'warning' },
  { value: 'high', label: '高', tagType: 'danger' },
  { value: 'urgent', label: '紧急', tagType: 'danger' }
]

// 状态选项
const statusOptions = [
  { value: 'new', label: '新建', tagType: '' },
  { value: 'processing', label: '处理中', tagType: 'warning' },
  { value: 'review', label: '待审核', tagType: 'info' },
  { value: 'resolved', label: '已解决', tagType: 'success' },
  { value: 'closed', label: '已关闭', tagType: 'info' }
]

// 表单数据
const form = reactive({
  title: '',
  type: '',
  priority: 'medium',
  orderNo: '',
  customerName: '',
  customerContact: '',
  content: '',
  assigneeId: '',
  status: 'new',
  remark: ''
})

// 计算属性：对话框标题
const dialogTitle = computed(() => {
  return props.mode === 'add' ? '创建工单' : '编辑工单'
})

// 表单验证规则
const rules = {
  title: [
    { required: true, message: '请输入工单标题', trigger: 'blur' },
    { min: 2, max: 100, message: '长度在 2 到 100 个字符', trigger: 'blur' }
  ],
  type: [
    { required: true, message: '请选择工单类型', trigger: 'change' }
  ],
  priority: [
    { required: true, message: '请选择优先级', trigger: 'change' }
  ],
  customerName: [
    { required: true, message: '请输入客户姓名', trigger: 'blur' }
  ],
  customerContact: [
    { required: true, message: '请输入联系方式', trigger: 'blur' },
    { 
      validator: (rule, value, callback) => {
        if (!value) {
          callback()
          return
        }
        // 验证手机号或邮箱
        const phoneRegex = /^1[3-9]\d{9}$/
        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
        if (phoneRegex.test(value) || emailRegex.test(value)) {
          callback()
        } else {
          callback(new Error('请输入有效的手机号或邮箱'))
        }
      },
      trigger: 'blur'
    }
  ],
  content: [
    { required: true, message: '请描述工单内容', trigger: 'blur' },
    { min: 10, max: 2000, message: '内容长度在 10 到 2000 个字符', trigger: 'blur' }
  ],
  assigneeId: [
    { required: false, message: '请选择处理人', trigger: 'change' }
  ],
  status: [
    { required: true, message: '请选择工单状态', trigger: 'change' }
  ]
}

// 监听 visible 变化，打开时重置或填充表单
watch(
  () => props.visible,
  (newVal) => {
    if (newVal) {
      if (props.mode === 'edit' && props.ticketData) {
        // 编辑模式：填充表单数据
        Object.assign(form, {
          title: props.ticketData.title || '',
          type: props.ticketData.type || '',
          priority: props.ticketData.priority || 'medium',
          orderNo: props.ticketData.orderNo || '',
          customerName: props.ticketData.customerName || '',
          customerContact: props.ticketData.customerContact || '',
          content: props.ticketData.content || '',
          assigneeId: props.ticketData.assigneeId || '',
          status: props.ticketData.status || 'new',
          remark: props.ticketData.remark || ''
        })
      } else {
        // 新增模式：重置表单
        resetForm()
      }
    }
  },
  { immediate: true }
)

// 重置表单
const resetForm = () => {
  Object.assign(form, {
    title: '',
    type: '',
    priority: 'medium',
    orderNo: '',
    customerName: '',
    customerContact: '',
    content: '',
    assigneeId: '',
    status: 'new',
    remark: ''
  })
  // 重置表单验证
  formRef.value?.resetFields()
}

// 处理 visible 变化
const handleVisibleChange = (val) => {
  emit('update:visible', val)
}

// 取消
const handleCancel = () => {
  emit('update:visible', false)
  resetForm()
}

// 搜索订单
const handleSearchOrder = () => {
  if (!form.orderNo) {
    ElMessage.warning('请输入订单号')
    return
  }
  // 模拟订单搜索
  ElMessage.info(`正在搜索订单: ${form.orderNo}`)
  // 实际项目中这里调用 API 获取订单信息
  // getOrderInfo(form.orderNo).then(res => { ... })
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return

  try {
    await formRef.value.validate()
    submitting.value = true

    // 构建提交数据
    const submitData = {
      ...form,
      mode: props.mode
    }

    // 如果是新增，移除 status 字段（后端默认新建）
    if (props.mode === 'add') {
      delete submitData.status
    }

    // 发射提交事件
    emit('submit', submitData)

    // 成功提示
    ElMessage.success(props.mode === 'add' ? '工单创建成功' : '工单更新成功')
    
    // 关闭对话框
    emit('update:visible', false)
    resetForm()
  } catch (error) {
    console.error('表单验证失败:', error)
    ElMessage.error('请检查表单填写是否正确')
  } finally {
    submitting.value = false
  }
}

// 暴露方法给父组件
defineExpose({
  resetForm,
  formRef
})
</script>

<style scoped>
.ticket-form {
  padding: 0 10px;
}

.ticket-form :deep(.el-divider__text) {
  font-size: 14px;
  font-weight: 500;
  color: #606266;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

/* 调整表单间距 */
.ticket-form :deep(.el-form-item) {
  margin-bottom: 18px;
}

/* 调整分割线间距 */
.ticket-form :deep(.el-divider) {
  margin: 24px 0 20px 0;
}

/* 选项样式优化 */
:deep(.el-select-dropdown__item) {
  display: flex;
  align-items: center;
}
</style>
