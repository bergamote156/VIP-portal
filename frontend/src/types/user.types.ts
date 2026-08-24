export interface Group {
  name: string
  publicGroup: boolean
  type: 'RESOURCE' | 'APPLICATION' | string
  auto: boolean
}

export interface GroupWithRole extends Group {
  role: string
}

export interface UserSuggestion {
  id: string
  email: string
  firstName?: string
  lastName?: string

  confirmed?: boolean
  accountLocked?: boolean
  institution?: string
  registration?: number
  lastLogin?: number
  level?: string
  maxRunningSimulations?: number
  countryCode?: string
  termsOfUse?: number
  lastUpdatePublications?: number
  groups?: Group[]
  groupsWithRoles?: GroupWithRole[]
}
