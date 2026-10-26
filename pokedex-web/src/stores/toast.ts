import { ref } from 'vue'
import { defineStore } from 'pinia'

export type ToastType = 'success' | 'error' | 'info'

export interface Toast {
  id: number
  type: ToastType
  message: string
}

export const useToastStore = defineStore('toast', () => {
  const toasts = ref<Toast[]>([])
  let nextId = 1

  function remove(id: number): void {
    toasts.value = toasts.value.filter((t) => t.id !== id)
  }

  function push(type: ToastType, message: string, durationMs = 4500): void {
    const id = nextId++
    toasts.value.push({ id, type, message })
    if (durationMs > 0) {
      setTimeout(() => remove(id), durationMs)
    }
  }

  return {
    toasts,
    remove,
    success: (message: string) => push('success', message),
    error: (message: string) => push('error', message, 7000),
    info: (message: string) => push('info', message),
  }
})
