import { describe, expect, it, vi } from 'vitest'
import { ref } from 'vue'
import { useDuplicatedLaunchInputs } from './useDuplicatedLaunchInputs'
import type { ParsedInput } from '@/utils/boutiquesDescriptor'
import type { FormValue } from '@/composables/useBoutiquesLaunchForm'

function makeInput(overrides: Partial<ParsedInput>): ParsedInput {
  return {
    id: overrides.id ?? 'input-id',
    name: overrides.name ?? 'Input',
    type: overrides.type ?? 'String',
    description: overrides.description ?? '',
    required: overrides.required ?? false,
    defaultValue: overrides.defaultValue,
    possibleValues: overrides.possibleValues,
    integer: overrides.integer ?? false,
    minimum: overrides.minimum,
    maximum: overrides.maximum,
    exclusiveMinimum: overrides.exclusiveMinimum ?? false,
    exclusiveMaximum: overrides.exclusiveMaximum ?? false,
    requiresInputs: overrides.requiresInputs ?? [],
    disablesInputs: overrides.disablesInputs ?? [],
    valueRequires: overrides.valueRequires ?? {},
    valueDisables: overrides.valueDisables ?? {},
    valueKey: overrides.valueKey,
    commandLineFlag: overrides.commandLineFlag,
    commandLineFlagSeparator: overrides.commandLineFlagSeparator,
    usesAbsolutePath: overrides.usesAbsolutePath ?? false,
  }
}

