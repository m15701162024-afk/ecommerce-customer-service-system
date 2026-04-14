<template>
  <el-dialog
    :model-value="visible"
    :title="dialogTitle"
    width="750px"
    :close-on-click-modal="false"
    :destroy-on-close="true"
    @close="handleClose"
    class="supplier-form-dialog"
  >
    <el-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      label-width="110px"
      class="supplier-form"
    >
      <!-- 基本信息卡片 -->
      <el-card class="form-section" shadow="never">
        <template #header>
          <div class="section-header">
            <el-icon><OfficeBuilding /></el-icon>
            <span>基本信息</span>
          </div>
        </template>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="供应商名称" prop="name">
              <el-input
                v-model="formData.name"
                placeholder="请输入供应商名称"
                maxlength="100"
                show-word-limit
                clearable
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="供应商编码" prop="code">
              <el-input
                v-model="formData.code"
                placeholder="请输入供应商编码"
                maxlength="50"
                show-word-limit
                clearable
                :disabled="mode === 'edit'"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="供应商类型" prop="type">
              <el-select
                v-model="formData.type"
                placeholder="请选择供应商类型"
                clearable
                style="width: 100%"
              >
                <el-option label="厂家" value="manufacturer" />
                <el-option label="批发商" value="wholesaler" />
                <el-option label="代理商" value="agent" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="合作状态" prop="cooperationStatus">
              <el-select
                v-model="formData.cooperationStatus"
                placeholder="请选择合作状态"
                clearable
                style="width: 100%"
              >
                <el-option label="合作中" value="cooperating">
                  <el-tag type="success" size="small">合作中</el-tag>
                </el-option>
                <el-option label="暂停合作" value="paused">
                  <el-tag type="warning" size="small">暂停合作</el-tag>
                </el-option>
                <el-option label="已终止" value="terminated">
                  <el-tag type="danger" size="small">已终止</el-tag>
                </el-option>
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="经营范围" prop="businessScope">
          <el-select
            v-model="formData.businessScope"
            multiple
            filterable
            allow-create
            default-first-option
            placeholder="请输入经营范围，按回车确认"
            style="width: 100%"
          >
            <el-option
              v-for="item in businessScopeOptions"
              :key="item"
              :label="item"
              :value="item"
            />
          </el-select>
          <div class="form-item-tip">可输入自定义经营范围，支持多选</div>
        </el-form-item>
      </el-card>

      <!-- 联系信息卡片 -->
      <el-card class="form-section" shadow="never">
        <template #header>
          <div class="section-header">
            <el-icon><User /></el-icon>
            <span>联系信息</span>
          </div>
        </template>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="联系人" prop="contactPerson">
              <el-input
                v-model="formData.contactPerson"
                placeholder="请输入联系人姓名"
                maxlength="50"
                clearable
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="联系电话" prop="contactPhone">
              <el-input
                v-model="formData.contactPhone"
                placeholder="请输入联系电话"
                maxlength="20"
                clearable
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="联系邮箱" prop="contactEmail">
              <el-input
                v-model="formData.contactEmail"
                placeholder="请输入联系邮箱"
                maxlength="100"
                clearable
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="结算方式" prop="settlementMethod">
              <el-select
                v-model="formData.settlementMethod"
                placeholder="请选择结算方式"
                clearable
                style="width: 100%"
              >
                <el-option label="现结" value="cash" />
                <el-option label="月结" value="monthly" />
                <el-option label="季结" value="quarterly" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="供应商地址" prop="address">
          <div class="address-input-group">
            <el-cascader
              v-model="formData.addressRegion"
              :options="regionOptions"
              :props="regionProps"
              placeholder="请选择省/市/区"
              clearable
              style="width: 280px"
              @change="handleRegionChange"
            />
            <el-input
              v-model="formData.addressDetail"
              placeholder="请输入详细地址"
              maxlength="200"
              show-word-limit
              clearable
              style="flex: 1"
            />
          </div>
        </el-form-item>
      </el-card>

      <!-- 银行账户信息卡片 -->
      <el-card class="form-section" shadow="never">
        <template #header>
          <div class="section-header">
            <el-icon><Money /></el-icon>
            <span>银行账户信息</span>
            <span class="section-desc">用于货款结算</span>
          </div>
        </template>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="开户银行" prop="bankName">
              <el-input
                v-model="formData.bankName"
                placeholder="请输入开户银行"
                maxlength="100"
                clearable
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="开户名" prop="accountName">
              <el-input
                v-model="formData.accountName"
                placeholder="请输入开户名"
                maxlength="50"
                clearable
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="银行账号" prop="bankAccount">
          <el-input
            v-model="formData.bankAccount"
            placeholder="请输入银行账号"
            maxlength="50"
            clearable
          />
        </el-form-item>
      </el-card>

      <!-- 备注信息卡片 -->
      <el-card class="form-section" shadow="never">
        <template #header>
          <div class="section-header">
            <el-icon><Document /></el-icon>
            <span>备注信息</span>
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
import type { FormInstance } from 'element-plus'
import {
  OfficeBuilding,
  User,
  Money,
  Document,
  Check
} from '@element-plus/icons-vue'

