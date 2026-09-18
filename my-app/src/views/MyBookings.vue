<script setup lang="ts">
import { ref, computed, onMounted, nextTick, Transition, TransitionGroup } from 'vue'
import { useBookingStore } from '@/stores/booking'
import { useUserStore } from '@/stores/user'
import { useApi } from '@/api/service'
import { agentChatStream } from '@/api/agent'
import type { Booking } from '@/types'

const bookingStore = useBookingStore()
const userStore = useUserStore()
const api = useApi()

onMounted(() => {
  if (userStore.isLoggedIn) {
    bookingStore.loadBookings()
  }
})

const activeTab = ref<'orders' | 'history' | 'account' | 'info' | 'service'>('orders')

const statusMap: Record<string, { text: string; color: string; bg: string }> = {
  pending: { text: '待确认', color: '#d97706', bg: '#fffbeb' },
  confirmed: { text: '已确认', color: '#16a34a', bg: '#f0fdf4' },
  completed: { text: '已完成', color: '#64748b', bg: '#f8fafc' },
  cancelled: { text: '已取消', color: '#dc2626', bg: '#fef2f2' }
}

const currentOrders = computed(() =>
  bookingStore.bookings.filter(b => b.status === 'pending' || b.status === 'confirmed')
)

const historyOrders = computed(() =>
  bookingStore.bookings.filter(b => b.status === 'completed' || b.status === 'cancelled')
)

// ========== 分页 ==========
const PAGE_SIZE = 3
const currentOrdersPage = ref(1)
const historyOrdersPage = ref(1)

const currentOrdersTotalPages = computed(() => Math.max(1, Math.ceil(currentOrders.value.length / PAGE_SIZE)))
const historyOrdersTotalPages = computed(() => Math.max(1, Math.ceil(historyOrders.value.length / PAGE_SIZE)))

const paginatedCurrentOrders = computed(() => {
  const start = (currentOrdersPage.value - 1) * PAGE_SIZE
  return currentOrders.value.slice(start, start + PAGE_SIZE)
})

const paginatedHistoryOrders = computed(() => {
  const start = (historyOrdersPage.value - 1) * PAGE_SIZE
  return historyOrders.value.slice(start, start + PAGE_SIZE)
})

function cancelOrder(id: number) {
  if (confirm('确定要取消此订单吗？')) bookingStore.cancelBooking(id)
}

// ========== 评价功能 ==========
const showReviewModal = ref(false)
const reviewBooking = ref<Booking | null>(null)
const reviewRating = ref(5)
const reviewContent = ref('')
const reviewSubmitting = ref(false)
const reviewedSet = ref<Set<number>>(new Set())

function openReview(booking: Booking) {
  reviewBooking.value = booking
  reviewRating.value = 5
  reviewContent.value = ''
  showReviewModal.value = true
}

async function submitReview() {
  if (!reviewBooking.value || !reviewContent.value.trim()) return
  reviewSubmitting.value = true
  try {
    await api.addReview({
      trimId: reviewBooking.value.carId,
      bookingId: reviewBooking.value.id,
      rating: reviewRating.value,
      content: reviewContent.value.trim(),
      carDays: reviewBooking.value.totalDays
    })
    reviewedSet.value.add(reviewBooking.value.id)
    showReviewModal.value = false
    alert('评价成功！')
  } catch (e: any) {
    alert(e?.response?.data?.message || '评价失败')
  } finally {
    reviewSubmitting.value = false
  }
}

interface ServiceMessage {
  id: number
  content: string
  sender: 'user' | 'agent'
  timestamp: string
}

const serviceMessages = ref<ServiceMessage[]>([])
const newMessage = ref('')
const serviceLoading = ref(false)
let msgIdCounter = 1000
let currentAbortController: AbortController | null = null
const chatContainer = ref<HTMLElement | null>(null)

function now() {
  return new Date().toLocaleString('zh-CN', { hour: '2-digit', minute: '2-digit' })
}

async function scrollToBottom() {
  await nextTick()
  if (chatContainer.value) {
    chatContainer.value.scrollTop = chatContainer.value.scrollHeight
  }
}

