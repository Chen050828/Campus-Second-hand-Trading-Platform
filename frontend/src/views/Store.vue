<template>
  <div class="store-page" v-loading="loading">
    <div class="store-header" v-if="merchant">
      <h2>{{ merchant.storeName || merchant.name }}的店</h2>
      <div class="store-meta">
        <span>商家等级：Lv.{{ merchant.merchantLevel || 1 }}</span>
        <span>服务评分：
          <el-rate :model-value="merchant.serviceRating || 5" disabled size="small" />
          {{ merchant.serviceRatingCount || 0 }}条评价
        </span>
      </div>
    </div>

    <h3>在售商品</h3>
    <div class="product-grid">
      <div class="product-card" v-for="p in products" :key="p.id"
        @click="$router.push(`/product/${p.id}`)">
        <div class="product-image">
          <img v-if="getFirstImage(p.images)" :src="getFirstImage(p.images)" alt="" />
          <div v-else class="no-image"><el-icon size="40"><PictureFilled /></el-icon></div>
        </div>
        <div class="product-info">
          <h4>{{ p.name }}</h4>
          <p class="price">¥{{ p.discountPrice }}</p>
          <p>已售 {{ p.salesCount }} | 库存 {{ p.quantity }}</p>
        </div>
      </div>
    </div>
    <el-empty v-if="!loading && products.length === 0" description="该店铺暂无在售商品" />

    <!-- Service Reviews -->
    <h3 style="margin-top:30px">服务评价</h3>
    <div v-if="serviceReviews.length === 0">
      <el-empty description="暂无服务评价" />
    </div>
    <div v-for="r in serviceReviews" :key="r.id" class="review-item">
      <div class="review-header">
        <span class="review-user">{{ r.user.name }}</span>
        <el-rate :model-value="r.rating" disabled size="small" />
        <span class="review-time">{{ r.createdAt }}</span>
      </div>
      <p>{{ r.content }}</p>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import api from '../api'

const route = useRoute()
const merchant = ref(null)
const products = ref([])
const serviceReviews = ref([])
const loading = ref(false)

onMounted(() => {
  loading.value = true
  // Load merchant info
  api.get(`/user/info/${route.params.id}`).then(res => {
    if (res.data.code === 200) merchant.value = res.data.data
  })
  // Load merchant products (from store search page we'd filter)
  // Simplified: use products API with merchant filter
  api.get('/products').then(res => {
    if (res.data.code === 200) {
      products.value = res.data.data.filter(p =>
        p.merchant && p.merchant.id == route.params.id)
    }
  })
  // Load service reviews
  api.get(`/reviews/merchant/${route.params.id}/service`).then(res => {
    if (res.data.code === 200) serviceReviews.value = res.data.data
  }).finally(() => loading.value = false)
})

function getFirstImage(images) {
  if (!images) return null
  try {
    const arr = JSON.parse(images)
    if (Array.isArray(arr) && arr.length > 0) return arr[0]
    return null
  } catch {
    if (images.match(/^https?:\/\//) || images.startsWith('/api/uploads/')) return images
    return null
  }
}
</script>

<style scoped>
.store-page { max-width: 1000px; margin: 0 auto; }
.store-header { background: #fff; padding: 20px; border-radius: 8px; margin-bottom: 20px; }
.store-meta { display: flex; gap: 20px; margin-top: 10px; color: #606266; }
.product-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(230px, 1fr)); gap: 20px; margin-top: 15px; }
.product-card { background: #fff; border-radius: 8px; overflow: hidden; cursor: pointer; border: 1px solid #ebeef5; transition: transform 0.2s; }
.product-card:hover { transform: translateY(-3px); box-shadow: 0 4px 12px rgba(0,0,0,0.08); }
.product-image { height: 180px; display: flex; align-items: center; justify-content: center; background: #f5f7fa; }
.product-image img { width: 100%; height: 100%; object-fit: cover; }
.product-info { padding: 10px 15px; }
.product-info h4 { margin: 0 0 5px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.price { color: #f56c6c; font-size: 16px; font-weight: bold; }
.review-item { padding: 12px 0; border-bottom: 1px solid #ebeef5; }
.review-header { display: flex; align-items: center; gap: 10px; margin-bottom: 6px; }
.review-user { font-weight: bold; }
.review-time { color: #c0c4cc; font-size: 12px; }
</style>
