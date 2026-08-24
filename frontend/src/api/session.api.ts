import { backendClient } from './client'

import type { LoginCredentials, VipSession } from '@/types/auth.types'

export const sessionApi = {
  login: (credentials: LoginCredentials) =>
    backendClient.post<VipSession>('/internal/session', credentials).then((r) => r.data),

  getSession: () =>
    backendClient.get<VipSession>('/internal/session').then((r) => r.data),

  logout: () =>
    backendClient.delete<void>('/internal/session').then((r) => r.data),

  getUser: () =>
    backendClient.get('/internal/users/me').then((r) => r.data),
}
