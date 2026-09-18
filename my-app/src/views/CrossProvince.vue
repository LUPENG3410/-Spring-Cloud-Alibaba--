<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useCarStore } from '@/stores/car'

const router = useRouter()
const carStore = useCarStore()

const pickupProvince = ref('北京')
const returnProvince = ref('上海')
const startDate = ref('')
const endDate = ref('')

const allProvinces = ['北京', '上海', '广东', '四川', '浙江', '江苏', '湖北', '重庆', '宁夏']

const filteredCars = computed(() => {
  return carStore.cars.filter(c => c.province === pickupProvince.value)
})

const totalDays = computed(() => {
  if (!startDate.value || !endDate.value) return 0
  const diff = new Date(endDate.value).getTime() - new Date(startDate.value).getTime()
  return Math.max(0, Math.ceil(diff / (1000 * 60 * 60 * 24)))
})

const crossProvinceFee = computed(() => {
  if (pickupProvince.value === returnProvince.value) return 0
  return totalDays.value * 50
})

function swapProvinces() {
  const temp = pickupProvince.value
  pickupProvince.value = returnProvince.value
  returnProvince.value = temp
}
</script>

<template>
  <div class="cp-page">
    <div class="page-banner">
      <div class="banner-bg"></div>
      <div class="banner-content">
        <span class="badge">灵活出行</span>
        <h1>跨省异地租车</h1>
        <p>取车A省，还车B省，全国门店通还，让旅程更自由</p>
      </div>
    </div>

    <div class="page-body">
      <!-- Search Panel -->
      <div class="search-panel">
        <div class="sp-row">
          <div class="sp-field">
            <label>取车省份</label>
            <select v-model="pickupProvince">
              <option v-for="p in allProvinces" :key="p" :value="p">{{ p }}</option>
            </select>
          </div>
          <button class="swap-btn" @click="swapProvinces">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M7 16V4m0 0L3 8m4-4l4 4M17 8v12m0 0l4-4m-4 4l-4-4"/>
            </svg>
          </button>
          <div class="sp-field">
            <label>还车省份</label>
            <select v-model="returnProvince">
              <option v-for="p in allProvinces" :key="p" :value="p">{{ p }}</option>
            </select>
          </div>
          <div class="sp-field">
            <label>取车时间</label>
            <input type="date" v-model="startDate" :min="new Date().toISOString().split('T')[0]" />
          </div>
          <div class="sp-field">
            <label>还车时间</label>
            <input type="date" v-model="endDate" :min="startDate || new Date().toISOString().split('T')[0]" />
          </div>
        </div>

        <div class="sp-info" v-if="pickupProvince !== returnProvince">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="#f59e0b" stroke-width="2">
            <path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"/>
            <line x1="12" y1="9" x2="12" y2="13"/><line x1="12" y1="17" x2="12.01" y2="17"/>
          </svg>
          <span>跨省还车将收取 <strong>¥50/天</strong> 异地还车费（{{ totalDays }}天 = ¥{{ crossProvinceFee }}）</span>
        </div>
        <div class="sp-info green" v-else-if="totalDays > 0">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="#16a34a" stroke-width="2">
            <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/><path d="m9 11 3 3L22 4"/>
          </svg>
          <span>同省还车，无需额外费用</span>
        </div>
      </div>

      <!-- Route Info -->
      <div class="route-card" v-if="totalDays > 0">
        <div class="route-endpoint">
          <div class="ep-dot start"></div>
          <div>
            <span class="ep-label">取车</span>
            <span class="ep-value">{{ pickupProvince }}</span>
          </div>
        </div>
        <div class="route-line">
          <div class="route-dash"></div>
          <span class="route-days">{{ totalDays }}天</span>
          <div class="route-dash"></div>
        </div>
        <div class="route-endpoint">
          <div class="ep-dot end"></div>
          <div>
            <span class="ep-label">还车</span>
            <span class="ep-value">{{ returnProvince }}</span>
          </div>
        </div>
        <div class="route-fee" v-if="crossProvinceFee > 0">
          异地费 +¥{{ crossProvinceFee }}
        </div>
      </div>

      <!-- Available Cars -->
      <div class="avail-section" v-if="filteredCars.length > 0">
        <h2>{{ pickupProvince }}可用车辆 <span class="count">({{ filteredCars.length }}辆)</span></h2>
        <div class="car-list">
          <div v-for="car in filteredCars" :key="car.id" class="cp-car-card" @click="router.push(`/car/${car.id}`)">
            <img :src="car.image" :alt="car.name" />
            <div class="cp-car-info">
              <h3>{{ car.name }}</h3>
              <p>{{ car.brand }} · {{ car.seats }}座 · {{ car.transmission }}</p>
              <div class="cp-car-price">
                <span class="cp-sym">¥</span><span class="cp-num">{{ car.rentalPrice }}</span><span class="cp-unit">/天</span>
                <span class="cp-total" v-if="totalDays > 0">合计 ¥{{ (car.rentalPrice || 0) * totalDays + crossProvinceFee }}</span>
              </div>
            </div>
            <button class="cp-book-btn">预订</button>
          </div>
        </div>
      </div>

      <div class="empty" v-else-if="totalDays > 0">
        <p>该省份暂无可用车辆</p>
      </div>
    </div>
  </div>
</template>

<style scoped>
.cp-page { min-height: 100vh; }

