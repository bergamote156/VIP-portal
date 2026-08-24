import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import { tagsApi } from '@/api/tags.api'
import type { Tag } from '@/types/tag.types'

export const useTagsStore = defineStore('tags', () => {
  const tags = ref<Tag[]>([])
  const totalCount = ref(0)
  const isLoading = ref(false)

  const tagKeys = computed(() => {
    const keys = new Set<string>()
    tags.value.forEach((tag) => {
      if (tag.key) {
        keys.add(tag.key)
      }
    })
    return Array.from(keys).sort((a, b) => a.localeCompare(b))
  })

  async function fetchTags(offset = 0, quantity = 50): Promise<Tag[]> {
    isLoading.value = true
    try {
      const page = await tagsApi.getAll(offset, quantity)
      tags.value = page.data
      totalCount.value = page.total
    } finally {
      isLoading.value = false
    }

    return tags.value
  }

  return {
    tags,
    tagKeys,
    totalCount,
    isLoading,
    fetchTags,
  }
})