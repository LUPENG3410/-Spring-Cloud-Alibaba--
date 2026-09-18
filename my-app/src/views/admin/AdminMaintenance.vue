<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useAdminApi } from '@/api/admin-service'
import type { MaintenanceReminder } from '@/types/admin'

const adminApi = useAdminApi()

const loading = ref(true)
const reminders = ref<MaintenanceReminder[]>([])
const activeTab = ref<'all' | 'overdue' | 'upcoming'>('all')
const showIntervalModal = ref(false)
const selectedCar = ref<MaintenanceReminder | null>(null)
const newInterval = ref(120)

const filteredReminders = computed(() => {
  if (activeTab.value === 'overdue') {
    return reminders.value.filter(r => r.status === 'overdue')
  }
  if (activeTab.value === 'upcoming') {
    return reminders.value.filter(r => r.status === 'upcoming')
  }
  return reminders.value
})

const PAGE_SIZE = 10
const currentPage = ref(1)
const totalPages = computed(() => Math.max(1, Math.ceil(filteredReminders.value.length / PAGE_SIZE)))
const paginatedReminders = computed(() => {
  const start = (currentPage.value - 1) * PAGE_SIZE
  return filteredReminders.value.slice(start, start + PAGE_SIZE)
})

watch(activeTab, () => { currentPage.value = 1 })

const visiblePages = computed(() => {
  const total = totalPages.value
  const cur = currentPage.value
  if (total <= 7) return Array.from({ length: total }, (_, i) => i + 1)
  const pages: (number | string)[] = [1]
  if (cur > 3) pages.push('...')
  for (let i = Math.max(2, cur - 1); i <= Math.min(total - 1, cur + 1); i++) pages.push(i)
  if (cur < total - 2) pages.push('...')
  pages.push(total)
  return pages
})

const overdueCount = computed(() => reminders.value.filter(r => r.status === 'overdue').length)
const upcomingCount = computed(() => reminders.value.filter(r => r.status === 'upcoming').length)

onMounted(async () => {
  await loadReminders()
})

async function loadReminders() {
  loading.value = true
  try {
    reminders.value = await adminApi.getMaintenanceReminders([])
  } catch {
    console.info('保养提醒API不可用')
  } finally {
    loading.value = false
  }
}

function openIntervalModal(reminder: MaintenanceReminder) {
  selectedCar.value = reminder
  newInterval.value = 120
  showIntervalModal.value = true
}

async function saveInterval() {
  if (!selectedCar.value) return
  await adminApi.updateMaintenanceInterval(selectedCar.value.carId, newInterval.value)
  showIntervalModal.value = false
  await loadReminders()
}

async function completeMaintenance(carId: number) {
  await adminApi.markMaintenanceDone(carId)
  await loadReminders()
}

function getStatusColor(status: string) {
  return status === 'overdue' ? { color: '#dc2626', bg: '#fef2f2' } : { color: '#d97706', bg: '#fffbeb' }
}

function formatDays(days: number) {
  if (days < 0) return `逾期 ${Math.abs(days)} 天`
  return `剩余 ${days} 天`
}
</script>

