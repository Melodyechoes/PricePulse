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
        userInfo.value = res.data.userInfo || res.data.user
        localStorage.setItem('token', token.value)
        if (userInfo.value) {
            localStorage.setItem('userInfo', JSON.stringify(userInfo.value))
        }
        return res
    }

    // 注册
    async function registerAction(registerForm) {
        const res = await register(registerForm)
        token.value = res.data.token
        userInfo.value = res.data.userInfo || res.data.user
        localStorage.setItem('token', token.value)
        if (userInfo.value) {
            localStorage.setItem('userInfo', JSON.stringify(userInfo.value))
        }
        return res
    }


    // 获取用户信息
    async function getUserInfoAction() {
        const res = await getCurrentUser()
        userInfo.value = res.data
        if (userInfo.value) {
            localStorage.setItem('userInfo', JSON.stringify(userInfo.value))
        }
        return res
    }
    // 初始化时从 localStorage 恢复用户信息
    function initUserInfo() {
        const savedUser = localStorage.getItem('userInfo')
        if (savedUser && token.value) {
            try {
                userInfo.value = JSON.parse(savedUser)
            } catch (e) {
                console.error('解析用户信息失败:', e)
            }
        }
    }

    // 退出登录
    function logout() {
        token.value = ''
        userInfo.value = null
        localStorage.removeItem('token')
        localStorage.removeItem('userInfo')
    }

    return {
        token,
        userInfo,
        isLoggedIn,
        loginAction,
        registerAction,
        getUserInfoAction,
        logout,
        initUserInfo
    }
})
