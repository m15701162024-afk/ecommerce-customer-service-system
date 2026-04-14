<template>
  <el-dialog
    :model-value="visible"
    :title="dialogTitle"
    width="800px"
    :close-on-click-modal="false"
    :destroy-on-close="true"
    @close="handleClose"
    class="product-form-dialog"
  >
    <el-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      label-width="100px"
      class="product-form"
    >
      <!-- 基本信息卡片 -->
      <el-card class="form-section" shadow="never">
        <template #header>
          <div class="section-header">
            <el-icon><Document /></el-icon>
            <span>基本信息</span>
          </div>
        </template>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="商品名称" prop="name">
              <el-input
                v-model="formData.name"
                placeholder="请输入商品名称"
                maxlength="100"
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
              >
                <el-option label="抖音" value="douyin" />
                <el-option label="淘宝" value="taobao" />
                <el-option label="小红书" value="xiaohongshu" />
                <el-option label="京东" value="jd" />
                <el-option label="拼多多" value="pdd" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="商品售价" prop="price">
              <el-input-number
                v-model="formData.price"
                :min="0"
                :precision="2"
                :step="0.01"
                controls-position="right"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="采购价格" prop="sourcePrice">
              <el-input-number
                v-model="formData.sourcePrice"
                :min="0"
                :precision="2"
                :step="0.01"
                controls-position="right"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="商品状态" prop="status">
              <el-radio-group v-model="formData.status">
                <el-radio label="active">
                  <el-tag type="success" size="small">上架</el-tag>
                </el-radio>
                <el-radio label="inactive">
                  <el-tag type="info" size="small">下架</el-tag>
                </el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="货源供应商" prop="sourceSupplier">
              <el-input
                v-model="formData.sourceSupplier"
                placeholder="请输入货源供应商"
                clearable
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="商品描述" prop="description">
          <el-input
            v-model="formData.description"
            type="textarea"
            :rows="3"
            placeholder="请输入商品描述"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
      </el-card>

      <!-- 图片上传卡片 -->
      <el-card class="form-section" shadow="never">
        <template #header>
          <div class="section-header">
            <el-icon><Picture /></el-icon>
            <span>商品图片</span>
            <span class="section-desc">支持上传主图和详情图，最多10张</span>
          </div>
        </template>

        <div class="upload-section">
          <div class="upload-label">商品主图</div>
          <el-upload
            class="main-image-uploader"
            :action="uploadAction"
            :headers="uploadHeaders"
            :show-file-list="false"
            :before-upload="beforeMainImageUpload"
            :on-success="handleMainImageSuccess"
            :on-error="handleUploadError"
            accept="image/*"
          >
            <div v-if="formData.mainImage" class="image-preview-wrapper">
              <el-image
                :src="formData.mainImage"
                fit="cover"
                class="uploaded-image"
              />
              <div class="image-actions">
                <el-button type="danger" circle size="small" @click.stop="removeMainImage">
                  <el-icon><Delete /></el-icon>
                </el-button>
              </div>
            </div>
            <div v-else class="upload-placeholder">
              <el-icon class="upload-icon"><Plus /></el-icon>
              <div class="upload-text">点击上传主图</div>
              <div class="upload-hint">建议尺寸 800x800</div>
            </div>
          </el-upload>
        </div>

        <div class="upload-section detail-images">
          <div class="upload-label">详情图片</div>
          <el-upload
            class="detail-images-uploader"
            :action="uploadAction"
            :headers="uploadHeaders"
            :file-list="detailImageList"
            list-type="picture-card"
            :before-upload="beforeDetailImageUpload"
            :on-success="handleDetailImageSuccess"
            :on-remove="handleDetailImageRemove"
            :on-error="handleUploadError"
            :limit="9"
            accept="image/*"
          >
            <el-icon><Plus /></el-icon>
            <template #tip>
              <div class="upload-tip">
                最多上传9张详情图，支持 jpg/png 格式，单张不超过 2MB
              </div>
            </template>
          </el-upload>
        </div>
      </el-card>

      <!-- SKU管理卡片 -->
      <el-card class="form-section" shadow="never">
        <template #header>
          <div class="section-header">
            <el-icon><Grid /></el-icon>
            <span>SKU 规格管理</span>
            <el-button
              type="primary"
              size="small"
              :icon="Plus"
              @click="addSku"
              class="add-sku-btn"
            >
              添加规格
            </el-button>
          </div>
        </template>

        <div v-if="formData.skus.length === 0" class="empty-sku-tip">
          <el-empty description="暂无SKU规格，点击上方按钮添加">
            <el-button type="primary" :icon="Plus" @click="addSku">
              添加SKU
            </el-button>
          </el-empty>
        </div>

        <div v-for="(sku, index) in formData.skus" :key="sku.id" class="sku-item">
          <div class="sku-header">
            <span class="sku-title">规格 {{ index + 1 }}</span>
            <el-button
              type="danger"
              link
              size="small"
              :icon="Delete"
              @click="removeSku(index)"
            >
              删除
            </el-button>
          </div>
          
          <el-row :gutter="16">
            <el-col :span="6">
              <el-form-item
                :label="'规格名称'"
                :prop="`skus.${index}.specName`"
                :rules="skuRules.specName"
              >
                <el-input
                  v-model="sku.specName"
                  placeholder="如：颜色"
                  clearable
                />
              </el-form-item>
            </el-col>
            <el-col :span="6">
              <el-form-item
                :label="'规格值'"
                :prop="`skus.${index}.specValue`"
                :rules="skuRules.specValue"
              >
                <el-input
                  v-model="sku.specValue"
                  placeholder="如：红色"
                  clearable
                />
              </el-form-item>
            </el-col>
            <el-col :span="5">
              <el-form-item
                :label="'价格'"
                :prop="`skus.${index}.price`"
                :rules="skuRules.price"
              >
                <el-input-number
                  v-model="sku.price"
                  :min="0"
                  :precision="2"
                  :step="0.01"
                  controls-position="right"
                  style="width: 100%"
                />
              </el-form-item>
            </el-col>
            <el-col :span="5">
              <el-form-item
                :label="'库存'"
                :prop="`skus.${index}.stock`"
                :rules="skuRules.stock"
              >
                <el-input-number
                  v-model="sku.stock"
                  :min="0"
                  :step="1"
                  controls-position="right"
                  style="width: 100%"
                />
              </el-form-item>
            </el-col>
            <el-col :span="2">
              <el-form-item label=" " class="sku-status-item">
                <el-switch
                  v-model="sku.status"
                  :active-value="'active'"
                  :inactive-value="'inactive'"
                  active-text=""
                  inactive-text=""
                />
              </el-form-item>
            </el-col>
          </el-row>

          <el-form-item label="SKU图片" class="sku-image-item">
            <el-upload
              class="sku-image-uploader"
              :action="uploadAction"
              :headers="uploadHeaders"
              :show-file-list="false"
              :before-upload="beforeSkuImageUpload"
              :on-success="(res) => handleSkuImageSuccess(res, index)"
              :on-error="handleUploadError"
              accept="image/*"
            >
              <div v-if="sku.image" class="sku-image-preview">
                <el-image :src="sku.image" fit="cover" />
                <div class="sku-image-actions">
                  <el-icon @click.stop="removeSkuImage(index)"><Delete /></el-icon>
                </div>
              </div>
              <div v-else class="sku-upload-placeholder">
                <el-icon><Plus /></el-icon>
              </div>
            </el-upload>
          </el-form-item>
        </div>

        <div v-if="formData.skus.length > 0" class="sku-summary">
          <el-descriptions :column="3" border>
            <el-descriptions-item label="规格总数">{{ formData.skus.length }}</el-descriptions-item>
            <el-descriptions-item label="总库存">{{ totalStock }}</el-descriptions-item>
            <el-descriptions-item label="价格区间">¥{{ minPrice }} - ¥{{ maxPrice }}</el-descriptions-item>
          </el-descriptions>
        </div>
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
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, UploadFile, UploadRawFile } from 'element-plus'
import {
  Plus,
  Delete,
  Check,
  Document,
  Picture,
  Grid
} from '@element-plus/icons-vue'

