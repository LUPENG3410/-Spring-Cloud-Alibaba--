import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { Car, CarDetail } from '@/types'
import { useApi } from '@/api/service'

export type { CarDetail }

export const useCarStore = defineStore('car', () => {
  const api = useApi()
  const cars = ref<Car[]>([])
  const carDetails = ref<Record<number, CarDetail>>({})
  const loading = ref(false)

  async function loadCars() {
    loading.value = true
    cars.value = await api.getCars()
    loading.value = false
  }

  async function loadCarDetail(id: number): Promise<CarDetail | undefined> {
    if (carDetails.value[id]) return carDetails.value[id]
    loading.value = true
    const detail = await api.getCarById(id)
    carDetails.value[id] = detail
    loading.value = false
    return detail
  }

  const searchQuery = ref('')
  const selectedType = ref<string>('all')
  const selectedProvince = ref<string>('all')
  const currentPage = ref(1)
  const pageSize = 20

  const provinces = computed(() => {
    const set = new Set(cars.value.map(c => c.province).filter(Boolean) as string[])
    return Array.from(set).sort()
  })

  const filteredCars = computed(() => {
    return cars.value.filter((car) => {
      const matchesSearch =
        car.name.toLowerCase().includes(searchQuery.value.toLowerCase()) ||
        car.brand.toLowerCase().includes(searchQuery.value.toLowerCase())
      const matchesType = selectedType.value === 'all' || car.type === selectedType.value
      const matchesProvince = selectedProvince.value === 'all' || car.province === selectedProvince.value
      return matchesSearch && matchesType && matchesProvince
    })
  })

  const totalPages = computed(() => Math.max(1, Math.ceil(filteredCars.value.length / pageSize)))

  const paginatedCars = computed(() => {
    const start = (currentPage.value - 1) * pageSize
    return filteredCars.value.slice(start, start + pageSize)
  })

  function resetPage() {
    currentPage.value = 1
  }

  function getCarById(id: number) {
    return cars.value.find((car) => car.id === id)
  }

  function getCarDetail(id: number): CarDetail | undefined {
    return carDetails.value[id]
  }

  return { cars, carDetails, loading, loadCars, loadCarDetail, searchQuery, selectedType, selectedProvince, currentPage, pageSize, provinces, filteredCars, totalPages, paginatedCars, resetPage, getCarById, getCarDetail }
})
