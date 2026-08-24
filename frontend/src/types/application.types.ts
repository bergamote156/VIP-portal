export interface VipGroup {
  name: string
  publicGroup: boolean
  type: string
  auto: boolean
}

export interface Application {
  name: string
  fullName: string | null
  citation: string | null
  owner: string | null
  groups: VipGroup[]
  note: string | null
}

export interface PrecisePage<T> {
  data: T[]
  total: number
}

export interface ApplicationImportPayload {
  jsonFile?: File
  executionResource?: string
  existingTag?: string
  customTag?: string
}
