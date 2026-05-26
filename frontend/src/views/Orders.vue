<template>
  <div class="orders-page">
    <h2>我的订单</h2>
    <el-tabs v-model="activeTab">
      <el-tab-pane label="全部订单" name="all" />
      <el-tab-pane label="待发货" name="PAID" />
      <el-tab-pane label="待收货" name="SHIPPED" />
      <el-tab-pane label="已收货" name="RECEIVED" />
      <el-tab-pane label="退货中" name="returning" />
      <el-tab-pane label="已完成" name="COMPLETED" />
    </el-tabs>

    <el-table :data="filteredOrders" v-loading="loading" empty-text="暂无订单">
      <el-table-column prop="orderNo" label="订单号" width="180" />
      <el-table-column label="商品" min-width="180">
        <template #default="{ row }">{{ row.product?.name }}</template>
      </el-table-column>
      <el-table-column label="商家" width="120">
        <template #default="{ row }">{{ row.product?.merchant?.storeName || row.product?.merchant?.name }}</template>
      </el-table-column>
      <el-table-column label="数量" width="60">
        <template #default="{ row }">{{ row.quantity }}</template>
      </el-table-column>
      <el-table-column label="金额" width="100">
        <template #default="{ row }">¥{{ row.totalPrice }}</template>
      </el-table-column>
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="statusType(row.status)">{{ statusText(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="时间" width="160">
        <template #default="{ row }">{{ row.createdAt }}</template>
      </el-table-column>
      <el-table-column label="操作" width="200">
        <template #default="{ row }">
          <el-button v-if="row.status === 'SHIPPED'" type="success" size="small"
            @click="confirmReceive(row)">确认收货</el-button>
          <el-button v-if="row.status === 'RECEIVED'" type="warning" size="small"
            @click="showReturnDialog(row)">申请退货</el-button>
          <el-button v-if="row.status === 'COMPLETED'" type="primary" size="small"
            @click="showReviewDialog(row)">评价</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- Return Dialog -->
    <el-dialog v-model="returnDialogVisible" title="申请退货" width="500px">
      <p style="margin-bottom:8px">退货原因：</p>
      <el-input v-model="returnReason" type="textarea" :rows="3" placeholder="请详细描述退货原因" />
      <div style="margin-top:15px">
        <p style="margin-bottom:8px">凭证图片（选填）：</p>
        <el-upload
          list-type="picture-card"
          :http-request="uploadReturnImage"
          :on-remove="onReturnImageRemove"
          :file-list="returnFileList"
          :auto-upload="true"
          multiple
          accept="image/*"
        >
          <el-icon><Plus /></el-icon>
        </el-upload>
      </div>
      <template #footer>
        <el-button @click="returnDialogVisible = false">取消</el-button>
        <el-button type="danger" @click="submitReturn">提交申请</el-button>
      </template>
    </el-dialog>

    <!-- Review Dialog -->
    <el-dialog v-model="reviewDialogVisible" title="评价商品" width="450px">
      <div>
        <p>商品评分：<el-rate v-model="reviewForm.rating" /></p>
        <el-input v-model="reviewForm.content" type="textarea" :rows="3" placeholder="写下您的评价..." />
      </div>
      <template #footer>
        <el-button @click="reviewDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitReview">提交评价</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import api from '../api'

const orders = ref([])
const returns = ref([])
const loading = ref(false)
const activeTab = ref('all')
const returnDialogVisible = ref(false)
const reviewDialogVisible = ref(false)
const returnReason = ref('')
const returnImages = ref([])
const returnFileList = ref([])
const currentOrder = ref(null)
const reviewForm = ref({ rating: 5, content: '' })

const filteredOrders = computed(() => {
  if (activeTab.value === 'all') return orders.value
  return orders.value.filter(o => o.status === activeTab.value)
})

onMounted(() => {
  loadOrders()
})

function loadOrders() {
  loading.value = true
  api.get('/orders').then(res => {
    if (res.data.code === 200) orders.value = res.data.data
  })
  api.get('/orders/returns').then(res => {
    if (res.data.code === 200) returns.value = res.data.data
  }).finally(() => loading.value = false)
}

function confirmReceive(order) {
  api.put(`/orders/${order.id}/receive`).then(res => {
    if (res.data.code === 200) {
      ElMessage.success('已确认收货')
      loadOrders()
    }
  })
}

function showReturnDialog(order) {
  currentOrder.value = order
  returnReason.value = ''
  returnImages.value = []
  returnFileList.value = []
  returnDialogVisible.value = true
}

// 上传退货凭证图片
async function uploadReturnImage(options) {
  const formData = new FormData()
  formData.append('files', options.file)
  try {
    const res = await api.post('/files/upload', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
    if (res.data.code === 200 && res.data.data) {
      returnImages.value.push(...res.data.data)
      options.onSuccess({ url: res.data.data[0] }, options.file)
    } else {
      options.onError(new Error(res.data.message))
    }
  } catch (e) {
    ElMessage.error('上传失败')
    options.onError(e)
  }
}

function onReturnImageRemove(file) {
  if (file.url) {
    const idx = returnImages.value.indexOf(file.url)
    if (idx > -1) returnImages.value.splice(idx, 1)
  }
}

function submitReturn() {
  if (!returnReason.value.trim()) {
    ElMessage.warning('请填写退货原因')
    return
  }
  api.post('/orders/return', {
    orderId: currentOrder.value.id,
    reason: returnReason.value,
    images: returnImages.value.length > 0 ? JSON.stringify(returnImages.value) : null
  }).then(res => {
    if (res.data.code === 200) {
      ElMessage.success('退货申请已提交')
      returnDialogVisible.value = false
      loadOrders()
    }
  })
}

function showReviewDialog(order) {
  currentOrder.value = order
  reviewForm.value = { rating: 5, content: '' }
  reviewDialogVisible.value = true
}

function submitReview() {
  api.post('/reviews', {
    orderId: currentOrder.value.id,
    productId: currentOrder.value.product.id,
    rating: reviewForm.value.rating,
    content: reviewForm.value.content,
    type: 'PRODUCT'
  }).then(res => {
    if (res.data.code === 200) {
      ElMessage.success('评价成功')
      reviewDialogVisible.value = false
    }
  })
}

function statusType(status) {
  const map = { PAID: 'warning', SHIPPED: 'primary', RECEIVED: 'success', RETURNING: 'danger', RETURN_APPROVED: '', RETURN_REJECTED: 'danger', RETURNED: 'info', COMPLETED: 'success' }
  return map[status] || ''
}

function statusText(status) {
  const map = { PAID: '待发货', SHIPPED: '已发货', RECEIVED: '已收货', RETURNING: '退货中', RETURN_APPROVED: '退货通过', RETURN_REJECTED: '退货拒绝', RETURNED: '已退货', COMPLETED: '已完成' }
  return map[status] || status
}
</script>

<style scoped>
.orders-page { max-width: 1000px; margin: 0 auto; }
</style>