describe('useDuplicatedLaunchInputs', () => {
  function createComposable() {
    const inputParams = ref<ParsedInput[]>([
      makeInput({ id: 'text', type: 'String' }),
      makeInput({ id: 'number', type: 'Number' }),
      makeInput({ id: 'file', type: 'File' }),
      makeInput({ id: 'flag', type: 'Boolean' }),
    ])

    const formValues = ref<Record<string, FormValue>>({
      text: 'hello',
      number: 12,
      file: null,
      flag: false,
    })

    const onFileChange = vi.fn<(inputId: string, event: Event) => void>()
    const setInputTextValue = vi.fn<(input: ParsedInput, value: string | number) => void>()

    const composable = useDuplicatedLaunchInputs(inputParams, formValues, onFileChange, setInputTextValue)

    return {
      inputParams,
      formValues,
      onFileChange,
      setInputTextValue,
      ...composable,
    }
  }

  it('allows duplication for non-boolean inputs only', () => {
    const composable = createComposable()

    expect(composable.canDuplicateInput('String')).toBe(true)
    expect(composable.canDuplicateInput('Number')).toBe(true)
    expect(composable.canDuplicateInput('File')).toBe(true)
    expect(composable.canDuplicateInput('Boolean')).toBe(false)
  })

  it('initializes instance ids and resets clone state', () => {
    const composable = createComposable()

    composable.initializeInputInstances()
    expect(composable.getInputInstanceIds('text')).toEqual(['0'])
    expect(composable.getInputInstanceIds('number')).toEqual(['0'])

    composable.addInputClone('text')
    expect(composable.getInputInstanceIds('text')).toEqual(['0', 'clone-1'])

    composable.initializeInputInstances()
    expect(composable.getInputInstanceIds('text')).toEqual(['0'])

    composable.addInputClone('text')
    expect(composable.getInputInstanceIds('text')).toEqual(['0', 'clone-1'])
  })

  it('adds and removes clone instances while preserving base instance', () => {
    const composable = createComposable()
    composable.initializeInputInstances()

    composable.addInputClone('text')
    composable.addInputClone('text')

    expect(composable.getInputInstanceIds('text')).toEqual(['0', 'clone-1', 'clone-2'])
    expect(composable.getInputValueForInstance('text', 'clone-1')).toBe('hello')

    composable.removeInputClone('text', 'clone-1')
    expect(composable.getInputInstanceIds('text')).toEqual(['0', 'clone-2'])

    composable.removeInputClone('text', '0')
    expect(composable.getInputInstanceIds('text')).toEqual(['0', 'clone-2'])
  })

  it('gets and sets values for base and clone instances', () => {
    const composable = createComposable()
    composable.initializeInputInstances()
    composable.addInputClone('text')

    expect(composable.getInputValueForInstance('text', '0')).toBe('hello')
    expect(composable.getInputValueForInstance('text', 'clone-1')).toBe('hello')

    composable.setInputValueForInstance('text', '0', 'base-updated')
    composable.setInputValueForInstance('text', 'clone-1', 'clone-updated')

    expect(composable.formValues.value.text).toBe('base-updated')
    expect(composable.getInputValueForInstance('text', 'clone-1')).toBe('clone-updated')
  })

  it('delegates file change for base instance and stores clone file directly', () => {
    const composable = createComposable()
    composable.initializeInputInstances()
    composable.addInputClone('file')

    const baseEvent = { target: document.createElement('input') } as unknown as Event
    composable.onFileChangeForInstance('file', '0', baseEvent)

    expect(composable.onFileChange).toHaveBeenCalledWith('file', baseEvent)

    const cloneInputEl = document.createElement('input')
    const cloneFile = new File(['abc'], 'clone.txt', { type: 'text/plain' })
    Object.defineProperty(cloneInputEl, 'files', {
      value: [cloneFile],
      configurable: true,
    })

    composable.onFileChangeForInstance('file', 'clone-1', { target: cloneInputEl } as unknown as Event)

    expect(composable.getInputValueForInstance('file', 'clone-1')).toBe(cloneFile)
  })

  it('delegates text setter for base and parses clone numeric values correctly', () => {
    const composable = createComposable()
    composable.initializeInputInstances()
    composable.addInputClone('number')

    const numberInput = composable.inputParams.value.find((input) => input.id === 'number')
    if (!numberInput) {
      throw new Error('Expected number input to exist')
    }

    composable.setInputTextValueForInstance(numberInput, '0', '99')
    expect(composable.setInputTextValue).toHaveBeenCalledWith(numberInput, '99')

    composable.setInputTextValueForInstance(numberInput, 'clone-1', '  ')
    expect(composable.getInputValueForInstance('number', 'clone-1')).toBe('')

    composable.setInputTextValueForInstance(numberInput, 'clone-1', 'not-a-number')
    expect(composable.getInputValueForInstance('number', 'clone-1')).toBe('')

    composable.setInputTextValueForInstance(numberInput, 'clone-1', '42')
    expect(composable.getInputValueForInstance('number', 'clone-1')).toBe(42)

    const textInput = composable.inputParams.value.find((input) => input.id === 'text')
    if (!textInput) {
      throw new Error('Expected text input to exist')
    }

    composable.addInputClone('text')
    composable.setInputTextValueForInstance(textInput, 'clone-2', 123)
    expect(composable.getInputValueForInstance('text', 'clone-2')).toBe('123')
  })

  it('builds submission payload for all instances and serializes files', () => {
    const composable = createComposable()
    composable.initializeInputInstances()

    composable.addInputClone('text')
    composable.setInputValueForInstance('text', 'clone-1', 'hello-clone')

    composable.addInputClone('file')
    const file = new File(['payload'], 'payload.txt', { type: 'text/plain' })
    composable.setInputValueForInstance('file', 'clone-2', file)

    const submissionInputs = composable.buildSubmissionInputs()

    const textSubmission = submissionInputs.find((input) => input.id === 'text')
    const fileSubmission = submissionInputs.find((input) => input.id === 'file')

    expect(textSubmission).toEqual({
      id: 'text',
      values: [
        { instanceId: '0', value: 'hello' },
        { instanceId: 'clone-1', value: 'hello-clone' },
      ],
    })

    expect(fileSubmission).toEqual({
      id: 'file',
      values: [
        { instanceId: '0', value: null },
        {
          instanceId: 'clone-2',
          value: {
            fileName: 'payload.txt',
            size: file.size,
            type: 'text/plain',
          },
        },
      ],
    })
  })
})
