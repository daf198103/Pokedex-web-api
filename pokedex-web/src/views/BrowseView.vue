<script setup lang="ts">
import { computed, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { keepPreviousData, useQuery } from '@tanstack/vue-query'
import { pokemonApi } from '@/api/pokemon'
import PokemonCard from '@/components/PokemonCard.vue'
import SkeletonCard from '@/components/SkeletonCard.vue'
import PaginationBar from '@/components/PaginationBar.vue'
import ErrorState from '@/components/ErrorState.vue'
import EmptyState from '@/components/EmptyState.vue'

const SIZES = [12, 24, 48]

const route = useRoute()
const router = useRouter()

// URL is the single source of truth for paging (shareable links, back button support).
const page = computed(() => {
  const n = parseInt(String(route.query.page), 10)
  return Number.isFinite(n) && n > 0 ? n : 1
})
const size = computed(() => {
  const n = parseInt(String(route.query.size), 10)
  return SIZES.includes(n) ? n : SIZES[0]
})

const { data, isPending, isError, error, refetch, isFetching, isPlaceholderData } = useQuery({
  queryKey: ['pokemon', 'list', page, size],
  queryFn: () => pokemonApi.list(page.value - 1, size.value),
  placeholderData: keepPreviousData,
})

const outOfRange = computed(
  () => !!data.value && data.value.items.length === 0 && data.value.totalElements > 0,
)

function goToPage(p: number) {
  void router.push({ query: { ...route.query, page: String(p) } })
}

function changeSize(event: Event) {
  const value = (event.target as HTMLSelectElement).value
  void router.push({ query: { ...route.query, size: value, page: '1' } })
}

const search = ref('')
function submitSearch() {
  const q = search.value.trim().toLowerCase()
  if (q) void router.push({ name: 'detail', params: { idOrName: q } })
}
</script>

<template>
  <section>
    <div class="page-head">
      <div>
        <h1>Browse Pokémon</h1>
        <p v-if="data" class="muted">{{ data.totalElements }} Pokémon available</p>
      </div>

      <div class="toolbar">
        <form class="search-form" role="search" @submit.prevent="submitSearch">
          <label for="search" class="sr-only">Find a Pokémon by name or number</label>
          <input id="search" v-model="search" type="search" placeholder="Name or number, e.g. pikachu" />
          <button type="submit" class="btn btn-primary">Go</button>
        </form>
        <div>
          <label for="size" class="small muted">Per page&nbsp;</label>
          <select id="size" :value="size" @change="changeSize">
            <option v-for="s in SIZES" :key="s" :value="s">{{ s }}</option>
          </select>
        </div>
      </div>
    </div>

    <ul v-if="isPending" class="grid" aria-busy="true" aria-label="Loading Pokémon">
      <li v-for="n in size" :key="n"><SkeletonCard /></li>
    </ul>

    <ErrorState v-else-if="isError" :error="error" @retry="refetch()" />

    <EmptyState
      v-else-if="outOfRange"
      title="That page does not exist"
      message="The page number is beyond the last page of results."
    >
      <button type="button" class="btn btn-primary" @click="goToPage(1)">Go to first page</button>
    </EmptyState>

    <EmptyState v-else-if="data && data.items.length === 0" title="No Pokémon found" />

    <template v-else-if="data">
      <ul class="grid" :aria-busy="isFetching" :style="{ opacity: isPlaceholderData ? 0.6 : 1 }">
        <li v-for="p in data.items" :key="p.id"><PokemonCard :pokemon="p" /></li>
      </ul>
      <PaginationBar
        :page="page"
        :total-pages="data.totalPages"
        :disabled="isPlaceholderData"
        @change="goToPage"
      />
    </template>
  </section>
</template>
