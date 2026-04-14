import request from './request'

// ==================== 售后订单管理 ====================

/**
 * 申请售后
 * @param {Object} data - 申请数据
 * @param {number} data.orderId - 订单ID
 * @param {string} data.orderNo - 订单编号
 * @param {string} data.type - 售后类型: refund_only/return_refund/exchange
 * @param {number} data.reasonId - 售后原因ID
 * @param {string} data.reasonDesc - 原因描述
 * @param {number} data.refundAmount - 退款金额
 * @param {string} data.refundReason - 退款原因
 * @param {string[]} data.evidenceImages - 凭证图片
 * @param {number} data.productId - 商品ID
 * @param {string} data.productName - 商品名称
 * @param {string} data.productSku - 商品SKU
 * @param {number} data.quantity - 商品数量
 * @param {string} data.exchangeSku - 换货SKU
 * @param {number} data.exchangeQuantity - 换货数量
 * @param {string} data.buyerRemark - 买家备注
 */
export function applyAfterSale(data) {
  return request({
    url: '/after-sales/apply',
    method: 'post',
    data
  })
}

/**
 * 获取售后列表
 * @param {Object} params - 查询参数
 * @param {number} params.pageNum - 页码
 * @param {number} params.pageSize - 每页数量
 * @param {string} params.status - 售后状态
 * @param {string} params.type - 售后类型
 * @param {string} params.orderNo - 订单编号
 * @param {string} params.afterSaleNo - 售后单号
 * @param {number} params.userId - 用户ID
 * @param {number} params.shopId - 店铺ID
 * @param {string} params.platform - 平台来源
 * @param {string} params.startTime - 开始时间
 * @param {string} params.endTime - 结束时间
 */
export function getAfterSaleList(params) {
  return request({
    url: '/after-sales',
    method: 'get',
    params
  })
}

/**
 * 获取售后详情
 * @param {number} id - 售后单ID
 */
export function getAfterSaleDetail(id) {
  return request({
    url: `/after-sales/${id}`,
    method: 'get'
  })
}

/**
 * 审批售后
 * @param {number} id - 售后单ID
 * @param {Object} data - 审批数据
 * @param {boolean} data.approved - 是否同意
 * @param {string} data.remark - 审批备注
 * @param {number} data.returnAddressId - 退货地址ID
 * @param {number} data.refundAmount - 调整后的退款金额
 */
export function approveAfterSale(id, data) {
  return request({
    url: `/after-sales/${id}/approve`,
    method: 'post',
    data
  })
}

/**
 * 填写退货物流
 * @param {number} id - 售后单ID
 * @param {Object} data - 物流信息
 * @param {string} data.logisticsCompany - 物流公司
 * @param {string} data.logisticsNo - 物流单号
 */
export function fillReturnLogistics(id, data) {
  return request({
    url: `/after-sales/${id}/return-logistics`,
    method: 'post',
    data
  })
}

/**
 * 确认收货(卖家确认收到退货)
 * @param {number} id - 售后单ID
 */
export function confirmReturnReceived(id) {
  return request({
    url: `/after-sales/${id}/confirm-received`,
    method: 'post'
  })
}

/**
 * 执行退款
 * @param {number} id - 售后单ID
 */
export function executeRefund(id) {
  return request({
    url: `/after-sales/${id}/refund`,
    method: 'post'
  })
}

/**
 * 换货发货
 * @param {number} id - 售后单ID
 * @param {Object} data - 发货信息
 * @param {string} data.logisticsCompany - 物流公司
 * @param {string} data.logisticsNo - 物流单号
 */
export function shipExchangeGoods(id, data) {
  return request({
    url: `/after-sales/${id}/exchange-ship`,
    method: 'post',
    data
  })
}

/**
 * 取消售后
 * @param {number} id - 售后单ID
 */
export function cancelAfterSale(id) {
  return request({
    url: `/after-sales/${id}/cancel`,
    method: 'post'
  })
}

/**
 * 添加卖家备注
 * @param {number} id - 售后单ID
 * @param {string} remark - 备注内容
 */
export function addSellerRemark(id, remark) {
  return request({
    url: `/after-sales/${id}/seller-remark`,
    method: 'post',
    params: { remark }
  })
}

