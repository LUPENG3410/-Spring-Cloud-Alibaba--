<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useAdminStore } from '@/stores/admin'
import { fetchAdminBookings, updateAdminBookingStatus } from '@/api/admin'
import type { AdminBooking } from '@/types/admin'

const adminStore = useAdminStore()

onMounted(async () => {
  try {
    const data = await fetchAdminBookings()
    adminStore.bookings = data
  } catch { /* fallback to local */ }
})

const searchQuery = ref('')
const filterStatus = ref<string>('all')
const filterProvince = ref<string>('all')
const showDetailModal = ref(false)
const selectedBooking = ref<AdminBooking | null>(null)

const statusLabels: Record<string, { label: string; color: string; bg: string; icon: string }> = {
  pending: { label: '待确认', color: '#ea580c', bg: '#fff7ed', icon: '⏳' },
  confirmed: { label: '已确认', color: '#2563eb', bg: '#eff6ff', icon: '✓' },
  completed: { label: '已完成', color: '#16a34a', bg: '#f0fdf4', icon: '✓✓' },
  cancelled: { label: '已取消', color: '#94a3b8', bg: '#f8fafc', icon: '✕' },
}

const provinces = computed(() => {
  const set = new Set(adminStore.bookings.map(b => b.pickupProvince))
  return Array.from(set).sort()
})

const filteredBookings = computed(() => {
  return adminStore.bookings.filter(b => {
    const matchSearch = b.userName.includes(searchQuery.value) || b.carName.includes(searchQuery.value) || b.userPhone.includes(searchQuery.value) || String(b.id).includes(searchQuery.value)
    const matchStatus = filterStatus.value === 'all' || b.status === filterStatus.value
    const matchProvince = filterProvince.value === 'all' || b.pickupProvince === filterProvince.value
    return matchSearch && matchStatus && matchProvince
  })
})

const PAGE_SIZE = 15
const currentPage = ref(1)
const totalPages = computed(() => Math.max(1, Math.ceil(filteredBookings.value.length / PAGE_SIZE)))
const paginatedBookings = computed(() => {
  const start = (currentPage.value - 1) * PAGE_SIZE
  return filteredBookings.value.slice(start, start + PAGE_SIZE)
})

watch([searchQuery, filterStatus, filterProvince], () => { currentPage.value = 1 })

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

const stats = computed(() => {
  const all = adminStore.bookings
  return {
    total: all.length,
    pending: all.filter(b => b.status === 'pending').length,
    confirmed: all.filter(b => b.status === 'confirmed').length,
    completed: all.filter(b => b.status === 'completed').length,
    cancelled: all.filter(b => b.status === 'cancelled').length,
    revenue: all.filter(b => b.status !== 'cancelled').reduce((s, b) => s + b.totalPrice, 0),
  }
})

async function handleStatusChange(bookingId: number, newStatus: string) {
  try {
    await updateAdminBookingStatus(bookingId, newStatus)
    adminStore.updateBookingStatus(bookingId, newStatus as AdminBooking['status'])
  } catch (e) {
    console.warn('更新订单状态失败', e)
  }
}

function viewDetail(booking: AdminBooking) {
  selectedBooking.value = booking
  showDetailModal.value = true
}

function closeDetail() {
  showDetailModal.value = false
  selectedBooking.value = null
}
</script>

