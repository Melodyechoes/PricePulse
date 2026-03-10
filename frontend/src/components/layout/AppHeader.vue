<template>
  <el-header class="app-header">
    <div class="header-content">
      <div class="logo" @click="goHome">
        🔷 Price Pulse
      </div>

      <div class="nav-menu">
        <router-link to="/home" class="nav-item">首页</router-link>
        <router-link to="/dashboard" class="nav-item">数据统计</router-link>
        <router-link to="/products" class="nav-item">商品列表</router-link>
        <router-link to="/profile" class="nav-item">个人中心</router-link>
      </div>

      <div class="user-info">
        <!-- 通知铃铛图标 -->
        <el-badge :value="unreadCount" :hidden="unreadCount === 0" class="notification-bell-wrapper">
          <el-button text size="large" @click="goToNotifications">
            <el-icon><bell /></el-icon>
          </el-button>
        </el-badge>

        <el-dropdown>
          <span class="user-name">
            {{ userStore.userInfo?.username || '用户' }}
            <el-icon><arrow-down /></el-icon>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item @click="goToProfile">个人中心</el-dropdown-item>
              <el-dropdown-item divided @click="handleLogout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </div>
  </el-header>
</template>

<script setup>import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessageBox, ElNotification } from 'element-plus'
import { ArrowDown, Bell } from '@element-plus/icons-vue'
import request from '@/utils/request'
import wsClient from '@/utils/websocket'

const router = useRouter()
const userStore = useUserStore()
const unreadCount = ref(0)
let interval = null


// 加载未读数量
const loadUnreadCount = async () => {
  try {
    const res = await request.get('/notifications/unread-count', {
      params: { userId: userStore.userInfo?.id || 1 }
    })

    if (res.code === 200) {
      unreadCount.value = res.data?.count || 0
    }
  } catch (error) {
    console.error('加载未读数量失败:', error)
  }
}

// WebSocket 消息处理函数
const handleWebSocketMessage = (event) => {
  console.log('收到 WebSocket 通知:', event.detail)

  // 更新未读数量
  loadUnreadCount()

  // 可以在这里添加更多处理逻辑
  const { type, message } = event.detail

  // 显示 Element Plus 通知
  ElNotification({
    title: type === 'PRICE_DROP' ? '💰 降价通知' : '📦 到货通知',
    message: message,
    type: type === 'PRICE_DROP' ? 'success' : 'info',
    duration: 4000,
    position: 'bottom-right'
  })
}

// ✅ 在 await 之前注册 onUnmounted
onUnmounted(() => {
  if (interval) {
    clearInterval(interval)
  }

  // 断开 WebSocket 连接
  wsClient.disconnect()

  // 移除事件监听
  window.removeEventListener('websocket-notification', handleWebSocketMessage)
})

onMounted(async () => {
  await loadUnreadCount()

  // 每 5 分钟更新一次未读数量
  interval = setInterval(loadUnreadCount, 5 * 60 * 1000)

  // 连接 WebSocket
  wsClient.connect()

  // 监听 WebSocket 消息
  window.addEventListener('websocket-notification', handleWebSocketMessage)
})

const goHome = () => {
  router.push('/home')
}

const goToProfile = () => {
  router.push('/profile')
}

const goToNotifications = () => {
  router.push('/notifications')
}

const handleLogout = async () => {
  try {
    await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    userStore.logout()
    router.push('/login')
  } catch (error) {
    if (error !== 'cancel') {
      console.error('退出登录失败:', error)
    }
  }
}
</script>

<style scoped>
.app-header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 100%;
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 20px;
}

.logo {
  font-size: 24px;
  font-weight: bold;
  color: white;
  cursor: pointer;
  transition: opacity 0.3s;
}

.logo:hover {
  opacity: 0.8;
}

.nav-menu {
  display: flex;
  gap: 30px;
}

.nav-item {
  color: white;
  text-decoration: none;
  font-size: 16px;
  padding: 8px 16px;
  border-radius: 6px;
  transition: background 0.3s;
}

.nav-item:hover {
  background: rgba(255, 255, 255, 0.2);
}

.nav-item.router-link-active {
  background: rgba(255, 255, 255, 0.3);
}

.user-info {
  color: white;
  display: flex;
  align-items: center;
  gap: 15px;
}

/* 铃铛图标样式 */
.notification-bell-wrapper :deep(.el-button) {
  color: white !important;
  border: none !important;
  background: transparent !important;
  font-size: 20px !important;
  padding: 8px !important;
  transition: all 0.3s;
}

.notification-bell-wrapper :deep(.el-button):hover {
  background: rgba(255, 255, 255, 0.2) !important;
  transform: scale(1.1);
}

.notification-bell-wrapper :deep(.el-badge__content) {
  background-color: #ff4949 !important;
  border: 2px solid rgba(255, 255, 255, 0.5);
}

/* 用户名样式 */
.user-name {
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  padding: 8px 12px;
  border-radius: 6px;
  transition: all 0.3s;
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.3);
}

.user-name:hover {
  background: rgba(255, 255, 255, 0.25);
  border-color: rgba(255, 255, 255, 0.5);
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(255, 255, 255, 0.2);
}

.user-name :deep(.el-icon) {
  vertical-align: middle;
  margin-left: 4px;
}
</style>
