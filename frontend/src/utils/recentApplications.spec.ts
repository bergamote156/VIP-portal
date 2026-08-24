import { beforeEach, describe, expect, it, vi } from 'vitest'
import { getRecentApplications, rememberRecentApplication } from './recentApplications'

describe('recentApplications utils', () => {
  beforeEach(() => {
    window.localStorage.clear()
    vi.useFakeTimers()
    vi.setSystemTime(new Date('2026-03-24T10:30:00.000Z'))
  })

  it('returns empty when storage is missing or invalid', () => {
    expect(getRecentApplications()).toEqual([])

    window.localStorage.setItem('vip.recentApplications', 'not-json')
    expect(getRecentApplications()).toEqual([])

    window.localStorage.setItem('vip.recentApplications', JSON.stringify({ nope: true }))
    expect(getRecentApplications()).toEqual([])
  })

  it('stores latest usage, deduplicates by name and keeps lastVersion', () => {
    rememberRecentApplication({ name: 'app-a', fullName: 'App A', version: '1.0.0' })

    vi.setSystemTime(new Date('2026-03-24T10:31:00.000Z'))
    rememberRecentApplication({ name: 'app-b', fullName: 'App B', version: '2.0.0' })

    vi.setSystemTime(new Date('2026-03-24T10:32:00.000Z'))
    rememberRecentApplication({ name: 'app-a', fullName: 'App A', version: '1.1.0' })

    const recent = getRecentApplications(10)

    expect(recent).toHaveLength(2)
    expect(recent[0]).toMatchObject({
      name: 'app-a',
      fullName: 'App A',
      lastVersion: '1.1.0',
    })
    expect(recent[1]).toMatchObject({
      name: 'app-b',
      fullName: 'App B',
      lastVersion: '2.0.0',
    })
  })

  it('sorts by usedAt desc and applies requested limit', () => {
    window.localStorage.setItem(
      'vip.recentApplications',
      JSON.stringify([
        { name: 'older', usedAt: '2026-03-24T09:00:00.000Z' },
        { name: 'newer', usedAt: '2026-03-24T11:00:00.000Z' },
        { name: 'middle', usedAt: '2026-03-24T10:00:00.000Z' },
      ]),
    )

    expect(getRecentApplications(2).map((item) => item.name)).toEqual(['newer', 'middle'])
    expect(getRecentApplications(0)).toEqual([])
  })

  it('keeps only the 20 most recent applications in storage', () => {
    for (let i = 1; i <= 22; i += 1) {
      vi.setSystemTime(new Date(`2026-03-24T10:${String(i).padStart(2, '0')}:00.000Z`))
      rememberRecentApplication({ name: `app-${i}` })
    }

    const storedRaw = window.localStorage.getItem('vip.recentApplications')
    expect(storedRaw).toBeTruthy()

    const stored = JSON.parse(String(storedRaw)) as Array<{ name: string }>
    expect(stored).toHaveLength(20)
    expect(stored[0]?.name).toBe('app-22')
    expect(stored[19]?.name).toBe('app-3')
  })
})
