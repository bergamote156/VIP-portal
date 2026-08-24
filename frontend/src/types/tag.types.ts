
export type TagValueType = 'STRING' | 'BOOLEAN'

export interface Tag {
  key: string
  value: string
  type: TagValueType
  application: string
  version: string
  visible: boolean
  boutiques: boolean
}
