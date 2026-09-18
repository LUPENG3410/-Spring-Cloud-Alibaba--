<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useAdminStore } from '@/stores/admin'
import { fetchAdminCars, createAdminCar, createAdminCarInventory, updateAdminCar, updateAdminCarStatus, deleteAdminCar } from '@/api/admin'
import type { AdminCar } from '@/types/admin'

const adminStore = useAdminStore()

onMounted(async () => {
  try {
    const data = await fetchAdminCars()
    adminStore.cars = data
  } catch { /* fallback to local */ }
})

const searchQuery = ref('')
const filterType = ref<string>('all')
const filterStatus = ref<string>('all')

const typeLabels: Record<string, string> = {
  sedan: '轿车', suv: 'SUV', mpv: 'MPV', luxury: '豪华', sports: '跑车'
}

const statusLabels: Record<string, { label: string; color: string; bg: string }> = {
  available: { label: '可用', color: '#16a34a', bg: '#f0fdf4' },
  rented: { label: '已出租', color: '#2563eb', bg: '#eff6ff' },
  maintenance: { label: '维修中', color: '#ea580c', bg: '#fff7ed' },
}

const filteredCars = computed(() => {
  return adminStore.cars.filter(car => {
    const matchSearch = car.name.includes(searchQuery.value) || car.brand.includes(searchQuery.value)
    const matchType = filterType.value === 'all' || car.type === filterType.value
    const matchStatus = filterStatus.value === 'all' || car.status === filterStatus.value
    return matchSearch && matchType && matchStatus
  })
})

const PAGE_SIZE = 30
const currentPage = ref(1)

const totalPages = computed(() => Math.max(1, Math.ceil(filteredCars.value.length / PAGE_SIZE)))

const paginatedCars = computed(() => {
  const start = (currentPage.value - 1) * PAGE_SIZE
  return filteredCars.value.slice(start, start + PAGE_SIZE)
})

watch([searchQuery, filterType, filterStatus], () => {
  currentPage.value = 1
})

const editingField = ref<{ carId: number; field: string } | null>(null)
const editValue = ref('')
const uploadTarget = ref<{ carId: number } | null>(null)

function startEdit(carId: number, field: string, value: any) {
  editingField.value = { carId, field }
  editValue.value = String(value)
}

async function saveEdit(car: AdminCar) {
  if (!editingField.value) return
  const { carId, field } = editingField.value
  const val = editValue.value
  editingField.value = null

  if (field === 'price') {
    const num = parseInt(val)
    if (isNaN(num) || num <= 0) return
    try {
      await updateAdminCar(carId, { price: num })
      car.price = num
    } catch { console.warn('更新失败') }
  } else if (field === 'mileage') {
    const num = parseInt(val)
    if (isNaN(num) || num < 0) return
    try {
      await updateAdminCar(carId, { mileage: num })
      car.mileage = num
    } catch { console.warn('更新失败') }
  }
}

function cancelEdit() {
  editingField.value = null
}

function triggerImageUpload(carId: number) {
  uploadTarget.value = { carId }
  const input = document.getElementById('inline-image-input') as HTMLInputElement
  if (input) input.click()
}

async function handleInlineImageUpload(event: Event) {
  const input = event.target as HTMLInputElement
  if (!input.files?.length || !uploadTarget.value) return
  const file = input.files[0]
  const carId = uploadTarget.value.carId
  uploadTarget.value = null
  input.value = ''

  try {
    const url = await uploadImage(file)
    await updateAdminCar(carId, { image: url })
    const car = adminStore.cars.find(c => c.id === carId)
    if (car) car.image = url
  } catch (e) {
    console.warn('更新图片失败', e)
    alert('图片更新失败')
  }
}

async function handleStatusChange(carId: number, newStatus: AdminCar['status']) {
  try {
    await updateAdminCarStatus(carId, newStatus)
    adminStore.updateCarStatus(carId, newStatus)
  } catch (e) {
    console.warn('更新车辆状态失败', e)
  }
}

async function handleDelete(carId: number) {
  if (confirm('确定要删除这辆车吗？')) {
    try {
      await deleteAdminCar(carId)
      adminStore.deleteCar(carId)
    } catch (e) {
      console.warn('删除车辆失败', e)
    }
  }
}

const showAddModal = ref(false)
const showInventoryModal = ref(false)
const inventoryTrimId = ref<number | null>(null)
const inventoryForm = ref({
  storeId: 1,
  plateProvince: '京',
  plateLetter: 'A',
  plateNumber: '',
  mileage: 0,
  lastMaintenance: new Date().toISOString().slice(0, 10),
  status: 'available' as AdminCar['status'],
})

const fuelTypes = ['汽油', '柴油', '纯电', '插电混动', '增程式']
const displacementOptions = ['1.0L', '1.0T', '1.2L', '1.4L', '1.4T', '1.5L', '1.5T', '1.6L', '1.8L', '2.0L', '2.0T', '2.5L', '3.0L', '12.0kWh/100km', '13.0kWh/100km', '14.0kWh/100km', '15.0kWh/100km', '16.0kWh/100km', '18.0kWh/100km', '20.0kWh/100km', '其他']
const transmissionOptions = ['手动', '自动', '双离合', 'CVT', 'AMT']
const bodyTypes = ['轿车', 'SUV', 'MPV', '豪华轿车', '跑车']
const seatOptions = ['2座', '4座', '5座', '6座', '7座', '8座']
const fuelGradeOptions = ['92号', '95号', '98号', '0号柴油']
const doorOptions = ['2门', '3门', '4门', '5门']
const driverAssistOptions = ['无', 'L1', 'L2', 'L2+', 'L3']
const smartConnectOptions = ['无', '基础互联', '智能互联', '高级互联']
const seatFunctionOptions = ['无', '座椅加热', '座椅通风', '座椅按摩', '加热+通风', '加热+按摩', '通风+按摩', '加热+通风+按摩']
const plateProvinces = ['京', '津', '沪', '渝', '冀', '豫', '云', '辽', '黑', '湘', '皖', '鲁', '新', '苏', '浙', '鄂', '桂', '甘', '晋', '蒙', '陕', '吉', '闽', '贵', '粤', '川', '青', '藏', '琼', '宁']

