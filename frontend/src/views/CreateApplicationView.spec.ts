import { beforeEach, describe, expect, it, vi } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import { createPinia } from 'pinia'
import CreateApplicationView from './CreateApplicationView.vue'

const mocked = vi.hoisted(() => ({
  push: vi.fn(),
  notifySuccess: vi.fn(),
  tagsGetAll: vi.fn(),
  groupsGetAll: vi.fn(),
  resourcesGetAll: vi.fn(),
  boutiquesCheckDescriptor: vi.fn(),
  applicationsGetById: vi.fn(),
  applicationsCreateOrUpdate: vi.fn(),
  appVersionsExists: vi.fn(),
  appVersionsCreate: vi.fn(),
  appVersionsCreateOrUpdate: vi.fn(),
}))

vi.mock('vue-router', () => ({
  useRouter: () => ({ push: mocked.push }),
}))

vi.mock('@/stores/notifications.store', () => ({
  useNotificationsStore: () => ({
    success: mocked.notifySuccess,
  }),
}))

vi.mock('@/api/tags.api', () => ({
  tagsApi: {
    getAll: mocked.tagsGetAll,
  },
}))

vi.mock('@/api/groups.api', () => ({
  groupsApi: {
    getAll: mocked.groupsGetAll,
  },
}))

vi.mock('@/api/resources.api', () => ({
  resourcesApi: {
    getAll: mocked.resourcesGetAll,
  },
}))

vi.mock('@/api/boutiques.api', () => ({
  boutiquesApi: {
    checkDescriptor: mocked.boutiquesCheckDescriptor,
  },
}))

vi.mock('@/api/applications.api', () => ({
  applicationsApi: {
    getById: mocked.applicationsGetById,
    createOrUpdate: mocked.applicationsCreateOrUpdate,
  },
}))

vi.mock('@/api/appVersions.api', () => ({
  appVersionsApi: {
    exists: mocked.appVersionsExists,
    create: mocked.appVersionsCreate,
    createOrUpdate: mocked.appVersionsCreateOrUpdate,
  },
}))

function createDescriptorFile(content: string): File {
  return new File([content], 'descriptor.json', { type: 'application/json' })
}

async function clickButtonByText(wrapper: ReturnType<typeof mount>, text: string) {
  const button = wrapper
    .findAll('button')
    .find((node: any) => node.text().trim().includes(text))

  expect(button).toBeTruthy()
  await button!.trigger('click')
}

function mountView() {
  return mount(CreateApplicationView, {
    global: {
      plugins: [createPinia()],
    },
  })
}

