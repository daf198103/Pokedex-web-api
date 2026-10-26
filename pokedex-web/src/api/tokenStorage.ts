import type { UserInfo } from '@/types/api'

export interface Session {
  token: string
  expiresAt: number
  user: UserInfo
}

const KEY = 'pokedex.session'

/**
 * The JWT is kept in localStorage so a page refresh keeps the user signed in.
 * Trade-off: readable by any script on the page (XSS). Mitigated by Vue's output escaping,
 * a short token lifetime (60 min) and no third-party scripts. An HttpOnly cookie would be the
 * hardening step if the backend supported it.
 */
export const tokenStorage = {
  load(): Session | null {
    try {
      const raw = localStorage.getItem(KEY)
      if (!raw) return null
      const session = JSON.parse(raw) as Session
      if (!session.token || session.expiresAt <= Date.now()) {
        localStorage.removeItem(KEY)
        return null
      }
      return session
    } catch {
      return null
    }
  },
  save(session: Session): void {
    try {
      localStorage.setItem(KEY, JSON.stringify(session))
    } catch {
      /* storage unavailable: session lives in memory only */
    }
  },
  clear(): void {
    try {
      localStorage.removeItem(KEY)
    } catch {
      /* ignore */
    }
  },
  getToken(): string | null {
    return this.load()?.token ?? null
  },
}
