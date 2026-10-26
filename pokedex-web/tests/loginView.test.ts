import { beforeEach, describe, expect, it, vi } from 'vitest'
import { flushPromises, mount } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import { createMemoryHistory, createRouter } from 'vue-router'
import { AxiosError, type AxiosResponse } from 'axios'
import { authApi } from '@/api/auth'
import LoginView from '@/views/LoginView.vue'

vi.mock('@/api/auth', () => ({
  authApi: { login: vi.fn(), register: vi.fn() },
}))

function setup() {
  const pinia = createPinia()
  setActivePinia(pinia)
  const router = createRouter({
    history: createMemoryHistory(),
    routes: [
      { path: '/login', name: 'login', component: LoginView },
      { path: '/register', name: 'register', component: { template: '<div />' } },
      { path: '/pokemon', name: 'browse', component: { template: '<div />' } },
    ],
  })
  return { pinia, router }
}

async function mountLogin() {
  const { pinia, router } = setup()
  await router.push('/login')
  await router.isReady()
  const wrapper = mount(LoginView, { global: { plugins: [pinia, router] } })
  return { wrapper, router }
}

describe('LoginView', () => {
  beforeEach(() => {
    localStorage.clear()
    vi.mocked(authApi.login).mockReset()
  })

  it('validates required fields before calling the API', async () => {
    const { wrapper } = await mountLogin()
    await wrapper.find('form').trigger('submit')

    expect(wrapper.text()).toContain('Username is required.')
    expect(wrapper.text()).toContain('Password is required.')
    expect(authApi.login).not.toHaveBeenCalled()
  })

  it('shows the server message on invalid credentials', async () => {
    const response = {
      status: 401,
      data: { detail: 'invalid username or password' },
      statusText: '',
      headers: {},
      config: {},
    } as AxiosResponse
    vi.mocked(authApi.login).mockRejectedValue(new AxiosError('x', 'ERR_BAD_REQUEST', undefined, undefined, response))

    const { wrapper } = await mountLogin()
    await wrapper.find('#username').setValue('demo')
    await wrapper.find('#password').setValue('wrong')
    await wrapper.find('form').trigger('submit')
    await flushPromises()

    expect(wrapper.find('[role="alert"]').text()).toContain('invalid username or password')
  })

  it('signs in and navigates to the browse page', async () => {
    vi.mocked(authApi.login).mockResolvedValue({
      token: 'jwt',
      tokenType: 'Bearer',
      expiresInSeconds: 3600,
      user: { id: 1, username: 'demo', email: 'demo@example.com', role: 'USER' },
    })

    const { wrapper, router } = await mountLogin()
    await wrapper.find('#username').setValue('demo')
    await wrapper.find('#password').setValue('demo1234')
    await wrapper.find('form').trigger('submit')
    await flushPromises()

    expect(authApi.login).toHaveBeenCalledWith('demo', 'demo1234')
    expect(router.currentRoute.value.name).toBe('browse')
  })
})
