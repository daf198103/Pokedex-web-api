import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

declare module 'vue-router' {
  interface RouteMeta {
    title?: string
    requiresAuth?: boolean
    guestOnly?: boolean
  }
}

export const router = createRouter({
  history: createWebHistory(),
  scrollBehavior: () => ({ top: 0 }),
  routes: [
    { path: '/', redirect: { name: 'browse' } },
    {
      path: '/pokemon',
      name: 'browse',
      component: () => import('@/views/BrowseView.vue'),
      meta: { title: 'Browse Pokémon' },
    },
    {
      path: '/pokemon/:idOrName',
      name: 'detail',
      component: () => import('@/views/DetailView.vue'),
      meta: { title: 'Pokémon details' },
    },
    {
      path: '/local',
      name: 'local-list',
      component: () => import('@/views/LocalListView.vue'),
      meta: { title: 'My collection', requiresAuth: true },
    },
    {
      path: '/local/:id(\\d+)/edit',
      name: 'local-edit',
      component: () => import('@/views/LocalEditView.vue'),
      meta: { title: 'Edit Pokémon', requiresAuth: true },
    },
    {
      path: '/login',
      name: 'login',
      component: () => import('@/views/LoginView.vue'),
      meta: { title: 'Sign in', guestOnly: true },
    },
    {
      path: '/register',
      name: 'register',
      component: () => import('@/views/RegisterView.vue'),
      meta: { title: 'Create account', guestOnly: true },
    },
    {
      path: '/:pathMatch(.*)*',
      name: 'not-found',
      component: () => import('@/views/NotFoundView.vue'),
      meta: { title: 'Page not found' },
    },
  ],
})

router.beforeEach((to) => {
  const auth = useAuthStore()
  if (to.meta.requiresAuth && !auth.isAuthenticated) {
    return { name: 'login', query: { redirect: to.fullPath } }
  }
  if (to.meta.guestOnly && auth.isAuthenticated) {
    return { name: 'browse' }
  }
  return true
})

router.afterEach((to) => {
  document.title = to.meta.title ? `${to.meta.title} · Pokédex` : 'Pokédex'
})
