import type { Tag } from '@/types/tag.types'

export interface BoutiquesContainerImage {
  image?: string
  type?: string
  index?: string
  'container-opts'?: string[]
  [key: string]: unknown
}

export interface BoutiquesInput {
  id?: string
  name?: string
  type?: string
  description?: string
  optional?: boolean
  list?: boolean
  integer?: boolean
  'value-key'?: string
  'command-line-flag'?: string
  'default-value'?: string | number | boolean | null
  'value-choices'?: Array<string | number | boolean>
  [key: string]: unknown
}

export interface BoutiquesOutputFile {
  id?: string
  name?: string
  description?: string
  optional?: boolean
  'value-key'?: string
  'path-template'?: string
  [key: string]: unknown
}

export interface BoutiquesDescriptor {
  name?: string
  description?: string
  author?: string
  'tool-version'?: string
  version?: string
  'schema-version'?: string
  'command-line'?: string
  'container-image'?: BoutiquesContainerImage
  inputs?: BoutiquesInput[]
  'output-files'?: BoutiquesOutputFile[]
  custom?: Record<string, unknown>
  [key: string]: unknown
}

export interface AppVersion {
    applicationName: string
    version: string
    descriptor: string | null
    parsedDescriptor: BoutiquesDescriptor | null
    doi: string | null
    visible: boolean
    resources: { name: string; status: boolean; configuration: string }[]
    tags: Tag[]
    settings: {}[]
    source: string | null
    note: string | null
}

export interface ApplicationInput {
  name: string
  type: 'File' | 'String' | 'Number' | 'Boolean' | 'List'
  description: string
  required: boolean
  defaultValue?: string
  possibleValues?: string[]
}

export interface PrecisePage<T> {
  data: T[]
  total: number
}