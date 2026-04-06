
import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

const routes = [
    {
        path: '/',
        redirect: '/login'
    },
    {
        path: '/login',
        name: 'Login',
        component: () => import('@/views/Login.vue'),
        meta: { requiresAuth: false }
    },
    {
        path: '/register',
        name: 'Register',
        component: () => import('@/views/Register.vue'),
        meta: { requiresAuth: false }
    },
    {
        path: '/home',
        name: 'Home',
        component: () => import('@/views/Home.vue'),
        meta: { requiresAuth: true }
    },
    {
        path: '/products',
        name: 'Products',
        component: () => import('@/views/Products.vue'),
        meta: { requiresAuth: true }
    },
    {
        path: '/products/add',
        name: 'AddProduct',
        component: () => import('@/views/AddProduct.vue'),
        meta: { requiresAuth: true }
    },
    {
        path: '/product/:id',
        name: 'ProductDetail',
        component: () => import('@/views/ProductDetail.vue'),
        meta: { requiresAuth: true }
    },
    {
        path: '/profile',
        name: 'Profile',
        component: () => import('@/views/Profile.vue'),
        meta: { requiresAuth: true }
    },
    {
        path: '/notifications',
        name: 'Notifications',
        component: () => import('@/views/NotificationCenter.vue'),
        meta: { requiresAuth: true }
    },
    {
        path: '/dashboard',
        name: 'Dashboard',
        component: () => import('@/views/Dashboard.vue'),
        meta: { requiresAuth: true }
    },
    {
        path: '/admin/users',
        name: 'UserManagement',
        component: () => import('@/views/admin/UserManagement.vue'),
        meta: { requiresAuth: true, requiresAdmin: true }
    },
    {
        path: '/admin/products',
        name: 'ProductAudit',
        component: () => import('@/views/admin/ProductAudit.vue'),
        meta: { requiresAuth: true, requiresAdmin: true }
    }
]

const router = createRouter({
    history: createWebHistory(process.env.BASE_URL),
    routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
    const userStore = useUserStore()

    if (to.meta.requiresAuth && !userStore.isLoggedIn) {
        next('/login')
    } else if (to.path === '/login' || to.path === '/register') {
        if (userStore.isLoggedIn) {
            next('/home')
        } else {
            next()
        }
    } else if (to.meta.requiresAdmin && userStore.userInfo?.role !== 'ADMIN') {
        // 需要管理员权限但用户不是管理员
        alert('无管理员权限，无法访问')
        next('/home')
    } else {
        next()
    }
})

export default router

