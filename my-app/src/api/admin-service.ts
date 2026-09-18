import { ref } from 'vue'
import type { AdminStats, AdminBooking, AdminUserInfo, AdminCar, AdminStoreLocation, MaintenanceReminder, AdminNotification } from '@/types/admin'
import {
  adminLogin as apiLogin,
  fetchAdminStats as apiFetchStats,
  fetchAdminBookings as apiFetchBookings,
  updateAdminBookingStatus as apiUpdateBookingStatus,
  fetchAdminUsers as apiFetchUsers,
  updateAdminUserStatus as apiUpdateUserStatus,
  fetchAdminCars as apiFetchCars,
  createAdminCar as apiCreateCar,
  updateAdminCarStatus as apiUpdateCarStatus,
  deleteAdminCar as apiDeleteCar,
  fetchAdminStores as apiFetchStores,
  updateAdminStoreStatus as apiUpdateStoreStatus,
  fetchMaintenanceReminders as apiFetchMaintenanceReminders,
  fetchOverdueReminders as apiFetchOverdueReminders,
  fetchUpcomingReminders as apiFetchUpcomingReminders,
  updateMaintenanceInterval as apiUpdateMaintenanceInterval,
  markMaintenanceDone as apiMarkMaintenanceDone,
  fetchAdminNotifications as apiFetchNotifications,
  fetchUnreadNotificationCount as apiFetchUnreadCount,
  markNotificationAsRead as apiMarkNotificationRead,
  markAllNotificationsAsRead as apiMarkAllRead
} from '@/api/admin'
import { useAdminStore } from '@/stores/admin'

const API_AVAILABLE = ref(true)

async function tryApi<T>(fn: () => Promise<T>, fallback: T): Promise<T> {
  if (!API_AVAILABLE.value) return fallback
  try {
    return await fn()
  } catch {
    API_AVAILABLE.value = false
    console.info('Admin API不可用，使用本地数据')
    return fallback
  }
}

export function useAdminApi() {
  const store = useAdminStore()

  return {
    async login(phone: string, password: string) {
      try {
        const result = await apiLogin(phone, password)
        localStorage.setItem('admin_token', result.token)
        return result.user
      } catch {
        if (phone === 'admin' && password === 'admin123') {
          return store.login(phone, password) ? store.adminUser! : null
        }
        return null
      }
    },

    async getStats(fallback: AdminStats) {
      return tryApi(() => apiFetchStats(), fallback)
    },

    async getBookings(fallback: AdminBooking[]) {
      return tryApi(() => apiFetchBookings(), fallback)
    },

    async updateBookingStatus(id: number, status: AdminBooking['status']) {
      try { await apiUpdateBookingStatus(id, status) } catch { /* use fallback */ }
      store.updateBookingStatus(id, status)
    },

    async getUsers(fallback: AdminUserInfo[]) {
      return tryApi(() => apiFetchUsers(), fallback)
    },

    async updateUserStatus(id: number, status: AdminUserInfo['status']) {
      try { await apiUpdateUserStatus(id, status) } catch { /* use fallback */ }
      store.updateUserStatus(id, status)
    },

    async getCars(fallback: AdminCar[]) {
      return tryApi(() => apiFetchCars(), fallback)
    },

    async createCar(data: Omit<AdminCar, 'id'>) {
      try { return await apiCreateCar(data) } catch { store.addCar(data); return store.cars[store.cars.length - 1] }
    },

    async updateCarStatus(id: number, status: AdminCar['status']) {
      try { await apiUpdateCarStatus(id, status) } catch { /* use fallback */ }
      store.updateCarStatus(id, status)
    },

    async deleteCar(id: number) {
      try { await apiDeleteCar(id) } catch { /* use fallback */ }
      store.deleteCar(id)
    },

    async getStores(fallback: AdminStoreLocation[]) {
      return tryApi(() => apiFetchStores(), fallback)
    },

    async updateStoreStatus(id: number, status: AdminStoreLocation['status']) {
      try { await apiUpdateStoreStatus(id, status) } catch { /* use fallback */ }
      store.updateStoreStatus(id, status)
    },

    async getMaintenanceReminders(fallback: MaintenanceReminder[]) {
      return tryApi(() => apiFetchMaintenanceReminders(), fallback)
    },

    async getOverdueReminders(fallback: MaintenanceReminder[]) {
      return tryApi(() => apiFetchOverdueReminders(), fallback)
    },

    async getUpcomingReminders(days: number, fallback: MaintenanceReminder[]) {
      return tryApi(() => apiFetchUpcomingReminders(days), fallback)
    },

    async updateMaintenanceInterval(carId: number, days: number) {
      try { await apiUpdateMaintenanceInterval(carId, days) } catch { /* use fallback */ }
    },

    async markMaintenanceDone(carId: number) {
      try { await apiMarkMaintenanceDone(carId) } catch { /* use fallback */ }
    },

    async getNotifications(fallback: AdminNotification[]) {
      return tryApi(() => apiFetchNotifications(), fallback)
    },

    async getUnreadCount(fallback: number) {
      return tryApi(() => apiFetchUnreadCount(), fallback)
    },

    async markNotificationRead(id: number) {
      try { await apiMarkNotificationRead(id) } catch { /* use fallback */ }
    },

    async markAllNotificationsRead() {
      try { await apiMarkAllRead() } catch { /* use fallback */ }
    }
  }
}
