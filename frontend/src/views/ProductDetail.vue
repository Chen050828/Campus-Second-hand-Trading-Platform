<template>
  <div class="product-detail" v-loading="loading">
    <div class="detail-main" v-if="product">
      <!-- Images and Info -->
      <div class="detail-top">
        <div class="product-images">
          <img v-if="images.length > 0" :src="images[currentImage]" alt="" class="main-image" />
          <div v-else class="no-image-main">
            <el-icon size="80"><PictureFilled /></el-icon>
          </div>
          <div class="image-thumbs" v-if="images.length > 1">
            <img v-for="(img, i) in images" :key="i" :src="img"
              :class="{ active: i === currentImage }" @click="currentImage = i" />
          </div>
        </div>
        <div class="product-detail-info">
          <h2>{{ product.name }}</h2>
          <div class="price-row">
            <span class="current-price">¥{{ product.discountPrice }}</span>
            <span class="orig-price" v-if="product.originalPrice > product.discountPrice">
              ¥{{ product.originalPrice }}
            </span>
          </div>
          <div class="info-items">
            <div><label>商家：</label>
              <el-link type="primary" @click="$router.push(`/merchant/store/${product.merchant.id}`)">
                {{ product.merchant.storeName || product.merchant.name }}的店
              </el-link>
            </div>
            <div><label>新旧程度：</label>{{ product.condition_ || '未标注' }}</div>
            <div><label>尺寸：</label>{{ product.size || '无' }}</div>
            <div><label>库存：</label>{{ product.quantity }}</div>
            <div><label>销量：</label>{{ product.salesCount }}</div>
            <div v-if="product.allowBargain"><el-tag type="warning">可议价</el-tag></div>
          </div>
          <div class="action-row" v-if="userStore.isLoggedIn && product.status === 'APPROVED'">
            <el-input-number v-model="buyQuantity" :min="1" :max="product.quantity" />
            <el-button type="primary" size="large" @click="addToCart">加入购物车</el-button>
            <el-button type="danger" size="large" @click="buyNow">立即购买</el-button>
          </div>
        </div>
      </div>

      <!-- Description -->
      <el-card header="商品描述" class="section">
        <p>{{ product.description || '暂无描述' }}</p>
      </el-card>

      <!-- Usage Notes -->
      <el-card v-if="product.usageNotes" header="使用说明" class="section">
        <p>{{ product.usageNotes }}</p>
      </el-card>

      <!-- Reviews -->
      <el-card header="商品评价" class="section">
        <div v-if="reviews.length === 0">
          <el-empty description="暂无评价" />
        </div>
        <div v-for="r in reviews" :key="r.id" class="review-item">
          <div class="review-header">
            <span class="review-user">{{ r.user.name }}</span>
            <el-rate :model-value="r.rating" disabled size="small" />
            <span class="review-time">{{ r.createdAt }}</span>
          </div>
          <p class="review-content">{{ r.content }}</p>
        </div>
      </el-card>

      <!-- Merchant Service Rating -->
      <el-card header="商家服务评价" class="section">
        <div>
          <span>服务评分：</span>
          <el-rate :model-value="detail.merchantServiceRating || 5" disabled />
          <span>{{ detail.merchantServiceReviewCount || 0 }}条评价</span>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import api from '../api'
import { useUserStore } from '../store/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)
const product = ref(null)
const detail = ref({})
const reviews = ref([])
const images = ref([])
const currentImage = ref(0)
const buyQuantity = ref(1)

onMounted(() => {
  loadDetail()
})

function loadDetail() {
  loading.value = true
  api.get(`/products/${route.params.id}`).then(res => {
    if (res.data.code === 200) {
      detail.value = res.data.data
      product.value = res.data.data.product
      reviews.value = res.data.data.reviews || []
      try {
        images.value = product.value.images ? JSON.parse(product.value.images) : []
        if (!Array.isArray(images.value)) images.value = []
      } catch {
        // try plain URL
        if (product.value.images && (product.value.images.match(/^https?:\/\//) || product.value.images.startsWith('/api/uploads/'))) {
          images.value = [product.value.images]
        } else {
          images.value = []
        }
      }
    }
  }).finally(() => loading.value = false)
}

function addToCart() {
  api.post('/cart/add', { productId: product.value.id, quantity: buyQuantity.value }).then(res => {
    if (res.data.code === 200) ElMessage.success('已加入购物车')
  })
}

function buyNow() {
  const orderReq = {
    meetLocation: '待定',
    meetTime: null,
    items: [{ productId: product.value.id, quantity: buyQuantity.value }]
  }
  api.post('/orders', orderReq).then(res => {
    if (res.data.code === 200) {
      ElMessage.success('购买成功')
      router.push('/orders')
    } else {
      ElMessage.error(res.data.message)
    }
  })
}
</script>

<style scoped>
.product-detail { max-width: 1000px; margin: 0 auto; }
.detail-top { display: flex; gap: 30px; margin-bottom: 30px; }
.product-images { flex: 0 0 400px; }
.main-image { width: 100%; height: 300px; object-fit: contain; border: 1px solid #ebeef5; border-radius: 8px; }
.no-image-main { width: 100%; height: 300px; display: flex; align-items: center; justify-content: center; color: #c0c4cc; background: #f5f7fa; border-radius: 8px; }
.image-thumbs { display: flex; gap: 8px; margin-top: 10px; overflow-x: auto; }
.image-thumbs img { width: 60px; height: 60px; object-fit: cover; border-radius: 4px; cursor: pointer; border: 2px solid transparent; }
.image-thumbs img.active { border-color: #409eff; }
.product-detail-info { flex: 1; }
.product-detail-info h2 { font-size: 22px; margin-bottom: 15px; }
.price-row { margin: 15px 0; }
.current-price { font-size: 28px; color: #f56c6c; font-weight: bold; }
.orig-price { color: #c0c4cc; text-decoration: line-through; margin-left: 12px; font-size: 16px; }
.info-items { margin: 15px 0; }
.info-items > div { margin: 8px 0; color: #606266; }
.info-items label { color: #909399; margin-right: 8px; }
.action-row { display: flex; gap: 10px; align-items: center; margin-top: 25px; }
.section { margin-top: 20px; }
.review-item { padding: 12px 0; border-bottom: 1px solid #ebeef5; }
.review-header { display: flex; align-items: center; gap: 10px; margin-bottom: 6px; }
.review-user { font-weight: bold; }
.review-time { color: #c0c4cc; font-size: 12px; }
.review-content { color: #606266; }
</style>