// ============================================
// 类型定义
// ============================================

/** SKU规格类型 */
export interface SkuItem {
  /** SKU唯一标识 */
  id: string
  /** 规格名称 */
  specName: string
  /** 规格值 */
  specValue: string
  /** SKU价格 */
  price: number
  /** SKU库存 */
  stock: number
  /** SKU状态 */
  status: 'active' | 'inactive'
  /** SKU图片 */
  image: string
}

/** 商品表单数据类型 */
export interface ProductFormData {
  /** 商品ID（编辑时存在） */
  id?: string | number
  /** 商品名称 */
  name: string
  /** 所属平台 */
  platform: string
  /** 售价 */
  price: number
  /** 采购价 */
  sourcePrice: number
  /** 总库存 */
  stock: number
  /** 商品状态 */
  status: 'active' | 'inactive'
  /** 货源供应商 */
  sourceSupplier: string
  /** 商品主图 */
  mainImage: string
  /** 详情图片列表 */
  detailImages: string[]
  /** 商品描述 */
  description: string
  /** SKU规格列表 */
  skus: SkuItem[]
}

/** 组件Props类型 */
interface Props {
  /** 控制弹窗显示 */
  visible: boolean
  /** 编辑时的商品数据 */
  productData?: Partial<ProductFormData> | null
  /** 弹窗模式 */
  mode: 'add' | 'edit'
}

