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
    console.log('=== [API] 调用 followProduct ===')
    console.log('productId:', productId)
    console.log('data:', data)

    // 从 localStorage 获取用户信息
    const userInfo = localStorage.getItem('userInfo')
    console.log('localStorage userInfo:', userInfo)

    let userId = 1

    if (userInfo) {
        try {
            const userData = JSON.parse(userInfo)
            userId = userData.id || 1
            console.log('解析后的 userId:', userId)
        } catch (e) {
            console.error('解析用户信息失败:', e)
        }
    }

    console.log('最终使用的 userId:', userId)
    console.log('请求配置:', {
        url: '/user-products',
        method: 'post',
        data: {
            userId: userId,
            productId: productId,
            alertThreshold: data?.alertThreshold || 0.1
        }
    })

    return request({
        url: '/user-products',
        method: 'post',
        data: {
            userId: userId,
            productId: productId,
            alertThreshold: data?.alertThreshold || 0.1
        }
    })
}

// 取消关注商品
export function unfollowProduct(productId) {
    console.log('=== [API] 调用 unfollowProduct ===')
    console.log('productId:', productId)

    // 从 localStorage 获取用户信息
    const userInfo = localStorage.getItem('userInfo')
    console.log('localStorage userInfo:', userInfo)

    let userId = 1

    if (userInfo) {
        try {
            const userData = JSON.parse(userInfo)
            userId = userData.id || 1
            console.log('解析后的 userId:', userId)
        } catch (e) {
            console.error('解析用户信息失败:', e)
        }
    }

    console.log('最终使用的 userId:', userId)

    return request({
        url: '/user-products',
        method: 'delete',
        params: {
            userId: userId,
            productId: productId
        }
    })
}

// 获取用户关注的商品列表
export function getUserFollowedProducts(userId) {
    return request({
        url: `/user-products/user/${userId}`,
        method: 'get'
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


