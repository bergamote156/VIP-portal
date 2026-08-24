import { ref } from 'vue'
import { defineStore } from 'pinia'
import { workflowsApi } from '@/api/workflows.api'
import { useNotificationsStore } from '@/stores/notifications.store'
import type { Workflow, WorkflowLaunchPayload, WorkflowListParams } from '@/types/workflow.types'

export const useWorkflowsStore = defineStore('workflows', () => {
  const currentWorkflow = ref<Workflow | null>(null)
  const workflows = ref<Workflow[]>([])
  const isLoading = ref(false)
  const total = ref(0)
  const notifications = useNotificationsStore()

  async function fetchWorkflow(wid: string) {
    isLoading.value = true
    try {
      currentWorkflow.value = await workflowsApi.get(wid)
    } catch {
      notifications.error('Unable to load workflow.')
    } finally {
      isLoading.value = false
    }
  }

  async function listWorkflows(params?: WorkflowListParams) {
    isLoading.value = true
    try {
      const page = await workflowsApi.list(params)
      workflows.value = page?.data ?? []
      total.value = page?.total ?? 0
    } catch {
      workflows.value = []
      total.value = 0
      notifications.error('Unable to load workflows.')
    } finally {
      isLoading.value = false
    }
  }

  async function launchWorkflow(payload: WorkflowLaunchPayload): Promise<Workflow | null> {
    try {
      const workflow = await workflowsApi.launch(payload)
      notifications.success('Workflow launched.')
      return workflow
    } catch {
      notifications.error('Unable to launch workflow.')
      return null
    }
  }

  async function killWorkflow(wid: string): Promise<boolean> {
    try {
      await workflowsApi.kill(wid)
      notifications.success('Workflow killed.')
      if (currentWorkflow.value?.id === wid) {
        currentWorkflow.value = await workflowsApi.get(wid)
      }
      return true
    } catch {
      notifications.error('Unable to kill workflow.')
      return false
    }
  }

  async function cleanWorkflow(wid: string, deleteFiles = true): Promise<boolean> {
    try {
      await workflowsApi.clean(wid, deleteFiles)
      notifications.success('Workflow cleaned.')
      if (currentWorkflow.value?.id === wid) {
        currentWorkflow.value = await workflowsApi.get(wid)
      }
      return true
    } catch {
      notifications.error('Unable to clean workflow.')
      return false
    }
  }

  return {
    currentWorkflow,
    workflows,
    isLoading,
    total,
    fetchWorkflow,
    listWorkflows,
    launchWorkflow,
    killWorkflow,
    cleanWorkflow,
  }
})
