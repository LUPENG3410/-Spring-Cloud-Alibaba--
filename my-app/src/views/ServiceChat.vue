<script setup lang="ts">
import { ref, reactive, computed, onMounted, nextTick, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { useCarStore } from '@/stores/car'
import {
  createConversation,
  sendMessage,
  getConversation,
  getUserConversations
} from '@/api/index'
import { agentChatStream } from '@/api/agent'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const carStore = useCarStore()

const carId = computed(() => route.query.carId ? Number(route.query.carId) : null)
const carName = computed(() => (route.query.carName as string) || '')
const car = computed(() => carId.value ? carStore.getCarById(carId.value) : null)

interface ChatMessage {
  id: number
  conversationId: string
  userId: number
  content: string
  sender: 'user' | 'agent' | 'system'
  timestamp: string
}

interface QuickReply {
  label: string
  text: string
}

const conversationId = ref('')
const messages = ref<ChatMessage[]>([])
const inputText = ref('')
const chatContainer = ref<HTMLElement | null>(null)
const isLoading = ref(false)
const isConnected = ref(false)
const showSidebar = ref(false)
const myConversations = ref<{ id: string; updatedAt: string; lastMessage: string }[]>([])

const quickReplies = computed<QuickReply[]>(() => {
  const replies: QuickReply[] = [
    { label: '营业时间', text: '请问门店的营业时间是什么？' },
    { label: '押金说明', text: '租车需要交多少押金？' },
    { label: '保险服务', text: '保险是怎么算的？' },
    { label: '送车上门', text: '可以送车上门吗？' },
  ]
  if (car.value) {
    replies.unshift(
      { label: '车辆咨询', text: `我想咨询一下${car.value.name}的详情` },
      { label: '价格优惠', text: `${car.value.name}现在有什么优惠活动吗？` },
    )
  }
  return replies
})

let msgIdCounter = 1000
let currentAbortController: AbortController | null = null

function now() {
  return new Date().toLocaleString('zh-CN', { hour: '2-digit', minute: '2-digit' })
}

function createLocalMsg(partial: Partial<ChatMessage>): ChatMessage {
  return {
    id: msgIdCounter++,
    conversationId: conversationId.value,
    userId: userStore.user?.id || 0,
    content: '',
    sender: 'user',
    timestamp: now(),
    ...partial,
  }
}

const defaultReply = '您好！我是畅行租车的在线客服，请问有什么可以帮您？'

async function scrollToBottom() {
  await nextTick()
  if (chatContainer.value) {
    chatContainer.value.scrollTop = chatContainer.value.scrollHeight
  }
}

async function initConversation() {
  try {
    if (userStore.isLoggedIn) {
      const conv = await createConversation()
      conversationId.value = conv.id
      if (conv.messages && conv.messages.length > 0) {
        messages.value = conv.messages
      }
    } else {
      conversationId.value = 'guest-' + Date.now()
    }
  } catch {
    conversationId.value = 'guest-' + Date.now()
  }

    messages.value = [
    createLocalMsg({
      id: 1,
      sender: 'system',
      content: car.value ? `正在咨询：${car.value.name}` : '畅行租车在线客服',
    }),
    createLocalMsg({
      id: 2,
      sender: 'agent',
      content: car.value
        ? `您好！我是畅行租车客服，看到您在浏览${car.value.name}，请问有什么可以帮您？`
        : defaultReply,
    }),
  ]
  isConnected.value = true
  scrollToBottom()
}

async function loadMyConversations() {
  if (!userStore.isLoggedIn) return
  try {
    const convs = await getUserConversations()
    myConversations.value = convs.map((c) => ({
      id: c.id,
      updatedAt: c.updatedAt,
      lastMessage: c.messages && c.messages.length > 0 ? c.messages[c.messages.length - 1]!.content : '暂无消息',
    }))
  } catch {
    myConversations.value = []
  }
}

async function send() {
  const text = inputText.value.trim()
  if (!text || isLoading.value) return

  const userMsg = createLocalMsg({ content: text, sender: 'user' })
  messages.value.push(userMsg)
  inputText.value = ''
  isLoading.value = true
  scrollToBottom()

  try {
    if (userStore.isLoggedIn && conversationId.value && !conversationId.value.startsWith('guest-')) {
      await sendMessage(conversationId.value, text)
    }
  } catch {
    // local-only mode
  }

  const agentMsg = createLocalMsg({ content: '', sender: 'agent' })
  messages.value.push(agentMsg)

  const token = localStorage.getItem('token') || undefined
  currentAbortController = await agentChatStream(
    conversationId.value,
    text,
    carId.value || undefined,
    carName.value || undefined,
    token,
    (chunk) => {
      agentMsg.content += chunk
      scrollToBottom()
    },
    () => {
      isLoading.value = false
      scrollToBottom()
    },
    (err) => {
      agentMsg.content = agentMsg.content || '抱歉，暂时无法回答您的问题，请稍后再试。'
      isLoading.value = false
      scrollToBottom()
    }
  )
}

function sendQuickReply(text: string) {
  inputText.value = text
  send()
}

function goBack() {
  if (window.history.length > 1) {
    router.back()
  } else {
    router.push('/')
  }
}

onMounted(() => {
  initConversation()
  loadMyConversations()
})

watch(() => messages.value.length, () => scrollToBottom())
</script>

<template>
  <div class="service-chat">
    <!-- Left Sidebar -->
    <aside class="chat-sidebar" :class="{ open: showSidebar }">
      <div class="sidebar-header">
        <h3>我的会话</h3>
        <button class="sidebar-close" @click="showSidebar = false">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M18 6 6 18M6 6l12 12"/></svg>
        </button>
      </div>
      <div class="conversation-list">
        <div v-if="myConversations.length === 0" class="empty-convs">
          <svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="#cbd5e1" stroke-width="1.5"><path d="M8 12h.01M12 12h.01M16 12h.01M21 12c0 4.418-4.03 8-9 8a9.863 9.863 0 01-4.255-.949L3 20l1.395-3.72C3.512 15.042 3 13.574 3 12c0-4.418 4.03-8 9-8s9 3.582 9 8z"/></svg>
          <span>暂无历史会话</span>
        </div>
        <div
          v-for="conv in myConversations"
          :key="conv.id"
          class="conv-item"
          :class="{ active: conv.id === conversationId }"
          @click="conversationId = conv.id; showSidebar = false"
        >
          <div class="conv-icon">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 15a2 2 0 01-2 2H7l-4 4V5a2 2 0 012-2h14a2 2 0 012 2z"/></svg>
          </div>
          <div class="conv-info">
            <span class="conv-id">会话 #{{ conv.id.slice(-6) }}</span>
            <span class="conv-last">{{ conv.lastMessage }}</span>
          </div>
        </div>
      </div>
    </aside>

    <!-- Overlay for mobile sidebar -->
    <div class="sidebar-overlay" v-if="showSidebar" @click="showSidebar = false"></div>

    <!-- Main Chat Area -->
    <div class="chat-main">
      <!-- Header -->
      <header class="chat-header">
        <div class="header-left">
          <button class="icon-btn" @click="goBack">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M19 12H5M12 19l-7-7 7-7"/></svg>
          </button>
          <button v-if="userStore.isLoggedIn" class="icon-btn sidebar-toggle" @click="showSidebar = !showSidebar">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 15a2 2 0 01-2 2H7l-4 4V5a2 2 0 012-2h14a2 2 0 012 2z"/></svg>
          </button>
        </div>
        <div class="header-center">
          <div class="agent-avatar-sm">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="#fff" stroke-width="2"><path d="M20 21v-2a4 4 0 00-4-4H8a4 4 0 00-4 4v2M12 3a4 4 0 110 8 4 4 0 010-8z"/></svg>
          </div>
          <div class="header-info">
            <span class="header-title">畅行租车客服</span>
            <span class="header-status">
              <span class="status-dot"></span>
              在线
            </span>
          </div>
        </div>
        <div class="header-right"></div>
      </header>

      <!-- Car Info Banner -->
      <div v-if="car" class="car-banner" @click="router.push(`/car/${car.id}`)">
        <img :src="car.image" :alt="car.name" class="car-banner-img" />
        <div class="car-banner-info">
          <span class="car-banner-name">{{ car.name }}</span>
          <span class="car-banner-price">¥{{ car.rentalPrice }}/天起</span>
        </div>
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="#94a3b8" stroke-width="2"><path d="M9 18l6-6-6-6"/></svg>
      </div>

      <!-- Messages -->
      <div class="messages-area" ref="chatContainer">
        <div
          v-for="msg in messages"
          :key="msg.id"
          :class="['msg-row', `sender-${msg.sender}`]"
        >
          <!-- Agent -->
          <template v-if="msg.sender === 'agent'">
            <div class="msg-avatar agent-avatar">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="#fff" stroke-width="2"><path d="M20 21v-2a4 4 0 00-4-4H8a4 4 0 00-4 4v2M12 3a4 4 0 110 8 4 4 0 010-8z"/></svg>
            </div>
            <div class="msg-content-wrap">
              <div class="msg-bubble agent-bubble">{{ msg.content }}</div>
              <span class="msg-time">{{ msg.timestamp }}</span>
            </div>
          </template>

          <!-- User -->
          <template v-else-if="msg.sender === 'user'">
            <div class="msg-content-wrap">
              <div class="msg-bubble user-bubble">{{ msg.content }}</div>
              <span class="msg-time">{{ msg.timestamp }}</span>
            </div>
            <div class="msg-avatar user-avatar">
              {{ userStore.user?.name?.charAt(0) || '我' }}
            </div>
          </template>

          <!-- System -->
          <template v-else>
            <div class="msg-system">{{ msg.content }}</div>
          </template>
        </div>

        <!-- Typing indicator -->
        <div v-if="isLoading" class="msg-row sender-agent">
          <div class="msg-avatar agent-avatar">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="#fff" stroke-width="2"><path d="M20 21v-2a4 4 0 00-4-4H8a4 4 0 00-4 4v2M12 3a4 4 0 110 8 4 4 0 010-8z"/></svg>
          </div>
          <div class="msg-content-wrap">
            <div class="msg-bubble agent-bubble typing-bubble">
              <span class="dot"></span>
              <span class="dot"></span>
              <span class="dot"></span>
            </div>
          </div>
        </div>
      </div>

      <!-- Quick Replies -->
      <div class="quick-replies" v-if="messages.length <= 3">
        <button
          v-for="qr in quickReplies"
          :key="qr.label"
          class="qr-btn"
          @click="sendQuickReply(qr.text)"
        >
          {{ qr.label }}
        </button>
      </div>

      <!-- Input Area -->
      <div class="input-area">
        <div class="input-wrap">
          <input
            v-model="inputText"
            type="text"
            placeholder="输入您的问题..."
            class="chat-input"
            @keyup.enter="send"
          />
          <button
            class="send-btn"
            :class="{ active: inputText.trim() }"
            :disabled="!inputText.trim() || isLoading"
            @click="send"
          >
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M22 2L11 13M22 2l-7 20-4-9-9-4 20-7z"/></svg>
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.service-chat {
  display: flex;
  height: 100vh;
  background: #f1f5f9;
  overflow: hidden;
}

/* Sidebar */
.chat-sidebar {
  width: 280px;
  background: #fff;
  border-right: 1px solid #e2e8f0;
  display: flex;
  flex-direction: column;
  flex-shrink: 0;
}
.sidebar-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  border-bottom: 1px solid #f1f5f9;
}
.sidebar-header h3 {
  font-size: 15px;
  font-weight: 700;
  color: #0f172a;
}
.sidebar-close {
  display: none;
  background: none;
  border: none;
  color: #64748b;
  cursor: pointer;
  padding: 4px;
  border-radius: 6px;
}
.conversation-list {
  flex: 1;
  overflow-y: auto;
  padding: 8px;
}
.empty-convs {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 40px 20px;
  color: #94a3b8;
  font-size: 13px;
}
.conv-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px;
  border-radius: 10px;
  cursor: pointer;
  transition: 0.15s;
}
.conv-item:hover {
  background: #f8fafc;
}
.conv-item.active {
  background: #f0fdfa;
}
.conv-icon {
  width: 32px;
  height: 32px;
  border-radius: 8px;
  background: #f1f5f9;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #64748b;
  flex-shrink: 0;
}
.conv-item.active .conv-icon {
  background: #0891b2;
  color: #fff;
}
.conv-info {
  display: flex;
  flex-direction: column;
  min-width: 0;
}
.conv-id {
  font-size: 12px;
  font-weight: 600;
  color: #0f172a;
}
.conv-last {
  font-size: 12px;
  color: #94a3b8;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* Overlay */
.sidebar-overlay {
  display: none;
}

/* Main */
.chat-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
}

