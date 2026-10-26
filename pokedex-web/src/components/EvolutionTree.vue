<script setup lang="ts">
import type { EvolutionNode } from '@/types/api'
import { formatName, spriteUrl } from '@/utils/format'

defineProps<{ node: EvolutionNode; currentId?: number }>()
</script>

<template>
  <div class="evo-branch">
    <RouterLink
      class="evo-node"
      :class="{ current: node.id === currentId }"
      :to="{ name: 'detail', params: { idOrName: String(node.id) } }"
      :aria-current="node.id === currentId ? 'page' : undefined"
    >
      <img :src="spriteUrl(node.id)" :alt="formatName(node.name)" width="72" height="72" loading="lazy" />
      <span>{{ formatName(node.name) }}</span>
    </RouterLink>

    <ul v-if="node.evolvesTo.length" class="evo-children">
      <li v-for="child in node.evolvesTo" :key="child.id" class="evo-child">
        <span class="evo-arrow" aria-label="evolves into">→</span>
        <EvolutionTree :node="child" :current-id="currentId" />
      </li>
    </ul>
  </div>
</template>
