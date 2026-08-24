import { computed, ref, type Ref } from 'vue'
import type { AppVersion } from '@/types/appversion.types'
import { parseDescriptorForm, type ParsedGroup, type ParsedInput } from '@/utils/boutiquesDescriptor'

export type FormValue = string | number | boolean | File | null

export function useBoutiquesLaunchForm(selectedVersion: Ref<AppVersion | null>) {
  const formValues = ref<Record<string, FormValue>>({})
  const formErrors = ref<Record<string, string>>({})
  const groupErrors = ref<string[]>([])

  const descriptorForm = computed(() => parseDescriptorForm(selectedVersion.value?.descriptor ?? null))
  const inputParams = computed(() => descriptorForm.value.inputs)
  const inputGroups = computed(() => descriptorForm.value.groups)

  const inputById = computed(() => {
    const entries = inputParams.value.map((input) => [input.id, input] as const)
    return new Map<string, ParsedInput>(entries)
  })

  const groupById = computed(() => {
    const entries = inputGroups.value.map((group) => [group.id, group] as const)
    return new Map<string, ParsedGroup>(entries)
  })

  function getInputSelectedKeys(input: ParsedInput): string[] {
    const value = formValues.value[input.id]
    if (value === null || value === undefined || value === '') return []

    if (value instanceof File) {
      return value.name ? [value.name] : []
    }

    return [String(value)]
  }

  function isInputActive(input: ParsedInput): boolean {
    const value = formValues.value[input.id]
    if (value === null || value === undefined) return false

    if (input.type === 'Boolean') return value === true
    if (value instanceof File) return true
    if (typeof value === 'number') return !Number.isNaN(value)
    if (typeof value === 'string') return value.trim().length > 0

    return Boolean(value)
  }

  const activeInputIds = computed(() => {
    return new Set(inputParams.value.filter((input) => isInputActive(input)).map((input) => input.id))
  })

  function isGroupSatisfiedForDependency(groupId: string): boolean {
    const group = groupById.value.get(groupId)
    if (!group) return false

    return group.members.every((memberId) => activeInputIds.value.has(memberId))
  }

  const disabledInputIds = computed(() => {
    const disabledIds = new Set<string>()

    for (const input of inputParams.value) {
      if (!isInputActive(input)) continue

      for (const targetId of input.disablesInputs) {
        disabledIds.add(targetId)
      }

      const selectedKeys = getInputSelectedKeys(input)
      for (const selectedKey of selectedKeys) {
        const valueSpecificDisabledIds = input.valueDisables[selectedKey] ?? []
        for (const targetId of valueSpecificDisabledIds) {
          disabledIds.add(targetId)
        }
      }
    }

    return disabledIds
  })

  function isInputAvailable(input: ParsedInput): boolean {
    if (disabledInputIds.value.has(input.id)) return false

    if (input.requiresInputs.length === 0) return true

    return input.requiresInputs.every((requiredIdOrGroupId) => {
      if (inputById.value.has(requiredIdOrGroupId)) {
        return activeInputIds.value.has(requiredIdOrGroupId)
      }

      if (groupById.value.has(requiredIdOrGroupId)) {
        return isGroupSatisfiedForDependency(requiredIdOrGroupId)
      }

      return false
    })
  }

  function getInputDisabledReason(input: ParsedInput): string | null {
    if (disabledInputIds.value.has(input.id)) {
      return 'Disabled by another selected option.'
    }

    const missingDependencies = input.requiresInputs.filter((requiredIdOrGroupId) => {
      if (inputById.value.has(requiredIdOrGroupId)) {
        return !activeInputIds.value.has(requiredIdOrGroupId)
      }
      if (groupById.value.has(requiredIdOrGroupId)) {
        return !isGroupSatisfiedForDependency(requiredIdOrGroupId)
      }
      return true
    })

    if (missingDependencies.length > 0) {
      return `Requires: ${missingDependencies.join(', ')}`
    }

    return null
  }

  function setInputTextValue(input: ParsedInput, value: string | number) {
    if (input.type === 'Number') {
      const asString = String(value)
      if (asString.trim().length === 0) {
        formValues.value[input.id] = ''
        return
      }

      const parsed = Number(value)
      formValues.value[input.id] = Number.isNaN(parsed) ? '' : parsed
      return
    }

    formValues.value[input.id] = String(value)
  }

  function initializeFormValues() {
    const initialValues: Record<string, FormValue> = {}

    for (const input of inputParams.value) {
      if (input.type === 'Boolean') {
        initialValues[input.id] = input.defaultValue === 'true'
        continue
      }

      if (input.type === 'File') {
        initialValues[input.id] = null
        continue
      }

      if (input.type === 'Number') {
        if (input.defaultValue === undefined || input.defaultValue === '') {
          initialValues[input.id] = ''
        } else {
          const num = Number(input.defaultValue)
          initialValues[input.id] = Number.isNaN(num) ? '' : num
        }
        continue
      }

      initialValues[input.id] = input.defaultValue ?? ''
    }

    formValues.value = initialValues
  }

  function validateInput(input: ParsedInput): string | null {
    if (!isInputAvailable(input)) return null

    const value = formValues.value[input.id]
    const active = isInputActive(input)

    if (input.required && !active) {
      return 'This field is required.'
    }

    if (!active) return null

    if (input.possibleValues && input.possibleValues.length > 0) {
      const scalarValue = String(value)
      if (!input.possibleValues.includes(scalarValue)) {
        return `Value must be one of: ${input.possibleValues.join(', ')}.`
      }
    }

    if (input.type === 'Number') {
      const numericValue = typeof value === 'number' ? value : Number(value)
      if (Number.isNaN(numericValue)) {
        return 'Please provide a valid number.'
      }

      if (input.integer && !Number.isInteger(numericValue)) {
        return 'This value must be an integer.'
      }

      if (input.minimum !== undefined) {
        const minIsValid = input.exclusiveMinimum ? numericValue > input.minimum : numericValue >= input.minimum
        if (!minIsValid) {
          return input.exclusiveMinimum
            ? `Value must be greater than ${input.minimum}.`
            : `Value must be greater than or equal to ${input.minimum}.`
        }
      }

      if (input.maximum !== undefined) {
        const maxIsValid = input.exclusiveMaximum ? numericValue < input.maximum : numericValue <= input.maximum
        if (!maxIsValid) {
          return input.exclusiveMaximum
            ? `Value must be less than ${input.maximum}.`
            : `Value must be less than or equal to ${input.maximum}.`
        }
      }
    }

    return null
  }

  function validateGroups(): string[] {
    const errors: string[] = []

    for (const group of inputGroups.value) {
      const availableMembers = group.members
        .map((memberId) => inputById.value.get(memberId))
        .filter((input): input is ParsedInput => input !== undefined)
        .filter((input) => isInputAvailable(input))

      if (availableMembers.length === 0) continue

      const activeCount = availableMembers.filter((input) => isInputActive(input)).length

      if (group.oneIsRequired && activeCount === 0) {
        errors.push(`Group "${group.name}" requires at least one active input.`)
      }

      if (group.mutuallyExclusive && activeCount > 1) {
        errors.push(`Group "${group.name}" accepts only one active input at a time.`)
      }

      if (group.allOrNone && activeCount > 0 && activeCount < availableMembers.length) {
        errors.push(`Group "${group.name}" requires all available members to be set together.`)
      }
    }

    return errors
  }

  function validateForm(): boolean {
    const nextErrors: Record<string, string> = {}

    for (const input of inputParams.value) {
      const error = validateInput(input)
      if (error) {
        nextErrors[input.id] = error
      }
    }

    formErrors.value = nextErrors
    groupErrors.value = validateGroups()

    return Object.keys(nextErrors).length === 0 && groupErrors.value.length === 0
  }

  function onFileChange(inputId: string, event: Event) {
    const target = event.target as HTMLInputElement
    formValues.value[inputId] = target.files?.[0] ?? null
  }

  function resetValidation() {
    formErrors.value = {}
    groupErrors.value = []
  }

  function clearForm() {
    formValues.value = {}
    resetValidation()
  }

  return {
    inputParams,
    formValues,
    formErrors,
    groupErrors,
    isInputAvailable,
    getInputDisabledReason,
    setInputTextValue,
    initializeFormValues,
    validateForm,
    onFileChange,
    resetValidation,
    clearForm,
  }
}
