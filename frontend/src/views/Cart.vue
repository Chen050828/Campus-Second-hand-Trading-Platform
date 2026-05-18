<template>
  <div class="cart-page">
    <h2>我的购物车</h2>
    <el-table :data="cartItems" v-loading="loading" empty-text="购物车为空">
      <el-table-column prop="product.name" label="商品名称" min-width="200">
        <template #default="{ row }">
          <el-link type="primary" @click="$router.push(`/product/${row.product.id}`)">
            {{ row.product.name }}
          </el-link>
        </template>
      </el-table-column>
      <el-table-column label="商家" width="150">
        <template #default="{ row }">
          {{ row.product.merchant?.storeName || row.product.merchant?.name }}
        </template>
      </el-table-column>
      <el-table-column prop="product.discountPrice" label="单价" width="100">
        <template #default="{ row }">¥{{ row.product.discountPrice }}</template>
      </el-table-column>
      <el-table-column label="数量" width="150">
        <template #default="{ row }">
          <el-input-number v-model="row.quantity" :min="1" :max="row.product.quantity"
            size="small" @change="updateQty(row)" />
        </template>
      </el-table-column>
      <el-table-column label="小计" width="100">
        <template #default="{ row }">¥{{ (row.product.discountPrice * row.quantity).toFixed(2) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="100">
        <template #default="{ row }">
          <el-button type="danger" size="small" text @click="removeItem(row.product.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div v-if="cartItems.length > 0" class="cart-footer">
      <div class="total">
        合计：<span class="total-price">¥{{ totalPrice.toFixed(2) }}</span>
      </div>
      <div class="meet-info">
        <el-input v-model="meetLocation" placeholder="约定交易地点" style="width:200px" />
        <el-date-picker v-model="meetTime" type="datetime" placeholder="约定交易时间" />
      </div>
      <el-button type="danger" size="large" @click="checkout">一键下单</el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import api from '../api'

const router = useRouter()
const cartItems = ref([])
const loading = ref(false)
const meetLocation = ref('')
const meetTime = ref('')

const totalPrice = computed(() =>
  cartItems.value.reduce((sum, item) => sum + item.product.discountPrice * item.quantity, 0)
)

onMounted(loadCart)

function loadCart() {
  loading.value = true
  api.get('/cart').then(res => {
    if (res.data.code === 200) cartItems.value = res.data.data
  }).finally(() => loading.value = false)
}

function updateQty(item) {
  api.put(`/cart/update/${item.product.id}`, { quantity: item.quantity })
}

function removeItem(productId) {
  api.delete(`/cart/remove/${productId}`).then(res => {
    if (res.data.code === 200) {
      cartItems.value = cartItems.value.filter(i => i.product.id !== productId)
      ElMessage.success('已移除')
    }
  })
}

function checkout() {
  ElMessageBox.confirm(`确认下单？共计 ¥${totalPrice.value.toFixed(2)}`, '确认下单', {
    confirmButtonText: '确认',
    type: 'warning'
  }).then(() => {
    const items = cartItems.value.map(i => ({ productId: i.product.id, quantity: i.quantity }))
    api.post('/orders', { meetLocation: meetLocation.value, meetTime: meetTime.value, items }).then(res => {
      if (res.data.code === 200) {
        ElMessage.success('下单成功')
        router.push('/orders')
      } else {
        ElMessage.error(res.data.message)
      }
    })
  }).catch(() => {})
}
</script>

<style scoped>
.cart-page { max-width: 1000px; margin: 0 auto; }
.cart-footer { margin-top: 20px; display: flex; align-items: center; gap: 15px; justify-content: flex-end; flex-wrap: wrap; }
.total { font-size: 18px; }
.total-price { color: #f56c6c; font-size: 24px; font-weight: bold; }
.meet-info { display: flex; gap: 10px; }
</style>
