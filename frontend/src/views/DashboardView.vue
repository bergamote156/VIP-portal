<script setup lang="ts">
import { onMounted, computed, ref } from 'vue'
import { RouterLink } from 'vue-router'
import { Rocket, Folder, Play, Mail, MessageSquare, Loader2, CheckCircle, XCircle, ChevronDown, ChevronUp } from 'lucide-vue-next'
import AppCard from '@/components/ui/AppCard.vue'
import AppBadge from '@/components/ui/AppBadge.vue'
import { useAuthStore } from '@/stores/auth.store'
import { useNotificationsStore } from '@/stores/notifications.store'
import { useFormatters } from '@/composables/useFormatters'
import { getRecentApplications } from '@/utils/recentApplications'

const INITIAL_DISPLAY = 5

const auth = useAuthStore()
const notificationsStore = useNotificationsStore()
const { formatRelativeTime } = useFormatters()
const showAll = ref(false)

const displayName = computed(() => {
  const { firstName, lastName, email } = auth.user ?? {}
  if (firstName && lastName) return `${firstName} ${lastName}`
  return email ?? ''
})

const recentApplications = getRecentApplications(4)

const showWelcome = computed(() =>
  auth.isAuthenticated && auth.user != null && auth.user.welcomeDismissed == null,
)

const displayedNotifications = computed(() =>
  showAll.value
    ? notificationsStore.dashboardNotifications
    : notificationsStore.dashboardNotifications.slice(0, INITIAL_DISPLAY),
)

const hasMore = computed(() =>
  notificationsStore.dashboardNotifications.length > INITIAL_DISPLAY,
)

function notificationIcon(type: string) {
  switch (type) {
    case 'message': return MessageSquare
    case 'execution_running': return Loader2
    case 'execution_completed': return CheckCircle
    case 'execution_failed': return XCircle
    default: return MessageSquare
  }
}

function notificationIconClass(type: string): string {
  switch (type) {
    case 'message': return 'bg-blue-50 text-blue-600'
    case 'execution_running': return 'bg-amber-50 text-amber-600'
    case 'execution_completed': return 'bg-emerald-50 text-emerald-600'
    case 'execution_failed': return 'bg-red-50 text-red-600'
    default: return 'bg-gray-50 text-gray-600'
  }
}

const quickActions = [
  {
    label: 'Launch an application',
    description: 'Start a new execution from available applications',
    route: { name: 'applications' },
    icon: Rocket,
  },
  {
    label: 'Manage files',
    description: 'Browse and manage your data files',
    route: { name: 'files' },
    icon: Folder,
  },
  {
    label: 'My executions',
    description: 'View and manage your workflow executions',
    route: { name: 'executions' },
    icon: Play,
  },
  {
    label: 'Messages',
    description: 'Read your mailbox and send messages',
    route: { name: 'messages' },
    icon: Mail,
  },
]

onMounted(() => {
  notificationsStore.fetchDashboardNotifications()
})
</script>

