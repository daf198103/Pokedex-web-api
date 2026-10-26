import { http } from './http'
import type { LocalPokemon, PageResponse, UpdateLocalRequest } from '@/types/api'

export const localApi = {
  list: (page: number, size: number) =>
    http
      .get<PageResponse<LocalPokemon>>('/api/local/pokemon', { params: { page, size } })
      .then((r) => r.data),

  get: (id: number) => http.get<LocalPokemon>(`/api/local/pokemon/${id}`).then((r) => r.data),

  update: (id: number, payload: UpdateLocalRequest) =>
    http.put<LocalPokemon>(`/api/local/pokemon/${id}`, payload).then((r) => r.data),

  remove: (id: number) => http.delete<void>(`/api/local/pokemon/${id}`).then(() => undefined),
}