.page-banner {
  position: relative; padding: 64px 32px 56px;
  overflow: hidden;
}
.banner-bg {
  position: absolute; inset: 0;
  background: linear-gradient(135deg, #1a1a2e, #0f3460, #16213e);
}
.banner-content { position: relative; max-width: 1280px; margin: 0 auto; }
.badge { display: inline-block; background: #ff6b35; color: #fff; padding: 4px 14px; border-radius: 6px; font-size: 12px; font-weight: 700; margin-bottom: 16px; }
.banner-content h1 { font-size: 36px; font-weight: 800; color: #fff; margin-bottom: 10px; }
.banner-content p { color: rgba(255,255,255,0.6); font-size: 15px; }

.page-body { max-width: 1280px; margin: 0 auto; padding: 32px; }

.search-panel {
  background: #fff; border-radius: 16px;
  padding: 28px; margin-bottom: 24px;
  border: 1px solid #f1f5f9;
  box-shadow: 0 2px 8px rgba(0,0,0,0.04);
}
.sp-row { display: flex; gap: 12px; align-items: flex-end; }
.sp-field { flex: 1; }
.sp-field label { display: block; font-size: 12px; font-weight: 600; color: #94a3b8; margin-bottom: 6px; text-transform: uppercase; letter-spacing: 0.5px; }
.sp-field select, .sp-field input {
  width: 100%; padding: 12px 14px; border: 1.5px solid #e2e8f0;
  border-radius: 10px; font-size: 14px; font-weight: 600;
  color: #1a1a2e; outline: none; background: #fff; transition: 0.15s;
}
.sp-field select:focus, .sp-field input:focus {
  border-color: #0891b2; box-shadow: 0 0 0 3px rgba(8,145,178,0.1);
}
.swap-btn {
  width: 44px; height: 44px; border-radius: 10px;
  border: 1.5px solid #e2e8f0; background: #fff;
  display: flex; align-items: center; justify-content: center;
  cursor: pointer; color: #64748b; transition: 0.15s;
  flex-shrink: 0; margin-bottom: 0;
}
.swap-btn:hover { border-color: #0891b2; color: #0891b2; background: #f0fdfa; }

.sp-info {
  display: flex; align-items: center; gap: 10px;
  margin-top: 16px; padding: 12px 16px;
  background: #fffbeb; border-radius: 10px;
  font-size: 13px; color: #92400e;
}
.sp-info.green { background: #f0fdf4; color: #166534; }
.sp-info strong { font-weight: 700; }

.route-card {
  display: flex; align-items: center; justify-content: center;
  gap: 24px; background: #fff; border-radius: 16px;
  padding: 28px; margin-bottom: 32px;
  border: 1px solid #f1f5f9; position: relative;
}
.route-endpoint { display: flex; align-items: center; gap: 12px; }
.ep-dot { width: 14px; height: 14px; border-radius: 50%; }
.ep-dot.start { background: #ff6b35; }
.ep-dot.end { background: #f72585; }
.ep-label { display: block; font-size: 11px; color: #94a3b8; font-weight: 600; text-transform: uppercase; }
.ep-value { font-size: 18px; font-weight: 800; color: #1a1a2e; }
.route-line { display: flex; align-items: center; gap: 12px; }
.route-dash { width: 60px; height: 2px; background: repeating-linear-gradient(90deg, #e2e8f0 0, #e2e8f0 6px, transparent 6px, transparent 12px); }
.route-days {
  background: #f1f5f9; padding: 4px 12px; border-radius: 100px;
  font-size: 12px; font-weight: 700; color: #475569; white-space: nowrap;
}
.route-fee {
  position: absolute; top: -10px; right: 24px;
  background: #fef3c7; color: #92400e;
  padding: 4px 12px; border-radius: 6px;
  font-size: 12px; font-weight: 700;
}

.avail-section h2 { font-size: 20px; font-weight: 700; color: #1a1a2e; margin-bottom: 20px; }
.avail-section .count { font-weight: 400; color: #94a3b8; font-size: 15px; }
.car-list { display: flex; flex-direction: column; gap: 12px; }

.cp-car-card {
  display: flex; align-items: center; gap: 20px;
  background: #fff; border-radius: 14px; padding: 16px 20px;
  border: 1px solid #f1f5f9; cursor: pointer;
  transition: 0.2s;
}
.cp-car-card:hover { border-color: #e2e8f0; box-shadow: 0 4px 16px rgba(0,0,0,0.06); }
.cp-car-card img { width: 160px; height: 100px; object-fit: cover; border-radius: 10px; }
.cp-car-info { flex: 1; }
.cp-car-info h3 { font-size: 16px; font-weight: 700; color: #1a1a2e; margin-bottom: 4px; }
.cp-car-info p { font-size: 13px; color: #94a3b8; margin-bottom: 8px; }
.cp-car-price { display: flex; align-items: baseline; gap: 4px; }
.cp-sym { font-size: 14px; font-weight: 700; color: #ef4444; }
.cp-num { font-size: 22px; font-weight: 800; color: #ef4444; }
.cp-unit { font-size: 12px; color: #94a3b8; }
.cp-total { margin-left: 12px; font-size: 13px; color: #64748b; font-weight: 500; }
.cp-book-btn {
  background: linear-gradient(135deg, #0891b2, #06b6d4);
  color: #fff; border: none; padding: 10px 24px;
  border-radius: 10px; font-size: 13px; font-weight: 600;
  cursor: pointer; transition: 0.2s; white-space: nowrap;
}
.cp-book-btn:hover { transform: scale(1.05); box-shadow: 0 4px 12px rgba(8,145,178,0.35); }

.empty { text-align: center; padding: 60px; color: #94a3b8; font-size: 15px; }
</style>
