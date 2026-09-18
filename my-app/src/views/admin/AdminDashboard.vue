<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useAdminStore } from '@/stores/admin'
import { fetchAdminStats, fetchAdminBookings } from '@/api/admin'

const adminStore = useAdminStore()
const loading = ref(true)

const stats = computed(() => adminStore.stats)

onMounted(async () => {
  try {
    const [s, b] = await Promise.all([
      fetchAdminStats(),
      fetchAdminBookings()
    ])
    adminStore.stats = s
    adminStore.bookings = b
  } catch {
    console.info('仪表盘API不可用，使用本地数据')
  } finally {
    loading.value = false
  }
})

const statCards = computed(() => [
  { label: '总车辆', value: stats.value.totalCars, suffix: '辆', color: '#2563eb', bg: '#eff6ff', icon: 'M19 17h2c.6 0 1-.4 1-1v-3c0-.9-.7-1.7-1.5-1.9C18.7 10.6 16 10 16 10s-1.3-1.4-2.2-2.3c-.5-.4-1.1-.7-1.8-.7H5c-.6 0-1.1.4-1.4.9l-1.4 2.9A3.7 3.7 0 002 12v4c0 .6.4 1 1 1h2M7 17a2 2 0 100 4 2 2 0 000-4zm10 0a2 2 0 100 4 2 2 0 000-4z' },
  { label: '总订单', value: stats.value.totalBookings, suffix: '单', color: '#0891b2', bg: '#f0fdfa', icon: 'M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2' },
  { label: '注册用户', value: stats.value.totalUsers, suffix: '人', color: '#7c3aed', bg: '#faf5ff', icon: 'M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197M13 7a4 4 0 11-8 0 4 4 0 018 0z' },
  { label: '本月营收', value: stats.value.monthlyRevenue, suffix: '元', color: '#16a34a', bg: '#f0fdf4', icon: 'M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1M21 12a9 9 0 11-18 0 9 9 0 0118 0z' },
])

const recentBookings = computed(() => adminStore.bookings.slice(0, 5))

const statusMap: Record<string, { label: string; color: string; bg: string }> = {
  pending: { label: '待确认', color: '#ea580c', bg: '#fff7ed' },
  confirmed: { label: '已确认', color: '#2563eb', bg: '#eff6ff' },
  completed: { label: '已完成', color: '#16a34a', bg: '#f0fdf4' },
  cancelled: { label: '已取消', color: '#94a3b8', bg: '#f8fafc' },
}
</script>

<template>
  <div class="dashboard">
    <div class="page-header">
      <h1>仪表盘</h1>
      <p>欢迎回来，{{ adminStore.adminUser?.name }}</p>
    </div>

    <div class="stats-grid">
      <div v-for="card in statCards" :key="card.label" class="stat-card">
        <div class="stat-icon" :style="{ background: card.bg, color: card.color }">
          <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
            <path :d="card.icon"/>
          </svg>
        </div>
        <div class="stat-info">
          <span class="stat-value">{{ card.value.toLocaleString() }}<small>{{ card.suffix }}</small></span>
          <span class="stat-label">{{ card.label }}</span>
        </div>
      </div>
    </div>

    <div class="dashboard-grid">
      <div class="card recent-orders">
        <div class="card-header">
          <h2>最近订单</h2>
          <router-link to="/admin/bookings" class="view-all">查看全部 →</router-link>
        </div>
        <div class="order-list">
          <div v-for="order in recentBookings" :key="order.id" class="order-item">
            <img :src="order.carImage" class="order-car-img" :alt="order.carName" />
            <div class="order-info">
              <span class="order-car">{{ order.carName }}</span>
              <span class="order-user">{{ order.userName }} · {{ order.userPhone }}</span>
            </div>
            <div class="order-meta">
              <span class="order-price">¥{{ order.totalPrice }}</span>
              <span class="order-status" :style="{ color: statusMap[order.status]?.color, background: statusMap[order.status]?.bg }">
                {{ statusMap[order.status]?.label }}
              </span>
            </div>
          </div>
        </div>
      </div>

      <div class="card quick-stats">
        <div class="card-header">
          <h2>运营概览</h2>
        </div>
        <div class="quick-list">
          <div class="quick-item">
            <span class="quick-label">待处理订单</span>
            <span class="quick-value orange">{{ stats.pendingBookings }} 单</span>
          </div>
          <div class="quick-item">
            <span class="quick-label">今日新增订单</span>
            <span class="quick-value blue">{{ stats.todayBookings }} 单</span>
          </div>
          <div class="quick-item">
            <span class="quick-label">可用车辆</span>
            <span class="quick-value green">{{ stats.availableCars }} / {{ stats.totalCars }}</span>
          </div>
          <div class="quick-item">
            <span class="quick-label">门店总数</span>
            <span class="quick-value purple">{{ stats.totalStores }} 家</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.dashboard { max-width: 1200px; }

