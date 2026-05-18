<template>
  <div class="wallet-page">
    <h2>我的钱包</h2>
    <el-row :gutter="20">
      <el-col :span="12">
        <el-card>
          <div class="balance-card">
            <span class="label">账户余额</span>
            <span class="value">¥{{ wallet?.balance?.toFixed(2) || '0.00' }}</span>
          </div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card>
          <div class="balance-card">
            <span class="label">我的积分</span>
            <span class="value">{{ wallet?.points || 0 }} <small>积分</small></span>
          </div>
          <p class="points-note">每{{ 100 }}积分可抵扣1元</p>
          <el-button v-if="wallet?.points >= 100" type="warning" size="small"
            @click="usePoints">使用积分抵扣</el-button>
        </el-card>
      </el-col>
    </el-row>

    <h3 style="margin-top:30px">交易记录</h3>
    <el-table :data="transactions" v-loading="loadingTxs" empty-text="暂无记录">
      <el-table-column label="类型" width="100">
        <template #default="{ row }">
          <el-tag :type="txType(row.type)">{{ txText(row.type) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="金额" width="120">
        <template #default="{ row }">
          <span :class="row.amount > 0 ? 'amount-plus' : 'amount-minus'">
            {{ row.amount > 0 ? '+' : '' }}¥{{ row.amount?.toFixed(2) }}
          </span>
        </template>
      </el-table-column>
      <el-table-column prop="description" label="说明" min-width="200" />
      <el-table-column prop="createdAt" label="时间" width="160" />
    </el-table>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import api from '../api'

const wallet = ref(null)
const transactions = ref([])
const loadingTxs = ref(false)

onMounted(() => {
  api.get('/wallet').then(res => {
    if (res.data.code === 200) wallet.value = res.data.data
  })
  loadingTxs.value = true
  api.get('/wallet/transactions').then(res => {
    if (res.data.code === 200) transactions.value = res.data.data
  }).finally(() => loadingTxs.value = false)
})

function usePoints() {
  ElMessageBox.prompt('请输入要使用的积分数量', '积分抵扣', {
    inputType: 'number', inputValue: wallet.value.points
  }).then(({ value }) => {
    api.post('/wallet/points/use', { points: parseInt(value) }).then(res => {
      if (res.data.code === 200) {
        ElMessage.success(res.data.message)
        api.get('/wallet').then(r => { if (r.data.code === 200) wallet.value = r.data.data })
      }
    })
  }).catch(() => {})
}

function txType(type) {
  return { RECHARGE: 'success', PURCHASE: 'danger', REFUND: 'warning', PLATFORM_FEE: 'info', POINTS_DEDUCTION: '' }[type] || ''
}

function txText(type) {
  return { RECHARGE: '充值', PURCHASE: '消费', REFUND: '退款', PLATFORM_FEE: '手续费', POINTS_DEDUCTION: '积分抵扣' }[type] || type
}
</script>

<style scoped>
.wallet-page { max-width: 800px; margin: 0 auto; }
.balance-card { text-align: center; padding: 20px; }
.balance-card .label { display: block; color: #909399; font-size: 14px; }
.balance-card .value { display: block; font-size: 36px; font-weight: bold; color: #409eff; margin-top: 8px; }
.balance-card .value small { font-size: 14px; }
.points-note { color: #909399; font-size: 12px; margin-top: 8px; text-align: center; }
.amount-plus { color: #67c23a; font-weight: bold; }
.amount-minus { color: #f56c6c; font-weight: bold; }
</style>