// ============================================
// 类型定义
// ============================================

/** 供应商表单数据类型 */
export interface SupplierFormData {
  /** 供应商ID（编辑时存在） */
  id?: string | number
  /** 供应商名称 */
  name: string
  /** 供应商编码 */
  code: string
  /** 联系人 */
  contactPerson: string
  /** 联系电话 */
  contactPhone: string
  /** 联系邮箱 */
  contactEmail: string
  /** 省市区 */
  addressRegion: string[]
  /** 详细地址 */
  addressDetail: string
  /** 完整地址 */
  address?: string
  /** 供应商类型 */
  type: string
  /** 经营范围 */
  businessScope: string[]
  /** 合作状态 */
  cooperationStatus: string
  /** 结算方式 */
  settlementMethod: string
  /** 开户银行 */
  bankName: string
  /** 银行账号 */
  bankAccount: string
  /** 开户名 */
  accountName: string
  /** 备注 */
  remark: string
}

/** 组件Props类型 */
interface Props {
  /** 控制弹窗显示 */
  visible: boolean
  /** 编辑时的供应商数据 */
  supplierData?: Partial<SupplierFormData> | null
  /** 弹窗模式 */
  mode: 'add' | 'edit'
}

/** 组件Emits类型 */
interface Emits {
  (e: 'update:visible', value: boolean): void
  (e: 'submit', data: SupplierFormData): void
}

// ============================================
// Props & Emits
// ============================================

const props = withDefaults(defineProps<Props>(), {
  visible: false,
  supplierData: null,
  mode: 'add'
})

const emit = defineEmits<Emits>()

// ============================================
// Refs & Reactive State
// ============================================

const formRef = ref<FormInstance>()
const submitLoading = ref(false)

// 表单数据
const formData = reactive<SupplierFormData>({
  name: '',
  code: '',
  contactPerson: '',
  contactPhone: '',
  contactEmail: '',
  addressRegion: [],
  addressDetail: '',
  type: '',
  businessScope: [],
  cooperationStatus: 'cooperating',
  settlementMethod: '',
  bankName: '',
  bankAccount: '',
  accountName: '',
  remark: ''
})

// 经营范围预设选项
const businessScopeOptions = [
  '电子产品',
  '服装鞋帽',
  '食品饮料',
  '家居用品',
  '美妆护肤',
  '母婴用品',
  '运动户外',
  '图书文具',
  '汽车用品',
  '医疗器械'
]

