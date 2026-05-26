<template>
  <div class="admin-page">
    <h2>后台管理中心</h2>
    <el-tabs v-model="activeTab">
      <!-- Pending Users -->
      <el-tab-pane label="用户审核" name="users">
        <el-table :data="pendingUsers" v-loading="loadingUsers">
          <el-table-column prop="username" label="用户名" width="120" />
          <el-table-column prop="name" label="姓名" width="100" />
          <el-table-column prop="phone" label="手机号" width="130" />
          <el-table-column prop="email" label="邮箱" />
          <el-table-column label="角色" width="80">
            <template #default="{ row }">
              <el-tag :type="row.role === 'MERCHANT' ? 'warning' : ''">{{ row.role === 'MERCHANT' ? '商家' : '用户' }}</el-tag>
            </template>
          </el-table-column>
          <!-- 商家注册时上传的证件图片，管理员审核时需要查看 -->
          <el-table-column label="证件图片" width="150">
            <template #default="{ row }">
              <template v-if="row.role === 'MERCHANT'">
                <div style="display:flex;gap:4px">
                  <el-image
                    v-if="row.businessLicenseImg"
                    :src="row.businessLicenseImg"
                    :preview-src-list="[row.businessLicenseImg]"
                    style="width:60px;height:60px;border-radius:4px"
                    fit="cover"
                    title="营业执照"
                  />
                  <el-image
                    v-if="row.idCardImg"
                    :src="row.idCardImg"
                    :preview-src-list="[row.idCardImg]"
                    style="width:60px;height:60px;border-radius:4px"
                    fit="cover"
                    title="身份证"
                  />
                  <span v-if="!row.businessLicenseImg && !row.idCardImg" style="color:#c0c4cc;font-size:12px">未上传</span>
                </div>
              </template>
              <span v-else>-</span>
            </template>
          </el-table-column>
          <el-table-column prop="createdAt" label="注册时间" width="160" />
          <el-table-column label="操作" width="200">
            <template #default="{ row }">
              <el-button type="success" size="small" @click="approveUser(row.id)">通过</el-button>
              <el-button type="danger" size="small" @click="rejectUser(row.id)">拒绝</el-button>
            </template>
          </el-table-column>
        </el-table>
        <el-empty v-if="!loadingUsers && pendingUsers.length === 0" description="暂无待审核用户" />
      </el-tab-pane>

      <!-- All Users -->
      <el-tab-pane label="用户管理" name="allUsers">
        <el-table :data="allUsers" v-loading="loadingAllUsers">
          <el-table-column prop="username" label="用户名" width="120" />
          <el-table-column prop="name" label="姓名" width="100" />
          <el-table-column prop="phone" label="手机号" width="130" />
          <el-table-column label="角色" width="80">
            <template #default="{ row }">
              <el-tag :type="row.role === 'MERCHANT' ? 'warning' : row.role === 'ADMIN' ? 'danger' : ''">
                {{ row.role === 'MERCHANT' ? '商家' : row.role === 'ADMIN' ? '管理员' : '用户' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="状态" width="80">
            <template #default="{ row }">
              <el-tag :type="row.status === 'APPROVED' ? 'success' : 'warning'">
                {{ row.status === 'APPROVED' ? '已审核' : row.status === 'PENDING' ? '待审核' : '已拒绝' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="250">
            <template #default="{ row }">
              <el-button size="small" text type="danger" @click="deleteUser(row.id)">删除</el-button>
              <el-button v-if="row.role === 'MERCHANT'" size="small" text type="warning"
                @click="showLevelDialog(row)">设置等级</el-button>
              <el-button size="small" text type="success" @click="showRechargeDialog(row)">充值</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <!-- Pending Products -->
      <el-tab-pane label="商品审核" name="products">
        <el-table :data="pendingProducts" v-loading="loadingProducts">
          <el-table-column label="图片" width="90">
            <template #default="{ row }">
              <el-image
                v-if="getFirstImage(row.images)"
                :src="getFirstImage(row.images)"
                :preview-src-list="parseImages(row.images)"
                style="width:60px;height:60px;border-radius:4px"
                fit="cover"
              />
              <span v-else style="color:#c0c4cc;font-size:12px">无图</span>
            </template>
          </el-table-column>
          <el-table-column prop="name" label="商品名称" />
          <el-table-column label="商家" width="120">
            <template #default="{ row }">{{ row.merchant?.storeName || row.merchant?.name }}</template>
          </el-table-column>
          <el-table-column label="价格" width="100">
            <template #default="{ row }">¥{{ row.discountPrice }}</template>
          </el-table-column>
          <el-table-column prop="quantity" label="库存" width="80" />
          <el-table-column prop="createdAt" label="发布时间" width="160" />
          <el-table-column label="操作" width="200">
            <template #default="{ row }">
              <el-button type="success" size="small" @click="approveProduct(row.id)">通过</el-button>
              <el-button type="danger" size="small" @click="rejectProduct(row.id)">拒绝</el-button>
            </template>
          </el-table-column>
        </el-table>
        <el-empty v-if="!loadingProducts && pendingProducts.length === 0" description="暂无待审核商品" />
      </el-tab-pane>

      <!-- All Products Management -->
      <el-tab-pane label="在售商品管理" name="allProducts">
        <el-table :data="allProducts" v-loading="loadingAllProducts">
          <el-table-column prop="name" label="商品名称" min-width="150" />
          <el-table-column label="商家" width="120">
            <template #default="{ row }">{{ row.merchant?.storeName || row.merchant?.name }}</template>
          </el-table-column>
          <el-table-column label="价格" width="100">
            <template #default="{ row }">¥{{ row.discountPrice }}</template>
          </el-table-column>
          <el-table-column label="状态" width="90">
            <template #default="{ row }">
              <el-tag :type="statusTag(row.status)">{{ statusText(row.status) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="salesCount" label="销量" width="70" />
          <el-table-column label="下架原因" min-width="120">
            <template #default="{ row }">
              {{ row.delistReason || '-' }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="150">
            <template #default="{ row }">
              <el-button v-if="row.status === 'APPROVED'" type="danger" size="small"
                @click="showDelistDialog(row)">下架商品</el-button>
            </template>
          </el-table-column>
        </el-table>
        <el-empty v-if="!loadingAllProducts && allProducts.length === 0" description="暂无商品" />
      </el-tab-pane>

      <!-- Merchant Management -->
      <el-tab-pane label="商家管理" name="merchants">
        <el-table :data="merchants" v-loading="loadingMerchants">
          <el-table-column prop="storeName" label="店铺名称" />
          <el-table-column prop="name" label="商家姓名" width="100" />
          <el-table-column label="等级" width="80">
            <template #default="{ row }">Lv.{{ row.merchantLevel || 1 }}</template>
          </el-table-column>
          <el-table-column label="服务评分" width="120">
            <template #default="{ row }">
              <el-rate :model-value="row.serviceRating" disabled size="small" />
            </template>
          </el-table-column>
          <el-table-column label="操作" width="150">
            <template #default="{ row }">
              <el-button size="small" type="warning" @click="showLevelDialog(row)">调整等级</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>
    </el-tabs>

    <!-- Level Dialog -->
    <el-dialog v-model="levelDialogVisible" title="设置商家等级" width="300px">
      <el-select v-model="selectedLevel" placeholder="选择等级">
        <el-option v-for="i in 5" :key="i" :label="`等级 ${i} (费率 ${feeRates[i]}%)`" :value="i" />
      </el-select>
      <template #footer>
        <el-button @click="levelDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveLevel">确认</el-button>
      </template>
    </el-dialog>

    <!-- Recharge Dialog -->
    <el-dialog v-model="rechargeDialogVisible" title="用户充值" width="350px">
      <div>
        <p>充值用户：{{ rechargeTarget?.name || rechargeTarget?.username }}</p>
        <el-input v-model.number="rechargeAmount" type="number" :min="0.01" step="0.01" placeholder="输入充值金额" style="width:100%" />
      </div>
      <template #footer>
        <el-button @click="rechargeDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="doRecharge">确认充值</el-button>
      </template>
    </el-dialog>

    <!-- Delist Product Dialog -->
    <el-dialog v-model="delistDialogVisible" title="下架商品" width="400px">
      <div>
        <p>商品：<b>{{ delistTarget?.name }}</b></p>
        <p>商家：{{ delistTarget?.merchant?.storeName || delistTarget?.merchant?.name }}</p>
        <el-input v-model="delistReason" type="textarea" :rows="3"
          placeholder="请输入下架原因，商家将收到该反馈..." />
      </div>
      <template #footer>
        <el-button @click="delistDialogVisible = false">取消</el-button>
        <el-button type="danger" @click="doDelist">确认下架</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import api from '../api'

const activeTab = ref('users')
const pendingUsers = ref([])
const allUsers = ref([])
const pendingProducts = ref([])
const allProducts = ref([])
const merchants = ref([])
const loadingUsers = ref(false), loadingAllUsers = ref(false), loadingProducts = ref(false), loadingMerchants = ref(false), loadingAllProducts = ref(false)

const levelDialogVisible = ref(false)
const rechargeDialogVisible = ref(false)
const delistDialogVisible = ref(false)
const selectedLevel = ref(1)
const selectedMerchant = ref(null)
const rechargeTarget = ref(null)
const delistTarget = ref(null)
const rechargeAmount = ref(null)
const delistReason = ref('')

const feeRates = { 1: 0.1, 2: 0.2, 3: 0.5, 4: 0.75, 5: 1 }

onMounted(() => {
  loadPendingUsers()
  loadAllUsers()
  loadPendingProducts()
  loadMerchants()
  loadAllProducts()
})

function loadPendingUsers() {
  loadingUsers.value = true
  api.get('/admin/users/pending').then(res => {
    if (res.data.code === 200) pendingUsers.value = res.data.data
  }).finally(() => loadingUsers.value = false)
}

function loadAllUsers() {
  loadingAllUsers.value = true
  api.get('/admin/users').then(res => {
    if (res.data.code === 200) allUsers.value = res.data.data
  }).finally(() => loadingAllUsers.value = false)
}

function loadPendingProducts() {
  loadingProducts.value = true
  api.get('/admin/products/pending').then(res => {
    if (res.data.code === 200) pendingProducts.value = res.data.data
  }).finally(() => loadingProducts.value = false)
}

function loadMerchants() {
  loadingMerchants.value = true
  api.get('/admin/merchants').then(res => {
    if (res.data.code === 200) merchants.value = res.data.data
  }).finally(() => loadingMerchants.value = false)
}

function approveUser(id) {
  api.put(`/admin/users/${id}/approve`).then(res => {
    if (res.data.code === 200) { ElMessage.success('已通过'); loadPendingUsers(); loadAllUsers() }
  })
}

function rejectUser(id) {
  api.put(`/admin/users/${id}/reject`).then(res => {
    if (res.data.code === 200) { ElMessage.success('已拒绝'); loadPendingUsers() }
  })
}

function deleteUser(id) {
  api.delete(`/admin/users/${id}`).then(res => {
    if (res.data.code === 200) { ElMessage.success('已删除'); loadAllUsers() }
  })
}

function approveProduct(id) {
  api.put(`/admin/products/${id}/approve`).then(res => {
    if (res.data.code === 200) { ElMessage.success('商品审核通过'); loadPendingProducts() }
  })
}

function rejectProduct(id) {
  api.put(`/admin/products/${id}/reject`).then(res => {
    if (res.data.code === 200) { ElMessage.success('商品已拒绝'); loadPendingProducts() }
  })
}

function showLevelDialog(merchant) {
  selectedMerchant.value = merchant
  selectedLevel.value = merchant.merchantLevel || 1
  levelDialogVisible.value = true
}

function saveLevel() {
  api.put(`/admin/users/${selectedMerchant.value.id}/level`, { level: selectedLevel.value }).then(res => {
    if (res.data.code === 200) { ElMessage.success('等级已更新'); loadAllUsers(); loadMerchants(); levelDialogVisible.value = false }
  })
}

function showRechargeDialog(user) {
  rechargeTarget.value = user
  rechargeAmount.value = null
  rechargeDialogVisible.value = true
}

function doRecharge() {
  api.post('/admin/wallet/recharge', {
    userId: rechargeTarget.value.id,
    amount: rechargeAmount.value,
    description: '管理员充值'
  }).then(res => {
    if (res.data.code === 200) { ElMessage.success('充值成功'); rechargeDialogVisible.value = false }
  })
}

function loadAllProducts() {
  loadingAllProducts.value = true
  api.get('/admin/products/all').then(res => {
    if (res.data.code === 200) allProducts.value = res.data.data
  }).finally(() => loadingAllProducts.value = false)
}

function showDelistDialog(product) {
  delistTarget.value = product
  delistReason.value = ''
  delistDialogVisible.value = true
}

function doDelist() {
  api.put(`/admin/products/${delistTarget.value.id}/delist`, { reason: delistReason.value }).then(res => {
    if (res.data.code === 200) {
      ElMessage.success('商品已下架，商家已收到反馈')
      delistDialogVisible.value = false
      loadAllProducts()
    }
  })
}

function statusTag(status) {
  return { APPROVED: 'success', PENDING: 'warning', REJECTED: 'danger', SOLD_OUT: 'info', DELISTED: '' }[status] || ''
}

function statusText(status) {
  return { APPROVED: '在售', PENDING: '待审核', REJECTED: '已拒绝', SOLD_OUT: '售罄', DELISTED: '已下架' }[status] || status
}

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

function parseImages(images) {
  if (!images) return []
  try {
    const arr = JSON.parse(images)
    return Array.isArray(arr) ? arr : []
  } catch {
    return images ? [images] : []
  }
}
</script>

<style scoped>
.admin-page { max-width: 1200px; margin: 0 auto; }
</style>
