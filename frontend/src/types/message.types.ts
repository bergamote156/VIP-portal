export interface MessageItem {
  id: string
  subject: string
  body: string
  from: string
  to: string[]
  date: string
  read: boolean
  groupName?: string
  isGroupMessage: boolean
}

export interface SendMessagePayload {
  to: string
  subject: string
  body: string
  isGroupMessage: boolean
}
