import { backendClient } from './client'
import type { PrecisePage } from '@/types/application.types'


export interface BackendApplication {
  name: string
  fullName: string | null
  citation: string | null
  owner: string | null
  groups: { name: string; publicGroup: boolean; type: string; auto: boolean }[]
  note: string | null
}

export interface ApplicationCreatePayload {
  name: string
  fullName?: string | null
  citation?: string | null
  groups?: { name: string; publicGroup: boolean; type: string; auto: boolean }[]
  note?: string | null
}

export const applicationsApi = {
  getAll: (offset = 0, quantity = 50, group?: string) => {
    const params: Record<string, string | number> = { offset, quantity }
    if (group) params.group = group
    return backendClient
      .get<PrecisePage<BackendApplication>>('/internal/applications', { params })
      .then((r) => r.data)
  },

  getPublic: () =>
    backendClient
      .get<BackendApplication[]>('/internal/applications/public')
      .then((r) => r.data),

  getById: (id: string) =>
    backendClient
      .get<BackendApplication>(`/internal/applications/${encodeURIComponent(id)}`)
      .then((r) => r.data),

  createOrUpdate: (payload: ApplicationCreatePayload) =>
    backendClient
      .post<BackendApplication>('/internal/applications', {
        name: payload.name,
        fullName: payload.fullName ?? null,
        citation: payload.citation ?? null,
        note: payload.note ?? null,
        groups: payload.groups ?? [],
      })
      .then((r) => r.data),
}