/** 组件Emits类型 */
interface Emits {
  (e: 'update:visible', value: boolean): void
  (e: 'submit', data: ProductFormData): void
}

// ============================================
// Props & Emits
// ============================================

const props = withDefaults(defineProps<Props>(), {
  visible: false,
  productData: null,
  mode: 'add'
})

const emit = defineEmits<Emits>()

// ============================================
// Refs & Reactive State
// ============================================

const formRef = ref<FormInstance>()
const submitLoading = ref(false)

// 详情图片上传列表（用于el-upload组件）
const detailImageList = ref<Array<{ name: string; url: string }>>([])

// 表单数据
const formData = reactive<ProductFormData>({
  name: '',
  platform: '',
  price: 0,
  sourcePrice: 0,
  stock: 0,
  status: 'active',
  sourceSupplier: '',
  mainImage: '',
  detailImages: [],
  description: '',
  skus: []
})

// ============================================
// 计算属性
// ============================================

/** 弹窗标题 */
const dialogTitle = computed(() => {
  return props.mode === 'add' ? '添加商品' : '编辑商品'
})

/** 提交按钮文本 */
const submitButtonText = computed(() => {
  return props.mode === 'add' ? '确认添加' : '确认修改'
})

/** 上传地址（根据实际情况配置） */
const uploadAction = computed(() => {
  // 这里应该使用实际的图片上传接口
  return '/api/upload/image'
})

/** 上传请求头 */
const uploadHeaders = computed(() => {
  // 这里可以添加认证token等
  return {
    Authorization: `Bearer ${localStorage.getItem('token') || ''}`
  }
})

/** SKU总库存 */
const totalStock = computed(() => {
  return formData.skus.reduce((sum, sku) => sum + (sku.stock || 0), 0)
})

/** SKU最低价格 */
const minPrice = computed(() => {
  if (formData.skus.length === 0) return formData.price
  const prices = formData.skus.map(sku => sku.price).filter(p => p > 0)
  return prices.length > 0 ? Math.min(...prices) : formData.price
})

/** SKU最高价格 */
const maxPrice = computed(() => {
  if (formData.skus.length === 0) return formData.price
  const prices = formData.skus.map(sku => sku.price).filter(p => p > 0)
  return prices.length > 0 ? Math.max(...prices) : formData.price
})

// ============================================
// 表单验证规则
// ============================================

const formRules = {
  name: [
    { required: true, message: '请输入商品名称', trigger: 'blur' },
    { min: 2, max: 100, message: '长度在 2 到 100 个字符', trigger: 'blur' }
  ],
  platform: [
    { required: true, message: '请选择所属平台', trigger: 'change' }
  ],
  price: [
    { required: true, message: '请输入商品售价', trigger: 'blur' },
    { type: 'number', min: 0, message: '价格不能为负数', trigger: 'blur' }
  ],
  sourcePrice: [
    { type: 'number', min: 0, message: '价格不能为负数', trigger: 'blur' }
  ]
}

const skuRules = {
  specName: [
    { required: true, message: '请输入规格名称', trigger: 'blur' }
  ],
  specValue: [
    { required: true, message: '请输入规格值', trigger: 'blur' }
  ],
  price: [
    { required: true, message: '请输入价格', trigger: 'blur' },
    { type: 'number', min: 0.01, message: '价格必须大于0', trigger: 'blur' }
  ],
  stock: [
    { required: true, message: '请输入库存', trigger: 'blur' },
    { type: 'number', min: 0, message: '库存不能为负数', trigger: 'blur' }
  ]
}

// ============================================
// 工具函数
// ============================================

