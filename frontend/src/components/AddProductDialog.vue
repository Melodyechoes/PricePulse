<template>
  <el-dialog
      v-model="dialogVisible"
      title="添加商品"
      width="600px"
      :close-on-click-modal="false"
      @closed="handleClose"
  >
    <el-form
        ref="productFormRef"
        :model="productForm"
        :rules="productRules"
        label-width="100px"
    >
      <el-form-item label="商品链接" prop="url">
        <div style="display: flex; gap: 10px; align-items: flex-start;">
          <el-input
              v-model="productForm.url"
              placeholder="请输入淘宝/京东/拼多多商品链接"
              type="textarea"
              :rows="2"
              @blur="parseUrlIfNeeded"
              style="flex: 1;"
          />
          <el-button
              type="primary"
              :loading="parsingUrl"
              @click="parseUrl"
              :disabled="!productForm.url"
              style="margin-top: 8px;"
          >
            <el-icon v-if="!parsingUrl"><Search /></el-icon>
            {{ parsingUrl ? '解析中...' : '解析' }}
          </el-button>
        </div>
      </el-form-item>

      <el-alert
          v-if="parsedInfo"
          title="已自动解析商品信息"
          type="success"
          :closable="false"
          show-icon
          style="margin-bottom: 15px;"
      />

      <el-form-item label="商品名称" prop="name">
        <el-input
            v-model="productForm.name"
            placeholder="请输入商品名称"
            maxlength="100"
            show-word-limit
        />
      </el-form-item>

      <el-form-item label="当前价格" prop="currentPrice">
        <el-input-number
            v-model="productForm.currentPrice"
            :min="0.01"
            :precision="2"
            :step="0.01"
            style="width: 100%"
        />
      </el-form-item>

      <el-form-item label="原价">
        <el-input-number
            v-model="productForm.originalPrice"
            :min="0"
            :precision="2"
            :step="0.01"
            placeholder="可不填，默认与现价相同"
            style="width: 100%"
        />
      </el-form-item>

      <el-form-item label="折扣率 (%)">
        <el-input-number
            v-model="discountRateDisplay"
            :precision="2"
            :min="0"
            :max="100"
            :disabled="true"
            style="width: 100%"
        />
      </el-form-item>

      <el-form-item label="商品平台" prop="platform">
        <el-select v-model="productForm.platform" placeholder="请选择平台" style="width: 100%">
          <el-option label="淘宝" value="taobao" />
          <el-option label="天猫" value="tmall" />
          <el-option label="京东" value="jd" />
          <el-option label="拼多多" value="pdd" />
        </el-select>
      </el-form-item>

      <el-form-item label="分类">
        <el-select v-model="productForm.category" placeholder="请选择分类（可选）" clearable style="width: 100%">
          <el-option label="数码" value="digital" />
          <el-option label="服装" value="clothing" />
          <el-option label="家居" value="home" />
          <el-option label="美妆" value="beauty" />
          <el-option label="食品" value="food" />
          <el-option label="其他" value="other" />
        </el-select>
      </el-form-item>

      <el-form-item label="品牌">
        <el-input
            v-model="productForm.brand"
            placeholder="请输入品牌名称（可选）"
            maxlength="50"
        />
      </el-form-item>

      <el-form-item label="提醒阈值" prop="alertThreshold">
        <el-slider
            v-model="productForm.alertThreshold"
            :min="0"
            :max="1"
            :step="0.01"
            :format-tooltip="formatTooltip"
            style="width: 100%"
        />
        <div style="text-align: center; color: #999; font-size: 12px; margin-top: 5px;">
          降价超过 {{ (productForm.alertThreshold * 100).toFixed(0) }}% 时提醒我
        </div>
      </el-form-item>

      <el-form-item label="商品图片">
        <el-input
            v-model="productForm.imageUrl"
            placeholder="请输入图片 URL（可选）"
        />
      </el-form-item>

      <el-form-item label="备注">
        <el-input
            v-model="productForm.description"
            type="textarea"
            :rows="2"
            placeholder="商品描述或备注（可选）"
            maxlength="500"
            show-word-limit
        />
      </el-form-item>
    </el-form>

    <template #footer>
      <span class="dialog-footer">
        <el-button @click="dialogVisible = false">取 消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">
          {{ submitLoading ? '提交中...' : '确 定' }}
        </el-button>
      </span>
    </template>
  </el-dialog>
</template>

