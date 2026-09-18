<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAdminStore } from '@/stores/admin'

const router = useRouter()
const adminStore = useAdminStore()

const phone = ref('')
const password = ref('')
const error = ref('')
const loading = ref(false)

function handleLogin() {
  error.value = ''
  if (!phone.value || !password.value) {
    error.value = '请输入账号和密码'
    return
  }
  loading.value = true
  adminStore.login(phone.value, password.value).then(success => {
    loading.value = false
    if (success) {
      router.push('/admin')
    } else {
      error.value = '账号或密码错误'
    }
  })
}
</script>

<template>
  <div class="login-page">
    <div class="login-bg">
      <div class="bg-shape s1"></div>
      <div class="bg-shape s2"></div>
    </div>
    <div class="login-card">
      <div class="card-header">
        <div class="logo-box">
          <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><path d="M12 15V3m0 12l-4-4m4 4l4-4M2 17l.621 2.485A2 2 0 004.561 21h14.878a2 2 0 001.94-1.515L22 17"/></svg>
        </div>
        <h1>管理后台</h1>
        <p>畅行租车管理系统</p>
      </div>

      <form class="login-form" @submit.prevent="handleLogin">
        <div class="form-group">
          <label>管理员账号</label>
          <input v-model="phone" type="text" placeholder="请输入账号" autocomplete="username" />
        </div>
        <div class="form-group">
          <label>密码</label>
          <input v-model="password" type="password" placeholder="请输入密码" autocomplete="current-password" />
        </div>
        <div class="error-msg" v-if="error">{{ error }}</div>
        <button type="submit" class="login-btn" :disabled="loading">
          <span v-if="loading">登录中...</span>
          <span v-else>登 录</span>
        </button>
      </form>

      <div class="login-footer">
        <router-link to="/" class="back-link">← 返回用户端</router-link>
      </div>
    </div>
  </div>
</template>

<style scoped>
.login-page {
  min-height: 100vh; display: flex; align-items: center; justify-content: center;
  background: #0f172a; position: relative; overflow: hidden;
}

.login-bg { position: absolute; inset: 0; }
.bg-shape { position: absolute; border-radius: 50%; }
.bg-shape.s1 { width: 600px; height: 600px; top: -200px; right: -100px; background: radial-gradient(circle, rgba(8,145,178,0.15) 0%, transparent 70%); }
.bg-shape.s2 { width: 400px; height: 400px; bottom: -150px; left: -50px; background: radial-gradient(circle, rgba(139,92,246,0.1) 0%, transparent 70%); }

.login-card {
  position: relative; width: 400px; background: #fff;
  border-radius: 20px; padding: 48px 40px;
  box-shadow: 0 24px 64px rgba(0,0,0,0.3);
}

.card-header { text-align: center; margin-bottom: 36px; }
.logo-box {
  width: 56px; height: 56px; border-radius: 14px; margin: 0 auto 16px;
  background: linear-gradient(135deg, #0891b2, #06b6d4);
  color: #fff; display: flex; align-items: center; justify-content: center;
}
.card-header h1 { font-size: 24px; font-weight: 800; color: #0f172a; margin-bottom: 4px; }
.card-header p { font-size: 14px; color: #94a3b8; }

.form-group { margin-bottom: 20px; }
.form-group label {
  display: block; font-size: 13px; font-weight: 600;
  color: #334155; margin-bottom: 6px;
}
.form-group input {
  width: 100%; padding: 12px 16px; border: 1.5px solid #e2e8f0;
  border-radius: 10px; font-size: 14px; outline: none; transition: 0.15s;
}
.form-group input:focus { border-color: #0891b2; box-shadow: 0 0 0 3px rgba(8,145,178,0.1); }

.error-msg {
  color: #ef4444; font-size: 13px; margin-bottom: 16px;
  padding: 10px 14px; background: #fef2f2; border-radius: 8px;
}

.login-btn {
  width: 100%; padding: 13px; border: none; border-radius: 10px;
  background: linear-gradient(135deg, #0891b2, #06b6d4);
  color: #fff; font-size: 15px; font-weight: 700;
  cursor: pointer; transition: 0.2s;
}
.login-btn:hover { transform: translateY(-1px); box-shadow: 0 6px 20px rgba(8,145,178,0.35); }
.login-btn:disabled { opacity: 0.7; cursor: not-allowed; transform: none; }

.login-footer { text-align: center; margin-top: 24px; }
.back-link {
  font-size: 13px; color: #94a3b8; text-decoration: none; transition: 0.15s;
}
.back-link:hover { color: #0891b2; }
</style>
