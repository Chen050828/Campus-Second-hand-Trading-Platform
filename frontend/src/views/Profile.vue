<template>
  <div class="profile-page">
    <h2>个人中心</h2>
    <el-card v-loading="loading" v-if="profile">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="用户名">{{ profile.username }}</el-descriptions-item>
        <el-descriptions-item label="姓名">{{ profile.name }}</el-descriptions-item>
        <el-descriptions-item label="手机号">{{ profile.phone }}</el-descriptions-item>
        <el-descriptions-item label="邮箱">{{ profile.email }}</el-descriptions-item>
        <el-descriptions-item label="城市">{{ profile.city }}</el-descriptions-item>
        <el-descriptions-item label="性别">{{ profile.gender }}</el-descriptions-item>
        <el-descriptions-item label="角色">
          <el-tag :type="roleTag(profile.role)">{{ roleText(profile.role) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="profile.status === 'APPROVED' ? 'success' : 'warning'">
            {{ profile.status === 'APPROVED' ? '已审核' : profile.status === 'PENDING' ? '待审核' : '已拒绝' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item v-if="profile.role === 'MERCHANT'" label="店铺名称">
          {{ profile.storeName }}
        </el-descriptions-item>
        <el-descriptions-item v-if="profile.role === 'MERCHANT'" label="商家等级">
          Lv.{{ profile.merchantLevel || 1 }}
        </el-descriptions-item>
      </el-descriptions>

      <el-button type="primary" style="margin-top:20px" @click="showEdit = true" v-if="!showEdit">编辑资料</el-button>

      <el-form v-if="showEdit" :model="editForm" style="margin-top:20px;max-width:400px">
        <el-form-item label="姓名">
          <el-input v-model="editForm.name" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="editForm.phone" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="editForm.email" />
        </el-form-item>
        <el-form-item label="城市">
          <el-input v-model="editForm.city" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="saveProfile">保存</el-button>
          <el-button @click="showEdit = false">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import api from '../api'

const profile = ref(null)
const loading = ref(false)
const showEdit = ref(false)
const editForm = reactive({ name: '', phone: '', email: '', city: '' })

onMounted(() => {
  loading.value = true
  api.get('/user/profile').then(res => {
    if (res.data.code === 200) {
      profile.value = res.data.data
      Object.assign(editForm, { name: profile.value.name, phone: profile.value.phone, email: profile.value.email, city: profile.value.city })
    }
  }).finally(() => loading.value = false)
})

function saveProfile() {
  api.put('/user/profile', editForm).then(res => {
    if (res.data.code === 200) {
      ElMessage.success('保存成功')
      profile.value = { ...profile.value, ...editForm }
      showEdit.value = false
    }
  })
}

function roleText(role) {
  return { USER: '普通用户', MERCHANT: '商家', ADMIN: '管理员' }[role] || role
}

function roleTag(role) {
  return { USER: '', MERCHANT: 'warning', ADMIN: 'danger' }[role] || ''
}
</script>

<style scoped>
.profile-page { max-width: 800px; margin: 0 auto; }
</style>
