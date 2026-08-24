import { beforeEach, describe, expect, it, vi } from 'vitest'
import { createPinia, setActivePinia } from 'pinia'
import { useApplicationsStore } from './applications.store'

const mocked = vi.hoisted(() => ({
  getAll: vi.fn(),
  getById: vi.fn(),
}))

vi.mock('@/api/applications.api', () => ({
  applicationsApi: {
    getAll: mocked.getAll,
    getById: mocked.getById,
  },
}))

describe('useApplicationsStore', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()
  })

  it('fetches applications and updates loading + total', async () => {
    mocked.getAll.mockResolvedValue({
      data: [
        {
          name: 'app-a',
          fullName: 'Application A',
          citation: null,
          owner: null,
          groups: [{ name: 'Neuro', publicGroup: true, type: 'team', auto: false }],
          note: 'First app',
        },
        {
          name: 'app-b',
          fullName: 'Application B',
          citation: null,
          owner: null,
          groups: undefined,
          note: null,
        },
      ],
      total: 2,
    })

    const store = useApplicationsStore()
    const pending = store.fetchApplications()

    expect(store.isLoading).toBe(true)
    await pending

    expect(mocked.getAll).toHaveBeenCalledWith(0, 50)
    expect(store.isLoading).toBe(false)
    expect(store.totalCount).toBe(2)
    expect(store.applications).toEqual([
      {
        name: 'app-a',
        fullName: 'Application A',
        citation: null,
        owner: null,
        groups: [{ name: 'Neuro', publicGroup: true, type: 'team', auto: false }],
        note: 'First app',
      },
      {
        name: 'app-b',
        fullName: 'Application B',
        citation: null,
        owner: null,
        groups: [],
        note: null,
      },
    ])
  })

  it('filters applications by name, note and group', async () => {
    mocked.getAll.mockResolvedValue({
      data: [
        {
          name: 'freesurfer',
          fullName: 'Freesurfer',
          citation: null,
          owner: null,
          groups: [{ name: 'Neuro', publicGroup: true, type: 'team', auto: false }],
          note: 'Cortical pipeline',
        },
        {
          name: 'fsl',
          fullName: 'FSL',
          citation: null,
          owner: null,
          groups: [{ name: 'Imaging', publicGroup: true, type: 'team', auto: false }],
          note: 'Structural tools',
        },
      ],
      total: 2,
    })

    const store = useApplicationsStore()
    await store.fetchApplications()

    store.searchQuery = 'neuro'
    expect(store.filteredApplications.map((app) => app.name)).toEqual(['freesurfer'])

    store.searchQuery = 'structural'
    expect(store.filteredApplications.map((app) => app.name)).toEqual(['fsl'])

    store.searchQuery = 'free'
    expect(store.filteredApplications.map((app) => app.name)).toEqual(['freesurfer'])
  })

  it('computes allGroups as unique sorted group names', async () => {
    mocked.getAll.mockResolvedValue({
      data: [
        {
          name: 'app-1',
          fullName: 'App 1',
          citation: null,
          owner: null,
          groups: [
            { name: 'Zeta', publicGroup: true, type: 'team', auto: false },
            { name: 'Alpha', publicGroup: true, type: 'team', auto: false },
          ],
          note: null,
        },
        {
          name: 'app-2',
          fullName: 'App 2',
          citation: null,
          owner: null,
          groups: [{ name: 'Alpha', publicGroup: true, type: 'team', auto: false }],
          note: null,
        },
      ],
      total: 2,
    })

    const store = useApplicationsStore()
    await store.fetchApplications()

    expect(store.allGroups).toEqual(['Alpha', 'Zeta'])
  })

  it('returns cached application without calling API', async () => {
    const store = useApplicationsStore()
    store.applications = [
      {
        name: 'cached-app',
        fullName: 'Cached App',
        citation: null,
        owner: null,
        groups: [],
        note: null,
      },
    ]

    const app = await store.getApplication('cached-app')

    expect(app.fullName).toBe('Cached App')
    expect(mocked.getById).not.toHaveBeenCalled()
  })

  it('fetches application by id when not cached', async () => {
    mocked.getById.mockResolvedValue({
      name: 'remote-app',
      fullName: 'Remote App',
      citation: null,
      owner: null,
      groups: [],
      note: 'From API',
    })

    const store = useApplicationsStore()
    const app = await store.getApplication('remote-app')

    expect(mocked.getById).toHaveBeenCalledWith('remote-app')
    expect(app).toEqual({
      name: 'remote-app',
      fullName: 'Remote App',
      citation: null,
      owner: null,
      groups: [],
      note: 'From API',
    })
  })
})
