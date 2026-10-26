<script setup lang="ts">
import { onMounted, ref, watch } from 'vue'

const props = defineProps<{
  open: boolean
  title: string
  message: string
  confirmLabel?: string
  busy?: boolean
}>()
const emit = defineEmits<{ confirm: []; cancel: [] }>()

const dialog = ref<HTMLDialogElement | null>(null)

function sync(open: boolean) {
  const el = dialog.value
  if (!el) return
  if (open && !el.open) el.showModal?.()
  if (!open && el.open) el.close?.()
}

watch(() => props.open, sync, { flush: 'post' })
onMounted(() => sync(props.open))
</script>

<template>
  <dialog
    ref="dialog"
    class="dialog"
    aria-labelledby="confirm-title"
    @cancel.prevent="emit('cancel')"
    @click.self="emit('cancel')"
  >
    <div class="dialog-body">
      <h2 id="confirm-title">{{ title }}</h2>
      <p>{{ message }}</p>
      <div class="dialog-actions">
        <button type="button" class="btn btn-secondary" :disabled="busy" @click="emit('cancel')">
          Cancel
        </button>
        <button type="button" class="btn btn-danger" :disabled="busy" @click="emit('confirm')">
          {{ busy ? 'Working…' : (confirmLabel ?? 'Confirm') }}
        </button>
      </div>
    </div>
  </dialog>
</template>
