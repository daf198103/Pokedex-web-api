import axios, { type AxiosError } from 'axios'
import { tokenStorage } from './tokenStorage'

let onUnauthorized: (() => void) | null = null

/** Registered once at startup so this module does not depend on the store/router (no import cycle). */
export function setUnauthorizedHandler(handler: () => void): void {
  onUnauthorized = handler
}

export const http = axios.create({
  baseURL: import.meta.env.VITE_API_URL ?? '',
  timeout: 15000,
  headers: { 'Content-Type': 'application/json' },
})

http.interceptors.request.use((config) => {
  const token = tokenStorage.getToken()
  if (token) {
    config.headers.set('Authorization', `Bearer ${token}`)
  }
  return config
})

http.interceptors.response.use(
  (response) => response,
  (error: AxiosError) => {
    // Only treat 401 as "session expired" if we actually sent a token (a failed login is also a 401).
    if (error.response?.status === 401 && error.config?.headers?.has('Authorization')) {
      onUnauthorized?.()
    }
    return Promise.reject(error)
  },
)
