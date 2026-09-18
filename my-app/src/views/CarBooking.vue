<script setup lang="ts">
import { computed, ref, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useCarStore } from '@/stores/car'
import { useBookingStore } from '@/stores/booking'
import { useUserStore } from '@/stores/user'
import { useApi } from '@/api/service'
import type { InsuranceProduct } from '@/types'

const route = useRoute()
const router = useRouter()
const carStore = useCarStore()
const bookingStore = useBookingStore()
const userStore = useUserStore()
const api = useApi()

const car = computed(() => {
  const id = Number(route.params.id)
  const province = route.params.province as string | undefined
  const cars = carStore.cars.filter(c => c.id === id)
  if (province) {
    return cars.find(c => c.province === province) || cars[0]
  }
  return cars[0]
})

const startDate = ref('')
const endDate = ref('')
const pickupTime = ref('09:00')
const returnTime = ref('09:00')
const pickupProvince = ref('')
const returnProvince = ref('')
const agreeTerms = ref(false)

// 保险产品
const insuranceProducts = ref<InsuranceProduct[]>([])
const selectedInsuranceId = ref<number | null>(null)

// 所有车辆的省份列表（从 API 直接获取）
const allProvinces = ref<string[]>([])

const provinces = computed(() => {
  const list = [...allProvinces.value]
  if (car.value?.province && !list.includes(car.value.province)) {
    list.push(car.value.province)
  }
  return list
})

const totalDays = computed(() => {
  if (!startDate.value || !endDate.value) return 0

  // 创建完整的日期时间对象
  const pickupDateTime = new Date(`${startDate.value}T${pickupTime.value}:00`)
  const returnDateTime = new Date(`${endDate.value}T${returnTime.value}:00`)

  // 计算小时差
  const diffMs = returnDateTime.getTime() - pickupDateTime.getTime()
  const diffHours = diffMs / (1000 * 60 * 60)

  // 未满24小时算1天，超过24小时按比例计算（向上取整）
  if (diffHours <= 0) return 0
  return Math.max(1, Math.ceil(diffHours / 24))
})

const isCrossProvince = computed(() => pickupProvince.value !== returnProvince.value)
const crossProvinceFee = computed(() => isCrossProvince.value ? totalDays.value * 50 : 0)

const selectedInsurance = computed(() => {
  return insuranceProducts.value.find(p => p.id === selectedInsuranceId.value)
})

const insurancePrice = computed(() => {
  if (!selectedInsurance.value) return 0
  return selectedInsurance.value.totalPrice
})

const HITCH_DISCOUNT = 20
const isHitchCar = computed(() => !!car.value?.isHitch)
const hitchDiscount = computed(() => isHitchCar.value ? totalDays.value * HITCH_DISCOUNT : 0)

// 时间校验
const todayStr = new Date().toISOString().split('T')[0]
const timeError = ref('')
function validateTime() {
  if (!startDate.value || !endDate.value || !pickupTime.value || !returnTime.value) {
    timeError.value = ''
    return
  }
  const now = new Date()
  const pickup = new Date(`${startDate.value}T${pickupTime.value}:00`)
  const ret = new Date(`${endDate.value}T${returnTime.value}:00`)
  if (pickup < now) { timeError.value = '取车时间不能早于当前时间'; return }
  if (ret < now) { timeError.value = '还车时间不能早于当前时间'; return }
  if (ret <= pickup) { timeError.value = '还车时间必须晚于取车时间'; return }
  timeError.value = ''
}
const dailyPrice = computed(() => {
  const base = car.value?.rentalPrice || 0
  return isHitchCar.value ? Math.max(0, base - HITCH_DISCOUNT) : base
})
const rentalPrice = computed(() => Math.round(totalDays.value * dailyPrice.value * 100) / 100)
const totalPrice = computed(() => Math.round((rentalPrice.value + crossProvinceFee.value + insurancePrice.value) * 100) / 100)

