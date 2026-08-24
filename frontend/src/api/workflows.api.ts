import { backendClient } from './client'
import type { PrecisePage } from '@/types/application.types'
import type { Workflow, WorkflowLaunchPayload, WorkflowListParams } from '@/types/workflow.types'

export const workflowsApi = {
  list: (params?: WorkflowListParams) =>
    backendClient
      .get<PrecisePage<Workflow>>('/internal/workflows', { params })
      .then((r) => r.data),

  get: (wid: string) =>
    backendClient
      .get<Workflow>(`/internal/workflows/${encodeURIComponent(wid)}`)
      .then((r) => r.data),

  launch: (payload: WorkflowLaunchPayload) =>
    backendClient
      .post<Workflow>('/internal/workflows', payload)
      .then((r) => r.data),

  kill: (wid: string) =>
    backendClient
      .post(`/internal/workflows/${encodeURIComponent(wid)}/kill`)
      .then((r) => r.data),

  clean: (wid: string, deleteFiles = true) =>
    backendClient
      .post(`/internal/workflows/${encodeURIComponent(wid)}/clean`, null, {
        params: { deleteFiles },
      })
      .then((r) => r.data),
}
