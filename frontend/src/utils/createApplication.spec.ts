import { describe, expect, it } from 'vitest'
import {
  addCustomTag,
  buildGroupSelectionItems,
  buildResourceSelectionItems,
  filterSelectableGroupNames,
  normalizeResources,
  parseBoutiquesIdentity,
  toAppVersionTagsPayload,
  toAppVersionResourcesPayload,
  toApplicationGroupsPayload,
  toggleItemSelection,
} from './createApplication'

describe('createApplication utils', () => {
  it('parses boutiques descriptor identity using tool-version fallback', () => {
    const parsedWithToolVersion = parseBoutiquesIdentity(
      JSON.stringify({ name: 'demo-app', 'tool-version': '1.2.3' }),
    )
    expect(parsedWithToolVersion).toEqual({ name: 'demo-app', version: '1.2.3' })

    const parsedWithVersion = parseBoutiquesIdentity(
      JSON.stringify({ name: 'demo-app', version: '2.0.0' }),
    )
    expect(parsedWithVersion).toEqual({ name: 'demo-app', version: '2.0.0' })
  })

  it('builds sorted group selection items with selectability metadata', () => {
    const items = buildGroupSelectionItems([
      { name: 'zeta', publicGroup: false, type: 'APPLICATION', auto: false },
      { name: 'alpha', publicGroup: true, type: 'APPLICATION', auto: true },
      { name: 'beta', publicGroup: false, type: 'RESOURCE', auto: false },
    ])

    expect(items.map((item) => item.name)).toEqual(['alpha', 'beta', 'zeta'])
    expect(items).toHaveLength(3)

    const first = items[0]
    const second = items[1]
    const third = items[2]

    expect(first).toBeDefined()
    expect(second).toBeDefined()
    expect(third).toBeDefined()

    expect(first!.selectable).toBe(false)
    expect(first!.disabledReason).toBe('Automatic group')
    expect(second!.selectable).toBe(false)
    expect(second!.disabledReason).toBe('Not an application group')
    expect(third!.selectable).toBe(true)
    expect(third!.disabledReason).toBe('')
  })

  it('filters selected group names to selectable application groups only', () => {
    const result = filterSelectableGroupNames(
      [
        { name: 'admins', publicGroup: false, type: 'APPLICATION', auto: false },
        { name: 'auto-public', publicGroup: true, type: 'APPLICATION', auto: true },
        { name: 'resource-team', publicGroup: false, type: 'RESOURCE', auto: false },
      ],
      ['admins', 'auto-public', 'resource-team', 'unknown'],
    )

    expect(result).toEqual(['admins'])
  })

  it('adds and toggles selections and custom tags without duplicates', () => {
    expect(toggleItemSelection(['a'], 'b', true)).toEqual(['a', 'b'])
    expect(toggleItemSelection(['a', 'b'], 'b', false)).toEqual(['a'])

    const added = addCustomTag(['stable'], ['stable'], '  neuro  ')
    expect(added.availableTags).toEqual(['stable', 'neuro'])
    expect(added.selectedTags).toEqual(['stable', 'neuro'])
    expect(added.normalizedInput).toBe('')

    const empty = addCustomTag(['stable'], ['stable'], '   ')
    expect(empty.availableTags).toEqual(['stable'])
    expect(empty.selectedTags).toEqual(['stable'])
  })

  it('normalizes resources and builds application group payload', () => {
    expect(
      normalizeResources([
        { name: ' cluster ', configuration: ' queue=short ', status: true },
        { name: '   ', configuration: 'ignored', status: true },
      ]),
    ).toEqual([{ name: 'cluster', configuration: 'queue=short', status: true }])

    expect(toApplicationGroupsPayload(['admins'])).toEqual([
      { name: 'admins', publicGroup: false, type: 'APPLICATION', auto: false },
    ])
  })

  it('builds resource selection items and maps selected resources payload', () => {
    const items = buildResourceSelectionItems([
      {
        name: 'cluster-b',
        status: false,
        type: 'BATCH',
        configuration: 'queue=long',
        engines: [],
        groups: [],
      },
      {
        name: 'cluster-a',
        status: true,
        type: 'BATCH',
        configuration: 'queue=short',
        engines: [],
        groups: [],
      },
    ])

    expect(items.map((item) => item.name)).toEqual(['cluster-a', 'cluster-b'])
    expect(items[0]).toBeDefined()
    expect(items[1]).toBeDefined()
    expect(items[0]!.selectable).toBe(true)
    expect(items[1]!.selectable).toBe(false)
    expect(items[1]!.disabledReason).toBe('Inactive resource')

    expect(
      toAppVersionResourcesPayload(
        [
          {
            name: 'cluster-a',
            status: true,
            type: 'BATCH',
            configuration: 'queue=short',
            engines: [],
            groups: [],
          },
          {
            name: 'cluster-b',
            status: false,
            type: 'BATCH',
            configuration: 'queue=long',
            engines: [],
            groups: [],
          },
        ],
        ['cluster-a', 'cluster-b', 'unknown'],
      ),
    ).toEqual([{ name: 'cluster-a', status: true, configuration: 'queue=short' }])
  })

  it('builds structured tags payload expected by backend AppVersion model', () => {
    expect(
      toAppVersionTagsPayload(
        ['stable', 'custom-tag'],
        [
          {
            key: 'stable',
            value: 'true',
            type: 'BOOLEAN',
            application: 'legacy-app',
            version: '0.1.0',
            visible: true,
            boutiques: false,
          },
        ],
        'demo-app',
        '1.2.3',
      ),
    ).toEqual([
      {
        key: 'stable',
        value: 'true',
        type: 'BOOLEAN',
        application: 'demo-app',
        version: '1.2.3',
        visible: true,
        boutiques: false,
      },
      {
        key: 'custom-tag',
        value: 'true',
        type: 'BOOLEAN',
        application: 'demo-app',
        version: '1.2.3',
        visible: true,
        boutiques: false,
      },
    ])
  })
})
