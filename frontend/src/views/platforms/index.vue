<template>
  <div class="platforms-page">
    <el-row :gutter="20">
      <el-col :span="8" v-for="platform in platforms" :key="platform.code">
        <el-card class="platform-card">
          <div class="platform-header">
            <div class="platform-icon" :style="{ background: platform.color }">
              <el-icon :size="32"><component :is="platform.icon" /></el-icon>
            </div>
            <div class="platform-title">{{ platform.name }}</div>
          </div>
          <div class="platform-status">
            <el-tag :type="platform.connected ? 'success' : 'danger'" size="large">
              {{ platform.connected ? '已连接' : '未连接' }}
            </el-tag>
          </div>
          <div class="platform-stats">
            <div class="stat">
              <div class="stat-value">{{ platform.stats.orders }}</div>
              <div class="stat-label">今日订单</div>
            </div>
            <div class="stat">
              <div class="stat-value">{{ platform.stats.messages }}</div>
              <div class="stat-label">待处理消息</div>
            </div>
          </div>
          <div class="platform-actions">
            <el-button type="primary" v-if="!platform.connected" @click="handleConnect(platform)">连接平台</el-button>
            <el-button v-else @click="handleConfig(platform)">配置</el-button>
            <el-button v-if="platform.connected" type="warning" @click="handleSync(platform)">同步数据</el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>
    
    <el-card class="mt-20">
      <template #header>
        <span>API配置</span>
      </template>
      <el-form :model="apiConfig" label-width="120px">
        <el-tabs v-model="activePlatform">
          <el-tab-pane label="抖音" name="douyin">
            <el-form-item label="App ID">
              <el-input v-model="apiConfig.douyin.appId" placeholder="请输入抖音App ID" />
            </el-form-item>
            <el-form-item label="App Secret">
              <el-input v-model="apiConfig.douyin.appSecret" type="password" placeholder="请输入App Secret" show-password />
            </el-form-item>
            <el-form-item label="消息回调URL">
              <el-input v-model="apiConfig.douyin.callbackUrl" placeholder="消息推送回调地址" />
            </el-form-item>
          </el-tab-pane>
          
          <el-tab-pane label="淘宝/千牛" name="taobao">
            <el-form-item label="App Key">
              <el-input v-model="apiConfig.taobao.appKey" placeholder="请输入淘宝App Key" />
            </el-form-item>
            <el-form-item label="App Secret">
              <el-input v-model="apiConfig.taobao.appSecret" type="password" placeholder="请输入App Secret" show-password />
            </el-form-item>
            <el-form-item label="千牛插件ID">
              <el-input v-model="apiConfig.taobao.pluginId" placeholder="千牛插件ID" />
            </el-form-item>
          </el-tab-pane>
          
          <el-tab-pane label="小红书" name="xiaohongshu">
            <el-form-item label="App ID">
              <el-input v-model="apiConfig.xiaohongshu.appId" placeholder="请输入小红书App ID" />
            </el-form-item>
            <el-form-item label="App Secret">
              <el-input v-model="apiConfig.xiaohongshu.appSecret" type="password" placeholder="请输入App Secret" show-password />
            </el-form-item>
          </el-tab-pane>
          
          <el-tab-pane label="1688" name="a1688">
            <el-form-item label="App Key">
              <el-input v-model="apiConfig.a1688.appKey" placeholder="请输入1688 App Key" />
            </el-form-item>
            <el-form-item label="App Secret">
              <el-input v-model="apiConfig.a1688.appSecret" type="password" placeholder="请输入App Secret" show-password />
            </el-form-item>
            <el-form-item label="诚信通账号">
              <el-input v-model="apiConfig.a1688.account" placeholder="诚信通账号" />
            </el-form-item>
          </el-tab-pane>
        </el-tabs>
        
        <el-form-item>
          <el-button type="primary" @click="handleSaveConfig">保存配置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'

const activePlatform = ref('douyin')

const platforms = ref([
  { code: 'douyin', name: '抖音', icon: 'VideoCamera', color: '#fe2c55', connected: true, stats: { orders: 45, messages: 12 } },
  { code: 'taobao', name: '淘宝/千牛', icon: 'Shop', color: '#ff5000', connected: true, stats: { orders: 32, messages: 8 } },
  { code: 'xiaohongshu', name: '小红书', icon: 'PictureFilled', color: '#ff2442', connected: false, stats: { orders: 0, messages: 0 } },
  { code: 'a1688', name: '1688采购', icon: 'ShoppingCart', color: '#ff6a00', connected: true, stats: { orders: 28, messages: 0 } }
])

const apiConfig = reactive({
  douyin: { appId: '', appSecret: '', callbackUrl: '' },
  taobao: { appKey: '', appSecret: '', pluginId: '' },
  xiaohongshu: { appId: '', appSecret: '' },
  a1688: { appKey: '', appSecret: '', account: '' }
})

const handleConnect = (platform) => {
  console.log('连接平台:', platform.name)
}

const handleConfig = (platform) => {
  activePlatform.value = platform.code
}

const handleSync = (platform) => {
  console.log('同步数据:', platform.name)
}

const handleSaveConfig = () => {
  console.log('保存配置:', apiConfig)
}
</script>

<style lang="scss" scoped>
.platforms-page {
  .platform-card {
    text-align: center;
    
    .platform-header {
      display: flex;
      flex-direction: column;
      align-items: center;
      margin-bottom: 15px;
      
      .platform-icon {
        width: 64px;
        height: 64px;
        border-radius: 16px;
        display: flex;
        align-items: center;
        justify-content: center;
        color: #fff;
        margin-bottom: 10px;
      }
      
      .platform-title {
        font-size: 18px;
        font-weight: bold;
      }
    }
    
    .platform-status {
      margin-bottom: 15px;
    }
    
    .platform-stats {
      display: flex;
      justify-content: space-around;
      margin-bottom: 15px;
      padding: 15px 0;
      border-top: 1px solid #eee;
      border-bottom: 1px solid #eee;
      
      .stat {
        .stat-value {
          font-size: 24px;
          font-weight: bold;
          color: #303133;
        }
        
        .stat-label {
          font-size: 12px;
          color: #909399;
        }
      }
    }
    
    .platform-actions {
      display: flex;
      justify-content: center;
      gap: 10px;
    }
  }
  
  .mt-20 {
    margin-top: 20px;
  }
}
</style>