// 省市区数据（简化版，实际项目中可从接口获取）
const regionOptions = [
  {
    value: '北京市',
    label: '北京市',
    children: [
      {
        value: '北京市',
        label: '北京市',
        children: [
          { value: '东城区', label: '东城区' },
          { value: '西城区', label: '西城区' },
          { value: '朝阳区', label: '朝阳区' },
          { value: '丰台区', label: '丰台区' },
          { value: '石景山区', label: '石景山区' },
          { value: '海淀区', label: '海淀区' },
          { value: '门头沟区', label: '门头沟区' },
          { value: '房山区', label: '房山区' },
          { value: '通州区', label: '通州区' },
          { value: '顺义区', label: '顺义区' },
          { value: '昌平区', label: '昌平区' },
          { value: '大兴区', label: '大兴区' },
          { value: '怀柔区', label: '怀柔区' },
          { value: '平谷区', label: '平谷区' },
          { value: '密云区', label: '密云区' },
          { value: '延庆区', label: '延庆区' }
        ]
      }
    ]
  },
  {
    value: '上海市',
    label: '上海市',
    children: [
      {
        value: '上海市',
        label: '上海市',
        children: [
          { value: '黄浦区', label: '黄浦区' },
          { value: '徐汇区', label: '徐汇区' },
          { value: '长宁区', label: '长宁区' },
          { value: '静安区', label: '静安区' },
          { value: '普陀区', label: '普陀区' },
          { value: '虹口区', label: '虹口区' },
          { value: '杨浦区', label: '杨浦区' },
          { value: '浦东新区', label: '浦东新区' },
          { value: '闵行区', label: '闵行区' },
          { value: '宝山区', label: '宝山区' },
          { value: '嘉定区', label: '嘉定区' },
          { value: '金山区', label: '金山区' },
          { value: '松江区', label: '松江区' },
          { value: '青浦区', label: '青浦区' },
          { value: '奉贤区', label: '奉贤区' },
          { value: '崇明区', label: '崇明区' }
        ]
      }
    ]
  },
  {
    value: '广东省',
    label: '广东省',
    children: [
      {
        value: '广州市',
        label: '广州市',
        children: [
          { value: '荔湾区', label: '荔湾区' },
          { value: '越秀区', label: '越秀区' },
          { value: '海珠区', label: '海珠区' },
          { value: '天河区', label: '天河区' },
          { value: '白云区', label: '白云区' },
          { value: '黄埔区', label: '黄埔区' },
          { value: '番禺区', label: '番禺区' },
          { value: '花都区', label: '花都区' },
          { value: '南沙区', label: '南沙区' },
          { value: '从化区', label: '从化区' },
          { value: '增城区', label: '增城区' }
        ]
      },
      {
        value: '深圳市',
        label: '深圳市',
        children: [
          { value: '罗湖区', label: '罗湖区' },
          { value: '福田区', label: '福田区' },
          { value: '南山区', label: '南山区' },
          { value: '宝安区', label: '宝安区' },
          { value: '龙岗区', label: '龙岗区' },
          { value: '盐田区', label: '盐田区' },
          { value: '龙华区', label: '龙华区' },
          { value: '坪山区', label: '坪山区' },
          { value: '光明区', label: '光明区' }
        ]
      },
      {
        value: '珠海市',
        label: '珠海市',
        children: [
          { value: '香洲区', label: '香洲区' },
          { value: '斗门区', label: '斗门区' },
          { value: '金湾区', label: '金湾区' }
        ]
      }
    ]
  },
  {
    value: '浙江省',
    label: '浙江省',
    children: [
      {
        value: '杭州市',
        label: '杭州市',
        children: [
          { value: '上城区', label: '上城区' },
          { value: '下城区', label: '下城区' },
          { value: '江干区', label: '江干区' },
          { value: '拱墅区', label: '拱墅区' },
          { value: '西湖区', label: '西湖区' },
          { value: '滨江区', label: '滨江区' },
          { value: '萧山区', label: '萧山区' },
          { value: '余杭区', label: '余杭区' },
          { value: '富阳区', label: '富阳区' },
          { value: '临安区', label: '临安区' },
          { value: '桐庐县', label: '桐庐县' },
          { value: '淳安县', label: '淳安县' },
          { value: '建德市', label: '建德市' }
        ]
      },
      {
        value: '宁波市',
        label: '宁波市',
        children: [
          { value: '海曙区', label: '海曙区' },
          { value: '江北区', label: '江北区' },
          { value: '北仑区', label: '北仑区' },
          { value: '镇海区', label: '镇海区' },
          { value: '鄞州区', label: '鄞州区' },
          { value: '奉化区', label: '奉化区' },
          { value: '余姚市', label: '余姚市' },
          { value: '慈溪市', label: '慈溪市' },
          { value: '象山县', label: '象山县' },
          { value: '宁海县', label: '宁海县' }
        ]
      }
    ]
  },
  {
    value: '江苏省',
    label: '江苏省',
    children: [
      {
        value: '南京市',
        label: '南京市',
        children: [
          { value: '玄武区', label: '玄武区' },
          { value: '秦淮区', label: '秦淮区' },
          { value: '建邺区', label: '建邺区' },
          { value: '鼓楼区', label: '鼓楼区' },
          { value: '浦口区', label: '浦口区' },
          { value: '栖霞区', label: '栖霞区' },
          { value: '雨花台区', label: '雨花台区' },
          { value: '江宁区', label: '江宁区' },
          { value: '六合区', label: '六合区' },
          { value: '溧水区', label: '溧水区' },
          { value: '高淳区', label: '高淳区' }
        ]
      },
      {
        value: '苏州市',
        label: '苏州市',
        children: [
          { value: '虎丘区', label: '虎丘区' },
          { value: '吴中区', label: '吴中区' },
          { value: '相城区', label: '相城区' },
          { value: '姑苏区', label: '姑苏区' },
          { value: '吴江区', label: '吴江区' },
          { value: '常熟市', label: '常熟市' },
          { value: '张家港市', label: '张家港市' },
          { value: '昆山市', label: '昆山市' },
          { value: '太仓市', label: '太仓市' }
        ]
      }
    ]
  }
]

