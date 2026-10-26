<script setup lang="ts">
defineProps<{ title: string; message?: string }>()
</script>

<template>
  <div class="state">
    <h2>{{ title }}</h2>
    <p v-if="message">{{ message }}</p>
    <div class="state-actions"><slot /></div>
  </div>
</template>
