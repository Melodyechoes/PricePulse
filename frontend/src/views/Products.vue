<!-- eslint-disable vue/multi-word-component-names -->
<template>
  <MainLayout>
    <div class="products-container">
      <div class="products-header">
        <h1>🔍 商品列表</h1>
        <el-button type="primary" @click="showAddDialog">
          <el-icon><Plus /></el-icon>
          添加商品
        </el-button>
      </div>

      <!-- 搜索栏 -->
      <div class="search-bar">
        <div class="search-row">
          <el-input
              v-model="searchKeyword"
              placeholder="搜索商品名称或品牌"            style="width: 300px"
              clearable
              @keyup.enter="fetchProducts"
          >
            <template #append>
              <el-button @click="fetchProducts">
                <el-icon><Search /></el-icon>
              </el-button>
            </template>
          </el-input>

          <el-select
              v-model="platformFilter"
              placeholder="选择平台"
              clearable
              @change="fetchProducts"            style="width: 150px"
          >
            <el-option label="全部平台" value="" />
            <el-option v-for="platform in availableFilters.platforms"
                       :key="platform"
                       :label="platform"
                       :value="platform" />
          </el-select>

          <el-select
              v-model="categoryFilter"
              placeholder="选择分类"
              clearable
              @change="fetchProducts"            style="width: 150px"
          >
            <el-option label="全部分类" value="" />
            <el-option v-for="category in availableFilters.categories"
                       :key="category"
                       :label="category"
                       :value="category" />
          </el-select>
        </div>

        <div class="search-row">
          <el-input-number
              v-model="minPrice"
              placeholder="最低价"
              :min="0"
              :precision="2"
              controls-position="right"            style="width: 150px"
              @change="fetchProducts"
          />
          <span style="margin: 0 10px;">-</span>
          <el-input-number
              v-model="maxPrice"
              placeholder="最高价"
              :min="0"
              :precision="2"
              controls-position="right"            style="width: 150px"
              @change="fetchProducts"
          />

          <el-select
              v-model="priceSort"
              placeholder="价格排序"
              @change="fetchProducts"            style="width: 150px"
          >
            <el-option label="默认排序" value="" />
            <el-option label="价格从低到高" value="asc" />
            <el-option label="价格从高到低" value="desc" />
          </el-select>

          <el-button type="primary" @click="fetchProducts">查询</el-button>
          <el-button @click="resetFilters">重置</el-button>
        </div>
      </div>

    <!-- 商品列表 -->
    <div class="product-list" v-loading="loading">
      <el-row :gutter="20">
        <el-col
            v-for="product in productList"
            :key="product.id"
            :xs="24"
            :sm="12"
            :md="8"
            :lg="6"
        >
          <el-card
              class="product-card"
              shadow="hover"
              @click="goToDetail(product.id)"
          >
            <div class="product-image">
              <img
                  v-if="product.imageUrl"
                  :src="product.imageUrl"
                  :alt="product.name"
                  @error="handleImageError"
              />
              <div v-else class="no-image">暂无图片</div>
            </div>
            <div class="product-info">
              <h3 class="product-name">{{ product.name }}</h3>
              <div class="product-price">
                <span class="price-label">当前价格</span>
                <span class="price-value">¥{{ product.currentPrice }}</span>
              </div>
              <div class="product-meta">
                <span class="platform-tag">{{ product.platform }}</span>
                <span class="category-tag">{{ product.category }}</span>
              </div>
              <div class="product-actions">
                <el-button
                    type="success"
                    size="small"
                    :loading="product.crawling"
                    @click.stop="crawlPrice(product)"
                >
                  刷新价格
                </el-button>
                <el-button
                    v-if="product.isFollowed"
                    type="danger"
                    size="small"
                    @click.stop="unfollowProduct(product.id)"
                >
                  取消关注
                </el-button>
                <el-button
                    v-else
                    type="primary"
                    size="small"
                    @click.stop="followProduct(product.id)"
                >
                  关注
                </el-button>
                <el-button
                    type="danger"
                    size="small"
                    plain
                    @click.stop="confirmDelete(product)"
                >
                  删除
                </el-button>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <!-- 空状态 -->
      <el-empty v-if="!loading && productList.length === 0" description="暂无商品" />
    </div>

    <!-- 分页 -->
    <div class="pagination">
      <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :total="pagination.total"
          layout="total, prev, pager, next"
          @current-change="fetchProducts"
      />
    </div>

    <!-- 添加商品弹窗 -->
    <AddProductDialog
        v-model="showAddDialogFlag"
        @success="fetchProducts"
    />
  </div>
  </MainLayout>
</template>

<script setup>
import { Search, Plus } from '@element-plus/icons-vue'
import { ref, onMounted, computed } from 'vue'
import MainLayout from '@/components/layout/MainLayout.vue'
import { useRouter } from 'vue-router'
import { useProductsStore } from '@/stores/products'
import { followProduct as followProductApi, unfollowProduct as unfollowProductApi, crawlProductPrice as crawlProductPriceApi, deleteProduct as deleteProductApi } from '@/api/products'
import { ElMessage, ElMessageBox } from 'element-plus'
import AddProductDialog from '@/components/AddProductDialog.vue'



const router = useRouter()
const productsStore = useProductsStore()

const searchKeyword = ref('')
const platformFilter = ref('')
const categoryFilter = ref('')
const minPrice = ref(null)
const maxPrice = ref(null)
const priceSort = ref('')
const loading = ref(false)
const showAddDialogFlag = ref(false)


const confirmDelete = async (product) => {
  try {
    await ElMessageBox.confirm(
        `确定要删除商品"${product.name}"吗？删除后无法恢复。`,
        '确认删除',
        {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }
    )

    await deleteProductApi(product.id)
    ElMessage.success('删除成功')

    // 重新加载列表
    await fetchProducts()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
      ElMessage.error('删除失败')
    }
  }
}


