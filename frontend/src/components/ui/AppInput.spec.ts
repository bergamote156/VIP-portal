import { describe, expect, it } from 'vitest'
import { mount } from '@vue/test-utils'
import AppInput from './AppInput.vue'

describe('AppInput', () => {
  it('renders label and required marker', () => {
    const wrapper = mount(AppInput, {
      props: {
        label: 'Execution name',
        required: true,
      },
    })

    expect(wrapper.text()).toContain('Execution name')
    expect(wrapper.text()).toContain('*')
  })

  it('emits update:modelValue on input', async () => {
    const wrapper = mount(AppInput)

    await wrapper.get('input').setValue('my-run')

    expect(wrapper.emitted('update:modelValue')).toBeTruthy()
    expect(wrapper.emitted('update:modelValue')?.[0]).toEqual(['my-run'])
  })

  it('displays error text when provided', () => {
    const wrapper = mount(AppInput, {
      props: {
        error: 'This field is required',
      },
    })

    expect(wrapper.text()).toContain('This field is required')
  })
})
