import { beforeEach, describe, expect, it, vi } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import DashboardView from './DashboardView.vue'

const mocked = vi.hoisted(() => ({
  authStore: {
    user: { email: 'user@example.com' } as { email: string } | null,
  },
  getRecentApplications: vi.fn(),
}))

vi.mock('vue-router', () => ({
  RouterLink: {
    name: 'RouterLink',
    props: ['to'],
    template: '<a><slot /></a>',
  },
}))

vi.mock('@/stores/auth.store', () => ({
  useAuthStore: () => mocked.authStore,
}))

vi.mock('@/stores/notifications.store', () => ({
  useNotificationsStore: () => ({
    dashboardNotifications: [] as any[],
    isLoadingDashboard: false,
    fetchDashboardNotifications: vi.fn(),
    markAsRead: vi.fn(),
  }),
}))

vi.mock('@/composables/useFormatters', () => ({
  useFormatters: () => ({
    formatRelativeTime: vi.fn(() => 'just now'),
  }),
}))

vi.mock('@/utils/recentApplications', () => ({
  getRecentApplications: mocked.getRecentApplications,
}))

describe('DashboardView', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    mocked.authStore.user = { email: 'user@example.com' }
  })

  it('shows empty-state when there is no recent app usage', async () => {
    mocked.getRecentApplications.mockReturnValue([])

    const wrapper = mount(DashboardView)
    await flushPromises()

    expect(mocked.getRecentApplications).toHaveBeenCalledWith(4)
    expect(wrapper.text()).toContain('Welcome, user@example.com')
    expect(wrapper.text()).toContain('No recent application usage yet')
  })

  it('renders recent applications cards', async () => {
    mocked.getRecentApplications.mockReturnValue([
      {
        name: 'freesurfer',
        fullName: 'FreeSurfer',
        lastVersion: '2.0.0',
        usedAt: '2026-03-24T10:00:00.000Z',
      },
      {
        name: 'fsl',
        usedAt: '2026-03-24T09:00:00.000Z',
      },
    ])

    const wrapper = mount(DashboardView)
    await flushPromises()

    expect(wrapper.text()).toContain('FreeSurfer')
    expect(wrapper.text()).toContain('fsl')
    expect(wrapper.text()).toContain('2.0.0')
    expect(wrapper.text()).toContain('Last used:')
  })
})
