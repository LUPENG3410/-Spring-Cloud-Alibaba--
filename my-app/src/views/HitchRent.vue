<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useApi } from '@/api/service'
import { useCarStore } from '@/stores/car'
import type { Car } from '@/types'

const router = useRouter()
const carStore = useCarStore()
const api = useApi()

const allCars = ref<Car[]>([])
const loading = ref(true)
const selectedProvince = ref('all')

const provinces = computed(() => {
  const set = new Set(allCars.value.map(c => c.province).filter(Boolean))
  return Array.from(set).sort()
})

// 顺风车：当前省份不是原籍的车（需要开回去的车）
const hitchCars = computed(() => {
  return allCars.value.filter(car => {
    const matchProvince = selectedProvince.value === 'all' || car.currentProvince === selectedProvince.value
    return car.isHitch && matchProvince
  })
})

const HITCH_DISCOUNT = 20

onMounted(async () => {
  try {
    const res = await api.getHitchCars()
    allCars.value = res
  } catch (e) {
    console.warn('加载顺风车失败:', e)
    // 如果接口不存在，从车辆列表中筛选
    if (carStore.cars.length === 0) {
      await carStore.loadCars()
    }
    allCars.value = carStore.cars.filter(c => (c as any).isHitch)
  } finally {
    loading.value = false
  }
})

function getDiscountedPrice(car: Car): number {
  return Math.max(0, (car.rentalPrice || 0) - HITCH_DISCOUNT)
}

function goToBooking(car: Car) {
  router.push(`/booking/${car.id}/${car.currentProvince || car.province || ''}`)
}
</script>

<template>
  <div class="hitch-page">
    <!-- Banner -->
    <div class="page-banner">
      <div class="banner-bg"></div>
      <div class="banner-content">
        <span class="badge">省钱好选择</span>
        <h1>顺风租车</h1>
        <p>帮忙把异地归还的车开回原籍，每天立省 ¥{{ HITCH_DISCOUNT }}</p>
        <div class="banner-stats">
          <div class="stat">
            <span class="stat-num">¥{{ HITCH_DISCOUNT }}</span>
            <span class="stat-label">每天立省</span>
          </div>
          <div class="stat-divider"></div>
          <div class="stat">
            <span class="stat-num">{{ hitchCars.length }}</span>
            <span class="stat-label">顺风车源</span>
          </div>
        </div>
      </div>
    </div>

    <div class="page-body">
      <!-- 筛选 -->
      <div class="filter-bar">
        <div class="filter-group">
          <label>当前所在省份</label>
          <div class="pills">
            <button :class="['pill', { active: selectedProvince === 'all' }]" @click="selectedProvince = 'all'">全部</button>
            <button v-for="p in provinces" :key="p" :class="['pill', { active: selectedProvince === p }]" @click="selectedProvince = p">{{ p }}</button>
          </div>
        </div>
      </div>

      <!-- 说明卡片 -->
      <div class="how-it-works">
        <h3>顺风租车怎么玩？</h3>
        <div class="steps">
          <div class="step">
            <div class="step-icon">🚗</div>
            <div class="step-text">
              <h4>选车</h4>
              <p>选择一辆需要回原籍的顺风车</p>
            </div>
          </div>
          <div class="step-arrow">→</div>
          <div class="step">
            <div class="step-icon">🗺️</div>
            <div class="step-text">
              <h4>出发</h4>
              <p>从当前所在城市取车</p>
            </div>
          </div>
          <div class="step-arrow">→</div>
          <div class="step">
            <div class="step-icon">🏠</div>
            <div class="step-text">
              <h4>回原籍</h4>
              <p>开到车辆原籍城市还车</p>
            </div>
          </div>
          <div class="step-arrow">→</div>
          <div class="step">
            <div class="step-icon">💰</div>
            <div class="step-text">
              <h4>省钱</h4>
              <p>每天租金立减 ¥{{ HITCH_DISCOUNT }}</p>
            </div>
          </div>
        </div>
      </div>

      <!-- 车辆列表 -->
      <div v-if="loading" class="loading-state">
        <div class="spinner"></div>
        <p>正在搜索顺风车源...</p>
      </div>

      <div v-else-if="hitchCars.length === 0" class="empty-state">
        <svg width="64" height="64" viewBox="0 0 24 24" fill="none" stroke="#cbd5e1" stroke-width="1.5">
          <circle cx="11" cy="11" r="8"/><path d="m21 21-4.35-4.35"/>
        </svg>
        <h3>暂无顺风车源</h3>
        <p>当前没有需要回原籍的车辆，稍后再来看看吧</p>
      </div>

      <div v-else class="car-grid">
        <div v-for="car in hitchCars" :key="`${car.id}-${car.currentProvince}`" class="hitch-card" @click="goToBooking(car)">
          <div class="card-img">
            <img :src="car.image" :alt="car.name" />
            <div class="discount-tag">省¥{{ HITCH_DISCOUNT }}/天</div>
            <div class="route-tag">
              <span>{{ car.currentProvince }}</span>
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M5 12h14"/><path d="m12 5 7 7-7 7"/></svg>
              <span>{{ car.homeProvince }}</span>
            </div>
          </div>
          <div class="card-body">
            <div class="card-top">
              <h3>{{ car.name }}</h3>
              <span class="brand">{{ car.brand }}</span>
            </div>
            <div class="specs">
              <span>{{ car.seats }}座</span>
              <span>{{ car.transmission }}</span>
              <span>{{ car.fuel }}</span>
            </div>
            <div class="card-bottom">
              <div class="price">
                <span class="original">¥{{ car.rentalPrice }}/天</span>
                <span class="discounted">¥{{ getDiscountedPrice(car) }}/天</span>
              </div>
              <button class="book-btn">立即预订</button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.hitch-page { min-height: 100vh; }