// 级联选择器配置
const regionProps = {
  value: 'value',
  label: 'label',
  children: 'children'
}

// ============================================
// 计算属性
// ============================================

/** 弹窗标题 */
const dialogTitle = computed(() => {
  return props.mode === 'add' ? '添加供应商' : '编辑供应商'
})

/** 提交按钮文本 */
const submitButtonText = computed(() => {
  return props.mode === 'add' ? '确认添加' : '确认修改'
})

// ============================================
// 表单验证规则
// ============================================

/** 手机号验证 */
const validatePhone = (rule: any, value: string, callback: Function) => {
  if (!value) {
    callback(new Error('请输入联系电话'))
    return
  }
  const phoneRegex = /^1[3-9]\d{9}$/
  const telRegex = /^0\d{2,3}-?\d{7,8}$/
  if (phoneRegex.test(value) || telRegex.test(value)) {
    callback()
  } else {
    callback(new Error('请输入正确的手机号或固定电话'))
  }
}

/** 邮箱验证 */
const validateEmail = (rule: any, value: string, callback: Function) => {
  if (!value) {
    callback()
    return
  }
  const emailRegex = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/
  if (emailRegex.test(value)) {
    callback()
  } else {
    callback(new Error('请输入正确的邮箱地址'))
  }
}

/** 银行账号验证 */
const validateBankAccount = (rule: any, value: string, callback: Function) => {
  if (!value) {
    callback()
    return
  }
  // 银行账号一般为10-30位数字
  const accountRegex = /^\d{10,30}$/
  if (accountRegex.test(value)) {
    callback()
  } else {
    callback(new Error('银行账号应为10-30位数字'))
  }
}

