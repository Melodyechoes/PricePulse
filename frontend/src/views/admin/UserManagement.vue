<template>
  <div class="user-management">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>用户管理</span>
          <el-button type="primary" @click="loadUsers">刷新</el-button>
        </div>
      </template>

      <!-- 统计卡片 -->
      <el-row :gutter="20" style="margin-bottom: 20px;">
        <el-col :span="6">
          <el-statistic title="总用户数" :value="statistics.totalUsers" />
        </el-col>
        <el-col :span="6">
          <el-statistic title="活跃用户" :value="statistics.activeUsers" />
        </el-col>
        <el-col :span="6">
          <el-statistic title="管理员" :value="statistics.adminUsers" />
        </el-col>
        <el-col :span="6">
          <el-statistic title="普通用户" :value="statistics.normalUsers" />
        </el-col>
      </el-row>

      <!-- 用户列表表格 -->
      <el-table :data="userList" v-loading="loading" border stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="username" label="用户名" width="150" />
        <el-table-column prop="role" label="角色" width="100">
          <template #default="{ row }">
            <el-tag :type="row.role === 'ADMIN' ? 'danger' : 'primary'">
              {{ row.role === 'ADMIN' ? '管理员' : '普通用户' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">
              {{ row.status === 1 ? '正常' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="注册时间" width="180" />
        <el-table-column label="操作" fixed="right" width="300">
          <template #default="{ row }">
            <el-button
              size="small"
              :type="row.status === 1 ? 'warning' : 'success'"
              @click="toggleUserStatus(row)"
              :disabled="row.role === 'ADMIN'"
            >
              {{ row.status === 1 ? '禁用' : '启用' }}
            </el-button>
            <el-button
              size="small"
              type="primary"
              @click="showResetPassword(row)"
            >
              重置密码
            </el-button>
            <el-button
              size="small"
              type="danger"
              @click="confirmDelete(row)"
              :disabled="row.role === 'ADMIN'"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :total="total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="loadUsers"
        @current-change="loadUsers"
        style="margin-top: 20px; justify-content: flex-end;"
      />
    </el-card>

    <!-- 重置密码对话框 -->
    <el-dialog v-model="passwordDialogVisible" title="重置密码" width="400px">
      <el-form :model="passwordForm" label-width="80px">
        <el-form-item label="新用户">
          <el-input v-model="passwordForm.newPassword" type="password" placeholder="请输入新密码" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="passwordDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="resetPassword">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'

const loading = ref(false)
const userList = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const statistics = ref({})

const passwordDialogVisible = ref(false)
const passwordForm = ref({
  userId: null,
  newPassword: ''
})

// 加载用户列表
const loadUsers = async () => {
  loading.value = true
  try {
    const res = await request.get('/admin/users', {
      params: {
        page: currentPage.value,
        pageSize: pageSize.value
      }
    })
    
    if (res.code === 200) {
      userList.value = res.data.list || []
      total.value = res.data.total || 0
    } else {
      ElMessage.error(res.message || '加载用户列表失败')
    }
  } catch (error) {
    console.error('加载用户列表失败:', error)
    ElMessage.error('加载失败')
  } finally {
    loading.value = false
  }
}

// 加载统计数据
const loadStatistics = async () => {
  try {
    const res = await request.get('/admin/users/statistics')
    if (res.code === 200) {
      statistics.value = res.data || {}
    }
  } catch (error) {
    console.error('加载统计数据失败:', error)
  }
}

// 切换用户状态
const toggleUserStatus = async (user) => {
  const newStatus = user.status === 1 ? 0 : 1
  const action = newStatus === 1 ? '启用' : '禁用'
  
  try {
    await ElMessageBox.confirm(
      `确定要${action}用户 "${user.username}" 吗？`,
      '提示',
      { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' }
    )
    
    const res = await request.put(`/admin/users/${user.id}/status`, null, {
      params: { status: newStatus }
    })
    
    if (res.code === 200) {
      ElMessage.success(`${action}成功`)
      loadUsers()
      loadStatistics()
    } else {
      ElMessage.error(res.message || `${action}失败`)
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error(`${action}失败:`, error)
      ElMessage.error(`${action}失败`)
    }
  }
}

// 显示重置密码对话框
const showResetPassword = (user) => {
  passwordForm.value.userId = user.id
  passwordForm.value.newPassword = ''
  passwordDialogVisible.value = true
}

// 重置密码
const resetPassword = async () => {
  if (!passwordForm.value.newPassword) {
    ElMessage.warning('请输入新密码')
    return
  }
  
  if (passwordForm.value.newPassword.length < 6) {
    ElMessage.warning('密码长度不能少于 6 位')
    return
  }
  
  try {
    const res = await request.put(
      `/admin/users/${passwordForm.value.userId}/reset-password`,
      null,
      { params: { newPassword: passwordForm.value.newPassword } }
    )
    
    if (res.code === 200) {
      ElMessage.success('密码重置成功')
      passwordDialogVisible.value = false
    } else {
      ElMessage.error(res.message || '密码重置失败')
    }
  } catch (error) {
    console.error('密码重置失败:', error)
    ElMessage.error('密码重置失败')
  }
}

// 确认删除
const confirmDelete = async (user) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除用户 "${user.username}" 吗？此操作不可恢复！`,
      '警告',
      { confirmButtonText: '确定', cancelButtonText: '取消', type: 'error' }
    )
    
    const res = await request.delete(`/admin/users/${user.id}`)
    
    if (res.code === 200) {
      ElMessage.success('删除成功')
      loadUsers()
      loadStatistics()
    } else {
      ElMessage.error(res.message || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
      ElMessage.error('删除失败')
    }
  }
}

onMounted(() => {
  loadUsers()
  loadStatistics()
})
</script>

<style scoped>
.user-management {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
