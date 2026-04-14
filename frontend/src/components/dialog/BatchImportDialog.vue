<template>
  <el-dialog
    v-model="dialogVisible"
    title="批量导入"
    width="800px"
    :close-on-click-modal="false"
    @close="handleClose"
  >
    <!-- 步骤指示器 -->
    <el-steps :active="currentStep" align-center class="import-steps">
      <el-step title="下载模板" />
      <el-step title="上传文件" />
      <el-step title="数据预览" />
      <el-step title="确认导入" />
    </el-steps>

    <!-- 步骤1: 下载模板 -->
    <div v-show="currentStep === 0" class="step-content">
      <el-card shadow="never">
        <div class="template-section">
          <el-icon class="template-icon"><Download /></el-icon>
          <div class="template-info">
            <h3>下载导入模板</h3>
            <p>请先下载模板文件，按照模板格式填写数据后上传</p>
          </div>
          <el-button type="primary" @click="downloadTemplate">
            <el-icon><Download /></el-icon>
            下载模板
          </el-button>
        </div>
        
        <el-divider />
        
        <div class="field-info">
          <h4>必填字段说明</h4>
          <el-table :data="requiredFields" size="small" border>
            <el-table-column prop="field" label="字段名" width="150" />
            <el-table-column prop="description" label="说明" />
            <el-table-column prop="example" label="示例" width="150" />
          </el-table>
        </div>
      </el-card>
      <div class="step-actions">
        <el-button @click="handleClose">取消</el-button>
        <el-button type="primary" @click="nextStep">下一步</el-button>
      </div>
    </div>

    <!-- 步骤2: 上传文件 -->
    <div v-show="currentStep === 1" class="step-content">
      <el-card shadow="never">
        <el-upload
          ref="uploadRef"
          class="upload-area"
          drag
          :auto-upload="false"
          :limit="1"
          :on-change="handleFileChange"
          :on-exceed="handleExceed"
          :before-upload="beforeUpload"
          accept=".xlsx,.xls,.csv"
        >
          <el-icon class="upload-icon"><UploadFilled /></el-icon>
          <div class="upload-text">
            <p>将文件拖到此处，或<em>点击上传</em></p>
            <p class="upload-hint">支持 .xlsx, .xls, .csv 格式，文件大小不超过 10MB</p>
          </div>
        </el-upload>
        
        <div v-if="uploadedFile" class="file-info">
          <el-icon class="file-icon"><Document /></el-icon>
          <span class="file-name">{{ uploadedFile.name }}</span>
          <span class="file-size">{{ formatFileSize(uploadedFile.size) }}</span>
          <el-button type="danger" link @click="removeFile">
            <el-icon><Delete /></el-icon>
            删除
          </el-button>
        </div>
      </el-card>
      <div class="step-actions">
        <el-button @click="prevStep">上一步</el-button>
        <el-button type="primary" :disabled="!uploadedFile" @click="parseFile">解析文件</el-button>
      </div>
    </div>

    <!-- 步骤3: 数据预览 -->
    <div v-show="currentStep === 2" class="step-content">
      <el-card shadow="never">
        <div class="preview-header">
          <div class="preview-stats">
            <el-statistic title="总行数" :value="previewData.length" />
            <el-statistic title="有效数据" :value="validCount" />
            <el-statistic title="错误数据" :value="errorCount" :value-style="{ color: errorCount > 0 ? '#f56c6c' : '#67c23a' }" />
          </div>
        </div>
        
        <el-divider />
        
        <div class="preview-table">
          <h4>数据预览（前10条）</h4>
          <el-table 
            :data="previewData.slice(0, 10)" 
            size="small" 
            border 
            max-height="300"
            :row-class-name="getRowClassName"
          >
            <el-table-column type="index" label="#" width="50" />
            <el-table-column 
              v-for="col in previewColumns" 
              :key="col.prop"
              :prop="col.prop" 
              :label="col.label"
              show-overflow-tooltip
            >
              <template #default="{ row }">
                <span :class="{ 'error-cell': row._errors?.includes(col.prop) }">
                  {{ row[col.prop] }}
                </span>
              </template>
            </el-table-column>
            <el-table-column v-if="errorCount > 0" label="错误信息" width="200">
              <template #default="{ row }">
                <el-tag v-if="row._errorMsg" type="danger" size="small">
                  {{ row._errorMsg }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
        </div>
        
        <div v-if="errorList.length > 0" class="error-list">
          <el-collapse>
            <el-collapse-item title="查看所有错误详情" name="errors">
              <el-table :data="errorList" size="small" border max-height="200">
                <el-table-column prop="row" label="行号" width="80" />
                <el-table-column prop="field" label="字段" width="120" />
                <el-table-column prop="message" label="错误信息" />
              </el-table>
            </el-collapse-item>
          </el-collapse>
        </div>
      </el-card>
      <div class="step-actions">
        <el-button @click="prevStep">重新上传</el-button>
        <el-button type="primary" :disabled="validCount === 0" @click="nextStep">下一步</el-button>
      </div>
    </div>

    <!-- 步骤4: 确认导入 -->
    <div v-show="currentStep === 3" class="step-content">
      <el-card shadow="never">
        <el-result
          v-if="importResult"
          :icon="importResult.success ? 'success' : 'error'"
          :title="importResult.success ? '导入成功' : '导入失败'"
          :sub-title="importResult.message"
        >
          <template #extra>
            <el-button type="primary" @click="handleClose">完成</el-button>
            <el-button v-if="!importResult.success" @click="resetImport">重新导入</el-button>
          </template>
        </el-result>
        
        <div v-else class="confirm-section">
          <el-icon class="confirm-icon"><Warning /></el-icon>
          <div class="confirm-info">
            <h3>确认导入数据</h3>
            <p>即将导入 <strong>{{ validCount }}</strong> 条有效数据</p>
            <p v-if="errorCount > 0" class="error-hint">
              有 {{ errorCount }} 条数据存在错误，将被跳过
            </p>
          </div>
          <el-button type="primary" :loading="importing" @click="executeImport">
            <el-icon><Check /></el-icon>
            确认导入
          </el-button>
        </div>
      </el-card>
      <div v-if="!importResult" class="step-actions">
        <el-button @click="prevStep">上一步</el-button>
      </div>
    </div>
  </el-dialog>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { 
  Download, UploadFilled, Document, Delete, 
  Warning, Check 
} from '@element-plus/icons-vue'

const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  },
  importType: {
    type: String,
    default: 'product'
  },
  templateUrl: {
    type: String,
    default: ''
  },
  uploadUrl: {
    type: String,
    default: ''
  }
})