<template>
  <div class="admin-bookings">
    <div class="page-header">
      <div>
        <h1>订单管理</h1>
        <p>查看和管理所有租车订单</p>
      </div>
    </div>

    <div class="stats-grid">
      <div class="stat-card">
        <div class="stat-icon total">📋</div>
        <div class="stat-info"><span class="stat-num">{{ stats.total }}</span><span class="stat-label">全部订单</span></div>
      </div>
      <div class="stat-card">
        <div class="stat-icon pending">⏳</div>
        <div class="stat-info"><span class="stat-num">{{ stats.pending }}</span><span class="stat-label">待确认</span></div>
      </div>
      <div class="stat-card">
        <div class="stat-icon confirmed">✓</div>
        <div class="stat-info"><span class="stat-num">{{ stats.confirmed }}</span><span class="stat-label">已确认</span></div>
      </div>
      <div class="stat-card">
        <div class="stat-icon completed">✓✓</div>
        <div class="stat-info"><span class="stat-num">{{ stats.completed }}</span><span class="stat-label">已完成</span></div>
      </div>
      <div class="stat-card">
        <div class="stat-icon cancelled">✕</div>
        <div class="stat-info"><span class="stat-num">{{ stats.cancelled }}</span><span class="stat-label">已取消</span></div>
      </div>
      <div class="stat-card revenue">
        <div class="stat-icon money">💰</div>
        <div class="stat-info"><span class="stat-num">¥{{ stats.revenue.toLocaleString() }}</span><span class="stat-label">总营收</span></div>
      </div>
    </div>

    <div class="filters">
      <input v-model="searchQuery" type="text" placeholder="搜索订单号/用户名/车辆/手机号..." class="search-input" />
      <select v-model="filterStatus" class="filter-select">
        <option value="all">全部状态</option>
        <option value="pending">待确认</option>
        <option value="confirmed">已确认</option>
        <option value="completed">已完成</option>
        <option value="cancelled">已取消</option>
      </select>
      <select v-model="filterProvince" class="filter-select">
        <option value="all">全部地区</option>
        <option v-for="p in provinces" :key="p" :value="p">{{ p }}</option>
      </select>
    </div>

    <div class="table-wrap">
      <table class="data-table">
        <thead>
          <tr>
            <th>订单号</th>
            <th>用户信息</th>
            <th>车辆信息</th>
            <th>租期</th>
            <th>费用</th>
            <th>取/还车门店</th>
            <th>状态</th>
            <th>下单时间</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="b in paginatedBookings" :key="b.id">
            <td>
              <span class="order-id" @click="viewDetail(b)">#{{ String(b.id).padStart(6, '0') }}</span>
            </td>
            <td>
              <div class="user-cell">
                <span class="user-name">{{ b.userName }}</span>
                <span class="user-phone">{{ b.userPhone }}</span>
              </div>
            </td>
            <td>
              <div class="car-cell">
                <img :src="b.carImage" class="car-thumb" :alt="b.carName" />
                <span class="car-name">{{ b.carName }}</span>
              </div>
            </td>
            <td>
              <div class="date-cell">
                <span class="date-range">{{ b.startDate }} {{ b.pickupTime ? b.pickupTime.split('T')[1]?.substring(0,5) : '' }} 至 {{ b.endDate }} {{ b.returnTime ? b.returnTime.split('T')[1]?.substring(0,5) : '' }}</span>
                <span class="days">共{{ b.totalDays }}天</span>
              </div>
            </td>
            <td>
              <div class="price-cell">
                <span class="price">¥{{ b.totalPrice }}</span>
                <span class="daily">¥{{ Math.round(b.totalPrice / b.totalDays) }}/天</span>
                <span v-if="b.insuranceName" class="insurance-tag">{{ b.insuranceName }}</span>
              </div>
            </td>
            <td>
              <div class="location-cell">
                <div class="loc-row"><span class="loc-label">取</span><span>{{ b.pickupProvince }} · {{ b.pickupLocation }}</span></div>
                <div class="loc-row"><span class="loc-label return">还</span><span>{{ b.returnProvince }} · {{ b.returnLocation }}</span></div>
              </div>
            </td>
            <td>
              <span class="status-badge" :style="{ color: statusLabels[b.status]?.color, background: statusLabels[b.status]?.bg }">
                {{ statusLabels[b.status]?.icon }} {{ statusLabels[b.status]?.label }}
              </span>
            </td>
            <td>
              <span class="create-time">{{ b.createdAt }}</span>
            </td>
            <td>
              <div class="actions">
                <button v-if="b.status === 'pending'" class="btn-action confirm" @click="handleStatusChange(b.id, 'confirmed')">确认</button>
                <button v-if="b.status === 'confirmed'" class="btn-action complete" @click="handleStatusChange(b.id, 'completed')">完成</button>
                <button v-if="b.status === 'pending' || b.status === 'confirmed'" class="btn-action cancel" @click="handleStatusChange(b.id, 'cancelled')">取消</button>
                <button class="btn-action detail" @click="viewDetail(b)">详情</button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>

      <div class="empty" v-if="filteredBookings.length === 0">
        <p>暂无匹配的订单</p>
      </div>
    </div>

    <div class="pagination" v-if="totalPages > 1">
      <button class="page-btn" :disabled="currentPage <= 1" @click="currentPage--">上一页</button>
      <template v-for="p in visiblePages" :key="p">
        <span v-if="p === '...'" class="page-dots">...</span>
        <button v-else :class="['page-btn', { active: p === currentPage }]" @click="currentPage = p as number">{{ p }}</button>
      </template>
      <button class="page-btn" :disabled="currentPage >= totalPages" @click="currentPage++">下一页</button>
      <span class="page-info">共 {{ filteredBookings.length }} 条，{{ totalPages }} 页</span>
    </div>

    <!-- Order Detail Modal -->
    <div class="modal-overlay" v-if="showDetailModal && selectedBooking" @click.self="closeDetail">
      <div class="modal detail-modal">
        <div class="modal-header">
          <h2>订单详情 #{{ String(selectedBooking.id).padStart(6, '0') }}</h2>
          <button class="close-btn" @click="closeDetail">&times;</button>
        </div>
        <div class="modal-body">
          <div class="detail-status" :style="{ color: statusLabels[selectedBooking.status]?.color, background: statusLabels[selectedBooking.status]?.bg }">
            {{ statusLabels[selectedBooking.status]?.icon }} {{ statusLabels[selectedBooking.status]?.label }}
          </div>

          <div class="detail-grid">
            <div class="detail-section">
              <h4>用户信息</h4>
              <div class="detail-row"><span>姓名</span><span>{{ selectedBooking.userName }}</span></div>
              <div class="detail-row"><span>手机号</span><span>{{ selectedBooking.userPhone }}</span></div>
            </div>
            <div class="detail-section">
              <h4>车辆信息</h4>
              <div class="detail-row"><span>车辆</span><span>{{ selectedBooking.carName }}</span></div>
              <div class="detail-row"><span>车辆ID</span><span>#{{ selectedBooking.carId }}</span></div>
            </div>
            <div class="detail-section">
              <h4>租期信息</h4>
              <div class="detail-row"><span>取车日期</span><span>{{ selectedBooking.startDate }}</span></div>
              <div class="detail-row"><span>还车日期</span><span>{{ selectedBooking.endDate }}</span></div>
              <div class="detail-row"><span>租赁天数</span><span>{{ selectedBooking.totalDays }}天</span></div>
            </div>
            <div class="detail-section">
              <h4>费用信息</h4>
              <div class="detail-row"><span>日租金</span><span>¥{{ Math.round(selectedBooking.totalPrice / selectedBooking.totalDays) }}</span></div>
              <div class="detail-row" v-if="selectedBooking.insuranceName"><span>保障服务</span><span class="insurance-tag">{{ selectedBooking.insuranceName }}</span></div>
              <div class="detail-row" v-if="selectedBooking.insurancePrice"><span>保障费用</span><span>¥{{ selectedBooking.insurancePrice }}</span></div>
              <div class="detail-row total"><span>总费用</span><span class="total-price">¥{{ selectedBooking.totalPrice }}</span></div>
            </div>
            <div class="detail-section full">
              <h4>取还车门店</h4>
              <div class="loc-detail">
                <div class="loc-item">
                  <span class="loc-dot pickup"></span>
                  <div><span class="loc-type">取车</span><span class="loc-text">{{ selectedBooking.pickupProvince }} · {{ selectedBooking.pickupLocation }}</span></div>
                </div>
                <div class="loc-line"></div>
                <div class="loc-item">
                  <span class="loc-dot return"></span>
                  <div><span class="loc-type">还车</span><span class="loc-text">{{ selectedBooking.returnProvince }} · {{ selectedBooking.returnLocation }}</span></div>
                </div>
              </div>
            </div>
            <div class="detail-section full">
              <h4>订单时间</h4>
              <div class="detail-row"><span>下单时间</span><span>{{ selectedBooking.createdAt }}</span></div>
            </div>
          </div>
        </div>
        <div class="modal-footer">
          <button v-if="selectedBooking.status === 'pending'" class="btn-primary" @click="handleStatusChange(selectedBooking.id, 'confirmed'); closeDetail()">确认订单</button>
          <button v-if="selectedBooking.status === 'confirmed'" class="btn-primary complete" @click="handleStatusChange(selectedBooking.id, 'completed'); closeDetail()">完成订单</button>
          <button v-if="selectedBooking.status === 'pending' || selectedBooking.status === 'confirmed'" class="btn-secondary cancel" @click="handleStatusChange(selectedBooking.id, 'cancelled'); closeDetail()">取消订单</button>
          <button class="btn-secondary" @click="closeDetail">关闭</button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.admin-bookings { max-width: 1200px; }

