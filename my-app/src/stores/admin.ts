import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { AdminUser, AdminStats, AdminBooking, AdminUserInfo, AdminCar, AdminStoreLocation } from '@/types/admin'
import request from '@/api/request'

export const useAdminStore = defineStore('admin', () => {
  const adminUser = ref<AdminUser | null>(null)
  const isLoggedIn = ref(false)

  const stats = ref<AdminStats>({
    totalCars: 0,
    availableCars: 0,
    totalBookings: 0,
    pendingBookings: 0,
    totalUsers: 0,
    totalStores: 0,
    monthlyRevenue: 0,
    todayBookings: 0
  })

  const bookings = ref<AdminBooking[]>([
    { id: 1001, userId: 1, userName: '张三', userPhone: '13800138000', carId: 2, carName: '丰田凯美瑞', carImage: 'https://images.unsplash.com/photo-1621007947382-bb3c3994e3fb?w=400&h=250&fit=crop', startDate: '2026-06-15', endDate: '2026-06-18', totalDays: 3, totalPrice: 594, status: 'confirmed', pickupLocation: '北京首都机场店', returnLocation: '北京首都机场店', pickupProvince: '北京', returnProvince: '北京', createdAt: '2026-06-10 14:30:00' },
    { id: 1002, userId: 2, userName: '李四', userPhone: '13900139000', carId: 5, carName: '宝马3系', carImage: 'https://images.unsplash.com/photo-1555215695-3004980ad54e?w=400&h=250&fit=crop', startDate: '2026-06-20', endDate: '2026-06-22', totalDays: 2, totalPrice: 796, status: 'pending', pickupLocation: '上海虹桥店', returnLocation: '上海浦东店', pickupProvince: '上海', returnProvince: '上海', createdAt: '2026-06-12 09:15:00' },
    { id: 1003, userId: 3, userName: '王五', userPhone: '13700137000', carId: 8, carName: '保时捷718', carImage: 'https://images.unsplash.com/photo-1503376780353-7e6692767b70?w=400&h=250&fit=crop', startDate: '2026-06-18', endDate: '2026-06-20', totalDays: 2, totalPrice: 2560, status: 'completed', pickupLocation: '北京首都机场店', returnLocation: '北京首都机场店', pickupProvince: '北京', returnProvince: '北京', createdAt: '2026-06-08 16:45:00' },
    { id: 1004, userId: 4, userName: '赵六', userPhone: '13600136000', carId: 3, carName: '本田CR-V', carImage: 'https://images.unsplash.com/photo-1568844293986-8d0400f4745b?w=400&h=250&fit=crop', startDate: '2026-06-22', endDate: '2026-06-25', totalDays: 3, totalPrice: 684, status: 'pending', pickupLocation: '广州白云机场店', returnLocation: '深圳宝安机场店', pickupProvince: '广东', returnProvince: '广东', createdAt: '2026-06-13 11:20:00' },
    { id: 1005, userId: 5, userName: '孙七', userPhone: '13500135000', carId: 1, carName: '大众朗逸', carImage: 'https://images.unsplash.com/photo-1541899481282-d53bffe3c35d?w=400&h=250&fit=crop', startDate: '2026-06-14', endDate: '2026-06-17', totalDays: 3, totalPrice: 384, status: 'cancelled', pickupLocation: '北京国贸店', returnLocation: '北京国贸店', pickupProvince: '北京', returnProvince: '北京', createdAt: '2026-06-11 08:30:00' },
    { id: 1006, userId: 1, userName: '张三', userPhone: '13800138000', carId: 6, carName: '奔驰C级', carImage: 'https://images.unsplash.com/photo-1618843479313-40f8afb4b4d8?w=400&h=250&fit=crop', startDate: '2026-06-25', endDate: '2026-06-28', totalDays: 3, totalPrice: 1284, status: 'pending', pickupLocation: '上海南京路店', returnLocation: '杭州萧山机场店', pickupProvince: '上海', returnProvince: '浙江', createdAt: '2026-06-14 10:00:00' },
  ])

  const users = ref<AdminUserInfo[]>([
    { id: 1, name: '张三', phone: '13800138000', avatar: '', memberLevel: '黄金会员', registerDate: '2025-03-15', totalOrders: 12, totalSpent: 4580, status: 'active' },
    { id: 2, name: '李四', phone: '13900139000', avatar: '', memberLevel: '白银会员', registerDate: '2025-06-20', totalOrders: 5, totalSpent: 1960, status: 'active' },
    { id: 3, name: '王五', phone: '13700137000', avatar: '', memberLevel: '黄金会员', registerDate: '2025-01-10', totalOrders: 18, totalSpent: 8920, status: 'active' },
    { id: 4, name: '赵六', phone: '13600136000', avatar: '', memberLevel: '普通会员', registerDate: '2026-01-05', totalOrders: 2, totalSpent: 684, status: 'active' },
    { id: 5, name: '孙七', phone: '13500135000', avatar: '', memberLevel: '钻石会员', registerDate: '2024-11-20', totalOrders: 35, totalSpent: 22400, status: 'active' },
    { id: 6, name: '周八', phone: '13400134000', avatar: '', memberLevel: '普通会员', registerDate: '2026-03-18', totalOrders: 1, totalSpent: 128, status: 'disabled' },
  ])

  const cars = ref<AdminCar[]>([
    { id: 1, trimId: 1, trimName: '朗逸 2024款 1.5L 自动舒适版', name: '朗逸', brand: '大众', type: 'sedan', image: 'https://images.unsplash.com/photo-1541899481282-d53bffe3c35d?w=400&h=250&fit=crop', price: 128, rentalPrice: 128, seats: 5, fuel: '92号汽油', transmission: '自动', year: 2024, storeId: 1, storeName: '北京首都机场店', province: '北京', city: '北京', plateNumber: '京A12345', mileage: 32000, lastMaintenance: '2026-05-20', status: 'available' },
    { id: 2, trimId: 2, trimName: '凯美瑞 2024款 2.0G 豪华版', name: '凯美瑞', brand: '丰田', type: 'sedan', image: 'https://images.unsplash.com/photo-1621007947382-bb3c3994e3fb?w=400&h=250&fit=crop', price: 198, rentalPrice: 198, seats: 5, fuel: '92号汽油', transmission: '自动', year: 2024, storeId: 4, storeName: '上海虹桥机场店', province: '上海', city: '上海', plateNumber: '沪B67890', mileage: 18500, lastMaintenance: '2026-06-01', status: 'rented' },
    { id: 3, trimId: 3, trimName: 'CR-V 2024款 1.5T 四驱版', name: 'CR-V', brand: '本田', type: 'suv', image: 'https://images.unsplash.com/photo-1568844293986-8d0400f4745b?w=400&h=250&fit=crop', price: 228, rentalPrice: 228, seats: 5, fuel: '92号汽油', transmission: '自动', year: 2024, storeId: 7, storeName: '广州白云机场店', province: '广东', city: '广州', plateNumber: '粤C11223', mileage: 25000, lastMaintenance: '2026-05-15', status: 'available' },
    { id: 4, trimId: 4, trimName: 'GL8 2024款 2.0T 豪华版', name: 'GL8', brand: '别克', type: 'mpv', image: 'https://images.unsplash.com/photo-1549317661-bd32c8ce0afa?w=400&h=250&fit=crop', price: 358, rentalPrice: 358, seats: 7, fuel: '95号汽油', transmission: '自动', year: 2024, storeId: 3, storeName: '北京国贸店', province: '北京', city: '北京', plateNumber: '京D44556', mileage: 42000, lastMaintenance: '2026-04-10', status: 'maintenance' },
    { id: 5, trimId: 5, trimName: '3系 2024款 325Li 运动版', name: '3系', brand: '宝马', type: 'luxury', image: 'https://images.unsplash.com/photo-1555215695-3004980ad54e?w=400&h=250&fit=crop', price: 398, rentalPrice: 398, seats: 5, fuel: '95号汽油', transmission: '自动', year: 2024, storeId: 5, storeName: '上海浦东机场店', province: '上海', city: '上海', plateNumber: '沪E78901', mileage: 15000, lastMaintenance: '2026-06-05', status: 'available' },
    { id: 6, trimId: 6, trimName: 'C级 2024款 C260L 运动版', name: 'C级', brand: '奔驰', type: 'luxury', image: 'https://images.unsplash.com/photo-1618843479313-40f8afb4b4d8?w=400&h=250&fit=crop', price: 428, rentalPrice: 428, seats: 5, fuel: '95号汽油', transmission: '自动', year: 2024, storeId: 9, storeName: '深圳宝安机场店', province: '广东', city: '深圳', plateNumber: '粤B23456', mileage: 21000, lastMaintenance: '2026-05-28', status: 'available' },
    { id: 7, trimId: 7, trimName: '汉兰达 2024款 2.5L HEV四驱版', name: '汉兰达', brand: '丰田', type: 'suv', image: 'https://images.unsplash.com/photo-1594611625027-0e40e7ab6e54?w=400&h=250&fit=crop', price: 298, rentalPrice: 298, seats: 7, fuel: '92号汽油', transmission: '自动', year: 2024, storeId: 11, storeName: '成都天府机场店', province: '四川', city: '成都', plateNumber: '川A34567', mileage: 38000, lastMaintenance: '2026-05-01', status: 'available' },
    { id: 8, trimId: 8, trimName: '718 2024款 T Style', name: '718', brand: '保时捷', type: 'sports', image: 'https://images.unsplash.com/photo-1503376780353-7e6692767b70?w=400&h=250&fit=crop', price: 1280, rentalPrice: 1280, seats: 2, fuel: '98号汽油', transmission: '自动', year: 2024, storeId: 2, storeName: '北京大兴机场店', province: '北京', city: '北京', plateNumber: '京F89012', mileage: 8000, lastMaintenance: '2026-06-10', status: 'available' },
    { id: 9, trimId: 9, trimName: '轩逸 2024款 1.6L CVT豪华版', name: '轩逸', brand: '日产', type: 'sedan', image: 'https://images.unsplash.com/photo-1590362891991-f776e747a588?w=400&h=250&fit=crop', price: 99, rentalPrice: 99, seats: 5, fuel: '92号汽油', transmission: '自动', year: 2024, storeId: 13, storeName: '杭州萧山机场店', province: '浙江', city: '杭州', plateNumber: '浙A90123', mileage: 28000, lastMaintenance: '2026-05-18', status: 'available' },
    { id: 10, trimId: 10, trimName: 'A4L 2024款 40TFSI 豪华版', name: 'A4L', brand: '奥迪', type: 'luxury', image: 'https://images.unsplash.com/photo-1606664515524-ed2f786a0bd6?w=400&h=250&fit=crop', price: 368, rentalPrice: 368, seats: 5, fuel: '95号汽油', transmission: '自动', year: 2024, storeId: 8, storeName: '广州天河店', province: '广东', city: '广州', plateNumber: '粤A45678', mileage: 19000, lastMaintenance: '2026-06-02', status: 'available' },
    { id: 11, trimId: 11, trimName: '速腾 2024款 1.4T 自动舒适版', name: '速腾', brand: '大众', type: 'sedan', image: 'https://images.unsplash.com/photo-1609521263047-f8f205293f24?w=400&h=250&fit=crop', price: 118, rentalPrice: 118, seats: 5, fuel: '92号汽油', transmission: '自动', year: 2024, storeId: 42, storeName: '石嘴山大武口店', province: '宁夏', city: '石嘴山', plateNumber: '宁B56789', mileage: 35000, lastMaintenance: '2026-04-25', status: 'available' },
    { id: 12, trimId: 12, trimName: 'H6 2024款 1.5T 自动豪华版', name: 'H6', brand: '哈弗', type: 'suv', image: 'https://images.unsplash.com/photo-1609521263047-f8f205293f24?w=400&h=250&fit=crop', price: 158, rentalPrice: 158, seats: 5, fuel: '92号汽油', transmission: '自动', year: 2024, storeId: 45, storeName: '中卫沙坡头店', province: '宁夏', city: '中卫', plateNumber: '宁C67890', mileage: 22000, lastMaintenance: '2026-05-10', status: 'available' },
  ])

  const stores = ref<AdminStoreLocation[]>([
    { id: 1, name: '北京首都机场店', province: '北京', city: '北京', address: '北京市顺义区首都机场T3航站楼', phone: '010-88881001', lat: 40.0799, lng: 116.6031, hours: '06:00-23:00', carCount: 15, status: 'active' },
    { id: 2, name: '北京大兴机场店', province: '北京', city: '北京', address: '北京市大兴区大兴国际机场', phone: '010-88881002', lat: 39.5098, lng: 116.4105, hours: '06:00-23:00', carCount: 12, status: 'active' },
    { id: 3, name: '北京国贸店', province: '北京', city: '北京', address: '北京市朝阳区建国路88号', phone: '010-88881003', lat: 39.9087, lng: 116.4612, hours: '08:00-21:00', carCount: 8, status: 'active' },
    { id: 4, name: '上海虹桥机场店', province: '上海', city: '上海', address: '上海市闵行区虹桥机场T2航站楼', phone: '021-88882001', lat: 31.1979, lng: 121.3363, hours: '06:00-23:00', carCount: 18, status: 'active' },
    { id: 5, name: '上海浦东机场店', province: '上海', city: '上海', address: '上海市浦东新区浦东机场T1航站楼', phone: '021-88882002', lat: 31.1443, lng: 121.8083, hours: '06:00-23:00', carCount: 16, status: 'active' },
    { id: 6, name: '上海南京路店', province: '上海', city: '上海', address: '上海市黄浦区南京东路199号', phone: '021-88882003', lat: 31.2375, lng: 121.4755, hours: '08:00-21:00', carCount: 10, status: 'active' },
    { id: 7, name: '广州白云机场店', province: '广东', city: '广州', address: '广州市白云区白云机场T2航站楼', phone: '020-88883001', lat: 23.3925, lng: 113.3012, hours: '06:00-23:00', carCount: 14, status: 'active' },
    { id: 8, name: '广州天河店', province: '广东', city: '广州', address: '广州市天河区天河路385号', phone: '020-88883002', lat: 23.1332, lng: 113.3289, hours: '08:00-21:00', carCount: 9, status: 'active' },
    { id: 9, name: '深圳宝安机场店', province: '广东', city: '深圳', address: '深圳市宝安区宝安机场T3航站楼', phone: '0755-88884001', lat: 22.6395, lng: 113.8108, hours: '06:00-23:00', carCount: 11, status: 'active' },
    { id: 10, name: '深圳南山店', province: '广东', city: '深圳', address: '深圳市南山区科技园南区', phone: '0755-88884002', lat: 22.5329, lng: 113.9439, hours: '08:00-21:00', carCount: 7, status: 'active' },
    { id: 11, name: '成都天府机场店', province: '四川', city: '成都', address: '成都市简阳市天府机场T1航站楼', phone: '028-88885001', lat: 30.3197, lng: 104.4412, hours: '06:00-23:00', carCount: 13, status: 'active' },
    { id: 12, name: '成都春熙路店', province: '四川', city: '成都', address: '成都市锦江区春熙路步行街', phone: '028-88885002', lat: 30.6571, lng: 104.0818, hours: '08:00-21:00', carCount: 6, status: 'active' },
    { id: 13, name: '杭州萧山机场店', province: '浙江', city: '杭州', address: '杭州市萧山区萧山机场T4航站楼', phone: '0571-88886001', lat: 30.2295, lng: 120.4345, hours: '06:00-23:00', carCount: 10, status: 'active' },
    { id: 14, name: '杭州武林广场店', province: '浙江', city: '杭州', address: '杭州市下城区武林广场21号', phone: '0571-88886002', lat: 30.2766, lng: 120.1708, hours: '08:00-21:00', carCount: 8, status: 'active' },
    { id: 15, name: '银川河东机场店', province: '宁夏', city: '银川', address: '银川市灵武市河东机场T3航站楼', phone: '0951-88887001', lat: 38.3219, lng: 106.3915, hours: '07:00-22:00', carCount: 9, status: 'active' },
    { id: 16, name: '银川金凤万达店', province: '宁夏', city: '银川', address: '银川市金凤区正源北街万达广场', phone: '0951-88887002', lat: 38.4729, lng: 106.2297, hours: '08:00-21:00', carCount: 5, status: 'active' },
    { id: 17, name: '天津滨海机场店', province: '天津', city: '天津', address: '天津市东丽区滨海机场T2航站楼', phone: '022-88888001', lat: 39.1244, lng: 117.3469, hours: '06:00-23:00', carCount: 12, status: 'active' },
    { id: 18, name: '重庆江北机场店', province: '重庆', city: '重庆', address: '重庆市渝北区江北机场T3航站楼', phone: '023-88889001', lat: 29.7192, lng: 106.6417, hours: '06:00-23:00', carCount: 14, status: 'active' },
    { id: 19, name: '石家庄正定机场店', province: '河北', city: '石家庄', address: '石家庄市正定县正定机场T2航站楼', phone: '0311-88881001', lat: 38.2806, lng: 114.6969, hours: '06:00-22:00', carCount: 8, status: 'active' },
    { id: 20, name: '太原武宿机场店', province: '山西', city: '太原', address: '太原市小店区武宿机场T2航站楼', phone: '0351-88881001', lat: 37.7469, lng: 112.6275, hours: '06:00-22:00', carCount: 9, status: 'active' },
    { id: 21, name: '呼和浩特白塔机场店', province: '内蒙古', city: '呼和浩特', address: '呼和浩特市赛罕区白塔机场T2航站楼', phone: '0471-88881001', lat: 40.8517, lng: 111.8231, hours: '07:00-22:00', carCount: 7, status: 'active' },
    { id: 22, name: '沈阳桃仙机场店', province: '辽宁', city: '沈阳', address: '沈阳市浑南区桃仙机场T3航站楼', phone: '024-88881001', lat: 41.6398, lng: 123.4833, hours: '06:00-23:00', carCount: 10, status: 'active' },
    { id: 23, name: '长春龙嘉机场店', province: '吉林', city: '长春', address: '长春市九台区龙嘉机场T2航站楼', phone: '0431-88881001', lat: 44.0028, lng: 125.6837, hours: '06:00-22:00', carCount: 8, status: 'active' },
    { id: 24, name: '哈尔滨太平机场店', province: '黑龙江', city: '哈尔滨', address: '哈尔滨市道里区太平机场T2航站楼', phone: '0451-88881001', lat: 45.6234, lng: 126.2513, hours: '06:00-22:00', carCount: 9, status: 'active' },
    { id: 25, name: '南京禄口机场店', province: '江苏', city: '南京', address: '南京市江宁区禄口机场T1航站楼', phone: '025-88881001', lat: 31.7420, lng: 118.8620, hours: '06:00-23:00', carCount: 12, status: 'active' },
    { id: 26, name: '合肥新桥机场店', province: '安徽', city: '合肥', address: '合肥市蜀山区新桥机场T1航站楼', phone: '0551-88881001', lat: 31.9896, lng: 117.0033, hours: '06:00-22:00', carCount: 8, status: 'active' },
    { id: 27, name: '福州长乐机场店', province: '福建', city: '福州', address: '福州市长乐区长乐机场T1航站楼', phone: '0591-88881001', lat: 25.9351, lng: 119.6633, hours: '06:00-22:00', carCount: 9, status: 'active' },
    { id: 28, name: '南昌昌北机场店', province: '江西', city: '南昌', address: '南昌市新建区昌北机场T2航站楼', phone: '0791-88881001', lat: 28.8650, lng: 115.9000, hours: '06:00-22:00', carCount: 8, status: 'active' },
    { id: 29, name: '济南遥墙机场店', province: '山东', city: '济南', address: '济南市历城区遥墙机场T1航站楼', phone: '0531-88881001', lat: 36.8569, lng: 117.2158, hours: '06:00-23:00', carCount: 11, status: 'active' },
    { id: 30, name: '郑州新郑机场店', province: '河南', city: '郑州', address: '郑州市新郑市新郑机场T2航站楼', phone: '0371-88881001', lat: 34.5197, lng: 113.8409, hours: '06:00-23:00', carCount: 10, status: 'active' },
    { id: 31, name: '武汉天河机场店', province: '湖北', city: '武汉', address: '武汉市黄陂区天河机场T3航站楼', phone: '027-88881001', lat: 30.7838, lng: 114.2091, hours: '06:00-23:00', carCount: 12, status: 'active' },
    { id: 32, name: '长沙黄花机场店', province: '湖南', city: '长沙', address: '长沙市长沙县黄花机场T2航站楼', phone: '0731-88881001', lat: 28.1892, lng: 113.2200, hours: '06:00-23:00', carCount: 11, status: 'active' },
    { id: 33, name: '南宁吴圩机场店', province: '广西', city: '南宁', address: '南宁市江南区吴圩机场T2航站楼', phone: '0771-88881001', lat: 22.6085, lng: 108.1722, hours: '06:00-23:00', carCount: 9, status: 'active' },
    { id: 34, name: '海口美兰机场店', province: '海南', city: '海口', address: '海口市美兰区美兰机场T1航站楼', phone: '0898-88881001', lat: 19.9340, lng: 110.4590, hours: '06:00-23:00', carCount: 10, status: 'active' },
    { id: 35, name: '贵阳龙洞堡机场店', province: '贵州', city: '贵阳', address: '贵阳市南明区龙洞堡机场T2航站楼', phone: '0851-88881001', lat: 26.5400, lng: 106.8025, hours: '06:00-22:00', carCount: 8, status: 'active' },
    { id: 36, name: '昆明长水机场店', province: '云南', city: '昆明', address: '昆明市官渡区长水机场T1航站楼', phone: '0871-88881001', lat: 25.1019, lng: 102.9292, hours: '06:00-23:00', carCount: 12, status: 'active' },
    { id: 37, name: '拉萨贡嘎机场店', province: '西藏', city: '拉萨', address: '拉萨市贡嘎县贡嘎机场T1航站楼', phone: '0891-88881001', lat: 29.3033, lng: 90.9111, hours: '07:00-21:00', carCount: 5, status: 'active' },
    { id: 38, name: '西安咸阳机场店', province: '陕西', city: '西安', address: '西安市咸阳市咸阳机场T3航站楼', phone: '029-88881001', lat: 34.4471, lng: 108.7516, hours: '06:00-23:00', carCount: 13, status: 'active' },
    { id: 39, name: '兰州中川机场店', province: '甘肃', city: '兰州', address: '兰州市永登县中川机场T2航站楼', phone: '0931-88881001', lat: 36.5152, lng: 103.6203, hours: '07:00-22:00', carCount: 8, status: 'active' },
    { id: 40, name: '西宁曹家堡机场店', province: '青海', city: '西宁', address: '西宁市互助县曹家堡机场T2航站楼', phone: '0971-88881001', lat: 36.5275, lng: 102.0426, hours: '07:00-22:00', carCount: 7, status: 'active' },
    { id: 41, name: '银川河东机场店', province: '宁夏', city: '银川', address: '银川市灵武市河东机场T3航站楼', phone: '0951-88887001', lat: 38.3219, lng: 106.3915, hours: '07:00-22:00', carCount: 9, status: 'active' },
    { id: 42, name: '石嘴山大武口店', province: '宁夏', city: '石嘴山', address: '石嘴山市大武口区朝阳西街88号', phone: '0952-88887001', lat: 39.0191, lng: 106.3839, hours: '08:00-21:00', carCount: 5, status: 'active' },
    { id: 43, name: '吴忠利通店', province: '宁夏', city: '吴忠', address: '吴忠市利通区胜利东街66号', phone: '0953-88887001', lat: 37.9860, lng: 106.1994, hours: '08:00-21:00', carCount: 5, status: 'active' },
    { id: 44, name: '固原原州店', province: '宁夏', city: '固原', address: '固原市原州区文化西街55号', phone: '0954-88887001', lat: 36.0029, lng: 106.2397, hours: '08:00-21:00', carCount: 4, status: 'active' },
    { id: 45, name: '中卫沙坡头店', province: '宁夏', city: '中卫', address: '中卫市沙坡头区文昌南街77号', phone: '0955-88887001', lat: 37.5139, lng: 105.1892, hours: '08:00-21:00', carCount: 4, status: 'active' },
    { id: 46, name: '乌鲁木齐地窝堡机场店', province: '新疆', city: '乌鲁木齐', address: '乌鲁木齐市新市区地窝堡机场T3航站楼', phone: '0991-88881001', lat: 43.9071, lng: 87.4742, hours: '07:00-23:00', carCount: 10, status: 'active' },
    { id: 47, name: '银川金凤万达店', province: '宁夏', city: '银川', address: '银川市金凤区正源北街万达广场', phone: '0951-88887002', lat: 38.4729, lng: 106.2297, hours: '08:00-21:00', carCount: 5, status: 'closed' },
  ])

  async function login(phone: string, password: string) {
    try {
      const res = await request.post('/admin/login', { phone, password })
      const data = res.data.data
      adminUser.value = {
        id: data.id,
        name: data.name,
        phone: data.phone,
        avatar: data.avatar || '',
        role: data.role as 'admin' | 'superadmin',
        lastLogin: new Date().toISOString(),
        status: 'active'
      }
      localStorage.setItem('admin_token', data.token)
      isLoggedIn.value = true
      return true
    } catch {
      return false
    }
  }

  function logout() {
    adminUser.value = null
    isLoggedIn.value = false
    localStorage.removeItem('admin_token')
  }

  function updateCarStatus(carId: number, status: AdminCar['status']) {
    const car = cars.value.find(c => c.id === carId)
    if (car) car.status = status
  }

  async function updateBookingStatus(bookingId: number, status: AdminBooking['status']) {
    const booking = bookings.value.find(b => b.id === bookingId)
    if (booking) booking.status = status
    try {
      await request.put(`/admin/bookings/${bookingId}/status`, { status })
    } catch (e) {
      console.warn('更新订单状态失败:', e)
    }
  }

  function updateUserStatus(userId: number, status: AdminUserInfo['status']) {
    const user = users.value.find(u => u.id === userId)
    if (user) user.status = status
  }

  function updateStoreStatus(storeId: number, status: AdminStoreLocation['status']) {
    const store = stores.value.find(s => s.id === storeId)
    if (store) store.status = status
  }

  function addCar(car: Omit<AdminCar, 'id'>) {
    const newId = Math.max(...cars.value.map(c => c.id)) + 1
    cars.value.push({ ...car, id: newId })
  }

  function deleteCar(carId: number) {
    cars.value = cars.value.filter(c => c.id !== carId)
  }

  return {
    adminUser, isLoggedIn, stats, bookings, users, cars, stores,
    login, logout, updateCarStatus, updateBookingStatus, updateUserStatus,
    updateStoreStatus, addCar, deleteCar
  }
})
