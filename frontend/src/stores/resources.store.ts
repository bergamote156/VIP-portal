import { ref } from 'vue'
import { defineStore } from 'pinia'
import { resourcesApi } from '@/api/resources.api'
import type { Resource } from '@/types/resource.types'

export const useResourcesStore = defineStore('resources', () => {
  const resources = ref<Resource[]>([])
  const totalCount = ref(0)
  const isLoading = ref(false)

  async function fetchResources(offset = 0, quantity = 50, group?: string): Promise<Resource[]> {
    isLoading.value = true
    try {
      const page = await resourcesApi.getAll(offset, quantity, group)
      resources.value = page.data
      totalCount.value = page.total
    } finally {
      isLoading.value = false
    }

    return resources.value
  }

  return {
    resources,
    totalCount,
    isLoading,
    fetchResources,
  }
})
