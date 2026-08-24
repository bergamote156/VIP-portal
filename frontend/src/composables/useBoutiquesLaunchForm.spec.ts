import { describe, expect, it } from 'vitest'
import { ref } from 'vue'
import type { AppVersion } from '@/types/appversion.types'
import { useBoutiquesLaunchForm } from './useBoutiquesLaunchForm'
import type { ParsedInput } from '@/utils/boutiquesDescriptor'

function createVersionFromDescriptor(descriptorObject: unknown): AppVersion {
  return {
    applicationName: 'demo-app',
    version: '1.0.0',
    descriptor: JSON.stringify(descriptorObject),
    parsedDescriptor: null,
    doi: null,
    visible: true,
    resources: [],
    tags: [],
    settings: [],
    source: null,
    note: null,
  }
}

function getInputOrThrow(inputs: ParsedInput[], inputId: string): ParsedInput {
  const input = inputs.find((current) => current.id === inputId)
  if (!input) {
    throw new Error(`Expected input not found: ${inputId}`)
  }

  return input
}

describe('useBoutiquesLaunchForm', () => {
  it('initializes defaults by input type', () => {
    const version = ref(
      createVersionFromDescriptor({
        inputs: [
          { id: 'flag', name: 'Flag', type: 'Flag', optional: true, 'default-value': true },
          { id: 'count', name: 'Count', type: 'Number', optional: true, 'default-value': 3 },
          { id: 'name', name: 'Name', type: 'String', optional: true, 'default-value': 'demo' },
          { id: 'file', name: 'File', type: 'File', optional: true },
        ],
      }),
    )

    const form = useBoutiquesLaunchForm(version)
    form.initializeFormValues()

    expect(form.formValues.value.flag).toBe(true)
    expect(form.formValues.value.count).toBe(3)
    expect(form.formValues.value.name).toBe('demo')
    expect(form.formValues.value.file).toBeNull()
  })

  it('handles requires-inputs and disables-inputs availability', () => {
    const version = ref(
      createVersionFromDescriptor({
        inputs: [
          { id: 'source', name: 'Source', type: 'String', optional: true },
          { id: 'target', name: 'Target', type: 'String', optional: true, 'requires-inputs': ['source'] },
          { id: 'legacy', name: 'Legacy', type: 'String', optional: true },
          { id: 'toggle', name: 'Toggle', type: 'Flag', optional: true, 'disables-inputs': ['legacy'] },
        ],
      }),
    )

    const form = useBoutiquesLaunchForm(version)
    form.initializeFormValues()

    const source = form.inputParams.value.find((input) => input.id === 'source')
    const target = form.inputParams.value.find((input) => input.id === 'target')
    const legacy = form.inputParams.value.find((input) => input.id === 'legacy')

    expect(source).toBeDefined()
    expect(target).toBeDefined()
    expect(legacy).toBeDefined()

    if (!source || !target || !legacy) {
      throw new Error('Expected inputs are missing in descriptor parsing')
    }

    expect(form.isInputAvailable(target)).toBe(false)
    expect(form.getInputDisabledReason(target)).toBe('Requires: source')

    form.setInputTextValue(source, 'run-1')

    expect(form.isInputAvailable(target)).toBe(true)
    expect(form.getInputDisabledReason(target)).toBeNull()

    form.formValues.value.toggle = true

    expect(form.isInputAvailable(legacy)).toBe(false)
    expect(form.getInputDisabledReason(legacy)).toBe('Disabled by another selected option.')
  })

  it('applies value-specific disables from selected choice', () => {
    const version = ref(
      createVersionFromDescriptor({
        inputs: [
          {
            id: 'mode',
            name: 'Mode',
            type: 'String',
            optional: true,
            'value-choices': ['safe', 'fast'],
            'value-disables': {
              fast: ['threads'],
              safe: [],
            },
          },
          { id: 'threads', name: 'Threads', type: 'Number', optional: true },
        ],
      }),
    )

    const form = useBoutiquesLaunchForm(version)
    form.initializeFormValues()

    const mode = form.inputParams.value.find((input) => input.id === 'mode')
    const threads = form.inputParams.value.find((input) => input.id === 'threads')

    expect(mode).toBeDefined()
    expect(threads).toBeDefined()

    if (!mode || !threads) {
      throw new Error('Expected inputs are missing in descriptor parsing')
    }

    form.setInputTextValue(mode, 'safe')
    expect(form.isInputAvailable(threads)).toBe(true)

    form.setInputTextValue(mode, 'fast')
    expect(form.isInputAvailable(threads)).toBe(false)
    expect(form.getInputDisabledReason(threads)).toBe('Disabled by another selected option.')
  })

  it('validates required and value choice constraints', () => {
    const version = ref(
      createVersionFromDescriptor({
        inputs: [
          {
            id: 'format',
            name: 'Format',
            type: 'String',
            optional: false,
            'value-choices': ['json', 'yaml'],
          },
        ],
      }),
    )

    const form = useBoutiquesLaunchForm(version)
    form.initializeFormValues()

    expect(form.validateForm()).toBe(false)
    expect(form.formErrors.value.format).toBe('This field is required.')

    const formatInput = getInputOrThrow(form.inputParams.value, 'format')
    form.setInputTextValue(formatInput, 'xml')

    expect(form.validateForm()).toBe(false)
    expect(form.formErrors.value.format).toContain('Value must be one of: json, yaml.')

    form.setInputTextValue(formatInput, 'json')

    expect(form.validateForm()).toBe(true)
    expect(form.formErrors.value.format).toBeUndefined()
  })

  it('validates number, integer and min/max exclusivity constraints', () => {
    const version = ref(
      createVersionFromDescriptor({
        inputs: [
          {
            id: 'count',
            name: 'Count',
            type: 'Number',
            optional: false,
            integer: true,
            minimum: 1,
            'exclusive-minimum': true,
            maximum: 5,
            'exclusive-maximum': true,
          },
        ],
      }),
    )

    const form = useBoutiquesLaunchForm(version)
    form.initializeFormValues()
    const countInput = getInputOrThrow(form.inputParams.value, 'count')

    form.setInputTextValue(countInput, 1.5)
    expect(form.validateForm()).toBe(false)
    expect(form.formErrors.value.count).toBe('This value must be an integer.')

    form.setInputTextValue(countInput, 1)
    expect(form.validateForm()).toBe(false)
    expect(form.formErrors.value.count).toBe('Value must be greater than 1.')

    form.setInputTextValue(countInput, 5)
    expect(form.validateForm()).toBe(false)
    expect(form.formErrors.value.count).toBe('Value must be less than 5.')

    form.setInputTextValue(countInput, 3)
    expect(form.validateForm()).toBe(true)
  })

  it('validates groups for one-is-required and mutually-exclusive', () => {
    const version = ref(
      createVersionFromDescriptor({
        inputs: [
          { id: 'alpha', name: 'Alpha', type: 'Flag', optional: true },
          { id: 'beta', name: 'Beta', type: 'Flag', optional: true },
          { id: 'left', name: 'Left', type: 'Flag', optional: true },
          { id: 'right', name: 'Right', type: 'Flag', optional: true },
        ],
        groups: [
          {
            id: 'required-group',
            name: 'Required group',
            members: ['alpha', 'beta'],
            'one-is-required': true,
          },
          {
            id: 'exclusive-group',
            name: 'Exclusive group',
            members: ['left', 'right'],
            'mutually-exclusive': true,
          },
        ],
      }),
    )

    const form = useBoutiquesLaunchForm(version)
    form.initializeFormValues()

    expect(form.validateForm()).toBe(false)
    expect(form.groupErrors.value).toContain('Group "Required group" requires at least one active input.')

    form.formValues.value.alpha = true
    form.formValues.value.left = true
    form.formValues.value.right = true

    expect(form.validateForm()).toBe(false)
    expect(form.groupErrors.value).toContain('Group "Exclusive group" accepts only one active input at a time.')

    form.formValues.value.right = false

    expect(form.validateForm()).toBe(true)
    expect(form.groupErrors.value).toHaveLength(0)
  })

  it('handles file changes and can clear validation state', () => {
    const version = ref(
      createVersionFromDescriptor({
        inputs: [{ id: 'payload', name: 'Payload', type: 'File', optional: false }],
      }),
    )

    const form = useBoutiquesLaunchForm(version)
    form.initializeFormValues()

    expect(form.validateForm()).toBe(false)
    expect(form.formErrors.value.payload).toBe('This field is required.')

    const inputEl = document.createElement('input')
    const file = new File(['hello'], 'payload.txt', { type: 'text/plain' })
    Object.defineProperty(inputEl, 'files', {
      value: [file],
      configurable: true,
    })

    form.onFileChange('payload', { target: inputEl } as unknown as Event)

    expect(form.formValues.value.payload).toBe(file)
    expect(form.validateForm()).toBe(true)

    form.resetValidation()
    expect(form.formErrors.value).toEqual({})
    expect(form.groupErrors.value).toEqual([])

    form.clearForm()
    expect(form.formValues.value).toEqual({})
    expect(form.formErrors.value).toEqual({})
    expect(form.groupErrors.value).toEqual([])
  })
})
