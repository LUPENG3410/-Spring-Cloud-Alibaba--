<script setup lang="ts">
import { useRouter, useRoute } from 'vue-router'
import { useAdminStore } from '@/stores/admin'
import { onMounted } from 'vue'

const router = useRouter()
const route = useRoute()
const adminStore = useAdminStore()

onMounted(() => {
  if (!adminStore.isLoggedIn && !localStorage.getItem('admin_token')) {
    router.push('/admin/login')
  }
})

function handleLogout() {
  adminStore.logout()
  localStorage.removeItem('admin_token')
  router.push('/admin/login')
}

const menuItems = [
  { path: '/admin', label: '仪表盘', icon: 'M3 12l2-2m0 0l7-7 7 7M5 10v10a1 1 0 001 1h3m10-11l2 2m-2-2v10a1 1 0 01-1 1h-3m-6 0a1 1 0 001-1v-4a1 1 0 011-1h2a1 1 0 011 1v4a1 1 0 001 1m-6 0h6' },
  { path: '/admin/cars', label: '车辆管理', icon: 'M19 17h2c.6 0 1-.4 1-1v-3c0-.9-.7-1.7-1.5-1.9C18.7 10.6 16 10 16 10s-1.3-1.4-2.2-2.3c-.5-.4-1.1-.7-1.8-.7H5c-.6 0-1.1.4-1.4.9l-1.4 2.9A3.7 3.7 0 002 12v4c0 .6.4 1 1 1h2M7 17a2 2 0 100 4 2 2 0 000-4zm10 0a2 2 0 100 4 2 2 0 000-4z' },
  { path: '/admin/maintenance', label: '保养提醒', icon: 'M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z' },
  { path: '/admin/bookings', label: '订单管理', icon: 'M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2m-3 7h3m-3 4h3m-6-4h.01M9 16h.01' },
  { path: '/admin/users', label: '用户管理', icon: 'M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197M13 7a4 4 0 11-8 0 4 4 0 018 0z' },
  { path: '/admin/stores', label: '门店管理', icon: 'M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z M15 11a3 3 0 11-6 0 3 3 0 016 0z' },
]
</script>

<template>
  <div class="admin-layout">
    <aside class="sidebar">
      <div class="sidebar-header">
        <div class="logo-box">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><path d="M12 15V3m0 12l-4-4m4 4l4-4M2 17l.621 2.485A2 2 0 004.561 21h14.878a2 2 0 001.94-1.515L22 17"/></svg>
        </div>
        <div class="logo-text">
          <span class="logo-title">畅行租车</span>
          <span class="logo-sub">管理后台</span>
        </div>
      </div>

      <nav class="sidebar-nav">
        <router-link
          v-for="item in menuItems"
          :key="item.path"
          :to="item.path"
          class="nav-item"
          :class="{ active: route.path === item.path }"
        >
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
            <path :d="item.icon"/>
          </svg>
          <span>{{ item.label }}</span>
        </router-link>
      </nav>

      <div class="sidebar-footer">
        <div class="admin-info">
          <div class="admin-avatar">{{ adminStore.adminUser?.name?.charAt(0) || 'A' }}</div>
          <div class="admin-detail">
            <span class="admin-name">{{ adminStore.adminUser?.name || '管理员' }}</span>
            <span class="admin-role">{{ adminStore.adminUser?.role === 'superadmin' ? '超级管理员' : '管理员' }}</span>
          </div>
        </div>
        <button class="logout-btn" @click="handleLogout">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><path d="M9 21H5a2 2 0 01-2-2V5a2 2 0 012-2h4M16 17l5-5-5-5M21 12H9"/></svg>
        </button>
      </div>
    </aside>

    <main class="admin-main">
      <RouterView />
    </main>
  </div>
</template>

<style scoped>
.admin-layout {
  display: flex; min-height: 100vh; background: #f1f5f9;
}

.sidebar {
  width: 240px; background: #0f172a; color: #fff;
  display: flex; flex-direction: column; flex-shrink: 0;
  position: fixed; top: 0; left: 0; bottom: 0; z-index: 100;
}

.sidebar-header {
  display: flex; align-items: center; gap: 12px;
  padding: 20px 20px 24px; border-bottom: 1px solid rgba(255,255,255,0.06);
}

.logo-box {
  width: 40px; height: 40px; border-radius: 10px;
  background: linear-gradient(135deg, #0891b2, #06b6d4);
  display: flex; align-items: center; justify-content: center;
  color: #fff; flex-shrink: 0;
}

.logo-text { display: flex; flex-direction: column; }
.logo-title { font-size: 16px; font-weight: 800; letter-spacing: -0.3px; }
.logo-sub { font-size: 11px; color: rgba(255,255,255,0.4); margin-top: 1px; }

.sidebar-nav {
  flex: 1; padding: 12px; display: flex; flex-direction: column; gap: 4px;
}

.nav-item {
  display: flex; align-items: center; gap: 12px;
  padding: 11px 14px; border-radius: 10px;
  color: rgba(255,255,255,0.55); text-decoration: none;
  font-size: 14px; font-weight: 500; transition: 0.15s;
}
.nav-item:hover { color: #fff; background: rgba(255,255,255,0.06); }
.nav-item.active {
  color: #fff; background: linear-gradient(135deg, #0891b2, #06b6d4);
  box-shadow: 0 4px 12px rgba(8,145,178,0.3);
}

.sidebar-footer {
  padding: 16px 20px; border-top: 1px solid rgba(255,255,255,0.06);
  display: flex; align-items: center; justify-content: space-between;
}

.admin-info { display: flex; align-items: center; gap: 10px; }
.admin-avatar {
  width: 34px; height: 34px; border-radius: 8px;
  background: rgba(255,255,255,0.1);
  display: flex; align-items: center; justify-content: center;
  font-size: 13px; font-weight: 700;
}
.admin-detail { display: flex; flex-direction: column; }
.admin-name { font-size: 13px; font-weight: 600; }
.admin-role { font-size: 11px; color: rgba(255,255,255,0.4); }

.logout-btn {
  background: none; border: none; color: rgba(255,255,255,0.4);
  cursor: pointer; padding: 6px; border-radius: 6px; transition: 0.15s;
}
.logout-btn:hover { color: #ef4444; background: rgba(239,68,68,0.1); }

.admin-main {
  flex: 1; margin-left: 240px; padding: 24px; min-height: 100vh;
}
</style>
