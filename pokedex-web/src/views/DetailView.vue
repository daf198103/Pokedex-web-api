<script setup lang="ts">
import { computed, ref } from 'vue'
import { useRoute } from 'vue-router'
import { useMutation, useQuery, useQueryClient } from '@tanstack/vue-query'
import { pokemonApi } from '@/api/pokemon'
import { toApiError } from '@/api/errors'
import { useAuthStore } from '@/stores/auth'
import { useToastStore } from '@/stores/toast'
import { formatName, toKg, toMetres } from '@/utils/format'
import StatBar from '@/components/StatBar.vue'
import EvolutionTree from '@/components/EvolutionTree.vue'
import ErrorState from '@/components/ErrorState.vue'

const route = useRoute()
const auth = useAuthStore()
const toast = useToastStore()
const queryClient = useQueryClient()

const idOrName = computed(() => String(route.params.idOrName))

const { data, isPending, isError, error, refetch } = useQuery({
  queryKey: ['pokemon', 'detail', idOrName],
  queryFn: () => pokemonApi.detail(idOrName.value),
})

const syncedId = ref<number | null>(null)

const syncMutation = useMutation({
  mutationFn: () => {
    if (!data.value) throw new Error('Pokémon not loaded yet')
    return pokemonApi.sync(data.value.id)
  },
  onSuccess: async (local) => {
    syncedId.value = local.id
    toast.success(`${formatName(local.name)} saved to your collection.`)
    await queryClient.invalidateQueries({ queryKey: ['local'] })
  },
  onError: (e) => toast.error(toApiError(e).message),
})
</script>

<template>
  <section>
    <p class="breadcrumb"><RouterLink :to="{ name: 'browse' }">← Back to all Pokémon</RouterLink></p>

    <div v-if="isPending" aria-busy="true" aria-label="Loading details">
      <div class="detail-grid">
        <div class="card detail-image"><div class="skeleton" style="height: 280px" /></div>
        <div>
          <div class="skeleton skeleton-line short" style="height: 2rem" />
          <div class="skeleton skeleton-line" />
          <div class="skeleton skeleton-line" />
          <div class="skeleton skeleton-line short" />
        </div>
      </div>
    </div>

    <ErrorState v-else-if="isError" :error="error" @retry="refetch()">
      <RouterLink class="btn btn-secondary" :to="{ name: 'browse' }">Browse all</RouterLink>
    </ErrorState>

    <article v-else-if="data" class="detail-grid">
      <div class="card detail-image">
        <img
          v-if="data.image"
          :src="data.image"
          :alt="`Artwork of ${formatName(data.name)}`"
          width="300"
          height="300"
        />
        <p v-else class="muted">No image available</p>
      </div>

      <div>
        <p class="muted small">#{{ data.id }}</p>
        <h1>{{ formatName(data.name) }}</h1>
        <p class="muted">{{ data.category ?? 'Unknown category' }}</p>

        <dl class="facts">
          <div class="fact"><dt>Height</dt><dd>{{ toMetres(data.height) }} m</dd></div>
          <div class="fact"><dt>Weight</dt><dd>{{ toKg(data.weight) }} kg</dd></div>
        </dl>

        <p v-if="data.description" class="description">{{ data.description }}</p>

        <div class="section">
          <h2>Abilities</h2>
          <ul class="chips">
            <li v-for="a in data.abilities" :key="a" class="chip">{{ formatName(a) }}</li>
          </ul>
        </div>

        <div class="section">
          <h2>Base stats</h2>
          <StatBar v-for="s in data.stats" :key="s.name" :name="s.name" :value="s.value" />
        </div>

        <div v-if="data.evolutionChain" class="section">
          <h2>Evolution</h2>
          <EvolutionTree :node="data.evolutionChain" :current-id="data.id" />
        </div>

        <div class="section">
          <h2>Local collection</h2>
          <template v-if="auth.isAuthenticated">
            <p class="muted small">
              Save a copy to the local database to add your own name, region and tags. Syncing again
              refreshes the Pokémon data without touching your custom fields.
            </p>
            <div class="form-actions">
              <button
                type="button"
                class="btn btn-primary"
                :disabled="syncMutation.isPending.value"
                @click="syncMutation.mutate()"
              >
                {{ syncMutation.isPending.value ? 'Syncing…' : 'Sync to my collection' }}
              </button>
              <RouterLink
                v-if="syncedId"
                class="btn btn-secondary"
                :to="{ name: 'local-edit', params: { id: syncedId } }"
              >
                Edit local copy
              </RouterLink>
            </div>
          </template>
          <p v-else class="muted">
            <RouterLink :to="{ name: 'login', query: { redirect: route.fullPath } }">Sign in</RouterLink>
            to save this Pokémon to your collection.
          </p>
        </div>
      </div>
    </article>
  </section>
</template>
