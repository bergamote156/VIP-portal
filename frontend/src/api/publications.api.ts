import { backendClient } from './client'
import type { Publication, PublicationInput } from '@/types/publication.types'

export const publicationsApi = {
  getPublic: () =>
    backendClient.get<Publication[]>('/internal/publications/public').then((r) => r.data),

  getAll: () =>
    backendClient.get<Publication[]>('/internal/publications').then((r) => r.data),

  getTypes: () =>
    backendClient.get<string[]>('/internal/publications/types').then((r) => r.data),

  getById: (id: number) =>
    backendClient.get<Publication>(`/internal/publications/${id}`).then((r) => r.data),

  create: (payload: PublicationInput) =>
    backendClient.post<void>('/internal/publications', payload).then((r) => r.data),

  update: (id: number, payload: PublicationInput) =>
    backendClient.put<void>(`/internal/publications/${id}`, payload).then((r) => r.data),

  remove: (id: number) =>
    backendClient.delete<void>(`/internal/publications/${id}`).then((r) => r.data),

  importBibtex: (bibtex: string) =>
    backendClient.post<Publication[]>('/internal/publications/import/bibtex', bibtex).then((r) => r.data),
}
