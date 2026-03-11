import { useUserStore } from '@/stores/user'

class WebSocketClient {
    constructor() {
        this.ws = null
        this.reconnectTimer = null
        this.heartbeatTimer = null
        this.reconnectCount = 0
        this.maxReconnectCount = 5
        this.reconnectInterval = 3000 // 3 秒重连
        this.heartbeatInterval = 30000 // 30 秒心跳
    }

    /**
     * 连接 WebSocket
     */
    connect() {
        const userStore = useUserStore()
        const token = userStore.token

        if (!token) {
            console.warn('WebSocket 连接失败：未登录')
            return
        }

        try {
            // 构建 WebSocket URL
            const wsUrl = `ws://localhost:8080/ws?token=${token}`

            // 创建 WebSocket 连接
            this.ws = new WebSocket(wsUrl)

            // 连接成功
            this.ws.onopen = () => {
                console.log('✅ WebSocket 连接成功')
                this.reconnectCount = 0

                // 启动心跳
                this.startHeartbeat()
            }

            // 收到消息
            this.ws.onmessage = (event) => {
                console.log('📨 收到 WebSocket 消息:', event.data)

                try {
                    const message = JSON.parse(event.data)
                    this.handleMessage(message)
                } catch (error) {
                    console.error('解析 WebSocket 消息失败:', error)
                }
            }

            // 连接关闭
            this.ws.onclose = () => {
                console.warn('❌ WebSocket 连接关闭')
                this.stopHeartbeat()

                // 触发断连事件
                window.dispatchEvent(new CustomEvent('websocket-disconnected'))

                // 尝试重连
                this.scheduleReconnect()
            }

            // 连接错误
            this.ws.onerror = (error) => {
                console.error('WebSocket 错误:', error)
            }

        } catch (error) {
            console.error('创建 WebSocket 连接失败:', error)
        }
    }

    /**
     * 处理收到的消息
     */
    handleMessage(message) {
        const { type, message: msg } = message

        // 显示浏览器通知
        this.showBrowserNotification(type, msg)

        // 触发全局事件（供其他组件监听）
        window.dispatchEvent(new CustomEvent('websocket-notification', {
            detail: message
        }))

        // 播放提示音
        this.playNotificationSound()
    }

    /**
     * 显示浏览器通知
     */
    showBrowserNotification(type, message) {
        // 检查浏览器是否支持通知
        if (!('Notification' in window)) {
            console.warn('浏览器不支持通知')
            return
        }

        // 请求通知权限
        if (Notification.permission === 'granted') {
            this.createNotification(type, message)
        } else if (Notification.permission !== 'denied') {
            Notification.requestPermission().then(permission => {
                if (permission === 'granted') {
                    this.createNotification(type, message)
                }
            })
        }
    }

    /**
     * 创建通知
     */
    createNotification(type, message) {
        const title = type === 'PRICE_DROP' ? '💰 降价通知' : '📦 到货通知'

        const notification= new Notification(title, {
            body: message,
            icon: '/logo.png',
            tag: 'price-pulse',
            requireInteraction: false
        })

        // 点击通知时打开应用
        notification.onclick = () => {
            window.focus()
            notification.close()
        }

        // 5 秒后自动关闭
        setTimeout(() => notification.close(), 5000)
    }

    /**
     * 播放提示音
     */
    playNotificationSound() {
        // 创建一个简单的提示音
        const audioContext = new (window.AudioContext || window.webkitAudioContext)()
        const oscillator = audioContext.createOscillator()
        const gainNode = audioContext.createGain()

        oscillator.connect(gainNode)
        gainNode.connect(audioContext.destination)

        oscillator.frequency.value = 800
        oscillator.type = 'sine'

        gainNode.gain.setValueAtTime(0.3, audioContext.currentTime)
        gainNode.gain.exponentialRampToValueAtTime(0.01, audioContext.currentTime + 0.5)

        oscillator.start(audioContext.currentTime)
        oscillator.stop(audioContext.currentTime + 0.5)
    }

    /**
     * 启动心跳
     */
    startHeartbeat() {
        this.heartbeatTimer= setInterval(() => {
            if (this.ws && this.ws.readyState === WebSocket.OPEN) {
                this.ws.send(JSON.stringify({ type: 'ping' }))
            }
        }, this.heartbeatInterval)
    }

    /**
     * 停止心跳
     */
    stopHeartbeat() {
        if (this.heartbeatTimer) {
            clearInterval(this.heartbeatTimer)
            this.heartbeatTimer = null
        }
    }

    /**
     * 安排重连
     */
    scheduleReconnect() {
        if (this.reconnectCount >= this.maxReconnectCount) {
            console.error('WebSocket 重连次数已达上限')
            return
        }

        this.reconnectCount++
        console.log(`准备第 ${this.reconnectCount} 次重连...`)

        this.reconnectTimer = setTimeout(() => {
            console.log('开始重连...')
            this.connect()
        }, this.reconnectInterval)
    }

    /**
     * 断开连接
     */
    disconnect() {
        this.stopHeartbeat()

        if (this.reconnectTimer) {
            clearTimeout(this.reconnectTimer)
            this.reconnectTimer = null
        }

        if (this.ws) {
            this.ws.close()
            this.ws = null
        }
    }
}

// 导出单例
const wsClient = new WebSocketClient()
export default wsClient
