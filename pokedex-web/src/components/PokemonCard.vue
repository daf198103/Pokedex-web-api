<script setup lang="ts">
import { computed } from 'vue'
import type { PokemonSummary } from '@/types/api'
import { formatName, toKg } from '@/utils/format'

const props = defineProps<{ pokemon: PokemonSummary }>()

const displayName = computed(() => formatName(props.pokemon.name))
const weight = computed(() => toKg(props.pokemon.weight))
</script>

<template>
  <RouterLink
    class="card pokemon-card"
    :to="{ name: 'detail', params: { idOrName: String(pokemon.id) } }"
  >
    <div class="sprite-wrap">
      <img
        v-if="pokemon.sprite"
        :src="pokemon.sprite"
        :alt="`${displayName} sprite`"
        width="96"
        height="96"
        loading="lazy"
      />
      <div v-else class="sprite-placeholder" aria-hidden="true">?</div>
    </div>
    <div class="card-body">
      <p class="muted small">#{{ pokemon.id }}</p>
      <h2 class="card-title">{{ displayName }}</h2>
      <p class="small">{{ pokemon.category ?? 'Unknown category' }}</p>
      <p class="small"><span class="muted">Weight:</span> {{ weight }} kg</p>
      <ul class="chips" aria-label="Abilities">
        <li v-for="ability in pokemon.abilities" :key="ability" class="chip">
          {{ formatName(ability) }}
        </li>
      </ul>
    </div>
  </RouterLink>
</template>
