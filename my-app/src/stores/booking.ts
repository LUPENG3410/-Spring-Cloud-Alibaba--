import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { Booking } from '@/types'
import { useApi } from '@/api/service'

export const useBookingStore = defineStore('booking', () => {
  const api = useApi()

  const bookings = ref<Booking[]>([])

  async function loadBookings() {
    try {
      bookings.value = await api.getBookings()
    } catch (e) {
      console.warn('加载订单失败:', e)
    }
  }

  async function addBooking(booking: Omit<Booking, 'id' | 'status'>) {
    const result = await api.addBooking(booking)
    bookings.value.push(result)
  }

  async function cancelBooking(id: number) {
    await api.cancelOrder(id)
    const b = bookings.value.find(b => b.id === id)
    if (b) b.status = 'cancelled'
  }

  return { bookings, loadBookings, addBooking, cancelBooking }
})
