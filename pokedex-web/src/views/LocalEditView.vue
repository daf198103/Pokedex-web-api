<script setup lang="ts">
import { computed, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useMutation, useQuery, useQueryClient } from '@tanstack/vue-query'
import { localApi } from '@/api/local'
import { toApiError, type ApiError } from '@/api/errors'
import { useToastStore } from '@/stores/toast'
import { formatDate, formatName, toKg, toMetres } from '@/utils/format'
import type { LocalPokemon, UpdateLocalRequest } from '@/types/api'
import FormField from '@/components/FormField.vue'
import ErrorState from '@/components/ErrorState.vue'

// Client-side rules mirror the backend (ManageLocalPokemonUseCase + Bean Validation).
const MAX_TEXT = 100
const MAX_TAGS = 10
const MAX_TAG_LENGTH = 30

const route = useRoute()
const router = useRouter()
const toast = useToastStore()
const queryClient = useQueryClient()

const id = computed(() => Number(route.params.id))

const { data, isPending, isError, error, refetch } = useQuery({
  queryKey: ['local', 'detail', id],
  queryFn: () => localApi.get(id.value),
  staleTime: 0,
})

const form = reactive({ localizedName: '', region: '', tags: [] as string[] })
const tagDraft = ref('')
const errors = reactive<{ localizedName?: string; region?: string; tags?: string }>({})
const formError = ref('')
const conflict = ref(false)
const gone = ref(false)

function clearErrors() {
  errors.localizedName = undefined
  errors.region = undefined
  errors.tags = undefined
  formError.value = ''
}

function resetForm(p: LocalPokemon) {
  form.localizedName = p.localizedName ?? ''
  form.region = p.region ?? ''
  form.tags = [...p.tags]
  tagDraft.value = ''
  clearErrors()
}

watch(data, (p) => { if (p) resetForm(p) }, { immediate: true })

function addTags() {
  const parts = tagDraft.value.split(',').map((t) => t.trim()).filter(Boolean)
  for (const part of parts) {
    if (!form.tags.includes(part)) form.tags.push(part)
  }
  tagDraft.value = ''
}

function removeTag(tag: string) {
  form.tags = form.tags.filter((t) => t !== tag)
}

function onTagKeydown(event: KeyboardEvent) {
  if (event.key === 'Enter' || event.key === ',') {
    event.preventDefault()
    addTags()
  }
}

function validate(): boolean {
  clearErrors()
  if (form.localizedName.trim().length > MAX_TEXT) errors.localizedName = `At most ${MAX_TEXT} characters.`
  if (form.region.trim().length > MAX_TEXT) errors.region = `At most ${MAX_TEXT} characters.`
  if (form.tags.length > MAX_TAGS) errors.tags = `At most ${MAX_TAGS} tags.`
  else if (form.tags.some((t) => t.length > MAX_TAG_LENGTH)) errors.tags = `Each tag can have at most ${MAX_TAG_LENGTH} characters.`
  return !errors.localizedName && !errors.region && !errors.tags
}

function applyApiError(apiError: ApiError) {
  if (apiError.status === 409) {
    conflict.value = true
  } else if (apiError.status === 404) {
    gone.value = true
  } else if (apiError.status === 400) {
    for (const [field, message] of Object.entries(apiError.fieldErrors)) {
      if (field.startsWith('tags')) errors.tags = message
      else if (field === 'localizedName') errors.localizedName = message
      else if (field === 'region') errors.region = message
    }
    if (!Object.keys(apiError.fieldErrors).length) formError.value = apiError.message
  } else {
    toast.error(apiError.message)
  }
}

const mutation = useMutation({
  mutationFn: (payload: UpdateLocalRequest) => localApi.update(id.value, payload),
  onSuccess: async (updated) => {
    queryClient.setQueryData(['local', 'detail', id.value], updated)
    await queryClient.invalidateQueries({ queryKey: ['local', 'list'] })
    toast.success(`${formatName(updated.name)} updated.`)
    await router.push({ name: 'local-list' })
  },
  onError: (e) => applyApiError(toApiError(e)),
})

