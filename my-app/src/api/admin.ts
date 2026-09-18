import request from './request'
import type { AdminUser, AdminStats, AdminBooking, AdminUserInfo, AdminCar, AdminStoreLocation, MaintenanceReminder, AdminNotification } from '@/types/admin'

export async function adminLogin(phone: string, password: string): Promise<{ user: AdminUser; token: string }> {
  const res = await request.post('/admin/login', { phone, password })
  return res.data.data
}

export async function fetchAdminStats(): Promise<AdminStats> {
  const res = await request.get('/admin/stats')
  return res.data.data
}

export async function fetchAdminBookings(): Promise<AdminBooking[]> {
  const res = await request.get('/admin/bookings')
  return res.data.data
}

export async function updateAdminBookingStatus(id: number, status: string): Promise<void> {
  await request.put(`/admin/bookings/${id}/status`, { status })
}

export async function fetchAdminUsers(): Promise<AdminUserInfo[]> {
  const res = await request.get('/admin/users')
  return res.data.data
}

export async function updateAdminUserStatus(id: number, status: string): Promise<void> {
  await request.put(`/admin/users/${id}/status`, { status })
}

export async function fetchAdminCars(): Promise<AdminCar[]> {
  const res = await request.get('/admin/cars')
  return res.data.data
}

export async function createAdminCar(data: Omit<AdminCar, 'id'>): Promise<AdminCar> {
  const res = await request.post('/admin/cars', data)
  return res.data.data
}

export async function createAdminCarInventory(data: { trimId: number; storeId: number; plateProvince: string; plateLetter: string; plateNumber: string; mileage?: number; lastMaintenance?: string; status?: string }): Promise<AdminCar> {
  const res = await request.post('/admin/cars/inventory', data)
  return res.data.data
}

export async function updateAdminCarStatus(id: number, status: string): Promise<void> {
  await request.put(`/admin/cars/${id}/status`, { status })
}

export async function updateAdminCar(id: number, data: { price?: number; mileage?: number; image?: string }): Promise<void> {
  await request.put(`/admin/cars/${id}`, data)
}

export async function deleteAdminCar(id: number): Promise<void> {
  await request.delete(`/admin/cars/${id}`)
}

export async function fetchAdminStores(): Promise<AdminStoreLocation[]> {
  const res = await request.get('/admin/stores')
  return res.data.data
}

export async function updateAdminStoreStatus(id: number, status: string): Promise<void> {
  await request.put(`/admin/stores/${id}/status`, { status })
}

export async function createAdminStore(data: { name: string; province: string; city: string; address: string; phone: string; lat: number; lng: number; hours: string }): Promise<any> {
  const res = await request.post('/admin/stores', data)
  return res.data.data
}

export async function deleteAdminStore(id: number): Promise<void> {
  await request.delete(`/admin/stores/${id}`)
}

export async function fetchMaintenanceReminders(): Promise<MaintenanceReminder[]> {
  const res = await request.get('/admin/maintenance/reminders')
  return res.data.data
}

export async function fetchOverdueReminders(): Promise<MaintenanceReminder[]> {
  const res = await request.get('/admin/maintenance/overdue')
  return res.data.data
}

export async function fetchUpcomingReminders(days: number = 7): Promise<MaintenanceReminder[]> {
  const res = await request.get('/admin/maintenance/upcoming', { params: { days } })
  return res.data.data
}

export async function updateMaintenanceInterval(carId: number, days: number): Promise<void> {
  await request.put(`/admin/maintenance/${carId}/interval`, { days })
}

export async function markMaintenanceDone(carId: number): Promise<void> {
  await request.put(`/admin/maintenance/${carId}/complete`)
}

export async function fetchAdminNotifications(): Promise<AdminNotification[]> {
  const res = await request.get('/admin/notifications')
  return res.data.data
}

export async function fetchUnreadNotificationCount(): Promise<number> {
  const res = await request.get('/admin/notifications/unread-count')
  return res.data.data.count
}

export async function markNotificationAsRead(id: number): Promise<void> {
  await request.put(`/admin/notifications/${id}/read`)
}

export async function markAllNotificationsAsRead(): Promise<void> {
  await request.put('/admin/notifications/read-all')
}
