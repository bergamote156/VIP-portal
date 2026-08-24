export type ParsedInputType = 'File' | 'String' | 'Number' | 'Boolean'

export type ParsedInput = {
  id: string
  name: string
  type: ParsedInputType
  description: string
  required: boolean
  defaultValue?: string
  possibleValues?: string[]
  integer: boolean
  minimum?: number
  maximum?: number
  exclusiveMinimum: boolean
  exclusiveMaximum: boolean
  requiresInputs: string[]
  disablesInputs: string[]
  valueRequires: Record<string, string[]>
  valueDisables: Record<string, string[]>
  valueKey?: string
  commandLineFlag?: string
  commandLineFlagSeparator?: string
  usesAbsolutePath: boolean
}

export type ParsedGroup = {
  id: string
  name: string
  description: string
  members: string[]
  mutuallyExclusive: boolean
  oneIsRequired: boolean
  allOrNone: boolean
}

export type ParsedDescriptorForm = {
  inputs: ParsedInput[]
  groups: ParsedGroup[]
}

function mapBoutiquesType(rawType: unknown): ParsedInputType {
  const normalized = typeof rawType === 'string' ? rawType.trim().toLowerCase() : ''
  if (normalized === 'file') return 'File'
  if (normalized === 'number' || normalized === 'integer' || normalized === 'float') return 'Number'
  if (normalized === 'flag' || normalized === 'boolean') return 'Boolean'
  return 'String'
}

function asString(value: unknown): string | undefined {
  return typeof value === 'string' && value.trim().length > 0 ? value.trim() : undefined
}

function asBoolean(value: unknown, fallback = false): boolean {
  return typeof value === 'boolean' ? value : fallback
}

function asNumber(value: unknown): number | undefined {
  return typeof value === 'number' && Number.isFinite(value) ? value : undefined
}

function asStringArray(value: unknown): string[] {
  if (!Array.isArray(value)) return []
  return value.filter((item): item is string => typeof item === 'string' && item.trim().length > 0)
}

function asStringMapOfStringArray(value: unknown): Record<string, string[]> {
  if (!value || typeof value !== 'object') return {}

  const source = value as Record<string, unknown>
  const output: Record<string, string[]> = {}

  for (const [key, rawIds] of Object.entries(source)) {
    const normalizedKey = key.trim()
    if (!normalizedKey) continue

    output[normalizedKey] = asStringArray(rawIds)
  }

  return output
}

function defaultValueAsString(value: unknown): string | undefined {
  if (value === undefined || value === null) return undefined
  if (typeof value === 'string') return value
  if (typeof value === 'number' || typeof value === 'boolean') return String(value)

  try {
    return JSON.stringify(value)
  } catch {
    return undefined
  }
}

function parseInput(input: Record<string, unknown>, index: number): ParsedInput {
  const rawName = asString(input.name)
  const rawId = asString(input.id)
  const fallbackId = `unnamed-input-${index + 1}`
  const id = rawId || rawName || fallbackId

  const possibleValues = Array.isArray(input['value-choices'])
    ? input['value-choices']
        .filter((choice): choice is string | number | boolean => ['string', 'number', 'boolean'].includes(typeof choice))
        .map((choice) => String(choice))
    : undefined

  return {
    id,
    name: rawName || rawId || `Unnamed input ${index + 1}`,
    type: mapBoutiquesType(input.type),
    description: asString(input.description) ?? '-',
    required: input.optional === true ? false : true,
    defaultValue: defaultValueAsString(input['default-value']),
    possibleValues,
    integer: asBoolean(input.integer, false),
    minimum: asNumber(input.minimum),
    maximum: asNumber(input.maximum),
    exclusiveMinimum: asBoolean(input['exclusive-minimum'], false),
    exclusiveMaximum: asBoolean(input['exclusive-maximum'], false),
    requiresInputs: asStringArray(input['requires-inputs']),
    disablesInputs: asStringArray(input['disables-inputs']),
    valueRequires: asStringMapOfStringArray(input['value-requires']),
    valueDisables: asStringMapOfStringArray(input['value-disables']),
    valueKey: asString(input['value-key']),
    commandLineFlag: asString(input['command-line-flag']),
    commandLineFlagSeparator: asString(input['command-line-flag-separator']),
    usesAbsolutePath: asBoolean(input['uses-absolute-path'], false),
  }
}

function parseGroup(group: Record<string, unknown>, index: number): ParsedGroup {
  const rawId = asString(group.id)
  const rawName = asString(group.name)

  return {
    id: rawId || `group-${index + 1}`,
    name: rawName || rawId || `Group ${index + 1}`,
    description: asString(group.description) ?? '',
    members: asStringArray(group.members),
    mutuallyExclusive: asBoolean(group['mutually-exclusive'], false),
    oneIsRequired: asBoolean(group['one-is-required'], false),
    allOrNone: asBoolean(group['all-or-none'], false),
  }
}

export function parseDescriptorForm(descriptor: string | null): ParsedDescriptorForm {
  if (!descriptor) {
    return { inputs: [], groups: [] }
  }

  try {
    const parsed = JSON.parse(descriptor) as {
      inputs?: unknown
      groups?: unknown
    }

    const inputs = Array.isArray(parsed.inputs)
      ? parsed.inputs
          .filter((item): item is Record<string, unknown> => item !== null && typeof item === 'object')
          .map((input, index) => parseInput(input, index))
      : []

    const groups = Array.isArray(parsed.groups)
      ? parsed.groups
          .filter((item): item is Record<string, unknown> => item !== null && typeof item === 'object')
          .map((group, index) => parseGroup(group, index))
      : []

    return { inputs, groups }
  } catch {
    return { inputs: [], groups: [] }
  }
}

export function parseDescriptorInputs(descriptor: string | null): ParsedInput[] {
  return parseDescriptorForm(descriptor).inputs
}
