<script setup lang="ts">
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useToastStore } from '@/stores/toast'

const auth = useAuthStore()
const toast = useToastStore()
const router = useRouter()

async function logout() {
  auth.logout()
  toast.info('You have been signed out.')
  await router.push({ name: 'browse' })
}
</script>

<template>
  <header class="app-header">
    <div class="container header-inner">
      <RouterLink class="brand" :to="{ name: 'browse' }">
        <span aria-hidden="true">⚡</span> Pokédex
      </RouterLink>

      <nav class="main-nav" aria-label="Main">
        <RouterLink :to="{ name: 'browse' }">Browse</RouterLink>
        <RouterLink v-if="auth.isAuthenticated" :to="{ name: 'local-list' }">
          My collection
        </RouterLink>
      </nav>

      <div class="auth-actions">
        <template v-if="auth.isAuthenticated">
          <span class="muted user-name">{{ auth.username }}</span>
          <button type="button" class="btn btn-secondary btn-sm" @click="logout">Sign out</button>
        </template>
        <template v-else>
          <RouterLink class="btn btn-secondary btn-sm" :to="{ name: 'login' }">Sign in</RouterLink>
          <RouterLink class="btn btn-primary btn-sm" :to="{ name: 'register' }">Register</RouterLink>
        </template>
      </div>
    </div>
  </header>
</template>
