export interface PageResponse<T> {
  items: T[]
  page: number
  size: number
  totalElements: number
  totalPages: number
}

export interface PokemonSummary {
  id: number
  name: string
  sprite: string | null
  category: string | null
  /** hectograms (PokeAPI unit) */
  weight: number
  abilities: string[]
}

export interface Stat {
  name: string
  value: number
}

export interface EvolutionNode {
  id: number
  name: string
  evolvesTo: EvolutionNode[]
}

export interface PokemonDetail {
  id: number
  name: string
  image: string | null
  category: string | null
  /** hectograms */
  weight: number
  /** decimetres */
  height: number
  abilities: string[]
  stats: Stat[]
  description: string | null
  evolutionChain: EvolutionNode | null
}

export interface LocalPokemon {
  id: number
  name: string
  sprite: string | null
  category: string | null
  weight: number
  height: number
  abilities: string[]
  localizedName: string | null
  region: string | null
  tags: string[]
  version: number
  createdAt: string
  updatedAt: string
}

export interface UpdateLocalRequest {
  localizedName: string | null
  region: string | null
  tags: string[]
  version: number | null
}

export interface UserInfo {
  id: number
  username: string
  email: string
  role: string
}

export interface AuthResponse {
  token: string
  tokenType: string
  expiresInSeconds: number
  user: UserInfo
}

export interface RegisterRequest {
  username: string
  email: string
  password: string
}

/** RFC 7807 body returned by the backend */
export interface ProblemDetail {
  status: number
  title?: string
  detail?: string
  errors?: Record<string, string>
}
