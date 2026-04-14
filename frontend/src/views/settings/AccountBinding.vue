<template>
  <div class="account-binding">
    <el-card class="page-header">
      <template #header>
        <div class="header-content">
          <el-icon :size="24"><Link /></el-icon>
          <span>账号绑定</span>
        </div>
      </template>
      <p class="description">绑定第三方平台账号，实现数据同步和统一管理</p>
    </el-card>

    <el-card class="binding-list" v-loading="loading">
      <div class="platform-item" v-for="(config, key) in PLATFORMS" :key="key">
        <div class="platform-info">
          <div class="platform-icon" :style="{ backgroundColor: config.color + '15', color: config.color }">
            <el-icon :size="28">
              <component :is="config.icon" />
            </el-icon>
          </div>
          <div class="platform-details">
            <h3>{{ config.name }}</h3>
            <p>{{ config.description }}</p>
          </div>
        </div>
        
        <div class="platform-status">
          <template v-if="getBindingInfo(key)?.bound">
            <div class="bound-info">
              <el-tag type="success" effect="light">
                <el-icon><CircleCheck /></el-icon>
                已绑定
              </el-tag>
              <span class="bind-time" v-if="getBindingInfo(key)?.bindTime">
                {{ formatDate(getBindingInfo(key).bindTime) }}
              </span>
              <span class="account-name" v-if="getBindingInfo(key)?.accountName">
                {{ getBindingInfo(key).accountName }}
              </span>
            </div>
            <el-button 
              type="danger" 
              plain 
              :loading="unbinding === key"
              @click="handleUnbind(key)"
            >
              解除绑定
            </el-button>
          </template>
          <template v-else>
            <el-tag type="info" effect="light">
              <el-icon><CircleClose /></el-icon>
              未绑定
            </el-tag>
            <el-button 
              type="primary" 
              :loading="binding === key"
              @click="handleBind(key)"
            >
              绑定账号
            </el-button>
          </template>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  Link, CircleCheck, CircleClose, VideoCamera, Picture, Shop 
} from '@element-plus/icons-vue'
import { 
  getAuthorizeUrl, 
  bindAccount, 
  unbindAccount, 
  getBindingStatus,
  PLATFORMS 
} from '@/api/oauth'

const loading = ref(false)
const binding = ref(null)
const unbinding = ref(null)
const bindings = ref([])

const getBindingInfo = (platform) => {
  return bindings.value.find(b => b.platform === platform)
}

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
}

const fetchBindingStatus = async () => {
  loading.value = true
  try {
    const res = await getBindingStatus()
    bindings.value = res.data?.bindings || []
  } catch (error) {
    console.error('获取绑定状态失败:', error)
  } finally {
    loading.value = false
  }
}

const handleBind = async (platform) => {
  binding.value = platform
  try {
    const res = await getAuthorizeUrl(platform)
    const { authorizeUrl } = res.data || {}
    
    if (authorizeUrl) {
      const width = 600
      const height = 700
      const left = (window.screen.width - width) / 2
      const top = (window.screen.height - height) / 2
      
      const authWindow = window.open(
        authorizeUrl,
        `oauth_${platform}`,
        `width=${width},height=${height},left=${left},top=${top},toolbar=no,menubar=no,resizable=yes`
      )
      
      const checkClosed = setInterval(() => {
        if (authWindow?.closed) {
          clearInterval(checkClosed)
          fetchBindingStatus()
        }
      }, 500)
    }
  } catch (error) {
    ElMessage.error('获取授权链接失败')
  } finally {
    binding.value = null
  }
}

const handleUnbind = async (platform) => {
  try {
    await ElMessageBox.confirm(
      '确定要解除绑定吗？解绑后将无法同步该平台的数据。',
      '解除绑定',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    unbinding.value = platform
    await unbindAccount(platform)
    ElMessage.success('解绑成功')
    await fetchBindingStatus()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('解绑失败')
    }
  } finally {
    unbinding.value = null
  }
}

onMounted(() => {
  fetchBindingStatus()
})
</script>

<style lang="scss" scoped>
.account-binding {
  .page-header {
    margin-bottom: 20px;
    
    .header-content {
      display: flex;
      align-items: center;
      gap: 10px;
      font-size: 18px;
      font-weight: 600;
    }
    
    .description {
      color: #909399;
      margin: 0;
      font-size: 14px;
    }
  }
  
  .binding-list {
    .platform-item {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 24px;
      border-bottom: 1px solid #ebeef5;
      
      &:last-child {
        border-bottom: none;
      }
      
      .platform-info {
        display: flex;
        align-items: center;
        gap: 16px;
        
        .platform-icon {
          width: 56px;
          height: 56px;
          border-radius: 12px;
          display: flex;
          align-items: center;
          justify-content: center;
        }
        
        .platform-details {
          h3 {
            margin: 0 0 4px 0;
            font-size: 16px;
            font-weight: 600;
          }
          
          p {
            margin: 0;
            color: #909399;
            font-size: 13px;
          }
        }
      }
      
      .platform-status {
        display: flex;
        align-items: center;
        gap: 16px;
        
        .bound-info {
          display: flex;
          align-items: center;
          gap: 12px;
          
          .bind-time, .account-name {
            color: #909399;
            font-size: 13px;
          }
        }
      }
    }
  }
}
</style>