// 模拟支付状态
const showPayment = ref(false)
const paymentMethod = ref<'alipay' | 'wechat'>('alipay')
const paying = ref(false)
const paySuccess = ref(false)

// 加载数据
onMounted(async () => {
  console.log('[CarBooking] onMounted 触发, route.params.id =', route.params.id)
  const trimId = Number(route.params.id)
  // 确保 store 有数据（模板 v-if="car" 依赖它）
  if (carStore.cars.length === 0) {
    console.log('[CarBooking] store 为空, 调用 loadCars')
    await carStore.loadCars()
    console.log('[CarBooking] loadCars 完成, 数量:', carStore.cars.length)
  }
  // 直接从 API 获取省份信息
  try {
    const allCars = await api.getCars()
    const province = route.params.province as string | undefined
    const candidates = allCars.filter(c => c.id === trimId)
    const found = province
      ? candidates.find(c => c.province === province) || candidates[0]
      : candidates[0]
    console.log('[CarBooking] 找到车辆:', found?.name, '省份:', found?.province)
    if (found?.province) {
      pickupProvince.value = found.province
      returnProvince.value = found.province
    }
    allProvinces.value = [...new Set(allCars.map(c => c.province).filter(Boolean))] as string[]
  } catch (e) {
    console.error('[CarBooking] 加载车辆数据失败:', e)
  }
  // 保险产品
  try {
    insuranceProducts.value = await api.getInsuranceProducts()
    const basicProduct = insuranceProducts.value.find(p => p.code === 'basic')
    if (basicProduct) {
      selectedInsuranceId.value = basicProduct.id
    }
  } catch (e) {
    console.warn('加载保险产品失败:', e)
  }
})

// 可用性检查
const carAvailable = ref<boolean | null>(null)
const checkingAvailability = ref(false)
let checkTimer: ReturnType<typeof setTimeout> | null = null

watch(
  [startDate, endDate, pickupProvince],
  () => {
    if (checkTimer) clearTimeout(checkTimer)
    if (!startDate.value || !endDate.value || !car.value) {
      carAvailable.value = null
      return
    }
    checkTimer = setTimeout(async () => {
      checkingAvailability.value = true
      try {
        carAvailable.value = await api.checkCarAvailability(car.value!.id, pickupProvince.value, startDate.value, endDate.value)
      } catch {
        carAvailable.value = null
      } finally {
        checkingAvailability.value = false
      }
    }, 500)
  }
)

// 时间变更时校验
watch([startDate, endDate, pickupTime, returnTime], () => {
  validateTime()
}, { immediate: true })

function handleSubmit() {
  validateTime()
  if (!userStore.isLoggedIn) { router.push('/login'); return }
  if (!agreeTerms.value || totalDays.value === 0) return
  if (carAvailable.value === false) return
  if (timeError.value) return
  showPayment.value = true
}

async function handlePay() {
  paying.value = true
  // 模拟支付过程 2 秒
  await new Promise(resolve => setTimeout(resolve, 2000))
  paying.value = false
  paySuccess.value = true

  // 支付成功后提交订单
  const pickupDateTime = `${startDate.value}T${pickupTime.value}:00`
  const returnDateTime = `${endDate.value}T${returnTime.value}:00`

  await bookingStore.addBooking({
    carId: car.value!.id, carName: car.value!.name, carImage: car.value!.image,
    startDate: startDate.value, endDate: endDate.value,
    pickupTime: pickupDateTime, returnTime: returnDateTime,
    totalDays: totalDays.value, totalPrice: totalPrice.value,
    pickupLocation: `${pickupProvince.value}门店`, returnLocation: `${returnProvince.value}门店`,
    pickupProvince: pickupProvince.value, returnProvince: returnProvince.value,
    insuranceProductId: selectedInsuranceId.value || undefined,
    insuranceName: selectedInsurance.value?.name,
    insurancePrice: insurancePrice.value
  })

  // 1.5 秒后跳转
  setTimeout(() => {
    router.push('/bookings')
  }, 1500)
}