const emit = defineEmits(['update:visible', 'success'])

const dialogVisible = computed({
  get: () => props.visible,
  set: (val) => emit('update:visible', val)
})

const currentStep = ref(0)
const uploadedFile = ref(null)
const previewData = ref([])
const previewColumns = ref([])
const errorList = ref([])
const importing = ref(false)
const importResult = ref(null)
const uploadRef = ref(null)

const requiredFields = computed(() => {
  const fieldsMap = {
    product: [
      { field: 'name', description: '商品名称', example: 'iPhone 15' },
      { field: 'platform', description: '所属平台', example: 'douyin' },
      { field: 'price', description: '售价（元）', example: '99.00' },
      { field: 'stock', description: '库存数量', example: '100' }
    ],
    order: [
      { field: 'orderNo', description: '订单编号', example: 'ORD20240101' },
      { field: 'buyerId', description: '买家ID', example: 'USER001' },
      { field: 'amount', description: '订单金额', example: '199.00' }
    ],
    customer: [
      { field: 'name', description: '客户姓名', example: '张三' },
      { field: 'phone', description: '联系电话', example: '13800138000' }
    ]
  }
  return fieldsMap[props.importType] || fieldsMap.product
})

const validCount = computed(() => 
  previewData.value.filter(row => !row._errorMsg).length
)

const errorCount = computed(() => 
  previewData.value.filter(row => row._errorMsg).length
)

watch(() => props.visible, (val) => {
  if (val) {
    resetImport()
  }
})

const handleClose = () => {
  emit('update:visible', false)
}

const nextStep = () => {
  if (currentStep.value < 3) {
    currentStep.value++
  }
}

const prevStep = () => {
  if (currentStep.value > 0) {
    currentStep.value--
  }
}

const downloadTemplate = () => {
  if (props.templateUrl) {
    window.open(props.templateUrl, '_blank')
  } else {
    ElMessage.info('模板下载功能待配置')
  }
}

const handleFileChange = (file) => {
  uploadedFile.value = file.raw
}

const handleExceed = () => {
  ElMessage.warning('只能上传一个文件')
}

const beforeUpload = (file) => {
  const isValidType = ['xlsx', 'xls', 'csv'].some(type => 
    file.name.endsWith(type)
  )
  const isLt10M = file.size / 1024 / 1024 < 10
  
  if (!isValidType) {
    ElMessage.error('只能上传 Excel 或 CSV 文件')
    return false
  }
  if (!isLt10M) {
    ElMessage.error('文件大小不能超过 10MB')
    return false
  }
  return true
}

const removeFile = () => {
  uploadedFile.value = null
  uploadRef.value?.clearFiles()
}

const formatFileSize = (size) => {
  if (size < 1024) return size + ' B'
  if (size < 1024 * 1024) return (size / 1024).toFixed(2) + ' KB'
  return (size / 1024 / 1024).toFixed(2) + ' MB'
}

