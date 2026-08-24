import { beforeEach, describe, expect, it, vi } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import ApplicationDetailView from './ApplicationDetailView.vue'

const mocked = vi.hoisted(() => ({
  route: {
    params: {
      name: 'freesurfer',
    },
  },
  getApplication: vi.fn(),
  fetchAppVersionsForApplication: vi.fn(),
  fetchAppVersion: vi.fn(),
}))

vi.mock('vue-router', () => ({
  useRoute: () => mocked.route,
  RouterLink: {
    name: 'RouterLink',
    props: ['to'],
    template: '<a><slot /></a>',
  },
}))

vi.mock('@/stores/applications.store', () => ({
  useApplicationsStore: () => ({
    getApplication: mocked.getApplication,
  }),
}))

vi.mock('@/stores/appversions.stores', () => ({
  useAppVersionsStore: () => ({
    fetchAppVersionsForApplication: mocked.fetchAppVersionsForApplication,
    fetchAppVersion: mocked.fetchAppVersion,
  }),
}))

describe('ApplicationDetailView', () => {
  beforeEach(() => {
    vi.clearAllMocks()

    mocked.getApplication.mockResolvedValue({
      name: 'freesurfer',
      fullName: 'FreeSurfer',
      citation: 'Some citation',
      owner: null,
      groups: [{ name: 'Neuro', publicGroup: true, type: 'TEAM', auto: false }],
      note: 'Main note',
    })

    mocked.fetchAppVersionsForApplication.mockResolvedValue([
      {
        applicationName: 'freesurfer',
        version: '2.0.0',
        descriptor: JSON.stringify({
          inputs: [
            {
              id: 'threads',
              name: 'Threads',
              type: 'Number',
              optional: true,
              description: 'Number of threads',
              'default-value': 4,
            },
          ],
        }),
        parsedDescriptor: { description: 'Latest descriptor description' },
        doi: '10.1234/demo',
        visible: true,
        resources: [],
        tags: [],
        settings: [],
        source: 'https://example.org/source',
        note: null,
      },
      {
        applicationName: 'freesurfer',
        version: '1.0.0',
        descriptor: '{}',
        parsedDescriptor: null,
        doi: null,
        visible: true,
        resources: [],
        tags: [],
        settings: [],
        source: null,
        note: null,
      },
    ])

    mocked.fetchAppVersion.mockResolvedValue({
      applicationName: 'freesurfer',
      version: '2.0.0',
      descriptor: JSON.stringify({
        description: 'Detailed descriptor',
        inputs: [
          {
            id: 'threads',
            name: 'Threads',
            type: 'Number',
            optional: true,
            description: 'Number of threads',
            'default-value': 4,
          },
        ],
      }),
      parsedDescriptor: { description: 'Detailed descriptor' },
      doi: '10.1234/demo',
      visible: true,
      resources: [],
      tags: [],
      settings: [],
      source: 'https://example.org/source',
      note: null,
    })
  })

  it('loads application detail and version data', async () => {
    const wrapper = mount(ApplicationDetailView)
    await flushPromises()

    expect(mocked.getApplication).toHaveBeenCalledWith('freesurfer')
    expect(mocked.fetchAppVersionsForApplication).toHaveBeenCalledWith('freesurfer')
    expect(mocked.fetchAppVersion).toHaveBeenCalledWith('freesurfer', '2.0.0')

    expect(wrapper.text()).toContain('FreeSurfer')
    expect(wrapper.text()).toContain('2.0.0')
    expect(wrapper.text()).toContain('1.0.0')
    expect(wrapper.text()).toContain('Input parameters')
    expect(wrapper.text()).toContain('Threads')
    expect(wrapper.text()).toContain('Launch app')
  })
})
