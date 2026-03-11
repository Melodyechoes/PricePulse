<template>
  <MainLayout>
    <div class="notification-container">
      <div class="notification-header">
        <h1>🔔 消息通知</h1>
        <el-button
            v-if="hasUnread"
            type="primary"
            size="small"
            @click="markAllAsRead"
        >
          全部已读
        </el-button>
      </div>

      <el-card>
        <div v-loading="loading" class="notification-list">
          <!-- 空状态 -->
          <el-empty v-if="notifications.length === 0" description="暂无通知" />

          <!-- 通知列表 -->
          <div v-else>
            <div
                v-for="item in notifications"
                :key="item.id"
                :class="['notification-item', { unread: !item.isRead }]"
            >
              <div class="notification-icon">
                <el-icon v-if="item.type === 'PRICE_DROP'" color="#67C23A"><money /></el-icon>
                <el-icon v-else color="#909399"><bell /></el-icon>
              </div>

              <div class="notification-content">
                <div class="notification-message">{{ item.message }}</div>
                <div class="notification-time">{{ formatTime(item.createdAt) }}</div>
              </div>

              <div class="notification-actions">
                <el-button
                    v-if="!item.isRead"
                    type="text"
                    size="small"
                    @click="markAsRead(item.id)"
                >
                  标记为已读
                </el-button>
                <el-button
                    type="text"
                    size="small"                    style="color: #F56C6C;"
                    @click="deleteNotification(item.id)"
                >
                  删除
                </el-button>
              </div>
            </div>
          </div>
        </div>
      </el-card>
    </div>
  </MainLayout>
</template>

<script setup>
import MainLayout from '@/components/layout/MainLayout.vue'
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Money, Bell } from '@element-plus/icons-vue'
import request from '@/utils/request'
import axios from "axios";

const userStore = useUserStore()
const loading = ref(false)
const notifications = ref([])

const hasUnread = computed(() => {
  return notifications.value.some(n => !n.isRead)
})

onMounted(async () => {
  await loadNotifications()

  // 每 5 分钟轮询一次新通知
  const interval = setInterval(loadNotifications, 5 * 60 * 1000)

  // 组件销毁时清除定时器
  onUnmounted(() => clearInterval(interval))
})

const loadNotifications = async () => {
  loading.value = true
  try {
    const res = await request.get('/notifications', {
      params: { userId: userStore.userInfo?.id || 1 }
    })

    console.log('=== 通知 API 响应 ===')
    console.log('原始响应:', res)
    console.log('响应 data:', res.data)
    console.log('通知列表:', res.data)

    if (res.code === 200) {
      // 修复：res 已经是 response.data，所以直接访问 res.data
      const responseData = res.data

      console.log('responseData:', responseData)
      console.log('responseData 类型:', typeof responseData, Array.isArray(responseData))

      // 如果 responseData 是对象且有 list 属性
      if (responseData && typeof responseData === 'object' && responseData.list) {
        notifications.value = responseData.list || []
        console.log('从 list 获取数据，数量:', notifications.value.length)
      }
      // 如果 responseData 本身就是数组
      else if (Array.isArray(responseData)) {
        notifications.value = responseData
        console.log('直接使用数组，数量:', notifications.value.length)
      }
      // 否则使用空数组
      else {
        notifications.value = []
        console.log('使用空数组')
      }

      console.log('最终通知数量:', notifications.value.length)
      if (notifications.value.length > 0) {
        console.log('第一条通知:', notifications.value[0])
        console.log('第一条通知的 isRead:', notifications.value[0]?.isRead)
      }

      // 如果有未读通知，显示角标
      const unreadCount = notifications.value.filter(n => n && !n.isRead).length
      if (unreadCount > 0) {
        console.log(`有 ${unreadCount} 条未读通知`)
      } else {
        console.log('没有未读通知')
      }
    } else {
      console.error('API 返回错误:', res.message)
    }
  } catch (error) {
    console.error('加载通知失败:', error)
    console.error('错误详情:', error.response?.data)
  } finally {
    loading.value = false
  }
}

const markAllAsRead = async () => {
  try {
    const unreadIds = notifications.value
        .filter(n => !n.isRead)
        .map(n => n.id)

    // 批量标记已读
    for (const id of unreadIds) {
      await markAsRead(id)
    }

    ElMessage.success('已全部标记为已读')
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const markAsRead = async (id) => {
  try {
    await axios.put(`/api/notifications/${id}/read`)

    // 更新本地状态
    const notification = notifications.value.find(n => n.id === id)
    if (notification) {
      notification.isRead = true
    }
  } catch (error) {
    console.error('标记已读失败:', error)
  }
}

const deleteNotification = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除这条通知吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    await axios.delete(`/api/notifications/${id}`)

    // 从列表中移除
    notifications.value = notifications.value.filter(n => n.id !== id)

    ElMessage.success('删除成功')
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  const now = new Date()
  const diff = now - date

  // 1 分钟内显示"刚刚"
  if (diff < 60 * 1000) return '刚刚'
  // 1 小时内显示分钟数
  if (diff < 60 * 60 * 1000) return `${Math.floor(diff / 60000)}分钟前`
  // 24 小时内显示小时数
  if (diff < 24 * 60 * 60 * 1000) return `${Math.floor(diff / 3600000)}小时前`
  // 超过 24 小时显示日期
  return date.toLocaleString('zh-CN', {
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}
</script>

<style scoped>
.notification-container {
  min-height: 100vh;
  background: #f5f7fa;
  padding: 20px;
}

.notification-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding: 20px;
  background: white;
  border-radius: 12px;
}

.notification-header h1 {
  font-size: 28px;
  color: #333;
}

.notification-list {
  max-height: 600px;
  overflow-y: auto;
}

.notification-item {
  display: flex;
  align-items: flex-start;
  padding: 15px;
  border-bottom: 1px solid #f0f0f0;
  transition: background 0.3s;
}

.notification-item:last-child {
  border-bottom: none;
}

.notification-item.unread {
  background: #f5f7fa;
}

.notification-item:hover {
  background: #fafafa;
}

.notification-icon {
  margin-right: 15px;
  font-size: 24px;
}

.notification-content {
  flex: 1;
}

.notification-message {
  font-size: 14px;
  color: #333;
  line-height: 1.6;
  margin-bottom: 5px;
}

.notification-time {
  font-size: 12px;
  color: #999;
}

.notification-actions {
  display: flex;
  gap: 10px;
  margin-left: 15px;
}
</style>
