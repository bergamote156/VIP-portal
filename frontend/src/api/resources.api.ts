import { backendClient } from './client'
import type { PrecisePage } from '@/types/application.types'
import type { Resource } from '@/types/resource.types'

export type BackendResource = Resource

export const resourcesApi = {
  getAll: (offset = 0, quantity = 50, group?: string) => {
    const params: Record<string, number | string> = { offset, quantity }
    if (group) {
      params.group = group
    }

    return backendClient
      .get<PrecisePage<BackendResource>>('/internal/resources', { params })
      .then((r) => r.data)
  },
}
