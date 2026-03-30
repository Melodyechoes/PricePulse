import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import App from './App.vue'
import router from './router'

const app = createApp(App)
const pinia = createPinia()

// 注册所有图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
    app.component(key, component)
}

// 全局错误处理
app.config.errorHandler = (err, vm, info) => {
    if (err.message.includes('ResizeObserver')) {
        // 忽略 ResizeObserver 相关的错误
        return
    }
    console.error('应用错误:', err, info)
}

app.use(pinia)
app.use(router)
app.use(ElementPlus)

// 初始化用户信息（从 localStorage 恢复）
import { useUserStore } from '@/stores/user'
const userStore = useUserStore(pinia)
userStore.initUserInfo()

// 【新增】初始化 WebSocket
import wsClient from '@/utils/websocket'
// 延迟 2 秒连接，确保用户信息已加载
setTimeout(() => {
    if (userStore.token) {
        console.log('初始化 WebSocket 连接...')
        wsClient.connect()
    } else {
        console.warn('未登录，跳过 WebSocket 连接')
    }
}, 2000)

app.mount('#app')
