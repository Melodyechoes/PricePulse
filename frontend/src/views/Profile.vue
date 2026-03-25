<!-- eslint-disable vue/multi-word-component-names -->
<template>
  <MainLayout>
  <div class="profile-container">
    <div class="profile-header">
      <h1>👤 个人中心</h1>
    </div>

    <el-row :gutter="20">
      <!-- 左侧：用户信息 -->
      <el-col :span="8">
        <el-card class="user-info-card">
          <template #header>
            <span>基本信息</span>
          </template>
          <el-form label-width="80px">
            <el-form-item label="用户名">
              <el-input v-model="userInfo.username" disabled />
            </el-form-item>
            <el-form-item label="用户 ID">
              <el-input v-model="userInfo.id" disabled />
            </el-form-item>
            <el-form-item label="注册时间">
              <el-input v-model="userInfo.createdAt" disabled />
            </el-form-item>
            <el-form-item>
              <el-button type="danger" @click="handleLogout">退出登录</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>

      <!-- 右侧：我的关注 -->
      <el-col :span="16">
        <el-card class="follow-list-card">
          <template #header>
            <div class="card-header">
              <span>我的关注</span>
              <el-badge :value="followedList.length" class="item" />
            </div>
          </template>

          <div v-loading="loading">
            <div v-if="followedList.length === 0" class="empty-state">
              <el-empty description="您还没有关注任何商品" />
            </div>

            <el-table v-else :data="followedList" style="width: 100%">
              <el-table-column prop="productName" label="商品名称" />
              <el-table-column prop="currentPrice" label="当前价格" width="100">
                <template #default="{ row }">
                  ¥{{ Number(row.currentPrice).toFixed(2) }}
                </template>
              </el-table-column>
              <el-table-column prop="alertThreshold" label="提醒阈值" width="100">
                <template #default="{ row }">
                  {{ (Number(row.alertThreshold) * 100).toFixed(0) }}%
                </template>
              </el-table-column>
              <el-table-column label="操作" width="150">
                <template #default="{ row }">
                  <el-button
                      size="small"
                      @click="viewProductDetail(row)"
                  >
                    查看详情
                  </el-button>
                  <el-button
                      size="small"
                      type="danger"
                      @click="unfollowProduct(row)"
                  >
                    取消关注
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
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
import { getUserFollowedProducts, unfollowProduct as unfollowProductApi } from '@/api/products'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getCurrentUser } from '@/api/auth'


const router = useRouter()
const userStore = useUserStore()

const userInfo = ref({
  id: '',
  username: '',
  createdAt: ''
})

const followedList = ref([])
const loading = ref(false)

onMounted(async () => {
  // 加载用户信息
  await loadUserInfo()

  // 加载关注列表
  await loadFollowedList()

  console.log('用户信息加载完成:', userInfo.value)
})

const loadUserInfo = async () => {
  console.log('开始加载用户信息')
  console.log('userStore.userInfo:', userStore.userInfo)
  console.log('localStorage userInfo:', localStorage.getItem('userInfo'))

  if (userStore.userInfo && userStore.userInfo.id) {
    userInfo.value = {
      id: userStore.userInfo.id || '',
      username: userStore.userInfo.username || '',
      createdAt: userStore.userInfo.createdAt
          ? new Date(userStore.userInfo.createdAt).toLocaleString('zh-CN')
          : '未知'
    }
  } else {
    // 如果 store 中没有，尝试从 localStorage 获取
    const savedUser = localStorage.getItem('userInfo')
    if (savedUser) {
      try {
        const userData = JSON.parse(savedUser)
        userInfo.value = {
          id: userData.id || '',
          username: userData.username || '',
          createdAt: userData.createdAt
              ? new Date(userData.createdAt).toLocaleString('zh-CN')
              : '未知'
        }
      } catch (e) {
        console.error('解析用户信息失败:', e)
      }
    } else {
      // 如果都没有，调用后端接口获取
      try {
        const res = await getCurrentUser()
        if (res.code === 200 && res.data) {
          userInfo.value = {
            id: res.data.id || '',
            username: res.data.username || '',
            createdAt: res.data.createdAt
                ? new Date(res.data.createdAt).toLocaleString('zh-CN')
                : '未知'
          }
          // 保存到 store 和 localStorage
          localStorage.setItem('userInfo', JSON.stringify(res.data))
        }
      } catch (error) {
        console.error('获取用户信息失败:', error)
      }
    }
  }

  console.log('最终用户信息:', userInfo.value)
}

