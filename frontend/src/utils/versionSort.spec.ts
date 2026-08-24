import { describe, expect, it } from 'vitest'
import { compareVersionDesc, sortByVersionDesc } from './versionSort'

describe('versionSort utils', () => {
  it('compares valid semver in descending order', () => {
    expect(compareVersionDesc('2.0.0', '1.9.9')).toBeLessThan(0)
    expect(compareVersionDesc('1.0.0', '1.0.0')).toBe(0)
    expect(compareVersionDesc('1.0.0', '1.1.0')).toBeGreaterThan(0)
  })

  it('normalizes shorthand numeric versions before semver compare', () => {
    expect(compareVersionDesc('2', '1.9.9')).toBeLessThan(0)
    expect(compareVersionDesc('1.10', '1.2.0')).toBeLessThan(0)
  })

  it('falls back to localeCompare for non-semver values', () => {
    expect(compareVersionDesc('beta', 'alpha')).toBeLessThan(0)
    expect(compareVersionDesc('alpha', 'beta')).toBeGreaterThan(0)
  })

  it('sorts objects by version descending without mutating source', () => {
    const source = [
      { id: 'a', version: '1.0.0' },
      { id: 'b', version: '2' },
      { id: 'c', version: '1.10' },
      { id: 'd', version: 'beta' },
    ]

    const sorted = sortByVersionDesc(source)

    expect(sorted.map((item) => item.id)).toEqual(['d', 'b', 'c', 'a'])
    expect(source.map((item) => item.id)).toEqual(['a', 'b', 'c', 'd'])
  })
})
