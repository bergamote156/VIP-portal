<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { RouterLink } from 'vue-router'
import { Search } from 'lucide-vue-next'
import AppBadge from '@/components/ui/AppBadge.vue'
import AppCard from '@/components/ui/AppCard.vue'
import { useWorkflowsStore } from '@/stores/workflows.store'
import type { WorkflowStatus, WorkflowListParams } from '@/types/workflow.types'
import { useFormatters } from '@/composables/useFormatters'

const workflowsStore = useWorkflowsStore()
const { formatRelativeTime } = useFormatters()

const searchQuery = ref('')
const statusFilter = ref('')
const startDateFilter = ref('')
const endDateFilter = ref('')

const statusColors = {
  Running: 'primary',
  Completed: 'success',
  Failed: 'danger',
  Killed: 'warning',
  Cleaned: 'gray',
  Queued: 'info',
  Unknown: 'gray',
} as const

const statusList: WorkflowStatus[] = [
  'Running', 'Queued', 'Completed', 'Failed', 'Killed', 'Cleaned', 'Unknown',
]

const page = ref(0)
const pageSize = 20

function buildParams(): WorkflowListParams {
  const params: WorkflowListParams = {
    offset: page.value * pageSize,
    quantity: pageSize,
  }
  if (searchQuery.value) params.search = searchQuery.value
  if (statusFilter.value) params.status = statusFilter.value
  if (startDateFilter.value) params.startDate = startDateFilter.value
  if (endDateFilter.value) params.endDate = endDateFilter.value
  return params
}

async function loadWorkflows() {
  page.value = 0
  await workflowsStore.listWorkflows(buildParams())
}

function onPage(delta: number) {
  page.value = Math.max(0, page.value + delta)
  workflowsStore.listWorkflows(buildParams())
}

onMounted(loadWorkflows)
</script>

<template>
  <div class="space-y-6">
    <div>
      <h1 class="text-2xl font-bold text-gray-900">Executions</h1>
      <p class="mt-1 text-sm text-gray-500">
        Browse and manage your workflow executions.
      </p>
    </div>

    <AppCard padding class="space-y-4">
      <div class="relative">
        <Search class="pointer-events-none absolute left-3 top-1/2 h-5 w-5 -translate-y-1/2 text-gray-400" />
        <input
          v-model="searchQuery"
          type="search"
          placeholder="Search by execution name, application or ID..."
          class="block w-full rounded-lg border border-gray-300 py-2.5 pl-10 pr-4 text-sm placeholder:text-gray-400 focus:border-primary-500 focus:outline-none focus:ring-2 focus:ring-primary-500 focus:ring-offset-0"
          @input="loadWorkflows"
        />
      </div>
      <div class="flex flex-wrap items-end gap-3">
        <div>
          <label class="block text-xs font-medium text-gray-600">Status</label>
          <select
            v-model="statusFilter"
            class="mt-1 block rounded-lg border border-gray-300 px-3 py-2 text-sm focus:border-primary-500 focus:outline-none focus:ring-2 focus:ring-primary-500"
            @change="loadWorkflows"
          >
            <option value="">All</option>
            <option v-for="s in statusList" :key="s" :value="s">{{ s }}</option>
          </select>
        </div>
        <div>
          <label class="block text-xs font-medium text-gray-600">From</label>
          <input
            v-model="startDateFilter"
            type="date"
            class="mt-1 block rounded-lg border border-gray-300 px-3 py-2 text-sm focus:border-primary-500 focus:outline-none focus:ring-2 focus:ring-primary-500"
            @change="loadWorkflows"
          />
        </div>
        <div>
          <label class="block text-xs font-medium text-gray-600">To</label>
          <input
            v-model="endDateFilter"
            type="date"
            class="mt-1 block rounded-lg border border-gray-300 px-3 py-2 text-sm focus:border-primary-500 focus:outline-none focus:ring-2 focus:ring-primary-500"
            @change="loadWorkflows"
          />
        </div>
      </div>
    </AppCard>

    <AppCard :padding="false">
      <div v-if="workflowsStore.isLoading" class="flex justify-center py-16 text-sm text-gray-500">
        Loading executions...
      </div>

      <div v-else-if="workflowsStore.workflows.length === 0" class="py-16 text-center text-sm text-gray-500">
        No executions found.
      </div>

      <div v-else class="overflow-x-auto">
        <table class="min-w-full divide-y divide-gray-200 text-sm">
          <thead class="bg-gray-50">
            <tr>
              <th class="px-4 py-3 text-left font-semibold text-gray-700">ID</th>
              <th class="px-4 py-3 text-left font-semibold text-gray-700">Name</th>
              <th class="px-4 py-3 text-left font-semibold text-gray-700">Application</th>
              <th class="px-4 py-3 text-left font-semibold text-gray-700">Status</th>
              <th class="px-4 py-3 text-left font-semibold text-gray-700">User</th>
              <th class="px-4 py-3 text-left font-semibold text-gray-700">Started</th>
            </tr>
          </thead>
          <tbody class="divide-y divide-gray-100">
            <tr
              v-for="wf in workflowsStore.workflows"
              :key="wf.id"
              class="transition hover:bg-gray-50"
            >
              <td class="px-4 py-3">
                <RouterLink
                  :to="{ name: 'workflow-detail', params: { id: wf.id } }"
                  class="font-mono text-s text-primary-600 hover:text-primary-700"
                >
                  {{ wf.id }}
                </RouterLink>
              </td>
              <td class="px-4 py-3 font-medium text-gray-900">
                {{ wf.workflowName }}
              </td>
              <td class="px-4 py-3 text-gray-600">
                {{ wf.applicationName }}
                <span class="text-gray-400">v{{ wf.applicationVersion }}</span>
              </td>
              <td class="px-4 py-3">
                <AppBadge :variant="statusColors[wf.status] || 'gray'">
                  {{ wf.status }}
                </AppBadge>
              </td>
              <td class="px-4 py-3 text-gray-600">
                {{ wf.userFullName || wf.userId }}
              </td>
              <td class="px-4 py-3 text-gray-500">
                {{ formatRelativeTime(wf.startDate) }}
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </AppCard>

    <div class="flex items-center justify-between gap-4 text-sm text-gray-600">
        <span>Total: {{ workflowsStore.total }} execution(s)</span>
        <div class="flex items-center gap-2">
          <button
            :disabled="page === 0"
            class="rounded-lg border border-gray-300 px-3 py-1.5 disabled:opacity-40"
            @click="onPage(-1)"
          >
            Previous
          </button>
          <span class="font-medium">Page {{ page + 1 }}</span>
          <button
            :disabled="(page + 1) * pageSize >= workflowsStore.total"
            class="rounded-lg border border-gray-300 px-3 py-1.5 disabled:opacity-40"
            @click="onPage(1)"
          >
            Next
          </button>
        </div>
      </div>
  </div>
</template>
