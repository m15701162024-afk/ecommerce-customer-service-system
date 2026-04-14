<template>
  <el-dialog
    :model-value="visible"
    :title="dialogTitle"
    width="750px"
    :close-on-click-modal="false"
    :destroy-on-close="true"
    @close="handleClose"
    class="shop-form-dialog"
  >
    <el-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      label-width="110px"
      class="shop-form"
    >
      <!-- 基本信息卡片 -->
      <el-card class="form-section" shadow="never">
        <template #header>
          <div class="section-header">
            <el-icon><Shop /></el-icon>
            <span>基本信息</span>
          </div>
        </template>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="店铺名称" prop="name">
              <el-input
                v-model="formData.name"
                placeholder="请输入店铺名称"
                maxlength="50"
                show-word-limit
                clearable
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="所属平台" prop="platform">
              <el-select
                v-model="formData.platform"
                placeholder="请选择平台"
                clearable
                style="width: 100%"
                @change="handlePlatformChange"
              >
                <el-option label="抖音" value="douyin">
                  <div class="platform-option">
                    <el-avatar :size="20" class="platform-icon douyin">抖</el-avatar>
                    <span>抖音</span>
                  </div>
                </el-option>
                <el-option label="淘宝" value="taobao">
                  <div class="platform-option">
                    <el-avatar :size="20" class="platform-icon taobao">淘</el-avatar>
                    <span>淘宝</span>
                  </div>
                </el-option>
                <el-option label="小红书" value="xiaohongshu">
                  <div class="platform-option">
                    <el-avatar :size="20" class="platform-icon xiaohongshu">书</el-avatar>
                    <span>小红书</span>
                  </div>
                </el-option>
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="店铺类型" prop="shopType">
              <el-select
                v-model="formData.shopType"
                placeholder="请选择店铺类型"
                clearable
                style="width: 100%"
              >
                <el-option
                  v-for="item in shopTypeOptions"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="店铺状态" prop="status">
              <el-radio-group v-model="formData.status">
                <el-radio label="enabled">
                  <el-tag type="success" size="small">启用</el-tag>
                </el-radio>
                <el-radio label="disabled">
                  <el-tag type="danger" size="small">禁用</el-tag>
                </el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="店铺Logo" prop="logo">
          <el-upload
            class="logo-uploader"
            :action="uploadAction"
            :headers="uploadHeaders"
            :show-file-list="false"
            :before-upload="beforeLogoUpload"
            :on-success="handleLogoSuccess"
            :on-error="handleUploadError"
            accept="image/*"
          >
            <div v-if="formData.logo" class="logo-preview-wrapper">
              <el-image
                :src="formData.logo"
                fit="cover"
                class="uploaded-logo"
              />
              <div class="logo-actions">
                <el-button type="danger" circle size="small" @click.stop="removeLogo">
                  <el-icon><Delete /></el-icon>
                </el-button>
              </div>
            </div>
            <div v-else class="logo-upload-placeholder">
              <el-icon class="upload-icon"><Plus /></el-icon>
              <div class="upload-text">点击上传Logo</div>
              <div class="upload-hint">建议尺寸 200x200</div>
            </div>
          </el-upload>
        </el-form-item>

        <el-form-item label="店铺简介" prop="description">
          <el-input
            v-model="formData.description"
            type="textarea"
            :rows="3"
            placeholder="请输入店铺简介"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="主营类目" prop="categories">
          <el-select
            v-model="formData.categories"
            multiple
            filterable
            allow-create
            default-first-option
            placeholder="请选择或输入主营类目"
            style="width: 100%"
          >
            <el-option
              v-for="item in categoryOptions"
              :key="item"
              :label="item"
              :value="item"
            />
          </el-select>
          <div class="form-item-tip">支持多选，也可自定义输入新类目</div>
        </el-form-item>
      </el-card>

      <!-- 授权信息卡片 -->
      <el-card class="form-section" shadow="never">
        <template #header>
          <div class="section-header">
            <el-icon><Key /></el-icon>
            <span>授权信息</span>
            <el-tag
              :type="authStatusTagType"
              size="small"
              class="auth-status-tag"
            >
              {{ authStatusText }}
            </el-tag>
          </div>
        </template>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="授权状态" prop="authStatus">
              <el-select
                v-model="formData.authStatus"
                placeholder="请选择授权状态"
                style="width: 100%"
              >
                <el-option label="已授权" value="authorized">
                  <div class="auth-option">
                    <el-icon color="#67c23a"><CircleCheck /></el-icon>
                    <span>已授权</span>
                  </div>
                </el-option>
                <el-option label="未授权" value="unauthorized">
                  <div class="auth-option">
                    <el-icon color="#f56c6c"><CircleClose /></el-icon>
                    <span>未授权</span>
                  </div>
                </el-option>
                <el-option label="授权过期" value="expired">
                  <div class="auth-option">
                    <el-icon color="#e6a23c"><Warning /></el-icon>
                    <span>授权过期</span>
                  </div>
                </el-option>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item
              label="授权时间"
              prop="authTime"
              v-if="formData.authStatus === 'authorized'"
            >
              <el-date-picker
                v-model="formData.authTime"
                type="datetime"
                placeholder="请选择授权时间"
                style="width: 100%"
                value-format="YYYY-MM-DD HH:mm:ss"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item
          label="过期时间"
          prop="expireTime"
          v-if="formData.authStatus === 'authorized'"
        >
          <el-date-picker
            v-model="formData.expireTime"
            type="datetime"
            placeholder="请选择授权过期时间"
            style="width: 100%"
            value-format="YYYY-MM-DD HH:mm:ss"
          />
          <div v-if="expireWarning" class="form-item-warning">
            <el-icon><Warning /></el-icon>
            <span>授权即将过期，请及时续期</span>
          </div>
        </el-form-item>

        <el-form-item v-if="formData.authStatus === 'unauthorized'">
          <el-alert
            title="店铺尚未授权"
            description="请完成平台授权以同步店铺数据"
            type="warning"
            :closable="false"
            show-icon
          />
        </el-form-item>
      </el-card>

      <!-- 联系信息卡片 -->
      <el-card class="form-section" shadow="never">
        <template #header>
          <div class="section-header">
            <el-icon><Phone /></el-icon>
            <span>联系信息</span>
          </div>
        </template>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="客服电话" prop="servicePhone">
              <el-input
                v-model="formData.servicePhone"
                placeholder="请输入客服电话"
                clearable
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="客服邮箱" prop="serviceEmail">
              <el-input
                v-model="formData.serviceEmail"
                placeholder="请输入客服邮箱"
                clearable
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="店铺地址" prop="address">
          <el-input
            v-model="formData.address"
            type="textarea"
            :rows="2"
            placeholder="请输入店铺地址"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>
      </el-card>

      <!-- 其他信息卡片 -->
      <el-card class="form-section" shadow="never">
        <template #header>
          <div class="section-header">
            <el-icon><InfoFilled /></el-icon>
            <span>其他信息</span>
          </div>
        </template>

        <el-form-item label="备注" prop="remark">
          <el-input
            v-model="formData.remark"
            type="textarea"
            :rows="3"
            placeholder="请输入备注信息"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
      </el-card>
    </el-form>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleClose">取消</el-button>
        <el-button
          type="primary"
          :loading="submitLoading"
          :icon="Check"
          @click="handleSubmit"
        >
          {{ submitButtonText }}
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, computed, watch, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import type { FormInstance, UploadRawFile } from 'element-plus'
import {
  Plus,
  Delete,
  Check,
  Shop,
  Key,
  Phone,
  InfoFilled,
  CircleCheck,
  CircleClose,
  Warning
} from '@element-plus/icons-vue'