/* Header */
.chat-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 20px;
  background: #fff;
  border-bottom: 1px solid #e2e8f0;
  flex-shrink: 0;
}
.header-left,
.header-right {
  width: 48px;
  display: flex;
  align-items: center;
}
.icon-btn {
  background: none;
  border: none;
  color: #64748b;
  cursor: pointer;
  padding: 8px;
  border-radius: 10px;
  transition: 0.15s;
  display: flex;
  align-items: center;
  justify-content: center;
}
.icon-btn:hover {
  background: #f1f5f9;
  color: #0f172a;
}
.sidebar-toggle {
  display: none;
}
.header-center {
  display: flex;
  align-items: center;
  gap: 10px;
}
.agent-avatar-sm {
  width: 34px;
  height: 34px;
  border-radius: 10px;
  background: linear-gradient(135deg, #0891b2, #06b6d4);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.header-info {
  display: flex;
  flex-direction: column;
}
.header-title {
  font-size: 14px;
  font-weight: 700;
  color: #0f172a;
}
.header-status {
  display: flex;
  align-items: center;
  gap: 5px;
  font-size: 12px;
  color: #16a34a;
}
.status-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #16a34a;
}

/* Car Banner */
.car-banner {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 20px;
  background: linear-gradient(135deg, #f0fdfa, #ecfeff);
  border-bottom: 1px solid #ccfbf1;
  cursor: pointer;
  transition: 0.15s;
  flex-shrink: 0;
}
.car-banner:hover {
  background: linear-gradient(135deg, #e0f9f5, #daf8fc);
}
.car-banner-img {
  width: 48px;
  height: 34px;
  border-radius: 8px;
  object-fit: cover;
  background: #e2e8f0;
}
.car-banner-info {
  display: flex;
  flex-direction: column;
  flex: 1;
}
.car-banner-name {
  font-size: 13px;
  font-weight: 700;
  color: #0f172a;
}
.car-banner-price {
  font-size: 12px;
  color: #0891b2;
  font-weight: 600;
}

/* Messages */
.messages-area {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.msg-row {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  max-width: 80%;
}
.msg-row.sender-user {
  align-self: flex-end;
  flex-direction: row;
}
.msg-row.sender-agent {
  align-self: flex-start;
}
.msg-row.sender-system {
  align-self: center;
}

.msg-avatar {
  width: 34px;
  height: 34px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  font-size: 13px;
  font-weight: 700;
  color: #fff;
}
.agent-avatar {
  background: linear-gradient(135deg, #0891b2, #06b6d4);
}
.user-avatar {
  background: linear-gradient(135deg, #6366f1, #818cf8);
}

.msg-content-wrap {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.msg-bubble {
  padding: 12px 16px;
  border-radius: 16px;
  font-size: 14px;
  line-height: 1.6;
  word-break: break-word;
}
.agent-bubble {
  background: #fff;
  color: #1e293b;
  border: 1px solid #e2e8f0;
  border-top-left-radius: 4px;
}
.user-bubble {
  background: linear-gradient(135deg, #0891b2, #06b6d4);
  color: #fff;
  border-top-right-radius: 4px;
}

.msg-time {
  font-size: 11px;
  color: #94a3b8;
  padding: 0 4px;
}
.sender-user .msg-time {
  text-align: right;
}

.msg-system {
  font-size: 12px;
  color: #94a3b8;
  background: rgba(148, 163, 184, 0.1);
  padding: 6px 16px;
  border-radius: 100px;
}

/* Typing */
.typing-bubble {
  display: flex;
  gap: 4px;
  padding: 14px 20px;
}
.typing-bubble .dot {
  width: 7px;
  height: 7px;
  border-radius: 50%;
  background: #94a3b8;
  animation: typingBounce 1.2s infinite;
}
.typing-bubble .dot:nth-child(2) {
  animation-delay: 0.2s;
}
.typing-bubble .dot:nth-child(3) {
  animation-delay: 0.4s;
}
@keyframes typingBounce {
  0%, 60%, 100% { transform: translateY(0); opacity: 0.4; }
  30% { transform: translateY(-4px); opacity: 1; }
}

/* Quick Replies */
.quick-replies {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  padding: 12px 20px;
  flex-shrink: 0;
}
.qr-btn {
  padding: 8px 16px;
  border: 1px solid #e2e8f0;
  border-radius: 100px;
  background: #fff;
  color: #475569;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: 0.15s;
}
.qr-btn:hover {
  border-color: #0891b2;
  color: #0891b2;
  background: #f0fdfa;
}

/* Input */
.input-area {
  padding: 16px 20px;
  background: #fff;
  border-top: 1px solid #e2e8f0;
  flex-shrink: 0;
}
.input-wrap {
  display: flex;
  align-items: center;
  gap: 10px;
  background: #f8fafc;
  border: 1.5px solid #e2e8f0;
  border-radius: 14px;
  padding: 6px 6px 6px 18px;
  transition: 0.2s;
}
.input-wrap:focus-within {
  border-color: #0891b2;
  box-shadow: 0 0 0 3px rgba(8, 145, 178, 0.1);
}
.chat-input {
  flex: 1;
  border: none;
  background: none;
  outline: none;
  font-size: 14px;
  color: #0f172a;
  padding: 8px 0;
}
.chat-input::placeholder {
  color: #94a3b8;
}
.send-btn {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  border: none;
  background: #e2e8f0;
  color: #94a3b8;
  cursor: not-allowed;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: 0.2s;
  flex-shrink: 0;
}
.send-btn.active {
  background: linear-gradient(135deg, #0891b2, #06b6d4);
  color: #fff;
  cursor: pointer;
}
.send-btn.active:hover {
  transform: scale(1.05);
  box-shadow: 0 4px 12px rgba(8, 145, 178, 0.3);
}

/* Responsive */
@media (max-width: 768px) {
  .chat-sidebar {
    position: fixed;
    left: -280px;
    top: 0;
    bottom: 0;
    z-index: 200;
    transition: 0.3s;
  }
  .chat-sidebar.open {
    left: 0;
  }
  .sidebar-close {
    display: flex;
  }
  .sidebar-overlay {
    display: block;
    position: fixed;
    inset: 0;
    background: rgba(0, 0, 0, 0.3);
    z-index: 150;
  }
  .sidebar-toggle {
    display: flex;
  }
  .msg-row {
    max-width: 90%;
  }
}
</style>
