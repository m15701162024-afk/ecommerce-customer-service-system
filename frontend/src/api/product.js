import request from './request'

// 获取商品列表
export function getProductList(params) {
  return request({ url: '/products', method: 'get', params })
}

// 创建商品
export function createProduct(data) {
  return request({ url: '/products', method: 'post', data })
}

// 更新商品
export function updateProduct(productId, data) {
  return request({ url: `/products/${productId}`, method: 'put', data })
}

// 删除商品
export function deleteProduct(productId) {
  return request({ url: `/products/${productId}`, method: 'delete' })
}

// 匹配货源
export function matchSourceProduct(productId) {
  return request({ url: `/products/${productId}/match-source`, method: 'post' })
}

export function exportProducts(params) {
  return request({
    url: '/products/export',
    method: 'get',
    params,
    responseType: 'blob'
  })
}

export function importProducts(formData) {
  return request({
    url: '/products/import',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

export function downloadTemplate() {
  return request({
    url: '/products/template',
    method: 'get',
    responseType: 'blob'
  })
}