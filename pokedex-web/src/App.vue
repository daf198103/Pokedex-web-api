<script setup lang="ts">
import AppHeader from '@/components/AppHeader.vue'
import ToastHost from '@/components/ToastHost.vue'
</script>

<template>
  <a class="skip-link" href="#main">Skip to content</a>
  <AppHeader />
  <main id="main" class="container" tabindex="-1">
    <RouterView />
  </main>
  <ToastHost />
</template>