async function sendMessage() {
  const text = newMessage.value.trim()
  if (!text || serviceLoading.value) return

  serviceMessages.value.push({
    id: msgIdCounter++,
    content: text,
    sender: 'user',
    timestamp: now(),
  })
  newMessage.value = ''
  serviceLoading.value = true
  scrollToBottom()

  const agentMsg: ServiceMessage = {
    id: msgIdCounter++,
    content: '',
    sender: 'agent',
    timestamp: now(),
  }
  serviceMessages.value.push(agentMsg)

  const token = localStorage.getItem('token') || undefined
  currentAbortController = await agentChatStream(
    'mybookings-' + (userStore.user?.id || 'guest'),
    text,
    undefined,
    undefined,
    token,
    (chunk) => {
      agentMsg.content += chunk
      scrollToBottom()
    },
    () => {
      serviceLoading.value = false
      scrollToBottom()
    },
    (err) => {
      agentMsg.content = agentMsg.content || '抱歉，暂时无法回答您的问题，请稍后再试。'
      serviceLoading.value = false
      scrollToBottom()
    }
  )
}
</script>

<template>
  <div class="center-page" v-if="userStore.user">
    <aside class="sidebar">
      <div class="user-card">
        <div class="avatar">{{ userStore.user.name.charAt(0) }}</div>
        <div class="user-name">{{ userStore.user.name }}</div>
        <div class="user-level">{{ userStore.user.memberLevel }}</div>
      </div>
      <nav class="side-nav">
        <button :class="{ active: activeTab === 'orders' }" @click="activeTab = 'orders'">
          <span class="nav-icon">📋</span>我的订单
        </button>
        <button :class="{ active: activeTab === 'history' }" @click="activeTab = 'history'">
          <span class="nav-icon">📚</span>历史订单
        </button>
        <button :class="{ active: activeTab === 'account' }" @click="activeTab = 'account'">
          <span class="nav-icon">💳</span>我的账户
        </button>
        <button :class="{ active: activeTab === 'info' }" @click="activeTab = 'info'">
          <span class="nav-icon">👤</span>我的信息
        </button>
        <button :class="{ active: activeTab === 'service' }" @click="activeTab = 'service'">
          <span class="nav-icon">💬</span>在线客服
        </button>
      </nav>
    </aside>

    <main class="content">
      <Transition name="tab-fade" mode="out-in">
      <div v-if="activeTab === 'orders'" key="orders">
        <h2>我的订单</h2>
        <div class="order-list" v-if="currentOrders.length > 0">
          <TransitionGroup name="order-list" tag="div">
            <div v-for="b in paginatedCurrentOrders" :key="b.id" class="order-card">
            <div class="order-img"><img :src="b.carImage" :alt="b.carName" /></div>
            <div class="order-body">
              <div class="ob-top">
                <div><h3>{{ b.carName }}</h3><span class="oid">#{{ String(b.id).padStart(6, '0') }}</span></div>
                <span class="badge" :style="{ color: statusMap[b.status]?.color, background: statusMap[b.status]?.bg }">{{ statusMap[b.status]?.text }}</span>
              </div>
              <div class="ob-details">
                <div><span class="lbl">取车</span><span class="val">{{ b.startDate }} {{ b.pickupTime ? b.pickupTime.split('T')[1]?.substring(0,5) : '' }} · {{ b.pickupProvince || b.pickupLocation }}</span></div>
                <div><span class="lbl">还车</span><span class="val">{{ b.endDate }} {{ b.returnTime ? b.returnTime.split('T')[1]?.substring(0,5) : '' }} · {{ b.returnProvince || b.returnLocation }}</span></div>
                <div v-if="b.insuranceName"><span class="lbl">保障服务</span><span class="val insurance-tag">{{ b.insuranceName }}</span></div>
              </div>
              <div class="ob-footer">
                <span class="days">{{ b.totalDays }}天</span>
                <span class="price">¥{{ b.totalPrice }}</span>
                <button v-if="b.status === 'pending' || b.status === 'confirmed'" class="btn-cancel" @click="cancelOrder(b.id)">取消</button>
              </div>
            </div>
            </div>
          </TransitionGroup>
        </div>
        <div class="pagination" v-if="currentOrdersTotalPages > 1">
          <button class="page-btn" :disabled="currentOrdersPage <= 1" @click="currentOrdersPage--">上一页</button>
          <span class="page-info">{{ currentOrdersPage }}/{{ currentOrdersTotalPages }}</span>
          <button class="page-btn" :disabled="currentOrdersPage >= currentOrdersTotalPages" @click="currentOrdersPage++">下一页</button>
        </div>
        <div class="empty" v-if="currentOrders.length === 0">
          <p>暂无进行中的订单</p>
          <router-link to="/cars" class="btn-primary">去选车</router-link>
        </div>
      </div>
      </Transition>

      <Transition name="tab-fade" mode="out-in">
      <div v-if="activeTab === 'history'" key="history">
        <h2>历史订单</h2>
        <div class="order-list" v-if="historyOrders.length > 0">
          <TransitionGroup name="order-list" tag="div">
            <div v-for="b in paginatedHistoryOrders" :key="b.id" class="order-card">
            <div class="order-img"><img :src="b.carImage" :alt="b.carName" /></div>
            <div class="order-body">
              <div class="ob-top">
                <div><h3>{{ b.carName }}</h3><span class="oid">#{{ String(b.id).padStart(6, '0') }}</span></div>
                <span class="badge" :style="{ color: statusMap[b.status]?.color, background: statusMap[b.status]?.bg }">{{ statusMap[b.status]?.text }}</span>
              </div>
              <div class="ob-details">
                <div><span class="lbl">取车</span><span class="val">{{ b.startDate }} {{ b.pickupTime ? b.pickupTime.split('T')[1]?.substring(0,5) : '' }} · {{ b.pickupProvince || b.pickupLocation }}</span></div>
                <div><span class="lbl">还车</span><span class="val">{{ b.endDate }} {{ b.returnTime ? b.returnTime.split('T')[1]?.substring(0,5) : '' }} · {{ b.returnProvince || b.returnLocation }}</span></div>
                <div v-if="b.insuranceName"><span class="lbl">保障服务</span><span class="val insurance-tag">{{ b.insuranceName }}</span></div>
              </div>
              <div class="ob-footer">
                <span class="days">{{ b.totalDays }}天</span>
                <span class="price">¥{{ b.totalPrice }}</span>
                <button v-if="b.status === 'completed' && !reviewedSet.has(b.id)" class="btn-review" @click="openReview(b)">去评价</button>
                <span v-if="b.status === 'completed' && reviewedSet.has(b.id)" class="reviewed-tag">已评价</span>
              </div>
            </div>
            </div>
          </TransitionGroup>
        </div>
        <div class="pagination" v-if="historyOrdersTotalPages > 1">
          <button class="page-btn" :disabled="historyOrdersPage <= 1" @click="historyOrdersPage--">上一页</button>
          <span class="page-info">{{ historyOrdersPage }}/{{ historyOrdersTotalPages }}</span>
          <button class="page-btn" :disabled="historyOrdersPage >= historyOrdersTotalPages" @click="historyOrdersPage++">下一页</button>
        </div>
        <div class="empty" v-if="historyOrders.length === 0">
          <p>暂无历史订单</p>
        </div>
      </div>
      </Transition>

      <Transition name="tab-fade" mode="out-in">
      <div v-if="activeTab === 'account'" key="account">
        <h2>我的账户</h2>
        <div class="info-section">
          <div class="info-row"><span class="info-label">余额</span><span class="info-value">¥0.00</span></div>
          <div class="info-row"><span class="info-label">优惠券</span><span class="info-value">0 张</span></div>
          <div class="info-row"><span class="info-label">积分</span><span class="info-value">0</span></div>
        </div>
      </div>
      </Transition>

      <Transition name="tab-fade" mode="out-in">
      <div v-if="activeTab === 'info'" key="info">
        <h2>我的信息</h2>
        <div class="info-section">
          <div class="info-row"><span class="info-label">姓名</span><span class="info-value">{{ userStore.user.name }}</span></div>
          <div class="info-row"><span class="info-label">手机号</span><span class="info-value">{{ userStore.user.phone }}</span></div>
          <div class="info-row"><span class="info-label">会员等级</span><span class="info-value">{{ userStore.user.memberLevel }}</span></div>
        </div>
      </div>
      </Transition>

      <Transition name="tab-fade" mode="out-in">
      <div v-if="activeTab === 'service'" key="service">
        <h2>在线客服</h2>
        <div class="chat-box">
          <div class="chat-messages" ref="chatContainer">
            <div v-if="serviceMessages.length === 0" class="chat-empty">您好，请问有什么可以帮您？</div>
            <div v-for="msg in serviceMessages" :key="msg.id" :class="['chat-msg', msg.sender === 'user' ? 'user-msg' : 'agent-msg']">
              {{ msg.content }}
            </div>
            <div v-if="serviceLoading && (!serviceMessages.length || serviceMessages[serviceMessages.length-1].sender === 'user')" class="chat-msg agent-msg typing">
              <span class="dot"></span><span class="dot"></span><span class="dot"></span>
            </div>
          </div>
          <div class="chat-input">
            <input v-model="newMessage" placeholder="输入消息..." @keyup.enter="sendMessage" :disabled="serviceLoading" />
            <button @click="sendMessage" :disabled="serviceLoading">{{ serviceLoading ? '思考中...' : '发送' }}</button>
          </div>
        </div>
      </div>
      </Transition>
    </main>
  </div>

  <div class="not-login" v-else>
    <p>请先登录</p>
    <router-link to="/login" class="btn-primary">去登录</router-link>
  </div>

  <!-- 评价弹窗 -->
  <Teleport to="body">
    <Transition name="modal-fade">
    <div class="review-overlay" v-if="showReviewModal" @click.self="showReviewModal = false">
      <div class="review-modal">
        <div class="review-header">
          <h2>评价订单</h2>
          <button class="review-close" @click="showReviewModal = false">&times;</button>
        </div>
        <div class="review-car-info" v-if="reviewBooking">
          <img :src="reviewBooking.carImage" :alt="reviewBooking.carName" />
          <div>
            <h3>{{ reviewBooking.carName }}</h3>
            <p>租期 {{ reviewBooking.totalDays }}天 · {{ reviewBooking.startDate }} ~ {{ reviewBooking.endDate }}</p>
          </div>
        </div>
        <div class="review-stars-select">
          <label>评分</label>
          <div class="star-row">
            <span v-for="i in 5" :key="i" class="star-btn" :class="{ active: i <= reviewRating }" @click="reviewRating = i">★</span>
          </div>
        </div>
        <div class="review-textarea">
          <label>评价内容</label>
          <textarea v-model="reviewContent" placeholder="分享您的租车体验，帮助其他用户做出选择..." rows="4" maxlength="500"></textarea>
          <div class="char-count">{{ reviewContent.length }}/500</div>
        </div>
        <button class="review-submit" @click="submitReview" :disabled="reviewSubmitting || !reviewContent.trim()">
          {{ reviewSubmitting ? '提交中...' : '提交评价' }}
        </button>
      </div>
    </div>
    </Transition>
  </Teleport>