function submit() {
  addTags()
  if (!data.value || !validate()) return
  conflict.value = false
  mutation.mutate({
    localizedName: form.localizedName.trim() || null,
    region: form.region.trim() || null,
    tags: form.tags,
    version: data.value.version, // enables optimistic locking on the backend (409 on stale write)
  })
}

async function reloadLatest() {
  conflict.value = false
  const result = await refetch()
  if (result.data) resetForm(result.data)
}
</script>

<template>
  <section>
    <p class="breadcrumb"><RouterLink :to="{ name: 'local-list' }">← Back to my collection</RouterLink></p>

    <div v-if="isPending" class="state" aria-busy="true">Loading…</div>

    <ErrorState v-else-if="isError || gone" :error="gone ? new Error('This Pokémon no longer exists in your collection.') : error" @retry="refetch()">
      <RouterLink class="btn btn-secondary" :to="{ name: 'local-list' }">Back to collection</RouterLink>
    </ErrorState>

    <template v-else-if="data">
      <div class="page-head">
        <div style="display: flex; gap: 1rem; align-items: center">
          <img v-if="data.sprite" :src="data.sprite" :alt="`${formatName(data.name)} sprite`" width="72" height="72" />
          <div>
            <h1>Edit {{ formatName(data.name) }}</h1>
            <p class="muted small">
              #{{ data.id }} · {{ data.category ?? 'Unknown category' }} · {{ toMetres(data.height) }} m ·
              {{ toKg(data.weight) }} kg · updated {{ formatDate(data.updatedAt) }}
            </p>
          </div>
        </div>
      </div>

      <div v-if="conflict" class="banner banner-warn" role="alert">
        <strong>This Pokémon was modified elsewhere.</strong>
        Your changes were not saved because a newer version exists.
        <button type="button" class="btn-link" @click="reloadLatest">Reload the latest version</button>
      </div>
      <div v-if="formError" class="banner banner-error" role="alert">{{ formError }}</div>

      <form class="card form-card form-wide" novalidate @submit.prevent="submit">
        <p class="muted small">
          Name, category and abilities come from PokeAPI and are refreshed on sync. The fields below are yours.
        </p>

        <FormField id="localizedName" label="Localized name" :error="errors.localizedName">
          <input
            id="localizedName"
            v-model="form.localizedName"
            type="text"
            :aria-invalid="!!errors.localizedName"
            :aria-describedby="errors.localizedName ? 'localizedName-error' : undefined"
          />
        </FormField>

        <FormField id="region" label="Region" :error="errors.region">
          <input
            id="region"
            v-model="form.region"
            type="text"
            placeholder="e.g. Kanto"
            :aria-invalid="!!errors.region"
            :aria-describedby="errors.region ? 'region-error' : undefined"
          />
        </FormField>

        <FormField
          id="tag-draft"
          label="Tags"
          :error="errors.tags"
          :hint="`Press Enter or comma to add. Up to ${MAX_TAGS} tags, ${MAX_TAG_LENGTH} characters each.`"
        >
          <div class="tag-input">
            <input
              id="tag-draft"
              v-model="tagDraft"
              type="text"
              :aria-invalid="!!errors.tags"
              :aria-describedby="errors.tags ? 'tag-draft-error' : 'tag-draft-hint'"
              @keydown="onTagKeydown"
              @blur="addTags"
            />
            <button type="button" class="btn btn-secondary" @click="addTags">Add</button>
          </div>
          <ul v-if="form.tags.length" class="chips" aria-label="Current tags">
            <li v-for="tag in form.tags" :key="tag" class="chip chip-removable">
              {{ tag }}
              <button type="button" :aria-label="`Remove tag ${tag}`" @click="removeTag(tag)">×</button>
            </li>
          </ul>
        </FormField>

        <div class="form-actions">
          <button type="submit" class="btn btn-primary" :disabled="mutation.isPending.value">
            {{ mutation.isPending.value ? 'Saving…' : 'Save changes' }}
          </button>
          <RouterLink class="btn btn-secondary" :to="{ name: 'local-list' }">Cancel</RouterLink>
        </div>
      </form>
    </template>
  </section>
</template>
