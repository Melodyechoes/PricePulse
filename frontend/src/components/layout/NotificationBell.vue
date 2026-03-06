<template>
  <el-badge :value="unreadCount" :hidden="unreadCount === 0" class="notification-bell">
    <el-button
        text
        size="large"
        @click="showNotifications = true"
    >
      <el-icon><bell /></el-icon>
    </el-button>
  </el-badge>

  <el-dialog
      v-model="showNotifications"
      title="我的通知"
      width="500px"
  >
    <div v-loading="loading" class="notification-list">
      <el-empty v-if="notifications.length === 0" description="暂无通知" />

      <el-timeline v-else>
        <el-timeline-item
            v-for="item in notifications"
            :key="item.id"
            :timestamp="formatTime(item.createdAt)"
            placement="top"
            :type="item.type === 'PRICE_DROP' ? 'success' : 'primary'"
        >
          <el-card>
            <p>{{ item.message }}</p>
          </el-card>
        </el-timeline-item>
      </el-timeline>
    </div>
  </el-dialog>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { Bell } from '@element-plus/icons-vue'
import axios from 'axios'

const userStore = useUserStore()
const showNotifications = ref(false)
const loading = ref(false)
const notifications = ref([])

const unreadCount = computed(() => {
  return notifications.value.filter(n => !n.isRead).length
})

onMounted(async () => {
  await loadNotifications()
})

const loadNotifications = async () => {
  loading.value = true
  try {
    const res = await axios.get('/api/notifications', {
      params: { userId: userStore.userInfo?.id || 1 }
    })

    if (res.data.code === 200) {
      notifications.value = res.data.data || []
    }
  } catch (error) {
    console.error('加载通知失败:', error)
  } finally {
    loading.value = false
  }
}

const formatTime = (time) => {
  if (!time) return ''
  return new Date(time).toLocaleString('zh-CN')
}
</script>

<style scoped>
.notification-bell {
  cursor: pointer;
}

.notification-list {
  max-height: 400px;
  overflow-y: auto;
}
</style>