.page-banner {
  position: relative; overflow: hidden;
  padding: 80px 32px 60px;
}
.banner-bg {
  position: absolute; inset: 0;
  background: linear-gradient(135deg, #0891b2 0%, #06b6d4 50%, #22d3ee 100%);
}
.banner-bg::before {
  content: ''; position: absolute; inset: 0;
  background: url("data:image/svg+xml,%3Csvg width='60' height='60' viewBox='0 0 60 60' xmlns='http://www.w3.org/2000/svg'%3E%3Cg fill='none' fill-rule='evenodd'%3E%3Cg fill='%23ffffff' fill-opacity='0.05'%3E%3Cpath d='M36 34v-4h-2v4h-4v2h4v4h2v-4h4v-2h-4zm0-30V0h-2v4h-4v2h4v4h2V6h4V4h-4zM6 34v-4H4v4H0v2h4v4h2v-4h4v-2H6zM6 4V0H4v4H0v2h4v4h2V6h4V4H6z'/%3E%3C/g%3E%3C/g%3E%3C/svg%3E");
}
.banner-content { position: relative; max-width: 1280px; margin: 0 auto; color: #fff; }
.badge {
  display: inline-block; padding: 6px 16px;
  background: rgba(255,255,255,0.2); border-radius: 100px;
  font-size: 13px; font-weight: 600; margin-bottom: 16px;
  backdrop-filter: blur(8px);
}
.banner-content h1 { font-size: 42px; font-weight: 900; margin-bottom: 12px; }
.banner-content > p { font-size: 18px; opacity: 0.9; margin-bottom: 32px; }
.banner-stats { display: flex; align-items: center; gap: 32px; }
.stat-num { display: block; font-size: 32px; font-weight: 900; }
.stat-label { font-size: 13px; opacity: 0.8; }
.stat-divider { width: 1px; height: 40px; background: rgba(255,255,255,0.3); }

.page-body { max-width: 1280px; margin: 0 auto; padding: 32px; }

.filter-bar {
  background: #fff; border-radius: 16px;
  padding: 20px 24px; margin-bottom: 32px;
  border: 1px solid #f1f5f9;
}
.filter-group label { display: block; font-size: 12px; font-weight: 600; color: #94a3b8; margin-bottom: 10px; }
.pills { display: flex; gap: 6px; flex-wrap: wrap; }
.pill {
  padding: 7px 14px; border: 1px solid #e2e8f0; background: #fff;
  border-radius: 100px; font-size: 12px; font-weight: 500;
  color: #475569; cursor: pointer; transition: 0.15s;
}
.pill:hover { border-color: #0891b2; color: #0891b2; }
.pill.active { background: linear-gradient(135deg, #0891b2, #06b6d4); color: #fff; border-color: transparent; }

.how-it-works {
  background: #fff; border-radius: 16px;
  padding: 28px; margin-bottom: 32px;
  border: 1px solid #f1f5f9;
}
.how-it-works h3 { font-size: 16px; font-weight: 700; color: #0f172a; margin-bottom: 20px; }
.steps { display: flex; align-items: center; gap: 12px; justify-content: center; }
.step { display: flex; align-items: center; gap: 12px; }
.step-icon { font-size: 28px; }
.step-text h4 { font-size: 14px; font-weight: 700; color: #0f172a; }
.step-text p { font-size: 12px; color: #64748b; }
.step-arrow { color: #cbd5e1; font-size: 20px; }

.loading-state { text-align: center; padding: 80px 20px; }
.spinner {
  width: 40px; height: 40px; border: 3px solid #e2e8f0;
  border-top-color: #0891b2; border-radius: 50%;
  animation: spin 0.8s linear infinite; margin: 0 auto 16px;
}
@keyframes spin { to { transform: rotate(360deg); } }
.loading-state p { color: #94a3b8; font-size: 14px; }

.empty-state { text-align: center; padding: 80px 20px; }
.empty-state h3 { font-size: 18px; font-weight: 700; color: #334155; margin: 16px 0 8px; }
.empty-state p { color: #94a3b8; }

.car-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 20px; }
.hitch-card {
  background: #fff; border-radius: 16px; overflow: hidden;
  border: 1px solid #f1f5f9; cursor: pointer;
  transition: all 0.25s cubic-bezier(0.4,0,0.2,1);
}
.hitch-card:hover { transform: translateY(-6px); box-shadow: 0 20px 40px rgba(0,0,0,0.1); }
.card-img { position: relative; height: 180px; overflow: hidden; background: #f8fafc; }
.card-img img { width: 100%; height: 100%; object-fit: cover; }
.discount-tag {
  position: absolute; top: 12px; left: 12px;
  background: linear-gradient(135deg, #ef4444, #f97316);
  color: #fff; padding: 5px 12px; border-radius: 8px;
  font-size: 12px; font-weight: 700;
}
.route-tag {
  position: absolute; bottom: 12px; left: 12px;
  background: rgba(0,0,0,0.7); color: #fff;
  padding: 6px 12px; border-radius: 8px;
  font-size: 12px; font-weight: 500;
  display: flex; align-items: center; gap: 6px;
  backdrop-filter: blur(8px);
}
.card-body { padding: 16px 18px 18px; }
.card-top { display: flex; align-items: baseline; gap: 8px; margin-bottom: 10px; }
.card-top h3 { font-size: 16px; font-weight: 700; color: #0f172a; }
.brand { font-size: 12px; color: #94a3b8; }
.specs { display: flex; gap: 10px; margin-bottom: 14px; }
.specs span { font-size: 11px; color: #64748b; font-weight: 500; background: #f8fafc; padding: 4px 8px; border-radius: 4px; }
.card-bottom { display: flex; align-items: center; justify-content: space-between; }
.price { display: flex; flex-direction: column; }
.original { font-size: 12px; color: #94a3b8; text-decoration: line-through; }
.discounted { font-size: 20px; font-weight: 800; color: #ef4444; }
.book-btn {
  background: linear-gradient(135deg, #0891b2, #06b6d4);
  color: #fff; border: none; padding: 8px 20px; border-radius: 8px;
  font-size: 12px; font-weight: 600; cursor: pointer; transition: 0.2s;
}
.book-btn:hover { transform: scale(1.05); box-shadow: 0 4px 12px rgba(8,145,178,0.35); }

@media (max-width: 768px) {
  .car-grid { grid-template-columns: 1fr; }
  .steps { flex-wrap: wrap; justify-content: center; }
  .step-arrow { display: none; }
  .banner-content h1 { font-size: 28px; }
}
</style>
