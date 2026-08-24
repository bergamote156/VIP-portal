import { backendClient } from './client'
import type { RegisterPayload } from '@/types/auth.types'
import type { Group } from '@/types/group.types'
import type { ProfileUpdatePayload, ProfileUser } from '@/types/profile.types'
import type { UserSuggestion } from '@/types/user.types'

interface SignUpUserPayload {
  id: null
  firstName: string
  lastName: string
  email: string
  countryCode: string
  institution: string
  groups: Group[]
}

interface SignUpFormPayload {
  user: SignUpUserPayload
  password: string
  comment: string
}

interface PasswordUpdatePayload {
  user: { email: string }
  password: string
}

function toSignUpFormPayload(payload: RegisterPayload): SignUpFormPayload {
  return {
    user: {
      id: null,
      firstName: payload.firstName,
      lastName: payload.lastName,
      email: payload.email,
      countryCode: payload.countryCode.toLowerCase(),
      institution: payload.institution,
      // Backend signup expects a non-null groups collection.
      groups: [],
    },
    password: payload.password,
    comment: payload.comments,
  }
}

export const usersApi = {
  register: (payload: RegisterPayload) =>
    backendClient.post('/internal/users', toSignUpFormPayload(payload)).then((r) => r.data),

  me: () =>
    backendClient.get<ProfileUser>('/internal/users/me').then((r) => r.data),

  getById: (id: string) =>
    backendClient.get<ProfileUser>(`/internal/users/${encodeURIComponent(id)}`).then((r) => r.data),

  update: (id: string, payload: ProfileUpdatePayload) =>
    backendClient.put<ProfileUser>(`/internal/users/${encodeURIComponent(id)}`, payload).then((r) => r.data),

  remove: (id: string) =>
    backendClient.delete<void>(`/internal/users/${encodeURIComponent(id)}`).then((r) => r.data),

  activate: (email: string, code: string) =>
    backendClient.put(`/internal/users/${encodeURIComponent(email)}/activate`, { code }).then((r) => r.data),

  updatePassword: (email: string, password: string) => {
    const payload: PasswordUpdatePayload = {
      user: { email },
      password,
    }
    return backendClient
      .put(`/internal/users/${encodeURIComponent(email)}/password`, payload)
      .then((r) => r.data)
  },

  search: (query: string, limit = 10) =>
    backendClient
      .get<UserSuggestion[]>('/internal/users', {
        params: { q: query, limit },
      })
      .then((r) => r.data),

  dismissWelcome: (id: string) =>
    backendClient.put(`/internal/users/${encodeURIComponent(id)}/welcome/dismiss`).then((r) => r.data),

  resetAllWelcome: () =>
    backendClient.put('/internal/users/welcome/reset-all').then((r) => r.data),
}
