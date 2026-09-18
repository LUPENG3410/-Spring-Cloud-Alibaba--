<script setup lang="ts">
import { computed, ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useCarStore } from '@/stores/car'
import type { Review } from '@/types'

const route = useRoute()
const router = useRouter()
const carStore = useCarStore()

onMounted(async () => {
  const id = Number(route.params.id)
  await carStore.loadCars()
  await carStore.loadCarDetail(id)
})

const car = computed(() => {
  const id = Number(route.params.id)
  const province = route.params.province as string | undefined
  const cars = carStore.cars.filter(c => c.id === id)
  if (province) {
    return cars.find(c => c.province === province) || cars[0]
  }
  return cars[0]
})
const detail = computed(() => carStore.getCarDetail(Number(route.params.id)))

// ========== 评论：优先使用后端数据，无数据时显示空列表 ==========
const displayReviews = computed(() => detail.value?.reviews || [])

const activeTab = ref<'exterior' | 'interior'>('exterior')
const currentImage = ref(0)

const typeMap: Record<string, string> = { sedan: '轿车', suv: 'SUV', mpv: 'MPV', luxury: '豪华', sports: '跑车' }

const specGroups = computed(() => {
  if (!detail.value) return []
  const d = detail.value
  return [
    {
      title: '动力性能',
      icon: '⚡',
      items: [
        { label: '发动机', value: d.engine },
        { label: '排量', value: d.displacement },
        { label: '最大马力', value: `${d.horsepower}Ps` },
        { label: '最大扭矩', value: `${d.torque}N·m` },
        { label: '0-100km/h', value: d.acceleration },
        { label: '最高时速', value: d.topSpeed },
      ]
    },
    {
      title: '车身尺寸',
      icon: '📐',
      items: [
        { label: '长×宽×高', value: `${d.length} × ${d.width} × ${d.height}` },
        { label: '轴距', value: d.wheelbase },
        { label: '行李箱', value: d.trunkVolume },
        { label: '整备质量', value: d.weight },
        { label: '座位数', value: `${d.seats}座` },
        { label: '车门数', value: d.doors },
      ]
    },
    {
      title: '能源信息',
      icon: '⛽',
      items: [
        { label: '能源类型', value: d.fuelType },
        { label: '燃油标号', value: d.fuelGrade },
        { label: '油箱容积', value: d.fuelTankCapacity },
        { label: '综合油耗', value: d.fuelConsumption },
        { label: '变速箱', value: d.transmission },
        { label: '年款', value: `${d.year}款` },
      ]
    },
    {
      title: '智能配置',
      icon: '🤖',
      items: [
        { label: '倒车影像', value: d.reversingCamera ? '有' : '无' },
        { label: '自动驻车', value: d.autoHold ? '有' : '无' },
        { label: '辅助驾驶', value: d.driverAssist },
        { label: '智能互联', value: d.smartConnect },
        { label: '电动座椅', value: d.electricSeat ? '有' : '无' },
        { label: '座椅功能', value: d.seatFunction },
      ]
    }
  ]
})

const galleryImages = computed(() => {
  if (!detail.value) return []
  return activeTab.value === 'exterior' ? detail.value.images : detail.value.interiorImages
})

const avgRating = computed(() => {
  const reviews = displayReviews.value
  if (!reviews || reviews.length === 0) return 0
  const sum = reviews.reduce((s, r) => s + r.rating, 0)
  return Number((sum / reviews.length).toFixed(1))
})
</script>

