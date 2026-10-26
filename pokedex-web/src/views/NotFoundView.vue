<template>
  <div class="state">
    <h1>Page not found</h1>
    <p>The page you are looking for does not exist.</p>
    <div class="state-actions">
      <RouterLink class="btn btn-primary" :to="{ name: 'browse' }">Back to browse</RouterLink>
    </div>
  </div>
</template>