// ============================================
// 类型定义
// ============================================

/** 授权状态 */
type AuthStatus = 'authorized' | 'unauthorized' | 'expired'

/** 店铺状态 */
type ShopStatus = 'enabled' | 'disabled'

/** 平台类型 */
type PlatformType = 'douyin' | 'taobao' | 'xiaohongshu'

/** 店铺表单数据类型 */
export interface ShopFormData {
  /** 店铺ID（编辑时存在） */
  id?: string | number
  /** 店铺名称 */
  name: string
  /** 所属平台 */
  platform: PlatformType | ''
  /** 店铺类型 */
  shopType: string
  /** 店铺Logo */
  logo: string
  /** 店铺简介 */
  description: string
  /** 主营类目 */
  categories: string[]
  /** 店铺状态 */
  status: ShopStatus
  /** 授权状态 */
  authStatus: AuthStatus
  /** 授权时间 */
  authTime: string
  /** 过期时间 */
  expireTime: string
  /** 客服电话 */
  servicePhone: string
  /** 客服邮箱 */
  serviceEmail: string
  /** 店铺地址 */
  address: string
  /** 备注 */
  remark: string
}

/** 组件Props类型 */
interface Props {
  /** 控制弹窗显示 */
  visible: boolean
  /** 编辑时的店铺数据 */
  shopData?: Partial<ShopFormData> | null
  /** 弹窗模式 */
  mode: 'add' | 'edit'
}