const formRules = {
  name: [
    { required: true, message: '请输入供应商名称', trigger: 'blur' },
    { min: 2, max: 100, message: '长度在 2 到 100 个字符', trigger: 'blur' }
  ],
  code: [
    { required: true, message: '请输入供应商编码', trigger: 'blur' },
    { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' },
    { pattern: /^[a-zA-Z0-9_-]+$/, message: '编码只能包含字母、数字、下划线和横线', trigger: 'blur' }
  ],
  contactPerson: [
    { required: true, message: '请输入联系人', trigger: 'blur' },
    { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  contactPhone: [
    { required: true, validator: validatePhone, trigger: 'blur' }
  ],
  contactEmail: [
    { validator: validateEmail, trigger: 'blur' }
  ],
  type: [
    { required: true, message: '请选择供应商类型', trigger: 'change' }
  ],
  cooperationStatus: [
    { required: true, message: '请选择合作状态', trigger: 'change' }
  ],
  settlementMethod: [
    { required: true, message: '请选择结算方式', trigger: 'change' }
  ],
  bankAccount: [
    { validator: validateBankAccount, trigger: 'blur' }
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
    code: '',
    contactPerson: '',
    contactPhone: '',
    contactEmail: '',
    addressRegion: [],
    addressDetail: '',
    type: '',
    businessScope: [],
    cooperationStatus: 'cooperating',
    settlementMethod: '',
    bankName: '',
    bankAccount: '',
    accountName: '',
    remark: ''
  })
}

/** 初始化表单数据（编辑模式） */
const initFormData = (data: Partial<SupplierFormData>) => {
  // 解析地址
  let addressRegion: string[] = []
  if (data.address) {
    // 尝试从完整地址解析省市区
    const addressParts = data.address.split(' ')
    if (addressParts.length >= 3) {
      addressRegion = [addressParts[0], addressParts[1], addressParts[2]]
    }
  }

  Object.assign(formData, {
    id: data.id,
    name: data.name || '',
    code: data.code || '',
    contactPerson: data.contactPerson || '',
    contactPhone: data.contactPhone || '',
    contactEmail: data.contactEmail || '',
    addressRegion: data.addressRegion || addressRegion,
    addressDetail: data.addressDetail || '',
    type: data.type || '',
    businessScope: data.businessScope || [],
    cooperationStatus: data.cooperationStatus || 'cooperating',
    settlementMethod: data.settlementMethod || '',
    bankName: data.bankName || '',
    bankAccount: data.bankAccount || '',
    accountName: data.accountName || '',
    remark: data.remark || ''
  })
}

// ============================================
// 事件处理
// ============================================

/** 省市区选择变化 */
const handleRegionChange = (value: string[]) => {
  console.log('Selected region:', value)
}

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

    // 构建完整地址
    const regionText = formData.addressRegion.join(' ')
    const fullAddress = regionText + (formData.addressDetail ? ' ' + formData.addressDetail : '')

    // 构建提交数据
    const submitData: SupplierFormData = {
      ...formData,
      address: fullAddress
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
        if (props.mode === 'edit' && props.supplierData) {
          initFormData(props.supplierData)
        } else {
          resetForm()
        }
        formRef.value?.clearValidate()
      })
    }
  },
  { immediate: true }
)

/** 监听supplierData变化 */
watch(
  () => props.supplierData,
  (newData) => {
    if (props.mode === 'edit' && newData && props.visible) {
      initFormData(newData)
    }
  },
  { deep: true }
)
</script>

<style lang="scss" scoped>
.supplier-form-dialog {
  :deep(.el-dialog__body) {
    padding: 20px;
    max-height: 70vh;
    overflow-y: auto;
  }
}

.supplier-form {
  .form-section {
    margin-bottom: 20px;

    &:last-child {
      margin-bottom: 0;
    }

    :deep(.el-card__header) {
      padding: 12px 20px;
      border-bottom: 1px solid var(--el-border-color-light);
      background-color: var(--el-fill-color-light);
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
  }

  .form-item-tip {
    font-size: 12px;
    color: var(--el-text-color-secondary);
    margin-top: 4px;
    line-height: 1.4;
  }

  .address-input-group {
    display: flex;
    gap: 12px;
    align-items: flex-start;
  }
}

// 底部按钮
.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

// 响应式调整
@media screen and (max-width: 768px) {
  .supplier-form {
    .address-input-group {
      flex-direction: column;

      .el-cascader {
        width: 100% !important;
      }
    }
  }
}
</style>