// ==================== 售后原因 ====================

/**
 * 获取售后原因列表
 * @param {string} type - 类型: refund/return/exchange
 */
export function getAfterSaleReasons(type) {
  return request({
    url: '/after-sales/reasons',
    method: 'get',
    params: { type }
  })
}

// ==================== 售后统计 ====================

/**
 * 获取售后统计数据
 */
export function getAfterSaleStatistics() {
  return request({
    url: '/after-sales/statistics',
    method: 'get'
  })
}

// ==================== 退货地址 ====================

/**
 * 获取退货地址列表
 * @param {number} shopId - 店铺ID
 */
export function getReturnAddresses(shopId) {
  return request({
    url: '/after-sales/return-addresses',
    method: 'get',
    params: { shopId }
  })
}

// ==================== 常量定义 ====================

/**
 * 售后类型
 */
export const AFTER_SALE_TYPE = {
  REFUND_ONLY: 'refund_only',    // 仅退款
  RETURN_REFUND: 'return_refund', // 退货退款
  EXCHANGE: 'exchange'           // 换货
}

/**
 * 售后类型名称
 */
export const AFTER_SALE_TYPE_NAME = {
  [AFTER_SALE_TYPE.REFUND_ONLY]: '仅退款',
  [AFTER_SALE_TYPE.RETURN_REFUND]: '退货退款',
  [AFTER_SALE_TYPE.EXCHANGE]: '换货'
}

/**
 * 售后状态
 */
export const AFTER_SALE_STATUS = {
  PENDING: 'pending',         // 待审核
  APPROVED: 'approved',       // 已同意
  REJECTED: 'rejected',       // 已拒绝
  RETURNING: 'returning',     // 退货中
  RETURNED: 'returned',       // 已退货
  REFUNDING: 'refunding',     // 退款中
  EXCHANGING: 'exchanging',   // 换货中
  COMPLETED: 'completed',     // 已完成
  CANCELLED: 'cancelled'      // 已取消
}

/**
 * 售后状态名称
 */
export const AFTER_SALE_STATUS_NAME = {
  [AFTER_SALE_STATUS.PENDING]: '待审核',
  [AFTER_SALE_STATUS.APPROVED]: '已同意',
  [AFTER_SALE_STATUS.REJECTED]: '已拒绝',
  [AFTER_SALE_STATUS.RETURNING]: '退货中',
  [AFTER_SALE_STATUS.RETURNED]: '已退货',
  [AFTER_SALE_STATUS.REFUNDING]: '退款中',
  [AFTER_SALE_STATUS.EXCHANGING]: '换货中',
  [AFTER_SALE_STATUS.COMPLETED]: '已完成',
  [AFTER_SALE_STATUS.CANCELLED]: '已取消'
}

/**
 * 售后状态颜色(用于Element Plus Tag)
 */
export const AFTER_SALE_STATUS_COLOR = {
  [AFTER_SALE_STATUS.PENDING]: 'warning',
  [AFTER_SALE_STATUS.APPROVED]: 'primary',
  [AFTER_SALE_STATUS.REJECTED]: 'danger',
  [AFTER_SALE_STATUS.RETURNING]: 'primary',
  [AFTER_SALE_STATUS.RETURNED]: 'info',
  [AFTER_SALE_STATUS.REFUNDING]: 'primary',
  [AFTER_SALE_STATUS.EXCHANGING]: 'primary',
  [AFTER_SALE_STATUS.COMPLETED]: 'success',
  [AFTER_SALE_STATUS.CANCELLED]: 'info'
}

/**
 * 常用物流公司
 */
export const LOGISTICS_COMPANIES = [
  { code: 'SF', name: '顺丰速运' },
  { code: 'YTO', name: '圆通速递' },
  { code: 'ZTO', name: '中通快递' },
  { code: 'STO', name: '申通快递' },
  { code: 'YD', name: '韵达快递' },
  { code: 'JD', name: '京东物流' },
  { code: 'EMS', name: 'EMS' },
  { code: 'HTKY', name: '百世快递' },
  { code: 'JTSD', name: '极兔速递' },
  { code: 'DBKD', name: '德邦快递' }
]