const newCar = ref({
  brand: '',
  name: '',
  year: new Date().getFullYear(),
  price: 0,
  province: '北京',
  storeId: 1,
  mileage: 0,
  lastMaintenance: new Date().toISOString().slice(0, 10),
  status: 'available' as AdminCar['status'],
  fuelType: '汽油',
  displacement: '1.5L',
  transmission: '自动',
  bodyType: '轿车',
  seats: '5座',
  fuelGrade: '92号',
  fuelTankCapacity: '',
  trunkVolume: '',
  doors: '4门',
  engine: '',
  horsepower: 0,
  torque: 0,
  acceleration: '',
  topSpeed: '',
  fuelConsumption: '',
  wheelbase: '',
  length: '',
  width: '',
  height: '',
  weight: '',
  reversingCamera: false,
  autoHold: false,
  driverAssist: '无',
  smartConnect: '无',
  electricSeat: false,
  seatFunction: '无',
  descriptions: [] as string[],
  plateProvince: '京',
  plateLetter: 'A',
  plateNumber: '',
})

const activeStores = computed(() => adminStore.stores.filter(s => s.status === 'active'))

const selectedStore = computed(() => adminStore.stores.find(s => s.id === newCar.value.storeId))

const exteriorImages = ref<string[]>([])
const interiorImages = ref<string[]>([])
const uploadingExterior = ref(false)
const uploadingInterior = ref(false)
const pendingUploads = ref<Promise<void>[]>([])

const DEFAULT_IMAGES = {
  exterior: 'https://images.unsplash.com/photo-1541899481282-d53bffe3c35d?w=800&h=500&fit=crop',
  interior: 'https://images.unsplash.com/photo-1503376780353-7e6692767b70?w=800&h=500&fit=crop'
}

async function uploadImage(file: File): Promise<string> {
  const formData = new FormData()
  formData.append('file', file)
  const baseUrl = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api'
  const res = await fetch(`${baseUrl}/upload/image`, {
    method: 'POST',
    headers: { 'Authorization': `Bearer ${localStorage.getItem('admin_token') || ''}` },
    body: formData
  })
  if (!res.ok) {
    throw new Error(`上传失败(${res.status})`)
  }
  const data = await res.json()
  if (data.code === 200) return data.data.url
  throw new Error(data.message || '上传失败')
}

function handleExteriorUpload(event: Event) {
  const input = event.target as HTMLInputElement
  if (!input.files?.length) return
  uploadingExterior.value = true
  const file = input.files[0]
  const localUrl = URL.createObjectURL(file)
  exteriorImages.value.push(localUrl)
  const uploadPromise = uploadImage(file).then(url => {
    const idx = exteriorImages.value.indexOf(localUrl)
    if (idx !== -1) exteriorImages.value[idx] = url
  }).catch(e => {
    console.error('外观图片上传失败', e)
    alert('图片上传失败: ' + (e.message || '请检查网络'))
    const idx = exteriorImages.value.indexOf(localUrl)
    if (idx !== -1) exteriorImages.value.splice(idx, 1)
  }).finally(() => {
    uploadingExterior.value = false
    input.value = ''
  })
  pendingUploads.value.push(uploadPromise)
}

function handleInteriorUpload(event: Event) {
  const input = event.target as HTMLInputElement
  if (!input.files?.length) return
  uploadingInterior.value = true
  const file = input.files[0]
  const localUrl = URL.createObjectURL(file)
  interiorImages.value.push(localUrl)
  const uploadPromise = uploadImage(file).then(url => {
    const idx = interiorImages.value.indexOf(localUrl)
    if (idx !== -1) interiorImages.value[idx] = url
  }).catch(e => {
    console.error('内饰图片上传失败', e)
    alert('图片上传失败: ' + (e.message || '请检查网络'))
    const idx = interiorImages.value.indexOf(localUrl)
    if (idx !== -1) interiorImages.value.splice(idx, 1)
  }).finally(() => {
    uploadingInterior.value = false
    input.value = ''
  })
  pendingUploads.value.push(uploadPromise)
}

function removeExteriorImage(index: number) {
  exteriorImages.value.splice(index, 1)
}

function removeInteriorImage(index: number) {
  interiorImages.value.splice(index, 1)
}

