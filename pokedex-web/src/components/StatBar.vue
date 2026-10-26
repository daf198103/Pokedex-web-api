<script setup lang="ts">
import { computed } from 'vue'
import { formatName } from '@/utils/format'

const props = withDefaults(defineProps<{ name: string; value: number; max?: number }>(), { max: 255 })

const label = computed(() => (props.name === 'hp' ? 'HP' : formatName(props.name)))
const percent = computed(() => Math.min(100, Math.round((props.value / props.max) * 100)))
</script>

<template>
  <div class="stat-row">
    <span class="stat-label">{{ label }}</span>
    <span class="stat-value">{{ value }}</span>
    <div
      class="stat-track"
      role="progressbar"
      :aria-label="label"
      :aria-valuenow="value"
      aria-valuemin="0"
      :aria-valuemax="max"
    >
      <div class="stat-fill" :style="{ width: `${percent}%` }" />
    </div>
  </div>
</template>
