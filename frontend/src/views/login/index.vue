<template>
  <div class="login-page">
    <div class="login-container">
      <div class="login-box">
        <div class="login-header">
          <el-icon :size="48"><Shop /></el-icon>
          <h1>网店客服采购系统</h1>
          <p>E-commerce Customer Service & Procurement System</p>
        </div>
        
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
        
        <div class="login-footer">
          <p>默认账号: admin / admin123</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const formRef = ref(null)
const loading = ref(false)

const loginForm = reactive({
  username: 'admin',
  password: 'admin123',
  remember: true
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const handleLogin = async () => {
  await formRef.value.validate()
  loading.value = true
  setTimeout(() => {
    loading.value = false
    router.push('/')
  }, 1000)
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
    width: 400px;
    
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
      
      .login-form {
        .el-form-item:last-of-type {
          margin-bottom: 0;
        }
      }
      
      .login-footer {
        text-align: center;
        margin-top: 20px;
        padding-top: 20px;
        border-top: 1px solid #eee;
        
        p {
          color: #909399;
          font-size: 12px;
        }
      }
    }
  }
}
</style>