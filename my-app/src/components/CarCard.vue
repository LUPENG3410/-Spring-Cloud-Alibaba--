<script setup lang="ts">
import type { Car } from '@/types'
import { useRouter } from 'vue-router'

defineProps<{ car: Car }>()
const router = useRouter()

const typeMap: Record<string, string> = { sedan: '轿车', suv: 'SUV', mpv: 'MPV', luxury: '豪华', sports: '跑车' }
const typeColor: Record<string, string> = { sedan: '#2563eb', suv: '#059669', mpv: '#7c3aed', luxury: '#d97706', sports: '#dc2626' }
</script>

<template>
  <div class="car-card" @click="router.push(`/car/${car.id}/${car.province || ''}`)">
    <div class="card-img">
      <img :src="car.image" :alt="car.name" />
      <div class="img-overlay">
        <span class="tag" :style="{ background: typeColor[car.type] }">{{ typeMap[car.type] }}</span>
        <span class="province-tag">{{ car.province }}</span>
      </div>
    </div>
    <div class="card-body">
      <div class="card-top">
        <h3>{{ car.name }}</h3>
        <span class="brand">{{ car.brand }}</span>
      </div>
      <div class="specs">
        <span>{{ car.seats }}座</span>
        <span>{{ car.transmission }}</span>
        <span>{{ car.fuel }}</span>
      </div>
      <div class="store-info">
        <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="#0891b2" stroke-width="2"><path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0118 0z"/><circle cx="12" cy="10" r="3"/></svg>
        <span class="store-name">{{ car.province }}</span>
      </div>
      <div class="card-bottom">
        <div class="price">
          <span class="sym">¥</span><span class="num">{{ car.rentalPrice }}</span><span class="unit">/天</span>
        </div>
        <button class="book-btn">预订</button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.car-card {
  background: #fff; border-radius: 16px; overflow: hidden;
  border: 1px solid #f1f5f9; cursor: pointer;
  transition: all 0.25s cubic-bezier(0.4,0,0.2,1);
}
.car-card:hover { transform: translateY(-6px); box-shadow: 0 20px 40px rgba(0,0,0,0.1); border-color: transparent; }
.card-img { position: relative; height: 180px; overflow: hidden; background: #f8fafc; }
.card-img img { width: 100%; height: 100%; object-fit: cover; transition: transform 0.4s; }
.car-card:hover .card-img img { transform: scale(1.08); }
.img-overlay {
  position: absolute; inset: 0;
  background: linear-gradient(180deg, rgba(0,0,0,0.3) 0%, transparent 40%, transparent 70%, rgba(0,0,0,0.4) 100%);
  display: flex; justify-content: space-between; align-items: flex-start; padding: 12px;
}
.tag { padding: 4px 10px; border-radius: 6px; color: #fff; font-size: 11px; font-weight: 700; }
.province-tag { background: rgba(255,255,255,0.9); color: #334155; padding: 4px 10px; border-radius: 6px; font-size: 11px; font-weight: 600; }
.card-body { padding: 16px 18px 18px; }
.card-top { display: flex; align-items: baseline; gap: 8px; margin-bottom: 10px; }
.card-top h3 { font-size: 16px; font-weight: 700; color: #0f172a; }
.brand { font-size: 12px; color: #94a3b8; }
.specs { display: flex; gap: 10px; margin-bottom: 10px; }
.specs span { font-size: 11px; color: #64748b; font-weight: 500; background: #f8fafc; padding: 4px 8px; border-radius: 4px; }
.store-info { display: flex; align-items: center; gap: 6px; margin-bottom: 14px; }
.store-name { font-size: 12px; color: #0891b2; font-weight: 500; }
.card-bottom { display: flex; align-items: center; justify-content: space-between; }
.price { display: flex; align-items: baseline; }
.sym { font-size: 13px; font-weight: 700; color: #ef4444; }
.num { font-size: 24px; font-weight: 800; color: #ef4444; line-height: 1; }
.unit { font-size: 11px; color: #94a3b8; margin-left: 2px; }
.book-btn {
  background: linear-gradient(135deg, #0891b2, #06b6d4);
  color: #fff; border: none; padding: 8px 20px; border-radius: 8px;
  font-size: 12px; font-weight: 600; cursor: pointer; transition: 0.2s;
}
.book-btn:hover { transform: scale(1.05); box-shadow: 0 4px 12px rgba(8,145,178,0.35); }
</style>
