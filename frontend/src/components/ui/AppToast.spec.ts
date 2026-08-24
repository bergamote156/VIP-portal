import { describe, expect, it, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import { nextTick } from 'vue'
import AppToast from './AppToast.vue'
import { useNotificationsStore } from '@/stores/notifications.store'

describe('AppToast', () => {
  function mountWithStore() {
    const pinia = createPinia()
    setActivePinia(pinia)

    const wrapper = mount(AppToast, {
      global: {
        plugins: [pinia],
        stubs: {
          TransitionGroup: false,
        },
      },
    })

    const notifications = useNotificationsStore()
    return { wrapper, notifications }
  }

  it('renders notification title and message', async () => {
    const { wrapper, notifications } = mountWithStore()

    notifications.$patch({
      toasts: [
        {
          id: 'toast-1',
          type: 'success',
          title: 'Saved',
          message: 'Application saved',
        },
      ],
    })

    await nextTick()

    expect(wrapper.text()).toContain('Saved')
    expect(wrapper.text()).toContain('Application saved')
  })

  it('removes a toast when close button is clicked', async () => {
    const { wrapper, notifications } = mountWithStore()

    notifications.$patch({
      toasts: [
        {
          id: 'toast-2',
          type: 'error',
          title: 'Failure',
          message: 'Request failed',
        },
      ],
    })

    await nextTick()

    expect(wrapper.text()).toContain('Failure')

    const removeToastSpy = vi.spyOn(notifications, 'removeToast')

    await wrapper.get('button').trigger('click')
    await nextTick()

    expect(removeToastSpy).toHaveBeenCalledWith('toast-2')
    expect(notifications.toasts).toHaveLength(0)
  })

  it('applies type-specific variant classes', async () => {
    const { wrapper, notifications } = mountWithStore()

    notifications.$patch({
      toasts: [
        {
          id: 'toast-3',
          type: 'success',
          title: 'Done',
          message: 'Everything is fine',
        },
      ],
    })

    await nextTick()

    const toastContainer = wrapper.get('[data-toast-id="toast-3"]')
    expect(toastContainer.classes()).toContain('border-emerald-200')
    expect(toastContainer.classes()).toContain('bg-emerald-50/80')
  })
})