/** 组件Emits类型 */
interface Emits {
  (e: 'update:visible', value: boolean): void
  (e: 'submit', data: ShopFormData): void
}

// ============================================
// Props & Emits
// ============================================

const props = withDefaults(defineProps<Props>(), {
  visible: false,
  shopData: null,
  mode: 'add'
})

const emit = defineEmits<Emits>()

// ============================================
// Refs & Reactive State
// ============================================

const formRef = ref<FormInstance>()
const submitLoading = ref(false)

// 表单数据
const formData = reactive<ShopFormData>({
  id: undefined,
  name: '',
  platform: '',
  shopType: '',
  logo: '',
  description: '',
  categories: [],
  status: 'enabled',
  authStatus: 'unauthorized',
  authTime: '',
  expireTime: '',
  servicePhone: '',
  serviceEmail: '',
  address: '',
  remark: ''
})

// 主营类目选项
const categoryOptions = [
  '服装鞋帽',
  '美妆护肤',
  '家居日用',
  '数码家电',
  '食品生鲜',
  '母婴用品',
  '运动户外',
  '图书文具',
  '珠宝配饰',
  '汽车用品',
  '医药保健',
  '宠物用品'
]

// 店铺类型选项
const shopTypeOptions = computed(() => [
  { label: '旗舰店', value: 'flagship' },
  { label: '专卖店', value: 'exclusive' },
  { label: '专营店', value: 'specialized' },
  { label: '个人店', value: 'personal' }
])

// ============================================
// 计算属性
// ============================================

/** 弹窗标题 */
const dialogTitle = computed(() => {
  return props.mode === 'add' ? '添加店铺' : '编辑店铺'
})

/** 提交按钮文本 */
const submitButtonText = computed(() => {
  return props.mode === 'add' ? '确认添加' : '确认修改'
})

/** 上传地址（根据实际情况配置） */
const uploadAction = computed(() => {
  return '/api/upload/image'
})

/** 上传请求头 */
const uploadHeaders = computed(() => {
  return {
    Authorization: `Bearer ${localStorage.getItem('token') || ''}`
  }
})

/** 授权状态文本 */
const authStatusText = computed(() => {
  const statusMap: Record<AuthStatus, string> = {
    authorized: '已授权',
    unauthorized: '未授权',
    expired: '授权过期'
  }
  return statusMap[formData.authStatus] || '未知'
})

/** 授权状态标签类型 */
const authStatusTagType = computed(() => {
  const typeMap: Record<AuthStatus, string> = {
    authorized: 'success',
    unauthorized: 'danger',
    expired: 'warning'
  }
  return typeMap[formData.authStatus] || 'info'
})

