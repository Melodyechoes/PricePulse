import request from '@/utils/request'


// 获取商品列表
export function getProductList(params) {
    return request({
        url: '/products',
        method: 'get',
        params
    })
}

// ... existing code ...



// 获取商品详情
export function getProductDetail(id) {
    return request({
        url: `/products/${id}`,
        method: 'get'
    })
}

// 关注商品
export function followProduct(productId, data) {
    return request({
        url: '/user-products',
        method: 'post',
        data: {
            userId: 1, // TODO: 从用户状态中获取
            productId: productId,
            alertThreshold: data?.alertThreshold || 0.1
        }
    })
}

// 取消关注商品
export function unfollowProduct(productId) {
    return request({
        url: '/user-products',
        method: 'delete',
        params: {
            userId: 1, // TODO: 从用户状态中获取
            productId: productId
        }
    })
}

// 获取用户关注的商品列表
export function getUserFollowedProducts(params) {
    return request({
        url: '/user-products/my-follows',
        method: 'get',
        params
    })
}

// 添加商品
export function addProduct(data) {
    return request({
        url: '/products',
        method: 'post',
        data
    })
}

// 获取价格历史记录
export function getPriceHistory(productId) {
    return request({
        url: `/products/${productId}/price-history`,
        method: 'get'
    })
}


