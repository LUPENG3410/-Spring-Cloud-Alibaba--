<script setup lang="ts">
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { useBookingStore } from '@/stores/booking'

const router = useRouter()
const userStore = useUserStore()
const bookingStore = useBookingStore()

const completed = bookingStore.bookings.filter(b => b.status === 'completed').length
const pending = bookingStore.bookings.filter(b => b.status !== 'completed' && b.status !== 'cancelled').length
const spent = bookingStore.bookings.filter(b => b.status !== 'cancelled').reduce((s, b) => s + b.totalPrice, 0)

function logout() { userStore.logout(); router.push('/') }

const menus = [
  { icon: '📋', label: '我的订单', route: '/bookings' },
  { icon: '📚', label: '历史订单', route: '/bookings' },
  { icon: '💳', label: '我的账户', route: '/bookings' },
  { icon: '👤', label: '我的信息', route: '/bookings' },
  { icon: '💬', label: '在线客服', route: '/bookings' },
]
</script>

<template>
  <div class="profile-page" v-if="userStore.user">
    <div class="hero-card">
      <div class="hero-bg"></div>
      <div class="hero-body">
        <div class="avatar">{{ userStore.user.name.charAt(0) }}</div>
        <div class="meta"><h2>{{ userStore.user.name }}</h2><p>{{ userStore.user.phone }}</p><span class="level">{{ userStore.user.memberLevel }}</span></div>
      </div>
    </div>

    <div class="stats">
      <div class="sc"><span class="sv">{{ bookingStore.bookings.length }}</span><span class="sl">总订单</span></div>
      <div class="sc"><span class="sv">{{ completed }}</span><span class="sl">已完成</span></div>
      <div class="sc"><span class="sv">{{ pending }}</span><span class="sl">进行中</span></div>
      <div class="sc"><span class="sv">¥{{ spent }}</span><span class="sl">总消费</span></div>
    </div>

    <div class="menu-card">
      <router-link v-for="m in menus" :key="m.label" :to="m.route || '#'" class="mi">
        <span class="mi-icon">{{ m.icon }}</span><span class="mi-label">{{ m.label }}</span>
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="#cbd5e1" stroke-width="2"><path d="m9 18 6-6-6-6"/></svg>
      </router-link>
    </div>

    <button class="btn-logout" @click="logout">
      <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/><polyline points="16 17 21 12 16 7"/><line x1="21" y1="12" x2="9" y2="12"/></svg>
      退出登录
    </button>
  </div>

  <div class="not-login" v-else>
    <svg width="56" height="56" viewBox="0 0 24 24" fill="none" stroke="#cbd5e1" stroke-width="1.5"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg>
    <p>请先登录</p>
    <router-link to="/login" class="btn-primary">去登录</router-link>
  </div>
</template>

<style scoped>
.profile-page { max-width: 600px; margin: 0 auto; padding: 0 32px 80px; }
.hero-card { position: relative; border-radius: 20px; overflow: hidden; margin-bottom: 20px; }
.hero-bg { position: absolute; inset: 0;   background: linear-gradient(135deg, #0891b2, #06b6d4, #8b5cf6); }
.hero-body { position: relative; display: flex; align-items: center; gap: 20px; padding: 44px 36px; }
.avatar { width: 72px; height: 72px; border-radius: 18px; background: rgba(255,255,255,0.2); backdrop-filter: blur(8px); color: #fff; display: flex; align-items: center; justify-content: center; font-size: 28px; font-weight: 800; flex-shrink: 0; }
.meta h2 { font-size: 22px; font-weight: 800; color: #fff; margin-bottom: 2px; }
.meta p { color: rgba(255,255,255,0.7); font-size: 13px; margin-bottom: 10px; }
.level { background: #f59e0b; color: #fff; padding: 3px 12px; border-radius: 100px; font-size: 11px; font-weight: 700; }

.stats { display: grid; grid-template-columns: repeat(4,1fr); gap: 10px; margin-bottom: 20px; }
.sc { background: #fff; padding: 20px 12px; border-radius: 12px; text-align: center; border: 1px solid #f1f5f9; }
.sv { display: block; font-size: 22px; font-weight: 800; color: #1a1a2e; margin-bottom: 2px; }
.sl { font-size: 12px; color: #94a3b8; }

.menu-card { background: #fff; border-radius: 14px; overflow: hidden; border: 1px solid #f1f5f9; margin-bottom: 20px; }
.mi { display: flex; align-items: center; padding: 16px 20px; text-decoration: none; color: #334155; border-bottom: 1px solid #f8fafc; transition: 0.15s; }
.mi:last-child { border-bottom: none; }
.mi:hover { background: #f8fafc; }
.mi-icon { font-size: 18px; margin-right: 14px; width: 24px; text-align: center; }
.mi-label { flex: 1; font-size: 14px; font-weight: 500; }

.btn-logout { width: 100%; display: flex; align-items: center; justify-content: center; gap: 6px; background: #fff; color: #dc2626; border: 1.5px solid #fecaca; padding: 13px; border-radius: 12px; font-size: 14px; font-weight: 600; cursor: pointer; transition: 0.15s; }
.btn-logout:hover { background: #fef2f2; border-color: #dc2626; }

.not-login { text-align: center; padding: 120px 24px; }
.not-login p { font-size: 16px; color: #64748b; margin: 16px 0 24px; }
.btn-primary { display: inline-block; background: linear-gradient(135deg, #0891b2, #06b6d4); color: #fff; text-decoration: none; padding: 10px 28px; border-radius: 10px; font-weight: 600; }
</style>
