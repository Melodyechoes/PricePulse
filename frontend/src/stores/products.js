import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getProductList, getProductDetail } from '@/api/products'

export const useProductsStore = defineStore('products', () => {
    const productList = ref([])
    const currentProduct = ref(null)
    const loading = ref(false)
    const pagination = ref({
        page: 1,
        size: 10,
        total: 0
    })

    // 获取商品列表
    async function fetchProducts(params) {
        loading.value = true
        console.log('=== 开始获取商品列表 ===')
        console.log('请求参数:', params)

        try {
            const res = await getProductList(params)

            console.log('=== API 响应 ===')
            console.log('原始响应数据:', res)
            console.log('res 是否为数组:', Array.isArray(res))
            console.log('res.data:', res?.data)

            // 后端返回的 res 已经是 Result 中的 data 字段（商品数组）
            const dataList = Array.isArray(res) ? res : (res?.data || [])

            console.log('解析后的商品数据:', dataList)
            console.log('商品数量:', dataList.length)

            productList.value = dataList
            pagination.value = {
                page: params?.page || 1,
                size: params?.size || 10,
                total: dataList.length
            }

            console.log('=== 更新完成 ===')
            console.log('商品列表:', productList.value)
            console.log('分页信息:', pagination.value)

            return res
        } catch (error) {
            console.error('=== 获取商品列表失败 ===')
            console.error('错误信息:', error)
            throw error
        } finally {
            loading.value = false
            console.log('=== 加载结束 ===')
        }
    }

    // 获取商品详情
    async function fetchProductDetail(id) {
        loading.value = true
        try {
            const res = await getProductDetail(id)
            currentProduct.value = res.data
            return res
        } finally {
            loading.value = false
        }
    }

    // 清空当前商品
    function clearCurrentProduct() {
        currentProduct.value = null
    }

    return {
        productList,
        currentProduct,
        loading,
        pagination,
        fetchProducts,
        fetchProductDetail,
        clearCurrentProduct
    }
})
