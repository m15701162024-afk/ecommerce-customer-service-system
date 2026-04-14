import request from './request'

/**
 * 获取第三方平台授权URL
 * @param {string} platform - 平台名称: douyin | xiaohongshu | 1688
 * @returns {Promise<{authorizeUrl: string}>}
 */
export function getAuthorizeUrl(platform) {
  return request({
    url: `/oauth/authorize/${platform}`,
    method: 'get'
  })
}

/**
 * 绑定第三方平台账号
 * @param {string} platform - 平台名称: douyin | xiaohongshu | 1688
 * @param {string} code - OAuth授权码
 * @param {string} state - 状态参数(可选)
 * @returns {Promise<{success: boolean, message: string}>}
 */
export function bindAccount(platform, code, state = '') {
  return request({
    url: `/oauth/bind/${platform}`,
    method: 'post',
    data: { code, state }
  })
}

/**
 * 解绑第三方平台账号
 * @param {string} platform - 平台名称: douyin | xiaohongshu | 1688
 * @returns {Promise<{success: boolean, message: string}>}
 */
export function unbindAccount(platform) {
  return request({
    url: `/oauth/unbind/${platform}`,
    method: 'post'
  })
}

/**
 * 获取所有第三方平台绑定状态
 * @returns {Promise<{bindings: Array<{platform: string, bound: boolean, bindTime: string, accountName: string}>}>}
 */
export function getBindingStatus() {
  return request({
    url: '/oauth/bindings',
    method: 'get'
  })
}

/**
 * 平台配置信息
 */
export const PLATFORMS = {
  douyin: {
    name: '抖音',
    icon: 'VideoCamera',
    color: '#000000',
    description: '绑定抖音小店账号，同步商品和订单数据'
  },
  xiaohongshu: {
    name: '小红书',
    icon: 'Picture',
    color: '#ff2442',
    description: '绑定小红书账号，管理笔记和商品'
  },
  '1688': {
    name: '1688',
    icon: 'Shop',
    color: '#ff6a00',
    description: '绑定1688账号，同步采购订单和商品信息'
  }
}