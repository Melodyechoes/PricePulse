<!-- eslint-disable vue/multi-word-component-names -->
<template>
  <MainLayout>
    <div class="dashboard-container">
      <div class="dashboard-header">
        <h1>📊 数据统计</h1>
      </div>

      <!-- 统计卡片 -->
      <el-row :gutter="20" class="stats-cards">
        <el-col :span="8">
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

        <el-col :span="8">
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

        <el-col :span="8">
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
          <el-card class="stat-card trend-card">
            <div class="card-header">
              <h3>📈 价格趋势</h3>
              <div style="display: flex; gap: 10px; align-items: center;">
                <el-select
                    v-model="selectedProductId"
                    placeholder="选择商品"
                    size="small"
                    @change="loadPriceTrend"
                    style="width: 200px;"
                    clearable
                >
                  <el-option
                      v-for="product in followedProducts"
                      :key="product.productId"
                      :label="product.productName"
                      :value="product.productId"
                  />
                </el-select>
                <el-select v-model="trendPeriod" size="small" @change="loadPriceTrend" style="width: 100px;">
                  <el-option label="7 天" :value="7" />
                  <el-option label="15 天" :value="15" />
                  <el-option label="30 天" :value="30" />
                </el-select>
              </div>
            </div>
            <div class="chart-wrapper" v-loading="trendLoading">
              <v-chart v-if="trendOption" :option="trendOption" autoresize />
              <el-empty v-else description="请选择要查看的商品" />
            </div>
          </el-card>
        </el-col>

      </el-row>

      <!-- 新增图表行 -->
      <el-row :gutter="20" style="margin-top: 20px;">

        <!-- 平台分布 -->
        <el-col :span="12">
          <el-card>
            <template #header>
              <span>🏪 平台分布</span>
            </template>
            <div v-loading="platformLoading" style="height: 300px;">
              <v-chart v-if="platformOption" :option="platformOption" autoresize />
            </div>
          </el-card>
        </el-col>
      </el-row>


      <!-- 管理员入口 -->
      <el-row :gutter="20" style="margin-top: 20px;" v-if="isAdmin">
        <el-col :span="24">
          <el-card>
            <template #header>
              <div class="card-header">
                <span>🔧 管理后台</span>
              </div>
            </template>
            <el-row :gutter="20">
              <el-col :span="12">
                <el-button type="primary" size="large" @click="$router.push('/admin/users')" style="width: 100%;">
                  👥 用户管理
                </el-button>
              </el-col>
              <el-col :span="12">
                <el-button type="success" size="large" @click="$router.push('/admin/products')" style="width: 100%;">
                  📦 商品审核
                </el-button>
              </el-col>
            </el-row>
          </el-card>
        </el-col>
      </el-row>

    </div>
  </MainLayout>
</template>

