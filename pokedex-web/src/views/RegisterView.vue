<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useToastStore } from '@/stores/toast'
import { toApiError } from '@/api/errors'
import FormField from '@/components/FormField.vue'

const auth = useAuthStore()
const toast = useToastStore()
const router = useRouter()

type Field = 'username' | 'email' | 'password' | 'confirm'

const form = reactive({ username: '', email: '', password: '', confirm: '' })
const errors = reactive<Partial<Record<Field, string>>>({})
const formError = ref('')
const loading = ref(false)

function validate(): boolean {
  errors.username = /^[A-Za-z0-9_.-]{3,50}$/.test(form.username.trim())
    ? undefined
    : '3-50 characters: letters, digits, dot, underscore or dash.'
  errors.email = /^[^@\s]+@[^@\s]+\.[^@\s]+$/.test(form.email.trim()) ? undefined : 'Enter a valid email address.'
  errors.password = form.password.length >= 8 && form.password.length <= 72 ? undefined : 'Use 8 to 72 characters.'
  errors.confirm = form.confirm === form.password ? undefined : 'Passwords do not match.'
  return !errors.username && !errors.email && !errors.password && !errors.confirm
}

async function submit() {
  formError.value = ''
  if (!validate()) return

  loading.value = true
  try {
    await auth.register({
      username: form.username.trim(),
      email: form.email.trim(),
      password: form.password,
    })
    toast.success('Account created. Welcome!')
    await router.push({ name: 'browse' })
  } catch (e) {
    const apiError = toApiError(e)
    for (const [field, message] of Object.entries(apiError.fieldErrors)) {
      if (field === 'username' || field === 'email' || field === 'password') errors[field] = message
    }
    if (!Object.keys(apiError.fieldErrors).length) formError.value = apiError.message
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <section class="card form-card">
    <h1>Create account</h1>
    <div v-if="formError" class="banner banner-error" role="alert">{{ formError }}</div>

    <form novalidate @submit.prevent="submit">
      <FormField id="reg-username" label="Username" :error="errors.username">
        <input
id="reg-username" v-model="form.username" type="text" autocomplete="username"
               :aria-invalid="!!errors.username" :aria-describedby="errors.username ? 'reg-username-error' : undefined" />
      </FormField>
      <FormField id="reg-email" label="Email" :error="errors.email">
        <input
id="reg-email" v-model="form.email" type="email" autocomplete="email"
               :aria-invalid="!!errors.email" :aria-describedby="errors.email ? 'reg-email-error' : undefined" />
      </FormField>
      <FormField id="reg-password" label="Password" :error="errors.password">
        <input
id="reg-password" v-model="form.password" type="password" autocomplete="new-password"
               :aria-invalid="!!errors.password" :aria-describedby="errors.password ? 'reg-password-error' : undefined" />
      </FormField>
      <FormField id="reg-confirm" label="Confirm password" :error="errors.confirm">
        <input
id="reg-confirm" v-model="form.confirm" type="password" autocomplete="new-password"
               :aria-invalid="!!errors.confirm" :aria-describedby="errors.confirm ? 'reg-confirm-error' : undefined" />
      </FormField>

      <div class="form-actions">
        <button type="submit" class="btn btn-primary" :disabled="loading">
          {{ loading ? 'Creating…' : 'Create account' }}
        </button>
      </div>
    </form>

    <p class="muted small" style="margin-top: 1rem">
      Already registered? <RouterLink :to="{ name: 'login' }">Sign in</RouterLink>
    </p>
  </section>
</template>
