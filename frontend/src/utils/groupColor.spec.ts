import { describe, expect, it } from 'vitest'
import { GROUP_BADGE_COLORS, getGroupBadgeColor } from './groupColor'

describe('getGroupBadgeColor', () => {
  it('returns a stable color from the allowed palette', () => {
    const colorA = getGroupBadgeColor('Neuro')
    const colorB = getGroupBadgeColor('Neuro')

    expect(colorA).toBe(colorB)
    expect(GROUP_BADGE_COLORS).toContain(colorA)
  })

  it('supports empty values without throwing', () => {
    expect(() => getGroupBadgeColor('')).not.toThrow()
    expect(GROUP_BADGE_COLORS).toContain(getGroupBadgeColor(''))
  })
})
