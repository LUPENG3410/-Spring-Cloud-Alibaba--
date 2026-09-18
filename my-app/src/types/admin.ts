export interface AdminUser {
  id: number
  name: string
  phone: string
  avatar: string
  role: 'admin' | 'superadmin'
  lastLogin: string
  status: 'active' | 'disabled'
}

export interface AdminStats {
  totalCars: number
  availableCars: number
  totalBookings: number
  pendingBookings: number
  totalUsers: number
  totalStores: number
  monthlyRevenue: number
  todayBookings: number
}

export interface AdminBooking {
  id: number
  userId: number
  userName: string
  userPhone: string
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
  createdAt: string
}

export interface AdminUserInfo {
  id: number
  name: string
  phone: string
  avatar: string
  memberLevel: string
  registerDate: string
  totalOrders: number
  totalSpent: number
  status: 'active' | 'disabled'
}

export interface AdminCar {
  id: number
  trimId: number
  trimName: string
  name: string
  brand: string
  type: string
  image: string
  price: number
  rentalPrice: number
  seats: number
  fuel: string
  transmission: string
  year: number
  storeId: number
  storeName: string
  province: string
  city: string
  plateNumber: string
  mileage: number
  lastMaintenance: string
  status: 'available' | 'rented' | 'maintenance'
}

export interface AdminTrim {
  id: number
  brand: string
  seriesName: string
  name: string
  type: 'sedan' | 'suv' | 'mpv' | 'luxury' | 'sports'
  year: number
  image: string
  price: number
  rentalPrice: number
  seats: number
  fuel: string
  transmission: string
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
  images: string[]
  interiorImages: string[]
  features: string[]
  descriptions: string[]
  status: number
  stockCount: number
}

export interface AdminStoreLocation {
  id: number
  name: string
  province: string
  city: string
  address: string
  phone: string
  lat: number
  lng: number
  hours: string
  carCount: number
  status: 'active' | 'closed'
}

export interface MaintenanceReminder {
  carId: number
  plateNumber: string
  carName: string
  storeId: number
  storeName: string
  storePhone: string
  lastMaintenance: string
  nextMaintenanceDate: string
  daysRemaining: number
  status: 'overdue' | 'upcoming' | 'normal'
}

export interface AdminNotification {
  id: number
  title: string
  content: string
  type: string
  relatedId: number
  isRead: boolean
  createdAt: string
}
