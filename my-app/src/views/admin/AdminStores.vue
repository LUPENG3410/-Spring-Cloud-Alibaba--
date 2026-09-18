<script setup lang="ts">
import { ref, computed, onMounted, nextTick } from 'vue'
import { useAdminStore } from '@/stores/admin'
import { fetchAdminStores, updateAdminStoreStatus } from '@/api/admin'
import request from '@/api/request'
import type { AdminStoreLocation } from '@/types/admin'

const adminStore = useAdminStore()

onMounted(async () => {
  try {
    const data = await fetchAdminStores()
    adminStore.stores = data
  } catch { /* fallback to local */ }
})

const searchQuery = ref('')
const filterProvince = ref<string>('all')

const filteredStores = computed(() => {
  return adminStore.stores.filter(s => {
    const matchSearch = s.name.includes(searchQuery.value) || s.city.includes(searchQuery.value)
    const matchProvince = filterProvince.value === 'all' || s.province === filterProvince.value
    return matchSearch && matchProvince
  })
})

async function handleStatusChange(storeId: number, newStatus: AdminStoreLocation['status']) {
  try {
    await updateAdminStoreStatus(storeId, newStatus)
    adminStore.updateStoreStatus(storeId, newStatus)
  } catch (e) {
    console.warn('更新门店状态失败', e)
  }
}

// 添加门店功能
const showAddModal = ref(false)
const newStore = ref({
  name: '',
  province: '',
  city: '',
  address: '',
  phone: '',
  lat: 0,
  lng: 0,
  hours: '08:00-21:00'
})

const provinces = ['北京', '上海', '广东', '四川', '浙江', '宁夏', '天津', '重庆', '河北', '山西', '内蒙古', '辽宁', '吉林', '黑龙江', '江苏', '安徽', '福建', '江西', '山东', '河南', '湖北', '湖南', '广西', '海南', '贵州', '云南', '西藏', '陕西', '甘肃', '青海', '新疆']

// eslint-disable-next-line @typescript-eslint/no-explicit-any
let addMapInstance: any = null
// eslint-disable-next-line @typescript-eslint/no-explicit-any
let addMarker: any = null
// eslint-disable-next-line @typescript-eslint/no-explicit-any
let addLeafletLib: any = null

function openAddModal() {
  newStore.value = {
    name: '',
    province: '',
    city: '',
    address: '',
    phone: '',
    lat: 35.8617,
    lng: 104.1954,
    hours: '08:00-21:00'
  }
  showAddModal.value = true
  nextTick(() => initAddMap())
}

async function initAddMap() {
  if (addMapInstance) {
    addMapInstance.remove()
    addMapInstance = null
  }
  const L = await import('leaflet')
  addLeafletLib = L

  addMapInstance = L.map('add-store-map', {
    center: [35.8617, 104.1954],
    zoom: 5,
    zoomControl: true
  })

  L.tileLayer('https://webrd0{s}.is.autonavi.com/appmaptile?lang=zh_cn&size=1&scale=1&style=8&x={x}&y={y}&z={z}', {
    subdomains: '1234',
    maxZoom: 18,
    attribution: '&copy; 高德地图'
  }).addTo(addMapInstance)

  const orangeIcon = L.divIcon({
    className: 'custom-marker',
    html: `<div style="width:32px;height:32px;background:linear-gradient(135deg,#0891b2,#06b6d4);border-radius:50% 50% 50% 0;transform:rotate(-45deg);border:3px solid #fff;box-shadow:0 4px 12px rgba(0,0,0,0.3);display:flex;align-items:center;justify-content:center"><svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="#fff" stroke-width="2.5" style="transform:rotate(45deg)"><path d="M12 2C8.13 2 5 5.13 5 9c0 5.25 7 13 7 13s7-7.75 7-13c0-3.87-3.13-7-7-7z"/><circle cx="12" cy="9" r="2.5"/></svg></div>`,
    iconSize: [32, 32],
    iconAnchor: [16, 32],
    popupAnchor: [0, -32]
  })

  addMarker = L.marker([35.8617, 104.1954], { icon: orangeIcon, draggable: true }).addTo(addMapInstance)

  addMarker.on('dragend', (e: any) => {
    const pos = e.target.getLatLng()
    newStore.value.lat = Math.round(pos.lat * 10000) / 10000
    newStore.value.lng = Math.round(pos.lng * 10000) / 10000
  })

  addMapInstance.on('click', (e: any) => {
    const pos = e.latlng
    newStore.value.lat = Math.round(pos.lat * 10000) / 10000
    newStore.value.lng = Math.round(pos.lng * 10000) / 10000
    addMarker.setLatLng(pos)
  })

  setTimeout(() => addMapInstance.invalidateSize(), 100)
}

async function handleAddStore() {
  if (!newStore.value.name || !newStore.value.province || !newStore.value.city || !newStore.value.address || !newStore.value.phone) {
    alert('请填写完整信息')
    return
  }
  try {
    await request.post('/admin/stores', newStore.value)
    showAddModal.value = false
    const data = await fetchAdminStores()
    adminStore.stores = data
    alert('门店添加成功')
  } catch (e: any) {
    alert(e?.response?.data?.message || '添加失败')
  }
}