const loadFollowedList = async () => {
  console.log('=== [个人中心] 开始加载关注列表 ===')

  loading.value = true
  try {
    // 优先从 store 获取 userId
    let userId = userStore.userInfo?.id
    console.log('从 userStore.userInfo 获取的 userId:', userId)

    // 如果 store 没有，尝试从 localStorage 获取
    if (!userId) {
      const savedUser = localStorage.getItem('userInfo')
      console.log('从 localStorage 获取的 userInfo:', savedUser)

      if (savedUser) {
        try {
          const userData = JSON.parse(savedUser)
          userId = userData.id
          console.log('从 localStorage 解析出的 userId:', userId)
        } catch (e) {
          console.error('解析用户信息失败:', e)
        }
      }
    }

    // 最后回退到默认值 1
    userId = userId || 1
    console.log('最终使用的 userId:', userId)

    console.log('准备调用 getUserFollowedProducts API...')
    const res = await getUserFollowedProducts(Number(userId))

    console.log('=== API 响应 ===')
    console.log('完整响应:', res)
    console.log('res.code:', res.code)
    console.log('res.data:', res.data)
    console.log('res.data 长度:', res.data?.length)

    // 处理返回数据
    followedList.value = (res.data || []).map(item => {
      console.log('处理单个关注项:', item)
      console.log('- productId:', item.productId)
      console.log('- productName:', item.productName)
      console.log('- currentPrice:', item.productCurrentPrice)

      return {
        productId: Number(item.productId || item.id),
        productName: item.productName || '商品详情',
        currentPrice: item.productCurrentPrice || item.currentPrice || 0,
        image_url: item.productImageUrl || item.imageUrl || '',
        alertThreshold: (item.priceDropThreshold || 5) / 100,
        ...item
      }
    })

    console.log('=== 处理后的关注列表 ===')
    console.log('followedList.length:', followedList.value.length)
    console.log('followedList:', followedList.value)
  } catch (error) {
    console.error('=== [个人中心] 加载关注列表失败 ===')
    console.error('错误对象:', error)
    console.error('错误响应:', error.response)
    console.error('错误消息:', error.response?.data?.message || error.message)

    const errorMsg = error.response?.data?.message || error.message || '加载关注列表失败'
    ElMessage.error(errorMsg)
  } finally {
    loading.value = false
    console.log('加载状态设置为 false')
  }
}

const viewProductDetail = (row) => {
  console.log('跳转商品详情，row:', row)

  const productId = row?.productId

  if (!productId || productId === 0) {
    ElMessage.error('商品 ID 不能为空')
    console.error('无效的商品 ID:', row)
    return
  }

  console.log('跳转到商品 ID:', productId)
  router.push(`/product/${productId}`)
}

const unfollowProduct = async (row) => {
  try {
    console.log('取消关注，row:', row)

    const productId = row?.productId

    if (!productId || productId === 0) {
      ElMessage.error('商品 ID 不能为空')
      console.error('无效的商品 ID:', row)
      return
    }

    await ElMessageBox.confirm('确定要取消关注该商品吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    console.log('发送取消关注请求，productId:', productId)
    await unfollowProductApi(Number(productId))
    ElMessage.success('已取消关注')
    await loadFollowedList()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('取消关注失败:', error)

      // 显示详细错误信息
      const errorMsg = error.response?.data?.message || error.message || '取消关注失败'
      ElMessage.error(errorMsg)
    }
  }
}
const handleLogout = async () => {
  try {
    await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    userStore.logout()
    ElMessage.success('已退出登录')
    router.push('/login')
  } catch (error) {
    if (error !== 'cancel') {
      console.error('退出登录失败:', error)
    }
  }
}
</script>

<style scoped>
.profile-container {
  min-height: 100vh;
  background: #f5f7fa;
  padding: 20px;
}

.profile-header {
  margin-bottom: 20px;
  padding: 20px;
  background: white;
  border-radius: 12px;
}

.profile-header h1 {
  font-size: 28px;
  color: #333;
}

.user-info-card,
.follow-list-card {
  border-radius: 12px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.empty-state {
  padding: 40px 0;
}

.item {
  margin-top: 10px;
}
</style>
