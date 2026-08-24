import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import { publicationsApi } from '@/api/publications.api'
import type { Publication, PublicationInput } from '@/types/publication.types'

export const usePublicationsStore = defineStore('publications', () => {
  const publications = ref<Publication[]>([])
  const isLoading = ref(false)
  const searchQuery = ref('')

  const filteredPublications = computed(() => {
    const query = searchQuery.value.trim().toLowerCase()
    if (!query) {
      return publications.value
    }

    return publications.value.filter((publication) => {
      const haystack = [
        publication.title,
        publication.authors,
        publication.type,
        publication.typeName,
        publication.vipApplication,
        publication.doi,
      ]
        .filter((value): value is string => Boolean(value))
        .join(' ')
        .toLowerCase()

      return haystack.includes(query)
    })
  })

  async function fetchPublications() {
    isLoading.value = true
    try {
      publications.value = await publicationsApi.getAll()
    } finally {
      isLoading.value = false
    }

    return publications.value
  }

  async function fetchPublicationTypes() {
    return publicationsApi.getTypes()
  }

  async function createPublication(payload: PublicationInput) {
    await publicationsApi.create(payload)
    await fetchPublications()
  }

  async function updatePublication(id: number, payload: PublicationInput) {
    await publicationsApi.update(id, payload)
    await fetchPublications()
  }

  async function deletePublication(id: number) {
    await publicationsApi.remove(id)
    publications.value = publications.value.filter((publication) => publication.id !== id)
  }

  async function importBibtex(bibtex: string) {
    await publicationsApi.importBibtex(bibtex)
    await fetchPublications()
  }

  return {
    publications,
    isLoading,
    searchQuery,
    filteredPublications,
    fetchPublications,
    fetchPublicationTypes,
    createPublication,
    updatePublication,
    deletePublication,
    importBibtex,
  }
})
