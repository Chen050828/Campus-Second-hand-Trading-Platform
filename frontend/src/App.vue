<template>
  <div id="app-root">
    <el-container>
      <el-header class="app-header">
        <div class="header-left">
          <h1 class="logo" @click="$router.push('/')">校园二手交易市场</h1>
        </div>
        <div class="header-center">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索商品..."
            class="search-input"
            @keyup.enter="search"
          >
            <template #append>
              <el-button @click="search" icon="Search">搜索</el-button>
            </template>
          </el-input>
        </div>
        <div class="header-right">
          <template v-if="!userStore.isLoggedIn">
            <el-button text @click="$router.push('/login')">登录</el-button>
            <el-button type="primary" @click="$router.push('/register')">注册</el-button>
          </template>
          <template v-else>
            <el-button text @click="$router.push('/cart')" icon="ShoppingCart">
              购物车
            </el-button>
            <el-dropdown @command="handleCommand">
              <span class="user-info">
                {{ userStore.user?.name || userStore.user?.username }}
                <el-icon><ArrowDown /></el-icon>
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="profile">个人中心</el-dropdown-item>
                  <el-dropdown-item command="wallet">我的钱包</el-dropdown-item>
                  <el-dropdown-item command="orders">我的订单</el-dropdown-item>
                  <el-dropdown-item v-if="userStore.isMerchant" command="merchant">
                    商家管理
                  </el-dropdown-item>
                  <el-dropdown-item v-if="userStore.isAdmin" command="admin">
                    后台管理
                  </el-dropdown-item>
                  <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
        </div>
      </el-header>

      <el-main>
        <router-view />
      </el-main>

      <el-footer class="app-footer">
        <p>校园二手交易市场 &copy; 2024 - 安全、便捷的校园二手交易平台</p>
      </el-footer>
    </el-container>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from './store/user'

const router = useRouter()
const userStore = useUserStore()
const searchKeyword = ref('')

function search() {
  if (searchKeyword.value.trim()) {
    router.push({ path: '/', query: { keyword: searchKeyword.value } })
  }
}

function handleCommand(command) {
  switch (command) {
    case 'profile':
      router.push('/profile')
      break
    case 'wallet':
      router.push('/wallet')
      break
    case 'orders':
      router.push('/orders')
      break
    case 'merchant':
      router.push('/merchant')
      break
    case 'admin':
      router.push('/admin')
      break
    case 'logout':
      userStore.logout()
      router.push('/')
      break
  }
}
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}
body {
  font-family: 'Microsoft YaHei', 'PingFang SC', sans-serif;
  background: #f5f7fa;
}
.app-header {
  background: #fff;
  border-bottom: 1px solid #e4e7ed;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  height: 60px;
  position: sticky;
  top: 0;
  z-index: 100;
}
.header-left .logo {
  font-size: 20px;
  color: #409eff;
  cursor: pointer;
  white-space: nowrap;
}
.header-center {
  flex: 1;
  max-width: 480px;
  margin: 0 20px;
}
.header-right {
  display: flex;
  align-items: center;
  gap: 10px;
}
.user-info {
  cursor: pointer;
  color: #409eff;
}
.app-footer {
  text-align: center;
  color: #999;
  padding: 20px;
  border-top: 1px solid #e4e7ed;
}
.app-footer p {
  margin: 0;
}
.el-main {
  min-height: calc(100vh - 120px);
  padding: 20px;
}
</style>
