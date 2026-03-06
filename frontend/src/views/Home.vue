<!-- eslint-disable vue/multi-word-component-names -->
<template>
  <MainLayout>
  <div class="home-container">
    <div class="home-header">
      <h1>欢迎使用 Price Pulse</h1>
      <p>智能价格监控系统 - 让您不再错过任何一次降价</p>
    </div>

    <div class="home-content">
      <el-row :gutter="20">
        <el-col :xs="24" :sm="12" :md="8">
          <el-card class="feature-card" shadow="hover">
            <div class="feature-icon">🛍️</div>
            <h3>商品管理</h3>
            <p>添加您感兴趣的商品，支持淘宝、京东等多个平台</p>
            <router-link to="/products">
              <el-button type="primary" plain>查看商品</el-button>
            </router-link>
          </el-card>
        </el-col>

        <el-col :xs="24" :sm="12" :md="8">
          <el-card class="feature-card" shadow="hover">
            <div class="feature-icon">📊</div>
            <h3>价格监控</h3>
            <p>自动跟踪商品价格变化，每小时更新一次</p>
            <el-button type="primary" plain disabled>即将上线</el-button>
          </el-card>
        </el-col>

        <el-col :xs="24" :sm="12" :md="8">
          <el-card class="feature-card" shadow="hover">
            <div class="feature-icon">🔔</div>
            <h3>降价提醒</h3>
            <p>商品价格下降时，第一时间通知您</p>
            <el-button type="primary" plain disabled>即将上线</el-button>
          </el-card>
        </el-col>
      </el-row>

      <div class="quick-actions">
        <h2>快速操作</h2>
        <el-space wrap>
          <router-link to="/products">
            <el-button type="primary" size="large">浏览商品</el-button>
          </router-link>
          <el-button size="large" @click="handleLogout">退出登录</el-button>
        </el-space>
      </div>
    </div>
  </div>
  </MainLayout>
</template>

<script setup>import MainLayout from '@/components/layout/MainLayout.vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessageBox } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()

const handleLogout = async () => {
  try {
    await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    userStore.logout()
    router.push('/login')
  } catch {
    // 取消退出
  }
}
</script>

<style scoped>
.home-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 40px 20px;
}

.home-header {
  text-align: center;
  color: white;
  margin-bottom: 40px;
}

.home-header h1 {
  font-size: 48px;
  margin-bottom: 10px;
}

.home-header p {
  font-size: 18px;
  opacity: 0.9;
}

.home-content {
  max-width: 1200px;
  margin: 0 auto;
}

.feature-card {
  text-align: center;
  padding: 30px 20px;
  margin-bottom: 20px;
  border-radius: 12px;
}

.feature-icon {
  font-size: 64px;
  margin-bottom: 20px;
}

.feature-card h3 {
  font-size: 24px;
  color: #333;
  margin-bottom: 10px;
}

.feature-card p {
  color: #666;
  margin-bottom: 20px;
  line-height: 1.6;
}

.quick-actions {
  text-align: center;
  margin-top: 40px;
  padding: 30px;
  background: white;
  border-radius: 12px;
}

.quick-actions h2 {
  font-size: 28px;
  color: #333;
  margin-bottom: 20px;
}
</style>
