<script setup lang="ts">
import { ref, computed, onMounted, nextTick, watch } from 'vue'
import { useStoreStore } from '@/stores/store'

const storeStore = useStoreStore()
const selectedProvince = ref('all')
const activeStore = ref<number | null>(null)
const showAllProvinces = ref(false)

const provinceList = computed(() => {
  const provinces = new Set(storeStore.stores.filter(s => s.status === 'active').map(s => s.province))
  return ['all', ...Array.from(provinces).sort()]
})

const provinceCount = computed(() => {
  const counts: Record<string, number> = {}
  storeStore.stores.filter(s => s.status === 'active').forEach(s => {
    counts[s.province] = (counts[s.province] || 0) + 1
  })
  return counts
})

function filterByProvince(p: string) {
  selectedProvince.value = p
  storeStore.filterStores(p)
  activeStore.value = null
}

watch(() => storeStore.filteredStores.length, () => {
  updateMarkers()
})

function selectStore(id: number) {
  activeStore.value = id
  const store = storeStore.getStoreById(id)
  if (store && mapInstance) {
    mapInstance.setView([store.lat, store.lng], 14)
  }
}

// eslint-disable-next-line @typescript-eslint/no-explicit-any
let mapInstance: any = null
// eslint-disable-next-line @typescript-eslint/no-explicit-any
let markersList: any[] = []
// eslint-disable-next-line @typescript-eslint/no-explicit-any
let leafletLib: any = null

function updateMarkers() {
  if (!leafletLib || !mapInstance) return

  markersList.forEach(m => m.remove())
  markersList = []

  const orangeIcon = leafletLib.divIcon({
    className: 'custom-marker',
    html: `<div style="width:36px;height:36px;background:linear-gradient(135deg,#0891b2,#06b6d4);border-radius:50% 50% 50% 0;transform:rotate(-45deg);border:3px solid #fff;box-shadow:0 4px 12px rgba(0,0,0,0.3);display:flex;align-items:center;justify-content:center"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="#fff" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round" style="transform:rotate(45deg)"><path d="M19 17h2c.6 0 1-.4 1-1v-3c0-.9-.7-1.7-1.5-1.9C18.7 10.6 16 10 16 10s-1.3-1.4-2.2-2.3c-.5-.4-1.1-.7-1.8-.7H5c-.6 0-1.1.4-1.4.9l-1.4 2.9A3.7 3.7 0 0 0 2 12v4c0 .6.4 1 1 1h2"/><circle cx="7" cy="17" r="2"/><circle cx="17" cy="17" r="2"/></svg></div>`,
    iconSize: [36, 36],
    iconAnchor: [18, 36],
    popupAnchor: [0, -36]
  })

  storeStore.filteredStores.forEach(s => {
    const marker = leafletLib.marker([s.lat, s.lng], { icon: orangeIcon })
      .addTo(mapInstance)
      .bindPopup(`
        <div style="font-family:-apple-system,'PingFang SC','Microsoft YaHei',sans-serif;min-width:220px;padding:2px">
          <div style="font-weight:700;font-size:15px;color:#1a1a2e;margin-bottom:8px;padding-bottom:8px;border-bottom:1px solid #f1f5f9">${s.name}</div>
          <div style="display:flex;align-items:flex-start;gap:6px;margin-bottom:6px">
            <span style="color:#94a3b8;font-size:13px">📍</span>
            <span style="font-size:13px;color:#475569;line-height:1.5">${s.address}</span>
          </div>
          <div style="display:flex;align-items:center;gap:6px;margin-bottom:6px">
            <span style="color:#94a3b8;font-size:13px">📞</span>
            <span style="font-size:13px;color:#475569">${s.phone}</span>
          </div>
          <div style="display:flex;align-items:center;gap:6px">
            <span style="color:#94a3b8;font-size:13px">🕐</span>
            <span style="font-size:13px;color:#475569">${s.hours}</span>
          </div>
        </div>
      `, { className: 'custom-popup', maxWidth: 300 })
    markersList.push(marker)
  })

  if (storeStore.filteredStores.length > 0) {
    const bounds = leafletLib.latLngBounds(
      storeStore.filteredStores.map((s: { lat: number; lng: number }) => [s.lat, s.lng])
    )
    mapInstance.fitBounds(bounds, { padding: [50, 50], maxZoom: 13 })
  }
}

