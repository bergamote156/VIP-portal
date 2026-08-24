import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { createPinia, setActivePinia } from 'pinia'
import { useNotificationsStore } from './notifications.store'

describe('useNotificationsStore', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.useFakeTimers()
    vi.setSystemTime(new Date('2026-03-24T09:00:00Z'))
  })

  afterEach(() => {
    vi.useRealTimers()
  })

  it('adds and auto-removes a toast with default duration', () => {
    const store = useNotificationsStore()

    store.success('Saved successfully')

    expect(store.toasts).toHaveLength(1)
    expect(store.toasts[0]?.id).toBe('toast-1774342800000')
    expect(store.toasts[0]?.type).toBe('success')

    vi.advanceTimersByTime(5000)

    expect(store.toasts).toHaveLength(0)
  })

  it('supports custom toast duration', () => {
    const store = useNotificationsStore()

    store.addToast({ type: 'warning', title: 'Warning', message: 'Check inputs', duration: 1000 })
    expect(store.toasts).toHaveLength(1)

    vi.advanceTimersByTime(999)
    expect(store.toasts).toHaveLength(1)

    vi.advanceTimersByTime(1)
    expect(store.toasts).toHaveLength(0)
  })

  it('adds wrappers with expected default titles', () => {
    const store = useNotificationsStore()

    store.error('Something failed')
    store.warning('Careful')
    store.info('FYI')

    expect(store.toasts.map((t) => t.title)).toEqual(['Error', 'Warning', 'Information'])
    expect(store.toasts.map((t) => t.type)).toEqual(['error', 'warning', 'info'])
  })

  it('marks dashboard notification as read and resets dashboard list', async () => {
    const store = useNotificationsStore()

    store.dashboardNotifications = [
      {
        id: 'n-1',
        type: 'message',
        title: 'Message',
        description: 'Hello',
        date: '2026-03-24',
        read: false,
      },
    ]

    store.markAsRead('n-1')
    expect(store.dashboardNotifications[0]?.read).toBe(true)

    await store.fetchDashboardNotifications()
    expect(store.dashboardNotifications).toEqual([])
  })
})
