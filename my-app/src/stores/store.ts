import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { StoreLocation } from '@/types'
import request from '@/api/request'

export const useStoreStore = defineStore('store', () => {
  const stores = ref<StoreLocation[]>([])
  const selectedProvinceFilter = ref('all')
  const filteredStores = ref<StoreLocation[]>([])
  const loading = ref(false)

  const activeStores = computed(() => stores.value.filter(s => s.status === 'active'))

  async function fetchStores() {
    loading.value = true
    try {
      const res = await request.get('/stores')
      const data = res.data.data || []
      stores.value = data.map((s: any) => ({
        id: s.id,
        name: s.name,
        province: s.province,
        city: s.city,
        address: s.address,
        phone: s.phone,
        lat: Number(s.lat),
        lng: Number(s.lng),
        hours: s.hours,
        status: s.status === 1 ? 'active' : 'closed'
      }))
      filterStores(selectedProvinceFilter.value)
    } catch (e) {
      console.warn('从API获取门店失败，使用本地数据')
    } finally {
      loading.value = false
    }
  }

  function filterStores(province: string) {
    selectedProvinceFilter.value = province
    const base = stores.value.filter(s => s.status === 'active')
    filteredStores.value = province === 'all' ? base : base.filter(s => s.province === province)
  }

  function getStoreById(id: number) {
    return stores.value.find(s => s.id === id)
  }

  fetchStores()

  return { stores, activeStores, selectedProvinceFilter, filteredStores, loading, fetchStores, filterStores, getStoreById }
})
