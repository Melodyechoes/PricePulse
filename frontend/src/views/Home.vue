<!-- eslint-disable vue/multi-word-component-names -->
<template>
  <MainLayout>
    <div class="home-container">
      <!-- 欢迎区域 -->
      <div class="welcome-section">
        <h1 class="welcome-title">🎯 Price Pulse - 智能价格监控</h1>
        <p class="welcome-subtitle">实时监控 · 自动提醒 · 省钱购物</p>

        <div class="stats-overview">
          <el-row :gutter="20">
            <el-col :span="6">
              <div class="stat-item">
                <div class="stat-icon" style="background: linear-gradient(135deg, #667eea 0%, #764ba2 100%)">
                  <el-icon><shopping-cart /></el-icon>
                </div>
                <div class="stat-info">
                  <div class="stat-value">{{ stats.productCount }}</div>
                  <div class="stat-label">商品总数</div>
                </div>
              </div>
            </el-col>

            <el-col :span="6">
              <div class="stat-item">
                <div class="stat-icon" style="background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%)">
                  <el-icon><trend-charts /></el-icon>
                </div>
                <div class="stat-info">
                  <div class="stat-value">{{ stats.priceDropToday }}</div>
                  <div class="stat-label">今日降价</div>
                </div>
              </div>
            </el-col>

            <el-col :span="6">
              <div class="stat-item">
                <div class="stat-icon" style="background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)">
                  <el-icon><bell /></el-icon>
                </div>
                <div class="stat-info">
                  <div class="stat-value">{{ stats.notificationCount }}</div>
                  <div class="stat-label">未读通知</div>
                </div>
              </div>
            </el-col>

            <el-col :span="6">
              <div class="stat-item">
                <div class="stat-icon" style="background: linear-gradient(135deg, #fa709a 0%, #fee140 100%)">
                  <el-icon><clock /></el-icon>
                </div>
                <div class="stat-info">
                  <div class="stat-value">30min</div>
                  <div class="stat-label">更新频率</div>
                </div>
              </div>
            </el-col>
          </el-row>
        </div>
      </div>

      <!-- 快捷入口 -->
      <div class="quick-actions">
        <h2 class="section-title">⚡ 快捷操作</h2>
        <el-row :gutter="20">
          <el-col :span="6">
            <el-card shadow="hover" class="action-card" @click="$router.push('/products')">
              <div class="action-icon">🛒</div>
              <div class="action-text">浏览商品</div>
            </el-card>
          </el-col>

          <el-col :span="6">
            <el-card shadow="hover" class="action-card" @click="$router.push('/dashboard')">
              <div class="action-icon">📊</div>
              <div class="action-text">数据统计</div>
            </el-card>
          </el-col>

          <el-col :span="6">
            <el-card shadow="hover" class="action-card" @click="$router.push('/notifications')">
              <div class="action-icon">🔔</div>
              <div class="action-text">消息通知</div>
            </el-card>
          </el-col>

          <el-col :span="6">
            <el-card shadow="hover" class="action-card" @click="$router.push('/profile')">
              <div class="action-icon">👤</div>
              <div class="action-text">个人中心</div>
            </el-card>
          </el-col>
        </el-row>
      </div>

      <!-- 功能特性 -->
      <div class="features-section">
        <h2 class="section-title">✨ 核心功能</h2>
        <el-row :gutter="20">
          <el-col :span="8">
            <el-card class="feature-card">
              <div class="feature-icon">🕷️</div>
              <h3>自动爬虫</h3>
              <p>支持京东/淘宝/拼多多三大平台，自动抓取商品价格</p>
            </el-card>
          </el-col>

          <el-col :span="8">
            <el-card class="feature-card">
              <div class="feature-icon">⏰</div>
              <h3>定时更新</h3>
              <p>每 30 分钟自动更新价格</p>
            </el-card>
          </el-col>

          <el-col :span="8">
            <el-card class="feature-card">
              <div class="feature-icon">🔔</div>
              <h3>实时通知</h3>
              <p>价格下降立即进行站内通知</p>
            </el-card>
          </el-col>
        </el-row>
      </div>
    </div>
  </MainLayout>
</template>

<script setup>
import MainLayout from '@/components/layout/MainLayout.vue'
import { ref, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { ShoppingCart, TrendCharts, Bell, Clock } from '@element-plus/icons-vue'
import request from '@/utils/request'

const userStore = useUserStore()

const stats = ref({
  productCount: 0,
  priceDropToday: 0,
  notificationCount: 0
})

onMounted(async () => {
  await loadStats()
})

const loadStats = async () => {
  try {
    const userId = userStore.userInfo?.id || 1

    // 获取商品总数
    const productsRes = await request.get('/products')
    if (productsRes.code === 200) {
      stats.value.productCount = productsRes.data?.length || 0
    }

    // 获取通知统计
    const notifRes = await request.get('/dashboard/notification-stats', {
      params: { userId }
    })
    if (notifRes.code === 200) {
      stats.value.priceDropToday = notifRes.data?.todayCount || 0
    }

    // 获取未读通知数
    const unreadRes = await request.get('/notifications/unread-count', {
      params: { userId }
    })
    if (unreadRes.code === 200) {
      stats.value.notificationCount = unreadRes.data?.count || 0
    }
  } catch (error) {
    console.error('加载统计数据失败:', error)
  }
}
</script>

<style scoped>
.home-container {
  min-height: 100vh;
  background: #f5f7fa;
  padding: 20px;
}

.welcome-section {
  margin-bottom: 30px;
  padding: 40px 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 16px;
  color: white;
}

.welcome-title {
  font-size: 36px;
  margin-bottom: 10px;
  text-align: center;
}

.welcome-subtitle {
  font-size: 16px;
  text-align: center;
  opacity: 0.9;
  margin-bottom: 30px;
}

.stats-overview {
  max-width: 1200px;
  margin: 0 auto;
}

.stat-item {
  display: flex;
  align-items: center;
  padding: 20px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 12px;
  backdrop-filter: blur(10px);
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 15px;
}

.stat-icon .el-icon {
  font-size: 28px;
  color: white;
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  margin-bottom: 5px;
}

.stat-label {
  font-size: 14px;
  opacity: 0.8;
}

.section-title {
  font-size: 24px;
  margin-bottom: 20px;
  color: #333;
}

.quick-actions {
  margin-bottom: 30px;
}

.action-card {
  text-align: center;
  padding: 30px 20px;
  cursor: pointer;
  transition: all 0.3s;
  border-radius: 12px;
}

.action-card:hover {
  transform: translateY(-5px);
}

.action-icon {
  font-size: 48px;
  margin-bottom: 10px;
}

.action-text {
  font-size: 16px;
  color: #666;
}

.features-section {
  margin-bottom: 30px;
}

.feature-card {
  text-align: center;
  padding: 30px 20px;
  border-radius: 12px;
}

.feature-icon {
  font-size: 48px;
  margin-bottom: 15px;
}

.feature-card h3 {
  font-size: 18px;
  margin-bottom: 10px;
  color: #333;
}

.feature-card p {
  font-size: 14px;
  color: #666;
  line-height: 1.6;
}
</style>
