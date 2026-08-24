import { ref, computed } from 'vue'
import { defineStore } from 'pinia'
import { applicationsApi, type BackendApplication } from '@/api/applications.api'
import type { Application } from '@/types/application.types'

function toApplication(backend: BackendApplication): Application {
  return {
    name: backend.name,
    fullName: backend.fullName,
    citation: backend.citation,
    owner: backend.owner,
    groups: backend.groups ?? [],
    note: backend.note,
  }
}

export const useApplicationsStore = defineStore('applications', () => {
  const applications = ref<Application[]>([])
  const totalCount = ref(0)
  const isLoading = ref(false)
  const searchQuery = ref('')

  const filteredApplications = computed(() => {
    if (!searchQuery.value) return applications.value
    const q = searchQuery.value.toLowerCase()
    return applications.value.filter(
      (app) =>
        app.name.toLowerCase().includes(q) ||
        (app.note ?? '').toLowerCase().includes(q) ||
        app.groups.some((g) => g.name.toLowerCase().includes(q)),
    )
  })

  const allGroups = computed(() => {
    const names = new Set<string>()
    applications.value.forEach((app) => app.groups.forEach((g) => names.add(g.name)))
    return Array.from(names).sort()
  })

  async function fetchApplications() {
    isLoading.value = true
    try {
      const page = await applicationsApi.getAll(0, 50)
      applications.value = page.data.map(toApplication)
      totalCount.value = page.total
    } finally {
      isLoading.value = false
    }
  }

  async function getApplication(name: string): Promise<Application> {
    const cached = applications.value.find((a) => a.name === name)
    if (cached) return cached

    const backend = await applicationsApi.getById(name)
    return toApplication(backend)
  }

  return {
    applications,
    totalCount,
    isLoading,
    searchQuery,
    filteredApplications,
    allGroups,
    fetchApplications,
    getApplication,
  }
})
