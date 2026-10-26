<script setup lang="ts">
import { computed } from 'vue'

const props = defineProps<{ page: number; totalPages: number; disabled?: boolean }>()
const emit = defineEmits<{ change: [page: number] }>()

const pages = computed(() => {
  const windowSize = 5
  let start = Math.max(1, props.page - 2)
  const end = Math.min(props.totalPages, start + windowSize - 1)
  start = Math.max(1, end - windowSize + 1)
  const list: number[] = []
  for (let p = start; p <= end; p++) list.push(p)
  return list
})

function go(page: number) {
  if (page < 1 || page > props.totalPages || page === props.page) return
  emit('change', page)
}
</script>

<template>
  <nav v-if="totalPages > 1" class="pagination" aria-label="Pagination">
    <button type="button" class="btn btn-secondary btn-sm" :disabled="disabled || page <= 1" @click="go(1)">
      « First
    </button>
    <button
      type="button"
      class="btn btn-secondary btn-sm"
      :disabled="disabled || page <= 1"
      @click="go(page - 1)"
    >
      ‹ Prev
    </button>

    <button
      v-for="p in pages"
      :key="p"
      type="button"
      class="btn btn-sm"
      :class="p === page ? 'btn-primary' : 'btn-secondary'"
      :aria-current="p === page ? 'page' : undefined"
      :aria-label="`Page ${p}`"
      :disabled="disabled"
      @click="go(p)"
    >
      {{ p }}
    </button>

    <button
      type="button"
      class="btn btn-secondary btn-sm"
      :disabled="disabled || page >= totalPages"
      @click="go(page + 1)"
    >
      Next ›
    </button>
    <button
      type="button"
      class="btn btn-secondary btn-sm"
      :disabled="disabled || page >= totalPages"
      @click="go(totalPages)"
    >
      Last »
    </button>
    <span class="muted small page-info">Page {{ page }} of {{ totalPages }}</span>
  </nav>
</template>
