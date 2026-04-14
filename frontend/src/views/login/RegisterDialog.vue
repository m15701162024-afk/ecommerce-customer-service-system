<template>
  <el-dialog
    v-model="visible"
    title="用户注册"
    width="450px"
    :close-on-click-modal="false"
    @close="handleClose"
  >
    <el-form :model="registerForm" :rules="rules" ref="formRef" label-width="80px">
      <el-form-item label="用户名" prop="username">
        <el-input v-model="registerForm.username" placeholder="请输入用户名" />
      </el-form-item>
      
      <el-form-item label="密码" prop="password">
        <el-input
          v-model="registerForm.password"
          type="password"
          placeholder="请输入密码"
          show-password
        />
      </el-form-item>
      
      <el-form-item label="确认密码" prop="confirmPassword">
        <el-input
          v-model="registerForm.confirmPassword"
          type="password"
          placeholder="请再次输入密码"
          show-password
        />
      </el-form-item>
      
      <el-form-item label="手机号" prop="phone">
        <el-input v-model="registerForm.phone" placeholder="请输入手机号" />
      </el-form-item>
    </el-form>
    
    <template #footer>
      <span class="dialog-footer">
        <el-button @click="handleClose">取消</el-button>
        <el-button type="primary" @click="handleRegister" :loading="loading">
          注册
        </el-button>
      </span>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { register } from '@/api/auth'

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['update:modelValue', 'success'])

const visible = ref(false)
const formRef = ref(null)
const loading = ref(false)

const registerForm = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  phone: ''
})

const validatePassword = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请再次输入密码'))
  } else if (value !== registerForm.password) {
    callback(new Error('两次输入密码不一致'))
  } else {
    callback()
  }
}

const validatePhone = (rule, value, callback) => {
  if (!value) {
    callback(new Error('请输入手机号'))
  } else if (!/^1[3-9]\d{9}$/.test(value)) {
    callback(new Error('请输入正确的手机号'))
  } else {
    callback()
  }
}

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度3-20字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度6-20字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, validator: validatePassword, trigger: 'blur' }
  ],
  phone: [
    { required: true, validator: validatePhone, trigger: 'blur' }
  ]
}

watch(() => props.modelValue, (val) => {
  visible.value = val
})

watch(visible, (val) => {
  emit('update:modelValue', val)
})

const handleRegister = async () => {
  try {
    await formRef.value.validate()
  } catch {
    return
  }
  
  loading.value = true
  
  try {
    const { username, password, phone } = registerForm
    await register({ username, password, phone })
    ElMessage.success('注册成功，请登录')
    emit('success')
    handleClose()
  } catch (error) {
    const message = error.response?.data?.message || '注册失败，请稍后重试'
    ElMessage.error(message)
  } finally {
    loading.value = false
  }
}

const handleClose = () => {
  formRef.value?.resetFields()
  registerForm.username = ''
  registerForm.password = ''
  registerForm.confirmPassword = ''
  registerForm.phone = ''
  visible.value = false
}
</script>