/** 授权过期警告 */
const expireWarning = computed(() => {
  if (!formData.expireTime || formData.authStatus !== 'authorized') {
    return false
  }
  const expireDate = new Date(formData.expireTime)
  const now = new Date()
  const diffDays = Math.ceil((expireDate.getTime() - now.getTime()) / (1000 * 60 * 60 * 24))
  return diffDays <= 7 && diffDays > 0
})

// ============================================
// 表单验证规则
// ============================================

const formRules = {
  name: [
    { required: true, message: '请输入店铺名称', trigger: 'blur' },
    { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  platform: [
    { required: true, message: '请选择所属平台', trigger: 'change' }
  ],
  shopType: [
    { required: true, message: '请选择店铺类型', trigger: 'change' }
  ],
  status: [
    { required: true, message: '请选择店铺状态', trigger: 'change' }
  ],
  authStatus: [
    { required: true, message: '请选择授权状态', trigger: 'change' }
  ],
  servicePhone: [
    {
      pattern: /^1[3-9]\d{9}$|^0\d{2,3}-?\d{7,8}$/,
      message: '请输入正确的电话号码',
      trigger: 'blur'
    }
  ],
  serviceEmail: [
    {
      type: 'email',
      message: '请输入正确的邮箱地址',
      trigger: 'blur'
    }
  ]
}

// ============================================
// 工具函数
// ============================================

/** 重置表单 */
const resetForm = () => {
  Object.assign(formData, {
    id: undefined,
    name: '',
    platform: '',
    shopType: '',
    logo: '',
    description: '',
    categories: [],
    status: 'enabled',
    authStatus: 'unauthorized',
    authTime: '',
    expireTime: '',
    servicePhone: '',
    serviceEmail: '',
    address: '',
    remark: ''
  })
}

/** 初始化表单数据（编辑模式） */
const initFormData = (data: Partial<ShopFormData>) => {
  Object.assign(formData, {
    id: data.id,
    name: data.name || '',
    platform: (data.platform as PlatformType) || '',
    shopType: data.shopType || '',
    logo: data.logo || '',
    description: data.description || '',
    categories: data.categories || [],
    status: (data.status as ShopStatus) || 'enabled',
    authStatus: (data.authStatus as AuthStatus) || 'unauthorized',
    authTime: data.authTime || '',
    expireTime: data.expireTime || '',
    servicePhone: data.servicePhone || '',
    serviceEmail: data.serviceEmail || '',
    address: data.address || '',
    remark: data.remark || ''
  })
}

/** 平台选择变化处理 */
const handlePlatformChange = (value: PlatformType) => {
  // 可以在这里根据不同平台设置不同的默认值或规则
  console.log('平台切换为:', value)
}

// ============================================
// Logo上传方法
// ============================================

/** Logo上传前验证 */
const beforeLogoUpload = (file: UploadRawFile) => {
  const isJpgOrPng = file.type === 'image/jpeg' || file.type === 'image/png'
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isJpgOrPng) {
    ElMessage.error('只支持 JPG/PNG 格式的图片!')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过 2MB!')
    return false
  }
  return true
}

/** Logo上传成功 */
const handleLogoSuccess = (response: any, file: UploadRawFile) => {
  if (response && response.url) {
    formData.logo = response.url
  } else {
    // 模拟上传成功，使用本地预览
    formData.logo = URL.createObjectURL(file)
  }
  ElMessage.success('Logo上传成功')
}

/** 删除Logo */
const removeLogo = () => {
  formData.logo = ''
  ElMessage.success('Logo已删除')
}

/** 上传错误处理 */
const handleUploadError = () => {
  ElMessage.error('图片上传失败，请重试')
}

// ============================================
// 事件处理
// ============================================

/** 关闭弹窗 */
const handleClose = () => {
  emit('update:visible', false)
  nextTick(() => {
    resetForm()
    formRef.value?.clearValidate()
  })
}

/** 提交表单 */
const handleSubmit = async () => {
  if (!formRef.value) return

  try {
    await formRef.value.validate((valid) => {
      if (!valid) {
        throw new Error('表单验证失败')
      }
    })

    submitLoading.value = true

    // 构建提交数据
    const submitData: ShopFormData = { ...formData }

    emit('submit', submitData)
  } catch (error) {
    ElMessage.warning('请检查表单填写是否正确')
  } finally {
    submitLoading.value = false
  }
}

// ============================================
// 监听器
// ============================================

/** 监听visible变化 */
watch(
  () => props.visible,
  (newVisible) => {
    if (newVisible) {
      nextTick(() => {
        if (props.mode === 'edit' && props.shopData) {
          initFormData(props.shopData)
        } else {
          resetForm()
        }
        formRef.value?.clearValidate()
      })
    }
  },
  { immediate: true }
)

/** 监听shopData变化 */
watch(
  () => props.shopData,
  (newData) => {
    if (props.mode === 'edit' && newData && props.visible) {
      initFormData(newData)
    }
  },
  { deep: true }
)
</script>

<style lang="scss" scoped>
.shop-form-dialog {
  :deep(.el-dialog__body) {
    padding: 20px;
    max-height: 70vh;
    overflow-y: auto;
  }
}

.shop-form {
  .form-section {
    margin-bottom: 20px;

    &:last-child {
      margin-bottom: 0;
    }

    :deep(.el-card__header) {
      padding: 12px 20px;
      border-bottom: 1px solid var(--el-border-color-light);
    }

    :deep(.el-card__body) {
      padding: 20px;
    }
  }

  .section-header {
    display: flex;
    align-items: center;
    gap: 8px;
    font-weight: 600;
    color: var(--el-text-color-primary);

    .el-icon {
      font-size: 18px;
      color: var(--el-color-primary);
    }

    .auth-status-tag {
      margin-left: auto;
    }
  }
}

// 平台选项样式
.platform-option {
  display: flex;
  align-items: center;
  gap: 8px;

  .platform-icon {
    font-size: 12px;
    font-weight: bold;

    &.douyin {
      background: linear-gradient(135deg, #1c1c1c, #000000);
      color: #fff;
    }

    &.taobao {
      background: linear-gradient(135deg, #ff5000, #ff6a00);
      color: #fff;
    }

    &.xiaohongshu {
      background: linear-gradient(135deg, #ff2442, #ff4d6d);
      color: #fff;
    }
  }
}

// 授权选项样式
.auth-option {
  display: flex;
  align-items: center;
  gap: 8px;
}

// Logo上传样式
.logo-uploader {
  :deep(.el-upload) {
    border: 1px dashed var(--el-border-color);
    border-radius: 8px;
    cursor: pointer;
    position: relative;
    overflow: hidden;
    transition: var(--el-transition-duration-fast);
    width: 150px;
    height: 150px;

    &:hover {
      border-color: var(--el-color-primary);
    }
  }

  .logo-upload-placeholder {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    width: 100%;
    height: 100%;

    .upload-icon {
      font-size: 32px;
      color: var(--el-text-color-secondary);
    }

    .upload-text {
      margin-top: 8px;
      font-size: 14px;
      color: var(--el-text-color-regular);
    }

    .upload-hint {
      margin-top: 4px;
      font-size: 12px;
      color: var(--el-text-color-secondary);
    }
  }

  .logo-preview-wrapper {
    position: relative;
    width: 100%;
    height: 100%;

    .uploaded-logo {
      width: 100%;
      height: 100%;
      object-fit: cover;
      border-radius: 8px;
    }

    .logo-actions {
      position: absolute;
      top: 8px;
      right: 8px;
      opacity: 0;
      transition: opacity 0.3s;
    }

    &:hover .logo-actions {
      opacity: 1;
    }
  }
}

// 表单提示
.form-item-tip {
  margin-top: 4px;
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.form-item-warning {
  margin-top: 8px;
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: var(--el-color-warning);

  .el-icon {
    font-size: 14px;
  }
}

// 底部按钮
.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}
</style>