function closeAddModal() {
  showAddModal.value = false
  if (addMapInstance) {
    addMapInstance.remove()
    addMapInstance = null
  }
}
</script>

<template>
  <div class="admin-stores">
    <div class="page-header">
      <div>
        <h1>门店管理</h1>
        <p>查看和管理所有门店信息</p>
      </div>
      <button class="btn-add" @click="openAddModal">
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M12 5v14M5 12h14"/></svg>
        添加门店
      </button>
    </div>

    <div class="filters">
      <input v-model="searchQuery" type="text" placeholder="搜索门店名称/城市..." class="search-input" />
      <select v-model="filterProvince" class="filter-select">
        <option value="all">全部省份</option>
        <option v-for="p in provinces" :key="p">{{ p }}</option>
      </select>
    </div>

    <div class="store-grid">
      <div v-for="s in filteredStores" :key="s.id" class="store-card">
        <div class="store-header">
          <div class="store-status" :class="s.status">
            {{ s.status === 'active' ? '营业中' : '已关闭' }}
          </div>
          <select
            :value="s.status"
            @change="handleStatusChange(s.id, ($event.target as HTMLSelectElement).value as AdminStoreLocation['status'])"
            class="status-select"
          >
            <option value="active">营业</option>
            <option value="closed">关闭</option>
          </select>
        </div>
        <h3 class="store-name">{{ s.name }}</h3>
        <div class="store-info">
          <div class="info-item">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z"/><circle cx="12" cy="11" r="3"/></svg>
            <span>{{ s.address }}</span>
          </div>
          <div class="info-item">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M3 5a2 2 0 012-2h3.28a1 1 0 01.948.684l1.498 4.493a1 1 0 01-.502 1.21l-2.257 1.13a11.042 11.042 0 005.516 5.516l1.13-2.257a1 1 0 011.21-.502l4.493 1.498a1 1 0 01.684.949V19a2 2 0 01-2 2h-1C9.716 21 3 14.284 3 6V5z"/></svg>
            <span>{{ s.phone }}</span>
          </div>
          <div class="info-item">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><path d="M12 6v6l4 2"/></svg>
            <span>{{ s.hours }}</span>
          </div>
        </div>
        <div class="store-footer">
          <span class="car-count">车辆数: {{ s.carCount }}</span>
          <span class="province-tag">{{ s.province }}</span>
        </div>
      </div>
    </div>

    <div v-if="filteredStores.length === 0" class="empty-state">
      <p>暂无匹配的门店</p>
    </div>
  </div>

  <!-- 添加门店弹窗 -->
  <Teleport to="body">
    <div class="modal-overlay" v-if="showAddModal" @click.self="closeAddModal">
      <div class="modal-content">
        <div class="modal-header">
          <h2>添加门店</h2>
          <button class="close-btn" @click="closeAddModal">&times;</button>
        </div>
        <div class="modal-body">
          <div class="form-row">
            <div class="form-group">
              <label>门店名称</label>
              <input v-model="newStore.name" type="text" placeholder="如：北京首都机场店" />
            </div>
          </div>
          <div class="form-row">
            <div class="form-group">
              <label>省份</label>
              <select v-model="newStore.province">
                <option value="">请选择省份</option>
                <option v-for="p in provinces" :key="p" :value="p">{{ p }}</option>
              </select>
            </div>
            <div class="form-group">
              <label>城市</label>
              <input v-model="newStore.city" type="text" placeholder="如：北京" />
            </div>
          </div>
          <div class="form-row">
            <div class="form-group full">
              <label>详细地址</label>
              <input v-model="newStore.address" type="text" placeholder="如：北京市顺义区首都机场T3航站楼" />
            </div>
          </div>
          <div class="form-row">
            <div class="form-group">
              <label>联系电话</label>
              <input v-model="newStore.phone" type="text" placeholder="如：010-88881001" />
            </div>
            <div class="form-group">
              <label>营业时间</label>
              <input v-model="newStore.hours" type="text" placeholder="如：08:00-21:00" />
            </div>
          </div>
          <div class="form-row">
            <div class="form-group">
              <label>纬度</label>
              <input v-model.number="newStore.lat" type="number" step="0.0001" />
            </div>
            <div class="form-group">
              <label>经度</label>
              <input v-model.number="newStore.lng" type="number" step="0.0001" />
            </div>
          </div>
          <div class="map-section">
            <label>在地图上选择位置（点击地图或拖动标记）</label>
            <div id="add-store-map" class="map-container"></div>
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn-cancel" @click="closeAddModal">取消</button>
          <button class="btn-confirm" @click="handleAddStore">确认添加</button>
        </div>
      </div>
    </div>
  </Teleport>
</template>

<style scoped>
.admin-stores { max-width: 1200px; }