<script setup>/* eslint-disable no-undef */
import { ref, reactive, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import request from '@/utils/request'
import { addProduct } from '@/api/products'

const props = defineProps({
  modelValue: Boolean
})

const emit = defineEmits(['update:modelValue', 'success'])

const productFormRef = ref(null)
const dialogVisible = ref(false)
const submitLoading = ref(false)
const parsingUrl = ref(false)
const parsedInfo = ref(false)

const productForm = reactive({
  name: '',
  url: '',
  currentPrice: 0,
  originalPrice: null,
  platform: '',
  category: '',
  brand: '',
  alertThreshold: 0.1,
  imageUrl: '',
  description: ''
})

const discountRateDisplay = ref(100)

const validateUrl = (rule, value, callback) => {
  if (!value) {
    callback(new Error('请输入商品链接'))
  } else if (!value.includes('taobao.com') && !value.includes('tmall.com') &&
      !value.includes('jd.com') && !value.includes('yangkeduo.com') &&
      !value.includes('pinduoduo.com')) {
    callback(new Error('仅支持淘宝、天猫、京东、拼多多平台的商品链接'))
  } else {
    callback()
  }
}

const productRules = {
  name: [
    { required: true, message: '请输入商品名称', trigger: 'blur' },
    { min: 2, max: 100, message: '商品名称长度在 2 到 100 个字符', trigger: 'blur' }
  ],
  url: [
    { validator: validateUrl, trigger: 'blur' }
  ],
  currentPrice: [
    { required: true, message: '请输入当前价格', trigger: 'blur' }
  ],
  platform: [
    { required: true, message: '请选择商品平台', trigger: 'change' }
  ]
}

const formatTooltip = (value) => {
  return `${(value * 100).toFixed(0)}%`
}

// 监听原价和现价变化，自动计算折扣率
watch([() => productForm.originalPrice, () => productForm.currentPrice], ([original, current]) => {
  if (original && current) {
    const rate = (current / original) * 100
    discountRateDisplay.value = Math.round(rate * 100) / 100
  } else if (current) {
    discountRateDisplay.value = 100
  }
})

watch(() => props.modelValue, (val) => {
  dialogVisible.value = val
}, { immediate: true })

watch(dialogVisible, (val) => {
  emit('update:modelValue', val)
  if (!val) {
    resetForm()
  }
})

const parseUrl = async () => {
  if (!productForm.url) {
    ElMessage.warning('请输入商品链接')
    return
  }

  // 验证 URL 格式
  try {
    new URL(productForm.url)
  } catch (e) {
    ElMessage.warning('请输入有效的 URL 地址')
    return
  }

  parsingUrl.value = true
  parsedInfo.value = false

  try {
    const res = await request.post('/products/parse-url', {
      url: productForm.url
    })

    if (res.code === 200 && res.data) {
      const data = res.data

      // 填充表单数据
      productForm.name = data.name || ''
      productForm.currentPrice = data.currentPrice || 0
      productForm.originalPrice = data.originalPrice || data.currentPrice || 0
      productForm.platform = data.platform || ''
      productForm.category = data.category || ''
      productForm.imageUrl = data.imageUrl || ''
      productForm.description = data.description || ''

      parsedInfo.value = true
      ElMessage.success('解析成功')
    } else {
      ElMessage.error(res.message || '解析失败')
    }
  } catch (error) {
    console.error('解析 URL 失败:', error)
    ElMessage.error('解析失败，请检查链接是否正确')
  } finally {
    parsingUrl.value = false
  }
}

// 当 URL 输入框失去焦点时，如果已经有 URL 且未解析，提示用户是否解析
const parseUrlIfNeeded = () => {
  if (productForm.url && !parsedInfo.value && !parsingUrl.value) {
    // 可以选择自动解析或提示用户
    // 这里不自动解析，让用户手动点击
  }
}

const resetForm = () => {
  if (productFormRef.value) {
    productFormRef.value.resetFields()
  }
  productForm.name = ''
  productForm.url = ''
  productForm.currentPrice = 0
  productForm.originalPrice = null
  productForm.platform = ''
  productForm.category = ''
  productForm.brand = ''
  productForm.alertThreshold = 0.1
  productForm.imageUrl = ''
  productForm.description = ''
  parsedInfo.value = false
  discountRateDisplay.value = 100
}

const handleSubmit = async () => {
  if (!productFormRef.value) return

  await productFormRef.value.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true
      try {
        await addProduct(productForm)
        ElMessage.success('商品添加成功')
        dialogVisible.value = false
        emit('success')
      } catch (error) {
        console.error('添加商品失败:', error)
      } finally {
        submitLoading.value = false
      }
    }
  })
}

const handleClose = () => {
  resetForm()
}
</script>

<style scoped>
.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
</style>