<template>
  <div class="detail-page" v-if="car && detail">
    <!-- Breadcrumb -->
    <div class="breadcrumb">
      <router-link to="/">首页</router-link>
      <span>/</span>
      <router-link to="/cars">全部车辆</router-link>
      <span>/</span>
      <span class="current">{{ car.name }}</span>
    </div>

    <!-- Image Gallery -->
    <div class="gallery-section">
      <div class="main-image">
        <img :src="galleryImages[currentImage]" :alt="car.name" />
        <div class="img-nav" v-if="galleryImages.length > 1">
          <button @click="currentImage = (currentImage - 1 + galleryImages.length) % galleryImages.length">‹</button>
          <button @click="currentImage = (currentImage + 1) % galleryImages.length">›</button>
        </div>
        <div class="img-counter">{{ currentImage + 1 }} / {{ galleryImages.length }}</div>
      </div>
      <div class="thumb-row">
        <button :class="['tab-btn', { active: activeTab === 'exterior' }]" @click="activeTab = 'exterior'; currentImage = 0">外观实拍</button>
        <button :class="['tab-btn', { active: activeTab === 'interior' }]" @click="activeTab = 'interior'; currentImage = 0">内饰展示</button>
      </div>
      <div class="thumbs" v-if="galleryImages.length > 1">
        <div
          v-for="(img, i) in galleryImages"
          :key="i"
          :class="['thumb', { active: currentImage === i }]"
          @click="currentImage = i"
        >
          <img :src="img" :alt="`${car.name} ${i+1}`" />
        </div>
      </div>
    </div>

    <!-- Info Panel -->
    <div class="info-section">
      <div class="info-left">
        <div class="car-header">
          <div class="tags">
            <span class="type-tag">{{ typeMap[car.type] }}</span>
            <span class="year-tag">{{ detail.year }}款</span>
            <span class="province-tag">{{ car.province }}可用</span>
          </div>
          <h1>{{ car.name }}</h1>
          <p class="brand-line">{{ car.brand }} · {{ car.type.toUpperCase() }} · {{ detail.engine }}</p>
        </div>

        <div class="price-card">
          <div class="price-main">
            <span class="currency">¥</span>
            <span class="amount">{{ car.rentalPrice }}</span>
            <span class="unit">/天起</span>
          </div>
          <div class="price-note">
            <span>含基础保险</span>
            <span>·</span>
            <span>芝麻信用免押</span>
          </div>
          <div class="mileage-notice" v-if="car.type === 'luxury' || car.type === 'sports'">
            车辆上限每天500公里
          </div>
        </div>

        <!-- Quick Specs -->
        <div class="quick-specs">
          <div class="qs-item">
            <span class="qs-val">{{ detail.horsepower }}</span>
            <span class="qs-unit">Ps马力</span>
          </div>
          <div class="qs-divider"></div>
          <div class="qs-item">
            <span class="qs-val">{{ detail.acceleration }}</span>
            <span class="qs-unit">百公里加速</span>
          </div>
          <div class="qs-divider"></div>
          <div class="qs-item">
            <span class="qs-val">{{ detail.fuelConsumption }}</span>
            <span class="qs-unit">综合油耗</span>
          </div>
          <div class="qs-divider"></div>
          <div class="qs-item">
            <span class="qs-val">{{ detail.seats }}</span>
            <span class="qs-unit">座位数</span>
          </div>
        </div>

        <!-- Features -->
        <div class="features-section">
          <h3>配置亮点</h3>
          <div class="feature-grid">
            <div v-for="f in detail.features" :key="f" class="feature-item">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="#0891b2" stroke-width="2.5"><path d="M20 6 9 17l-5-5"/></svg>
              {{ f }}
            </div>
          </div>
        </div>

        <!-- Description -->
        <div class="desc-section">
          <h3>车型介绍</h3>
          <div v-for="(desc, i) in detail.descriptions" :key="i" class="desc-item">
            <span class="desc-dot"></span>
            <p>{{ desc }}</p>
          </div>
        </div>
      </div>

      <!-- Right Sidebar -->
      <div class="info-right">
        <div class="book-card">
          <h3>立即预订</h3>
          <div class="book-price">
            <span class="bp-amount">¥{{ car.rentalPrice }}</span>
            <span class="bp-unit">/天</span>
          </div>
          <div class="book-includes">
            <div class="bi-item"><svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="#16a34a" stroke-width="2.5"><path d="M20 6 9 17l-5-5"/></svg> 基础保险已含</div>
            <div class="bi-item"><svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="#16a34a" stroke-width="2.5"><path d="M20 6 9 17l-5-5"/></svg> 24h道路救援</div>
            <div class="bi-item"><svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="#16a34a" stroke-width="2.5"><path d="M20 6 9 17l-5-5"/></svg> 免费上门送车</div>
            <div class="bi-item"><svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="#16a34a" stroke-width="2.5"><path d="M20 6 9 17l-5-5"/></svg> 不限公里数</div>
          </div>
          <button class="btn-book" @click="router.push(`/booking/${car.id}/${car.province || ''}`)">立即预订</button>
          <button class="btn-consult" @click="router.push({ path: '/service', query: { carId: car.id, carName: car.name } })">咨询客服</button>
        </div>

        <!-- Rental Info -->
        <div class="rental-info">
          <h4>租车须知</h4>
          <div class="ri-item">
            <span class="ri-icon">📍</span>
            <div>
              <span class="ri-title">取车方式</span>
              <span class="ri-desc">到店自取 / 上门送车</span>
            </div>
          </div>
          <div class="ri-item">
            <span class="ri-icon">💳</span>
            <div>
              <span class="ri-title">押金说明</span>
              <span class="ri-desc">芝麻信用650+免押金</span>
            </div>
          </div>
          <div class="ri-item">
            <span class="ri-icon">🛡️</span>
            <div>
              <span class="ri-title">保险服务</span>
              <span class="ri-desc">基础险已含，可升级全险</span>
            </div>
          </div>
          <div class="ri-item">
            <span class="ri-icon">⏰</span>
            <div>
              <span class="ri-title">超时说明</span>
              <span class="ri-desc">超2h按小时/超时按天</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Full Specs -->
    <div class="specs-section">
      <h2>详细参数配置</h2>
      <div class="spec-grid">
        <div v-for="group in specGroups" :key="group.title" class="spec-group">
          <div class="sg-header">
            <span class="sg-icon">{{ group.icon }}</span>
            <h3>{{ group.title }}</h3>
          </div>
          <div class="sg-body">
            <div v-for="item in group.items" :key="item.label" class="sg-row">
              <span class="sg-label">{{ item.label }}</span>
              <span class="sg-value">{{ item.value }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Reviews -->
    <div class="reviews-section" v-if="displayReviews.length > 0">
      <div class="reviews-header">
        <h2>用户评价</h2>
        <div class="rating-summary">
          <span class="avg-score">{{ avgRating.toFixed(1) }}</span>
          <div class="stars">
            <span v-for="i in 5" :key="i" :class="['star', { filled: i <= Math.round(avgRating) }]">★</span>
          </div>
          <span class="review-count">{{ displayReviews.length }} 条评价</span>
        </div>
      </div>
      <div class="reviews-list">
        <div v-for="review in displayReviews" :key="review.id" class="review-card">
          <div class="review-left">
            <div class="review-avatar">{{ review.avatar }}</div>
          </div>
          <div class="review-body">
            <div class="review-top">
              <div class="review-user">
                <span class="review-name">{{ review.userName }}</span>
                <span class="review-date">{{ review.date }}</span>
              </div>
              <div class="review-stars">
                <span v-for="i in 5" :key="i" :class="['star', { filled: i <= review.rating }]">★</span>
              </div>
            </div>
            <p class="review-content">{{ review.content }}</p>
            <div class="review-meta">租期 {{ review.carDays }} 天</div>
          </div>
        </div>
      </div>
    </div>
  </div>

  <div class="not-found" v-else>
    <h2>车辆未找到</h2>
    <button @click="router.push('/cars')">返回车辆列表</button>
  </div>
</template>

<style scoped>
.detail-page { max-width: 1280px; margin: 0 auto; padding: 24px 32px 80px; }

/* Breadcrumb */
.breadcrumb { display: flex; align-items: center; gap: 8px; margin-bottom: 24px; font-size: 13px; color: #94a3b8; }
.breadcrumb a { color: #64748b; text-decoration: none; transition: 0.15s; }
.breadcrumb a:hover { color: #0891b2; }
.breadcrumb .current { color: #0f172a; font-weight: 600; }

/* Gallery */
.gallery-section { margin-bottom: 40px; }
.main-image { position: relative; border-radius: 20px; overflow: hidden; background: #f8fafc; margin-bottom: 16px; }
.main-image img { width: 100%; height: 480px; object-fit: cover; }
.img-nav { position: absolute; top: 50%; left: 0; right: 0; display: flex; justify-content: space-between; padding: 0 16px; transform: translateY(-50%); pointer-events: none; }
.img-nav button {
  width: 44px; height: 44px; border-radius: 50%;
  background: rgba(255,255,255,0.9); border: none;
  font-size: 20px; color: #334155; cursor: pointer;
  pointer-events: all; transition: 0.15s;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}
.img-nav button:hover { background: #fff; box-shadow: 0 4px 16px rgba(0,0,0,0.15); }
.img-counter {
  position: absolute; bottom: 16px; right: 16px;
  background: rgba(0,0,0,0.5); color: #fff;
  padding: 4px 12px; border-radius: 100px; font-size: 12px;
}
.thumb-row { display: flex; gap: 8px; margin-bottom: 12px; }
.tab-btn {
  padding: 8px 20px; border: 1px solid #e2e8f0; background: #fff;
  border-radius: 8px; font-size: 13px; font-weight: 600; color: #64748b;
  cursor: pointer; transition: 0.15s;
}
.tab-btn.active { background: #0891b2; color: #fff; border-color: #0891b2; }
.thumbs { display: flex; gap: 12px; }
.thumb { width: 100px; height: 68px; border-radius: 10px; overflow: hidden; cursor: pointer; border: 2px solid transparent; transition: 0.15s; }
.thumb.active { border-color: #0891b2; }
.thumb img { width: 100%; height: 100%; object-fit: cover; }

/* Info Section */
.info-section { display: grid; grid-template-columns: 1fr 380px; gap: 40px; margin-bottom: 60px; }

.car-header .tags { display: flex; gap: 8px; margin-bottom: 12px; }
.type-tag { background: #f0fdfa; color: #0891b2; padding: 5px 14px; border-radius: 6px; font-size: 12px; font-weight: 700; }
.year-tag { background: #f1f5f9; color: #475569; padding: 5px 14px; border-radius: 6px; font-size: 12px; font-weight: 600; }
.province-tag { background: #eff6ff; color: #2563eb; padding: 5px 14px; border-radius: 6px; font-size: 12px; font-weight: 600; }
.car-header h1 { font-size: 34px; font-weight: 900; color: #0f172a; margin-bottom: 6px; }
.brand-line { color: #94a3b8; font-size: 14px; }

.price-card { background: linear-gradient(135deg, #f0fdfa, #ecfeff); border: 1px solid #ccfbf1; border-radius: 14px; padding: 24px; margin: 24px 0; }
.price-main { display: flex; align-items: baseline; margin-bottom: 6px; }
.currency { font-size: 18px; font-weight: 700; color: #0891b2; }
.amount { font-size: 44px; font-weight: 900; color: #0891b2; line-height: 1; }
.unit { font-size: 14px; color: #64748b; margin-left: 4px; }
.price-note { display: flex; gap: 8px; font-size: 13px; color: #64748b; }
.mileage-notice { font-size: 11px; color: #f59e0b; margin-top: 8px; padding: 4px 8px; background: #fffbeb; border-radius: 6px; }

.quick-specs { display: flex; align-items: center; background: #fff; border: 1px solid #f1f5f9; border-radius: 14px; padding: 24px; margin-bottom: 28px; }
.qs-item { flex: 1; text-align: center; }
.qs-val { display: block; font-size: 24px; font-weight: 800; color: #0f172a; }
.qs-unit { font-size: 12px; color: #94a3b8; }
.qs-divider { width: 1px; height: 40px; background: #e2e8f0; }

.features-section { margin-bottom: 28px; }
.features-section h3, .desc-section h3 { font-size: 16px; font-weight: 700; color: #0f172a; margin-bottom: 14px; }
.feature-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 10px; }
.feature-item { display: flex; align-items: center; gap: 8px; padding: 10px 14px; background: #f8fafc; border-radius: 8px; font-size: 13px; color: #334155; font-weight: 500; }

.desc-item { display: flex; gap: 10px; margin-bottom: 10px; }
.desc-dot { width: 6px; height: 6px; border-radius: 50%; background: #0891b2; margin-top: 8px; flex-shrink: 0; }
.desc-item p { font-size: 14px; color: #475569; line-height: 1.7; }

/* Right Sidebar */
.book-card { background: #fff; border: 1px solid #f1f5f9; border-radius: 16px; padding: 28px; margin-bottom: 20px; position: sticky; top: 88px; }
.book-card h3 { font-size: 16px; font-weight: 700; margin-bottom: 16px; }
.book-price { margin-bottom: 20px; }
.bp-amount { font-size: 32px; font-weight: 900; color: #0891b2; }
.bp-unit { font-size: 14px; color: #94a3b8; }
.book-includes { display: flex; flex-direction: column; gap: 10px; margin-bottom: 24px; }
.bi-item { display: flex; align-items: center; gap: 8px; font-size: 13px; color: #475569; }
.btn-book {
  width: 100%; padding: 14px; border: none; border-radius: 12px;
  background: linear-gradient(135deg, #0891b2, #06b6d4);
  color: #fff; font-size: 15px; font-weight: 700;
  cursor: pointer; transition: 0.2s; margin-bottom: 10px;
}
.btn-book:hover { transform: translateY(-1px); box-shadow: 0 6px 20px rgba(8,145,178,0.35); }
.btn-consult {
  width: 100%; padding: 12px; border: 1.5px solid #e2e8f0; border-radius: 10px;
  background: #fff; color: #475569; font-size: 14px; font-weight: 600;
  cursor: pointer; transition: 0.15s;
}
.btn-consult:hover { border-color: #0891b2; color: #0891b2; }

.rental-info { background: #fff; border: 1px solid #f1f5f9; border-radius: 16px; padding: 24px; }
.rental-info h4 { font-size: 14px; font-weight: 700; margin-bottom: 16px; }
.ri-item { display: flex; gap: 12px; padding: 10px 0; border-bottom: 1px solid #f8fafc; }
.ri-item:last-child { border-bottom: none; }
.ri-icon { font-size: 18px; }
.ri-title { display: block; font-size: 13px; font-weight: 600; color: #0f172a; }
.ri-desc { font-size: 12px; color: #94a3b8; }

/* Specs Section */
.specs-section { margin-top: 20px; }
.specs-section h2 { font-size: 22px; font-weight: 800; margin-bottom: 24px; }
.spec-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 20px; }
.spec-group { background: #fff; border: 1px solid #f1f5f9; border-radius: 14px; overflow: hidden; }
.sg-header { display: flex; align-items: center; gap: 10px; padding: 18px 20px; background: #f8fafc; border-bottom: 1px solid #f1f5f9; }
.sg-icon { font-size: 18px; }
.sg-header h3 { font-size: 14px; font-weight: 700; color: #0f172a; }
.sg-body { padding: 4px 0; }
.sg-row { display: flex; justify-content: space-between; padding: 12px 20px; border-bottom: 1px solid #f8fafc; }
.sg-row:last-child { border-bottom: none; }
.sg-label { font-size: 13px; color: #94a3b8; }
.sg-value { font-size: 13px; font-weight: 600; color: #0f172a; }

.not-found { text-align: center; padding: 120px 32px; }
.not-found h2 { font-size: 22px; margin-bottom: 24px; color: #334155; }
.not-found button { background: #0891b2; color: #fff; border: none; padding: 12px 28px; border-radius: 10px; font-size: 14px; font-weight: 600; cursor: pointer; }

/* Reviews */
.reviews-section { margin-top: 20px; }
.reviews-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 24px; }
.reviews-header h2 { font-size: 22px; font-weight: 800; }
.rating-summary { display: flex; align-items: center; gap: 12px; }
.avg-score { font-size: 36px; font-weight: 900; color: #f59e0b; }
.stars { display: flex; gap: 2px; }
.star { font-size: 18px; color: #e2e8f0; }
.star.filled { color: #f59e0b; }
.review-count { font-size: 14px; color: #94a3b8; }

.reviews-list { display: flex; flex-direction: column; gap: 16px; }
.review-card { display: flex; gap: 16px; background: #fff; border: 1px solid #f1f5f9; border-radius: 14px; padding: 24px; }
.review-avatar {
  width: 44px; height: 44px; border-radius: 12px;
  background: linear-gradient(135deg, #0891b2, #06b6d4);
  color: #fff; display: flex; align-items: center; justify-content: center;
  font-size: 16px; font-weight: 700; flex-shrink: 0;
}
.review-body { flex: 1; }
.review-top { display: flex; justify-content: space-between; align-items: center; margin-bottom: 10px; }
.review-user { display: flex; align-items: center; gap: 10px; }
.review-name { font-size: 14px; font-weight: 700; color: #0f172a; }
.review-date { font-size: 12px; color: #94a3b8; }
.review-stars .star { font-size: 14px; }
.review-content { font-size: 14px; color: #475569; line-height: 1.7; margin-bottom: 10px; }
.review-meta { font-size: 12px; color: #94a3b8; }
</style>