</template>

<style scoped>
.center-page { display: flex; max-width: 1100px; margin: 0 auto; padding: 48px 32px 80px; gap: 28px; }
.sidebar { width: 220px; flex-shrink: 0; }
.content { flex: 1; min-width: 0; }

.user-card { background: linear-gradient(135deg, #0891b2, #06b6d4); border-radius: 16px; padding: 28px 20px; text-align: center; margin-bottom: 16px; }
.user-card .avatar { width: 56px; height: 56px; border-radius: 14px; background: rgba(255,255,255,0.2); backdrop-filter: blur(8px); color: #fff; display: flex; align-items: center; justify-content: center; font-size: 22px; font-weight: 800; margin: 0 auto 12px; }
.user-card .user-name { color: #fff; font-size: 16px; font-weight: 700; margin-bottom: 4px; }
.user-card .user-level { color: rgba(255,255,255,0.7); font-size: 12px; }

.side-nav { display: flex; flex-direction: column; gap: 4px; background: #fff; border-radius: 12px; border: 1px solid #f1f5f9; overflow: hidden; }
.side-nav button { display: flex; align-items: center; gap: 10px; padding: 14px 18px; border: none; background: none; font-size: 14px; font-weight: 500; color: #64748b; cursor: pointer; transition: 0.15s; text-align: left; }
.side-nav button:hover { background: #f8fafc; color: #334155; }
.side-nav button.active { background: #f0fdf4; color: #16a34a; font-weight: 600; }
.nav-icon { font-size: 16px; width: 22px; text-align: center; }

.content h2 { font-size: 22px; font-weight: 800; color: #1a1a2e; margin-bottom: 24px; }
.order-list { display: flex; flex-direction: column; gap: 14px; }
.order-card { display: flex; background: #fff; border-radius: 14px; border: 1px solid #f1f5f9; overflow: hidden; transition: 0.2s; }
.order-card:hover { box-shadow: 0 8px 24px rgba(0,0,0,0.06); }
.order-img { width: 180px; flex-shrink: 0; }
.order-img img { width: 100%; height: 100%; object-fit: cover; }
.order-body { flex: 1; padding: 20px; display: flex; flex-direction: column; }
.ob-top { display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 16px; }
.ob-top h3 { font-size: 17px; font-weight: 700; color: #1a1a2e; margin-bottom: 2px; }
.oid { font-size: 12px; color: #94a3b8; }
.badge { padding: 4px 12px; border-radius: 100px; font-size: 12px; font-weight: 600; }
.ob-details { display: grid; grid-template-columns: 1fr 1fr; gap: 12px; flex: 1; }
.ob-details .lbl { display: block; font-size: 11px; color: #94a3b8; margin-bottom: 2px; }
.ob-details .val { font-size: 13px; color: #334155; font-weight: 500; }
.ob-footer { display: flex; align-items: center; gap: 16px; margin-top: 16px; padding-top: 16px; border-top: 1px solid #f1f5f9; }
.days { font-size: 13px; color: #94a3b8; }
.price { font-size: 22px; font-weight: 800; color: #ef4444; margin-left: auto; }
.btn-cancel { background: none; border: 1px solid #e2e8f0; color: #64748b; padding: 6px 16px; border-radius: 8px; font-size: 12px; cursor: pointer; transition: 0.15s; }
.btn-cancel:hover { border-color: #dc2626; color: #dc2626; }
.insurance-tag { color: #0891b2; font-weight: 600; }

.pagination { display: flex; align-items: center; justify-content: center; gap: 12px; margin-top: 20px; }
.page-info { font-size: 13px; color: #64748b; font-weight: 500; }
.page-btn { padding: 6px 16px; border: 1.5px solid #e2e8f0; border-radius: 8px; background: #fff; font-size: 13px; font-weight: 500; color: #334155; cursor: pointer; transition: 0.15s; }
.page-btn:hover:not(:disabled) { border-color: #0891b2; color: #0891b2; }
.page-btn:disabled { opacity: 0.4; cursor: not-allowed; }

.empty { text-align: center; padding: 80px 20px; }
.empty p { color: #94a3b8; margin-bottom: 20px; font-size: 15px; }
.btn-primary { display: inline-block; background: linear-gradient(135deg, #0891b2, #06b6d4); color: #fff; text-decoration: none; padding: 10px 28px; border-radius: 10px; font-weight: 600; font-size: 13px; }

.info-section { background: #fff; border-radius: 14px; border: 1px solid #f1f5f9; overflow: hidden; }
.info-row { display: flex; justify-content: space-between; align-items: center; padding: 18px 24px; border-bottom: 1px solid #f8fafc; }
.info-row:last-child { border-bottom: none; }
.info-label { font-size: 14px; color: #64748b; }
.info-value { font-size: 14px; color: #1a1a2e; font-weight: 600; }

.chat-box { background: #fff; border-radius: 14px; border: 1px solid #f1f5f9; overflow: hidden; }
.chat-messages { height: 320px; padding: 20px; overflow-y: auto; }
.chat-empty { color: #94a3b8; text-align: center; padding-top: 120px; font-size: 14px; }
.chat-msg { margin-bottom: 10px; padding: 10px 16px; border-radius: 12px; max-width: 70%; font-size: 14px; line-height: 1.5; }
.user-msg { background: #0891b2; color: #fff; margin-left: auto; border-bottom-right-radius: 4px; }
.agent-msg { background: #f1f5f9; color: #334155; border-bottom-left-radius: 4px; white-space: pre-wrap; }
.agent-msg.typing { display: flex; gap: 4px; padding: 14px 20px; }
.agent-msg.typing .dot { width: 7px; height: 7px; border-radius: 50%; background: #94a3b8; animation: typingBounce 1.2s infinite; }
.agent-msg.typing .dot:nth-child(2) { animation-delay: 0.2s; }
.agent-msg.typing .dot:nth-child(3) { animation-delay: 0.4s; }
@keyframes typingBounce {
  0%, 60%, 100% { transform: translateY(0); opacity: 0.4; }
  30% { transform: translateY(-4px); opacity: 1; }
}
.chat-input { display: flex; border-top: 1px solid #f1f5f9; padding: 12px 16px; gap: 10px; }
.chat-input input { flex: 1; border: 1px solid #e2e8f0; border-radius: 10px; padding: 10px 14px; font-size: 14px; outline: none; }
.chat-input input:focus { border-color: #0891b2; }
.chat-input button { background: linear-gradient(135deg, #0891b2, #06b6d4); color: #fff; border: none; padding: 10px 20px; border-radius: 10px; font-size: 14px; font-weight: 600; cursor: pointer; }

.not-login { text-align: center; padding: 120px 24px; }
.not-login p { font-size: 16px; color: #64748b; margin-bottom: 24px; }

.btn-review { background: linear-gradient(135deg, #0891b2, #06b6d4); color: #fff; border: none; padding: 6px 16px; border-radius: 8px; font-size: 12px; font-weight: 600; cursor: pointer; transition: 0.15s; }
.btn-review:hover { box-shadow: 0 4px 12px rgba(8,145,178,0.3); }
.reviewed-tag { font-size: 12px; color: #16a34a; font-weight: 600; }

.review-overlay { position: fixed; inset: 0; background: rgba(0,0,0,0.5); display: flex; align-items: center; justify-content: center; z-index: 9999; backdrop-filter: blur(4px); }
.review-modal { background: #fff; border-radius: 20px; width: 440px; max-width: 90vw; padding: 28px; box-shadow: 0 20px 60px rgba(0,0,0,0.15); }
.review-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.review-header h2 { font-size: 20px; font-weight: 800; }
.review-close { background: none; border: none; font-size: 24px; color: #94a3b8; cursor: pointer; }
.review-car-info { display: flex; gap: 14px; padding: 14px; background: #f8fafc; border-radius: 12px; margin-bottom: 20px; }
.review-car-info img { width: 80px; height: 56px; object-fit: cover; border-radius: 8px; }
.review-car-info h3 { font-size: 15px; font-weight: 700; margin-bottom: 4px; }
.review-car-info p { font-size: 12px; color: #94a3b8; }
.review-stars-select { margin-bottom: 16px; }
.review-stars-select label { display: block; font-size: 13px; font-weight: 600; color: #475569; margin-bottom: 8px; }
.star-row { display: flex; gap: 6px; }
.star-btn { font-size: 28px; color: #e2e8f0; cursor: pointer; transition: 0.1s; }
.star-btn.active, .star-btn:hover { color: #f59e0b; }
.review-textarea { margin-bottom: 20px; }
.review-textarea label { display: block; font-size: 13px; font-weight: 600; color: #475569; margin-bottom: 8px; }
.review-textarea textarea { width: 100%; border: 1.5px solid #e2e8f0; border-radius: 10px; padding: 12px; font-size: 14px; resize: vertical; outline: none; font-family: inherit; }
.review-textarea textarea:focus { border-color: #0891b2; }
.char-count { text-align: right; font-size: 12px; color: #94a3b8; margin-top: 4px; }
.review-submit { width: 100%; padding: 14px; border: none; border-radius: 12px; background: linear-gradient(135deg, #0891b2, #06b6d4); color: #fff; font-size: 15px; font-weight: 700; cursor: pointer; transition: 0.2s; }
.review-submit:hover:not(:disabled) { box-shadow: 0 6px 20px rgba(8,145,178,0.35); }
.review-submit:disabled { background: #cbd5e1; cursor: not-allowed; }

/* Tab fade transition */
.tab-fade-enter-active { transition: all 0.5s ease-out; }
.tab-fade-leave-active { transition: all 0.4s ease-in; }
.tab-fade-enter-from { opacity: 0; transform: translateY(16px); }
.tab-fade-leave-to { opacity: 0; transform: translateY(-12px); }

/* Order list transition */
.order-list-enter-active { transition: all 0.6s ease-out; }
.order-list-leave-active { transition: all 0.5s ease-in; }
.order-list-enter-from { opacity: 0; transform: translateX(-32px); }
.order-list-leave-to { opacity: 0; transform: translateX(32px); }
.order-list-move { transition: transform 0.6s ease; }

/* Modal fade transition */
.modal-fade-enter-active { transition: opacity 0.4s ease-out; }
.modal-fade-leave-active { transition: opacity 0.3s ease-in; }
.modal-fade-enter-from { opacity: 0; }
.modal-fade-leave-to { opacity: 0; }
.modal-fade-enter-active .review-modal { animation: modalSlideIn 0.5s ease-out; }
.modal-fade-leave-active .review-modal { animation: modalSlideOut 0.4s ease-in; }

@keyframes modalSlideIn { from { transform: scale(0.9) translateY(24px); opacity: 0; } to { transform: scale(1) translateY(0); opacity: 1; } }
@keyframes modalSlideOut { from { transform: scale(1) translateY(0); opacity: 1; } to { transform: scale(0.9) translateY(24px); opacity: 0; } }

@media (max-width: 768px) {
  .center-page { flex-direction: column; padding: 24px 16px 60px; }
  .sidebar { width: 100%; }
  .side-nav { flex-direction: row; flex-wrap: wrap; gap: 8px; padding: 12px; }
  .side-nav button { flex: 1; min-width: 80px; justify-content: center; padding: 10px 8px; font-size: 12px; }
  .order-img { width: 120px; }
}
</style>
