<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useCarStore } from '@/stores/car'
import CarCard from '@/components/CarCard.vue'
import Globe3D from '@/components/Globe3D.vue'
import { useRouter } from 'vue-router'
import { fetchHotCars } from '@/api'

const carStore = useCarStore()
const router = useRouter()

const hotCars = ref<any[]>([])

onMounted(async () => {
  carStore.loadCars()
  try {
    hotCars.value = await fetchHotCars()
  } catch { /* ignore */ }
})

const featuredCars = computed(() => carStore.cars.slice(0, 8))

const hotCities = ['北京', '上海', '广州', '深圳', '成都', '杭州', '宁夏']

const now = new Date()
const defaultPickup = new Date(now.getTime() + 3 * 24 * 60 * 60 * 1000)
const defaultReturn = new Date(now.getTime() + 6 * 24 * 60 * 60 * 1000)

function formatDatetime(d: Date) {
  const pad = (n: number) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}T${pad(d.getHours())}:${pad(d.getMinutes())}`
}

const pickupTime = ref(formatDatetime(defaultPickup))
const returnTime = ref(formatDatetime(defaultReturn))

const faqList = ref([
  { q: '租车需要什么条件？', a: '年满18周岁，持有有效驾驶证，芝麻信用650分以上可免押金。非芝麻信用用户需缴纳押金。', open: false },
  { q: '如何取车和还车？', a: '支持到店自取和上门送车两种方式。还车时将车辆开至约定门店即可，也可选择上门取车服务。', open: false },
  { q: '跨省租车怎么收费？', a: '同省还车无额外费用；跨省还车收取50元/天的异地还车费，具体费用在下单时会明确显示。', open: false },
  { q: '车辆保险包含哪些？', a: '基础保险已包含在租金中，涵盖第三者责任险和车损险。可升级全险，增加玻璃险、涉水险等。', open: false },
  { q: '超时还车怎么算？', a: '超时2小时内按小时计费，超过2小时按全天计费。建议提前续租，避免产生额外费用。', open: false },
  { q: '可以中途换车吗？', a: '可以联系客服协商换车，同级别车型免费换，升级车型需补差价。换车需到指定门店办理。', open: false },
])
</script>

<template>
  <div class="home">
    <!-- Hero -->
    <section class="hero">
      <div class="hero-bg">
        <div class="hero-shape s1"></div>
        <div class="hero-shape s2"></div>
        <div class="hero-shape s3"></div>
      </div>

      <!-- 3D Globe -->
      <div class="globe-wrap">
        <Globe3D />
      </div>
      <div class="hero-content">
        <div class="hero-badge">全国30+省份 · 200+门店</div>
        <h1>轻松租车<br/><em>畅享每一程</em></h1>
        <p>超过500+车型任选，支持跨省异地还车，让出行更自由</p>

        <div class="search-box">
          <div class="search-tabs">
            <button class="stab active">普通租车</button>
            <button class="stab" @click="router.push('/cross-province')">跨省租车</button>
          </div>
          <div class="search-body">
            <div class="sf">
              <label>取车城市</label>
              <select>
                <option v-for="c in hotCities" :key="c">{{ c }}</option>
              </select>
            </div>
            <div class="sd"></div>
            <div class="sf">
              <label>取车时间</label>
              <input type="datetime-local" v-model="pickupTime" :min="formatDatetime(new Date())" />
            </div>
            <div class="sd"></div>
            <div class="sf">
              <label>还车时间</label>
              <input type="datetime-local" v-model="returnTime" :min="pickupTime" />
            </div>
            <button class="search-btn" @click="router.push('/cars')">搜索车辆</button>
          </div>
        </div>

        <div class="hero-bottom">
          <div class="hero-stat"><strong>500+</strong><span>在库车辆</span></div>
          <div class="hero-stat"><strong>200+</strong><span>服务门店</span></div>
          <div class="hero-stat"><strong>30+</strong><span>覆盖省份</span></div>
          <div class="hero-stat"><strong>80万+</strong><span>服务客户</span></div>
        </div>
      </div>
    </section>

    <!-- Hot Cities -->
    <section class="section cities-section">
      <div class="container">
        <h2 class="sec-title">热门城市</h2>
        <p class="sec-sub">选择城市，即刻出发</p>
        <div class="city-grid">
          <div v-for="(c, i) in hotCities" :key="c" class="city-card" @click="router.push('/stores')">
            <div class="city-bg" :style="{background: ['#2563eb','#0891b2','#059669','#7c3aed','#dc2626','#ea580c','#ca8a04'][i]}"></div>
            <span class="city-name">{{ c }}</span>
            <span class="city-stores">查看门店 →</span>
          </div>
        </div>
      </div>
    </section>

    <!-- Features -->
    <section class="section feat-section">
      <div class="container">
        <div class="feat-grid">
          <div class="feat-card">
            <div class="feat-icon blue">
              <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><path d="M12 22s-8-4.5-8-11.8A8 8 0 0 1 12 2a8 8 0 0 1 8 8.2c0 7.3-8 11.8-8 11.8z"/><circle cx="12" cy="10" r="3"/></svg>
            </div>
            <h3>全国连锁</h3>
            <p>30+省份200+门店，覆盖主要城市和机场</p>
          </div>
          <div class="feat-card">
            <div class="feat-icon teal">
              <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><path d="M12 2v20M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6"/></svg>
            </div>
            <h3>价格透明</h3>
            <p>一口价无隐藏费用，芝麻信用免押金</p>
          </div>
          <div class="feat-card">
            <div class="feat-icon green">
              <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><path d="M12 22c5.523 0 10-4.477 10-10S17.523 2 12 2 2 6.477 2 12s4.477 10 10 10z"/><path d="m9 12 2 2 4-4"/></svg>
            </div>
            <h3>品质保障</h3>
            <p>全车定期检测保养，车况安全有保障</p>
          </div>
          <div class="feat-card">
            <div class="feat-icon purple">
              <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><path d="M22 16.92v3a2 2 0 0 1-2.18 2 19.79 19.79 0 0 1-8.63-3.07 19.5 19.5 0 0 1-6-6 19.79 19.79 0 0 1-3.07-8.67A2 2 0 0 1 4.11 2h3a2 2 0 0 1 2 1.72c.127.96.361 1.903.7 2.81a2 2 0 0 1-.45 2.11L8.09 9.91a16 16 0 0 0 6 6l1.27-1.27a2 2 0 0 1 2.11-.45c.907.339 1.85.573 2.81.7A2 2 0 0 1 22 16.92z"/></svg>
            </div>
            <h3>24小时服务</h3>
            <p>全天候客服在线，道路救援随叫随到</p>
          </div>
        </div>
      </div>
    </section>

    <!-- Hot Cars -->
    <section class="section">
      <div class="container">
        <div class="sec-header">
          <div>
            <h2 class="sec-title">热门车型</h2>
            <p class="sec-sub">基于真实成交数据，为您精选最受欢迎的车型</p>
          </div>
          <a class="view-all" @click="router.push('/cars')">查看全部 →</a>
        </div>
        <div class="hot-grid" v-if="hotCars.length">
          <div v-for="(car, idx) in hotCars" :key="car.trimId" class="hot-card">
            <div class="hot-rank" :class="`rank-${idx + 1}`">{{ idx + 1 }}</div>
            <div class="hot-img">
              <img :src="car.image" :alt="car.name" />
            </div>
            <div class="hot-info">
              <div class="hot-brand">{{ car.brand }}</div>
              <div class="hot-name">{{ car.name }}</div>
              <div class="hot-meta">
                <span v-if="car.seats">{{ car.seats }}座</span>
                <span v-if="car.transmission">{{ car.transmission }}</span>
                <span v-if="car.fuel">{{ car.fuel }}</span>
              </div>
              <div class="hot-price">¥{{ car.rentalPrice || car.price }}<small>/天</small></div>
              <div class="hot-orders">已成交 <strong>{{ car.orderCount }}</strong> 单</div>
            </div>
          </div>
        </div>
        <div class="car-grid" v-if="!hotCars.length">
          <CarCard v-for="car in featuredCars" :key="car.id" :car="car" />
        </div>
      </div>
    </section>

    <!-- Cross Province Banner -->
    <section class="section cp-section">
      <div class="container">
        <div class="cp-banner">
          <div class="cp-content">
            <span class="cp-badge">新功能</span>
            <h2>跨省异地还车</h2>
            <p>取车北京，还车上海，灵活行程更自由。全国门店通还，省心省力。</p>
            <button class="cp-btn" @click="router.push('/cross-province')">了解更多</button>
          </div>
          <div class="cp-visual">
            <div class="cp-map">
              <div class="cp-dot d1"></div>
              <div class="cp-dot d2"></div>
              <div class="cp-line"></div>
              <span class="cp-label l1">北京</span>
              <span class="cp-label l2">上海</span>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- Q&A Section -->
    <section class="section faq-section">
      <div class="container">
        <h2 class="sec-title">租车常见问题</h2>
        <p class="sec-sub">关于租车的疑问，在这里都能找到答案</p>
        <div class="faq-grid">
          <div v-for="(item, i) in faqList" :key="i" class="faq-item" :class="{ open: item.open }" @click="item.open = !item.open">
            <div class="faq-q">
              <span>{{ item.q }}</span>
              <svg :class="['faq-arrow', { rotated: item.open }]" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="m6 9 6 6 6-6"/></svg>
            </div>
            <div class="faq-a" v-show="item.open">
              <p>{{ item.a }}</p>
            </div>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<style scoped>
.home { min-height: 100vh; }

/* Hero */
.hero {
  position: relative; padding: 80px 64px 64px;
  overflow: hidden; min-height: 560px;
  display: flex; align-items: center;
}
.hero-bg {
  position: absolute; inset: 0;
  background: linear-gradient(135deg, #0c1222 0%, #162447 40%, #1f4068 70%, #1b3a5c 100%);
}
.hero-shape {
  position: absolute; border-radius: 50%;
}
.hero-shape.s1 { width: 600px; height: 600px; top: -250px; right: -150px; background: radial-gradient(circle, rgba(56,189,248,0.12) 0%, transparent 70%); }
.hero-shape.s2 { width: 400px; height: 400px; bottom: -150px; left: -80px; background: radial-gradient(circle, rgba(139,92,246,0.1) 0%, transparent 70%); }
.hero-shape.s3 { width: 300px; height: 300px; top: 40%; left: 55%; background: radial-gradient(circle, rgba(16,185,129,0.08) 0%, transparent 70%); }

.hero-content {
  position: relative; width: 60%; margin-left: 5%; z-index: 1;
  text-align: center;
}
.hero-badge {
  display: inline-block;
  background: rgba(255,255,255,0.08);
  backdrop-filter: blur(8px);
  color: rgba(255,255,255,0.7);
  padding: 6px 20px; border-radius: 100px;
  font-size: 13px; font-weight: 500; letter-spacing: 0.5px;
  margin-bottom: 28px;
  border: 1px solid rgba(255,255,255,0.06);
}
.hero h1 {
  font-size: 56px; font-weight: 900; color: #fff;
  line-height: 1.1; margin-bottom: 20px;
  letter-spacing: -1px;
}
.hero h1 em {
  font-style: normal;
  background: linear-gradient(135deg, #38bdf8, #a78bfa);
  -webkit-background-clip: text; -webkit-text-fill-color: transparent;
  background-clip: text;
}
.hero-content > p {
  font-size: 17px; color: rgba(255,255,255,0.55);
  margin-bottom: 40px; line-height: 1.6;
}

/* Search Box */
.search-box {
  background: #fff; border-radius: 20px;
  box-shadow: 0 24px 64px rgba(0,0,0,0.3);
  overflow: hidden; max-width: 720px; margin: 0 auto 48px;
}
.search-tabs {
  display: flex; border-bottom: 1px solid #f1f5f9;
}
.stab {
  flex: 1; padding: 14px; background: none; border: none;
  font-size: 14px; font-weight: 600; color: #94a3b8;
  cursor: pointer; transition: 0.15s; position: relative;
}
.stab.active { color: #0891b2; }
.stab.active::after {
  content: ''; position: absolute; bottom: 0; left: 20%; right: 20%;
  height: 3px; background: linear-gradient(90deg, #0891b2, #06b6d4);
  border-radius: 3px 3px 0 0;
}
.search-body {
  display: flex; align-items: center; padding: 8px;
}
.sf { flex: 1; padding: 12px 16px; text-align: left; }
.sf label { display: block; font-size: 11px; font-weight: 600; color: #94a3b8; text-transform: uppercase; letter-spacing: 0.5px; margin-bottom: 4px; }
.sf select, .sf input {
  border: none; outline: none; font-size: 14px;
  font-weight: 600; color: #0f172a; width: 100%;
  background: transparent; cursor: pointer;
}
.sf input::-webkit-calendar-picker-indicator { cursor: pointer; opacity: 0.5; }
.sd { width: 1px; height: 32px; background: #e2e8f0; flex-shrink: 0; }
.search-btn {
  background: linear-gradient(135deg, #0891b2, #06b6d4);
  color: #fff; border: none;
  padding: 14px 28px; border-radius: 12px;
  font-size: 14px; font-weight: 700;
  cursor: pointer; white-space: nowrap;
  transition: 0.2s; margin: 8px;
}
.search-btn:hover { transform: translateY(-1px); box-shadow: 0 6px 20px rgba(8,145,178,0.4); }

.hero-bottom {
  display: flex; justify-content: center; gap: 48px;
}
.hero-stat { text-align: center; }
.hero-stat strong { display: block; font-size: 28px; font-weight: 800; color: #fff; }
.hero-stat span { font-size: 13px; color: rgba(255,255,255,0.45); }

/* 3D Globe */
.globe-wrap {
  position: absolute;
  top: 50%;
  right: 5%;
  transform: translateY(-50%);
  width: 420px;
  height: 420px;
  z-index: 5;
}

/* Sections */
.section { padding: 72px 32px; }
.container { max-width: 1280px; margin: 0 auto; }
.sec-title { font-size: 30px; font-weight: 800; color: #0f172a; margin-bottom: 6px; }
.sec-sub { color: #94a3b8; font-size: 15px; }
.sec-header { display: flex; justify-content: space-between; align-items: flex-end; margin-bottom: 40px; }
.view-all { color: #0891b2; font-weight: 600; font-size: 14px; cursor: pointer; text-decoration: none; transition: 0.15s; }
.view-all:hover { opacity: 0.8; }

/* Cities */
.cities-section { background: #f8fafc; }
.city-grid { display: grid; grid-template-columns: repeat(7, 1fr); gap: 14px; margin-top: 32px; }
.city-card {
  position: relative; border-radius: 14px; padding: 28px 16px;
  overflow: hidden; cursor: pointer; transition: 0.25s;
  min-height: 120px; display: flex; flex-direction: column;
  justify-content: flex-end;
}
.city-card:hover { transform: translateY(-4px); box-shadow: 0 12px 32px rgba(0,0,0,0.12); }
.city-bg { position: absolute; inset: 0; opacity: 0.88; }
.city-name { position: relative; color: #fff; font-size: 18px; font-weight: 800; margin-bottom: 4px; }
.city-stores { position: relative; color: rgba(255,255,255,0.65); font-size: 11px; font-weight: 500; }

/* Features */
.feat-section { background: #fff; padding: 48px 32px; }
.feat-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 16px; }
.feat-card { text-align: center; padding: 24px 16px; border-radius: 12px; transition: 0.25s; }
.feat-card:hover { background: #f8fafc; transform: translateY(-4px); }
.feat-icon {
  width: 48px; height: 48px; border-radius: 12px;
  display: flex; align-items: center; justify-content: center;
  margin: 0 auto 12px;
}
.feat-icon.blue { background: #eff6ff; color: #2563eb; }
.feat-icon.teal { background: #f0fdfa; color: #0891b2; }
.feat-icon.green { background: #f0fdf4; color: #16a34a; }
.feat-icon.purple { background: #faf5ff; color: #9333ea; }
.feat-card h3 { font-size: 15px; font-weight: 700; color: #0f172a; margin-bottom: 6px; }
.feat-card p { font-size: 12px; color: #94a3b8; line-height: 1.5; }

/* Car Grid */
.car-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 20px; }

/* Hot Cars Grid */
.hot-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 20px; }
.hot-card {
  position: relative; background: #fff; border-radius: 16px;
  border: 1px solid #f1f5f9; overflow: hidden; cursor: pointer;
  transition: 0.25s;
}
.hot-card:hover { transform: translateY(-6px); box-shadow: 0 16px 40px rgba(0,0,0,0.1); }
.hot-rank {
  position: absolute; top: 12px; left: 12px; z-index: 2;
  width: 32px; height: 32px; border-radius: 10px;
  display: flex; align-items: center; justify-content: center;
  font-size: 16px; font-weight: 900; color: #fff;
}
.rank-1 { background: linear-gradient(135deg, #f59e0b, #f97316); }
.rank-2 { background: linear-gradient(135deg, #94a3b8, #64748b); }
.rank-3 { background: linear-gradient(135deg, #cd7f32, #b8860b); }
.rank-4 { background: linear-gradient(135deg, #0891b2, #06b6d4); }
.hot-img { width: 100%; height: 180px; overflow: hidden; }
.hot-img img { width: 100%; height: 100%; object-fit: cover; transition: 0.3s; }
.hot-card:hover .hot-img img { transform: scale(1.06); }
.hot-info { padding: 16px; }
.hot-brand { font-size: 11px; font-weight: 600; color: #0891b2; text-transform: uppercase; letter-spacing: 0.5px; margin-bottom: 4px; }
.hot-name { font-size: 16px; font-weight: 700; color: #0f172a; margin-bottom: 8px; }
.hot-meta { display: flex; gap: 8px; margin-bottom: 10px; }
.hot-meta span { font-size: 11px; color: #94a3b8; background: #f1f5f9; padding: 2px 8px; border-radius: 4px; }
.hot-price { font-size: 20px; font-weight: 800; color: #ef4444; margin-bottom: 6px; }
.hot-price small { font-size: 12px; font-weight: 500; color: #94a3b8; }
.hot-orders { font-size: 12px; color: #64748b; }
.hot-orders strong { color: #0891b2; font-weight: 700; }

/* Cross Province Banner */
.cp-banner {
  display: flex; align-items: center; justify-content: space-between;
  background: linear-gradient(135deg, #0f172a, #1e3a5f, #162447);
  border-radius: 24px; padding: 56px 64px; overflow: hidden;
  position: relative;
}
.cp-content { max-width: 480px; }
.cp-badge {
  display: inline-block; background: #f59e0b; color: #fff;
  padding: 4px 14px; border-radius: 6px; font-size: 12px;
  font-weight: 700; margin-bottom: 20px;
}
.cp-content h2 { font-size: 36px; font-weight: 800; color: #fff; margin-bottom: 16px; }
.cp-content p { color: rgba(255,255,255,0.55); font-size: 15px; line-height: 1.7; margin-bottom: 28px; }
.cp-btn {
  background: linear-gradient(135deg, #0891b2, #06b6d4);
  color: #fff; border: none; padding: 14px 32px;
  border-radius: 12px; font-size: 15px; font-weight: 700;
  cursor: pointer; transition: 0.2s;
}
.cp-btn:hover { transform: translateY(-2px); box-shadow: 0 8px 24px rgba(8,145,178,0.4); }

.cp-visual { flex-shrink: 0; }
.cp-map {
  width: 280px; height: 200px; position: relative;
  background: rgba(255,255,255,0.04); border-radius: 16px;
  border: 1px solid rgba(255,255,255,0.06);
}
.cp-dot {
  position: absolute; width: 12px; height: 12px;
  border-radius: 50%; background: #38bdf8;
  animation: pulse 2s infinite;
}
.cp-dot.d1 { top: 40px; left: 60px; }
.cp-dot.d2 { top: 100px; right: 50px; background: #a78bfa; }
.cp-line {
  position: absolute; top: 46px; left: 72px;
  width: 120px; height: 2px;
  background: linear-gradient(90deg, #38bdf8, #a78bfa);
  transform: rotate(20deg);
}
.cp-label {
  position: absolute; color: rgba(255,255,255,0.8); font-size: 13px;
  font-weight: 600;
}
.cp-label.l1 { top: 20px; left: 48px; }
.cp-label.l2 { bottom: 50px; right: 38px; }

@keyframes pulse {
  0%, 100% { box-shadow: 0 0 0 0 rgba(56,189,248,0.4); }
  50% { box-shadow: 0 0 0 8px rgba(56,189,248,0); }
}

/* Q&A */
.faq-section { background: #fff; }
.faq-grid { max-width: 800px; margin: 32px auto 0; }
.faq-item {
  border: 1px solid #f1f5f9; border-radius: 12px;
  margin-bottom: 10px; cursor: pointer;
  transition: 0.2s; overflow: hidden;
}
.faq-item:hover { border-color: #e2e8f0; }
.faq-item.open { border-color: #0891b2; background: #f0fdfa; }
.faq-q {
  display: flex; justify-content: space-between; align-items: center;
  padding: 18px 22px; font-size: 15px; font-weight: 600; color: #0f172a;
}
.faq-arrow { transition: transform 0.2s; flex-shrink: 0; color: #94a3b8; }
.faq-arrow.rotated { transform: rotate(180deg); color: #0891b2; }
.faq-a { padding: 0 22px 18px; }
.faq-a p { font-size: 14px; color: #64748b; line-height: 1.7; }


</style>
