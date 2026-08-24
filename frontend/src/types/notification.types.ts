export type NotificationType = 'success' | 'error' | 'warning' | 'info'

export interface Notification {
  id: string
  type: NotificationType
  title: string
  message: string
  duration?: number
}

export interface DashboardNotification {
  id: string
  type: 'message' | 'execution_running' | 'execution_completed' | 'execution_failed'
  title: string
  description: string
  date: string
  read: boolean
  link?: string
}
