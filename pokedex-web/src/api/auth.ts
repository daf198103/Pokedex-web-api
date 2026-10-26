import { http } from './http'
import type { AuthResponse, RegisterRequest, UserInfo } from '@/types/api'

export const authApi = {
  login: (username: string, password: string) =>
    http.post<AuthResponse>('/api/auth/login', { username, password }).then((r) => r.data),

  register: (payload: RegisterRequest) =>
    http.post<UserInfo>('/api/auth/register', payload).then((r) => r.data),
}