/** 生成唯一ID */
const generateId = (): string => {
  return `${Date.now()}_${Math.random().toString(36).substr(2, 9)}`
}

/** 重置表单 */
const resetForm = () => {
  Object.assign(formData, {
    id: undefined,
    name: '',
    platform: '',
    price: 0,
    sourcePrice: 0,
    stock: 0,
    status: 'active',
    sourceSupplier: '',
    mainImage: '',
    detailImages: [],
    description: '',
    skus: []
  })
  detailImageList.value = []
}

/** 初始化表单数据（编辑模式） */
const initFormData = (data: Partial<ProductFormData>) => {
  Object.assign(formData, {
    id: data.id,
    name: data.name || '',
    platform: data.platform || '',
    price: data.price || 0,
    sourcePrice: data.sourcePrice || 0,
    stock: data.stock || 0,
    status: data.status || 'active',
    sourceSupplier: data.sourceSupplier || '',
    mainImage: data.mainImage || data.image || '',
    detailImages: data.detailImages || [],
    description: data.description || '',
    skus: data.skus?.map(sku => ({
      id: sku.id || generateId(),
      specName: sku.specName || '',
      specValue: sku.specValue || '',
      price: sku.price || 0,
      stock: sku.stock || 0,
      status: sku.status || 'active',
      image: sku.image || ''
    })) || []
  })
  
  // 初始化详情图片列表
  detailImageList.value = formData.detailImages.map((url, index) => ({
    name: `detail_${index}`,
    url
  }))
}

// ============================================
// SKU管理方法
// ============================================

/** 添加SKU */
const addSku = () => {
  const newSku: SkuItem = {
    id: generateId(),
    specName: '',
    specValue: '',
    price: formData.price,
    stock: 0,
    status: 'active',
    image: ''
  }
  formData.skus.push(newSku)
}

/** 删除SKU */
const removeSku = async (index: number) => {
  try {
    await ElMessageBox.confirm('确定删除该SKU规格吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    formData.skus.splice(index, 1)
    ElMessage.success('删除成功')
  } catch {
    // 用户取消删除
  }
}

// ============================================
// 图片上传方法
// ============================================

/** 主图上传前验证 */
const beforeMainImageUpload = (file: UploadRawFile) => {
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

/** 主图上传成功 */
const handleMainImageSuccess = (response: any) => {
  // 根据实际接口返回调整
  if (response && response.url) {
    formData.mainImage = response.url
    ElMessage.success('主图上传成功')
  } else {
    // 模拟上传成功，使用base64
    ElMessage.success('主图上传成功')
  }
}

/** 删除主图 */
const removeMainImage = () => {
  formData.mainImage = ''
  ElMessage.success('主图已删除')
}

/** 详情图上传前验证 */
const beforeDetailImageUpload = (file: UploadRawFile) => {
  return beforeMainImageUpload(file)
}

/** 详情图上传成功 */
const handleDetailImageSuccess = (response: any, file: UploadFile) => {
  if (response && response.url) {
    formData.detailImages.push(response.url)
  } else {
    // 使用上传后的临时URL
    const url = file.url || URL.createObjectURL(file.raw!)
    formData.detailImages.push(url)
  }
  ElMessage.success('图片上传成功')
}

/** 详情图删除 */
const handleDetailImageRemove = (file: UploadFile) => {
  const index = formData.detailImages.findIndex(url => url === file.url)
  if (index > -1) {
    formData.detailImages.splice(index, 1)
  }
}

/** SKU图片上传前验证 */
const beforeSkuImageUpload = (file: UploadRawFile) => {
  return beforeMainImageUpload(file)
}

/** SKU图片上传成功 */
const handleSkuImageSuccess = (response: any, skuIndex: number) => {
  if (response && response.url) {
    formData.skus[skuIndex].image = response.url
  }
  ElMessage.success('SKU图片上传成功')
}

/** 删除SKU图片 */
const removeSkuImage = (skuIndex: number) => {
  formData.skus[skuIndex].image = ''
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
    
    // 更新总库存
    if (formData.skus.length > 0) {
      formData.stock = totalStock.value
    }
    
    submitLoading.value = true
    
    // 构建提交数据
    const submitData: ProductFormData = {
      ...formData,
      // 兼容旧字段
      image: formData.mainImage
    }
    
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
      // 弹窗打开时初始化数据
      nextTick(() => {
        if (props.mode === 'edit' && props.productData) {
          initFormData(props.productData)
        } else {
          resetForm()
        }
        formRef.value?.clearValidate()
      })
    }
  },
  { immediate: true }
)

