<script setup lang="ts">
import { RouterLink, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const router = useRouter()

function handleLogout() {
  userStore.logout()
  router.push('/')
}
</script>

<template>
  <nav class="navbar">
    <div class="nav-inner">
      <RouterLink to="/" class="logo">
        <div class="logo-box">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
            <path d="M19 17h2c.6 0 1-.4 1-1v-3c0-.9-.7-1.7-1.5-1.9C18.7 10.6 16 10 16 10s-1.3-1.4-2.2-2.3c-.5-.4-1.1-.7-1.8-.7H5c-.6 0-1.1.4-1.4.9l-1.4 2.9A3.7 3.7 0 0 0 2 12v4c0 .6.4 1 1 1h2"/>
            <circle cx="7" cy="17" r="2"/><circle cx="17" cy="17" r="2"/>
          </svg>
        </div>
        <span>畅行租车</span>
      </RouterLink>

      <div class="nav-links">
        <RouterLink to="/">首页</RouterLink>
        <RouterLink to="/cars">全部车辆</RouterLink>
        <RouterLink to="/hitch">顺风租车</RouterLink>
        <RouterLink to="/cross-province">跨省租车</RouterLink>
        <RouterLink to="/stores">网点查询</RouterLink>
        <RouterLink to="/bookings" v-if="userStore.isLoggedIn">我的订单</RouterLink>
      </div>

      <div class="nav-right">
        <template v-if="userStore.isLoggedIn">
          <RouterLink to="/profile" class="user-chip">
            <span class="avatar">{{ userStore.user?.name?.charAt(0) }}</span>
            <span class="uname">{{ userStore.user?.name }}</span>
          </RouterLink>
          <button class="btn-ghost" @click="handleLogout">退出</button>
        </template>
        <template v-else>
          <RouterLink to="/login" class="btn-ghost">登录</RouterLink>
          <RouterLink to="/register" class="btn-accent">免费注册</RouterLink>
        </template>
      </div>
    </div>
  </nav>
</template>

<style scoped>
.navbar {
  position: sticky; top: 0; z-index: 1000;
  background: rgba(255,255,255,0.88);
  backdrop-filter: blur(16px) saturate(180%);
  border-bottom: 1px solid rgba(0,0,0,0.06);
}
.nav-inner {
  max-width: 1280px; margin: 0 auto; padding: 0 32px;
  height: 64px; display: flex; align-items: center; gap: 40px;
}
.logo { display: flex; align-items: center; gap: 10px; text-decoration: none; flex-shrink: 0; }
.logo-box {
  width: 36px; height: 36px;
  background: linear-gradient(135deg, #0891b2, #06b6d4);
  color: #fff; border-radius: 10px;
  display: flex; align-items: center; justify-content: center;
}
.logo span { font-size: 18px; font-weight: 800; color: #0f172a; letter-spacing: -0.5px; }
.nav-links { display: flex; gap: 4px; flex: 1; }
.nav-links a {
  text-decoration: none; color: #64748b; font-size: 14px;
  font-weight: 600; padding: 8px 14px; border-radius: 8px;
  transition: color 0.3s ease, background-color 0.3s ease; white-space: nowrap;
}
.nav-links a:hover { color: #0f172a; background: #f1f5f9; }
.nav-links a.router-link-exact-active { color: #0891b2; background: #f0fdfa; }
.nav-right { display: flex; align-items: center; gap: 8px; flex-shrink: 0; }
.user-chip {
  display: flex; align-items: center; gap: 8px;
  text-decoration: none; padding: 5px 14px 5px 5px;
  border-radius: 100px; border: 1px solid #e2e8f0; transition: 0.15s;
}
.user-chip:hover { border-color: #cbd5e1; background: #f8fafc; }
.avatar {
  width: 28px; height: 28px; border-radius: 50%;
  background: linear-gradient(135deg, #0891b2, #06b6d4);
  color: #fff; display: flex; align-items: center; justify-content: center;
  font-size: 12px; font-weight: 700;
}
.uname { font-size: 13px; font-weight: 500; color: #334155; }
.btn-ghost {
  background: none; border: none; color: #64748b;
  font-size: 14px; font-weight: 500; padding: 8px 14px;
  border-radius: 8px; cursor: pointer; text-decoration: none; transition: 0.15s;
}
.btn-ghost:hover { color: #0f172a; background: #f1f5f9; }
.btn-accent {
  background: linear-gradient(135deg, #0891b2, #06b6d4);
  color: #fff; text-decoration: none;
  padding: 8px 20px; border-radius: 10px;
  font-size: 13px; font-weight: 600; transition: 0.2s;
}
.btn-accent:hover { transform: translateY(-1px); box-shadow: 0 4px 12px rgba(8,145,178,0.35); }
</style>
