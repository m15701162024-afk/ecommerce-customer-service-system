import request from './request'

// 获取聊天会话
export function getChatSessions(params) {
  return request({ url: '/chat/sessions', method: 'get', params })
}

// 获取聊天历史
export function getChatHistory(sessionId, params) {
  return request({ url: `/chat/sessions/${sessionId}/history`, method: 'get', params })
}

// 发送消息
export function sendMessage(data) {
  return request({ url: '/chat/send', method: 'post', data })
}

// 转人工客服
export function transferToHuman(sessionId, data) {
  return request({ url: `/chat/sessions/${sessionId}/transfer`, method: 'post', data })
}

// 获取知识库列表
export function getKnowledgeList(params) {
  return request({ url: '/knowledge', method: 'get', params })
}

// 创建知识
export function createKnowledge(data) {
  return request({ url: '/knowledge', method: 'post', data })
}

// 更新知识
export function updateKnowledge(id, data) {
  return request({ url: `/knowledge/${id}`, method: 'put', data })
}

// 删除知识
export function deleteKnowledge(id) {
  return request({ url: `/knowledge/${id}`, method: 'delete' })
}

// 获取待审核知识列表
export function getPendingKnowledge(params) {
  return request({ url: '/knowledge/pending', method: 'get', params })
}

// 审核知识（通过/拒绝）
export function auditKnowledge(id, data) {
  return request({ url: `/knowledge/${id}/audit`, method: 'post', data })
}

// 获取审核历史
export function getAuditHistory(id) {
  return request({ url: `/knowledge/${id}/audit-history`, method: 'get' })
}