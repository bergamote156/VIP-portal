import { backendClient } from './client'
import type { PrecisePage } from '@/types/application.types'
import type { Group } from '@/types/group.types'

export type BackendGroup = Group

export const groupsApi = {
  getAll: (onlyApplications = false, onlyResources = false, offset = 0, quantity = 50) => {
    const params: Record<string, number | boolean> = { onlyApplications, onlyResources, offset, quantity }

    return backendClient
      .get<PrecisePage<BackendGroup>>('/internal/groups', { params })
      .then((r) => r.data)
  },

  search: (query: string, limit = 10) =>
    backendClient
      .get<BackendGroup[]>('/internal/groups', {
        params: { q: query, limit },
      })
      .then((r) => r.data),
}