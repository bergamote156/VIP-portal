<script setup lang="ts">
import { computed, onMounted } from 'vue'
import { RouterLink } from 'vue-router'
import { Search } from 'lucide-vue-next'
import AppCard from '@/components/ui/AppCard.vue'
import AppBadge from '@/components/ui/AppBadge.vue'
import { useApplicationsStore } from '@/stores/applications.store'
import { useAppVersionsStore } from '@/stores/appversions.stores'
import type { AppVersion } from '@/types/appversion.types'
import type { Application } from '@/types/application.types'
import { getGroupBadgeColor } from '@/utils/groupColor'
import { sortByVersionDesc } from '@/utils/versionSort'

const applicationsStore = useApplicationsStore()
const appversionsStore = useAppVersionsStore()

type ApplicationWithVersions = Application & {
  versions: AppVersion[]
  latestVersion: AppVersion | null
}

const applicationsWithVersions = computed<ApplicationWithVersions[]>(() => {
  const enrichedApplications = applicationsStore.filteredApplications.map((app) => {
    const appVersions = appversionsStore.appVersions.filter((version) => version.applicationName === app.name)
    const sortedVersions = sortByVersionDesc(appVersions)

    return {
      ...app,
      versions: sortedVersions,
      latestVersion: sortedVersions[0] ?? null,
    }
  })

  // If no version for an app, remove it from the list
  return enrichedApplications.filter((app) => app.versions.length > 0)
})

const isLoading = computed(() => applicationsStore.isLoading || appversionsStore.isLoading)

onMounted(async () => {
  await Promise.all([
    applicationsStore.fetchApplications(),
    appversionsStore.fetchAppVersions(),
  ])
})
</script>

<template>
  <div class="space-y-6">
    <div>
      <h1 class="text-2xl font-bold text-gray-900">
        Applications
      </h1>
      <p class="mt-1 text-sm text-gray-500">
        Explore and launch applications available in the VIP Portal. Use the search to quickly find the application you need, or create a new one if you have the necessary permissions.
      </p>
      <router-link
        to="/applications/create"
        class="mt-3 inline-flex items-center gap-1 text-sm font-medium text-primary-600 hover:text-primary-700"
      >
        <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" class="h-4 w-4">
          <path stroke-linecap="round" stroke-linejoin="round" d="M12 4.5v15m7.5-7.5
M4.5 12h15" />
        </svg>
        Create a new application
      </router-link>
    </div>

    <div class="relative">
      <Search
        class="pointer-events-none absolute left-3 top-1/2 h-5 w-5 -translate-y-1/2 text-gray-400"
      />
      <input
        v-model="applicationsStore.searchQuery"
        type="search"
        placeholder="Search for an application..."
        class="block w-full rounded-lg border border-gray-300 py-2.5 pl-10 pr-4 text-sm placeholder:text-gray-400 focus:border-primary-500 focus:outline-none focus:ring-2 focus:ring-primary-500 focus:ring-offset-0"
      />
    </div>

    <div v-if="isLoading" class="flex justify-center py-16 text-sm text-gray-500">
      Loading applications...
    </div>

    <div
      v-else
      class="grid grid-cols-1 gap-4 md:grid-cols-2 lg:grid-cols-3"
    >
      <div
        v-for="app in applicationsWithVersions"
        :key="app.name"
      >
        <AppCard hoverable padding class="h-full">
          <RouterLink :to="{ name: 'application-detail', params: { name: app.name } }" class="block">
            <div class="flex items-start justify-between gap-2">
              <h3 class="text-lg font-bold text-gray-900">
                {{ app.name }}
              </h3>
              <div class="flex flex-wrap gap-1.5">
                <AppBadge v-for="version in app.versions" :key="version.version" :variant="version === app.latestVersion ? 'primary' : 'gray'">
                  {{ version.version }}
                </AppBadge>
              </div>
            </div>
            <div v-if="app.latestVersion?.tags.length" class="mt-2 flex flex-wrap gap-1.5">
              <AppBadge
                v-for="tag in app.latestVersion.tags"
                :key="`tag-${app.name}-${tag}`"
                :variant="getGroupBadgeColor(tag.key)"
              >
                {{ tag.key }}
              </AppBadge>
            </div>
            <p v-if="app.latestVersion?.parsedDescriptor?.description" class="mt-3 line-clamp-2 text-xs text-gray-500">
              {{ app.latestVersion.parsedDescriptor.description }}
            </p>
            <p v-if="app.note" class="mt-3 text-xs text-gray-500">
              {{ app.note }}
            </p>
            <p class="mt-2 text-sm font-medium text-primary-600 hover:text-primary-700">
              View details &rarr;
            </p>
          </RouterLink>
        </AppCard>
      </div>
    </div>

    <p
      v-if="!isLoading && applicationsWithVersions.length === 0"
      class="py-12 text-center text-gray-500"
    >
      No applications found. Try adjusting your search or
      <RouterLink to="/applications/create" class="font-medium text-primary-600 hover:text-primary-700">
        create a new one.
      </RouterLink>
    </p>
  </div>
</template>
