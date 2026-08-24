import { describe, expect, it } from 'vitest'
import { mount } from '@vue/test-utils'
import AppCard from './AppCard.vue'

describe('AppCard', () => {
  it('renders slot content', () => {
    const wrapper = mount(AppCard, {
      slots: {
        default: 'Card content',
      },
    })

    expect(wrapper.text()).toContain('Card content')
  })

  it('applies optional classes from props', () => {
    const wrapper = mount(AppCard, {
      props: {
        hoverable: true,
        padding: false,
        marginTop: true,
      },
    })

    expect(wrapper.classes()).toContain('cursor-pointer')
    expect(wrapper.classes()).toContain('mt-6')
    expect(wrapper.classes()).not.toContain('p-6')
  })
})
