import { backendClient } from './client'

export const apikeyApi = {
  get(): Promise<string> {
    return backendClient.get<string>('/internal/users/me/apikey').then((r) => r.data)
  },

  generateNew(): Promise<string> {
    return backendClient.post<string>('/internal/users/me/apikey').then((r) => r.data)
  },

  delete(): Promise<void> {
    return backendClient.delete<void>('/internal/users/me/apikey').then((r) => r.data)
  },
}
