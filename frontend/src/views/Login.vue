<template>
  <div class="login-page">
    <el-card class="login-card">
      <template #header><h2 style="text-align:center">用户登录</h2></template>
      <el-form :model="form" :rules="rules" ref="formRef" label-width="80px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" type="password" show-password placeholder="请输入密码" />
        </el-form-item>
        <el-form-item label="验证码" prop="captcha">
          <div class="captcha-row">
            <el-input v-model="form.captcha" placeholder="验证码" style="width:140px" />
            <span class="captcha-code">{{ captchaCode }}</span>
            <el-button text type="primary" @click="refreshCaptcha">刷新</el-button>
          </div>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleLogin" :loading="loading" style="width:100%">
            登录
          </el-button>
        </el-form-item>
        <div style="text-align:center">
          <el-button text type="primary" @click="$router.push('/register')">没有账号？立即注册</el-button>
        </div>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import api from '../api'
import { useUserStore } from '../store/user'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref(null)
const loading = ref(false)
const captchaCode = ref('')
const captchaKey = ref('')

const form = reactive({
  username: '', password: '', captcha: ''
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  captcha: [{ required: true, message: '请输入验证码', trigger: 'blur' }]
}

onMounted(refreshCaptcha)

function refreshCaptcha() {
  api.get('/auth/captcha').then(res => {
    if (res.data.code === 200) {
      captchaKey.value = res.data.data.captchaKey
      captchaCode.value = res.data.data.captchaCode
    }
  })
}

async function handleLogin() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  loading.value = true
  const result = await userStore.login(form.username, form.password, form.captcha, captchaKey.value)
  loading.value = false
  if (result.success) {
    ElMessage.success('登录成功')
    router.push('/')
  } else {
    ElMessage.error(result.message)
    refreshCaptcha()
  }
}
</script>

<style scoped>
.login-page {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 70vh;
}
.login-card {
  width: 420px;
}
.captcha-row {
  display: flex;
  align-items: center;
  gap: 10px;
}
.captcha-code {
  font-size: 22px;
  font-weight: bold;
  color: #409eff;
  letter-spacing: 4px;
  background: #f0f5ff;
  padding: 2px 8px;
  border-radius: 4px;
  font-family: 'Courier New', monospace;
  user-select: none;
}
</style>
