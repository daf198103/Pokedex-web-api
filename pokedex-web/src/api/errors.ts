import { isAxiosError } from 'axios'
import type { ProblemDetail } from '@/types/api'

export interface ApiError {
  /** HTTP status, or null for network/unknown errors */
  status: number | null
  message: string
  /** field name -> message (400 responses from Bean Validation) */
  fieldErrors: Record<string, string>
}

const FALLBACKS: Record<number, string> = {
  400: 'The request was not valid.',
  401: 'You are not signed in or your session has expired.',
  403: 'You do not have permission to do that.',
  404: 'The requested item was not found.',
  409: 'The item was modified by someone else.',
  502: 'The Pokémon data service is currently unavailable.',
}

export function toApiError(error: unknown): ApiError {
  if (isAxiosError(error)) {
    if (error.response) {
      const status = error.response.status
      const data = error.response.data as Partial<ProblemDetail> | undefined
      return {
        status,
        message: data?.detail || data?.title || FALLBACKS[status] || 'Unexpected server error.',
        fieldErrors: data?.errors ?? {},
      }
    }
    return {
      status: null,
      message: 'Cannot reach the server. Check your connection and try again.',
      fieldErrors: {},
    }
  }
  return {
    status: null,
    message: error instanceof Error ? error.message : 'Unexpected error.',
    fieldErrors: {},
  }
}

/** Do not retry client errors; retry transient/network/5xx errors once or twice. */
export function shouldRetry(failureCount: number, error: unknown): boolean {
  const { status } = toApiError(error)
  if (status !== null && status < 500) return false
  return failureCount < 2
}