<template>
  <div class="maintenance-page">
    <div class="page-header">
      <div>
        <h1>保养提醒</h1>
        <p>管理车辆保养计划，及时通知门店进行保养</p>
      </div>
    </div>

    <div class="tabs">
      <button
        class="tab"
        :class="{ active: activeTab === 'all' }"
        @click="activeTab = 'all'"
      >
        全部提醒
        <span class="tab-badge">{{ reminders.length }}</span>
      </button>
      <button
        class="tab overdue"
        :class="{ active: activeTab === 'overdue' }"
        @click="activeTab = 'overdue'"
      >
        已逾期
        <span class="tab-badge danger">{{ overdueCount }}</span>
      </button>
      <button
        class="tab upcoming"
        :class="{ active: activeTab === 'upcoming' }"
        @click="activeTab = 'upcoming'"
      >
        即将到期
        <span class="tab-badge warning">{{ upcomingCount }}</span>
      </button>
    </div>

    <div v-if="loading" class="loading">
      <div class="spinner"></div>
      <span>加载中...</span>
    </div>

    <div v-else-if="filteredReminders.length === 0" class="empty-state">
      <svg width="64" height="64" viewBox="0 0 24 24" fill="none" stroke="#94a3b8" stroke-width="1.5">
        <path d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"/>
      </svg>
      <p>暂无保养提醒</p>
    </div>

    <div v-else class="reminder-list">
      <div
        v-for="reminder in paginatedReminders"
        :key="reminder.carId"
        class="reminder-card"
        :class="reminder.status"
      >
        <div class="reminder-header">
          <div class="car-info">
            <span class="plate-number">{{ reminder.plateNumber }}</span>
            <span class="car-name">{{ reminder.carName }}</span>
          </div>
          <span
            class="status-badge"
            :style="{ color: getStatusColor(reminder.status).color, background: getStatusColor(reminder.status).bg }"
          >
            {{ reminder.status === 'overdue' ? '已逾期' : '即将到期' }}
          </span>
        </div>

        <div class="reminder-body">
          <div class="info-row">
            <span class="label">门店</span>
            <span class="value">{{ reminder.storeName }}</span>
          </div>
          <div class="info-row">
            <span class="label">联系电话</span>
            <span class="value">{{ reminder.storePhone || '暂无' }}</span>
          </div>
          <div class="info-row">
            <span class="label">上次保养</span>
            <span class="value">{{ reminder.lastMaintenance }}</span>
          </div>
          <div class="info-row">
            <span class="label">下次保养</span>
            <span class="value highlight">{{ reminder.nextMaintenanceDate }}</span>
          </div>
          <div class="info-row">
            <span class="label">状态</span>
            <span
              class="days-remaining"
              :class="reminder.status"
            >
              {{ formatDays(reminder.daysRemaining) }}
            </span>
          </div>
        </div>

        <div class="reminder-actions">
          <button class="btn-secondary" @click="openIntervalModal(reminder)">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M10.325 4.317c.426-1.756 2.924-1.756 3.35 0a1.724 1.724 0 002.573 1.066c1.543-.94 3.31.826 2.37 2.37a1.724 1.724 0 001.065 2.572c1.756.426 1.756 2.924 0 3.35a1.724 1.724 0 00-1.066 2.573c.94 1.543-.826 3.31-2.37 2.37a1.724 1.724 0 00-2.572 1.065c-.426 1.756-2.924 1.756-3.35 0a1.724 1.724 0 00-2.573-1.066c-1.543.94-3.31-.826-2.37-2.37a1.724 1.724 0 00-1.065-2.572c-1.756-.426-1.756-2.924 0-3.35a1.724 1.724 0 001.066-2.573c-.94-1.543.826-3.31 2.37-2.37.996.608 2.296.07 2.572-1.065z"/>
              <circle cx="12" cy="12" r="3"/>
            </svg>
            设置周期
          </button>
          <button class="btn-primary" @click="completeMaintenance(reminder.carId)">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M5 13l4 4L19 7"/>
            </svg>
            标记完成
          </button>
        </div>
      </div>
    </div>

    <div class="pagination" v-if="totalPages > 1">
      <button class="page-btn" :disabled="currentPage <= 1" @click="currentPage--">上一页</button>
      <template v-for="p in visiblePages" :key="p">
        <span v-if="p === '...'" class="page-dots">...</span>
        <button v-else :class="['page-btn', { active: p === currentPage }]" @click="currentPage = p as number">{{ p }}</button>
      </template>
      <button class="page-btn" :disabled="currentPage >= totalPages" @click="currentPage++">下一页</button>
      <span class="page-info">共 {{ filteredReminders.length }} 条，{{ totalPages }} 页</span>
    </div>

    <div v-if="showIntervalModal" class="modal-overlay" @click.self="showIntervalModal = false">
      <div class="modal">
        <h3>设置保养周期</h3>
        <p class="modal-desc">车辆: {{ selectedCar?.plateNumber }} - {{ selectedCar?.carName }}</p>
        <div class="form-group">
          <label>保养周期（天）</label>
          <input v-model.number="newInterval" type="number" min="1" max="365" />
        </div>
        <div class="modal-actions">
          <button class="btn-cancel" @click="showIntervalModal = false">取消</button>
          <button class="btn-confirm" @click="saveInterval">保存</button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.maintenance-page { max-width: 1200px; }