.page-header { display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 24px; }
.page-header h1 { font-size: 24px; font-weight: 800; color: #0f172a; }
.page-header p { font-size: 14px; color: #94a3b8; margin-top: 4px; }

/* Stats */
.stats-grid { display: grid; grid-template-columns: repeat(6, 1fr); gap: 12px; margin-bottom: 24px; }
.stat-card {
  background: #fff; border-radius: 12px; padding: 16px; display: flex; align-items: center; gap: 12px;
  border: 1px solid #f1f5f9; transition: 0.2s;
}
.stat-card:hover { box-shadow: 0 4px 12px rgba(0,0,0,0.06); }
.stat-icon {
  width: 40px; height: 40px; border-radius: 10px; display: flex; align-items: center; justify-content: center;
  font-size: 16px; flex-shrink: 0;
}
.stat-icon.total { background: #f1f5f9; }
.stat-icon.pending { background: #fff7ed; }
.stat-icon.confirmed { background: #eff6ff; }
.stat-icon.completed { background: #f0fdf4; }
.stat-icon.cancelled { background: #f8fafc; }
.stat-icon.money { background: #fef3c7; }
.stat-info { display: flex; flex-direction: column; }
.stat-num { font-size: 18px; font-weight: 800; color: #0f172a; line-height: 1.2; }
.stat-label { font-size: 12px; color: #94a3b8; }

/* Filters */
.filters { display: flex; gap: 12px; margin-bottom: 20px; }
.search-input {
  flex: 1; padding: 10px 16px; border: 1.5px solid #e2e8f0;
  border-radius: 10px; font-size: 14px; outline: none;
}
.search-input:focus { border-color: #0891b2; }
.filter-select {
  padding: 10px 16px; border: 1.5px solid #e2e8f0;
  border-radius: 10px; font-size: 14px; outline: none; background: #fff;
}

/* Table */
.table-wrap {
  background: #fff; border-radius: 14px; overflow: hidden;
  box-shadow: 0 1px 3px rgba(0,0,0,0.04);
}
.data-table { width: 100%; border-collapse: collapse; }
.data-table th {
  text-align: left; padding: 14px 12px; font-size: 11px;
  font-weight: 600; color: #94a3b8; text-transform: uppercase;
  letter-spacing: 0.5px; background: #f8fafc; border-bottom: 1px solid #f1f5f9;
  white-space: nowrap;
}
.data-table td {
  padding: 14px 12px; border-bottom: 1px solid #f1f5f9;
  font-size: 13px; color: #334155; vertical-align: middle;
}
.data-table tr:hover td { background: #f8fafc; }

.order-id { font-weight: 700; color: #0891b2; cursor: pointer; }
.order-id:hover { text-decoration: underline; }

.user-cell { display: flex; flex-direction: column; gap: 2px; }
.user-name { font-weight: 600; color: #0f172a; }
.user-phone { font-size: 12px; color: #94a3b8; }

.car-cell { display: flex; align-items: center; gap: 8px; }
.car-thumb { width: 48px; height: 30px; border-radius: 4px; object-fit: cover; }
.car-name { font-weight: 500; }

.date-cell { display: flex; flex-direction: column; gap: 2px; }
.date-range { font-size: 12px; color: #334155; white-space: nowrap; }
.days { font-size: 11px; color: #0891b2; font-weight: 600; }

.price-cell { display: flex; flex-direction: column; gap: 2px; }
.price { font-weight: 700; color: #0f172a; }
.daily { font-size: 11px; color: #94a3b8; }
.insurance-tag { font-size: 11px; color: #0891b2; font-weight: 600; margin-top: 2px; }

.location-cell { display: flex; flex-direction: column; gap: 4px; }
.loc-row { display: flex; align-items: center; gap: 6px; font-size: 12px; }
.loc-label {
  font-size: 10px; font-weight: 700; color: #fff; background: #0891b2;
  padding: 1px 5px; border-radius: 3px; flex-shrink: 0;
}
.loc-label.return { background: #8b5cf6; }

.status-badge {
  display: inline-flex; align-items: center; gap: 4px;
  padding: 4px 10px; border-radius: 6px; font-size: 12px; font-weight: 600;
  white-space: nowrap;
}

.actions { display: flex; gap: 6px; }
.btn-action {
  border: none; padding: 5px 10px; border-radius: 6px;
  font-size: 11px; font-weight: 600; cursor: pointer; transition: 0.15s;
}
.btn-action.confirm { background: #eff6ff; color: #2563eb; }
.btn-action.confirm:hover { background: #2563eb; color: #fff; }
.btn-action.complete { background: #f0fdf4; color: #16a34a; }
.btn-action.complete:hover { background: #16a34a; color: #fff; }
.btn-action.cancel { background: #fef2f2; color: #dc2626; }
.btn-action.cancel:hover { background: #dc2626; color: #fff; }
.btn-action.detail { background: #f8fafc; color: #64748b; }
.btn-action.detail:hover { background: #f1f5f9; color: #334155; }

.create-time { font-size: 12px; color: #94a3b8; white-space: nowrap; }

.empty { text-align: center; padding: 60px 20px; color: #94a3b8; }

/* Modal */
.modal-overlay {
  position: fixed; inset: 0; background: rgba(0,0,0,0.5);
  display: flex; align-items: center; justify-content: center; z-index: 1000;
}
.detail-modal {
  background: #fff; border-radius: 16px; width: 680px; max-height: 85vh;
  display: flex; flex-direction: column; overflow: hidden;
}
.modal-header {
  display: flex; justify-content: space-between; align-items: center;
  padding: 20px 24px; border-bottom: 1px solid #f1f5f9;
}
.modal-header h2 { font-size: 16px; font-weight: 700; color: #0f172a; }
.close-btn {
  background: none; border: none; font-size: 24px; color: #94a3b8;
  cursor: pointer; padding: 0 4px; line-height: 1;
}
.close-btn:hover { color: #334155; }

.modal-body { flex: 1; overflow-y: auto; padding: 20px 24px; }

.detail-status {
  display: inline-flex; align-items: center; gap: 6px;
  padding: 6px 14px; border-radius: 8px; font-size: 13px; font-weight: 600;
  margin-bottom: 20px;
}

.detail-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; }
.detail-section {
  background: #f8fafc; border-radius: 10px; padding: 14px;
}
.detail-section.full { grid-column: 1 / -1; }
.detail-section h4 {
  font-size: 12px; font-weight: 700; color: #0891b2;
  margin-bottom: 10px; text-transform: uppercase; letter-spacing: 0.5px;
}
.detail-row {
  display: flex; justify-content: space-between; padding: 6px 0;
  border-bottom: 1px solid #e2e8f0; font-size: 13px;
}
.detail-row:last-child { border-bottom: none; }
.detail-row span:first-child { color: #94a3b8; }
.detail-row span:last-child { font-weight: 600; color: #0f172a; }
.detail-row.total { padding-top: 8px; }
.total-price { font-size: 16px; color: #ef4444; font-weight: 800; }
.insurance-tag { color: #0891b2; font-weight: 600; }

.loc-detail { display: flex; flex-direction: column; gap: 0; }
.loc-item { display: flex; align-items: center; gap: 10px; padding: 8px 0; }
.loc-dot { width: 10px; height: 10px; border-radius: 50%; flex-shrink: 0; }
.loc-dot.pickup { background: #0891b2; }
.loc-dot.return { background: #8b5cf6; }
.loc-line { width: 2px; height: 16px; background: #e2e8f0; margin-left: 4px; }
.loc-type { display: block; font-size: 11px; color: #94a3b8; font-weight: 600; }
.loc-text { font-size: 13px; color: #0f172a; font-weight: 500; }

.modal-footer {
  display: flex; justify-content: flex-end; gap: 10px;
  padding: 16px 24px; border-top: 1px solid #f1f5f9;
}
.btn-primary {
  background: linear-gradient(135deg, #0891b2, #06b6d4);
  color: #fff; border: none; padding: 8px 18px; border-radius: 8px;
  font-size: 13px; font-weight: 600; cursor: pointer;
}
.btn-primary.complete { background: linear-gradient(135deg, #16a34a, #22c55e); }
.btn-secondary {
  padding: 8px 18px; border: 1.5px solid #e2e8f0; border-radius: 8px;
  background: #fff; font-size: 13px; font-weight: 500; cursor: pointer; color: #64748b;
}
.btn-secondary.cancel { border-color: #fecaca; color: #dc2626; }
.btn-secondary.cancel:hover { background: #fef2f2; }

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