<script setup>
import MainLayout from '@/components/layout/MainLayout.vue'
import { ref, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { TrendCharts, BellFilled } from '@element-plus/icons-vue'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { LineChart, PieChart, BarChart } from 'echarts/charts'
import { TitleComponent, TooltipComponent, GridComponent, LegendComponent } from 'echarts/components'
import VChart from 'vue-echarts'
import * as echarts from 'echarts'
import request from '@/utils/request'

use([CanvasRenderer, LineChart, PieChart, BarChart, TitleComponent, TooltipComponent, GridComponent, LegendComponent])

const userStore = useUserStore()

// 是否为管理员
const isAdmin = ref(userStore.userInfo?.role === 'ADMIN')

// 统计数据
const stats = ref({
  followedCount: 0,
  priceDropCount: 0,
  savedAmount: 0,
  unreadCount: 0
})

// 价格趋势
const trendPeriod = ref(7)
const selectedProductId = ref(null)
const followedProducts = ref([])
const trendLoading = ref(false)
const trendOption = ref(null)

// 分类占比
const categoryLoading = ref(false)
const categoryOption = ref(null)

// 通知统计
const notificationLoading = ref(false)
const notificationOption = ref(null)

// 平台分布
const platformLoading = ref(false)
const platformOption = ref(null)


onMounted(async () => {
  await loadStats()
  await loadFollowedProducts()
  await loadCategoryDistribution()
  await loadNotificationStats()
  await loadPlatformDistribution()
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


// 获取用户关注的商品列表
const loadFollowedProducts = async () => {
  try {
    const userId = userStore.userInfo?.id || 1
    const res = await request.get(`/user-products/user/${userId}`)

    if (res.code === 200) {
      followedProducts.value = res.data || []
    }
  } catch (error) {
    console.error('加载关注商品列表失败:', error)
  }
}


// 加载价格趋势
const loadPriceTrend = async () => {
  // 如果没有选择商品，不加载
  if (!selectedProductId.value) {
    trendOption.value = null
    return
  }

  trendLoading.value = true
  try {
    console.log('=== [Dashboard] 开始加载价格趋势 ===')
    console.log('选择的 productId:', selectedProductId.value)
    console.log('选择的天数:', trendPeriod.value)

    const res = await request.get(`/products/${selectedProductId.value}/price-history`)

    console.log('价格历史 API 响应:', res)
    console.log('价格历史 data:', res.data)

    if (res.code === 200) {
      const data = res.data || []

      console.log('=== 价格历史数据详情 ===')
      console.log('数据长度:', data.length)

      if (data.length === 0) {
        console.warn('价格历史数据为空')
        trendOption.value = null
        return
      }

      // 按日期排序
      const sortedData = data.sort((a, b) =>
          new Date(a.checkedAt).getTime() - new Date(b.checkedAt).getTime()
      )

      // 【新增】根据选择的天数过滤数据
      const now = new Date()
      const daysToKeep = parseInt(trendPeriod.value)
      const startDate = new Date()
      startDate.setDate(now.getDate() - daysToKeep)

      console.log(`过滤前数据量：${sortedData.length}`)
      console.log(`起始日期：${startDate.toISOString()}`)

      const filteredData = sortedData.filter(item => {
        const itemDate = new Date(item.checkedAt)
        return itemDate >= startDate
      })

      console.log(`过滤后数据量：${filteredData.length}`)

      if (filteredData.length === 0) {
        console.warn('过滤后数据为空')
        trendOption.value = null
        return
      }

      const dates = filteredData.map(item => {
        const date = new Date(item.checkedAt)
        return `${date.getMonth() + 1}/${date.getDate()}`
      })

      const prices = filteredData.map(item => item.price)

      console.log('dates:', dates)
      console.log('prices:', prices)

      // 获取商品信息
      const product = followedProducts.value.find(p => p.productId === selectedProductId.value)
      const productName = product ? product.productName : '未知商品'

      console.log('商品名称:', productName)

      trendOption.value = {
        title: {
          text: `${productName} 价格走势（近${daysToKeep}天）`,
          left: 'center',
          textStyle: {
            fontSize: 16,
            fontWeight: 'bold'
          }
        },
        tooltip: {
          trigger: 'axis',
          formatter: (params) => {
            const param = params[0]
            return `
              <div style="padding: 8px;">
                <div style="font-weight: bold; margin-bottom: 6px;">${param.name}</div>
                <div style="color: #f56c6c; font-size: 18px; font-weight: bold;">¥${param.value}</div>
              </div>
            `
          }
        },
        grid: {
          left: '3%',
          right: '4%',
          bottom: '3%',
          top: '12%',
          containLabel: true
        },
        xAxis: {
          type: 'category',
          data: dates,
          axisLabel: {
            rotate: 45,
            margin: 20
          }
        },
        yAxis: {
          type: 'value',
          name: '价格 (元)',
          nameTextStyle: {
            fontSize: 12,
            color: '#666'
          },
          axisLabel: {
            formatter: '¥{value}',
            margin: 15
          },
          splitLine: {
            lineStyle: {
              color: '#e6e6e6',
              type: 'dashed'
            }
          }
        },
        series: [{
          data: prices,
          type: 'line',
          smooth: true,
          symbol: 'circle',
          symbolSize: 8,
          areaStyle: {
            color: {
              type: 'linear',
              x: 0,
              y: 0,
              x2: 0,
              y2: 1,
              colorStops: [
                {
                  offset: 0,
                  color: 'rgba(64, 158, 255, 0.3)'
                },
                {
                  offset: 1,
                  color: 'rgba(64, 158, 255, 0.05)'
                }
              ]
            }
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

      console.log('图表配置已设置')
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
    console.log('=== [Dashboard] 加载分类分布 ===')
    console.log('userId:', userId)

    const res = await request.get('/dashboard/category-distribution', {
      params: { userId }
    })

    console.log('=== 分类分布 API 响应 ===')
    console.log('完整响应:', res)
    console.log('响应 data:', res.data)

    if (res.code === 200) {
      const categories = res.data || []
      console.log('分类数据:', categories)
      console.log('分类数量:', categories.length)

      if (categories.length === 0) {
        console.warn('分类数据为空，不显示图表')
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
          radius: ['35%', '65%'],
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
            formatter: '{b}: {d}%',
            fontSize: 12
          },
          avoidLabelOverlap: true,
          labelLine: {
            length: 10,
            length2: 10
          }
        }]
      }
      console.log('分类图表配置已设置')
    }
  } catch (error) {
    console.error('加载分类分布失败:', error)
    console.error('错误详情:', error.response?.data)
  } finally {
    categoryLoading.value = false
  }
}


// 加载通知统计
const loadNotificationStats = async () => {
  notificationLoading.value = true
  try {
    const userId = userStore.userInfo?.id || 1
    const res = await request.get('/dashboard/notification-stats', {
      params: { userId }
    })

    if (res.code === 200) {
      const data = res.data
      notificationOption.value = {
        title: {
          text: '通知接收统计',
          left: 'center',
          textStyle: {
            fontSize: 16,
            fontWeight: 'bold'
          }
        },
        tooltip: {
          trigger: 'axis',
          axisPointer: {
            type: 'shadow'
          }
        },
        xAxis: {
          type: 'category',
          data: ['今日', '本周', '本月'],
          axisLabel: {
            interval: 0,
            rotate: 0
          }
        },
        yAxis: {
          type: 'value',
          name: '通知数量'
        },
        series: [{
          data: [
            data.todayCount || 0,
            data.weekCount || 0,
            data.monthCount || 0
          ],
          type: 'bar',
          barWidth: '40%',
          itemStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: '#83bff6' },
              { offset: 0.5, color: '#188df0' },
              { offset: 1, color: '#188df0' }
            ])
          },
          label: {
            show: true,
            position: 'top'
          }
        }]
      }
    }
  } catch (error) {
    console.error('加载通知统计失败:', error)
  } finally {
    notificationLoading.value = false
  }
}

// 加载平台分布
const loadPlatformDistribution = async () => {
  platformLoading.value = true
  try {
    const userId = userStore.userInfo?.id || 1
    const res = await request.get('/dashboard/platform-distribution', {
      params: { userId }
    })

    if (res.code === 200) {
      const platforms = res.data || []

      if (platforms.length === 0) {
        platformOption.value = null
        return
      }

      platformOption.value = {
        title: {
          text: '关注商品平台分布',
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
          radius: ['35%', '65%'],
          center: ['55%', '50%'],
          data: platforms,
          emphasis: {
            itemStyle: {
              shadowBlur: 10,
              shadowOffsetX: 0,
              shadowColor: 'rgba(0, 0, 0, 0.5)'
            }
          },
          label: {
            formatter: '{b}: {d}%',
            fontSize: 12
          },
          avoidLabelOverlap: true,
          labelLine: {
            length: 10,
            length2: 10
          }
        }]
      }
    }
  } catch (error) {
    console.error('加载平台分布失败:', error)
  } finally {
    platformLoading.value = false
  }
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

.chart-wrapper {
  height: 400px;
}
</style>