const crawlPrice = async (product) => {
  product.crawling = true
  try {
    const res = await crawlProductPriceApi(product.id)

    if (res.code === 200) {
      ElMessage.success(`价格刷新成功！`)
      // 重新加载商品列表
      await fetchProducts()
    } else {
      ElMessage.error(res.message || '刷新失败')
    }
  } catch (error) {
    console.error('刷新价格失败:', error)
    ElMessage.error('刷新价格失败，请重试')
  } finally {
    product.crawling = false
  }
}


const availableFilters = ref({
  platforms: [],
  categories: []
})

const productList = computed(() => productsStore.productList)
const pagination = computed(() => productsStore.pagination)

// 加载可用筛选条件
const loadAvailableFilters = async () => {
  try {
    const res = await fetch('/api/products/filters')
    const data = await res.json()
    if (data.code === 200) {
      availableFilters.value = data.data
    }
  } catch (error) {
    console.error('加载筛选条件失败:', error)
  }
}

const fetchProducts = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.value.page,
      size: pagination.value.size,
      keyword: searchKeyword.value || undefined,
      platform: platformFilter.value || undefined,
      category: categoryFilter.value || undefined,
      minPrice: minPrice.value || undefined,
      maxPrice: maxPrice.value || undefined,
      sort: priceSort.value || undefined
    }
    await productsStore.fetchProducts(params)

    // 调试输出
    console.log('商品列表:', productsStore.productList)
    console.log('分页信息:', productsStore.pagination)

    // 【新增】检查每个商品的 isFollowed 字段
    productsStore.productList.forEach(product => {
      console.log(`商品 ${product.id}:`, product.name, 'isFollowed:', product.isFollowed)
    })
  } catch (error) {
    console.error('加载商品列表失败:', error)
    ElMessage.error('加载商品列表失败')
  } finally {
    loading.value = false
  }
}

const resetFilters = () => {
  searchKeyword.value = ''
  platformFilter.value = ''
  categoryFilter.value = ''
  minPrice.value = null
  maxPrice.value = null
  priceSort.value = ''
  fetchProducts()
}

const goToDetail = (id) => {
  router.push(`/product/${id}`)
}

const followProduct = async (productId) => {
  try {
    await followProductApi(productId, { alertThreshold: 0.1 })
    ElMessage.success('关注成功')

    // 【修改】直接更新本地状态，不重新加载列表
    const index = productsStore.productList.findIndex(p => p.id === productId)
    if (index !== -1) {
      productsStore.productList[index].isFollowed = true
    }
  } catch (error) {
    console.error('关注失败:', error)
    if (error.message && error.message.includes('已关注')) {
      ElMessage.info('您已关注该商品')
      // 直接更新本地状态
      const index = productsStore.productList.findIndex(p => p.id === productId)
      if (index !== -1) {
        productsStore.productList[index].isFollowed = true
      }
    } else {
      ElMessage.error('关注失败')
    }
  }
}

const unfollowProduct = async (productId) => {
  try {
    await unfollowProductApi(productId)
    ElMessage.success('已取消关注')

    // 【修改】直接更新本地状态，不重新加载列表
    const index = productsStore.productList.findIndex(p => p.id === productId)
    if (index !== -1) {
      productsStore.productList[index].isFollowed = false
    }
  } catch (error) {
    console.error('取消关注失败:', error)
    ElMessage.error('取消关注失败，请重试')
  }
}

const showAddDialog = () => {
  showAddDialogFlag.value = true
}

onMounted(() => {
  fetchProducts()
})

onMounted(async () => {
  await loadAvailableFilters()
  await fetchProducts()
})

const handleImageError = (e) => {
  e.target.style.display = 'none'
}


</script>

<style scoped>
.products-container {
  min-height: 100vh;
  background: #f5f7fa;
  padding: 20px;
}

.products-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding: 20px;
  background: white;
  border-radius: 12px;
}

.products-header h1 {
  font-size: 28px;
  color: #333;
}

.search-bar {
  display: flex;
  flex-direction: column;
  gap: 15px;
  margin-bottom: 20px;
  padding: 20px;
  background: white;
  border-radius: 12px;
}

.search-row {
  display: flex;
  gap: 15px;
  align-items: center;
  flex-wrap: wrap;
}

.product-list {
  padding: 20px 0;
}

.product-card {
  margin-bottom: 20px;
  cursor: pointer;
  transition: transform 0.3s;
  border-radius: 12px;
}

.product-card:hover {
  transform: translateY(-5px);
}

.product-image {
  height: 200px;
  overflow: hidden;
  border-radius: 8px 8px 0 0;
}

.product-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.product-info {
  padding: 15px;
}

.product-name {
  font-size: 16px;
  color: #333;
  margin-bottom: 10px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.product-price {
  margin-bottom: 10px;
}

.price-label {
  font-size: 12px;
  color: #999;
}

.price-value {
  font-size: 24px;
  color: #f56c6c;
  font-weight: bold;
}

.product-meta {
  display: flex;
  gap: 8px;
  margin-bottom: 10px;
}

.platform-tag,
.category-tag {
  font-size: 12px;
  padding: 2px 8px;
  border-radius: 4px;
  background: #f0f2f5;
  color: #666;
}

.product-actions {
  text-align: center;
}

.pagination {
  display: flex;
  justify-content: center;
  padding: 20px;
  background: white;
  border-radius: 12px;
}

.product-image {
  height: 200px;
  overflow: hidden;
  border-radius: 8px 8px 0 0;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f7fa;
}

.product-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.no-image {
  color: #999;
  font-size: 14px;
}

</style>
