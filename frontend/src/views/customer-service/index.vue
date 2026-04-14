<template>
  <div class="customer-service-page">
    <el-row :gutter="20">
      <el-col :span="6">
        <el-card class="session-list">
          <template #header>
            <div class="card-header">
              <span>会话列表</span>
              <el-badge :value="unreadCount" type="danger" />
            </div>
          </template>
          <el-input v-model="searchKeyword" placeholder="搜索会话" prefix-icon="Search" clearable style="margin-bottom: 10px" />
          <div class="session-items">
            <div 
              v-for="session in sessions" 
              :key="session.id" 
              class="session-item"
              :class="{ active: currentSession?.id === session.id }"
              @click="selectSession(session)"
            >
              <el-avatar :size="40" :src="session.avatar" />
              <div class="session-info">
                <div class="session-name">{{ session.buyerName }}</div>
                <div class="session-last-msg">{{ session.lastMessage }}</div>
              </div>
              <div class="session-meta">
                <div class="session-time">{{ session.time }}</div>
                <el-badge :value="session.unread" v-if="session.unread > 0" type="danger" />
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="18">
        <el-card class="chat-area">
          <template #header>
            <div class="chat-header">
              <div class="buyer-info">
                <el-avatar :size="36" :src="currentSession?.avatar" />
                <div>
                  <div class="buyer-name">{{ currentSession?.buyerName }}</div>
                  <div class="buyer-platform">
                    <el-tag size="small" :type="getPlatformType(currentSession?.platform)">
                      {{ currentSession?.platform }}
                    </el-tag>
                  </div>
                </div>
              </div>
              <div class="chat-actions">
                <el-button type="primary" size="small" @click="handleTransferToHuman">转人工</el-button>
                <el-button size="small" @click="handleViewOrder">查看订单</el-button>
              </div>
            </div>
          </template>
          
          <div class="messages-container" ref="messagesRef">
            <div 
              v-for="msg in messages" 
              :key="msg.id" 
              class="message-item"
              :class="{ 'is-own': msg.isOwn }"
            >
              <el-avatar :size="32" :src="msg.avatar" />
              <div class="message-content">
                <div class="message-bubble">{{ msg.content }}</div>
                <div class="message-time">{{ msg.time }}</div>
              </div>
            </div>
          </div>
          
          <div class="input-area">
            <div class="quick-replies">
              <el-tag 
                v-for="reply in quickReplies" 
                :key="reply" 
                class="quick-reply-tag"
                @click="sendQuickReply(reply)"
              >
                {{ reply }}
              </el-tag>
            </div>
            <div class="input-box">
              <el-input 
                v-model="inputMessage" 
                type="textarea" 
                :rows="3" 
                placeholder="输入消息..." 
                @keyup.ctrl.enter="handleSendMessage"
              />
              <div class="input-actions">
                <el-button type="primary" @click="handleSendMessage">发送</el-button>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useChatStore } from '@/stores/chat'
import { sendMessage, transferToHuman } from '@/api/chat'

const router = useRouter()
const chatStore = useChatStore()

const searchKeyword = ref('')
const inputMessage = ref('')
const messagesRef = ref(null)

const sessions = ref([])
const currentSession = ref(null)
const messages = ref([])
const loading = ref(false)
const unreadCount = ref(0)

const quickReplies = ref([
  '您好，有什么可以帮助您的？',
  '商品有现货，24小时内发货',
  '感谢您的支持！',
  '请稍等，我帮您查询一下',
  '这是我们的优惠活动'
])

// 获取会话列表
const fetchSessions = async () => {
  try {
    await chatStore.fetchSessions()
    sessions.value = chatStore.sessions
    unreadCount.value = sessions.value.reduce((sum, s) => sum + (s.unread || 0), 0)
  } catch (error) {
    ElMessage.error('获取会话列表失败')
  }
}

// 选择会话
const selectSession = async (session) => {
  currentSession.value = session
  session.unread = 0
  try {
    await chatStore.fetchMessages(session.id)
    messages.value = chatStore.messages
    nextTick(() => {
      scrollToBottom()
    })
  } catch (error) {
    ElMessage.error('获取消息失败')
  }
}

// 滚动到底部
const scrollToBottom = () => {
  if (messagesRef.value) {
    messagesRef.value.scrollTop = messagesRef.value.scrollHeight
  }
}

const getPlatformType = (platform) => {
  const types = { '抖音': 'danger', '淘宝': 'warning', '小红书': 'success' }
  return types[platform] || 'info'
}

