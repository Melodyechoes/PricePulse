<template>
  <el-header class="app-header">
    <div class="header-content">
      <div class="logo" @click="goHome">
        🔷 Price Pulse
      </div>

      <div class="nav-menu">
        <router-link to="/home" class="nav-item">首页</router-link>
        <router-link to="/products" class="nav-item">商品列表</router-link>
        <router-link to="/profile" class="nav-item">个人中心</router-link>
      </div>

      <div class="user-info">
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

<script setup>
import { useRouter} from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessageBox } from 'element-plus'
import { ArrowDown } from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()
const goHome = () => {
  router.push('/home')
}

const goToProfile = () => {
  router.push('/profile')
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
}

.user-name {
  cursor: pointer;
  font-size: 14px;
  padding: 8px 12px;
  border-radius: 6px;
  transition: background 0.3s;
}

.user-name:hover {
  background: rgba(255, 255, 255, 0.2);
}
</style>
