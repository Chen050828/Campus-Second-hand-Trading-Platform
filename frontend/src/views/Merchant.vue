<template>
  <div class="merchant-page">
    <h2>商家管理中心</h2>
    <el-tabs v-model="activeTab">
      <!-- Publish Product -->
      <el-tab-pane label="发布商品" name="publish">
        <el-card>
          <el-form :model="form" label-width="100px" style="max-width:600px">
            <el-form-item label="商品名称" required>
              <el-input v-model="form.name" />
            </el-form-item>
            <el-form-item label="分类">
              <el-select v-model="form.categoryId" placeholder="选择分类">
                <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" />
              </el-select>
            </el-form-item>
            <el-form-item label="原价" required>
              <el-input-number v-model="form.originalPrice" :min="0" :precision="2" />
            </el-form-item>
            <el-form-item label="售价" required>
              <el-input-number v-model="form.discountPrice" :min="0" :precision="2" />
            </el-form-item>
            <el-form-item label="数量" required>
              <el-input-number v-model="form.quantity" :min="1" />
            </el-form-item>
            <el-form-item label="新旧程度">
              <el-select v-model="form.condition_">
                <el-option label="全新" value="全新" />
                <el-option label="九五成新" value="九五成新" />
                <el-option label="九成新" value="九成新" />
                <el-option label="八成新" value="八成新" />
                <el-option label="七成新" value="七成新" />
                <el-option label="六成新及以下" value="六成新及以下" />
              </el-select>
            </el-form-item>
            <el-form-item label="尺寸">
              <el-input v-model="form.size" placeholder="如：L码、42号等" />
            </el-form-item>
            <el-form-item label="商品描述">
              <el-input v-model="form.description" type="textarea" :rows="3" />
            </el-form-item>
            <el-form-item label="使用说明">
              <el-input v-model="form.usageNotes" type="textarea" :rows="2" />
            </el-form-item>
            <el-form-item label="允许议价">
              <el-switch v-model="form.allowBargain" />
            </el-form-item>
            <el-form-item label="商品图片">
              <div class="upload-area">
                <el-upload
                  ref="uploadRef"
                  list-type="picture-card"
                  :http-request="customUpload"
                  :on-remove="onUploadRemove"
                  :auto-upload="true"
                  :file-list="fileList"
                  multiple
                  accept="image/*"
                >
                  <el-icon><Plus /></el-icon>
                </el-upload>
                <span style="color:#909399;font-size:12px">支持多张图片，上传后自动保存</span>
              </div>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="publish">发布商品</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-tab-pane>

      <!-- My Products -->
      <el-tab-pane label="我的商品" name="products">
        <el-radio-group v-model="productFilter" @change="loadProducts" style="margin-bottom:15px">
          <el-radio-button value="ALL">全部</el-radio-button>
          <el-radio-button value="APPROVED">已上架</el-radio-button>
          <el-radio-button value="PENDING">审核中</el-radio-button>
          <el-radio-button value="SOLD_OUT">已售罄</el-radio-button>
          <el-radio-button value="DELISTED">已下架</el-radio-button>
        </el-radio-group>
        <el-table :data="products" v-loading="loading">
          <el-table-column prop="name" label="商品名称" />
          <el-table-column label="价格">
            <template #default="{ row }">¥{{ row.discountPrice }}</template>
          </el-table-column>
          <el-table-column prop="quantity" label="库存" width="80" />
          <el-table-column prop="salesCount" label="销量" width="80" />
          <el-table-column prop="delistReason" label="管理员反馈" min-width="120">
            <template #default="{ row }">
              <span v-if="row.delistReason" style="color:#f56c6c">{{ row.delistReason }}</span>
              <span v-else>-</span>
            </template>
          </el-table-column>
          <el-table-column label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="productStatusType(row.status)">{{ row.status }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="150">
            <template #default="{ row }">
              <el-button v-if="row.status === 'APPROVED'" type="danger" size="small"
                @click="delistProduct(row.id)">下架</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <!-- Orders -->
      <el-tab-pane label="订单管理" name="orders">
        <el-table :data="merchantOrders" v-loading="loadingOrders">
          <el-table-column prop="orderNo" label="订单号" width="180" />
          <el-table-column prop="product.name" label="商品" />
          <el-table-column label="数量" width="60"><template #default="{ row }">{{ row.quantity }}</template></el-table-column>
          <el-table-column label="金额" width="100"><template #default="{ row }">¥{{ row.totalPrice }}</template></el-table-column>
          <el-table-column label="状态" width="100">
            <template #default="{ row }"><el-tag>{{ row.status }}</el-tag></template>
          </el-table-column>
          <el-table-column prop="createdAt" label="时间" width="160" />
        </el-table>
      </el-tab-pane>

      <!-- Return Requests -->
      <el-tab-pane label="退货审核" name="returns">
        <el-table :data="returnList" v-loading="loadingReturns">
          <el-table-column prop="order.orderNo" label="订单号" width="180" />
          <el-table-column prop="order.product.name" label="商品" />
          <el-table-column prop="reason" label="退货原因" />
          <el-table-column prop="status" label="状态" width="80" />
          <el-table-column label="操作" width="200">
            <template #default="{ row }">
              <template v-if="row.status === 'PENDING'">
                <el-button type="success" size="small" @click="approveReturn(row.id)">同意</el-button>
                <el-button type="danger" size="small" @click="rejectReturn(row.id)">拒绝</el-button>
              </template>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import api from '../api'

const activeTab = ref('publish')
const categories = ref([])
const products = ref([])
const merchantOrders = ref([])
const returnList = ref([])
const loading = ref(false)
const loadingOrders = ref(false)
const loadingReturns = ref(false)
const productFilter = ref('ALL')
const uploadRef = ref(null)
const uploadedImages = ref([])
const fileList = ref([])

const form = ref({
  name: '', categoryId: null, originalPrice: 0, discountPrice: 0,
  quantity: 1, condition_: '九成新', size: '', description: '',
  usageNotes: '', allowBargain: false
})

onMounted(() => {
  api.get('/categories').then(res => { if (res.data.code === 200) categories.value = res.data.data })
  loadProducts()
  loadOrders()
  loadReturns()
})

async function customUpload(options) {
  const formData = new FormData()
  formData.append('files', options.file)
  try {
    const res = await api.post('/files/upload', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
    if (res.data.code === 200 && res.data.data) {
      const urls = res.data.data
      uploadedImages.value.push(...urls)
      // Tell el-upload the upload succeeded
      options.onSuccess({ url: urls[0] }, options.file)
    } else {
      ElMessage.error(res.data.message || '上传失败')
      options.onError(new Error(res.data.message))
    }
  } catch (e) {
    ElMessage.error('上传失败')
    options.onError(e)
  }
}

function onUploadRemove(file) {
  // Remove the URL associated with this file from uploadedImages
  if (file.url) {
    const idx = uploadedImages.value.indexOf(file.url)
    if (idx > -1) uploadedImages.value.splice(idx, 1)
  }
}

function publish() {
  const payload = {
    ...form.value,
    images: JSON.stringify(uploadedImages.value)
  }
  api.post('/products/merchant/publish', payload).then(res => {
    if (res.data.code === 200) {
      ElMessage.success('发布成功，等待审核')
      uploadedImages.value = []
      fileList.value = []
      uploadRef.value?.clearFiles()
    }
  })
}

function loadProducts() {
  loading.value = true
  const params = productFilter.value !== 'ALL' ? { status: productFilter.value } : {}
  api.get('/products/merchant/my', { params }).then(res => {
    if (res.data.code === 200) products.value = res.data.data
  }).finally(() => loading.value = false)
}

function delistProduct(id) {
  api.put(`/products/merchant/delist/${id}`).then(res => {
    if (res.data.code === 200) { ElMessage.success('已下架'); loadProducts() }
  })
}

function loadOrders() {
  loadingOrders.value = true
  api.get('/orders/merchant').then(res => {
    if (res.data.code === 200) merchantOrders.value = res.data.data
  }).finally(() => loadingOrders.value = false)
}

function loadReturns() {
  loadingReturns.value = true
  api.get('/orders/merchant/returns').then(res => {
    if (res.data.code === 200) returnList.value = res.data.data
  }).finally(() => loadingReturns.value = false)
}

function approveReturn(id) {
  api.put(`/orders/merchant/return/${id}/approve`).then(res => {
    if (res.data.code === 200) { ElMessage.success('已同意退货'); loadReturns(); loadOrders() }
  })
}

function rejectReturn(id) {
  api.put(`/orders/merchant/return/${id}/reject`, '"商家拒绝了退货申请"', {
    headers: { 'Content-Type': 'application/json' }
  }).then(res => {
    if (res.data.code === 200) { ElMessage.success('已拒绝'); loadReturns(); loadOrders() }
  })
}

function productStatusType(status) {
  return { APPROVED: 'success', PENDING: 'warning', REJECTED: 'danger', SOLD_OUT: 'info', DELISTED: '' }[status] || ''
}
</script>

<style scoped>
.merchant-page { max-width: 1000px; margin: 0 auto; }
</style>