.page-header {
  display: flex; justify-content: space-between; align-items: flex-start;
  margin-bottom: 24px;
}
.page-header h1 { font-size: 24px; font-weight: 800; color: #0f172a; }
.page-header p { font-size: 14px; color: #94a3b8; margin-top: 4px; }

.btn-add {
  display: flex; align-items: center; gap: 6px;
  padding: 10px 20px; border-radius: 10px;
  background: linear-gradient(135deg, #0891b2, #06b6d4);
  color: #fff; font-size: 14px; font-weight: 600;
  border: none; cursor: pointer; transition: 0.2s;
}
.btn-add:hover { box-shadow: 0 4px 12px rgba(8,145,178,0.3); }

.filters { display: flex; gap: 12px; margin-bottom: 24px; }
.search-input {
  flex: 1; padding: 10px 16px; border: 1.5px solid #e2e8f0;
  border-radius: 10px; font-size: 14px; outline: none;
}
.search-input:focus { border-color: #0891b2; }
.filter-select {
  padding: 10px 16px; border: 1.5px solid #e2e8f0;
  border-radius: 10px; font-size: 14px; outline: none; background: #fff;
}

.store-grid {
  display: grid; grid-template-columns: repeat(3, 1fr); gap: 16px;
}

.store-card {
  background: #fff; border-radius: 14px; padding: 20px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.04); transition: 0.2s;
}
.store-card:hover { box-shadow: 0 4px 16px rgba(0,0,0,0.08); transform: translateY(-2px); }

.store-header {
  display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px;
}
.store-status {
  font-size: 11px; font-weight: 600; padding: 4px 10px; border-radius: 6px;
}
.store-status.active { color: #16a34a; background: #f0fdf4; }
.store-status.closed { color: #ef4444; background: #fef2f2; }

.status-select {
  border: 1px solid #e2e8f0; padding: 4px 8px; border-radius: 6px;
  font-size: 11px; outline: none; background: #fff;
}

.store-name {
  font-size: 16px; font-weight: 700; color: #0f172a; margin-bottom: 12px;
}

.store-info { display: flex; flex-direction: column; gap: 8px; margin-bottom: 14px; }
.info-item {
  display: flex; align-items: flex-start; gap: 8px;
  font-size: 13px; color: #64748b; line-height: 1.4;
}
.info-item svg { flex-shrink: 0; margin-top: 2px; color: #94a3b8; }

.store-footer {
  display: flex; justify-content: space-between; align-items: center;
  padding-top: 12px; border-top: 1px solid #f1f5f9;
}
.car-count { font-size: 13px; color: #64748b; }
.province-tag {
  font-size: 11px; font-weight: 600; padding: 3px 8px;
  border-radius: 4px; background: #f1f5f9; color: #64748b;
}

.empty-state {
  text-align: center; padding: 60px; color: #94a3b8;
}

/* 弹窗样式 */
.modal-overlay {
  position: fixed; inset: 0; background: rgba(0,0,0,0.5);
  display: flex; align-items: center; justify-content: center;
  z-index: 1000; backdrop-filter: blur(4px);
}
.modal-content {
  background: #fff; border-radius: 16px; width: 600px; max-width: 90vw;
  max-height: 90vh; overflow-y: auto;
}
.modal-header {
  display: flex; justify-content: space-between; align-items: center;
  padding: 20px 24px; border-bottom: 1px solid #f1f5f9;
}
.modal-header h2 { font-size: 18px; font-weight: 700; }
.close-btn { background: none; border: none; font-size: 24px; color: #94a3b8; cursor: pointer; }

.modal-body { padding: 24px; }
.form-row { display: flex; gap: 16px; margin-bottom: 16px; }
.form-group { flex: 1; }
.form-group.full { flex: 2; }
.form-group label {
  display: block; font-size: 13px; font-weight: 600; color: #475569; margin-bottom: 6px;
}
.form-group input, .form-group select {
  width: 100%; padding: 10px 12px; border: 1.5px solid #e2e8f0;
  border-radius: 8px; font-size: 14px; outline: none;
}
.form-group input:focus, .form-group select:focus { border-color: #0891b2; }

.map-section { margin-top: 16px; }
.map-section label {
  display: block; font-size: 13px; font-weight: 600; color: #475569; margin-bottom: 8px;
}
.map-container {
  width: 100%; height: 300px; border-radius: 10px;
  border: 1.5px solid #e2e8f0; overflow: hidden;
}

.modal-footer {
  display: flex; justify-content: flex-end; gap: 12px;
  padding: 16px 24px; border-top: 1px solid #f1f5f9;
}
.btn-cancel {
  padding: 10px 20px; border-radius: 8px; background: #f1f5f9;
  color: #475569; font-size: 14px; font-weight: 500; cursor: pointer; border: none;
}
.btn-confirm {
  padding: 10px 20px; border-radius: 8px; background: #0891b2;
  color: #fff; font-size: 14px; font-weight: 600; cursor: pointer; border: none;
}
.btn-confirm:hover { background: #0e7490; }
</style>
