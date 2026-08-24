import { describe, expect, it } from 'vitest'
import { mount } from '@vue/test-utils'
import AppButton from './AppButton.vue'

describe('AppButton', () => {
  it('renders slot content', () => {
    const wrapper = mount(AppButton, {
      slots: {
        default: 'Launch application',
      },
    })

    expect(wrapper.text()).toContain('Launch application')
  })

  it('disables the native button when loading', () => {
    const wrapper = mount(AppButton, {
      props: {
        loading: true,
      },
    })

    expect(wrapper.get('button').attributes('disabled')).toBeDefined()
  })

  it('sets the button type from props', () => {
    const wrapper = mount(AppButton, {
      props: {
        type: 'submit',
      },
    })

    expect(wrapper.get('button').attributes('type')).toBe('submit')
  })
})
