<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()
const phone = ref('')
const password = ref('')
const errorMsg = ref('')
const loading = ref(false)

async function login() {
  errorMsg.value = ''
  if (!phone.value || !password.value) {
    errorMsg.value = '请输入手机号和密码'
    return
  }
  loading.value = true
  const success = await userStore.login(phone.value, password.value)
  loading.value = false
  if (success) {
    router.push('/')
  } else {
    errorMsg.value = userStore.loginError || '登录失败'
  }
}
</script>

<template>
  <div class="auth-page">
    <div class="auth-card">
      <div class="auth-icon blue"><svg width="26" height="26" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M15 3h4a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2h-4"/><polyline points="10 17 15 12 10 7"/><line x1="15" y1="12" x2="3" y2="12"/></svg></div>
      <h1>欢迎回来</h1>
      <p class="sub">登录您的畅行租车账户</p>

      <form @submit.prevent="login">
        <div class="fg"><label>手机号</label><input v-model="phone" type="tel" placeholder="请输入手机号" maxlength="11" required /></div>
        <div class="fg"><label>密码</label><input v-model="password" type="password" placeholder="请输入密码" required /></div>
        <div class="error-msg" v-if="errorMsg">{{ errorMsg }}</div>
        <div class="form-row"><label class="check"><input type="checkbox" /><span>记住我</span></label><a href="#" class="link">忘记密码？</a></div>
        <button type="submit" class="btn-submit" :disabled="loading">
          <span v-if="loading">登录中...</span>
          <span v-else>登录</span>
        </button>
      </form>

      <div class="divider"><span>或</span></div>

      <button class="btn-wechat">
        <svg width="20" height="20" viewBox="0 0 24 24" fill="#07c160"><path d="M8.691 2.188C3.891 2.188 0 5.476 0 9.53c0 2.212 1.17 4.203 3.002 5.55a.59.59 0 0 1 .213.665l-.39 1.48c-.019.07-.048.141-.048.213 0 .163.13.295.29.295a.326.326 0 0 0 .167-.054l1.903-1.114a.864.864 0 0 1 .717-.098 10.16 10.16 0 0 0 2.837.403c.276 0 .543-.027.811-.05-.857-2.578.157-4.972 1.932-6.446 1.703-1.415 3.882-1.98 5.853-1.838-.576-3.583-4.196-6.348-8.596-6.348z"/></svg>
        微信登录
      </button>

      <p class="switch">还没有账户？<router-link to="/register">免费注册</router-link></p>
      <router-link to="/admin/login" class="admin-link">管理员入口</router-link>
    </div>
  </div>
</template>

<style scoped>
.auth-page { min-height: calc(100vh - 64px); display: flex; align-items: center; justify-content: center; background: linear-gradient(135deg, #f8fafc, #e2e8f0); padding: 40px 24px; }
.auth-card { width: 100%; max-width: 420px; background: #fff; border-radius: 20px; padding: 44px 36px; box-shadow: 0 4px 24px rgba(0,0,0,0.06); text-align: center; }
.auth-icon { width: 56px; height: 56px; border-radius: 14px; display: flex; align-items: center; justify-content: center; margin: 0 auto 20px; }
.auth-icon.blue { background: #f0fdfa; color: #0891b2; }
.auth-card h1 { font-size: 22px; font-weight: 800; color: #1a1a2e; margin-bottom: 6px; }
.sub { color: #94a3b8; font-size: 14px; margin-bottom: 28px; }
.fg { margin-bottom: 16px; text-align: left; }
.fg label { display: block; font-size: 12px; font-weight: 600; color: #475569; margin-bottom: 6px; }
.fg input { width: 100%; padding: 12px 14px; border: 1.5px solid #e2e8f0; border-radius: 10px; font-size: 14px; outline: none; transition: 0.15s; box-sizing: border-box; }
.fg input:focus { border-color: #0891b2; box-shadow: 0 0 0 3px rgba(8,145,178,0.1); }
.fg input::placeholder { color: #94a3b8; }
.error-msg {
  color: #ef4444; font-size: 13px; margin-bottom: 16px;
  padding: 10px 14px; background: #fef2f2; border-radius: 8px; text-align: left;
}
.form-row { display: flex; justify-content: space-between; margin-bottom: 24px; }
.check { display: flex; align-items: center; gap: 6px; font-size: 13px; color: #64748b; cursor: pointer; }
.check input { accent-color: #0891b2; }
.link { font-size: 13px; color: #0891b2; text-decoration: none; font-weight: 500; }
.btn-submit { width: 100%; background: linear-gradient(135deg, #0891b2, #06b6d4); color: #fff; border: none; padding: 13px; border-radius: 10px; font-size: 15px; font-weight: 700; cursor: pointer; transition: 0.2s; }
.btn-submit:hover:not(:disabled) { transform: translateY(-1px); box-shadow: 0 6px 20px rgba(8,145,178,0.35); }
.btn-submit:disabled { opacity: 0.7; cursor: not-allowed; }
.divider { display: flex; align-items: center; margin: 20px 0; }
.divider::before, .divider::after { content: ''; flex: 1; height: 1px; background: #e2e8f0; }
.divider span { padding: 0 14px; font-size: 12px; color: #94a3b8; }
.btn-wechat { width: 100%; display: flex; align-items: center; justify-content: center; gap: 8px; background: #fff; border: 1.5px solid #e2e8f0; padding: 11px; border-radius: 10px; font-size: 13px; font-weight: 500; color: #334155; cursor: pointer; transition: 0.15s; }
.btn-wechat:hover { border-color: #07c160; background: #f0fdf4; }
.switch { margin-top: 24px; font-size: 13px; color: #94a3b8; }
.switch a { color: #0891b2; text-decoration: none; font-weight: 600; }
.admin-link {
  display: block; margin-top: 16px; font-size: 11px; color: #cbd5e1;
  text-decoration: none; transition: 0.15s;
}
.admin-link:hover { color: #94a3b8; }
</style>