onMounted(async () => {
  await nextTick()
  const L = await import('leaflet')
  leafletLib = L

  mapInstance = L.map('map-container', {
    center: [35.8617, 104.1954],
    zoom: 4,
    zoomControl: false,
    scrollWheelZoom: true
  })

  L.control.zoom({ position: 'bottomright' }).addTo(mapInstance)

  // 高德地图瓦片图层
  L.tileLayer('https://webrd0{s}.is.autonavi.com/appmaptile?lang=zh_cn&size=1&scale=1&style=8&x={x}&y={y}&z={z}', {
    subdomains: '1234',
    maxZoom: 18,
    attribution: '&copy; 高德地图'
  }).addTo(mapInstance)

  updateMarkers()

  setTimeout(() => { mapInstance.invalidateSize() }, 300)
})
</script>

<template>
  <div class="stores-page">
    <div class="page-header">
      <h1>网点查询</h1>
      <p>全国{{ storeStore.stores.filter(s => s.status === 'active').length }}家门店，覆盖{{ provinceList.length - 1 }}个省份，为您提供便捷的取还车服务</p>
    </div>

    <div class="stores-layout">
      <div class="sidebar">
        <div class="filter-section">
          <h3>选择省份</h3>
          <div class="province-list" :class="{ collapsed: !showAllProvinces }">
            <button
              v-for="(p, i) in provinceList"
              :key="p"
              v-show="i < 6 || showAllProvinces"
              :class="['prov-btn', { active: selectedProvince === p }]"
              @click="filterByProvince(p)"
            >
              {{ p === 'all' ? '全部省份' : p }}
              <span class="count">{{ p === 'all' ? storeStore.stores.filter(s => s.status === 'active').length : (provinceCount[p] || 0) }}</span>
            </button>
          </div>
          <button class="toggle-btn" @click="showAllProvinces = !showAllProvinces" v-if="provinceList.length > 6">
            {{ showAllProvinces ? '收起' : '展开全部' }}
            <svg :class="['toggle-arrow', { rotated: showAllProvinces }]" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="m6 9 6 6 6-6"/></svg>
          </button>
        </div>

        <div class="store-list">
          <h3>门店列表 <span class="total">({{ storeStore.filteredStores.length }}家)</span></h3>
          <div
            v-for="store in storeStore.filteredStores"
            :key="store.id"
            :class="['store-card', { active: activeStore === store.id }]"
            @click="selectStore(store.id)"
          >
            <div class="store-name">{{ store.name }}</div>
            <div class="store-addr">{{ store.address }}</div>
            <div class="store-meta">
              <span>📞 {{ store.phone }}</span>
              <span>🕐 {{ store.hours }}</span>
            </div>
          </div>
        </div>
      </div>

      <div class="map-panel">
        <div id="map-container"></div>
        <div class="map-tip">
          <span>数据来源：高德地图</span>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.stores-page { max-width: 1280px; margin: 0 auto; padding: 0 32px 80px; }
