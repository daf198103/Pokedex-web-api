<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { toApiError } from '@/api/errors'
import FormField from '@/components/FormField.vue'

const auth = useAuthStore()
const route = useRoute()
const router = useRouter()

const form = reactive({ username: '', password: '' })
const errors = reactive<{ username?: string; password?: string }>({})
const formError = ref('')
const loading = ref(false)
const expired = computed(() => route.query.expired === '1')

function safeRedirect(): string | null {
  const r = route.query.redirect
  return typeof r === 'string' && r.startsWith('/') && !r.startsWith('//') ? r : null
}

async function submit() {
  formError.value = ''
  errors.username = form.username.trim() ? undefined : 'Username is required.'
  errors.password = form.password ? undefined : 'Password is required.'
  if (errors.username || errors.password) return

  loading.value = true
  try {
    await auth.login(form.username.trim(), form.password)
    await router.push(safeRedirect() ?? { name: 'browse' })
  } catch (e) {
    formError.value = toApiError(e).message
  } finally {
    loading.value = false
  }
}

function fillDemo() {
  form.username = 'demo'
  form.password = 'demo1234'
}
</script>

<template>
  <section class="card form-card">
    <h1>Sign in</h1>

    <div v-if="expired" class="banner banner-warn" role="status">
      Your session expired. Please sign in again.
    </div>
    <div v-if="formError" class="banner banner-error" role="alert">{{ formError }}</div>

    <form novalidate @submit.prevent="submit">
      <FormField id="username" label="Username" :error="errors.username">
        <input
          id="username"
          v-model="form.username"
          type="text"
          autocomplete="username"
          :aria-invalid="!!errors.username"
          :aria-describedby="errors.username ? 'username-error' : undefined"
        />
      </FormField>

      <FormField id="password" label="Password" :error="errors.password">
        <input
          id="password"
          v-model="form.password"
          type="password"
          autocomplete="current-password"
          :aria-invalid="!!errors.password"
          :aria-describedby="errors.password ? 'password-error' : undefined"
        />
      </FormField>

      <div class="form-actions">
        <button type="submit" class="btn btn-primary" :disabled="loading">
          {{ loading ? 'Signing in…' : 'Sign in' }}
        </button>
        <button type="button" class="btn btn-secondary" @click="fillDemo">Use demo account</button>
      </div>
    </form>

    <p class="muted small" style="margin-top: 1rem">
      New here? <RouterLink :to="{ name: 'register' }">Create an account</RouterLink>
    </p>
  </section>
</template>
