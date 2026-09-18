import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '../views/HomeView.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    { path: '/', name: 'home', component: HomeView },
    { path: '/cars', name: 'cars', component: () => import('../views/CarList.vue') },
    { path: '/hitch', name: 'hitch-rent', component: () => import('../views/HitchRent.vue') },
    { path: '/car/:id/:province?', name: 'car-detail', component: () => import('../views/CarDetail.vue') },
    { path: '/booking/:id/:province?', name: 'car-booking', component: () => import('../views/CarBooking.vue') },
    { path: '/bookings', name: 'my-bookings', component: () => import('../views/MyBookings.vue') },
    { path: '/cross-province', name: 'cross-province', component: () => import('../views/CrossProvince.vue') },
    { path: '/stores', name: 'store-locator', component: () => import('../views/StoreLocator.vue') },
    { path: '/profile', name: 'user-profile', component: () => import('../views/UserProfile.vue') },
    { path: '/service', name: 'service-chat', component: () => import('../views/ServiceChat.vue') },
    { path: '/login', name: 'user-login', component: () => import('../views/UserLogin.vue') },
    { path: '/register', name: 'user-register', component: () => import('../views/UserRegister.vue') },

    { path: '/admin/login', name: 'admin-login', component: () => import('../views/admin/AdminLogin.vue') },
    {
      path: '/admin',
      component: () => import('../views/admin/AdminLayout.vue'),
      children: [
        { path: '', name: 'admin-dashboard', component: () => import('../views/admin/AdminDashboard.vue') },
        { path: 'cars', name: 'admin-cars', component: () => import('../views/admin/AdminCars.vue') },
        { path: 'bookings', name: 'admin-bookings', component: () => import('../views/admin/AdminBookings.vue') },
        { path: 'users', name: 'admin-users', component: () => import('../views/admin/AdminUsers.vue') },
        { path: 'stores', name: 'admin-stores', component: () => import('../views/admin/AdminStores.vue') },
        { path: 'maintenance', name: 'admin-maintenance', component: () => import('../views/admin/AdminMaintenance.vue') },
      ],
    },
  ],
  scrollBehavior() { return { top: 0 } },
})

export default router
