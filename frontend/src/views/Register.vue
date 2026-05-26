<template>
  <div class="register-page">
    <el-card class="register-card">
      <template #header><h2 style="text-align:center">用户注册</h2></template>
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="用户类型" prop="role">
          <el-radio-group v-model="form.role">
            <el-radio value="USER">普通用户</el-radio>
            <el-radio value="MERCHANT">商家</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" placeholder="设置登录用户名" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" type="password" show-password placeholder="设置密码" />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPwd">
          <el-input v-model="form.confirmPwd" type="password" show-password placeholder="再次输入密码" />
        </el-form-item>
        <el-form-item label="姓名" prop="name">
          <el-input v-model="form.name" placeholder="真实姓名" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" placeholder="手机号码" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="form.email" placeholder="电子邮箱" />
        </el-form-item>
        <el-form-item label="城市" prop="city">
          <el-input v-model="form.city" placeholder="所在城市" />
        </el-form-item>
        <el-form-item label="性别" prop="gender">
          <el-radio-group v-model="form.gender">
            <el-radio value="男">男</el-radio>
            <el-radio value="女">女</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="银行账号" prop="bankAccount">
          <el-input v-model="form.bankAccount" placeholder="16位银行卡号" maxlength="16" />
        </el-form-item>
        <el-form-item v-if="form.role === 'MERCHANT'" label="店铺名称" prop="storeName">
          <el-input v-model="form.storeName" placeholder="您的店铺名称" />
        </el-form-item>
        <el-form-item v-if="form.role === 'MERCHANT'" label="营业执照" prop="businessLicense">
          <el-upload
            list-type="picture-card"
            :http-request="uploadLicense"
            :on-remove="() => licenseUrl = ''"
            :auto-upload="true"
            :limit="1"
            accept="image/*"
          >
            <el-icon><Plus /></el-icon>
          </el-upload>
          <span style="color:#909399;font-size:12px">请上传营业执照图片</span>
        </el-form-item>
        <el-form-item v-if="form.role === 'MERCHANT'" label="身份证" prop="idCard">
          <el-upload
            list-type="picture-card"
            :http-request="uploadIdCard"
            :on-remove="() => idCardUrl = ''"
            :auto-upload="true"
            :limit="1"
            accept="image/*"
          >
            <el-icon><Plus /></el-icon>
          </el-upload>
          <span style="color:#909399;font-size:12px">请上传身份证图片</span>
        </el-form-item>
        <el-form-item label="验证码" prop="captcha">
          <div class="captcha-row">
            <el-input v-model="form.captcha" placeholder="验证码" style="width:140px" />
            <span class="captcha-code">{{ captchaCode }}</span>
            <el-button text type="primary" @click="refreshCaptcha">刷新</el-button>
          </div>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleRegister" :loading="loading" style="width:100%">
            注册
          </el-button>
        </el-form-item>
        <div style="text-align:center">
          <el-button text type="primary" @click="$router.push('/login')">已有账号？去登录</el-button>
        </div>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import api from '../api'

const router = useRouter()
const formRef = ref(null)
const loading = ref(false)
const captchaCode = ref('')
const captchaKey = ref('')
// 商家注册时上传的图片URL
const licenseUrl = ref('')
const idCardUrl = ref('')

const form = reactive({
  role: 'USER',
  username: '', password: '', confirmPwd: '',
  name: '', phone: '', email: '', city: '', gender: '',
  bankAccount: '', storeName: '', captcha: ''
})

const rules = {
  username: [{ required: true, message: '请输入用户名' }],
  password: [{ required: true, message: '请输入密码' }, { min: 6, message: '密码至少6位' }],
  confirmPwd: [{ required: true, message: '请确认密码' },
    { validator: (_, v, cb) => cb(v === form.password ? undefined : new Error('两次密码不一致')) }],
  name: [{ required: true, message: '请输入姓名' }],
  phone: [{ required: true, message: '请输入手机号' }],
  bankAccount: [{ pattern: /^\d{16}$/, message: '银行账号需16位数字' }],
  storeName: [{ required: true, message: '请输入店铺名称', trigger: 'blur' }],
  captcha: [{ required: true, message: '请输入验证码' }]
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

// 上传营业执照
async function uploadLicense(options) {
  const formData = new FormData()
  formData.append('files', options.file)
  try {
    const res = await api.post('/files/upload', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
    if (res.data.code === 200 && res.data.data) {
      licenseUrl.value = res.data.data[0]
      options.onSuccess({ url: licenseUrl.value }, options.file)
    } else {
      options.onError(new Error(res.data.message))
    }
  } catch (e) {
    ElMessage.error('营业执照上传失败')
    options.onError(e)
  }
}

// 上传身份证
async function uploadIdCard(options) {
  const formData = new FormData()
  formData.append('files', options.file)
  try {
    const res = await api.post('/files/upload', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
    if (res.data.code === 200 && res.data.data) {
      idCardUrl.value = res.data.data[0]
      options.onSuccess({ url: idCardUrl.value }, options.file)
    } else {
      options.onError(new Error(res.data.message))
    }
  } catch (e) {
    ElMessage.error('身份证上传失败')
    options.onError(e)
  }
}

async function handleRegister() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  // 商家必须上传营业执照和身份证
  if (form.role === 'MERCHANT' && !licenseUrl.value) {
    ElMessage.warning('请上传营业执照')
    return
  }
  if (form.role === 'MERCHANT' && !idCardUrl.value) {
    ElMessage.warning('请上传身份证')
    return
  }
  loading.value = true
  const payload = {
    username: form.username, password: form.password, name: form.name,
    phone: form.phone, email: form.email, city: form.city, gender: form.gender,
    bankAccount: form.bankAccount, role: form.role,
    storeName: form.role === 'MERCHANT' ? form.storeName : null,
    businessLicenseImg: form.role === 'MERCHANT' ? licenseUrl.value : null,
    idCardImg: form.role === 'MERCHANT' ? idCardUrl.value : null,
    captcha: form.captcha, captchaKey: captchaKey.value
  }
  api.post('/auth/register', payload).then(res => {
    if (res.data.code === 200) {
      ElMessage.success('注册成功，等待管理员审核')
      router.push('/login')
    } else {
      ElMessage.error(res.data.message)
      refreshCaptcha()
    }
  }).finally(() => loading.value = false)
}
</script>

<style scoped>
.register-page {
  display: flex;
  justify-content: center;
  padding: 20px;
}
.register-card {
  width: 520px;
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
}
</style>
