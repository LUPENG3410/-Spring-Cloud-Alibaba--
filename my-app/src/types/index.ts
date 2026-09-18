export interface Car {
  id: number
  name: string
  brand: string
  type: 'sedan' | 'suv' | 'mpv' | 'luxury' | 'sports'
  image: string
  price: number
  rentalPrice: number
  seats: number
  fuel: string
  transmission: string
  available?: boolean
  features: string[]
  province?: string
  currentProvince?: string
  homeProvince?: string
  isHitch?: boolean
  fuelType: string
  fuelGrade: string
  fuelTankCapacity: string
  doors: string
  reversingCamera: boolean
  autoHold: boolean
  driverAssist: string
  smartConnect: string
  electricSeat: boolean
  seatFunction: string
}

export interface Booking {
  id: number
  carId: number
  carName: string
  carImage: string
  startDate: string
  endDate: string
  pickupTime?: string
  returnTime?: string
  totalDays: number
  totalPrice: number
  status: 'pending' | 'confirmed' | 'active' | 'completed' | 'cancelled'
  pickupLocation: string
  returnLocation: string
  pickupProvince: string
  returnProvince: string
  insuranceProductId?: number
  insuranceName?: string
  insurancePrice?: number
}

export interface User {
  id: number
  name: string
  phone: string
  avatar: string
  memberLevel: string
  status: 'active' | 'disabled'
}

export interface StoreLocation {
  id: number
  name: string
  province: string
  city: string
  address: string
  phone: string
  lat: number
  lng: number
  hours: string
  status: 'active' | 'closed'
}

export interface ServiceMessage {
  id: number
  conversationId: string
  userId: number
  content: string
  sender: 'user' | 'agent' | 'system'
  timestamp: string
}

export interface ServiceConversation {
  id: string
  userId: number
  status: 'active' | 'closed'
  createdAt: string
  updatedAt: string
  messages: ServiceMessage[]
}

export interface ServiceChatRequest {
  conversationId: string
  content: string
  carId?: number
  carName?: string
}

export interface Review {
  id: number
  userName: string
  avatar: string
  rating: number
  date: string
  content: string
  carDays: number
}

export interface CarDetail extends Car {
  year: number
  engine: string
  horsepower: number
  torque: number
  displacement: string
  acceleration: string
  topSpeed: string
  fuelConsumption: string
  wheelbase: string
  length: string
  width: string
  height: string
  trunkVolume: string
  weight: string
  images: string[]
  interiorImages: string[]
  descriptions: string[]
  reviews: Review[]
}

export interface ServiceQuickReplies {
  general: string[]
  car: string[]
}

export interface InsuranceProduct {
  id: number
  code: string
  name: string
  description: string
  dailyPrice: number
  totalPrice: number
  vehicleLossDeductible: number
  vehicleLossCoverageRate: number
  tireLossCovered: boolean
  thirdPartyLimit: number
  thirdPartyMedicalCovered: boolean
  thirdPartyMedicalLimit: number
  driverLossLimit: number
  stopFeeCovered: boolean
  singleAccidentNoDocLimit: number
  status: number
  sortOrder: number
}
