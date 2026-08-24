import { describe, expect, it } from 'vitest'
import { mount } from '@vue/test-utils'
import AppBadge from './AppBadge.vue'

describe('AppBadge', () => {
  it('renders slot content', () => {
    const wrapper = mount(AppBadge, {
      slots: {
        default: 'Running',
      },
    })

    expect(wrapper.text()).toContain('Running')
  })

  it('applies variant classes', () => {
    const wrapper = mount(AppBadge, {
      props: {
        variant: 'success',
      },
    })

    expect(wrapper.classes()).toContain('bg-emerald-50')
    expect(wrapper.classes()).toContain('text-emerald-700')
  })
})
