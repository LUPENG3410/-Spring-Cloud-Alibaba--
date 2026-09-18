<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()
const name = ref('')
const phone = ref('')
const password = ref('')
const confirmPassword = ref('')
const errorMsg = ref('')
const loading = ref(false)

async function register() {
  errorMsg.value = ''
  if (!name.value || !phone.value || !password.value || !confirmPassword.value) {
    errorMsg.value = '请填写所有字段'
    return
  }
  if (password.value !== confirmPassword.value) {
    errorMsg.value = '两次密码输入不一致'
    return
  }
  loading.value = true
  try {
    await userStore.register(name.value, phone.value, password.value)
    router.push('/login')
  } catch (e: any) {
    errorMsg.value = e?.response?.data?.message || e?.message || '注册失败，请稍后重试'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="auth-page">
    <div class="auth-card">
      <div class="auth-icon green"><svg width="26" height="26" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><line x1="19" y1="8" x2="19" y2="14"/><line x1="22" y1="11" x2="16" y2="11"/></svg></div>
      <h1>创建账户</h1>
      <p class="sub">加入畅行租车，开启便捷出行</p>

      <form @submit.prevent="register">
        <div class="fg"><label>姓名</label><input v-model="name" type="text" placeholder="请输入姓名" required /></div>
        <div class="fg"><label>手机号</label><input v-model="phone" type="tel" placeholder="请输入手机号" maxlength="11" required /></div>
        <div class="fg"><label>密码</label><input v-model="password" type="password" placeholder="6-20位密码" required /></div>
        <div class="fg"><label>确认密码</label><input v-model="confirmPassword" type="password" placeholder="再次输入密码" required /></div>
        <div class="error-msg" v-if="errorMsg">{{ errorMsg }}</div>
        <label class="terms"><input type="checkbox" required /><span>同意<a href="#">《用户协议》</a>和<a href="#">《隐私政策》</a></span></label>
        <button type="submit" class="btn-submit" :disabled="loading">
          <span v-if="loading">注册中...</span>
          <span v-else>注册</span>
        </button>
      </form>

      <p class="switch">已有账户？<router-link to="/login">立即登录</router-link></p>
    </div>
  </div>
</template>

<style scoped>
.auth-page { min-height: calc(100vh - 64px); display: flex; align-items: center; justify-content: center; background: linear-gradient(135deg, #f8fafc, #e2e8f0); padding: 40px 24px; }
.auth-card { width: 100%; max-width: 420px; background: #fff; border-radius: 20px; padding: 44px 36px; box-shadow: 0 4px 24px rgba(0,0,0,0.06); text-align: center; }
.auth-icon { width: 56px; height: 56px; border-radius: 14px; display: flex; align-items: center; justify-content: center; margin: 0 auto 20px; }
.auth-icon.green { background: #f0fdf4; color: #16a34a; }
.auth-card h1 { font-size: 22px; font-weight: 800; color: #1a1a2e; margin-bottom: 6px; }
.sub { color: #94a3b8; font-size: 14px; margin-bottom: 28px; }
.fg { margin-bottom: 16px; text-align: left; }
.fg label { display: block; font-size: 12px; font-weight: 600; color: #475569; margin-bottom: 6px; }
.fg input { width: 100%; padding: 12px 14px; border: 1.5px solid #e2e8f0; border-radius: 10px; font-size: 14px; outline: none; transition: 0.15s; box-sizing: border-box; }
.fg input:focus { border-color: #0891b2; box-shadow: 0 0 0 3px rgba(8,145,178,0.1); }
.fg input::placeholder { color: #94a3b8; }
.terms { display: flex; align-items: flex-start; gap: 8px; margin: 20px 0; font-size: 13px; color: #64748b; cursor: pointer; line-height: 1.5; text-align: left; }
.terms input { margin-top: 2px; accent-color: #0891b2; }
.terms a { color: #0891b2; text-decoration: none; }
.error-msg {
  color: #ef4444; font-size: 13px; margin-bottom: 16px;
  padding: 10px 14px; background: #fef2f2; border-radius: 8px; text-align: left;
}
.btn-submit { width: 100%; background: linear-gradient(135deg, #0891b2, #06b6d4); color: #fff; border: none; padding: 13px; border-radius: 10px; font-size: 15px; font-weight: 700; cursor: pointer; transition: 0.2s; }
.btn-submit:hover:not(:disabled) { transform: translateY(-1px); box-shadow: 0 6px 20px rgba(8,145,178,0.35); }
.btn-submit:disabled { opacity: 0.7; cursor: not-allowed; }
.switch { margin-top: 24px; font-size: 13px; color: #94a3b8; }
.switch a { color: #0891b2; text-decoration: none; font-weight: 600; }
</style>
