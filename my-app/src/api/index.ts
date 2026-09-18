import request from './request'
import type { Car, CarDetail, Booking, User, StoreLocation, ServiceMessage, ServiceConversation, Review, InsuranceProduct } from '@/types'

// ========== 顺风车 ==========

export async function fetchHotCars(): Promise<any[]> {
  const isDev = import.meta.env.DEV
  const base = isDev ? '' : (import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api').replace('/api', '')
  const res = await fetch(`${base}/agent/hot-cars`)
  const json = await res.json()
  return json.data || []
}

export async function fetchHitchCars(): Promise<Car[]> {
  const res = await request.get('/cars/hitch')
  return res.data.data
}

// ========== 车辆 ==========

export async function checkCarAvailability(trimId: number, pickupProvince: string, startDate: string, endDate: string): Promise<boolean> {
  const res = await request.get('/bookings/check-availability', { params: { trimId, pickupProvince, startDate, endDate } })
  return res.data.data
}

export async function fetchCars(): Promise<Car[]> {
  const res = await request.get('/cars')
  return res.data.data
}

export async function fetchCarById(id: number): Promise<CarDetail> {
  const res = await request.get(`/cars/${id}`)
  return res.data.data
}

// ========== 订单 ==========

export async function fetchBookings(): Promise<Booking[]> {
  const res = await request.get('/bookings')
  return res.data.data
}

export async function createBooking(data: Omit<Booking, 'id' | 'status'>): Promise<Booking> {
  const res = await request.post('/bookings', data)
  return res.data.data
}

export async function cancelBooking(id: number): Promise<void> {
  await request.put(`/bookings/${id}/cancel`)
}

// ========== 评价 ==========

export async function fetchReviewsByTrimId(trimId: number): Promise<Review[]> {
  const res = await request.get(`/reviews/trim/${trimId}`)
  return res.data.data
}

export async function createReview(data: { trimId: number; bookingId?: number; rating: number; content: string; carDays?: number }): Promise<any> {
  const res = await request.post('/reviews', data)
  return res.data.data
}

// ========== 用户 ==========

interface AuthResponse {
  accessToken: string
  refreshToken: string
  expiresIn: number
  tokenType: string
  userId: number
  phone: string
  name: string
  avatar: string | null
  memberLevel: string
}

function parseAuth(raw: AuthResponse): { user: User; token: string } {
  return {
    token: raw.accessToken,
    user: {
      id: raw.userId,
      name: raw.name,
      phone: raw.phone,
      avatar: raw.avatar || '',
      memberLevel: raw.memberLevel,
      status: 'active'
    }
  }
}

export async function loginApi(phone: string, password: string): Promise<{ user: User; token: string }> {
  const res = await request.post('/auth/login', { phone, password })
  return parseAuth(res.data.data)
}

export async function registerApi(name: string, phone: string, password: string): Promise<{ user: User; token: string }> {
  const res = await request.post('/auth/register', { phone, password, confirmPassword: password })
  if (res.data.code !== 200) throw new Error(res.data.message || '注册失败')
  return parseAuth(res.data.data)
}

export async function fetchProfile(): Promise<User> {
  const res = await request.get('/user/profile')
  return res.data.data
}

// ========== 门店 ==========

export async function fetchStores(): Promise<StoreLocation[]> {
  const res = await request.get('/stores')
  return res.data.data
}

// ========== 保险产品 ==========

export async function fetchInsuranceProducts(): Promise<InsuranceProduct[]> {
  const res = await request.get('/insurance-products')
  return res.data.data
}

// ========== 客服 ==========

export async function createConversation(): Promise<ServiceConversation> {
  const res = await request.post('/service/conversations')
  return res.data.data
}

export async function sendMessage(conversationId: string, content: string): Promise<ServiceMessage> {
  const res = await request.post(`/service/conversations/${conversationId}/messages`, { content })
  return res.data.data
}

export async function getConversation(conversationId: string): Promise<ServiceConversation> {
  const res = await request.get(`/service/conversations/${conversationId}`)
  return res.data.data
}

export async function getUserConversations(): Promise<ServiceConversation[]> {
  const res = await request.get('/service/conversations')
  return res.data.data
}

export async function closeConversation(conversationId: string): Promise<void> {
  await request.put(`/service/conversations/${conversationId}/close`)
}
