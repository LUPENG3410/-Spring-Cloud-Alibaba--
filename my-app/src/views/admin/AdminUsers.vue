<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useAdminStore } from '@/stores/admin'
import { fetchAdminUsers, updateAdminUserStatus } from '@/api/admin'
import type { AdminUserInfo } from '@/types/admin'

const adminStore = useAdminStore()

onMounted(async () => {
  try {
    const data = await fetchAdminUsers()
    adminStore.users = data
  } catch { /* fallback to local */ }
})

const searchQuery = ref('')
const filterStatus = ref<string>('all')

const levelColors: Record<string, string> = {
  '普通会员': '#94a3b8', '白银会员': '#64748b', '黄金会员': '#f59e0b', '钻石会员': '#7c3aed'
}

const filteredUsers = computed(() => {
  return adminStore.users.filter(u => {
    const matchSearch = u.name.includes(searchQuery.value) || u.phone.includes(searchQuery.value)
    const matchStatus = filterStatus.value === 'all' || u.status === filterStatus.value
    return matchSearch && matchStatus
  })
})

async function handleStatusChange(userId: number, newStatus: AdminUserInfo['status']) {
  try {
    await updateAdminUserStatus(userId, newStatus)
    adminStore.updateUserStatus(userId, newStatus)
  } catch (e) {
    console.warn('更新用户状态失败', e)
  }
}
</script>

<template>
  <div class="admin-users">
    <div class="page-header">
      <h1>用户管理</h1>
      <p>查看和管理注册用户</p>
    </div>

    <div class="filters">
      <input v-model="searchQuery" type="text" placeholder="搜索用户名/手机号..." class="search-input" />
      <select v-model="filterStatus" class="filter-select">
        <option value="all">全部状态</option>
        <option value="active">正常</option>
        <option value="disabled">已禁用</option>
      </select>
    </div>

    <div class="table-wrap">
      <table class="data-table">
        <thead>
          <tr>
            <th>用户信息</th>
            <th>手机号</th>
            <th>会员等级</th>
            <th>注册日期</th>
            <th>累计订单</th>
            <th>累计消费</th>
            <th>状态</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="u in filteredUsers" :key="u.id">
            <td>
              <div class="user-cell">
                <div class="user-avatar" :style="{ background: levelColors[u.memberLevel] || '#94a3b8' }">
                  {{ u.name.charAt(0) }}
                </div>
                <span class="user-name">{{ u.name }}</span>
              </div>
            </td>
            <td>{{ u.phone }}</td>
            <td>
              <span class="level-badge" :style="{ color: levelColors[u.memberLevel], background: levelColors[u.memberLevel] + '15' }">
                {{ u.memberLevel }}
              </span>
            </td>
            <td>{{ u.registerDate }}</td>
            <td>{{ u.totalOrders }} 单</td>
            <td class="price">¥{{ u.totalSpent.toLocaleString() }}</td>
            <td>
              <select
                :value="u.status"
                @change="handleStatusChange(u.id, ($event.target as HTMLSelectElement).value as AdminUserInfo['status'])"
                class="status-select"
                :class="u.status"
              >
                <option value="active">正常</option>
                <option value="disabled">禁用</option>
              </select>
            </td>
            <td>
              <span class="action-text" v-if="u.status === 'active'" @click="handleStatusChange(u.id, 'disabled')">禁用</span>
              <span class="action-text green" v-else @click="handleStatusChange(u.id, 'active')">启用</span>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<style scoped>
.admin-users { max-width: 1200px; }

.page-header { margin-bottom: 24px; }
.page-header h1 { font-size: 24px; font-weight: 800; color: #0f172a; }
.page-header p { font-size: 14px; color: #94a3b8; margin-top: 4px; }

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

.table-wrap {
  background: #fff; border-radius: 14px; overflow: hidden;
  box-shadow: 0 1px 3px rgba(0,0,0,0.04);
}

.data-table { width: 100%; border-collapse: collapse; }
.data-table th {
  text-align: left; padding: 14px 16px; font-size: 12px;
  font-weight: 600; color: #94a3b8; text-transform: uppercase;
  letter-spacing: 0.5px; background: #f8fafc; border-bottom: 1px solid #f1f5f9;
}
.data-table td {
  padding: 14px 16px; border-bottom: 1px solid #f1f5f9;
  font-size: 14px; color: #334155;
}
.data-table tr:hover td { background: #f8fafc; }

.user-cell { display: flex; align-items: center; gap: 10px; }
.user-avatar {
  width: 32px; height: 32px; border-radius: 8px;
  color: #fff; display: flex; align-items: center; justify-content: center;
  font-size: 13px; font-weight: 700;
}
.user-name { font-weight: 600; color: #0f172a; }

.level-badge {
  font-size: 12px; font-weight: 600; padding: 4px 10px; border-radius: 6px;
}

.price { font-weight: 700; color: #0f172a; }

.status-select {
  border: none; padding: 5px 10px; border-radius: 6px;
  font-size: 12px; font-weight: 600; cursor: pointer; outline: none;
}
.status-select.active { color: #16a34a; background: #f0fdf4; }
.status-select.disabled { color: #ef4444; background: #fef2f2; }

.action-text {
  font-size: 13px; font-weight: 500; color: #ef4444; cursor: pointer;
}
.action-text.green { color: #16a34a; }
</style>
