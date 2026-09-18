<script setup lang="ts">
import { ref, onMounted } from 'vue'

const footerRef = ref<HTMLElement | null>(null)

onMounted(() => {
  if (!footerRef.value) return
  const observer = new IntersectionObserver(
    (entries) => {
      entries.forEach((entry) => {
        if (entry.isIntersecting) {
          entry.target.classList.add('footer-visible')
          observer.unobserve(entry.target)
        }
      })
    },
    { threshold: 0.1 }
  )
  observer.observe(footerRef.value)
})
</script>

<template>
  <footer class="footer footer-hidden" ref="footerRef">
    <div class="footer-inner">
      <div class="footer-grid">
        <div class="footer-col brand-col">
          <div class="f-logo">
            <div class="f-logo-box">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><path d="M19 17h2c.6 0 1-.4 1-1v-3c0-.9-.7-1.7-1.5-1.9C18.7 10.6 16 10 16 10s-1.3-1.4-2.2-2.3c-.5-.4-1.1-.7-1.8-.7H5c-.6 0-1.1.4-1.4.9l-1.4 2.9A3.7 3.7 0 0 0 2 12v4c0 .6.4 1 1 1h2"/><circle cx="7" cy="17" r="2"/><circle cx="17" cy="17" r="2"/></svg>
            </div>
            <span>畅行租车</span>
          </div>
          <p>全国连锁汽车租赁品牌，覆盖30+省份，200+门店，为您提供便捷、安全、实惠的租车服务。</p>
          <div class="contact">
            <span>400-888-8888</span>
            <span>service@changxing.com</span>
          </div>
        </div>
        <div class="footer-col">
          <h4>租车服务</h4>
          <a href="/cars">全部车辆</a>
          <a href="/cross-province">跨省租车</a>
          <a href="/stores">网点查询</a>
          <a href="#">企业用车</a>
        </div>
        <div class="footer-col">
          <h4>帮助中心</h4>
          <a href="#">租车指南</a>
          <a href="#">保险说明</a>
          <a href="#">押金退还</a>
          <a href="#">常见问题</a>
        </div>
        <div class="footer-col">
          <h4>关于我们</h4>
          <a href="#">公司介绍</a>
          <a href="#">加入我们</a>
          <a href="#">商务合作</a>
          <a href="#">联系客服</a>
        </div>
      </div>
      <div class="footer-bottom">
        <span>&copy; 2026 畅行租车 版权所有</span>
        <div class="bottom-links">
          <a href="#">用户协议</a>
          <a href="#">隐私政策</a>
          <a href="#">营业执照</a>
        </div>
      </div>
    </div>
  </footer>
</template>

<style scoped>
.footer { background: #0f172a; transition: opacity 0.6s ease-out, transform 0.6s ease-out; }
.footer-hidden { opacity: 0; transform: translateY(20px); }
.footer-visible { opacity: 1; transform: translateY(0); }
.footer-inner { max-width: 1280px; margin: 0 auto; padding: 56px 32px 0; }
.footer-grid {
  display: grid;
  grid-template-columns: 1.8fr 1fr 1fr 1fr;
  gap: 48px;
  padding-bottom: 40px;
  border-bottom: 1px solid #1e293b;
}
.f-logo { display: flex; align-items: center; gap: 10px; margin-bottom: 16px; }
.f-logo-box {
  width: 32px; height: 32px;
  background: linear-gradient(135deg, #0891b2, #06b6d4);
  color: #fff; border-radius: 8px;
  display: flex; align-items: center; justify-content: center;
}
.f-logo span { font-size: 16px; font-weight: 700; color: #fff; }
.brand-col p { font-size: 13px; color: #64748b; line-height: 1.8; margin-bottom: 16px; }
.contact { display: flex; flex-direction: column; gap: 6px; font-size: 13px; color: #64748b; }
.footer-col h4 { color: #f1f5f9; font-size: 14px; font-weight: 600; margin-bottom: 18px; }
.footer-col a {
  display: block; color: #64748b; font-size: 13px;
  text-decoration: none; padding: 5px 0; transition: 0.15s;
}
.footer-col a:hover { color: #0891b2; }
.footer-bottom {
  display: flex; justify-content: space-between; align-items: center;
  padding: 20px 0; font-size: 12px; color: #475569;
}
.bottom-links { display: flex; gap: 24px; }
.bottom-links a { color: #475569; text-decoration: none; transition: 0.15s; }
.bottom-links a:hover { color: #94a3b8; }
</style>
