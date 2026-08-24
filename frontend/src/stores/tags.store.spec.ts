import { beforeEach, describe, expect, it, vi } from 'vitest'
import { createPinia, setActivePinia } from 'pinia'
import { useTagsStore } from './tags.store'

const mocked = vi.hoisted(() => ({
  getAll: vi.fn(),
}))

vi.mock('@/api/tags.api', () => ({
  tagsApi: {
    getAll: mocked.getAll,
  },
}))

describe('useTagsStore', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()
  })

  it('fetches tags and computes unique sorted keys', async () => {
    mocked.getAll.mockResolvedValue({
      data: [
        {
          key: 'stable',
          value: 'true',
          type: 'BOOLEAN',
          application: 'demo-app',
          version: '1.0.0',
          visible: true,
          boutiques: false,
        },
        {
          key: 'domain',
          value: 'neuro',
          type: 'STRING',
          application: 'demo-app',
          version: '1.0.0',
          visible: true,
          boutiques: false,
        },
        {
          key: 'stable',
          value: 'false',
          type: 'BOOLEAN',
          application: 'demo-app',
          version: '2.0.0',
          visible: true,
          boutiques: false,
        },
      ],
      total: 3,
    })

    const store = useTagsStore()
    const result = await store.fetchTags(0, 50)

    expect(mocked.getAll).toHaveBeenCalledWith(0, 50)
    expect(store.totalCount).toBe(3)
    expect(store.tags).toHaveLength(3)
    expect(store.tagKeys).toEqual(['domain', 'stable'])
    expect(result).toEqual(store.tags)
  })
})