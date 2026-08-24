import { ref, type Ref } from 'vue'
import type { ParsedInput, ParsedInputType } from '@/utils/boutiquesDescriptor'
import type { FormValue } from '@/composables/useBoutiquesLaunchForm'

type SubmissionInputValue = {
  instanceId: string
  value: unknown
}

export function useDuplicatedLaunchInputs(
  inputParams: Ref<ParsedInput[]>,
  formValues: Ref<Record<string, FormValue>>,
  onFileChange: (inputId: string, event: Event) => void,
  setInputTextValue: (input: ParsedInput, value: string | number) => void,
) {
  const clonedValues = ref<Record<string, FormValue>>({})
  const inputInstanceIds = ref<Record<string, string[]>>({})
  const cloneCounter = ref(0)

  function cloneValue(value: FormValue): FormValue {
    return value
  }

  function getInstanceKey(inputId: string, instanceId: string): string {
    return `${inputId}::${instanceId}`
  }

  function canDuplicateInput(inputType: ParsedInputType): boolean {
    return inputType !== 'Boolean'
  }

  function getInputInstanceIds(inputId: string): string[] {
    return inputInstanceIds.value[inputId] ?? ['0']
  }

  function initializeInputInstances() {
    const nextInstances: Record<string, string[]> = {}

    for (const input of inputParams.value) {
      nextInstances[input.id] = ['0']
    }

    inputInstanceIds.value = nextInstances
    clonedValues.value = {}
    cloneCounter.value = 0
  }

  function addInputClone(inputId: string) {
    const existingInstances = getInputInstanceIds(inputId)
    const newInstanceId = `clone-${cloneCounter.value + 1}`
    cloneCounter.value += 1

    inputInstanceIds.value[inputId] = [...existingInstances, newInstanceId]
    clonedValues.value[getInstanceKey(inputId, newInstanceId)] = cloneValue(formValues.value[inputId] ?? null)
  }

  function removeInputClone(inputId: string, instanceId: string) {
    if (instanceId === '0') return

    inputInstanceIds.value[inputId] = getInputInstanceIds(inputId).filter((id) => id !== instanceId)
    delete clonedValues.value[getInstanceKey(inputId, instanceId)]
  }

  function getInputValueForInstance(inputId: string, instanceId: string): FormValue {
    if (instanceId === '0') {
      return formValues.value[inputId] ?? null
    }

    return clonedValues.value[getInstanceKey(inputId, instanceId)] ?? null
  }

  function setInputValueForInstance(inputId: string, instanceId: string, value: FormValue) {
    if (instanceId === '0') {
      formValues.value[inputId] = value
      return
    }

    clonedValues.value[getInstanceKey(inputId, instanceId)] = value
  }

  function onFileChangeForInstance(inputId: string, instanceId: string, event: Event) {
    if (instanceId === '0') {
      onFileChange(inputId, event)
      return
    }

    const target = event.target as HTMLInputElement
    setInputValueForInstance(inputId, instanceId, target.files?.[0] ?? null)
  }

  function setInputTextValueForInstance(input: ParsedInput, instanceId: string, value: string | number) {
    if (instanceId === '0') {
      setInputTextValue(input, value)
      return
    }

    if (input.type === 'Number') {
      const trimmed = String(value).trim()
      if (trimmed.length === 0) {
        setInputValueForInstance(input.id, instanceId, '')
        return
      }

      const parsed = Number(value)
      setInputValueForInstance(input.id, instanceId, Number.isNaN(parsed) ? '' : parsed)
      return
    }

    setInputValueForInstance(input.id, instanceId, String(value))
  }

  function serializeFormValue(value: FormValue): unknown {
    if (value instanceof File) {
      return {
        fileName: value.name,
        size: value.size,
        type: value.type,
      }
    }

    return value
  }

  function buildSubmissionInputs(): Array<{ id: string; values: SubmissionInputValue[] }> {
    return inputParams.value.map((input) => {
      const instanceIds = getInputInstanceIds(input.id)

      return {
        id: input.id,
        values: instanceIds.map((instanceId) => ({
          instanceId,
          value: serializeFormValue(getInputValueForInstance(input.id, instanceId)),
        })),
      }
    })
  }

  return {
    canDuplicateInput,
    getInputInstanceIds,
    initializeInputInstances,
    addInputClone,
    removeInputClone,
    getInputValueForInstance,
    setInputValueForInstance,
    onFileChangeForInstance,
    setInputTextValueForInstance,
    buildSubmissionInputs,
  }
}
