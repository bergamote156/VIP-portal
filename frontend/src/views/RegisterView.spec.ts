import { beforeEach, describe, expect, it, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import RegisterView from './RegisterView.vue'

const mocked = vi.hoisted(() => ({
  push: vi.fn(),
  register: vi.fn(),
  authStore: {
    isLoading: false,
    register: vi.fn(),
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

describe('RegisterView', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    mocked.authStore.register = mocked.register
    mocked.authStore.isLoading = false
  })

  it('prevents submit when passwords mismatch', async () => {
    const wrapper = mount(RegisterView)

    const inputs = wrapper.findAll('input')
    await inputs[0]?.setValue('John')
    await inputs[1]?.setValue('Doe')
    await inputs[2]?.setValue('john@example.com')
    await inputs[3]?.setValue('password-1')
    await inputs[4]?.setValue('password-2')
    await inputs[5]?.setValue('fr')
    await inputs[6]?.setValue('VIP')
    await inputs[7]?.setValue(true)

    await wrapper.get('form').trigger('submit')

    expect(mocked.register).not.toHaveBeenCalled()
    expect(mocked.push).not.toHaveBeenCalled()
  })

  it('registers and redirects to login when form is valid', async () => {
    mocked.register.mockResolvedValue(undefined)

    const wrapper = mount(RegisterView)

    const inputs = wrapper.findAll('input')
    await inputs[0]?.setValue('John')
    await inputs[1]?.setValue('Doe')
    await inputs[2]?.setValue('john@example.com')
    await inputs[3]?.setValue('password-1')
    await inputs[4]?.setValue('password-1')
    await inputs[5]?.setValue('fr')
    await inputs[6]?.setValue('VIP Lab')
    await inputs[7]?.setValue(true)

    await wrapper.get('textarea').setValue('Research context')
    await wrapper.get('form').trigger('submit')

    expect(mocked.register).toHaveBeenCalledWith({
      firstName: 'John',
      lastName: 'Doe',
      email: 'john@example.com',
      password: 'password-1',
      countryCode: 'fr',
      institution: 'VIP Lab',
      comments: 'Research context',
    })
    expect(mocked.push).toHaveBeenCalledWith({ name: 'activate', params: { id: 'john@example.com' } })
  })
})
