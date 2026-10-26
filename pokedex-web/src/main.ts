import { createApp } from 'vue'
import { createPinia } from 'pinia'
import { VueQueryPlugin, QueryClient } from '@tanstack/vue-query'
import App from './App.vue'
import { router } from './router'
import { useAuthStore } from './stores/auth'
import { setUnauthorizedHandler } from './api/http'
import { shouldRetry } from './api/errors'
import './style.css'

const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      staleTime: 60_000,
      refetchOnWindowFocus: false,
      retry: shouldRetry,
    },
  },
})

const app = createApp(App)
const pinia = createPinia()

// Pinia must be installed before the router: the first navigation runs the auth guard.
app.use(pinia)
app.use(router)
app.use(VueQueryPlugin, { queryClient })

setUnauthorizedHandler(() => {
  useAuthStore(pinia).logout()
  queryClient.removeQueries({ queryKey: ['local'] })
  void router.push({
    name: 'login',
    query: { redirect: router.currentRoute.value.fullPath, expired: '1' },
  })
})

app.mount('#app')