.page-header { padding: 48px 0 32px; }
.page-header h1 { font-size: 32px; font-weight: 800; color: #1a1a2e; margin-bottom: 6px; }
.page-header p { color: #94a3b8; font-size: 15px; }

.stores-layout { display: grid; grid-template-columns: 400px 1fr; gap: 24px; height: 640px; }

.sidebar {
  display: flex; flex-direction: column;
  background: #fff; border-radius: 16px;
  border: 1px solid #f1f5f9; overflow: hidden;
}

.filter-section { padding: 20px; border-bottom: 1px solid #f1f5f9; }
.filter-section h3 { font-size: 14px; font-weight: 700; color: #1a1a2e; margin-bottom: 12px; }
.province-list { display: flex; flex-wrap: wrap; gap: 6px; }
.province-list.collapsed { max-height: 38px; overflow: hidden; }
.toggle-btn {
  display: inline-flex; align-items: center; gap: 4px;
  margin-top: 10px; padding: 4px 0;
  background: none; border: none; font-size: 12px;
  font-weight: 600; color: #0891b2; cursor: pointer;
}
.toggle-btn:hover { opacity: 0.8; }
.toggle-arrow { transition: transform 0.2s; }
.toggle-arrow.rotated { transform: rotate(180deg); }
.prov-btn {
  display: flex; align-items: center; gap: 6px;
  padding: 7px 14px; border: 1px solid #e2e8f0;
  background: #fff; border-radius: 100px;
  font-size: 12px; font-weight: 500; color: #475569;
  cursor: pointer; transition: 0.15s;
}
.prov-btn:hover { border-color: #0891b2; color: #0891b2; }
.prov-btn.active {
  background: linear-gradient(135deg, #0891b2, #06b6d4);
  color: #fff; border-color: transparent;
}
.prov-btn .count {
  background: rgba(0,0,0,0.06); padding: 1px 6px; border-radius: 10px; font-size: 11px;
}
.prov-btn.active .count { background: rgba(255,255,255,0.2); }

.store-list { flex: 1; overflow-y: auto; padding: 16px; }
.store-list h3 { font-size: 13px; font-weight: 600; color: #64748b; margin-bottom: 12px; }
.total { font-weight: 400; color: #94a3b8; }

.store-card {
  padding: 14px; border-radius: 10px;
  border: 1px solid #f1f5f9;
  margin-bottom: 8px; cursor: pointer;
  transition: all 0.15s;
}
.store-card:hover { border-color: #e2e8f0; background: #f8fafc; }
.store-card.active { border-color: #0891b2; background: #f0fdfa; }
.store-name { font-size: 14px; font-weight: 700; color: #1a1a2e; margin-bottom: 4px; }
.store-addr { font-size: 12px; color: #94a3b8; margin-bottom: 8px; line-height: 1.4; }
.store-meta { display: flex; gap: 16px; font-size: 11px; color: #64748b; }

.map-panel {
  border-radius: 16px; overflow: hidden;
  border: 1px solid #f1f5f9; position: relative;
}
#map-container { width: 100%; height: 100%; background: #e8eaed; }
.map-tip {
  position: absolute; bottom: 12px; left: 12px;
  background: rgba(255,255,255,0.9);
  backdrop-filter: blur(8px);
  padding: 4px 10px; border-radius: 6px;
  font-size: 11px; color: #94a3b8;
  z-index: 500;
}
</style>

<style>
.custom-marker { background: none !important; border: none !important; }
.custom-popup .leaflet-popup-content-wrapper {
  border-radius: 14px;
  box-shadow: 0 10px 40px rgba(0,0,0,0.15);
  padding: 0;
  border: 1px solid #f1f5f9;
}
.custom-popup .leaflet-popup-content {
  margin: 14px 18px;
  font-family: -apple-system, 'PingFang SC', 'Microsoft YaHei', sans-serif;
}
.custom-popup .leaflet-popup-tip {
  box-shadow: 0 4px 12px rgba(0,0,0,0.08);
  border: 1px solid #f1f5f9;
}
.leaflet-control-zoom a {
  width: 36px !important;
  height: 36px !important;
  line-height: 36px !important;
  font-size: 18px !important;
  border-radius: 8px !important;
  border: none !important;
  box-shadow: 0 2px 8px rgba(0,0,0,0.12) !important;
  color: #475569 !important;
}
.leaflet-control-zoom a:hover {
  background: #f8fafc !important;
  color: #1a1a2e !important;
}
.leaflet-control-zoom {
  border: none !important;
  box-shadow: none !important;
}
</style>
