import { beforeEach, describe, expect, it, vi } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import type { AppVersion } from '@/types/appversion.types'
import type { Application } from '@/types/application.types'
import ApplicationLaunchView from './ApplicationLaunchView.vue'

const mocked = vi.hoisted(() => {
  return {
    route: {
      params: {
        name: 'demo-app',
        version: '1.0.0',
      },
    },
    getApplication: vi.fn(),
    fetchAppVersion: vi.fn(),
    notificationSuccess: vi.fn(),
    rememberRecentApplication: vi.fn(),
    routerPush: vi.fn(),
    launchWorkflow: vi.fn(),
  }
})

vi.mock('vue-router', () => ({
  useRoute: () => mocked.route,
  useRouter: () => ({ push: mocked.routerPush }),
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
    fetchAppVersion: mocked.fetchAppVersion,
  }),
}))

vi.mock('@/stores/notifications.store', () => ({
  useNotificationsStore: () => ({
    success: mocked.notificationSuccess,
  }),
}))

vi.mock('@/stores/workflows.store', () => ({
  useWorkflowsStore: () => ({
    launchWorkflow: mocked.launchWorkflow,
  }),
}))

vi.mock('@/utils/recentApplications', () => ({
  rememberRecentApplication: mocked.rememberRecentApplication,
}))

vi.mock('@/stores/auth.store', () => ({
  useAuthStore: () => ({
    user: { email: 'demo@vip.local', folder: 'demo_user' },
  }),
}))

vi.mock('@/api/files.api', () => ({
  filesApi: {
    listChildren: vi.fn().mockResolvedValue([]),
    createDirectory: vi.fn().mockResolvedValue(undefined),
    uploadFile: vi.fn().mockResolvedValue(undefined),
  },
}))

function buildApplication(): Application {
  return {
    name: 'demo-app',
    fullName: 'Demo Application',
    citation: null,
    owner: null,
    groups: [],
    note: null,
  }
}

function buildAppVersion(descriptorObject: unknown = { inputs: [], groups: [] }): AppVersion {
  return {
    applicationName: 'demo-app',
    version: '1.0.0',
    descriptor: JSON.stringify(descriptorObject),
    parsedDescriptor: null,
    doi: null,
    visible: true,
    resources: [],
    tags: [],
    settings: [],
    source: null,
    note: null,
  }
}

describe('ApplicationLaunchView', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    mocked.route.params.name = 'demo-app'
    mocked.route.params.version = '1.0.0'

    mocked.getApplication.mockResolvedValue(buildApplication())
    mocked.fetchAppVersion.mockResolvedValue(buildAppVersion())
  })

  it('loads application/version and renders launch form', async () => {
    const wrapper = mount(ApplicationLaunchView)
    await flushPromises()

    expect(mocked.getApplication).toHaveBeenCalledWith('demo-app')
    expect(mocked.fetchAppVersion).toHaveBeenCalledWith('demo-app', '1.0.0')

    expect(wrapper.text()).toContain('Launch Demo Application')
    expect(wrapper.text()).toContain('Execution form')
    expect(wrapper.text()).toContain('1.0.0')
  })

  it('shows an error when version is missing in route params', async () => {
    mocked.route.params.version = ''

    const wrapper = mount(ApplicationLaunchView)
    await flushPromises()

    expect(wrapper.text()).toContain('No version provided in the URL.')
    expect(mocked.fetchAppVersion).not.toHaveBeenCalled()
  })

  it('submits launch and stores payload when form is valid', async () => {
    const setItemSpy = vi.spyOn(window.localStorage.__proto__, 'setItem')
    mocked.launchWorkflow.mockResolvedValue({ id: 'wf-123' })

    const wrapper = mount(ApplicationLaunchView)
    await flushPromises()

    await wrapper.get('input[placeholder="e.g. freesurfer-run-001"]').setValue('run-001')
    await wrapper.get('input[placeholder="e.g. /vip/Home/user/results"]').setValue('/vip/Home/user/results')

    await wrapper.get('form').trigger('submit')
    await flushPromises()

    expect(setItemSpy).toHaveBeenCalledTimes(1)
    const firstSetItemCall = setItemSpy.mock.calls[0]
    if (!firstSetItemCall) {
      throw new Error('Expected localStorage.setItem to be called at least once')
    }

    expect(firstSetItemCall[0]).toBe('vip.lastLaunchPayload')

    const serializedPayload = firstSetItemCall[1]
    expect(typeof serializedPayload).toBe('string')

    const payload = JSON.parse(String(serializedPayload))
    expect(payload).toEqual({
      applicationName: 'demo-app',
      version: '1.0.0',
      executionName: 'run-001',
      resultsDirectory: '/vip/Home/user/results',
      inputs: [],
    })

    expect(mocked.rememberRecentApplication).toHaveBeenCalledWith({
      name: 'demo-app',
      fullName: 'Demo Application',
      version: '1.0.0',
    })

    expect(mocked.routerPush).toHaveBeenCalledWith({
      name: 'workflow-detail',
      params: { id: 'wf-123' },
    })
  })
})