function closePayment() {
  if (paying.value) return
  showPayment.value = false
  paySuccess.value = false
}
</script>

<template>
  <div class="book-page" v-if="car">
    <button class="back" @click="router.back()">
      <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="m15 18-6-6 6-6"/></svg>
      返回
    </button>
    <h1>确认预订</h1>

    <div class="book-grid">
      <div class="form-card">
        <div class="car-preview">
          <img :src="car.image" :alt="car.name" />
          <div><h3>{{ car.name }}</h3><p>{{ car.brand }} · {{ car.seats }}座 · {{ car.transmission }}</p></div>
        </div>

        <form @submit.prevent="handleSubmit">
          <div class="fg-row">
            <div class="fg"><label>取车省份</label><select v-model="pickupProvince"><option v-for="p in provinces" :key="p">{{ p }}</option></select></div>
            <div class="fg"><label>还车省份</label><select v-model="returnProvince"><option v-for="p in provinces" :key="p">{{ p }}</option></select></div>
          </div>
          <div class="cross-tip" v-if="isCrossProvince">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="#f59e0b" stroke-width="2"><path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"/><line x1="12" y1="9" x2="12" y2="13"/><line x1="12" y1="17" x2="12.01" y2="17"/></svg>
            跨省还车收取 ¥50/天 异地费
          </div>
          <div class="fg-row">
            <div class="fg"><label>取车日期</label><input v-model="startDate" type="date" :min="todayStr" @change="validateTime" required /></div>
            <div class="fg"><label>取车时间</label><input v-model="pickupTime" type="time" @change="validateTime" required /></div>
          </div>
          <div class="fg-row">
            <div class="fg"><label>还车日期</label><input v-model="endDate" type="date" :min="startDate || todayStr" @change="validateTime" required /></div>
            <div class="fg"><label>还车时间</label><input v-model="returnTime" type="time" @change="validateTime" required /></div>
          </div>
          <div class="avail-tip unavailable" v-if="timeError">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="#ef4444" stroke-width="2"><circle cx="12" cy="12" r="10"/><path d="m15 9-6 6"/><path d="m9 9 6 6"/></svg>
            {{ timeError }}
          </div>

          <div class="avail-tip available" v-if="carAvailable === true">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="#16a34a" stroke-width="2"><path d="M20 6 9 17l-5-5"/></svg>
            该时间段车辆可用，可以放心预订
          </div>
          <div class="avail-tip unavailable" v-else-if="carAvailable === false">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="#ef4444" stroke-width="2"><circle cx="12" cy="12" r="10"/><path d="m15 9-6 6"/><path d="m9 9 6 6"/></svg>
            该时间段车辆已被预约，请更换日期或选择其他车辆
          </div>
          <div class="avail-tip checking" v-else-if="checkingAvailability">
            <span class="mini-spinner"></span>
            正在检查车辆可用性...
          </div>

          <!-- 保险选择 -->
          <div class="insurance-section">
            <label class="section-label">选择保障服务</label>
            <div class="insurance-options">
              <div 
                v-for="product in insuranceProducts" 
                :key="product.id"
                class="insurance-option"
                :class="{ selected: selectedInsuranceId === product.id }"
                @click="selectedInsuranceId = product.id"
              >
                <div class="insurance-header">
                  <div class="insurance-radio">
                    <div class="radio-dot" v-if="selectedInsuranceId === product.id"></div>
                  </div>
                  <div class="insurance-info">
                    <span class="insurance-name">{{ product.name }}</span>
                    <span class="insurance-price" v-if="product.totalPrice > 0">¥{{ product.totalPrice }}/天</span>
                    <span class="insurance-price free" v-else>免费</span>
                  </div>
                </div>
                <div class="insurance-details">
                  <div class="detail-item">
                    <span class="detail-label">车辆损失免赔额</span>
                    <span class="detail-value">{{ product.vehicleLossDeductible }}元</span>
                  </div>
                  <div class="detail-item">
                    <span class="detail-label">第三者责任限额</span>
                    <span class="detail-value">{{ product.thirdPartyLimit / 10000 }}万</span>
                  </div>
                  <div class="detail-item">
                    <span class="detail-label">驾驶员保障</span>
                    <span class="detail-value">{{ product.driverLossLimit / 10000 }}万</span>
                  </div>
                  <div class="detail-item" v-if="product.tireLossCovered">
                    <span class="detail-label">单独轮胎损失</span>
                    <span class="detail-value highlight">100%保障</span>
                  </div>
                  <div class="detail-item" v-if="product.thirdPartyMedicalCovered">
                    <span class="detail-label">第三者医保外医疗</span>
                    <span class="detail-value highlight">{{ product.thirdPartyMedicalLimit / 10000 }}万</span>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <label class="terms"><input v-model="agreeTerms" type="checkbox" /><span>同意<a href="#">《租车协议》</a>和<a href="#">《保险条款》</a></span></label>
          <button type="submit" class="btn-submit" :disabled="!agreeTerms || totalDays === 0 || carAvailable === false || !!timeError">确认预订</button>
        </form>
      </div>

      <div class="summary" v-if="totalDays > 0">
        <h3>费用明细</h3>
        <div class="rows">
          <div class="r"><span>日租金</span><span>¥{{ car.rentalPrice }} × {{ totalDays }}天</span></div>
          <div class="r hitch-discount" v-if="isHitchCar"><span>顺风车优惠</span><span>-¥{{ hitchDiscount }}</span></div>
          <div class="r" v-if="isCrossProvince"><span>异地还车费</span><span>¥{{ crossProvinceFee }}</span></div>
          <div class="r"><span>保障服务</span><span v-if="insurancePrice > 0">¥{{ insurancePrice }}</span><span class="free" v-else>免费</span></div>
        </div>
        <div class="total-row"><span>合计</span><span class="tp">¥{{ totalPrice }}</span></div>
        <div class="note" v-if="isHitchCar">
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="#16a34a" stroke-width="2"><path d="M20 6 9 17l-5-5"/></svg>
          顺风车：帮忙开回原籍，每天省 ¥{{ HITCH_DISCOUNT }}
        </div>
        <div class="note" v-else>
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="#94a3b8" stroke-width="2"><circle cx="12" cy="12" r="10"/><path d="M12 16v-4"/><path d="M12 8h.01"/></svg>
          芝麻信用650+可免押金
        </div>
      </div>
    </div>
  </div>

  <!-- 模拟支付弹窗 -->
  <Teleport to="body">
    <div class="pay-overlay" v-if="showPayment" @click.self="closePayment">
      <div class="pay-modal">
        <!-- 支付中 -->
        <template v-if="!paySuccess">
          <div class="pay-header">
            <h2>确认支付</h2>
            <button class="pay-close" @click="closePayment" :disabled="paying">&times;</button>
          </div>
          <div class="pay-amount">
            <span class="pay-label">支付金额</span>
            <span class="pay-price">¥{{ totalPrice.toFixed(2) }}</span>
          </div>
          <div class="pay-info">
            <div class="pay-row"><span>车辆</span><span>{{ car?.name }}</span></div>
            <div class="pay-row"><span>租期</span><span>{{ totalDays }}天</span></div>
            <div class="pay-row"><span>取车</span><span>{{ startDate }} {{ pickupTime }}</span></div>
            <div class="pay-row"><span>还车</span><span>{{ endDate }} {{ returnTime }}</span></div>
          </div>
          <div class="pay-methods">
            <div class="pay-method" :class="{ active: paymentMethod === 'alipay' }" @click="paymentMethod = 'alipay'">
              <div class="pm-icon alipay-icon">支</div>
              <span>支付宝</span>
              <div class="pm-check" v-if="paymentMethod === 'alipay'"></div>
            </div>
            <div class="pay-method" :class="{ active: paymentMethod === 'wechat' }" @click="paymentMethod = 'wechat'">
              <div class="pm-icon wechat-icon">微</div>
              <span>微信支付</span>
              <div class="pm-check" v-if="paymentMethod === 'wechat'"></div>
            </div>
          </div>
          <button class="pay-btn" :class="{ paying }" @click="handlePay" :disabled="paying">
            <span class="spinner" v-if="paying"></span>
            {{ paying ? '支付处理中...' : '立即支付' }}
          </button>
        </template>

        <!-- 支付成功 -->
        <template v-else>
          <div class="pay-success">
            <div class="success-icon">✓</div>
            <h2>支付成功</h2>
            <p>¥{{ totalPrice.toFixed(2) }} 已支付</p>
            <p class="success-tip">正在跳转到订单页...</p>
          </div>
        </template>
      </div>
    </div>
  </Teleport>
