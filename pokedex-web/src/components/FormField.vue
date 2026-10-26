<script setup lang="ts">
defineProps<{ id: string; label: string; error?: string; hint?: string }>()
</script>

<template>
  <div class="field" :class="{ 'has-error': !!error }">
    <label :for="id">{{ label }}</label>
    <slot />
    <p v-if="hint && !error" :id="`${id}-hint`" class="muted small">{{ hint }}</p>
    <p v-if="error" :id="`${id}-error`" class="field-error" role="alert">{{ error }}</p>
  </div>
</template>