async function handleAddCar() {
  if (!newCar.value.brand || !newCar.value.name || !newCar.value.price) return
  await Promise.all(pendingUploads.value)

  const bodyTypeMap: Record<string, AdminCar['type']> = {
    '轿车': 'sedan', 'SUV': 'suv', 'MPV': 'mpv', '豪华轿车': 'luxury', '跑车': 'sports'
  }

  const fuelText = newCar.value.fuelType === '汽油' ? newCar.value.fuelGrade + '汽油' : newCar.value.fuelType

  const finalExterior = exteriorImages.value.length > 0 ? exteriorImages.value : [DEFAULT_IMAGES.exterior]
  const finalInterior = interiorImages.value.length > 0 ? interiorImages.value : [DEFAULT_IMAGES.interior]

  const store = adminStore.stores.find(s => s.id === newCar.value.storeId)

  const carData: Omit<AdminCar, 'id'> = {
    name: newCar.value.name,
    brand: newCar.value.brand,
    type: bodyTypeMap[newCar.value.bodyType] || 'sedan',
    image: finalExterior[0],
    price: newCar.value.price,
    seats: parseInt(newCar.value.seats),
    fuel: fuelText,
    transmission: newCar.value.transmission,
    available: true,
    features: buildFeatures(),
    province: store?.province || newCar.value.province,
    storeId: newCar.value.storeId,
    mileage: newCar.value.mileage,
    lastMaintenance: newCar.value.lastMaintenance,
    status: newCar.value.status,
    year: newCar.value.year,
    engine: newCar.value.engine || `${newCar.value.displacement} ${newCar.value.transmission === '自动' ? 'AT' : 'MT'}`,
    horsepower: newCar.value.horsepower,
    torque: newCar.value.torque,
    displacement: newCar.value.displacement,
    acceleration: newCar.value.acceleration,
    topSpeed: newCar.value.topSpeed,
    fuelConsumption: newCar.value.fuelConsumption,
    wheelbase: newCar.value.wheelbase,
    length: newCar.value.length,
    width: newCar.value.width,
    height: newCar.value.height,
    trunkVolume: newCar.value.trunkVolume,
    weight: newCar.value.weight,
    images: finalExterior,
    interiorImages: finalInterior,
    descriptions: newCar.value.descriptions.filter(d => d.trim()),
    fuelType: newCar.value.fuelType,
    fuelGrade: newCar.value.fuelType === '汽油' ? newCar.value.fuelGrade : '',
    fuelTankCapacity: newCar.value.fuelTankCapacity,
    doors: newCar.value.doors,
    reversingCamera: newCar.value.reversingCamera,
    autoHold: newCar.value.autoHold,
    driverAssist: newCar.value.driverAssist,
    smartConnect: newCar.value.smartConnect,
    electricSeat: newCar.value.electricSeat,
    seatFunction: newCar.value.seatFunction,
    plateProvince: newCar.value.plateProvince,
    plateLetter: newCar.value.plateLetter,
    plateNumber: newCar.value.plateNumber,
  }

  try {
    const created = await createAdminCar(carData as any)
    adminStore.addCar(created)
  } catch (e) {
    console.warn('添加车辆失败', e)
    adminStore.addCar(carData)
  }
  resetForm()
}

function buildFeatures(): string[] {
  const features: string[] = []
  if (newCar.value.reversingCamera) features.push('倒车影像')
  if (newCar.value.autoHold) features.push('自动驻车')
  if (newCar.value.driverAssist !== '无') features.push(`${newCar.value.driverAssist}辅助驾驶`)
  if (newCar.value.smartConnect !== '无') features.push(newCar.value.smartConnect)
  if (newCar.value.electricSeat) features.push('电动座椅')
  if (newCar.value.seatFunction !== '无') features.push(newCar.value.seatFunction)
  return features
}

function resetForm() {
  showAddModal.value = false
  exteriorImages.value = []
  interiorImages.value = []
  pendingUploads.value = []
  newCar.value = {
    brand: '', name: '', year: new Date().getFullYear(), price: 0,
    province: '北京', storeId: 1, mileage: 0, lastMaintenance: new Date().toISOString().slice(0, 10),
    status: 'available', fuelType: '汽油', displacement: '1.5L', transmission: '自动',
    bodyType: '轿车', seats: '5座', fuelGrade: '92号', fuelTankCapacity: '',
    trunkVolume: '', doors: '4门', engine: '', horsepower: 0, torque: 0,
    acceleration: '', topSpeed: '', fuelConsumption: '', wheelbase: '',
    length: '', width: '', height: '', weight: '',
    reversingCamera: false, autoHold: false, driverAssist: '无',
    smartConnect: '无', electricSeat: false, seatFunction: '无',
    descriptions: [],
    plateProvince: '京', plateLetter: 'A', plateNumber: '',
  }
}

function openInventoryModal(trimId: number) {
  inventoryTrimId.value = trimId
  inventoryForm.value = {
    storeId: adminStore.stores.find(s => s.status === 'active')?.id || 1,
    plateProvince: '京',
    plateLetter: 'A',
    plateNumber: '',
    mileage: 0,
    lastMaintenance: new Date().toISOString().slice(0, 10),
    status: 'available',
  }
  showInventoryModal.value = true
}

async function handleAddInventory() {
  if (!inventoryTrimId.value || !inventoryForm.value.plateNumber) return
  try {
    const created = await createAdminCarInventory({
      trimId: inventoryTrimId.value,
      storeId: inventoryForm.value.storeId,
      plateProvince: inventoryForm.value.plateProvince,
      plateLetter: inventoryForm.value.plateLetter,
      plateNumber: inventoryForm.value.plateNumber,
      mileage: inventoryForm.value.mileage,
      lastMaintenance: inventoryForm.value.lastMaintenance,
      status: inventoryForm.value.status,
    })
    adminStore.addCar(created)
    showInventoryModal.value = false
  } catch (e) {
    console.warn('添加库存车辆失败', e)
    alert('添加失败，请检查车牌号是否重复')
  }
}
</script>