const handleSendMessage = async () => {
  if (!inputMessage.value.trim() || !currentSession.value) return
  const content = inputMessage.value.trim()
  inputMessage.value = ''
  
  // 添加本地消息
  messages.value.push({
    id: Date.now(),
    content,
    isOwn: true,
    avatar: '',
    time: new Date().toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
  })
  
  nextTick(() => scrollToBottom())
  
  try {
    loading.value = true
    const res = await sendMessage({ session_id: currentSession.value.id, message: content })
    if (res.data?.reply) {
      messages.value.push({
        id: Date.now() + 1,
        content: res.data.reply,
        isOwn: false,
        avatar: currentSession.value?.avatar || '',
        time: new Date().toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
      })
      nextTick(() => scrollToBottom())
    }
  } catch (error) {
    ElMessage.error('发送失败')
  } finally {
    loading.value = false
  }
}

const sendQuickReply = (reply) => {
  inputMessage.value = reply
  handleSendMessage()
}

const handleTransferToHuman = async () => {
  if (!currentSession.value) {
    ElMessage.warning('请先选择会话')
    return
  }
  try {
    await ElMessageBox.confirm('确认将该会话转给人工客服处理?', '提示', { type: 'warning' })
    loading.value = true
    await transferToHuman(currentSession.value.id, { reason: '用户请求转人工' })
    ElMessage.success('已转人工客服处理')
    // 更新会话状态
    currentSession.value.status = 'transferred'
    // 刷新会话列表
    fetchSessions()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('转人工失败')
    }
  } finally {
    loading.value = false
  }
}

const handleViewOrder = () => {
  if (!currentSession.value?.orderNo) {
    ElMessage.warning('该会话暂无关联订单')
    return
  }
  router.push(`/orders/detail/${currentSession.value.orderNo}`)
}

onMounted(() => {
  fetchSessions()
})
</script>

<style lang="scss" scoped>
.customer-service-page {
  height: calc(100vh - 140px);
  
  .session-list {
    height: 100%;
    
    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
    }
    
    .session-items {
      height: calc(100% - 60px);
      overflow-y: auto;
      
      .session-item {
        display: flex;
        align-items: center;
        gap: 12px;
        padding: 12px;
        border-radius: 8px;
        cursor: pointer;
        transition: background 0.2s;
        
        &:hover {
          background: #f5f7fa;
        }
        
        &.active {
          background: #ecf5ff;
        }
        
        .session-info {
          flex: 1;
          min-width: 0;
          
          .session-name {
            font-weight: 500;
            color: #303133;
          }
          
          .session-last-msg {
            font-size: 12px;
            color: #909399;
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
          }
        }
        
        .session-meta {
          text-align: right;
          
          .session-time {
            font-size: 12px;
            color: #909399;
          }
        }
      }
    }
  }
  
  .chat-area {
    height: 100%;
    display: flex;
    flex-direction: column;
    
    .chat-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      
      .buyer-info {
        display: flex;
        align-items: center;
        gap: 12px;
        
        .buyer-name {
          font-weight: 500;
        }
        
        .buyer-platform {
          margin-top: 4px;
        }
      }
    }
    
    .messages-container {
      flex: 1;
      overflow-y: auto;
      padding: 20px;
      
      .message-item {
        display: flex;
        gap: 12px;
        margin-bottom: 20px;
        
        &.is-own {
          flex-direction: row-reverse;
          
          .message-content {
            align-items: flex-end;
          }
          
          .message-bubble {
            background: #409eff;
            color: #fff;
          }
        }
        
        .message-content {
          display: flex;
          flex-direction: column;
          max-width: 60%;
          
          .message-bubble {
            background: #f5f7fa;
            padding: 10px 14px;
            border-radius: 8px;
            line-height: 1.5;
          }
          
          .message-time {
            font-size: 12px;
            color: #909399;
            margin-top: 4px;
          }
        }
      }
    }
    
    .input-area {
      border-top: 1px solid #eee;
      padding-top: 15px;
      
      .quick-replies {
        margin-bottom: 10px;
        
        .quick-reply-tag {
          margin-right: 8px;
          cursor: pointer;
          
          &:hover {
            opacity: 0.8;
          }
        }
      }
      
      .input-box {
        .input-actions {
          display: flex;
          justify-content: flex-end;
          margin-top: 10px;
        }
      }
    }
  }
}
</style>