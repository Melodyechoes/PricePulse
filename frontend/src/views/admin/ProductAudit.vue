<template>
  <div class="product-audit">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>商品审核</span>
          <div>
            <el-select v-model="statusFilter" placeholder="审核状态" @change="loadProducts" style="width: 150px; margin-right: 10px;">
              <el-option label="全部" :value="null" />
              <el-option label="已上架" :value="1" />
              <el-option label="已下架" :value="0" />
            </el-select>
            <el-button type="primary" @click="loadProducts">刷新</el-button>
          </div>
        </div>
      </template>

      <!-- 统计卡片 -->
      <el-row :gutter="20" style="margin-bottom: 20px;">
        <el-col :span="8">
          <el-statistic title="总商品数" :value="statistics.totalProducts" />
        </el-col>
        <el-col :span="8">
          <el-statistic title="已上架" :value="statistics.activeProducts" />
        </el-col>
        <el-col :span="8">
          <el-statistic title="已下架" :value="statistics.inactiveProducts" />
        </el-col>
      </el-row>

      <!-- 商品列表表格 -->
      <el-table :data="productList" v-loading="loading" border stripe>
        <el-table-column type="selection" width="55" />
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="商品名称" min-width="200" show-overflow-tooltip />
        <el-table-column prop="platform" label="平台" width="100">
          <template #default="{ row }">
            <el-tag :type="getPlatformType(row.platform)">{{ getPlatformName(row.platform) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="currentPrice" label="当前价格" width="100">
          <template #default="{ row }">
            ¥{{ row.currentPrice?.toFixed(2) || '0.00' }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">
              {{ row.status === 1 ? '已上架' : '已下架' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="添加时间" width="180" />
        <el-table-column label="操作" fixed="right" width="350">
          <template #default="{ row }">
            <el-button
              size="small"
              type="primary"
              @click="showAuditDialog(row)"
            >
              审核
            </el-button>
            <el-button
              size="small"
              type="info"
              @click="viewProduct(row)"
            >
              查看
            </el-button>
            <el-button
              size="small"
              type="danger"
              @click="confirmDelete(row)"
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
        @size-change="loadProducts"
        @current-change="loadProducts"
        style="margin-top: 20px; justify-content: flex-end;"
      />
    </el-card>

    <!-- 审核对话框 -->
    <el-dialog v-model="auditDialogVisible" title="商品审核" width="500px">
      <el-form :model="auditForm" label-width="80px">
        <el-form-item label="商品名称">
          <el-input v-model="auditForm.productName" disabled />
        </el-form-item>
        <el-form-item label="审核状态">
          <el-radio-group v-model="auditForm.status">
            <el-radio :label="1">通过（上架）</el-radio>
            <el-radio :label="0">拒绝（下架）</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="审核意见">
          <el-input
            v-model="auditForm.comment"
            type="textarea"
            :rows="4"
            placeholder="请输入审核意见（选填）"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="auditDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitAudit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 商品详情对话框 -->
    <el-dialog v-model="detailDialogVisible" title="商品详情" width="600px">
      <el-descriptions :column="2" border v-if="currentProduct">
        <el-descriptions-item label="商品 ID">{{ currentProduct.id }}</el-descriptions-item>
        <el-descriptions-item label="商品名称">{{ currentProduct.name }}</el-descriptions-item>
        <el-descriptions-item label="平台">{{ getPlatformName(currentProduct.platform) }}</el-descriptions-item>
        <el-descriptions-item label="品牌">{{ currentProduct.brand || '-' }}</el-descriptions-item>
        <el-descriptions-item label="分类">{{ currentProduct.category || '-' }}</el-descriptions-item>
        <el-descriptions-item label="价格">¥{{ currentProduct.currentPrice?.toFixed(2) || '0.00' }}</el-descriptions-item>
        <el-descriptions-item label="原价">¥{{ currentProduct.originalPrice?.toFixed(2) || '0.00' }}</el-descriptions-item>
        <el-descriptions-item label="折扣率">{{ (currentProduct.discountRate * 100).toFixed(0) }}%</el-descriptions-item>
        <el-descriptions-item label="销量">{{ currentProduct.salesCount || 0 }}</el-descriptions-item>
        <el-descriptions-item label="评分">{{ currentProduct.rating || 0 }}</el-descriptions-item>
        <el-descriptions-item label="评论数">{{ currentProduct.reviewCount || 0 }}</el-descriptions-item>
        <el-descriptions-item label="库存状态">
          {{ currentProduct.stockStatus === 1 ? '有货' : '缺货' }}
        </el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="currentProduct.status === 1 ? 'success' : 'info'">
            {{ currentProduct.status === 1 ? '已上架' : '已下架' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="添加时间">{{ currentProduct.createdAt }}</el-descriptions-item>
        <el-descriptions-item label="更新时间">{{ currentProduct.updatedAt }}</el-descriptions-item>
        <el-descriptions-item label="商品链接" :span="2">
          <el-link :href="currentProduct.url" target="_blank" type="primary">点击查看</el-link>
        </el-descriptions-item>
        <el-descriptions-item label="描述" :span="2">
          {{ currentProduct.description || '无' }}
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'

const loading = ref(false)
const productList = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const statusFilter = ref(null)
const statistics = ref({})

const auditDialogVisible = ref(false)
const auditForm = ref({
  productId: null,
  productName: '',
  status: 1,
  comment: ''
})

const detailDialogVisible = ref(false)
const currentProduct = ref(null)

// 加载商品列表
const loadProducts = async () => {
  loading.value = true
  try {
    const res = await request.get('/api/admin/products', {
      params: {
        page: currentPage.value,
        pageSize: pageSize.value,
        status: statusFilter.value
      }
    })
    
    if (res.code === 200) {
      productList.value = res.data.list || []
      total.value = res.data.total || 0
    } else {
      ElMessage.error(res.message || '加载商品列表失败')
    }
  } catch (error) {
    console.error('加载商品列表失败:', error)
    ElMessage.error('加载失败')
  } finally {
    loading.value = false
  }
}

// 加载统计数据
const loadStatistics = async () => {
  try {
    const res = await request.get('/api/admin/products/statistics')
    if (res.code === 200) {
      statistics.value = res.data || {}
    }
  } catch (error) {
    console.error('加载统计数据失败:', error)
  }
}

// 显示审核对话框
const showAuditDialog = (product) => {
  auditForm.value.productId = product.id
  auditForm.value.productName = product.name
  auditForm.value.status = product.status
  auditForm.value.comment = ''
  auditDialogVisible.value = true
}

// 提交审核
const submitAudit = async () => {
  try {
    const res = await request.put(
      `/api/admin/products/${auditForm.value.productId}/status`,
      null,
      {
        params: {
          status: auditForm.value.status,
          auditComment: auditForm.value.comment
        }
      }
    )
    
    if (res.code === 200) {
      ElMessage.success('审核成功')
      auditDialogVisible.value = false
      loadProducts()
      loadStatistics()
    } else {
      ElMessage.error(res.message || '审核失败')
    }
  } catch (error) {
    console.error('审核失败:', error)
    ElMessage.error('审核失败')
  }
}

// 查看商品详情
const viewProduct = async (product) => {
  try {
    const res = await request.get(`/api/admin/products/${product.id}`)
    if (res.code === 200) {
      currentProduct.value = res.data
      detailDialogVisible.value = true
    } else {
      ElMessage.error(res.message || '获取商品详情失败')
    }
  } catch (error) {
    console.error('获取商品详情失败:', error)
    ElMessage.error('获取失败')
  }
}

// 确认删除
const confirmDelete = async (product) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除商品 "${product.name}" 吗？此操作不可恢复！`,
      '警告',
      { confirmButtonText: '确定', cancelButtonText: '取消', type: 'error' }
    )
    
    const res = await request.delete(`/api/admin/products/${product.id}`)
    
    if (res.code === 200) {
      ElMessage.success('删除成功')
      loadProducts()
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

// 获取平台名称
const getPlatformName = (platform) => {
  const platformMap = {
    'pdd': '拼多多',
    'jd': '京东',
    'taobao': '淘宝',
    'tmall': '天猫'
  }
  return platformMap[platform] || platform || '其他'
}

// 获取平台类型
const getPlatformType = (platform) => {
  const typeMap = {
    'pdd': 'success',
    'jd': 'primary',
    'taobao': 'warning',
    'tmall': 'danger'
  }
  return typeMap[platform] || 'info'
}

onMounted(() => {
  loadProducts()
  loadStatistics()
})
</script>

<style scoped>
.product-audit {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
