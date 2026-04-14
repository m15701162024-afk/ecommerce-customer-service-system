<template>
  <div class="login-page">
    <div class="login-container">
      <div class="login-box">
        <div class="login-header">
          <el-icon :size="48"><Shop /></el-icon>
          <h1>网店客服采购系统</h1>
          <p>E-commerce Customer Service & Procurement System</p>
        </div>
        
        <el-tabs v-model="activeTab" class="login-tabs">
          <el-tab-pane label="账号登录" name="username">
            <el-form :model="loginForm" :rules="rules" ref="formRef" class="login-form">
              <el-form-item prop="username">
                <el-input v-model="loginForm.username" prefix-icon="User" placeholder="用户名" size="large" />
              </el-form-item>
              
              <el-form-item prop="password">
                <el-input v-model="loginForm.password" prefix-icon="Lock" type="password" placeholder="密码" size="large" show-password />
              </el-form-item>
              
              <el-form-item>
                <el-checkbox v-model="loginForm.remember">记住我</el-checkbox>
              </el-form-item>
              
              <el-form-item>
                <el-button type="primary" size="large" style="width: 100%" @click="handleLogin" :loading="loading">
                  登录
                </el-button>
              </el-form-item>
            </el-form>
          </el-tab-pane>
          
          <el-tab-pane label="手机号登录" name="phone">
            <el-form :model="phoneLoginForm" :rules="phoneRules" ref="phoneFormRef" class="login-form">
              <el-form-item prop="phone">
                <el-input v-model="phoneLoginForm.phone" prefix-icon="Phone" placeholder="手机号" size="large" />
              </el-form-item>
              
              <el-form-item prop="code">
                <el-input v-model="phoneLoginForm.code" prefix-icon="Key" placeholder="验证码" size="large" style="flex: 1" />
                <el-button :disabled="countdown > 0" @click="handleSendCode" style="margin-left: 10px; width: 120px">
                  {{ countdown > 0 ? `${countdown}秒` : '发送验证码' }}
                </el-button>
              </el-form-item>
              
              <el-form-item>
                <el-button type="primary" size="large" style="width: 100%" @click="handlePhoneLogin" :loading="phoneLoading">
                  登录
                </el-button>
              </el-form-item>
            </el-form>
          </el-tab-pane>
        </el-tabs>
        
        <div class="login-footer">
          <el-button type="text" @click="showRegisterDialog">还没有账号？立即注册</el-button>
        </div>
      </div>
    </div>
    
    <RegisterDialog v-model="showRegister" @success="handleRegisterSuccess" />
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { sendSmsCode } from '@/api/auth'
import RegisterDialog from './RegisterDialog.vue'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref(null)
const phoneFormRef = ref(null)
const loading = ref(false)
const phoneLoading = ref(false)
const activeTab = ref('username')
const showRegister = ref(false)
const countdown = ref(0)

const loginForm = reactive({
  username: '',
  password: '',
  remember: false
})

const phoneLoginForm = reactive({
  phone: '',
  code: ''
})

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度3-20字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度6-20字符', trigger: 'blur' }
  ]
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

const phoneRules = {
  phone: [
    { required: true, validator: validatePhone, trigger: 'blur' }
  ],
  code: [
    { required: true, message: '请输入验证码', trigger: 'blur' },
    { len: 6, message: '验证码长度为6位', trigger: 'blur' }
  ]
}

const handleLogin = async () => {
  try {
    await formRef.value.validate()
  } catch {
    return
  }
  
  loading.value = true
  
  try {
    const result = await userStore.loginAction(loginForm)
    
    if (result.success) {
      ElMessage.success('登录成功')
      router.push('/')
    } else {
      ElMessage.error(result.message || '登录失败')
    }
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '登录失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

const handleSendCode = async () => {
  try {
    await phoneFormRef.value.validateField('phone')
  } catch {
    return
  }
  
  try {
    await sendSmsCode(phoneLoginForm.phone)
    ElMessage.success('验证码已发送')
    countdown.value = 60
    
    const timer = setInterval(() => {
      countdown.value--
      if (countdown.value <= 0) {
        clearInterval(timer)
      }
    }, 1000)
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '验证码发送失败')
  }
}

const handlePhoneLogin = async () => {
  try {
    await phoneFormRef.value.validate()
  } catch {
    return
  }
  
  phoneLoading.value = true
  
  try {
    ElMessage.warning('手机号登录功能正在开发中，请使用账号登录')
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '登录失败')
  } finally {
    phoneLoading.value = false
  }
}

const showRegisterDialog = () => {
  showRegister.value = true
}

const handleRegisterSuccess = () => {
  activeTab.value = 'username'
}
</script>

<style lang="scss" scoped>
.login-page {
  height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  
  .login-container {
    width: 450px;
    
    .login-box {
      background: #fff;
      border-radius: 12px;
      padding: 40px;
      box-shadow: 0 10px 40px rgba(0, 0, 0, 0.2);
      
      .login-header {
        text-align: center;
        margin-bottom: 30px;
        
        .el-icon { color: #409eff; }
        
        h1 {
          margin: 15px 0 5px;
          font-size: 24px;
          color: #303133;
        }
        
        p {
          color: #909399;
          font-size: 12px;
        }
      }
      
      .login-tabs {
        .el-tabs__header {
          margin-bottom: 20px;
        }
        
        .login-form {
          .el-form-item {
            margin-bottom: 18px;
            
            &:last-child {
              margin-bottom: 0;
            }
            
            .el-input-group {
              display: flex;
              align-items: center;
            }
          }
        }
      }
      
      .login-footer {
        text-align: center;
        margin-top: 20px;
        padding-top: 20px;
        border-top: 1px solid #eee;
      }
    }
  }
}
</style>