<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth.store'
import { LogOut, ChevronDown, User, Menu } from 'lucide-vue-next'

const router = useRouter()
const auth = useAuthStore()
const showMenu = ref(false)

const initials = computed(() => {
  const first = auth.user?.firstName?.charAt(0) ?? ''
  const last = auth.user?.lastName?.charAt(0) ?? ''
  return first || last ? `${first}${last}`.toUpperCase() : (auth.user?.email?.charAt(0).toUpperCase() ?? '?')
})

const displayName = computed(() => {
  const { firstName, lastName, email } = auth.user ?? {}
  if (firstName && lastName) return `${firstName} ${lastName}`
  return email ?? ''
})

const emit = defineEmits<{
  'open-sidebar': []
}>()

async function handleLogout() {
  await auth.logout()
  router.push('/login')
}
</script>

<template>
  <header
    class="sticky top-0 z-30 flex h-16 items-center justify-between border-b border-gray-200 bg-white/80 px-4 backdrop-blur-sm sm:px-6"
  >
    <div class="flex items-center gap-3">
      <button
        type="button"
        class="inline-flex items-center justify-center rounded-lg p-2 text-gray-500 hover:bg-gray-100 hover:text-gray-700 lg:hidden"
        @click="emit('open-sidebar')"
      >
        <Menu class="h-5 w-5" />
      </button>
      <h1 class="truncate text-base font-semibold text-gray-900 sm:text-lg">
        {{ ($route.meta.title as string) || 'VIP' }}
      </h1>
    </div>

    <div class="relative">
      <button
        class="flex items-center gap-2 rounded-lg px-2 py-2 text-sm text-gray-600 transition-colors hover:bg-gray-50 sm:px-3"
        @click="showMenu = !showMenu"
      >
        <div
          class="flex h-8 w-8 items-center justify-center rounded-full bg-primary-100 text-sm font-medium text-primary-700"
        >
          {{ initials }}
        </div>
        <span class="hidden text-sm font-medium text-gray-700 sm:block">
          {{ displayName }}
        </span>
        <ChevronDown class="h-4 w-4 text-gray-400" />
      </button>

      <Transition name="dropdown">
        <div
          v-if="showMenu"
          class="absolute right-0 mt-1 w-56 rounded-lg border border-gray-200 bg-white py-1 shadow-lg"
          @mouseleave="showMenu = false"
        >
          <div class="border-b border-gray-100 px-4 py-3">
            <p class="text-sm font-medium text-gray-900">{{ displayName }}</p>
            <p class="truncate text-xs text-gray-500">{{ auth.user?.email }}</p>
          </div>
          <RouterLink
            to="/profile"
            class="flex items-center gap-2 px-4 py-2 text-sm text-gray-700 transition-colors hover:bg-gray-50"
            @click="showMenu = false"
          >
            <User class="h-4 w-4 text-gray-400" />
            Mon profil
          </RouterLink>
          <button
            class="flex w-full items-center gap-2 px-4 py-2 text-sm text-red-600 transition-colors hover:bg-red-50"
            @click="handleLogout"
          >
            <LogOut class="h-4 w-4" />
            Se déconnecter
          </button>
        </div>
      </Transition>
    </div>
  </header>
</template>

<style scoped>
.dropdown-enter-active {
  transition: all 0.15s ease-out;
}
.dropdown-leave-active {
  transition: all 0.1s ease-in;
}
.dropdown-enter-from,
.dropdown-leave-to {
  opacity: 0;
  transform: translateY(-4px);
}
</style>
