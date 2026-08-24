import type { Group } from './group.types'
import type { GroupWithRole } from './user.types'

export interface ProfileUser {
  id: string
  firstName: string
  lastName: string
  email: string
  institution: string
  countryCode: string
  maxRunningSimulations: number
  level: string
  termsOfUse: string | null
  lastUpdatePublications: string | null
  welcomeDismissed: string | null
  groups: Group[]
  groupsWithRoles?: GroupWithRole[]
  folder?: string
}

export interface ProfileUpdatePayload {
  id: string
  firstName: string
  lastName: string
  email: string
  institution: string
  countryCode: string
  maxRunningSimulations: number
  level: string
  termsOfUse: string | null
  lastUpdatePublications: string | null
  groups: Group[]
}
