<template>
  <div class="home">
    <!-- Sort Options -->
    <div class="sort-bar">
      <el-radio-group v-model="sortBy" @change="loadProducts">
        <el-radio-button value="">默认</el-radio-button>
        <el-radio-button value="price_asc">价格升序</el-radio-button>
        <el-radio-button value="price_desc">价格降序</el-radio-button>
        <el-radio-button value="sales">销量优先</el-radio-button>
        <el-radio-button value="rating">好评优先</el-radio-button>
      </el-radio-group>
    </div>

    <!-- Product List -->
    <div class="product-grid" v-loading="loading">
      <div class="product-card" v-for="p in products" :key="p.id" @click="$router.push(`/product/${p.id}`)">
        <div class="product-image">
          <img v-if="getFirstImage(p.images)" :src="getFirstImage(p.images)" alt="" />
          <div v-else class="no-image">
            <el-icon size="40"><PictureFilled /></el-icon>
          </div>
          <span class="condition-tag" v-if="p.condition_">{{ p.condition_ }}</span>
        </div>
        <div class="product-info">
          <h3 class="product-name">{{ p.name }}</h3>
          <div class="product-price">
            <span class="discount-price">¥{{ p.discountPrice }}</span>
            <span class="original-price" v-if="p.originalPrice > p.discountPrice">
              ¥{{ p.originalPrice }}
            </span>
          </div>
          <div class="product-meta">
            <span @click.stop="$router.push(`/merchant/store/${p.merchant.id}`)">
              {{ p.merchant.storeName || p.merchant.name }}的店
            </span>
            <span>已售 {{ p.salesCount }}</span>
          </div>
          <div class="product-rating" v-if="p.reviewCount > 0">
            <el-rate :model-value="p.avgRating" disabled size="small" />
            <span>{{ p.reviewCount }}条评价</span>
          </div>
        </div>
      </div>
    </div>

    <el-empty v-if="!loading && products.length === 0" description="暂无商品" />

    <!-- Categories -->
    <el-card class="category-section" header="商品分类">
      <div class="category-list">
        <el-tag v-for="cat in categories" :key="cat.id" type="success" class="category-tag"
          @click="filterByCategory(cat.id)">
          {{ cat.name }}
        </el-tag>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import api from '../api'

const route = useRoute()
const products = ref([])
const categories = ref([])
const loading = ref(false)
const sortBy = ref('')

onMounted(() => {
  loadProducts()
  loadCategories()
})

function loadProducts() {
  loading.value = true
  const params = {}
  if (route.query.keyword) params.keyword = route.query.keyword
  if (sortBy.value) params.sortBy = sortBy.value
  api.get('/products', { params }).then(res => {
    if (res.data.code === 200) products.value = res.data.data
  }).finally(() => loading.value = false)
}

function loadCategories() {
  api.get('/categories').then(res => {
    if (res.data.code === 200) categories.value = res.data.data
  })
}

function filterByCategory(catId) {
  api.get(`/products/category/${catId}`).then(res => {
    if (res.data.code === 200) products.value = res.data.data
  })
}

function getFirstImage(images) {
  if (!images) return null
  try {
    const arr = JSON.parse(images)
    if (Array.isArray(arr) && arr.length > 0) return arr[0]
    return null
  } catch {
    // plain URL string
    if (images.match(/^https?:\/\//) || images.startsWith('/api/uploads/')) return images
    return null
  }
}
</script>

<style scoped>
.home { max-width: 1200px; margin: 0 auto; }
.sort-bar { margin-bottom: 20px; }
.product-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
  gap: 20px;
}
.product-card {
  background: #fff;
  border-radius: 8px;
  overflow: hidden;
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;
  border: 1px solid #ebeef5;
}
.product-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 4px 20px rgba(0,0,0,0.1);
}
.product-image {
  height: 200px;
  position: relative;
  background: #f5f7fa;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}
.product-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.no-image { color: #c0c4cc; }
.condition-tag {
  position: absolute;
  top: 8px;
  left: 8px;
  background: rgba(64,158,255,0.85);
  color: #fff;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
}
.product-info { padding: 12px 15px; }
.product-name {
  font-size: 15px;
  margin: 0 0 8px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.discount-price { color: #f56c6c; font-size: 18px; font-weight: bold; }
.original-price { color: #c0c4cc; font-size: 13px; text-decoration: line-through; margin-left: 8px; }
.product-meta {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: #909399;
  margin-top: 8px;
}
.product-meta span:first-child { color: #409eff; cursor: pointer; }
.product-rating { margin-top: 6px; display: flex; align-items: center; gap: 6px; font-size: 12px; color: #909399; }
.category-section { margin-top: 30px; }
.category-list { display: flex; flex-wrap: wrap; gap: 10px; }
.category-tag { cursor: pointer; font-size: 14px; padding: 8px 16px; }
</style>