</template>

<style scoped>
.book-page { max-width: 960px; margin: 0 auto; padding: 32px 32px 80px; }
.back { display: inline-flex; align-items: center; gap: 4px; background: none; border: none; color: #64748b; font-size: 13px; cursor: pointer; padding: 8px 0; margin-bottom: 16px; }
.back:hover { color: #1a1a2e; }
.book-page h1 { font-size: 28px; font-weight: 800; color: #1a1a2e; margin-bottom: 28px; }

.book-grid { display: grid; grid-template-columns: 1fr 320px; gap: 24px; }
.form-card { background: #fff; border-radius: 16px; padding: 28px; border: 1px solid #f1f5f9; }
.car-preview { display: flex; gap: 14px; padding-bottom: 20px; margin-bottom: 24px; border-bottom: 1px solid #f1f5f9; }
.car-preview img { width: 120px; height: 80px; object-fit: cover; border-radius: 10px; }
.car-preview h3 { font-size: 16px; font-weight: 700; color: #1a1a2e; margin-bottom: 4px; }
.car-preview p { font-size: 13px; color: #94a3b8; }

.fg-row { display: grid; grid-template-columns: 1fr 1fr; gap: 14px; margin-bottom: 16px; }
.fg { display: flex; flex-direction: column; gap: 6px; }
.fg label { font-size: 12px; font-weight: 600; color: #475569; }
.fg input, .fg select { padding: 11px 14px; border: 1.5px solid #e2e8f0; border-radius: 10px; font-size: 14px; color: #1a1a2e; outline: none; background: #fff; transition: 0.15s; }
.fg input:focus, .fg select:focus { border-color: #0891b2; box-shadow: 0 0 0 3px rgba(8,145,178,0.1); }

.cross-tip { display: flex; align-items: center; gap: 8px; padding: 10px 14px; background: #fffbeb; border-radius: 8px; font-size: 12px; color: #92400e; margin-bottom: 16px; }

.avail-tip {
  display: flex; align-items: center; gap: 8px;
  padding: 10px 14px; border-radius: 8px; font-size: 12px; margin-bottom: 16px;
}
.avail-tip.available { background: #f0fdf4; color: #166534; }
.avail-tip.unavailable { background: #fef2f2; color: #991b1b; }
.avail-tip.checking { background: #f0f9ff; color: #0369a1; }
.mini-spinner {
  width: 14px; height: 14px; border: 2px solid rgba(3,105,161,0.2);
  border-top-color: #0369a1; border-radius: 50%;
  animation: spin 0.8s linear infinite; display: inline-block;
}

/* 保险选择 */
.insurance-section { margin-bottom: 20px; }
.section-label { display: block; font-size: 12px; font-weight: 600; color: #475569; margin-bottom: 12px; }
.insurance-options { display: flex; flex-direction: column; gap: 12px; }
.insurance-option {
  border: 2px solid #e2e8f0; border-radius: 12px; padding: 16px;
  cursor: pointer; transition: all 0.2s;
}
.insurance-option:hover { border-color: #cbd5e1; }
.insurance-option.selected { border-color: #0891b2; background: #f0fdfa; }
.insurance-header { display: flex; align-items: center; gap: 12px; margin-bottom: 12px; }
.insurance-radio {
  width: 20px; height: 20px; border: 2px solid #cbd5e1; border-radius: 50%;
  display: flex; align-items: center; justify-content: center; flex-shrink: 0;
}
.insurance-option.selected .insurance-radio { border-color: #0891b2; }
.radio-dot { width: 10px; height: 10px; background: #0891b2; border-radius: 50%; }
.insurance-info { display: flex; align-items: center; gap: 8px; flex: 1; }
.insurance-name { font-size: 14px; font-weight: 600; color: #1a1a2e; }
.insurance-price { font-size: 14px; color: #ef4444; font-weight: 600; }
.insurance-price.free { color: #16a34a; }
.insurance-details { display: grid; grid-template-columns: 1fr 1fr; gap: 8px; padding-left: 32px; }
.detail-item { display: flex; justify-content: space-between; font-size: 12px; }
.detail-label { color: #64748b; }
.detail-value { color: #475569; font-weight: 500; }
.detail-value.highlight { color: #16a34a; }

.terms { display: flex; align-items: flex-start; gap: 8px; margin: 20px 0; font-size: 13px; color: #64748b; cursor: pointer; line-height: 1.5; }
.terms input { margin-top: 2px; accent-color: #ff6b35; }
.terms a { color: #0891b2; text-decoration: none; }

.btn-submit {
  width: 100%; padding: 14px; border: none; border-radius: 12px;
  background: linear-gradient(135deg, #0891b2, #06b6d4);
  color: #fff; font-size: 15px; font-weight: 700;
  cursor: pointer; transition: 0.2s;
}
.btn-submit:hover:not(:disabled) { transform: translateY(-1px); box-shadow: 0 6px 20px rgba(8,145,178,0.35); }
.btn-submit:disabled { background: #cbd5e1; cursor: not-allowed; }

.summary { background: #fff; border-radius: 16px; padding: 24px; border: 1px solid #f1f5f9; height: fit-content; position: sticky; top: 88px; }
.summary h3 { font-size: 15px; font-weight: 700; margin-bottom: 18px; }
.rows { display: flex; flex-direction: column; gap: 12px; margin-bottom: 18px; }
.r { display: flex; justify-content: space-between; font-size: 13px; color: #475569; }
.r.hitch-discount { color: #16a34a; }
.free { color: #16a34a; font-weight: 500; }
.total-row { display: flex; justify-content: space-between; align-items: center; padding-top: 16px; border-top: 1px solid #f1f5f9; }
.total-row span:first-child { font-size: 13px; color: #475569; }
.tp { font-size: 28px; font-weight: 900; color: #ef4444; }
.note { display: flex; align-items: center; gap: 6px; margin-top: 14px; padding: 10px 12px; background: #f8fafc; border-radius: 8px; font-size: 12px; color: #94a3b8; }

/* 支付弹窗 */
.pay-overlay {
  position: fixed; inset: 0; background: rgba(0,0,0,0.5);
  display: flex; align-items: center; justify-content: center;
  z-index: 9999; backdrop-filter: blur(4px);
  animation: fadeIn 0.2s ease;
}
@keyframes fadeIn { from { opacity: 0; } to { opacity: 1; } }

.pay-modal {
  background: #fff; border-radius: 20px; width: 400px; max-width: 90vw;
  padding: 28px; box-shadow: 0 20px 60px rgba(0,0,0,0.15);
  animation: slideUp 0.3s ease;
}
@keyframes slideUp { from { transform: translateY(20px); opacity: 0; } to { transform: translateY(0); opacity: 1; } }

.pay-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 24px; }
.pay-header h2 { font-size: 20px; font-weight: 800; color: #1a1a2e; }
.pay-close { background: none; border: none; font-size: 24px; color: #94a3b8; cursor: pointer; padding: 0 4px; }
.pay-close:hover { color: #475569; }
.pay-close:disabled { cursor: not-allowed; opacity: 0.5; }

.pay-amount { text-align: center; padding: 20px 0; border-bottom: 1px solid #f1f5f9; margin-bottom: 20px; }
.pay-label { display: block; font-size: 13px; color: #94a3b8; margin-bottom: 8px; }
.pay-price { font-size: 36px; font-weight: 900; color: #ef4444; }

.pay-info { margin-bottom: 20px; }
.pay-row { display: flex; justify-content: space-between; padding: 8px 0; font-size: 13px; color: #475569; }
.pay-row span:last-child { font-weight: 600; color: #1a1a2e; }

.pay-methods { display: flex; gap: 12px; margin-bottom: 24px; }
.pay-method {
  flex: 1; display: flex; align-items: center; gap: 10px;
  padding: 14px; border: 2px solid #e2e8f0; border-radius: 12px;
  cursor: pointer; transition: 0.15s; position: relative;
}
.pay-method:hover { border-color: #cbd5e1; }
.pay-method.active { border-color: #0891b2; background: #f0fdfa; }

.pm-icon { width: 36px; height: 36px; border-radius: 8px; display: flex; align-items: center; justify-content: center; font-size: 16px; font-weight: 800; color: #fff; }
.alipay-icon { background: linear-gradient(135deg, #1677ff, #4096ff); }
.wechat-icon { background: linear-gradient(135deg, #07c160, #2aae67); }

.pay-method span { font-size: 14px; font-weight: 600; color: #334155; }

.pm-check {
  position: absolute; top: 8px; right: 8px;
  width: 18px; height: 18px; border-radius: 50%;
  background: #0891b2; display: flex; align-items: center; justify-content: center;
}
.pm-check::after { content: '✓'; color: #fff; font-size: 11px; font-weight: 700; }

.pay-btn {
  width: 100%; padding: 15px; border: none; border-radius: 12px;
  background: linear-gradient(135deg, #0891b2, #06b6d4);
  color: #fff; font-size: 16px; font-weight: 700;
  cursor: pointer; transition: 0.2s; display: flex; align-items: center; justify-content: center; gap: 8px;
}
.pay-btn:hover:not(:disabled) { box-shadow: 0 6px 20px rgba(8,145,178,0.4); }
.pay-btn.paying { background: #94a3b8; cursor: not-allowed; }

.spinner {
  width: 18px; height: 18px; border: 2.5px solid rgba(255,255,255,0.3);
  border-top-color: #fff; border-radius: 50%;
  animation: spin 0.8s linear infinite;
}
@keyframes spin { to { transform: rotate(360deg); } }

/* 支付成功 */
.pay-success { text-align: center; padding: 20px 0; }
.success-icon {
  width: 72px; height: 72px; border-radius: 50%;
  background: linear-gradient(135deg, #16a34a, #22c55e);
  display: flex; align-items: center; justify-content: center;
  margin: 0 auto 20px; font-size: 32px; color: #fff; font-weight: 700;
  animation: scaleIn 0.3s ease;
}
@keyframes scaleIn { from { transform: scale(0); } to { transform: scale(1); } }
.pay-success h2 { font-size: 22px; font-weight: 800; color: #1a1a2e; margin-bottom: 8px; }
.pay-success p { font-size: 15px; color: #64748b; }
.pay-success .success-tip { font-size: 13px; color: #94a3b8; margin-top: 16px; }
</style>
