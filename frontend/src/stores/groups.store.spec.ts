import { beforeEach, describe, expect, it, vi } from 'vitest'
import { createPinia, setActivePinia } from 'pinia'
import { useGroupsStore } from './groups.store'

const mocked = vi.hoisted(() => ({
  getAll: vi.fn(),
}))

vi.mock('@/api/groups.api', () => ({
  groupsApi: {
    getAll: mocked.getAll,
  },
}))

describe('useGroupsStore', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()
  })

  it('fetches groups and exposes application groups', async () => {
    mocked.getAll.mockResolvedValue({
      data: [
        { name: 'admins', publicGroup: false, type: 'APPLICATION', auto: false },
        { name: 'resource-team', publicGroup: false, type: 'RESOURCE', auto: false },
      ],
      total: 2,
    })

    const store = useGroupsStore()
    const pending = store.fetchGroups()

    expect(store.isLoading).toBe(true)
    await pending

    expect(store.isLoading).toBe(false)
    expect(store.totalCount).toBe(2)
    expect(store.groups).toHaveLength(2)
    expect(store.groups.map((group) => group.name)).toEqual(['admins', 'resource-team'])
  })
})
