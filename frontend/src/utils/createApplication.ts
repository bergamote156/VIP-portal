import type { Group } from '@/types/group.types'
import type { Resource } from '@/types/resource.types'
import type { Tag } from '@/types/tag.types'

export type ParsedDescriptorIdentity = {
  name: string
  version: string
}

export type ResourceInput = {
  name: string
  configuration: string
  status: boolean
}

export type ResourceSelectionItem = Resource & {
  selectable: boolean
  disabledReason: string
}

export type GroupSelectionItem = Group & {
  selectable: boolean
  disabledReason: string
}

export function parseBoutiquesIdentity(raw: string): ParsedDescriptorIdentity {
  const parsed = JSON.parse(raw) as Record<string, unknown>
  const name = typeof parsed.name === 'string' ? parsed.name.trim() : ''
  const version =
    typeof parsed['tool-version'] === 'string'
      ? parsed['tool-version'].trim()
      : typeof parsed.version === 'string'
        ? parsed.version.trim()
        : ''

  return { name, version }
}

export function buildGroupSelectionItems(groups: Group[]): GroupSelectionItem[] {
  return [...groups]
    .sort((a, b) => a.name.localeCompare(b.name, undefined, { sensitivity: 'base' }))
    .map((group) => {
      const isApplicationGroup = group.type === 'APPLICATION'
      const selectable = isApplicationGroup && !group.auto

      const disabledReason = !isApplicationGroup
        ? 'Not an application group'
        : group.auto
          ? 'Automatic group'
          : ''

      return {
        ...group,
        selectable,
        disabledReason,
      }
    })
}

export function filterSelectableGroupNames(groups: Group[], selectedGroupNames: string[]): string[] {
  const selectableGroupNames = new Set(
    groups.filter((group) => group.type === 'APPLICATION' && !group.auto).map((group) => group.name),
  )

  return selectedGroupNames.filter((name) => selectableGroupNames.has(name))
}

export function toggleItemSelection(items: string[], name: string, checked: boolean): string[] {
  if (checked) {
    return items.includes(name) ? items : [...items, name]
  }

  return items.filter((item) => item !== name)
}

export function addCustomTag(
  availableTags: string[],
  selectedTags: string[],
  customTagInput: string,
): { availableTags: string[]; selectedTags: string[]; normalizedInput: string } {
  const tag = customTagInput.trim()
  if (!tag) {
    return { availableTags, selectedTags, normalizedInput: '' }
  }

  return {
    availableTags: availableTags.includes(tag) ? availableTags : [...availableTags, tag],
    selectedTags: selectedTags.includes(tag) ? selectedTags : [...selectedTags, tag],
    normalizedInput: '',
  }
}

export function normalizeResources(resources: ResourceInput[]): ResourceInput[] {
  return resources
    .map((resource) => ({
      name: resource.name.trim(),
      configuration: resource.configuration.trim(),
      status: resource.status,
    }))
    .filter((resource) => resource.name.length > 0)
}

export function buildResourceSelectionItems(resources: Resource[]): ResourceSelectionItem[] {
  return [...resources]
    .sort((a, b) => a.name.localeCompare(b.name, undefined, { sensitivity: 'base' }))
    .map((resource) => {
      const selectable = resource.status

      return {
        ...resource,
        selectable,
        disabledReason: selectable ? '' : 'Inactive resource',
      }
    })
}

export function toAppVersionResourcesPayload(
  resources: Resource[],
  selectedResourceNames: string[],
): Array<Pick<Resource, 'name' | 'status' | 'configuration'>> {
  const selectableResourcesByName = new Map(
    resources.filter((resource) => resource.status).map((resource) => [resource.name, resource]),
  )

  return selectedResourceNames
    .map((name) => selectableResourcesByName.get(name))
    .filter((resource): resource is Resource => Boolean(resource))
    .map((resource) => ({
      name: resource.name,
      status: resource.status,
      configuration: resource.configuration,
    }))
}

export function toAppVersionTagsPayload(
  selectedTagKeys: string[],
  availableTags: Tag[],
  applicationName: string,
  version: string,
): Tag[] {
  const firstTagByKey = new Map<string, Tag>()
  for (const tag of availableTags) {
    if (!firstTagByKey.has(tag.key)) {
      firstTagByKey.set(tag.key, tag)
    }
  }

  return selectedTagKeys.map((key) => {
    const existing = firstTagByKey.get(key)

    if (existing) {
      return {
        ...existing,
        application: applicationName,
        version,
      }
    }

    // For custom tags created from the UI, use a deterministic default value/type.
    return {
      key,
      value: 'true',
      type: 'BOOLEAN',
      application: applicationName,
      version,
      visible: true,
      boutiques: false,
    }
  })
}

export function toApplicationGroupsPayload(groupNames: string[]): Group[] {
  return groupNames.map((name) => ({
    name,
    publicGroup: false,
    type: 'APPLICATION',
    auto: false,
  }))
}
