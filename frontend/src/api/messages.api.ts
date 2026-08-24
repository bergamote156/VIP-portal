import { backendClient } from './client'

interface BackendUser {
  id?: string
  email?: string
  firstName?: string
  lastName?: string
}

export interface BackendMessage {
  id: number
  sender?: BackendUser | null
  receivers?: BackendUser[]
  title?: string
  message?: string
  posted?: string
  postedDate?: string | number
  read?: boolean
}

export interface BackendGroupMessage {
  id: number
  sender?: BackendUser | null
  groupName?: string
  title?: string
  message?: string
  posted?: string
  postedDate?: string | number
}

/** Payload for POST /internal/messages — matches backend Message model */
interface SendMessagePayload {
  receivers: { id: string }[]
  title: string
  message: string
}

/** Payload for POST /internal/messages/groups — matches backend GroupMessage model */
interface SendGroupMessagePayload {
  groupName: string
  title: string
  message: string
}

export const messagesApi = {
  getReceived: (startDate?: number) =>
    backendClient
      .get<BackendMessage[]>('/internal/messages', {
        params: startDate ? { startDate } : undefined,
      })
      .then((r) => r.data),

  getSent: (startDate?: number) =>
    backendClient
      .get<BackendMessage[]>('/internal/messages/send', {
        params: startDate ? { startDate } : undefined,
      })
      .then((r) => r.data),

  getGroupMessages: (groupName: string, startDate?: number) =>
    backendClient
      .get<BackendGroupMessage[]>('/internal/messages/groups', {
        params: startDate ? { groupName, startDate } : { groupName },
      })
      .then((r) => r.data),

  send: (receivers: string[], title: string, message: string) =>
    backendClient.post<void>('/internal/messages', {
      receivers: receivers.map((id) => ({ id })),
      title,
      message,
    } satisfies SendMessagePayload).then((r) => r.data),

  sendGroup: (groupName: string, title: string, message: string) =>
    backendClient.post<void>('/internal/messages/groups', {
      groupName,
      title,
      message,
    } satisfies SendGroupMessagePayload).then((r) => r.data),

  deleteReceived: (id: number) =>
    backendClient.delete(`/internal/messages/${id}`).then((r) => r.data),

  deleteSent: (id: number) =>
    backendClient.delete(`/internal/messages/send/${id}`).then((r) => r.data),

  deleteGroupMessage: (id: number) =>
    backendClient.delete(`/internal/messages/groups/${id}`).then((r) => r.data),

  markAsRead: (id: number) =>
    backendClient.put(`/internal/messages/${id}/read`).then((r) => r.data),
}