describe('CreateApplicationView', () => {
  beforeEach(() => {
    vi.clearAllMocks()

    mocked.tagsGetAll.mockResolvedValue({
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
          key: 'neuro',
          value: 'domain-neuro',
          type: 'STRING',
          application: 'demo-app',
          version: '1.0.0',
          visible: true,
          boutiques: false,
        },
      ],
      total: 2,
    })
    mocked.groupsGetAll.mockResolvedValue({
      data: [
        { name: 'admins', publicGroup: false, type: 'APPLICATION', auto: false },
        { name: 'public', publicGroup: true, type: 'APPLICATION', auto: true },
        { name: 'resource-team', publicGroup: false, type: 'RESOURCE', auto: false },
      ],
      total: 3,
    })
    mocked.resourcesGetAll.mockResolvedValue({
      data: [
        { name: 'cluster-a', status: true, type: 'BATCH', configuration: 'queue=short', engines: [], groups: [] },
        { name: 'cluster-b', status: false, type: 'BATCH', configuration: 'queue=long', engines: [], groups: [] },
      ],
      total: 2,
    })
    mocked.boutiquesCheckDescriptor.mockResolvedValue({ valid: true, errors: [] })
    mocked.applicationsGetById.mockRejectedValue(new Error('not found'))
    mocked.applicationsCreateOrUpdate.mockResolvedValue({})
    mocked.appVersionsExists.mockResolvedValue(false)
    mocked.appVersionsCreate.mockResolvedValue({})
    mocked.appVersionsCreateOrUpdate.mockResolvedValue({})
  })

  it('loads available tags on mount', async () => {
    const wrapper = mountView()
    await flushPromises()

    expect(mocked.tagsGetAll).toHaveBeenCalledWith(0, 50)
    expect(mocked.groupsGetAll).toHaveBeenCalledWith(true, false, 0, 50)
    expect(mocked.resourcesGetAll).toHaveBeenCalledWith(0, 50, undefined)
    expect(wrapper.text()).toContain('Create a new application')
  })

  it('shows validation error when checking descriptor without file', async () => {
    const wrapper = mountView()
    await flushPromises()

    await clickButtonByText(wrapper, 'Check descriptor & existence')

    expect(wrapper.text()).toContain('Le fichier Boutiques (.json) est requis.')
    expect(mocked.boutiquesCheckDescriptor).not.toHaveBeenCalled()
  })

  it('completes create flow for new application and version', async () => {
    const wrapper = mountView()
    await flushPromises()

    const descriptor = {
      name: 'demo-app',
      version: '1.2.0',
      inputs: [],
    }
    const file = createDescriptorFile(JSON.stringify(descriptor))

    const fileInput = wrapper.get('input[type="file"]')
    Object.defineProperty(fileInput.element, 'files', {
      value: [file],
      configurable: true,
    })
    await fileInput.trigger('change')

    await clickButtonByText(wrapper, 'Check descriptor & existence')
    await flushPromises()

    expect(mocked.boutiquesCheckDescriptor).toHaveBeenCalled()
    expect(mocked.applicationsGetById).toHaveBeenCalledWith('demo-app')
    expect(mocked.appVersionsExists).toHaveBeenCalledWith('demo-app', '1.2.0')

    expect(wrapper.text()).toContain('Application: demo-app')
    expect(wrapper.text()).toContain('Version: 1.2.0')

    await clickButtonByText(wrapper, 'Next step')
    await flushPromises()

    expect(wrapper.text()).toContain('Valider la création')

    const adminsLabel = wrapper
      .findAll('label')
      .find((node: any) => node.text().includes('admins'))

    expect(adminsLabel).toBeTruthy()

    const adminsCheckbox = adminsLabel!.find('input[type="checkbox"]')

    expect(adminsCheckbox).toBeTruthy()
    await adminsCheckbox.setValue(true)

    const resourceLabel = wrapper
      .findAll('label')
      .find((node: any) => node.text().includes('cluster-a'))

    expect(resourceLabel).toBeTruthy()

    const resourceCheckbox = resourceLabel!.find('input[type="checkbox"]')

    expect(resourceCheckbox).toBeTruthy()
    await resourceCheckbox.setValue(true)

    const tagLabel = wrapper
      .findAll('label')
      .find((node: any) => node.text().includes('stable'))

    expect(tagLabel).toBeTruthy()

    const tagCheckbox = tagLabel!.find('input[type="checkbox"]')

    expect(tagCheckbox).toBeTruthy()
    await tagCheckbox.setValue(true)

    await wrapper.get('form').trigger('submit')
    await flushPromises()

    expect(mocked.applicationsCreateOrUpdate).toHaveBeenCalledWith({
      name: 'demo-app',
      fullName: null,
      citation: null,
      note: null,
      groups: [
        { name: 'admins', publicGroup: false, type: 'APPLICATION', auto: false },
      ],
    })

    expect(mocked.appVersionsCreate).toHaveBeenCalledTimes(1)
    expect(mocked.appVersionsCreate).toHaveBeenCalledWith(
      expect.objectContaining({
        tags: [
          {
            key: 'stable',
            value: 'true',
            type: 'BOOLEAN',
            application: 'demo-app',
            version: '1.2.0',
            visible: true,
            boutiques: false,
          },
        ],
        resources: [
          { name: 'cluster-a', status: true, configuration: 'queue=short' },
        ],
      }),
    )
    expect(mocked.appVersionsCreateOrUpdate).not.toHaveBeenCalled()
    expect(mocked.notifySuccess).toHaveBeenCalledWith('Application / version created successfully.')
    expect(mocked.push).toHaveBeenCalledWith({
      name: 'application-detail',
      params: { name: 'demo-app' },
    })
  })

  it('goes back to step 1 from step 2', async () => {
    const wrapper = mountView()
    await flushPromises()

    const file = createDescriptorFile(JSON.stringify({ name: 'demo-app', version: '1.2.0' }))
    const fileInput = wrapper.get('input[type="file"]')
    Object.defineProperty(fileInput.element, 'files', {
      value: [file],
      configurable: true,
    })
    await fileInput.trigger('change')

    await clickButtonByText(wrapper, 'Check descriptor & existence')
    await flushPromises()

    await clickButtonByText(wrapper, 'Next step')
    await flushPromises()

    expect(wrapper.text()).toContain('Valider la création')

    await clickButtonByText(wrapper, "Retour à l'étape 1")
    await flushPromises()

    expect(wrapper.text()).toContain('Check descriptor & existence')
  })
})