<template>
  <div class="admin-cars">
    <div class="page-header">
      <div>
        <h1>车辆管理</h1>
        <p>管理所有车辆信息和状态</p>
      </div>
      <button class="btn-primary" @click="showAddModal = true">+ 添加车辆</button>
    </div>

    <div class="filters">
      <input v-model="searchQuery" type="text" placeholder="搜索车辆名称/品牌..." class="search-input" />
      <select v-model="filterType" class="filter-select">
        <option value="all">全部类型</option>
        <option value="sedan">轿车</option>
        <option value="suv">SUV</option>
        <option value="mpv">MPV</option>
        <option value="luxury">豪华</option>
        <option value="sports">跑车</option>
      </select>
      <select v-model="filterStatus" class="filter-select">
        <option value="all">全部状态</option>
        <option value="available">可用</option>
        <option value="rented">已出租</option>
        <option value="maintenance">维修中</option>
      </select>
    </div>

    <input id="inline-image-input" type="file" accept="image/*" @change="handleInlineImageUpload" style="display:none" />

    <div class="table-wrap">
      <table class="data-table">
        <thead>
          <tr>
            <th>车辆信息</th>
            <th>类型</th>
            <th>日租金</th>
            <th>车牌号</th>
            <th>里程数</th>
            <th>状态</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="car in paginatedCars" :key="car.id">
            <td>
              <div class="car-cell">
                <img :src="car.image" class="car-thumb clickable-thumb" :alt="car.name" @click="triggerImageUpload(car.id)" title="点击更换图片" />
                <div>
                  <span class="car-name">{{ car.name }}</span>
                  <span class="car-brand">{{ car.brand }}</span>
                </div>
              </div>
            </td>
            <td><span class="type-badge">{{ typeLabels[car.type] }}</span></td>
            <td class="price">
              <template v-if="editingField?.carId === car.id && editingField?.field === 'price'">
                <input v-model="editValue" type="number" class="inline-edit" @keyup.enter="saveEdit(car)" @keyup.esc="cancelEdit" @blur="saveEdit(car)" autofocus />
              </template>
              <template v-else>
                <span class="editable" @click="startEdit(car.id, 'price', car.price)">¥{{ car.price }}/天</span>
              </template>
            </td>
            <td><span class="plate-number">{{ car.plateNumber }}</span></td>
            <td>
              <template v-if="editingField?.carId === car.id && editingField?.field === 'mileage'">
                <input v-model="editValue" type="number" class="inline-edit" @keyup.enter="saveEdit(car)" @keyup.esc="cancelEdit" @blur="saveEdit(car)" autofocus />
              </template>
              <template v-else>
                <span class="editable" @click="startEdit(car.id, 'mileage', car.mileage)">{{ car.mileage.toLocaleString() }}km</span>
              </template>
            </td>
            <td>
              <select
                :value="car.status"
                @change="handleStatusChange(car.id, ($event.target as HTMLSelectElement).value as AdminCar['status'])"
                class="status-select"
                :style="{ color: statusLabels[car.status]?.color, background: statusLabels[car.status]?.bg }"
              >
                <option value="available">可用</option>
                <option value="rented">已出租</option>
                <option value="maintenance">维修中</option>
              </select>
            </td>
            <td>
              <div class="actions-cell">
                <button class="btn-text" @click="openInventoryModal(car.trimId)">+ 库存</button>
                <button class="btn-text danger" @click="handleDelete(car.id)">删除</button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <div class="pagination" v-if="filteredCars.length > PAGE_SIZE">
      <span class="page-info">共 {{ filteredCars.length }} 辆，第 {{ currentPage }}/{{ totalPages }} 页</span>
      <div class="page-buttons">
        <button class="page-btn" :disabled="currentPage <= 1" @click="currentPage--">上一页</button>
        <button
          v-for="p in totalPages"
          :key="p"
          class="page-btn"
          :class="{ active: p === currentPage }"
          @click="currentPage = p"
        >{{ p }}</button>
        <button class="page-btn" :disabled="currentPage >= totalPages" @click="currentPage++">下一页</button>
      </div>
    </div>

    <!-- Add Car Modal -->
    <div class="modal-overlay" v-if="showAddModal" @click.self="resetForm">
      <div class="modal add-modal">
        <div class="modal-header">
          <h2>添加车辆</h2>
          <button class="close-btn" @click="resetForm">&times;</button>
        </div>
        <div class="modal-body">
          <div class="form-section">
            <h3>基本信息</h3>
            <div class="form-row">
              <div class="form-group">
                <label>品牌 <span class="required">*</span></label>
                <input v-model="newCar.brand" type="text" placeholder="如：大众、丰田、宝马" />
              </div>
              <div class="form-group">
                <label>车型名称 <span class="required">*</span></label>
                <input v-model="newCar.name" type="text" placeholder="如：朗逸、凯美瑞" />
              </div>
              <div class="form-group">
                <label>年款</label>
                <input v-model.number="newCar.year" type="number" />
              </div>
            </div>
            <div class="form-row">
              <div class="form-group">
                <label>日租金(元) <span class="required">*</span></label>
                <input v-model.number="newCar.price" type="number" />
              </div>
              <div class="form-group">
                <label>所属门店 <span class="required">*</span></label>
                <select v-model.number="newCar.storeId">
                  <option v-for="store in activeStores" :key="store.id" :value="store.id">
                    {{ store.name }} ({{ store.province }}{{ store.city }})
                  </option>
                </select>
              </div>
              <div class="form-group" v-if="selectedStore">
                <label>门店地址</label>
                <input :value="selectedStore.address" type="text" disabled class="disabled-input" />
              </div>
            </div>
            <div class="form-row">
              <div class="form-group">
                <label>车牌号省份</label>
                <select v-model="newCar.plateProvince">
                  <option v-for="p in plateProvinces" :key="p" :value="p">{{ p }}</option>
                </select>
              </div>
              <div class="form-group">
                <label>车牌字母</label>
                <select v-model="newCar.plateLetter">
                  <option v-for="l in 'ABCDEFGHJKLMNPQRSTUVWXYZ'.split('')" :key="l" :value="l">{{ l }}</option>
                </select>
              </div>
              <div class="form-group">
                <label>车牌号数字</label>
                <input v-model="newCar.plateNumber" type="text" maxlength="5" placeholder="如：12345" />
              </div>
            </div>
          </div>

          <div class="form-section">
            <h3>车辆描述</h3>
            <p class="upload-hint" style="margin-bottom:12px">添加车辆卖点或特色描述，每条一行，将展示在车辆详情页</p>
            <div class="descriptions-input">
              <div v-for="(desc, idx) in newCar.descriptions" :key="idx" class="desc-row">
                <input v-model="newCar.descriptions[idx]" type="text" placeholder="如：全景天窗，真皮座椅" />
                <button class="desc-remove-btn" @click="newCar.descriptions.splice(idx, 1)">&times;</button>
              </div>
              <button class="desc-add-btn" @click="newCar.descriptions.push('')">+ 添加描述</button>
            </div>
          </div>

          <div class="form-section">
            <h3>车辆图片</h3>
            <div class="image-upload-group">
              <div class="image-upload-section">
                <label class="upload-label">外观图片</label>
                <p class="upload-hint">支持 JPG/PNG，建议尺寸 800x500，最多上传5张</p>
                <div class="image-preview-list">
                  <div v-for="(img, idx) in exteriorImages" :key="idx" class="image-preview-item">
                    <img :src="img" class="preview-thumb" />
                    <button class="remove-btn" @click="removeExteriorImage(idx)">&times;</button>
                  </div>
                  <label class="upload-btn" v-if="exteriorImages.length < 5">
                    <input type="file" accept="image/*" @change="handleExteriorUpload" hidden />
                    <span v-if="uploadingExterior" class="uploading">上传中...</span>
                    <span v-else class="upload-icon">+</span>
                  </label>
                </div>
              </div>
              <div class="image-upload-section">
                <label class="upload-label">内饰图片</label>
                <p class="upload-hint">支持 JPG/PNG，建议尺寸 800x500，最多上传5张</p>
                <div class="image-preview-list">
                  <div v-for="(img, idx) in interiorImages" :key="idx" class="image-preview-item">
                    <img :src="img" class="preview-thumb" />
                    <button class="remove-btn" @click="removeInteriorImage(idx)">&times;</button>
                  </div>
                  <label class="upload-btn" v-if="interiorImages.length < 5">
                    <input type="file" accept="image/*" @change="handleInteriorUpload" hidden />
                    <span v-if="uploadingInterior" class="uploading">上传中...</span>
                    <span v-else class="upload-icon">+</span>
                  </label>
                </div>
              </div>
            </div>
          </div>

          <div class="form-section">
            <h3>能源与动力</h3>
            <div class="form-row">
              <div class="form-group">
                <label>能源类型 <span class="required">*</span></label>
                <select v-model="newCar.fuelType">
                  <option v-for="f in fuelTypes" :key="f">{{ f }}</option>
                </select>
              </div>
              <div class="form-group">
                <label>排量/电耗</label>
                <select v-model="newCar.displacement">
                  <option v-for="d in displacementOptions" :key="d">{{ d }}</option>
                </select>
              </div>
              <div class="form-group">
                <label>变速箱</label>
                <select v-model="newCar.transmission">
                  <option v-for="t in transmissionOptions" :key="t">{{ t }}</option>
                </select>
              </div>
            </div>
            <div class="form-row" v-if="newCar.fuelType === '汽油'">
              <div class="form-group">
                <label>燃油标号</label>
                <select v-model="newCar.fuelGrade">
                  <option v-for="g in fuelGradeOptions" :key="g">{{ g }}</option>
                </select>
              </div>
              <div class="form-group">
                <label>油箱容积(L)</label>
                <input v-model="newCar.fuelTankCapacity" type="text" placeholder="如：50" />
              </div>
              <div class="form-group">
                <label>发动机型号</label>
                <input v-model="newCar.engine" type="text" placeholder="如：1.5L EA211" />
              </div>
            </div>
            <div class="form-row">
              <div class="form-group">
                <label>最大马力(匹)</label>
                <input v-model.number="newCar.horsepower" type="number" />
              </div>
              <div class="form-group">
                <label>最大扭矩(N·m)</label>
                <input v-model.number="newCar.torque" type="number" />
              </div>
              <div class="form-group">
                <label>综合油耗(L/100km)</label>
                <input v-model="newCar.fuelConsumption" type="text" placeholder="如：6.5" />
              </div>
            </div>
          </div>

          <div class="form-section">
            <h3>车身结构</h3>
            <div class="form-row">
              <div class="form-group">
                <label>车身类型</label>
                <select v-model="newCar.bodyType">
                  <option v-for="b in bodyTypes" :key="b">{{ b }}</option>
                </select>
              </div>
              <div class="form-group">
                <label>座位数</label>
                <select v-model="newCar.seats">
                  <option v-for="s in seatOptions" :key="s">{{ s }}</option>
                </select>
              </div>
              <div class="form-group">
                <label>车门数</label>
                <select v-model="newCar.doors">
                  <option v-for="d in doorOptions" :key="d">{{ d }}</option>
                </select>
              </div>
            </div>
            <div class="form-row">
              <div class="form-group">
                <label>行李箱容积(L)</label>
                <input v-model="newCar.trunkVolume" type="text" placeholder="如：510" />
              </div>
              <div class="form-group">
                <label>轴距(mm)</label>
                <input v-model="newCar.wheelbase" type="text" placeholder="如：2688" />
              </div>
              <div class="form-group">
                <label>整备质量(kg)</label>
                <input v-model="newCar.weight" type="text" placeholder="如：1280" />
              </div>
            </div>
            <div class="form-row">
              <div class="form-group">
                <label>车长(mm)</label>
                <input v-model="newCar.length" type="text" placeholder="如：4678" />
              </div>
              <div class="form-group">
                <label>车宽(mm)</label>
                <input v-model="newCar.width" type="text" placeholder="如：1806" />
              </div>
              <div class="form-group">
                <label>车高(mm)</label>
                <input v-model="newCar.height" type="text" placeholder="如：1474" />
              </div>
            </div>
          </div>

          <div class="form-section">
            <h3>性能参数</h3>
            <div class="form-row">
              <div class="form-group">
                <label>0-100km/h加速(秒)</label>
                <input v-model="newCar.acceleration" type="text" placeholder="如：9.5" />
              </div>
              <div class="form-group">
                <label>最高时速(km/h)</label>
                <input v-model="newCar.topSpeed" type="text" placeholder="如：200" />
              </div>
              <div class="form-group">
                <label>当前里程(km)</label>
                <input v-model.number="newCar.mileage" type="number" />
              </div>
            </div>
            <div class="form-row">
              <div class="form-group">
                <label>最近保养日期</label>
                <input v-model="newCar.lastMaintenance" type="date" />
              </div>
            </div>
          </div>

          <div class="form-section">
            <h3>配置信息</h3>
            <div class="form-row">
              <div class="form-group">
                <label>倒车影像</label>
                <div class="toggle-group">
                  <button :class="{ active: newCar.reversingCamera }" @click="newCar.reversingCamera = true">有</button>
                  <button :class="{ active: !newCar.reversingCamera }" @click="newCar.reversingCamera = false">无</button>
                </div>
              </div>
              <div class="form-group">
                <label>自动驻车</label>
                <div class="toggle-group">
                  <button :class="{ active: newCar.autoHold }" @click="newCar.autoHold = true">有</button>
                  <button :class="{ active: !newCar.autoHold }" @click="newCar.autoHold = false">无</button>
                </div>
              </div>
              <div class="form-group">
                <label>电动座椅</label>
                <div class="toggle-group">
                  <button :class="{ active: newCar.electricSeat }" @click="newCar.electricSeat = true">有</button>
                  <button :class="{ active: !newCar.electricSeat }" @click="newCar.electricSeat = false">无</button>
                </div>
              </div>
            </div>
            <div class="form-row">
              <div class="form-group">
                <label>辅助驾驶类型</label>
                <select v-model="newCar.driverAssist">
                  <option v-for="d in driverAssistOptions" :key="d">{{ d }}</option>
                </select>
              </div>
              <div class="form-group">
                <label>智能互联</label>
                <select v-model="newCar.smartConnect">
                  <option v-for="s in smartConnectOptions" :key="s">{{ s }}</option>
                </select>
              </div>
              <div class="form-group">
                <label>座椅功能</label>
                <select v-model="newCar.seatFunction">
                  <option v-for="s in seatFunctionOptions" :key="s">{{ s }}</option>
                </select>
              </div>
            </div>
          </div>
        </div>

        <div class="modal-footer">
          <button class="btn-secondary" @click="resetForm">取消</button>
          <button class="btn-primary" @click="handleAddCar" :disabled="!newCar.brand || !newCar.name || !newCar.price">确认添加</button>
        </div>
      </div>
    </div>

    <!-- 添加库存弹窗 -->
    <div class="modal-overlay" v-if="showInventoryModal" @click.self="showInventoryModal = false">
      <div class="modal inventory-modal">
        <div class="modal-header">
          <h2>添加库存车辆</h2>
          <button class="close-btn" @click="showInventoryModal = false">&times;</button>
        </div>
        <div class="modal-body">
          <div class="form-row">
            <div class="form-group">
              <label>所属门店 <span class="required">*</span></label>
              <select v-model.number="inventoryForm.storeId">
                <option v-for="store in activeStores" :key="store.id" :value="store.id">
                  {{ store.name }} ({{ store.province }}{{ store.city }})
                </option>
              </select>
            </div>
          </div>
          <div class="form-row">
            <div class="form-group">
              <label>车牌号省份</label>
              <select v-model="inventoryForm.plateProvince">
                <option v-for="p in plateProvinces" :key="p" :value="p">{{ p }}</option>
              </select>
            </div>
            <div class="form-group">
              <label>车牌字母</label>
              <select v-model="inventoryForm.plateLetter">
                <option v-for="l in 'ABCDEFGHJKLMNPQRSTUVWXYZ'.split('')" :key="l" :value="l">{{ l }}</option>
              </select>
            </div>
            <div class="form-group">
              <label>车牌号数字 <span class="required">*</span></label>
              <input v-model="inventoryForm.plateNumber" type="text" maxlength="5" placeholder="如：12345" />
            </div>
          </div>
          <div class="form-row">
            <div class="form-group">
              <label>当前里程(km)</label>
              <input v-model.number="inventoryForm.mileage" type="number" />
            </div>
            <div class="form-group">
              <label>最近保养日期</label>
              <input v-model="inventoryForm.lastMaintenance" type="date" />
            </div>
            <div class="form-group">
              <label>状态</label>
              <select v-model="inventoryForm.status">
                <option value="available">可用</option>
                <option value="maintenance">维修中</option>
              </select>
            </div>
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn-secondary" @click="showInventoryModal = false">取消</button>
          <button class="btn-primary" @click="handleAddInventory" :disabled="!inventoryForm.plateNumber">确认添加</button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.admin-cars { max-width: 1200px; }

