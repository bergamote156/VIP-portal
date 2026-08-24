import { describe, expect, it } from 'vitest'
import { parseDescriptorForm, parseDescriptorInputs } from './boutiquesDescriptor'

describe('boutiquesDescriptor utils', () => {
  it('returns empty form for null or invalid json descriptor', () => {
    expect(parseDescriptorForm(null)).toEqual({ inputs: [], groups: [] })
    expect(parseDescriptorForm('{invalid')).toEqual({ inputs: [], groups: [] })
  })

  it('parses inputs with defaults, type mapping and constraints', () => {
    const descriptor = JSON.stringify({
      inputs: [
        {
          id: 'input-file',
          name: 'Input file',
          type: 'File',
          description: 'A file input',
          optional: false,
          'default-value': null,
          'uses-absolute-path': true,
        },
        {
          id: 'threads',
          name: 'Threads',
          type: 'integer',
          optional: true,
          integer: true,
          minimum: 1,
          maximum: 16,
          'exclusive-minimum': false,
          'exclusive-maximum': true,
          'default-value': 4,
          'value-choices': [1, 2, 4, 8],
          'requires-inputs': ['input-file'],
          'disables-inputs': ['mode'],
          'value-requires': {
            '4': ['memory'],
          },
          'value-disables': {
            '8': ['safe-mode'],
          },
          'command-line-flag': '--threads',
          'command-line-flag-separator': '=',
          'value-key': '[THREADS]',
        },
        {
          name: 'Boolean flag',
          type: 'flag',
          optional: true,
          'default-value': true,
        },
      ],
      groups: [
        {
          id: 'group-a',
          name: 'Group A',
          description: 'Constraint group',
          members: ['threads', 'mode'],
          'mutually-exclusive': true,
          'one-is-required': true,
          'all-or-none': false,
        },
      ],
    })

    const parsed = parseDescriptorForm(descriptor)

    expect(parsed.inputs).toHaveLength(3)
    expect(parsed.groups).toHaveLength(1)

    expect(parsed.inputs[0]).toMatchObject({
      id: 'input-file',
      type: 'File',
      required: true,
      description: 'A file input',
      usesAbsolutePath: true,
    })

    expect(parsed.inputs[1]).toMatchObject({
      id: 'threads',
      type: 'Number',
      required: false,
      integer: true,
      minimum: 1,
      maximum: 16,
      exclusiveMinimum: false,
      exclusiveMaximum: true,
      defaultValue: '4',
      possibleValues: ['1', '2', '4', '8'],
      requiresInputs: ['input-file'],
      disablesInputs: ['mode'],
      valueRequires: { '4': ['memory'] },
      valueDisables: { '8': ['safe-mode'] },
      commandLineFlag: '--threads',
      commandLineFlagSeparator: '=',
      valueKey: '[THREADS]',
    })

    expect(parsed.inputs[2]).toMatchObject({
      type: 'Boolean',
      required: false,
      defaultValue: 'true',
    })

    expect(parsed.groups[0]).toEqual({
      id: 'group-a',
      name: 'Group A',
      description: 'Constraint group',
      members: ['threads', 'mode'],
      mutuallyExclusive: true,
      oneIsRequired: true,
      allOrNone: false,
    })
  })

  it('falls back gracefully for sparse input/group definitions', () => {
    const descriptor = JSON.stringify({
      inputs: [
        {
          type: 'unknown-type',
          description: '  ',
          optional: false,
          'default-value': { nested: true },
          'value-choices': ['ok', null, false],
          'value-requires': {
            ' ': ['ignored'],
            yes: ['a', 42, 'b'],
          },
          'value-disables': {
            no: ['x'],
          },
        },
      ],
      groups: [{}],
    })

    const parsed = parseDescriptorForm(descriptor)

    expect(parsed.inputs[0]).toMatchObject({
      id: 'unnamed-input-1',
      name: 'Unnamed input 1',
      type: 'String',
      description: '-',
      required: true,
      defaultValue: '{"nested":true}',
      possibleValues: ['ok', 'false'],
      valueRequires: { yes: ['a', 'b'] },
      valueDisables: { no: ['x'] },
      usesAbsolutePath: false,
    })

    expect(parsed.groups[0]).toEqual({
      id: 'group-1',
      name: 'Group 1',
      description: '',
      members: [],
      mutuallyExclusive: false,
      oneIsRequired: false,
      allOrNone: false,
    })
  })

  it('parseDescriptorInputs returns the same parsed input list', () => {
    const descriptor = JSON.stringify({
      inputs: [{ id: 'a', name: 'A', type: 'String' }],
      groups: [{ id: 'g', name: 'G', members: ['a'] }],
    })

    const form = parseDescriptorForm(descriptor)
    const inputs = parseDescriptorInputs(descriptor)

    expect(inputs).toEqual(form.inputs)
    expect(inputs).toHaveLength(1)
    expect(inputs[0]?.id).toBe('a')
  })
})
