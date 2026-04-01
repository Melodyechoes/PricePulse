<template>
  <div class="add-product-container">
    <el-card class="add-product-card">
      <template #header>
        <div class="card-header">
          <span>添加商品</span>
          <el-button @click="$router.push('/products')">返回列表</el-button>
        </div>
      </template>

      <el-alert
          title="提示"
          type="info"
          :closable="false"
          style="margin-bottom: 20px;"
      >
        请输入京东、淘宝或拼多多的商品链接，点击"解析"按钮自动获取商品信息
      </el-alert>

      <el-form :model="formData" label-width="120px">
        <el-form-item label="商品链接">
          <el-input
              v-model="formData.productUrl"
              placeholder="例如：https://item.jd.com/100012345678.html"
              clearable
              @keyup.enter="parseUrl"
          >
            <template #prefix>
              <el-icon><Link /></el-icon>
            </template>
            <template #append>
              <el-button
                  type="primary"
                  :loading="parsing"
                  @click="parseUrl"
                  :disabled="!formData.productUrl"
              >
                <el-icon v-if="!parsing"><Search /></el-icon>
                {{ parsing ? '解析中...' : '解析' }}
              </el-button>
            </template>
          </el-input>
        </el-form-item>

        <el-divider v-if="parsedProduct" />

        <div v-if="parsedProduct" class="preview-section">
          <h3>商品信息预览</h3>

          <el-row :gutter="20">
            <el-col :span="8">
              <div class="product-image">
                <el-image
                    :src="parsedProduct.imageUrl || '/placeholder.png'"
                    fit="cover"
                    style="width: 100%; height: 300px;"
                />
              </div>
            </el-col>

            <el-col :span="16">
              <el-form :model="parsedProduct" label-width="100px">
                <el-form-item label="商品名称" required>
                  <el-input v-model="parsedProduct.name" />
                </el-form-item>

                <el-form-item label="商品分类">
                  <el-select v-model="parsedProduct.category" placeholder="请选择分类（可选）" clearable>
                    <el-option label="数码" value="数码" />
                    <el-option label="服装" value="服装" />
                    <el-option label="食品" value="食品" />
                    <el-option label="家居" value="家居" />
                    <el-option label="美妆" value="美妆" />
                    <el-option label="其他" value="其他" />
                  </el-select>
                </el-form-item>

                <el-form-item label="当前价格" required>
                  <el-input-number
                      v-model="parsedProduct.currentPrice"
                      :precision="2"
                      :min="0.01"
                      style="width: 100%;"
                  />
                </el-form-item>

                <el-form-item label="原价">
                  <el-input-number
                      v-model="parsedProduct.originalPrice"
                      :precision="2"
                      :min="0"
                      placeholder="可不填，默认与现价相同"
                      style="width: 100%;"
                  />
                </el-form-item>

                <el-form-item label="折扣率 (%)">
                  <el-input-number
                      v-model="parsedProduct.discountRate"
                      :precision="2"
                      :min="0"
                      :max="100"
                      placeholder="自动计算"
                      :disabled="true"
                      style="width: 100%;"
                  />
                </el-form-item>

                <el-form-item label="商品描述">
                  <el-input
                      v-model="parsedProduct.description"
                      type="textarea"
                      :rows="3"
                      placeholder="可不填"
                  />
                </el-form-item>
              </el-form>
            </el-col>
          </el-row>

          <el-button type="primary" :loading="saving" @click="saveProduct" style="margin-top: 20px;">
            确认添加
          </el-button>
        </div>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Link, Search } from '@element-plus/icons-vue'
import request from '@/utils/request'

const router = useRouter()

const parsing = ref(false)
const saving = ref(false)

const formData = reactive({
  productUrl: ''
})

const parsedProduct = ref(null)

// 监听原价变化，自动计算折扣率
watch(() => parsedProduct.value?.originalPrice, (newVal) => {
  if (parsedProduct.value && newVal && parsedProduct.value.currentPrice) {
    const rate = parsedProduct.value.currentPrice / newVal * 100
    parsedProduct.value.discountRate = Math.round(rate * 100) / 100
  }
})

// 监听现价变化，自动计算折扣率
watch(() => parsedProduct.value?.currentPrice, (newVal) => {
  if (parsedProduct.value && newVal && parsedProduct.value.originalPrice) {
    const rate = newVal / parsedProduct.value.originalPrice * 100
    parsedProduct.value.discountRate = Math.round(rate * 100) / 100
  }
})

const parseUrl = async () => {
  if (!formData.productUrl) {
    ElMessage.warning('请输入商品链接')
    return
  }

  // 验证 URL 格式
  try {
    new URL(formData.productUrl)
  } catch (e) {
    ElMessage.warning('请输入有效的 URL 地址')
    return
  }

  parsing.value = true
  parsedProduct.value = null

  try {
    const res = await request.post('/api/products/parse-url', {
      url: formData.productUrl
    })

    if (res.code === 200 && res.data) {
      parsedProduct.value = res.data
      // 如果原价为空，设置为与现价相同
      if (!parsedProduct.value.originalPrice) {
        parsedProduct.value.originalPrice = parsedProduct.value.currentPrice
      }
      ElMessage.success('解析成功')
    } else {
      ElMessage.error(res.message || '解析失败')
    }
  } catch (error) {
    console.error('解析 URL 失败:', error)
    ElMessage.error('解析失败，请检查链接是否正确')
  } finally {
    parsing.value = false
  }
}

const saveProduct = async () => {
  if (!parsedProduct.value) {
    ElMessage.warning('请先解析商品链接')
    return
  }

  // 验证必填字段
  if (!parsedProduct.value.name || !parsedProduct.value.name.trim()) {
    ElMessage.error('商品名称不能为空')
    return
  }
  if (!parsedProduct.value.currentPrice || parsedProduct.value.currentPrice <= 0) {
    ElMessage.error('商品价格必须大于 0')
    return
  }

  saving.value = true
  try {
    const res = await request.post('/api/products', parsedProduct.value)

    if (res.code === 200) {
      ElMessage.success('添加成功')
      router.push('/products')
    } else {
      ElMessage.error(res.message || '添加失败')
    }
  } catch (error) {
    console.error('保存商品失败:', error)
    ElMessage.error('添加失败，请重试')
  } finally {
    saving.value = false
  }
}
</script>


<style scoped>
.add-product-container {
  padding: 20px;
}

.add-product-card {
  max-width: 1200px;
  margin: 0 auto;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.preview-section h3 {
  margin-bottom: 20px;
  color: #303133;
}

.product-image {
  background: #f5f7fa;
  border-radius: 8px;
  padding: 20px;
  text-align: center;
}
</style>