const parseFile = async () => {
  if (!uploadedFile.value) return
  
  try {
    ElMessage.info('正在解析文件...')
    
    const mockData = generateMockPreviewData()
    previewData.value = mockData
    previewColumns.value = requiredFields.value.map(f => ({
      prop: f.field,
      label: f.description
    }))
    
    validateData()
    nextStep()
  } catch (error) {
    ElMessage.error('文件解析失败: ' + error.message)
  }
}

const generateMockPreviewData = () => {
  const count = Math.floor(Math.random() * 20) + 10
  const data = []
  
  for (let i = 0; i < count; i++) {
    const row = {}
    requiredFields.value.forEach(field => {
      row[field.field] = `${field.example}_${i + 1}`
    })
    
    if (i % 7 === 0) {
      row._errorMsg = '字段验证失败'
      row._errors = [requiredFields.value[0].field]
    }
    
    data.push(row)
  }
  
  return data
}

const validateData = () => {
  errorList.value = []
  
  previewData.value.forEach((row, index) => {
    if (!row._errorMsg) {
      requiredFields.value.forEach(field => {
        if (!row[field.field]) {
          row._errorMsg = row._errorMsg || '必填字段缺失'
          row._errors = row._errors || []
          row._errors.push(field.field)
          errorList.value.push({
            row: index + 1,
            field: field.field,
            message: '必填字段不能为空'
          })
        }
      })
    } else {
      errorList.value.push({
        row: index + 1,
        field: '-',
        message: row._errorMsg
      })
    }
  })
}

const getRowClassName = ({ row }) => {
  return row._errorMsg ? 'error-row' : ''
}

const executeImport = async () => {
  importing.value = true
  
  try {
    const validData = previewData.value.filter(row => !row._errorMsg)
    
    await new Promise(resolve => setTimeout(resolve, 2000))
    
    importResult.value = {
      success: true,
      message: `成功导入 ${validData.length} 条数据`,
      count: validData.length
    }
    
    emit('success', {
      type: props.importType,
      count: validData.length,
      data: validData
    })
    
    ElMessage.success('导入成功')
  } catch (error) {
    importResult.value = {
      success: false,
      message: '导入失败: ' + error.message
    }
    ElMessage.error('导入失败')
  } finally {
    importing.value = false
  }
}

const resetImport = () => {
  currentStep.value = 0
  uploadedFile.value = null
  previewData.value = []
  previewColumns.value = []
  errorList.value = []
  importing.value = false
  importResult.value = null
  uploadRef.value?.clearFiles()
}
</script>

<style lang="scss" scoped>
.import-steps {
  margin-bottom: 24px;
}

.step-content {
  min-height: 400px;
}

.step-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 24px;
}

.template-section {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 24px;
  
  .template-icon {
    font-size: 48px;
    color: var(--el-color-primary);
  }
  
  .template-info {
    flex: 1;
    
    h3 {
      margin: 0 0 8px;
      font-size: 18px;
    }
    
    p {
      margin: 0;
      color: var(--el-text-color-secondary);
    }
  }
}

.field-info {
  h4 {
    margin: 0 0 12px;
    font-size: 14px;
  }
}

.upload-area {
  width: 100%;
  
  :deep(.el-upload-dragger) {
    width: 100%;
    height: 200px;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
  }
  
  .upload-icon {
    font-size: 64px;
    color: var(--el-text-color-placeholder);
    margin-bottom: 16px;
  }
  
  .upload-text {
    p {
      margin: 0;
      font-size: 16px;
      
      em {
        color: var(--el-color-primary);
        font-style: normal;
      }
    }
    
    .upload-hint {
      font-size: 12px;
      color: var(--el-text-color-placeholder);
      margin-top: 8px;
    }
  }
}

.file-info {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  background: var(--el-fill-color-light);
  border-radius: 4px;
  margin-top: 16px;
  
  .file-icon {
    font-size: 24px;
    color: var(--el-color-primary);
  }
  
  .file-name {
    font-weight: 500;
  }
  
  .file-size {
    color: var(--el-text-color-secondary);
    font-size: 12px;
  }
}

.preview-header {
  .preview-stats {
    display: flex;
    justify-content: space-around;
  }
}

.preview-table {
  h4 {
    margin: 0 0 12px;
    font-size: 14px;
  }
}

.error-row {
  background-color: var(--el-color-danger-light-9);
}

.error-cell {
  color: var(--el-color-danger);
}

.error-list {
  margin-top: 16px;
}

.error-hint {
  color: var(--el-color-danger);
  font-size: 12px;
}

.confirm-section {
  display: flex;
  align-items: center;
  gap: 24px;
  padding: 32px;
  
  .confirm-icon {
    font-size: 64px;
    color: var(--el-color-warning);
  }
  
  .confirm-info {
    flex: 1;
    
    h3 {
      margin: 0 0 12px;
      font-size: 20px;
    }
    
    p {
      margin: 0;
      color: var(--el-text-color-secondary);
    }
  }
}
</style>