export interface CarTemplate {
  brand: string
  models: {
    name: string
    type: 'sedan' | 'suv' | 'mpv' | 'luxury' | 'sports'
    trims: CarTrim[]
  }[]
}

export interface CarTrim {
  name: string
  year: number
  price: number
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
  features: string[]
  images: string[]
  interiorImages: string[]
  descriptions: string[]
}

export const carDatabase: CarTemplate[] = [
  {
    brand: '大众',
    models: [
      {
        name: '朗逸', type: 'sedan',
        trims: [
          {
            name: '朗逸 2024款 1.5L 自动舒适版', year: 2024, price: 128, seats: 5, fuel: '92号汽油', transmission: '自动',
            engine: '1.5L EA211', horsepower: 113, torque: 145, displacement: '1.5L',
            acceleration: '11.2s', topSpeed: '190km/h', fuelConsumption: '5.9L/100km',
            wheelbase: '2688mm', length: '4678mm', width: '1806mm', height: '1474mm',
            trunkVolume: '510L', weight: '1280kg',
            features: ['倒车影像', '定速巡航', '自动空调', '多功能方向盘', '蓝牙连接', '后排出风口'],
            images: ['https://images.unsplash.com/photo-1541899481282-d53bffe3c35d?w=800&h=500&fit=crop', 'https://images.unsplash.com/photo-1583121274602-3e2820c69888?w=800&h=500&fit=crop', 'https://images.unsplash.com/photo-1494976388531-d1058494cdd8?w=800&h=500&fit=crop'],
            interiorImages: ['https://images.unsplash.com/photo-1503376780353-7e6692767b70?w=800&h=500&fit=crop', 'https://images.unsplash.com/photo-1504215680853-026ed2a45def?w=800&h=500&fit=crop'],
            descriptions: ['朗逸作为大众旗下经典家轿，以可靠品质和经济油耗著称', '宽敞的乘坐空间和510L超大后备箱，满足家庭出行需求', '配备ESP车身稳定系统、倒车影像等实用配置']
          },
          {
            name: '朗逸 2024款 1.5L 自动豪华版', year: 2024, price: 148, seats: 5, fuel: '92号汽油', transmission: '自动',
            engine: '1.5L EA211', horsepower: 113, torque: 145, displacement: '1.5L',
            acceleration: '11.2s', topSpeed: '190km/h', fuelConsumption: '5.9L/100km',
            wheelbase: '2688mm', length: '4678mm', width: '1806mm', height: '1474mm',
            trunkVolume: '510L', weight: '1280kg',
            features: ['倒车影像', '定速巡航', '自动空调', '多功能方向盘', '蓝牙连接', '后排出风口', '全景天窗', '无钥匙进入'],
            images: ['https://images.unsplash.com/photo-1541899481282-d53bffe3c35d?w=800&h=500&fit=crop', 'https://images.unsplash.com/photo-1583121274602-3e2820c69888?w=800&h=500&fit=crop'],
            interiorImages: ['https://images.unsplash.com/photo-1503376780353-7e6692767b70?w=800&h=500&fit=crop'],
            descriptions: ['朗逸豪华版增加全景天窗、无钥匙进入等高级配置', 'EA211发动机成熟可靠，油耗经济']
          }
        ]
      },
      {
        name: '速腾', type: 'sedan',
        trims: [
          {
            name: '速腾 2024款 1.4T 自动舒适版', year: 2024, price: 118, seats: 5, fuel: '92号汽油', transmission: '自动',
            engine: '1.4T EA211', horsepower: 150, torque: 250, displacement: '1.4T',
            acceleration: '9.3s', topSpeed: '200km/h', fuelConsumption: '5.8L/100km',
            wheelbase: '2731mm', length: '4753mm', width: '1800mm', height: '1462mm',
            trunkVolume: '553L', weight: '1350kg',
            features: ['ESP车身稳定', '倒车雷达', '定速巡航', '多功能方向盘', '蓝牙', '后排出风口'],
            images: ['https://images.unsplash.com/photo-1609521263047-f8f205293f24?w=800&h=500&fit=crop', 'https://images.unsplash.com/photo-1583121274602-3e2820c69888?w=800&h=500&fit=crop'],
            interiorImages: ['https://images.unsplash.com/photo-1504215680853-026ed2a45def?w=800&h=500&fit=crop'],
            descriptions: ['速腾是A+级轿车市场的热门选择', '1.4T涡轮增压发动机+7速双离合，动力充沛', '2731mm轴距带来同级领先的乘坐空间']
          }
        ]
      }
    ]
  },
  {
    brand: '丰田',
    models: [
      {
        name: '凯美瑞', type: 'sedan',
        trims: [
          {
            name: '凯美瑞 2024款 2.0G 豪华版', year: 2024, price: 198, seats: 5, fuel: '92号汽油', transmission: '自动',
            engine: '2.0L Dynamic Force', horsepower: 178, torque: 210, displacement: '2.0L',
            acceleration: '9.1s', topSpeed: '210km/h', fuelConsumption: '6.0L/100km',
            wheelbase: '2825mm', length: '4900mm', width: '1840mm', height: '1455mm',
            trunkVolume: '524L', weight: '1430kg',
            features: ['全景天窗', '车道偏离预警', '自适应巡航', '预碰撞安全', '智能互联', 'JBL音响'],
            images: ['https://images.unsplash.com/photo-1621007947382-bb3c3994e3fb?w=800&h=500&fit=crop', 'https://images.unsplash.com/photo-1617531653342-5a1f8a517b8e?w=800&h=500&fit=crop'],
            interiorImages: ['https://images.unsplash.com/photo-1504215680853-026ed2a45def?w=800&h=500&fit=crop', 'https://images.unsplash.com/photo-1503376780353-7e6692767b70?w=800&h=500&fit=crop'],
            descriptions: ['凯美瑞基于TNGA架构打造，操控与舒适兼备', '2.0L Dynamic Force发动机热效率达40%，油耗更低', 'Toyota Safety Sense智行安全系统，全方位守护']
          }
        ]
      },
      {
        name: '汉兰达', type: 'suv',
        trims: [
          {
            name: '汉兰达 2024款 2.5L HEV 四驱豪华版', year: 2024, price: 298, seats: 7, fuel: '92号汽油', transmission: '自动',
            engine: '2.5L HEV混动', horsepower: 250, torque: 238, displacement: '2.5L',
            acceleration: '8.4s', topSpeed: '180km/h', fuelConsumption: '5.3L/100km',
            wheelbase: '2850mm', length: '4965mm', width: '1930mm', height: '1750mm',
            trunkVolume: '456L', weight: '1870kg',
            features: ['四驱系统', '第三排座椅', '电动后备箱', '丰田智行安全', 'JBL音响', '车道保持'],
            images: ['https://images.unsplash.com/photo-1594611625027-0e40e7ab6e54?w=800&h=500&fit=crop', 'https://images.unsplash.com/photo-1519245659620-e859806a8d7b?w=800&h=500&fit=crop'],
            interiorImages: ['https://images.unsplash.com/photo-1503376780353-7e6692767b70?w=800&h=500&fit=crop'],
            descriptions: ['汉兰达是国内7座SUV市场的常青树，口碑极佳', '2.5L混合动力系统，油耗低至5.3L/100km', 'E-Four电子四驱系统，稳定可靠']
          }
        ]
      }
    ]
  },
  {
    brand: '本田',
    models: [
      {
        name: 'CR-V', type: 'suv',
        trims: [
          {
            name: 'CR-V 2024款 240TURBO 四驱豪华版', year: 2024, price: 228, seats: 5, fuel: '92号汽油', transmission: '自动',
            engine: '1.5T VTEC Turbo', horsepower: 193, torque: 243, displacement: '1.5T',
            acceleration: '9.5s', topSpeed: '200km/h', fuelConsumption: '6.8L/100km',
            wheelbase: '2700mm', length: '4703mm', width: '1866mm', height: '1680mm',
            trunkVolume: '589L', weight: '1545kg',
            features: ['四驱系统', '全景天窗', '360全景影像', 'HUD抬头显示', 'BOSE音响', '电动尾门'],
            images: ['https://images.unsplash.com/photo-1568844293986-8d0400f4745b?w=800&h=500&fit=crop', 'https://images.unsplash.com/photo-1519245659620-e859806a8d7b?w=800&h=500&fit=crop'],
            interiorImages: ['https://images.unsplash.com/photo-1503376780353-7e6692767b70?w=800&h=500&fit=crop', 'https://images.unsplash.com/photo-1504215680853-026ed2a45def?w=800&h=500&fit=crop'],
            descriptions: ['CR-V是全球最畅销的SUV之一，空间表现出色', '1.5T涡轮增压+CVT动力组合，动力充沛油耗经济', 'Honda SENSING安全超感，L2级辅助驾驶']
          }
        ]
      }
    ]
  },
  {
    brand: '别克',
    models: [
      {
        name: 'GL8', type: 'mpv',
        trims: [
          {
            name: 'GL8 2024款 ES陆尊 2.0T 豪华型', year: 2024, price: 358, seats: 7, fuel: '95号汽油', transmission: '自动',
            engine: '2.0T Ecotec', horsepower: 237, torque: 350, displacement: '2.0T',
            acceleration: '9.8s', topSpeed: '200km/h', fuelConsumption: '8.5L/100km',
            wheelbase: '3088mm', length: '5238mm', width: '1878mm', height: '1776mm',
            trunkVolume: '521L', weight: '1880kg',
            features: ['航空座椅', '电动侧滑门', '后排娱乐屏', '车载冰箱', 'Bose音响', '无线充电'],
            images: ['https://images.unsplash.com/photo-1549317661-bd32c8ce0afa?w=800&h=500&fit=crop', 'https://images.unsplash.com/photo-1552519507-da3b142c6e3d?w=800&h=500&fit=crop'],
            interiorImages: ['https://images.unsplash.com/photo-1503376780353-7e6692767b70?w=800&h=500&fit=crop', 'https://images.unsplash.com/photo-1504215680853-026ed2a45def?w=800&h=500&fit=crop'],
            descriptions: ['GL8是国内商务MPV标杆，接待首选车型', '第二排航空座椅带腿托、按摩、加热功能', '3088mm超长轴距，三排空间同样宽裕']
          }
        ]
      }
    ]
  },
  {
    brand: '宝马',
    models: [
      {
        name: '3系', type: 'luxury',
        trims: [
          {
            name: '宝马3系 2024款 325Li M运动套装', year: 2024, price: 398, seats: 5, fuel: '95号汽油', transmission: '自动',
            engine: '2.0T B48', horsepower: 184, torque: 300, displacement: '2.0T',
            acceleration: '7.3s', topSpeed: '240km/h', fuelConsumption: '6.9L/100km',
            wheelbase: '2966mm', length: '4838mm', width: '1827mm', height: '1454mm',
            trunkVolume: '480L', weight: '1545kg',
            features: ['M运动套件', '哈曼卡顿音响', '全液晶仪表', '座椅加热', '自动泊车', 'CarPlay'],
            images: ['https://images.unsplash.com/photo-1555215695-3004980ad54e?w=800&h=500&fit=crop', 'https://images.unsplash.com/photo-1556189250-72ba954cfc2b?w=800&h=500&fit=crop', 'https://images.unsplash.com/photo-1580273916550-e323be2ae537?w=800&h=500&fit=crop'],
            interiorImages: ['https://images.unsplash.com/photo-1503376780353-7e6692767b70?w=800&h=500&fit=crop', 'https://images.unsplash.com/photo-1504215680853-026ed2a45def?w=800&h=500&fit=crop'],
            descriptions: ['宝马3系是豪华运动轿车的标杆，操控乐趣十足', 'B48 2.0T发动机+ZF 8AT变速箱，黄金动力总成', '50:50前后配重比，带来极致驾驶体验']
          }
        ]
      }
    ]
  },
  {
    brand: '奔驰',
    models: [
      {
        name: 'C级', type: 'luxury',
        trims: [
          {
            name: '奔驰C级 2024款 C 260 L 运动版', year: 2024, price: 428, seats: 5, fuel: '95号汽油', transmission: '自动',
            engine: '1.5T+48V M254', horsepower: 204, torque: 300, displacement: '1.5T',
            acceleration: '7.8s', topSpeed: '235km/h', fuelConsumption: '6.5L/100km',
            wheelbase: '2954mm', length: '4882mm', width: '1820mm', height: '1461mm',
            trunkVolume: '475L', weight: '1550kg',
            features: ['64色氛围灯', '柏林之声', '智能泊车', 'MBUX系统', '无线充电', '座椅记忆'],
            images: ['https://images.unsplash.com/photo-1618843479313-40f8afb4b4d8?w=800&h=500&fit=crop', 'https://images.unsplash.com/photo-1617531653342-5a1f8a517b8e?w=800&h=500&fit=crop'],
            interiorImages: ['https://images.unsplash.com/photo-1503376780353-7e6692767b70?w=800&h=500&fit=crop', 'https://images.unsplash.com/photo-1504215680853-026ed2a45def?w=800&h=500&fit=crop'],
            descriptions: ['全新C级采用家族最新设计语言，豪华感大幅提升', '11.9英寸中控竖屏+MBUX智能人机交互系统', '64色主动式氛围灯，营造沉浸式座舱体验']
          }
        ]
      }
    ]
  },
  {
    brand: '保时捷',
    models: [
      {
        name: '718', type: 'sports',
        trims: [
          {
            name: '保时捷718 2024款 Boxster 2.0T', year: 2024, price: 1280, seats: 2, fuel: '98号汽油', transmission: '自动',
            engine: '2.0T Flat-4', horsepower: 300, torque: 380, displacement: '2.0T',
            acceleration: '4.7s', topSpeed: '275km/h', fuelConsumption: '8.1L/100km',
            wheelbase: '2475mm', length: '4379mm', width: '1801mm', height: '1295mm',
            trunkVolume: '150L', weight: '1335kg',
            features: ['PDK变速箱', 'Sport Chrono', '运动排气', '碳纤维内饰', 'SC组件', 'PDLS大灯'],
            images: ['https://images.unsplash.com/photo-1503376780353-7e6692767b70?w=800&h=500&fit=crop', 'https://images.unsplash.com/photo-1544636331-e26879cd4d9b?w=800&h=500&fit=crop', 'https://images.unsplash.com/photo-1580273916550-e323be2ae537?w=800&h=500&fit=crop'],
            interiorImages: ['https://images.unsplash.com/photo-1503376780353-7e6692767b70?w=800&h=500&fit=crop'],
            descriptions: ['保时捷718是入门级跑车的标杆，驾驶乐趣极致', '中置水平对置发动机，完美的50:50配重', 'Sport Chrono组件，0-100km/h仅需4.7秒']
          }
        ]
      }
    ]
  },
  {
    brand: '日产',
    models: [
      {
        name: '轩逸', type: 'sedan',
        trims: [
          {
            name: '轩逸 2024款 1.6L CVT 智驾版', year: 2024, price: 99, seats: 5, fuel: '92号汽油', transmission: '自动',
            engine: '1.6L HR16', horsepower: 122, torque: 155, displacement: '1.6L',
            acceleration: '12.0s', topSpeed: '185km/h', fuelConsumption: '5.2L/100km',
            wheelbase: '2712mm', length: '4641mm', width: '1815mm', height: '1450mm',
            trunkVolume: '560L', weight: '1230kg',
            features: ['智能互联', '主动刹车', '疲劳提醒', '倒车影像', '多功能方向盘', '后排出风口'],
            images: ['https://images.unsplash.com/photo-1590362891991-f776e747a588?w=800&h=500&fit=crop', 'https://images.unsplash.com/photo-1583121274602-3e2820c69888?w=800&h=500&fit=crop'],
            interiorImages: ['https://images.unsplash.com/photo-1504215680853-026ed2a45def?w=800&h=500&fit=crop'],
            descriptions: ['轩逸是国内家轿销量王，以舒适性和经济性著称', '日产Multi-Layer人体工学座椅，"移动大沙发"', 'HR16发动机+CVT，百公里油耗仅5.2L']
          }
        ]
      }
    ]
  },
  {
    brand: '奥迪',
    models: [
      {
        name: 'A4L', type: 'luxury',
        trims: [
          {
            name: '奥迪A4L 2024款 40 TFSI 豪华致雅型', year: 2024, price: 368, seats: 5, fuel: '95号汽油', transmission: '自动',
            engine: '2.0T EA888', horsepower: 190, torque: 320, displacement: '2.0T',
            acceleration: '7.6s', topSpeed: '240km/h', fuelConsumption: '6.8L/100km',
            wheelbase: '2908mm', length: '4858mm', width: '1847mm', height: '1439mm',
            trunkVolume: '460L', weight: '1585kg',
            features: ['矩阵大灯', 'B&O音响', '虚拟座舱', 'MMI系统', '座椅加热', '30色氛围灯'],
            images: ['https://images.unsplash.com/photo-1606664515524-ed2f786a0bd6?w=800&h=500&fit=crop', 'https://images.unsplash.com/photo-1617531653342-5a1f8a517b8e?w=800&h=500&fit=crop'],
            interiorImages: ['https://images.unsplash.com/photo-1503376780353-7e6692767b70?w=800&h=500&fit=crop'],
            descriptions: ['奥迪A4L是豪华中型轿车的标杆之一', 'EA888 2.0T发动机+7速S tronic双离合变速箱', '虚拟座舱+MMI触控系统，科技感十足']
          }
        ]
      }
    ]
  },
  {
    brand: '哈弗',
    models: [
      {
        name: 'H6', type: 'suv',
        trims: [
          {
            name: '哈弗H6 2024款 1.5T 自动冠军版', year: 2024, price: 158, seats: 5, fuel: '92号汽油', transmission: '自动',
            engine: '1.5T GW4B15A', horsepower: 169, torque: 285, displacement: '1.5T',
            acceleration: '10.2s', topSpeed: '190km/h', fuelConsumption: '6.8L/100km',
            wheelbase: '2738mm', length: '4703mm', width: '1886mm', height: '1730mm',
            trunkVolume: '600L', weight: '1555kg',
            features: ['全景天窗', '360全景影像', '智能语音', '自动泊车', 'L2辅助驾驶', '无线充电'],
            images: ['https://images.unsplash.com/photo-1609521263047-f8f205293f24?w=800&h=500&fit=crop', 'https://images.unsplash.com/photo-1519245659620-e859806a8d7b?w=800&h=500&fit=crop'],
            interiorImages: ['https://images.unsplash.com/photo-1503376780353-7e6692767b70?w=800&h=500&fit=crop'],
            descriptions: ['哈弗H6连续多年蝉联SUV销量冠军', '1.5T发动机+7DCT湿式双离合，动力平顺', '600L超大后备箱，满足全家出行需求']
          }
        ]
      }
    ]
  }
]

export function getAllBrands(): string[] {
  return carDatabase.map(b => b.brand)
}

export function getModelsByBrand(brand: string): { name: string; type: string }[] {
  const found = carDatabase.find(b => b.brand === brand)
  return found ? found.models.map(m => ({ name: m.name, type: m.type })) : []
}

export function getTrimsByBrandAndModel(brand: string, modelName: string) {
  const brandData = carDatabase.find(b => b.brand === brand)
  if (!brandData) return []
  const modelData = brandData.models.find(m => m.name === modelName)
  return modelData ? modelData.trims : []
}

export function getTrimDetails(brand: string, modelName: string, trimName: string) {
  const brandData = carDatabase.find(b => b.brand === brand)
  if (!brandData) return null
  const modelData = brandData.models.find(m => m.name === modelName)
  if (!modelData) return null
  return modelData.trims.find(t => t.name === trimName) || null
}
