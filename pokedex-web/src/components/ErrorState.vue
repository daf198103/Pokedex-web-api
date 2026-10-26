<script setup lang="ts">
import { computed } from 'vue'
import { toApiError } from '@/api/errors'

const props = defineProps<{ error: unknown; retryable?: boolean }>()
const emit = defineEmits<{ retry: [] }>()

const apiError = computed(() => toApiError(props.error))
const title = computed(() => {
  switch (apiError.value.status) {
    case 404:
      return 'Not found'
    case 502:
      return 'Pokémon service unavailable'
    default:
      return 'Something went wrong'
  }
})
</script>

<template>
  <div class="state state-error" role="alert">
    <h2>{{ title }}</h2>
    <p>{{ apiError.message }}</p>
    <div class="state-actions">
      <button
        v-if="retryable !== false && apiError.status !== 404"
        type="button"
        class="btn btn-primary"
        @click="emit('retry')"
      >
        Try again
      </button>
      <slot />
    </div>
  </div>
</template>
