import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import { authApi } from '@/api/auth'
import { tokenStorage, type Session } from '@/api/tokenStorage'
import type { RegisterRequest } from '@/types/api'

export const useAuthStore = defineStore('auth', () => {
  const session = ref<Session | null>(tokenStorage.load())

  const isAuthenticated = computed(() => !!session.value && session.value.expiresAt > Date.now())
  const username = computed(() => session.value?.user.username ?? '')

  async function login(usernameInput: string, password: string): Promise<void> {
    const res = await authApi.login(usernameInput, password)
    session.value = {
      token: res.token,
      expiresAt: Date.now() + res.expiresInSeconds * 1000,
      user: res.user,
    }
    tokenStorage.save(session.value)
  }

  async function register(payload: RegisterRequest): Promise<void> {
    await authApi.register(payload)
    await login(payload.username, payload.password)
  }

  function logout(): void {
    session.value = null
    tokenStorage.clear()
  }

  return { session, isAuthenticated, username, login, register, logout }
})
