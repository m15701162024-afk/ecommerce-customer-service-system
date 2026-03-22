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
                @keyup.ctrl.enter="sendMessage"
              />
              <div class="input-actions">
                <el-button type="primary" @click="sendMessage">发送</el-button>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const searchKeyword = ref('')
const unreadCount = ref(12)
const inputMessage = ref('')
const currentSession = ref(null)
const messagesRef = ref(null)

const sessions = ref([
  { id: 1, buyerName: '张三', avatar: 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png', lastMessage: '这个商品有货吗？', time: '10:30', unread: 2, platform: '抖音' },
  { id: 2, buyerName: '李四', avatar: 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png', lastMessage: '什么时候发货？', time: '09:45', unread: 1, platform: '淘宝' },
  { id: 3, buyerName: '王五', avatar: 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png', lastMessage: '好的，谢谢', time: '昨天', unread: 0, platform: '小红书' },
  { id: 4, buyerName: '赵六', avatar: 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png', lastMessage: '可以优惠吗？', time: '昨天', unread: 0, platform: '抖音' }
])

const messages = ref([
  { id: 1, content: '您好，请问这个商品有货吗？', isOwn: false, avatar: 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png', time: '10:28' },
  { id: 2, content: '您好！该商品目前有现货，您下单后我们会在24小时内发货哦~', isOwn: true, avatar: '', time: '10:28' },
  { id: 3, content: '好的，那我可以拍了吗？', isOwn: false, avatar: 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png', time: '10:29' },
  { id: 4, content: '当然可以！现在下单还有新人优惠券可以领取哦，满100减20，非常划算！', isOwn: true, avatar: '', time: '10:30' }
])

const quickReplies = ref([
  '您好，有什么可以帮助您的？',
  '商品有现货，24小时内发货',
  '感谢您的支持！',
  '请稍等，我帮您查询一下',
  '这是我们的优惠活动'
])

const getPlatformType = (platform) => {
  const types = { '抖音': 'danger', '淘宝': 'warning', '小红书': 'success' }
  return types[platform] || 'info'
}

const selectSession = (session) => {
  currentSession.value = session
  session.unread = 0
}

const sendMessage = () => {
  if (!inputMessage.value.trim()) return
  messages.value.push({
    id: Date.now(),
    content: inputMessage.value,
    isOwn: true,
    avatar: '',
    time: new Date().toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
  })
  inputMessage.value = ''
}

const sendQuickReply = (reply) => {
  inputMessage.value = reply
  sendMessage()
}

const handleTransferToHuman = () => {
  console.log('转人工客服')
}

const handleViewOrder = () => {
  console.log('查看订单')
}

if (sessions.value.length > 0) {
  currentSession.value = sessions.value[0]
}
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