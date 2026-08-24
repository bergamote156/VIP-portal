import { beforeEach, describe, expect, it, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import LoginView from './LoginView.vue'

const mocked = vi.hoisted(() => ({
  push: vi.fn(),
  login: vi.fn(),
  notifySuccess: vi.fn(),
  notifyError: vi.fn(),
  isAxiosError: vi.fn((value: unknown) => Boolean((value as { isAxiosError?: boolean })?.isAxiosError)),
  authStore: {
    isLoading: false,
    login: vi.fn(),
  },
}))

vi.mock('vue-router', () => ({
  useRouter: () => ({ push: mocked.push }),
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
    success: mocked.notifySuccess,
    error: mocked.notifyError,
  }),
}))

vi.mock('axios', () => ({
  default: {
    isAxiosError: mocked.isAxiosError,
  },
}))

describe('LoginView', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    mocked.authStore.login = mocked.login
    mocked.authStore.isLoading = false
  })

  it('logs in and redirects to dashboard on success', async () => {
    mocked.login.mockResolvedValue({ confirmed: true })

    const wrapper = mount(LoginView)

    await wrapper.get('input[type="email"]').setValue('user@example.com')
    await wrapper.get('input[type="password"]').setValue('secret')
    await wrapper.get('form').trigger('submit')

    expect(mocked.login).toHaveBeenCalledWith({ username: 'user@example.com', password: 'secret' })
    expect(mocked.notifySuccess).toHaveBeenCalledWith('Vous êtes maintenant connecté.')
    expect(mocked.push).toHaveBeenCalledWith('/dashboard')
  })

  it('shows dedicated error message on 401', async () => {
    mocked.login.mockRejectedValue({
      isAxiosError: true,
      response: { status: 401 },
    })

    const wrapper = mount(LoginView)

    await wrapper.get('input[type="email"]').setValue('user@example.com')
    await wrapper.get('input[type="password"]').setValue('wrong')
    await wrapper.get('form').trigger('submit')

    expect(mocked.notifyError).toHaveBeenCalledWith('Invalid email or password.')
    expect(mocked.push).not.toHaveBeenCalled()
  })
})
