import { backendClient } from './client'
import type { PrecisePage } from '@/types/application.types'
import type { Tag } from '@/types/tag.types'

export type BackendTag = Tag

export const tagsApi = {
  getAll: (offset = 0, quantity = 50) => {
    const params: Record<string, number> = { offset, quantity }
    return backendClient
      .get<PrecisePage<BackendTag>>('/internal/tags', { params })
      .then((r) => r.data)
  },
}