.page-header {
  display: flex; justify-content: space-between; align-items: flex-start;
  margin-bottom: 24px;
}
.page-header h1 { font-size: 24px; font-weight: 800; color: #0f172a; }
.page-header p { font-size: 14px; color: #94a3b8; margin-top: 4px; }

.tabs {
  display: flex; gap: 8px; margin-bottom: 24px;
}

.tab {
  display: flex; align-items: center; gap: 8px;
  padding: 10px 18px; border-radius: 10px;
  background: #fff; border: 1px solid #e2e8f0;
  font-size: 14px; font-weight: 500; color: #64748b;
  cursor: pointer; transition: 0.15s;
}
.tab:hover { border-color: #cbd5e1; color: #334155; }
.tab.active {
  background: #0891b2; border-color: #0891b2; color: #fff;
}
.tab.active .tab-badge { background: rgba(255,255,255,0.2); color: #fff; }

.tab-badge {
  padding: 2px 8px; border-radius: 12px;
  background: #f1f5f9; font-size: 12px; font-weight: 600;
}
.tab-badge.danger { background: #fef2f2; color: #dc2626; }
.tab-badge.warning { background: #fffbeb; color: #d97706; }
.tab.active .tab-badge.danger,
.tab.active .tab-badge.warning { background: rgba(255,255,255,0.2); color: #fff; }

.loading {
  display: flex; flex-direction: column; align-items: center;
  padding: 60px; gap: 16px; color: #94a3b8;
}

.spinner {
  width: 36px; height: 36px; border: 3px solid #e2e8f0;
  border-top-color: #0891b2; border-radius: 50%;
  animation: spin 0.8s linear infinite;
}
@keyframes spin { to { transform: rotate(360deg); } }

.empty-state {
  display: flex; flex-direction: column; align-items: center;
  padding: 60px; gap: 16px; color: #94a3b8;
}
.empty-state p { font-size: 15px; }

.reminder-list {
  display: grid; gap: 16px;
}

.reminder-card {
  background: #fff; border-radius: 14px; padding: 24px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.04);
  border-left: 4px solid transparent;
}
.reminder-card.overdue { border-left-color: #dc2626; }
.reminder-card.upcoming { border-left-color: #d97706; }

.reminder-header {
  display: flex; justify-content: space-between; align-items: center;
  margin-bottom: 16px;
}

.car-info { display: flex; align-items: center; gap: 12px; }
.plate-number {
  font-size: 18px; font-weight: 700; color: #0f172a;
  font-family: monospace;
}
.car-name { font-size: 14px; color: #64748b; }

.status-badge {
  padding: 6px 12px; border-radius: 8px;
  font-size: 13px; font-weight: 600;
}

.reminder-body {
  display: grid; grid-template-columns: repeat(2, 1fr); gap: 12px;
  margin-bottom: 20px;
}

.info-row {
  display: flex; flex-direction: column; gap: 4px;
}
.info-row .label { font-size: 12px; color: #94a3b8; }
.info-row .value { font-size: 14px; color: #334155; font-weight: 500; }
.info-row .value.highlight { color: #0891b2; font-weight: 600; }

.days-remaining {
  font-size: 14px; font-weight: 600;
}
.days-remaining.overdue { color: #dc2626; }
.days-remaining.upcoming { color: #d97706; }

.reminder-actions {
  display: flex; gap: 12px; padding-top: 16px;
  border-top: 1px solid #f1f5f9;
}

.btn-secondary, .btn-primary {
  display: flex; align-items: center; gap: 6px;
  padding: 10px 16px; border-radius: 8px;
  font-size: 13px; font-weight: 600; cursor: pointer;
  transition: 0.15s; border: none;
}

.btn-secondary {
  background: #f1f5f9; color: #475569;
}
.btn-secondary:hover { background: #e2e8f0; }

.btn-primary {
  background: #0891b2; color: #fff;
}
.btn-primary:hover { background: #0e7490; }

.modal-overlay {
  position: fixed; inset: 0; background: rgba(0,0,0,0.5);
  display: flex; align-items: center; justify-content: center;
  z-index: 1000;
}

.modal {
  background: #fff; border-radius: 16px; padding: 28px;
  width: 400px; max-width: 90vw;
}
.modal h3 { font-size: 18px; font-weight: 700; color: #0f172a; margin-bottom: 8px; }
.modal-desc { font-size: 14px; color: #64748b; margin-bottom: 20px; }

.form-group { margin-bottom: 20px; }
.form-group label {
  display: block; font-size: 13px; font-weight: 600;
  color: #334155; margin-bottom: 8px;
}
.form-group input {
  width: 100%; padding: 12px; border: 1px solid #e2e8f0;
  border-radius: 8px; font-size: 14px; outline: none;
}
.form-group input:focus { border-color: #0891b2; }

.modal-actions {
  display: flex; gap: 12px; justify-content: flex-end;
}

.btn-cancel {
  padding: 10px 20px; border-radius: 8px;
  background: #f1f5f9; color: #475569;
  font-size: 14px; font-weight: 500; cursor: pointer; border: none;
}
.btn-cancel:hover { background: #e2e8f0; }

.btn-confirm {
  padding: 10px 20px; border-radius: 8px;
  background: #0891b2; color: #fff;
  font-size: 14px; font-weight: 600; cursor: pointer; border: none;
}
.btn-confirm:hover { background: #0e7490; }

.pagination {
  display: flex; align-items: center; gap: 6px;
  padding: 20px 0; justify-content: center;
}
.page-btn {
  min-width: 36px; height: 36px; padding: 0 10px;
  border: 1px solid #e2e8f0; background: #fff; border-radius: 8px;
  font-size: 13px; font-weight: 500; color: #475569; cursor: pointer; transition: 0.15s;
}
.page-btn:hover:not(:disabled):not(.active) { border-color: #0891b2; color: #0891b2; }
.page-btn.active { background: #0891b2; color: #fff; border-color: transparent; }
.page-btn:disabled { opacity: 0.4; cursor: not-allowed; }
.page-dots { color: #94a3b8; padding: 0 4px; }
.page-info { font-size: 13px; color: #94a3b8; margin-left: 12px; }
</style>
