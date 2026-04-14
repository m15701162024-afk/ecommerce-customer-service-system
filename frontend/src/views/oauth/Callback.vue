<template>
  <div class="oauth-callback">
    <div class="callback-content">
      <div v-if="loading" class="loading-state">
        <el-icon class="is-loading" :size="48"><Loading /></el-icon>
        <h2>正在绑定账号...</h2>
        <p>请稍候，正在处理授权信息</p>
      </div>
      
      <div v-else-if="success" class="success-state">
        <el-icon :size="64" color="#67c23a"><CircleCheck /></el-icon>
        <h2>绑定成功</h2>
        <p>{{ platformName }}账号已成功绑定</p>
        <el-button type="primary" @click="handleClose">关闭窗口</el-button>
      </div>
      
      <div v-else class="error-state">
        <el-icon :size="64" color="#f56c6c"><CircleClose /></el-icon>
        <h2>绑定失败</h2>
        <p>{{ errorMessage }}</p>
        <el-button type="primary" @click="handleRetry">重新绑定</el-button>
        <el-button @click="handleClose">关闭窗口</el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Loading, CircleCheck, CircleClose } from '@element-plus/icons-vue'
import { bindAccount, PLATFORMS } from '@/api/oauth'

const route = useRoute()

const loading = ref(true)
const success = ref(false)
const errorMessage = ref('')

const platform = computed(() => route.params.platform)
const platformName = computed(() => PLATFORMS[platform.value]?.name || platform.value)

const handleCallback = async () => {
  const code = route.query.code
  const state = route.query.state || ''
  const error = route.query.error
  const errorDesc = route.query.error_description

  if (error) {
    loading.value = false
    errorMessage.value = errorDesc || '授权被拒绝'
    return
  }

  if (!code) {
    loading.value = false
    errorMessage.value = '未获取到授权码'
    return
  }

  try {
    await bindAccount(platform.value, code, state)
    loading.value = false
    success.value = true
    
    if (window.opener) {
      window.opener.postMessage({ type: 'OAUTH_SUCCESS', platform: platform.value }, '*')
    }
  } catch (error) {
    loading.value = false
    errorMessage.value = error.message || '绑定失败，请稍后重试'
  }
}

const handleClose = () => {
  if (window.opener) {
    window.close()
  } else {
    window.location.href = '/settings/account-binding'
  }
}

const handleRetry = () => {
  if (window.opener) {
    window.opener.postMessage({ type: 'OAUTH_RETRY', platform: platform.value }, '*')
    window.close()
  } else {
    window.location.href = '/settings/account-binding'
  }
}

onMounted(() => {
  handleCallback()
})
</script>

<style lang="scss" scoped>
.oauth-callback {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  
  .callback-content {
    background: #fff;
    border-radius: 12px;
    padding: 48px 64px;
    text-align: center;
    box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
    
    h2 {
      margin: 16px 0 8px 0;
      font-size: 20px;
      color: #303133;
    }
    
    p {
      margin: 0 0 24px 0;
      color: #909399;
    }
    
    .loading-state {
      .is-loading {
        animation: rotating 2s linear infinite;
        color: #409eff;
      }
    }
    
    .success-state, .error-state {
      .el-button {
        margin: 0 8px;
      }
    }
  }
}

@keyframes rotating {
  0% {
    transform: rotate(0deg);
  }
  100% {
    transform: rotate(360deg);
  }
}
</style>