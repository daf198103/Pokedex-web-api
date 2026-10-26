import { beforeEach, describe, expect, it, vi } from 'vitest'
import { createPinia, setActivePinia } from 'pinia'
import { authApi } from '@/api/auth'
import { useAuthStore } from '@/stores/auth'

vi.mock('@/api/auth', () => ({
  authApi: { login: vi.fn(), register: vi.fn() },
}))

const user = { id: 1, username: 'demo', email: 'demo@example.com', role: 'USER' }

describe('auth store', () => {
  beforeEach(() => {
    localStorage.clear()
    setActivePinia(createPinia())
    vi.mocked(authApi.login).mockReset()
    vi.mocked(authApi.register).mockReset()
  })

  it('stores the session after login and clears it on logout', async () => {
    vi.mocked(authApi.login).mockResolvedValue({ token: 'jwt', tokenType: 'Bearer', expiresInSeconds: 3600, user })
    const auth = useAuthStore()

    await auth.login('demo', 'demo1234')

    expect(auth.isAuthenticated).toBe(true)
    expect(auth.username).toBe('demo')
    expect(localStorage.getItem('pokedex.session')).toContain('jwt')

    auth.logout()
    expect(auth.isAuthenticated).toBe(false)
    expect(localStorage.getItem('pokedex.session')).toBeNull()
  })

  it('registers then signs in', async () => {
    vi.mocked(authApi.register).mockResolvedValue(user)
    vi.mocked(authApi.login).mockResolvedValue({ token: 't', tokenType: 'Bearer', expiresInSeconds: 60, user })
    const auth = useAuthStore()

    await auth.register({ username: 'demo', email: 'demo@example.com', password: 'demo1234' })

    expect(authApi.register).toHaveBeenCalledOnce()
    expect(auth.isAuthenticated).toBe(true)
  })

  it('ignores an expired stored session', () => {
    localStorage.setItem('pokedex.session', JSON.stringify({ token: 'old', expiresAt: Date.now() - 1000, user }))
    setActivePinia(createPinia())
    expect(useAuthStore().isAuthenticated).toBe(false)
  })
})