.page-header { margin-bottom: 28px; }
.page-header h1 { font-size: 24px; font-weight: 800; color: #0f172a; }
.page-header p { font-size: 14px; color: #94a3b8; margin-top: 4px; }

.stats-grid {
  display: grid; grid-template-columns: repeat(4, 1fr); gap: 16px; margin-bottom: 24px;
}

.stat-card {
  background: #fff; border-radius: 14px; padding: 22px;
  display: flex; align-items: center; gap: 16px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.04);
}

.stat-icon {
  width: 48px; height: 48px; border-radius: 12px;
  display: flex; align-items: center; justify-content: center; flex-shrink: 0;
}

.stat-info { display: flex; flex-direction: column; }
.stat-value { font-size: 22px; font-weight: 800; color: #0f172a; }
.stat-value small { font-size: 13px; font-weight: 500; color: #94a3b8; margin-left: 2px; }
.stat-label { font-size: 13px; color: #94a3b8; margin-top: 2px; }

.dashboard-grid { display: grid; grid-template-columns: 2fr 1fr; gap: 20px; }

.card {
  background: #fff; border-radius: 14px; padding: 24px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.04);
}

.card-header {
  display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px;
}
.card-header h2 { font-size: 16px; font-weight: 700; color: #0f172a; }
.view-all { font-size: 13px; color: #0891b2; text-decoration: none; font-weight: 500; }

.order-list { display: flex; flex-direction: column; gap: 12px; }
.order-item {
  display: flex; align-items: center; gap: 12px;
  padding: 12px; border-radius: 10px; background: #f8fafc; transition: 0.15s;
}
.order-item:hover { background: #f1f5f9; }
.order-car-img { width: 48px; height: 32px; border-radius: 6px; object-fit: cover; }
.order-info { flex: 1; display: flex; flex-direction: column; }
.order-car { font-size: 14px; font-weight: 600; color: #0f172a; }
.order-user { font-size: 12px; color: #94a3b8; margin-top: 2px; }
.order-meta { text-align: right; display: flex; flex-direction: column; align-items: flex-end; gap: 4px; }
.order-price { font-size: 14px; font-weight: 700; color: #0f172a; }
.order-status {
  font-size: 11px; font-weight: 600; padding: 3px 8px;
  border-radius: 6px; white-space: nowrap;
}

.quick-list { display: flex; flex-direction: column; gap: 14px; }
.quick-item {
  display: flex; justify-content: space-between; align-items: center;
  padding: 14px 16px; border-radius: 10px; background: #f8fafc;
}
.quick-label { font-size: 14px; color: #64748b; }
.quick-value { font-size: 15px; font-weight: 700; }
.quick-value.orange { color: #ea580c; }
.quick-value.blue { color: #2563eb; }
.quick-value.green { color: #16a34a; }
.quick-value.purple { color: #7c3aed; }
</style>
