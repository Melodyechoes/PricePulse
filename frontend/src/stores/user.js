import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { login, register, getCurrentUser } from '@/api/auth'

export const useUserStore = defineStore('user', () => {
    const token = ref(localStorage.getItem('token') || '')
    const userInfo = ref(null)

    const isLoggedIn = computed(() => !!token.value)

    // 登录
    async function loginAction(loginForm) {
        const res = await login(loginForm)
        token.value = res.data.token
        userInfo.value = res.data.user
        localStorage.setItem('token', token.value)
        return res
    }

    // 注册
    async function registerAction(registerForm) {
        const res = await register(registerForm)
        token.value = res.data.token
        userInfo.value = res.data.user
        localStorage.setItem('token', token.value)
        return res
    }

    // 获取用户信息
    async function getUserInfoAction() {
        const res = await getCurrentUser()
        userInfo.value = res.data
        return res
    }

    // 退出登录
    function logout() {
        token.value = ''
        userInfo.value = null
        localStorage.removeItem('token')
    }

    return {
        token,
        userInfo,
        isLoggedIn,
        loginAction,
        registerAction,
        getUserInfoAction,
        logout
    }
})