/** 监听productData变化 */
watch(
  () => props.productData,
  (newData) => {
    if (props.mode === 'edit' && newData && props.visible) {
      initFormData(newData)
    }
  },
  { deep: true }
)
</script>

<style lang="scss" scoped>
.product-form-dialog {
  :deep(.el-dialog__body) {
    padding: 20px;
    max-height: 70vh;
    overflow-y: auto;
  }
}

.product-form {
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
    
    .section-desc {
      margin-left: 12px;
      font-size: 12px;
      font-weight: normal;
      color: var(--el-text-color-secondary);
    }
    
    .add-sku-btn {
      margin-left: auto;
    }
  }
}

// 图片上传样式
.upload-section {
  margin-bottom: 20px;
  
  &.detail-images {
    margin-bottom: 0;
  }
  
  .upload-label {
    font-size: 14px;
    color: var(--el-text-color-regular);
    margin-bottom: 12px;
    font-weight: 500;
  }
}

.main-image-uploader {
  :deep(.el-upload) {
    border: 1px dashed var(--el-border-color);
    border-radius: 6px;
    cursor: pointer;
    position: relative;
    overflow: hidden;
    transition: var(--el-transition-duration-fast);
    width: 200px;
    height: 200px;
    
    &:hover {
      border-color: var(--el-color-primary);
    }
  }
  
  .upload-placeholder {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    width: 100%;
    height: 100%;
    
    .upload-icon {
      font-size: 28px;
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
  
  .image-preview-wrapper {
    position: relative;
    width: 100%;
    height: 100%;
    
    .uploaded-image {
      width: 100%;
      height: 100%;
      object-fit: cover;
    }
    
    .image-actions {
      position: absolute;
      top: 8px;
      right: 8px;
      opacity: 0;
      transition: opacity 0.3s;
    }
    
    &:hover .image-actions {
      opacity: 1;
    }
  }
}

.detail-images-uploader {
  :deep(.el-upload--picture-card) {
    width: 120px;
    height: 120px;
  }
  
  :deep(.el-upload-list--picture-card .el-upload-list__item) {
    width: 120px;
    height: 120px;
  }
  
  .upload-tip {
    margin-top: 8px;
    font-size: 12px;
    color: var(--el-text-color-secondary);
  }
}

// SKU样式
.empty-sku-tip {
  padding: 40px 0;
}

.sku-item {
  background-color: var(--el-fill-color-light);
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 16px;
  
  &:last-child {
    margin-bottom: 0;
  }
  
  .sku-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 16px;
    padding-bottom: 12px;
    border-bottom: 1px dashed var(--el-border-color);
    
    .sku-title {
      font-weight: 600;
      color: var(--el-text-color-primary);
      font-size: 14px;
    }
  }
  
  .sku-status-item {
    :deep(.el-form-item__content) {
      justify-content: center;
    }
  }
  
  .sku-image-item {
    margin-top: 8px;
    
    :deep(.el-form-item__label) {
      font-weight: normal;
    }
  }
}

.sku-image-uploader {
  :deep(.el-upload) {
    border: 1px dashed var(--el-border-color);
    border-radius: 6px;
    cursor: pointer;
    position: relative;
    overflow: hidden;
    transition: var(--el-transition-duration-fast);
    width: 80px;
    height: 80px;
    
    &:hover {
      border-color: var(--el-color-primary);
    }
  }
  
  .sku-upload-placeholder {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 100%;
    height: 100%;
    
    .el-icon {
      font-size: 20px;
      color: var(--el-text-color-secondary);
    }
  }
  
  .sku-image-preview {
    position: relative;
    width: 100%;
    height: 100%;
    
    .el-image {
      width: 100%;
      height: 100%;
    }
    
    .sku-image-actions {
      position: absolute;
      top: 0;
      left: 0;
      right: 0;
      bottom: 0;
      background: rgba(0, 0, 0, 0.5);
      display: flex;
      align-items: center;
      justify-content: center;
      opacity: 0;
      transition: opacity 0.3s;
      cursor: pointer;
      
      .el-icon {
        color: #fff;
        font-size: 20px;
      }
    }
    
    &:hover .sku-image-actions {
      opacity: 1;
    }
  }
}

.sku-summary {
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid var(--el-border-color-light);
}

// 底部按钮
.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}
</style>
