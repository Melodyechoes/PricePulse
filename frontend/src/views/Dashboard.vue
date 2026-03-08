<!-- eslint-disable vue/multi-word-component-names -->
<template>
  <MainLayout>
    <div class="dashboard-container">
      <div class="dashboard-header">
        <h1>📊 数据统计</h1>
      </div>

      <!-- 统计卡片 -->
      <el-row :gutter="20" class="stats-cards">
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-icon" style="background: linear-gradient(135deg, #667eea 0%, #764ba2 100%)">
              <el-icon><shopping-cart /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-value">{{ stats.followedCount }}</div>
              <div class="stat-label">关注商品</div>
            </div>
          </el-card>
        </el-col>

        <el-col :span="6">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-icon" style="background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%)">
              <el-icon><trend-charts /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-value">{{ stats.priceDropCount }}</div>
              <div class="stat-label">今日降价</div>
            </div>
          </el-card>
        </el-col>

        <el-col :span="6">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-icon" style="background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)">
              <el-icon><money /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-value">¥{{ stats.savedAmount }}</div>
              <div class="stat-label">预计省钱</div>
            </div>
          </el-card>
        </el-col>

        <el-col :span="6">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-icon" style="background: linear-gradient(135deg, #fa709a 0%, #fee140 100%)">
              <el-icon><bell-filled /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-value">{{ stats.unreadCount }}</div>
              <div class="stat-label">未读通知</div>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <!-- 图表区域 -->
      <el-row :gutter="20">
        <!-- 价格趋势图 -->
        <el-col :span="16">
          <el-card>
            <template #header>
              <div class="card-header">
                <span>📈 价格趋势</span>
                <el-radio-group v-model="trendPeriod" size="small" @change="loadPriceTrend">
                  <el-radio-button label="7 天" value="7" />
                  <el-radio-button label="30 天" value="30" />
                  <el-radio-button label="90 天" value="90" />
                </el-radio-group>
              </div>
            </template>
            <div v-loading="trendLoading" style="height: 400px;">
              <v-chart v-if="trendOption" :option="trendOption" autoresize />
            </div>
          </el-card>
        </el-col>

        <!-- 分类占比 -->
        <el-col :span="8">
          <el-card>
            <template #header>
              <span>📊 分类分布</span>
            </template>
            <div v-loading="categoryLoading" style="height: 400px;">
              <v-chart v-if="categoryOption" :option="categoryOption" autoresize />
            </div>
          </el-card>
        </el-col>
      </el-row>

      <!-- 降价排行榜 -->
      <el-row :gutter="20" style="margin-top: 20px;">
        <el-col :span="24">
          <el-card>
            <template #header>
              <span>🔥 降价排行榜 Top 10</span>
            </template>
            <el-table :data="priceDropRanking" style="width: 100%" v-loading="rankingLoading">
              <el-table-column type="index" label="排名" width="60" />
              <el-table-column prop="productName" label="商品名称" />
              <el-table-column prop="originalPrice" label="原价" width="100">
                <template #default="{ row }">
                  ¥{{ row.originalPrice }}
                </template>
              </el-table-column>
              <el-table-column prop="currentPrice" label="现价" width="100">
                <template #default="{ row }">
                  ¥{{ row.currentPrice }}
                </template>
              </el-table-column>
              <el-table-column prop="dropPercent" label="降幅" width="100">
                <template #default="{ row }">
                  <el-tag type="danger">{{ row.dropPercent }}%</el-tag>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="100">
                <template #default="{ row }">
                  <el-button size="small" @click="viewProduct(row.productId)">
                    查看详情
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
          </el-card>
        </el-col>
      </el-row>
    </div>
  </MainLayout>
</template>

<script setup>
import MainLayout from '@/components/layout/MainLayout.vue'
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { TrendCharts, Money, BellFilled } from '@element-plus/icons-vue'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { LineChart, PieChart, BarChart } from 'echarts/charts'
import { TitleComponent, TooltipComponent, GridComponent, LegendComponent } from 'echarts/components'
import VChart from 'vue-echarts'
import * as echarts from 'echarts'
import request from '@/utils/request'

use([CanvasRenderer, LineChart, PieChart, BarChart, TitleComponent, TooltipComponent, GridComponent, LegendComponent])

const router = useRouter()
const userStore = useUserStore()

// 统计数据
const stats = ref({
  followedCount: 0,
  priceDropCount: 0,
  savedAmount: 0,
  unreadCount: 0
})

// 价格趋势
const trendPeriod = ref('7')
const trendLoading = ref(false)
const trendOption = ref(null)

// 分类占比
const categoryLoading = ref(false)
const categoryOption = ref(null)

// 降价排行榜
const rankingLoading = ref(false)
const priceDropRanking = ref([])

onMounted(async () => {
  await loadStats()
  await loadPriceTrend()
  await loadCategoryDistribution()
  await loadPriceDropRanking()
})

// 加载统计数据
const loadStats = async () => {
  try {
    const userId = userStore.userInfo?.id || 1

    const res = await request.get('/dashboard/stats', {
      params: { userId }
    })

    if (res.code === 200) {
      stats.value = res.data || {}
    }
  } catch (error) {
    console.error('加载统计数据失败:', error)
  }
}

// 加载价格趋势
const loadPriceTrend = async () => {
  trendLoading.value = true
  try {
    const userId = userStore.userInfo?.id || 1
    const res = await request.get('/dashboard/price-trend', {
      params: {
        userId,
        days: parseInt(trendPeriod.value)
      }
    })

    if (res.code === 200) {
      const data = res.data || []

      if (data.length === 0) {
        trendOption.value = null
        return
      }

      const dates = data.map(item => item.date)
      const prices = data.map(item => item.price)

      trendOption.value = {
        title: {
          text: '关注商品平均价格走势',
          left: 'center',
          textStyle: {
            fontSize: 16,
            fontWeight: 'bold'
          }
        },
        tooltip: {
          trigger: 'axis',
          formatter: '{b}<br />平均价格：¥{c}'
        },
        grid: {
          left: '3%',
          right: '4%',
          bottom: '3%',
          containLabel: true
        },
        xAxis: {
          type: 'category',
          data: dates,
          axisLabel: {
            rotate: 45
          }
        },
        yAxis: {
          type: 'value',
          name: '价格 (元)',
          axisLabel: {
            formatter: '¥{value}'
          }
        },
        series: [{
          data: prices,
          type: 'line',
          smooth: true,
          areaStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: 'rgba(64, 158, 255, 0.3)' },
              { offset: 1, color: 'rgba(64, 158, 255, 0.05)' }
            ])
          },
          lineStyle: {
            color: '#409EFF',
            width: 3
          },
          itemStyle: {
            color: '#409EFF'
          }
        }]
      }
    }
  } catch (error) {
    console.error('加载价格趋势失败:', error)
  } finally {
    trendLoading.value = false
  }
}
// 加载分类分布
const loadCategoryDistribution = async () => {
  categoryLoading.value = true
  try {
    const userId = userStore.userInfo?.id || 1
    const res = await request.get('/dashboard/category-distribution', {
      params: { userId }
    })

    console.log('=== 分类分布 API 响应 ===')
    console.log('响应数据:', res.data)

    if (res.code === 200) {
      const categories = res.data || []

      console.log('分类数据:', categories)

      if (categories.length === 0) {
        categoryOption.value = null
        return
      }

      categoryOption.value = {
        title: {
          text: '关注商品分类',
          left: 'center',
          textStyle: {
            fontSize: 16,
            fontWeight: 'bold'
          }
        },
        tooltip: {
          trigger: 'item',
          formatter: '{b}: {c} ({d}%)'
        },
        legend: {
          orient: 'vertical',
          left: 'left',
          top: 'middle'
        },
        series: [{
          type: 'pie',
          radius: ['40%', '70%'],
          center: ['55%', '50%'],
          data: categories,
          emphasis: {
            itemStyle: {
              shadowBlur: 10,
              shadowOffsetX: 0,
              shadowColor: 'rgba(0, 0, 0, 0.5)'
            }
          },
          label: {
            formatter: '{b}: {d}%'
          }
        }]
      }
    }
  } catch (error) {
    console.error('加载分类分布失败:', error)
    console.error('错误详情:', error.response?.data)
  } finally {
    categoryLoading.value = false
  }
}
// 加载降价排行榜
const loadPriceDropRanking = async () => {
  rankingLoading.value = true
  try {
    const userId = userStore.userInfo?.id || 1
    const res = await request.get('/dashboard/price-drop-ranking', {
      params: {
        userId,
        limit: 10
      }
    })

    if (res.code === 200) {
      priceDropRanking.value = res.data || []
    }
  } catch (error) {
    console.error('加载降价排行榜失败:', error)
  } finally {
    rankingLoading.value = false
  }
}
const viewProduct = (productId) => {
  router.push(`/product/${productId}`)
}
</script>

<style scoped>
.dashboard-container {
  min-height: 100vh;
  background: #f5f7fa;
  padding: 20px;
}

.dashboard-header {
  margin-bottom: 20px;
  padding: 20px;
  background: white;
  border-radius: 12px;
}

.dashboard-header h1 {
  font-size: 28px;
  color: #333;
}

.stats-cards {
  margin-bottom: 20px;
}

.stat-card {
  display: flex;
  align-items: center;
  padding: 20px;
  border-radius: 12px;
  transition: transform 0.3s;
}

.stat-card:hover {
  transform: translateY(-5px);
}

.stat-icon {
  width: 80px;
  height: 80px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 20px;
}

.stat-icon .el-icon {
  font-size: 36px;
  color: white;
}

.stat-content {
  flex: 1;
}

.stat-value {
  font-size: 32px;
  font-weight: bold;
  color: #333;
  margin-bottom: 5px;
}

.stat-label {
  font-size: 14px;
  color: #999;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
