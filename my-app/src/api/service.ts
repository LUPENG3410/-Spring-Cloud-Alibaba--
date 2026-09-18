import { loginApi, registerApi, fetchProfile, fetchCars, fetchCarById, fetchBookings, createBooking, cancelBooking, fetchReviewsByTrimId, createReview, fetchInsuranceProducts, checkCarAvailability, fetchHitchCars } from '@/api'

export function useApi() {
  return {
    async login(phone: string, password: string) {
      const result = await loginApi(phone, password)
      localStorage.setItem('token', result.token)
      return result.user
    },

    async register(name: string, phone: string, password: string) {
      const result = await registerApi(name, phone, password)
      localStorage.setItem('token', result.token)
      return result.user
    },

    async getProfile() {
      return fetchProfile()
    },

    async getCars() {
      return fetchCars()
    },

    async getHitchCars() {
      return fetchHitchCars()
    },

    async checkCarAvailability(trimId: number, pickupProvince: string, startDate: string, endDate: string) {
      return checkCarAvailability(trimId, pickupProvince, startDate, endDate)
    },

    async getCarById(id: number) {
      return fetchCarById(id)
    },

    async getBookings() {
      return fetchBookings()
    },

    async addBooking(data: Parameters<typeof createBooking>[0]) {
      return createBooking(data)
    },

    async cancelOrder(id: number) {
      return cancelBooking(id)
    },

    async getReviews(trimId: number) {
      return fetchReviewsByTrimId(trimId)
    },

    async addReview(data: { trimId: number; bookingId?: number; rating: number; content: string; carDays?: number }) {
      return createReview(data)
    },

    async getInsuranceProducts() {
      return fetchInsuranceProducts()
    }
  }
}
