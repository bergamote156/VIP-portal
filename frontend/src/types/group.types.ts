export interface Group {
  name: string
  publicGroup: boolean
  type: string
  auto: boolean
}

export type GroupType = 'APPLICATION' | 'RESOURCE'