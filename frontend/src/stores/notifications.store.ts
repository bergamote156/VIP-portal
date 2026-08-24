import { ref } from 'vue'
import { defineStore } from 'pinia'
import { messagesApi, type BackendMessage } from '@/api/messages.api'
import { workflowsApi } from '@/api/workflows.api'
import type { Notification, DashboardNotification } from '@/types/notification.types'
import type { Workflow, WorkflowStatus } from '@/types/workflow.types'

function toDisplayName(user?: { email?: string; firstName?: string; lastName?: string } | null): string {
  if (!user) return 'Unknown'
  const fullName = [user.firstName, user.lastName].filter(Boolean).join(' ')
  return fullName || user.email || 'Unknown'
}

function mapMessageToNotification(msg: BackendMessage): DashboardNotification {
  return {
    id: `msg-${msg.id}`,
    type: 'message',
    title: msg.title || '(No subject)',
    description: `From: ${toDisplayName(msg.sender)} — ${(msg.message || '').slice(0, 120)}`,
    date: new Date(msg.postedDate ?? msg.posted ?? Date.now()).toISOString(),
    read: Boolean(msg.read),
    link: '/messages',
  }
}

function mapWorkflowToNotification(wf: Workflow): DashboardNotification {
  const status: WorkflowStatus = wf.status
  let type: DashboardNotification['type']
  let title: string

  if (status === 'Running' || status === 'Queued' || status === 'Unknown') {
    type = 'execution_running'
    title = `Workflow running: ${wf.workflowName || wf.id}`
  } else if (status === 'Completed') {
    type = 'execution_completed'
    title = `Workflow completed: ${wf.workflowName || wf.id}`
  } else if (status === 'Failed') {
    type = 'execution_failed'
    title = `Workflow failed: ${wf.workflowName || wf.id}`
  } else {
    type = 'execution_completed'
    title = `Workflow ${status.toLowerCase()}: ${wf.workflowName || wf.id}`
  }

  return {
    id: `wf-${wf.id}`,
    type,
    title,
    description: `${wf.applicationName} v${wf.applicationVersion} — ${status}${wf.userFullName ? ` by ${wf.userFullName}` : ''}`,
    date: wf.endDate || wf.startDate,
    read: false,
    link: `/workflows/${wf.id}`,
  }
}

export const useNotificationsStore = defineStore('notifications', () => {
  const toasts = ref<Notification[]>([])
  const dashboardNotifications = ref<DashboardNotification[]>([])
  const isLoadingDashboard = ref(false)

  function addToast(toast: Omit<Notification, 'id'>): string {
    const id = 'toast-' + Date.now()
    toasts.value.push({ ...toast, id })
    const duration = toast.duration ?? 5000
    setTimeout(() => removeToast(id), duration)
    return id
  }

  function updateToast(id: string, updates: Partial<Omit<Notification, 'id'>>) {
    const toast = toasts.value.find((t) => t.id === id)
    if (!toast) {
      return
    }

    Object.assign(toast, updates)

    if (typeof updates.duration === 'number') {
      setTimeout(() => removeToast(id), updates.duration)
    }
  }

  function removeToast(id: string) {
    toasts.value = toasts.value.filter((t) => t.id !== id)
  }

  function success(message: string, title = 'Success') {
    addToast({ type: 'success', title, message })
  }

  function error(message: string, title = 'Error') {
    addToast({ type: 'error', title, message })
  }

  function warning(message: string, title = 'Warning') {
    addToast({ type: 'warning', title, message })
  }

  function info(message: string, title = 'Information') {
    addToast({ type: 'info', title, message })
  }

  async function fetchDashboardNotifications() {
    isLoadingDashboard.value = true
    try {
      const [messages, workflowsPage] = await Promise.all([
        messagesApi.getReceived().catch(() => [] as BackendMessage[]),
        workflowsApi.list({ quantity: 20 }).catch(() => null),
      ])

      const notifications: DashboardNotification[] = [
        ...messages.map(mapMessageToNotification),
        ...(workflowsPage?.data ?? []).map(mapWorkflowToNotification),
      ]

      notifications.sort((a, b) => new Date(b.date).getTime() - new Date(a.date).getTime())

      dashboardNotifications.value = notifications
    } finally {
      isLoadingDashboard.value = false
    }
  }

  function markAsRead(id: string) {
    const notif = dashboardNotifications.value.find((n) => n.id === id)
    if (notif) notif.read = true
  }

  return {
    toasts,
    dashboardNotifications,
    isLoadingDashboard,
    addToast,
    updateToast,
    removeToast,
    success,
    error,
    warning,
    info,
    fetchDashboardNotifications,
    markAsRead,
  }
})
