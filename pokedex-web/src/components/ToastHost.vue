<script setup lang="ts">
import { useToastStore } from '@/stores/toast'

const toast = useToastStore()
</script>

<template>
  <div class="toast-host" aria-live="polite" aria-atomic="false">
    <div
      v-for="t in toast.toasts"
      :key="t.id"
      class="toast"
      :class="`toast-${t.type}`"
      :role="t.type === 'error' ? 'alert' : 'status'"
    >
      <span>{{ t.message }}</span>
      <button type="button" class="toast-close" aria-label="Dismiss notification" @click="toast.remove(t.id)">
        ×
      </button>
    </div>
  </div>
</template>
