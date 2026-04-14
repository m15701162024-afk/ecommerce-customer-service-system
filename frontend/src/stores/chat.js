import { defineStore } from 'pinia'
import { getChatSessions, getChatHistory, sendMessage } from '@/api/chat'

export const useChatStore = defineStore('chat', {
  state: () => ({
    sessions: [],
    currentSession: null,
    messages: [],
    loading: false
  }),

  actions: {
    async fetchSessions() {
      try {
        const res = await getChatSessions()
        this.sessions = res.data || []
        if (this.sessions.length > 0 && !this.currentSession) {
          this.currentSession = this.sessions[0]
          await this.fetchMessages(this.currentSession.id)
        }
      } catch (error) {
        this.sessions = []
      }
    },

    async fetchMessages(sessionId) {
      try {
        const res = await getChatHistory(sessionId)
        this.messages = res.data || []
      } catch (error) {
        this.messages = []
      }
    },

    async sendMessageAction(sessionId, content) {
      try {
        const res = await sendMessage({ session_id: sessionId, message: content })
        this.messages.push({
          id: Date.now(),
          content,
          isOwn: true,
          createTime: new Date().toISOString()
        })
        if (res.data?.reply) {
          this.messages.push({
            id: Date.now() + 1,
            content: res.data.reply,
            isOwn: false,
            createTime: new Date().toISOString()
          })
        }
        return true
      } catch (error) {
        return false
      }
    },

    selectSession(session) {
      this.currentSession = session
      this.fetchMessages(session.id)
    }
  }
})