<template>
  <div class="space-y-8">
    <div>
      <h1 class="text-2xl font-bold">
        Welcome, {{ displayName }}
      </h1>
      <p>Welcome to your VIP Portal dashboard!</p>
    </div>

    <section class="space-y-3">
      <h2 class="text-lg font-semibold text-gray-900">
        Recently used applications
      </h2>

      <p v-if="recentApplications.length === 0" class="text-sm text-gray-500">
        No recent application usage yet. Launch an application to see it here.
      </p>

      <div v-else class="grid grid-cols-1 gap-4 md:grid-cols-4">
        <AppCard
          v-for="app in recentApplications"
          :key="app.name"
          hoverable
        >
          <RouterLink
            :to="{
              name: 'application-launch',
              params: { name: app.name, version: app.lastVersion || undefined },
            }"
            class="block"
          >
            <div class="flex items-center justify-between gap-2">
              <h3 class="text-base font-semibold text-gray-900">
                {{ app.fullName || app.name }}
              </h3>
              <AppBadge v-if="app.lastVersion" variant="gray">
                {{ app.lastVersion }}
              </AppBadge>
            </div>
            <p class="mt-2 text-xs text-gray-500">
              Last used: {{ new Date(app.usedAt).toLocaleString() }}
            </p>
          </RouterLink>
        </AppCard>
      </div>
    </section>

    <div class="grid gap-6 lg:grid-cols-3">
      <section class="space-y-3 lg:col-span-2">
        <div class="flex items-center justify-between">
          <h2 class="text-lg font-semibold text-gray-900">Notifications</h2>
          <RouterLink
            to="/messages"
            class="text-sm font-medium text-primary-600 hover:text-primary-700"
          >
            View all messages
          </RouterLink>
        </div>
        <AppCard padding>
          <div v-if="notificationsStore.isLoadingDashboard" class="flex justify-center py-8 text-sm text-gray-500">
            <Loader2 class="mr-2 h-4 w-4 animate-spin" />
            Loading notifications...
          </div>
          <div v-else-if="notificationsStore.dashboardNotifications.length === 0" class="py-8 text-center text-sm text-gray-500">
            No notifications yet.
          </div>
          <div v-else class="-mx-4 divide-y divide-gray-100">
            <RouterLink
              v-for="n in displayedNotifications"
              :key="n.id"
              :to="n.link || '#'"
              class="flex items-start gap-3 px-4 py-3 transition hover:bg-gray-50"
              @click="notificationsStore.markAsRead(n.id)"
            >
              <div
                :class="[
                  'flex h-9 w-9 shrink-0 items-center justify-center rounded-lg',
                  notificationIconClass(n.type),
                ]"
              >
                <component
                  :is="notificationIcon(n.type)"
                  :class="[
                    'h-5 w-5',
                    n.type === 'execution_running' && 'animate-spin',
                  ]"
                />
              </div>
              <div class="min-w-0 flex-1">
                <div class="flex items-center justify-between gap-2">
                  <p
                    :class="[
                      'truncate text-sm',
                      n.read ? 'text-gray-600' : 'font-semibold text-gray-900',
                    ]"
                  >
                    {{ n.title }}
                  </p>
                  <div class="flex shrink-0 items-center gap-2">
                    <span v-if="!n.read" class="h-2 w-2 rounded-full bg-primary-500" />
                    <span class="text-xs text-gray-400">{{ formatRelativeTime(n.date) }}</span>
                  </div>
                </div>
                <p class="mt-0.5 truncate text-xs text-gray-500">
                  {{ n.description }}
                </p>
              </div>
            </RouterLink>
          </div>
          <div v-if="hasMore" class="border-t border-gray-100 pt-2 text-center">
            <button
              class="inline-flex items-center gap-1 text-sm font-medium text-primary-600 hover:text-primary-700"
              @click="showAll = !showAll"
            >
              <template v-if="showAll">
                Show less
                <ChevronUp class="h-4 w-4" />
              </template>
              <template v-else>
                Show more ({{ notificationsStore.dashboardNotifications.length - INITIAL_DISPLAY }} more)
                <ChevronDown class="h-4 w-4" />
              </template>
            </button>
          </div>
        </AppCard>
      </section>

      <section class="space-y-3">
        <h2 class="text-lg font-semibold text-gray-900">Quick actions</h2>
        <div class="space-y-3">
          <AppCard
            v-for="action in quickActions"
            :key="action.label"
            hoverable
            padding
            class="transition hover:border-primary-200"
          >
            <RouterLink :to="action.route" class="flex items-start gap-3">
              <div class="flex h-9 w-9 shrink-0 items-center justify-center rounded-lg bg-primary-50 text-primary-600">
                <component :is="action.icon" class="h-5 w-5" />
              </div>
              <div class="min-w-0">
                <p class="text-sm font-medium text-gray-900">{{ action.label }}</p>
                <p class="mt-0.5 text-xs text-gray-500">{{ action.description }}</p>
              </div>
            </RouterLink>
          </AppCard>
        </div>
      </section>
    </div>
  </div>
</template>