.page-header {
  display: flex; justify-content: space-between; align-items: flex-start;
  margin-bottom: 24px;
}
.page-header h1 { font-size: 24px; font-weight: 800; color: #0f172a; }
.page-header p { font-size: 14px; color: #94a3b8; margin-top: 4px; }

.btn-primary {
  background: linear-gradient(135deg, #0891b2, #06b6d4);
  color: #fff; border: none; padding: 10px 20px; border-radius: 10px;
  font-size: 14px; font-weight: 600; cursor: pointer; transition: 0.2s;
}
.btn-primary:hover { transform: translateY(-1px); box-shadow: 0 4px 12px rgba(8,145,178,0.3); }
.btn-primary:disabled { opacity: 0.5; cursor: not-allowed; transform: none; }

.filters { display: flex; gap: 12px; margin-bottom: 20px; }
.search-input {
  flex: 1; padding: 10px 16px; border: 1.5px solid #e2e8f0;
  border-radius: 10px; font-size: 14px; outline: none; transition: 0.15s;
}
.search-input:focus { border-color: #0891b2; }
.filter-select {
  padding: 10px 16px; border: 1.5px solid #e2e8f0;
  border-radius: 10px; font-size: 14px; outline: none; background: #fff;
}

.table-wrap {
  background: #fff; border-radius: 14px; overflow: hidden;
  box-shadow: 0 1px 3px rgba(0,0,0,0.04);
}
.data-table { width: 100%; border-collapse: collapse; }
.data-table th {
  text-align: left; padding: 14px 16px; font-size: 12px;
  font-weight: 600; color: #94a3b8; text-transform: uppercase;
  letter-spacing: 0.5px; background: #f8fafc; border-bottom: 1px solid #f1f5f9;
}
.data-table td { padding: 14px 16px; border-bottom: 1px solid #f1f5f9; font-size: 14px; color: #334155; }
.data-table tr:hover td { background: #f8fafc; }
.car-cell { display: flex; align-items: center; gap: 12px; }
.car-thumb { width: 56px; height: 36px; border-radius: 6px; object-fit: cover; }
.car-name { display: block; font-weight: 600; color: #0f172a; }
.car-brand { display: block; font-size: 12px; color: #94a3b8; }
.type-badge { font-size: 12px; font-weight: 600; padding: 4px 10px; border-radius: 6px; background: #f1f5f9; color: #64748b; }
.price { font-weight: 700; color: #0f172a; }
.status-select { border: none; padding: 5px 10px; border-radius: 6px; font-size: 12px; font-weight: 600; cursor: pointer; outline: none; }
.btn-text { background: none; border: none; font-size: 13px; font-weight: 500; cursor: pointer; padding: 4px 8px; border-radius: 6px; }
.btn-text.danger { color: #ef4444; }
.btn-text.danger:hover { background: #fef2f2; }
.actions-cell { display: flex; gap: 6px; }
.inventory-modal { width: 520px; }
.inventory-modal .modal-body { color: #1e293b; }
.inventory-modal .form-group label { color: #0f172a; font-weight: 700; }
.inventory-modal .form-group input,
.inventory-modal .form-group select { color: #0f172a; background: #fff; }
.plate-number { font-family: monospace; font-weight: 600; color: #1e40af; background: #eff6ff; padding: 2px 8px; border-radius: 4px; }

.editable {
  cursor: pointer; padding: 2px 6px; border-radius: 4px; transition: 0.15s;
}
.editable:hover { background: #f0fdfa; color: #0891b2; }

.inline-edit {
  width: 90px; padding: 4px 8px; border: 1.5px solid #0891b2; border-radius: 6px;
  font-size: 14px; font-weight: 600; outline: none; background: #fff;
}

.clickable-thumb { cursor: pointer; transition: 0.15s; }
.clickable-thumb:hover { opacity: 0.7; box-shadow: 0 0 0 2px #0891b2; }

.pagination {
  display: flex; justify-content: space-between; align-items: center;
  margin-top: 16px; padding: 12px 0;
}
.page-info { font-size: 13px; color: #64748b; }
.page-buttons { display: flex; gap: 6px; }
.page-btn {
  padding: 6px 12px; border: 1.5px solid #e2e8f0; border-radius: 8px;
  background: #fff; font-size: 13px; color: #334155; cursor: pointer;
  transition: 0.15s;
}
.page-btn:hover:not(:disabled) { border-color: #0891b2; color: #0891b2; }
.page-btn.active { background: #0891b2; color: #fff; border-color: #0891b2; }
.page-btn:disabled { opacity: 0.4; cursor: not-allowed; }

/* Modal */
.modal-overlay {
  position: fixed; inset: 0; background: rgba(0,0,0,0.75);
  display: flex; align-items: center; justify-content: center; z-index: 1000;
}
.add-modal {
  background: #fff; border-radius: 16px; width: 900px; max-height: 85vh;
  display: flex; flex-direction: column; overflow: hidden;
}
.modal-header {
  display: flex; justify-content: space-between; align-items: center;
  padding: 20px 24px; border-bottom: 1px solid #f1f5f9;
}
.modal-header h2 { font-size: 18px; font-weight: 700; color: #0f172a; }
.close-btn {
  background: none; border: none; font-size: 24px; color: #94a3b8;
  cursor: pointer; padding: 0 4px; line-height: 1;
}
.close-btn:hover { color: #334155; }

.modal-body {
  flex: 1; overflow-y: auto; padding: 20px 24px;
}

.form-section {
  margin-bottom: 24px;
}
.form-section h3 {
  font-size: 14px; font-weight: 700; color: #0891b2;
  margin-bottom: 14px; padding-bottom: 8px; border-bottom: 1px solid #f1f5f9;
}

.form-row {
  display: grid; grid-template-columns: repeat(3, 1fr); gap: 16px;
  margin-bottom: 14px;
}
.form-group label {
  display: block; font-size: 13px; font-weight: 600; color: #334155; margin-bottom: 6px;
}
.required { color: #ef4444; }
.form-group input,
.form-group select {
  width: 100%; padding: 10px 14px; border: 1.5px solid #e2e8f0;
  border-radius: 8px; font-size: 14px; outline: none; transition: 0.15s;
}
.form-group input:focus,
.form-group select:focus { border-color: #0891b2; box-shadow: 0 0 0 3px rgba(8,145,178,0.08); }
.disabled-input { background: #f8fafc; color: #94a3b8; cursor: not-allowed; }

.toggle-group {
  display: flex; gap: 0; border: 1.5px solid #e2e8f0; border-radius: 8px; overflow: hidden;
}
.toggle-group button {
  flex: 1; padding: 10px; border: none; background: #f8fafc;
  font-size: 13px; font-weight: 500; color: #64748b; cursor: pointer; transition: 0.15s;
}
.toggle-group button.active {
  background: #0891b2; color: #fff;
}

/* Descriptions Input */
.descriptions-input { display: flex; flex-direction: column; gap: 8px; }
.desc-row { display: flex; gap: 8px; align-items: center; }
.desc-row input {
  flex: 1; padding: 10px 14px; border: 1.5px solid #e2e8f0;
  border-radius: 8px; font-size: 14px; outline: none; transition: 0.15s;
}
.desc-row input:focus { border-color: #0891b2; box-shadow: 0 0 0 3px rgba(8,145,178,0.08); }
.desc-remove-btn {
  width: 32px; height: 32px; border-radius: 8px; border: 1.5px solid #e2e8f0;
  background: #fff; color: #94a3b8; font-size: 16px; cursor: pointer;
  display: flex; align-items: center; justify-content: center; flex-shrink: 0;
}
.desc-remove-btn:hover { border-color: #ef4444; color: #ef4444; background: #fef2f2; }
.desc-add-btn {
  padding: 10px 16px; border: 1.5px dashed #cbd5e1; border-radius: 8px;
  background: #f8fafc; color: #0891b2; font-size: 13px; font-weight: 600;
  cursor: pointer; transition: 0.15s; text-align: center;
}
.desc-add-btn:hover { border-color: #0891b2; background: #f0fdfa; }

/* Image Upload */
.image-upload-group {
  display: flex; flex-direction: column; gap: 20px;
}
.image-upload-section {
  background: #f8fafc; border-radius: 10px; padding: 16px;
  border: 1px solid #e2e8f0;
}
.upload-label {
  display: block; font-size: 13px; font-weight: 600; color: #334155; margin-bottom: 4px;
}
.upload-hint {
  font-size: 12px; color: #94a3b8; margin-bottom: 12px;
}
.image-preview-list {
  display: flex; gap: 10px; flex-wrap: wrap;
}
.image-preview-item {
  position: relative; width: 120px; height: 80px; border-radius: 8px; overflow: hidden;
  border: 1.5px solid #e2e8f0;
}
.preview-thumb {
  width: 100%; height: 100%; object-fit: cover;
}
.remove-btn {
  position: absolute; top: 4px; right: 4px; width: 20px; height: 20px;
  border-radius: 50%; background: rgba(0,0,0,0.6); color: #fff;
  border: none; font-size: 14px; cursor: pointer; display: flex;
  align-items: center; justify-content: center; line-height: 1;
}
.remove-btn:hover { background: rgba(220,38,38,0.9); }
.upload-btn {
  width: 120px; height: 80px; border-radius: 8px;
  border: 2px dashed #cbd5e1; display: flex; align-items: center;
  justify-content: center; cursor: pointer; transition: 0.15s;
}
.upload-btn:hover { border-color: #0891b2; background: #f0fdfa; }
.upload-icon {
  font-size: 24px; color: #94a3b8; font-weight: 300;
}
.uploading {
  font-size: 12px; color: #94a3b8;
}

.modal-footer {
  display: flex; justify-content: flex-end; gap: 12px;
  padding: 16px 24px; border-top: 1px solid #f1f5f9;
}
.btn-secondary {
  padding: 10px 20px; border: 1.5px solid #e2e8f0; border-radius: 10px;
  background: #fff; font-size: 14px; font-weight: 500; cursor: pointer;
}
</style>
