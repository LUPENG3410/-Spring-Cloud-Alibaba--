<script setup lang="ts">
import { watch, computed, onMounted } from 'vue'
import { useCarStore } from '@/stores/car'
import CarCard from '@/components/CarCard.vue'

const carStore = useCarStore()
onMounted(() => { carStore.loadCars() })

const types = [
  { value: 'all', label: '全部' },
  { value: 'sedan', label: '轿车' },
  { value: 'suv', label: 'SUV' },
  { value: 'mpv', label: 'MPV' },
  { value: 'luxury', label: '豪华' },
  { value: 'sports', label: '跑车' }
]

watch(
  () => [carStore.searchQuery, carStore.selectedType, carStore.selectedProvince],
  () => carStore.resetPage()
)

const visiblePages = computed(() => {
  const total = carStore.totalPages
  const cur = carStore.currentPage
  if (total <= 7) return Array.from({ length: total }, (_, i) => i + 1)
  const pages: (number | string)[] = [1]
  if (cur > 3) pages.push('...')
  for (let i = Math.max(2, cur - 1); i <= Math.min(total - 1, cur + 1); i++) {
    pages.push(i)
  }
  if (cur < total - 2) pages.push('...')
  pages.push(total)
  return pages
})

function goTo(page: number) {
  if (page < 1 || page > carStore.totalPages) return
  carStore.currentPage = page
  window.scrollTo({ top: 0, behavior: 'smooth' })
}
</script>

<template>
  <div class="car-list-page">
    <div class="page-head">
      <h1>全部车辆</h1>
      <p>共 <strong>{{ carStore.filteredCars.length }}</strong> 辆可用车辆</p>
    </div>

    <div class="filter-bar">
      <div class="search-wrap">
        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="#94a3b8" stroke-width="2"><circle cx="11" cy="11" r="8"/><path d="m21 21-4.35-4.35"/></svg>
        <input v-model="carStore.searchQuery" type="text" placeholder="搜索车型名称或品牌..." />
      </div>
      <div class="filter-row">
        <div class="filter-group">
          <label>车型</label>
          <div class="pills">
            <button v-for="t in types" :key="t.value" :class="['pill', { active: carStore.selectedType === t.value }]" @click="carStore.selectedType = t.value">{{ t.label }}</button>
          </div>
        </div>
        <div class="filter-group">
          <label>省份</label>
          <div class="pills">
            <button :class="['pill', { active: carStore.selectedProvince === 'all' }]" @click="carStore.selectedProvince = 'all'">全部</button>
            <button v-for="p in carStore.provinces" :key="p" :class="['pill', { active: carStore.selectedProvince === p }]" @click="carStore.selectedProvince = p">{{ p }}</button>
          </div>
        </div>
      </div>
    </div>

    <div class="car-grid" v-if="carStore.paginatedCars.length > 0">
      <CarCard v-for="car in carStore.paginatedCars" :key="car.id" :car="car" />
    </div>

    <div class="empty-state" v-else>
      <svg width="56" height="56" viewBox="0 0 24 24" fill="none" stroke="#cbd5e1" stroke-width="1.5"><circle cx="11" cy="11" r="8"/><path d="m21 21-4.35-4.35"/></svg>
      <h3>没有找到匹配的车辆</h3>
      <p>试试调整筛选条件</p>
      <button @click="carStore.searchQuery = ''; carStore.selectedType = 'all'; carStore.selectedProvince = 'all'">清除筛选</button>
    </div>

    <div class="pagination" v-if="carStore.totalPages > 1">
      <button class="page-btn" :disabled="carStore.currentPage <= 1" @click="goTo(carStore.currentPage - 1)">
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="m15 18-6-6 6-6"/></svg>
      </button>
      <template v-for="p in visiblePages" :key="p">
        <span v-if="p === '...'" class="page-dots">...</span>
        <button v-else :class="['page-btn', { active: p === carStore.currentPage }]" @click="goTo(p as number)">{{ p }}</button>
      </template>
      <button class="page-btn" :disabled="carStore.currentPage >= carStore.totalPages" @click="goTo(carStore.currentPage + 1)">
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="m9 18 6-6-6-6"/></svg>
      </button>
    </div>
  </div>
</template>

<style scoped>
.car-list-page { max-width: 1280px; margin: 0 auto; padding: 0 32px 80px; }
.page-head { padding: 48px 0 32px; }
.page-head h1 { font-size: 32px; font-weight: 800; color: #1a1a2e; margin-bottom: 6px; }
.page-head p { color: #94a3b8; font-size: 15px; }
.page-head strong { color: #0891b2; font-weight: 700; }

.filter-bar {
  background: #fff; border-radius: 16px;
  padding: 24px; margin-bottom: 32px;
  border: 1px solid #f1f5f9;
}
.search-wrap {
  display: flex; align-items: center; gap: 12px;
  padding: 12px 16px; background: #f8fafc;
  border-radius: 10px; margin-bottom: 20px;
  transition: 0.15s; border: 2px solid transparent;
}
.search-wrap:focus-within { background: #fff; border-color: #0891b2; box-shadow: 0 0 0 3px rgba(8,145,178,0.1); }
.search-wrap input { flex: 1; border: none; outline: none; font-size: 14px; color: #1a1a2e; background: transparent; }
.search-wrap input::placeholder { color: #94a3b8; }

.filter-row { display: flex; gap: 24px; }
.filter-group { display: flex; align-items: center; gap: 10px; }
.filter-group label { font-size: 12px; font-weight: 600; color: #94a3b8; white-space: nowrap; }
.pills { display: flex; gap: 6px; flex-wrap: wrap; }
.pill {
  padding: 7px 14px; border: 1px solid #e2e8f0; background: #fff;
  border-radius: 100px; font-size: 12px; font-weight: 500;
  color: #475569; cursor: pointer; transition: 0.15s;
}
.pill:hover { border-color: #0891b2; color: #0891b2; }
.pill.active { background: linear-gradient(135deg, #0891b2, #06b6d4); color: #fff; border-color: transparent; }

.car-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 20px; }
.empty-state { text-align: center; padding: 100px 20px; }
.empty-state h3 { font-size: 18px; font-weight: 700; color: #334155; margin: 16px 0 8px; }
.empty-state p { color: #94a3b8; margin-bottom: 24px; }
.empty-state button {
  background: linear-gradient(135deg, #0891b2, #06b6d4);
  color: #fff; border: none; padding: 10px 24px;
  border-radius: 10px; font-size: 13px; font-weight: 600; cursor: pointer;
}

.pagination {
  display: flex; justify-content: center; align-items: center; gap: 6px;
  margin-top: 40px; padding: 20px 0;
}
.page-btn {
  display: inline-flex; align-items: center; justify-content: center;
  min-width: 36px; height: 36px; padding: 0 8px;
  border: 1px solid #e2e8f0; background: #fff; border-radius: 8px;
  font-size: 13px; font-weight: 500; color: #475569;
  cursor: pointer; transition: 0.15s;
}
.page-btn:hover:not(:disabled):not(.active) { border-color: #0891b2; color: #0891b2; }
.page-btn.active {
  background: linear-gradient(135deg, #0891b2, #06b6d4);
  color: #fff; border-color: transparent;
}
.page-btn:disabled { opacity: 0.4; cursor: not-allowed; }
.page-dots { color: #94a3b8; font-size: 13px; padding: 0 4px; }
</style>
