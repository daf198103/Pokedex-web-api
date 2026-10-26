import { http } from './http'
import type { LocalPokemon, PageResponse, PokemonDetail, PokemonSummary } from '@/types/api'

export const pokemonApi = {
  /** page is 0-based, as expected by the backend */
  list: (page: number, size: number) =>
    http
      .get<PageResponse<PokemonSummary>>('/api/pokemon', { params: { page, size } })
      .then((r) => r.data),

  detail: (idOrName: string) =>
    http.get<PokemonDetail>(`/api/pokemon/${encodeURIComponent(idOrName)}`).then((r) => r.data),

  sync: (idOrName: string | number) =>
    http
      .post<LocalPokemon>(`/api/pokemon/${encodeURIComponent(String(idOrName))}/sync`)
      .then((r) => r.data),
}
