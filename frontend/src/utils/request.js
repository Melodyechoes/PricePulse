import axios from 'axios'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'

// 创建 axios 实例
const request = axios.create({
    baseURL: '/api',
    timeout: 10000
})

// 请求拦截器
request.interceptors.request.use(
    config => {
        console.log('=== [Request] 发送请求 ===')
        console.log('请求 URL:', config.url)
        console.log('请求方法:', config.method)
        console.log('请求参数:', config.params || config.data)

        const userStore = useUserStore()
        if (userStore.token) {
            config.headers['Authorization'] = `Bearer ${userStore.token}`
            console.log('已添加 Authorization header')
        } else {
            console.warn('未找到 token，用户可能未登录')
        }

        return config
    },
    error => {
        console.error('=== [Request] 请求错误 ===')
        console.error('错误详情:', error)
        return Promise.reject(error)
    }
)

// 响应拦截器
request.interceptors.response.use(
    response => {
        console.log('=== [Response] 收到响应 ===')
        console.log('请求 URL:', response.config.url)
        console.log('响应状态码:', response.status)
        console.log('响应数据:', response.data)

        const res = response.data

        // 如果返回的状态码不是 200，说明接口有错误
        if (res.code !== 200) {
            ElMessage.error(res.message || '请求失败')

            // 401: 未授权，需要重新登录
            if (res.code === 401) {
                const userStore = useUserStore()
                userStore.logout()
                window.location.reload()
            }

            return Promise.reject(new Error(res.message || '请求失败'))
        }

        return res
    },
    error => {
        console.error('=== [Response] 响应错误 ===')
        console.error('错误对象:', error)
        console.error('错误响应:', error.response)
        console.error('错误消息:', error.message)

        ElMessage.error(error.message || '网络错误')
        return Promise.reject(error)
    }
)

export default request
