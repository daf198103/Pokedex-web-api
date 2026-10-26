<script setup lang="ts">
import { computed, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { keepPreviousData, useMutation, useQuery, useQueryClient } from '@tanstack/vue-query'
import { localApi } from '@/api/local'
import { toApiError } from '@/api/errors'
import { useToastStore } from '@/stores/toast'
import { formatName, toKg } from '@/utils/format'
import type { LocalPokemon } from '@/types/api'
import PaginationBar from '@/components/PaginationBar.vue'
import ErrorState from '@/components/ErrorState.vue'
import EmptyState from '@/components/EmptyState.vue'
import ConfirmDialog from '@/components/ConfirmDialog.vue'

const PAGE_SIZE = 10

const route = useRoute()
const router = useRouter()
const toast = useToastStore()
const queryClient = useQueryClient()

const page = computed(() => {
  const n = parseInt(String(route.query.page), 10)
  return Number.isFinite(n) && n > 0 ? n : 1
})

const { data, isPending, isError, error, refetch, isPlaceholderData } = useQuery({
  queryKey: ['local', 'list', page],
  queryFn: () => localApi.list(page.value - 1, PAGE_SIZE),
  placeholderData: keepPreviousData,
  staleTime: 0,
})

const toDelete = ref<LocalPokemon | null>(null)

const deleteMutation = useMutation({
  mutationFn: (id: number) => localApi.remove(id),
  onSuccess: async () => {
    const name = toDelete.value ? formatName(toDelete.value.name) : 'Pokémon'
    toast.success(`${name} removed from your collection.`)
    // If we just emptied the current page, step back one page.
    if (data.value && data.value.items.length === 1 && page.value > 1) {
      await router.replace({ query: { page: String(page.value - 1) } })
    }
  },
  onError: (e) => {
    const apiError = toApiError(e)
    toast.error(
      apiError.status === 404 ? 'That Pokémon was already removed.' : apiError.message,
    )
  },
  onSettled: async () => {
    toDelete.value = null
    await queryClient.invalidateQueries({ queryKey: ['local'] })
  },
})

function goToPage(p: number) {
  void router.push({ query: { page: String(p) } })
}
</script>

<template>
  <section>
    <div class="page-head">
      <div>
        <h1>My collection</h1>
        <p v-if="data" class="muted">{{ data.totalElements }} saved Pokémon</p>
      </div>
      <RouterLink class="btn btn-primary" :to="{ name: 'browse' }">Find more Pokémon</RouterLink>
    </div>

    <div v-if="isPending" class="state" aria-busy="true">Loading your collection…</div>

    <ErrorState v-else-if="isError" :error="error" @retry="refetch()" />

    <EmptyState
      v-else-if="data && data.totalElements === 0"
      title="Your collection is empty"
      message="Open a Pokémon and use “Sync to my collection” to save it here."
    >
      <RouterLink class="btn btn-primary" :to="{ name: 'browse' }">Browse Pokémon</RouterLink>
    </EmptyState>

    <EmptyState v-else-if="data && data.items.length === 0" title="That page does not exist">
      <button type="button" class="btn btn-primary" @click="goToPage(1)">Go to first page</button>
    </EmptyState>

    <template v-else-if="data">
      <!-- desktop: table -->
      <div class="table-wrap desktop-only" :style="{ opacity: isPlaceholderData ? 0.6 : 1 }">
        <table class="data">
          <caption class="sr-only">Pokémon saved in the local database</caption>
          <thead>
            <tr>
              <th scope="col"><span class="sr-only">Sprite</span></th>
              <th scope="col">#</th>
              <th scope="col">Name</th>
              <th scope="col">Local name</th>
              <th scope="col">Region</th>
              <th scope="col">Tags</th>
              <th scope="col"><span class="sr-only">Actions</span></th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="p in data.items" :key="p.id">
              <td><img v-if="p.sprite" :src="p.sprite" :alt="`${formatName(p.name)} sprite`" loading="lazy" width="48" height="48" /></td>
              <td>{{ p.id }}</td>
              <td>
                <RouterLink :to="{ name: 'detail', params: { idOrName: String(p.id) } }">
                  {{ formatName(p.name) }}
                </RouterLink>
                <div class="muted small">{{ toKg(p.weight) }} kg</div>
              </td>
              <td>{{ p.localizedName ?? '—' }}</td>
              <td>{{ p.region ?? '—' }}</td>
              <td>
                <ul v-if="p.tags.length" class="chips" style="margin: 0">
                  <li v-for="t in p.tags" :key="t" class="chip">{{ t }}</li>
                </ul>
                <span v-else>—</span>
              </td>
              <td>
                <div class="row-actions">
                  <RouterLink class="btn btn-secondary btn-sm" :to="{ name: 'local-edit', params: { id: p.id } }">
                    Edit<span class="sr-only"> {{ formatName(p.name) }}</span>
                  </RouterLink>
                  <button type="button" class="btn btn-danger btn-sm" @click="toDelete = p">
                    Delete<span class="sr-only"> {{ formatName(p.name) }}</span>
                  </button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- mobile: cards -->
      <ul class="local-cards mobile-only" :style="{ opacity: isPlaceholderData ? 0.6 : 1 }">
        <li v-for="p in data.items" :key="p.id" class="card local-card">
          <img v-if="p.sprite" :src="p.sprite" :alt="`${formatName(p.name)} sprite`" loading="lazy" width="64" height="64" />
          <div class="local-card-main">
            <h2 class="card-title">#{{ p.id }} {{ formatName(p.name) }}</h2>
            <p class="small"><span class="muted">Local name:</span> {{ p.localizedName ?? '—' }}</p>
            <p class="small"><span class="muted">Region:</span> {{ p.region ?? '—' }}</p>
            <ul v-if="p.tags.length" class="chips">
              <li v-for="t in p.tags" :key="t" class="chip">{{ t }}</li>
            </ul>
            <div class="row-actions" style="margin-top: 0.6rem">
              <RouterLink class="btn btn-secondary btn-sm" :to="{ name: 'local-edit', params: { id: p.id } }">
                Edit
              </RouterLink>
              <button type="button" class="btn btn-danger btn-sm" @click="toDelete = p">Delete</button>
            </div>
          </div>
        </li>
      </ul>

      <PaginationBar :page="page" :total-pages="data.totalPages" :disabled="isPlaceholderData" @change="goToPage" />
    </template>

    <ConfirmDialog
      :open="!!toDelete"
      title="Remove from collection?"
      :message="`${toDelete ? formatName(toDelete.name) : ''} and your custom fields will be deleted from the local database. You can sync it again later.`"
      confirm-label="Delete"
      :busy="deleteMutation.isPending.value"
      @confirm="toDelete && deleteMutation.mutate(toDelete.id)"
      @cancel="toDelete = null"
    />
  </section>
</template>
