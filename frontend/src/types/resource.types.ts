import type { Group } from '@/types/group.types'

export type ResourceType = 'LOCAL' | 'BATCH' | 'KUBERNETES' | 'DIRAC'

export interface Resource {
  name: string
  status: boolean
  type: ResourceType
  configuration: string
  engines: string[]
  groups: Group[]
}
