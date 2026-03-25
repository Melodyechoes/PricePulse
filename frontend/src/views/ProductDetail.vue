<template>
  <div class="detail-container">
    <el-button @click="goBack" icon="ArrowLeft">返回</el-button>

    <div class="detail-content" v-loading="loading">
      <el-row :gutter="20">
        <el-col :span="12">
          <div class="product-image-section">
            <img :src="currentProduct?.imageUrl || '/placeholder.png'" :alt="currentProduct?.name" />
          </div>
        </el-col>

        <el-col :span="12">
          <div class="product-info-section">
            <h1 class="product-title">{{ currentProduct?.name }}</h1>

            <div class="price-section">
              <div class="current-price">
                <span class="label">当前价格</span>
                <span class="value">¥{{ currentProduct?.currentPrice }}</span>
              </div>
              <div class="original-price" v-if="currentProduct?.originalPrice">
                <span class="label">原价</span>
                <span class="value">¥{{ currentProduct?.originalPrice }}</span>
              </div>
            </div>

            <div class="product-meta">
              <div class="meta-item">
                <span class="label">平台</span>
                <span class="value">{{ currentProduct?.platform }}</span>
              </div>
              <div class="meta-item">
                <span class="label">分类</span>
                <span class="value">{{ currentProduct?.category }}</span>
              </div>
              <div class="meta-item">
                <span class="label">品牌</span>
                <span class="value">{{ currentProduct?.brand || '无' }}</span>
              </div>
            </div>

            <div class="description" v-if="currentProduct?.description">
              <h3>商品描述</h3>
              <p>{{ currentProduct.description }}</p>
            </div>

            <div class="actions">
              <el-button
                  v-if="isFollowed"
                  type="danger"
                  size="large"
                  @click="handleUnfollow"
              >
                取消关注
              </el-button>
              <el-button
                  v-else
                  type="primary"
                  size="large"
                  @click="handleFollow"
              >
                关注此商品
              </el-button>

              <el-button size="large" @click="viewPriceHistory">
                查看价格历史
              </el-button>
            </div>
          </div>
        </el-col>
      </el-row>

      <!-- 价格历史图表弹窗 -->
      <el-dialog
          v-model="showPriceHistory"
          title="价格历史趋势"
          width="800px"
          :close-on-click-modal="false"
      >
        <PriceHistoryChart
            :price-history="priceHistoryList"
            :loading="priceHistoryLoading"
        />
      </el-dialog>
    </div>
  </div>
</template>

<script setup>
import {ref, computed, onMounted} from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useProductsStore } from '@/stores/products'
import { followProduct, unfollowProduct, getPriceHistory } from '@/api/products'
import { ElMessage } from 'element-plus'
import PriceHistoryChart from '@/components/PriceHistoryChart.vue'

const showPriceHistory = ref(false)
const priceHistoryList = ref([])
const priceHistoryLoading = ref(false)
const route = useRoute()
const router = useRouter()
const productsStore = useProductsStore()
const loading = ref(false)
const isFollowed = ref(false)

const goBack = () => {
  router.back()
}

const handleFollow = async () => {
  console.log('=== [商品详情] 点击关注按钮 ===')
  console.log('当前商品 ID:', route.params.id)

  try {
    console.log('准备调用 followProduct API...')
    const result = await followProduct(route.params.id, { alertThreshold: 0.1 })
    console.log('followProduct 返回结果:', result)

    ElMessage.success('关注成功')
    isFollowed.value = true

    // 刷新 Profile 页面的数据（如果有父组件通信）
    console.log('关注成功，提示用户刷新个人中心查看')
  } catch (error) {
    console.error('=== [商品详情] 关注失败 ===')
    console.error('错误对象:', error)
    console.error('错误响应:', error.response)
    console.error('错误消息:', error.response?.data?.message || error.message)

    ElMessage.error(`关注失败：${error.response?.data?.message || error.message}`)
  }
}

const handleUnfollow = async () => {
  console.log('=== [商品详情] 点击取消关注按钮 ===')
  console.log('当前商品 ID:', route.params.id)

  try {
    console.log('准备调用 unfollowProduct API...')
    const result = await unfollowProduct(route.params.id)
    console.log('unfollowProduct 返回结果:', result)

    ElMessage.success('已取消关注')
    isFollowed.value = false
  } catch (error) {
    console.error('=== [商品详情] 取消关注失败 ===')
    console.error('错误对象:', error)
    console.error('错误响应:', error.response)
    console.error('错误消息:', error.response?.data?.message || error.message)

    ElMessage.error(`取消关注失败：${error.response?.data?.message || error.message}`)
  }
}

const viewPriceHistory = async () => {
  showPriceHistory.value = true
  priceHistoryLoading.value = true
  try {
    const res = await getPriceHistory(route.params.id)
    priceHistoryList.value = res.data || []
  } catch (error) {
    console.error('加载价格历史失败:', error)
    ElMessage.error('加载价格历史失败')
  } finally {
    priceHistoryLoading.value = false
  }
}
onMounted(() => {
  fetchProductDetail()
})

// 检查路由参数是否存在
if (!route.params.id) {
  console.error('缺少商品 ID 参数')
  ElMessage.error('商品 ID 不能为空')
  router.push('/products')
}

const currentProduct = computed(() => productsStore.currentProduct)

const fetchProductDetail = async () => {
  if (!route.params.id) {
    console.error('商品 ID 为空，无法加载详情')
    return
  }

  loading.value = true
  try {
    await productsStore.fetchProductDetail(route.params.id)
  } catch (error) {
    console.error('加载商品详情失败:', error)
    ElMessage.error('加载失败')
  } finally {
    loading.value = false
  }
}

</script>

<style scoped>
.detail-container {
  min-height: 100vh;
  background: #f5f7fa;
  padding: 20px;
}

.detail-content {
  margin-top: 20px;
  padding: 30px;
  background: white;
  border-radius: 12px;
}

.product-image-section {
  padding: 20px;
}

.product-image-section img {
  width: 100%;
  border-radius: 12px;
}

.product-info-section {
  padding: 20px;
}

.product-title {
  font-size: 28px;
  color: #333;
  margin-bottom: 20px;
}

.price-section {
  margin-bottom: 30px;
  padding: 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 12px;
  color: white;
}

.current-price,
.original-price {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.current-price .value {
  font-size: 36px;
  font-weight: bold;
}

.original-price {
  opacity: 0.8;
  text-decoration: line-through;
}

.product-meta {
  margin-bottom: 30px;
}

.meta-item {
  display: flex;
  justify-content: space-between;
  padding: 15px 0;
  border-bottom: 1px solid #eee;
}

.meta-item .label {
  color: #666;
}

.meta-item .value {
  color: #333;
  font-weight: 500;
}

.description {
  margin-bottom: 30px;
}

.description h3 {
  font-size: 18px;
  color: #333;
  margin-bottom: 10px;
}

.description p {
  color: #666;
  line-height: 1.8;
}

.actions {
  display: flex;
  gap: 15px;
}
</style>
