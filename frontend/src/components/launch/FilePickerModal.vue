<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { ChevronRight, File, Folder, FolderPlus, House, LoaderCircle, RefreshCw, Upload } from 'lucide-vue-next'
import { filesApi, type BackendData } from '@/api/files.api'
import { useNotificationsStore } from '@/stores/notifications.store'

const ROOT_PATH = '/vip'

const props = withDefaults(defineProps<{
  mode?: 'file' | 'folder'
}>(), {
  mode: 'file',
})

const emit = defineEmits<{
  select: [path: string]
  close: []
}>()

const currentPath = ref(ROOT_PATH)
const entries = ref<BackendData[]>([])
const isLoading = ref(false)
const errorMessage = ref<string | null>(null)
const selectedEntryPath = ref<string | null>(null)
const notifications = useNotificationsStore()

const breadcrumbs = computed(() => {
  const segments = currentPath.value.split('/').filter(Boolean)
  const crumbs: { label: string; path: string }[] = [{ label: 'vip', path: ROOT_PATH }]
  let path = ''
  for (const segment of segments) {
    path += `/${segment}`
    if (path === ROOT_PATH) continue
    crumbs.push({ label: segment, path })
  }
  return crumbs
})

const canGoUp = computed(() => currentPath.value !== ROOT_PATH)

const sortedEntries = computed(() => {
  const list = props.mode === 'folder'
    ? entries.value.filter(e => e.type === 'folder')
    : entries.value
  return [...list].sort((a, b) => {
    if (a.type !== b.type) return a.type === 'folder' ? -1 : 1
    return (a.name || '').localeCompare(b.name || '')
  })
})

const canConfirm = computed(() => {
  if (props.mode === 'folder') return true
  return selectedEntryPath.value !== null
})

function entryPath(name: string): string {
  const base = currentPath.value.endsWith('/') ? currentPath.value.slice(0, -1) : currentPath.value
  return `${base}/${name}`.replace(/\/{2,}/g, '/')
}

function parentPath(path: string): string {
  if (path === ROOT_PATH) return ROOT_PATH
  const parts = path.split('/').filter(Boolean)
  if (parts.length <= 1) return ROOT_PATH
  return `/${parts.slice(0, -1).join('/')}`
}

async function loadDirectory(path: string = currentPath.value) {
  isLoading.value = true
  errorMessage.value = null
  selectedEntryPath.value = null
  try {
    const data = await filesApi.listChildren(path)
    currentPath.value = path
    entries.value = data
  } catch {
    errorMessage.value = `Unable to load folder ${path}.`
  } finally {
    isLoading.value = false
  }
}

function openEntry(entry: BackendData) {
  if (props.mode === 'folder' && entry.type === 'folder') {
    selectedEntryPath.value = entryPath(entry.name)
    return
  }
  if (entry.type === 'folder') {
    void loadDirectory(entryPath(entry.name))
  } else {
    selectedEntryPath.value = entryPath(entry.name)
  }
}

function onDblClick(entry: BackendData) {
  if (entry.type === 'folder') {
    void loadDirectory(entryPath(entry.name))
  }
}

function goUp() {
  if (!canGoUp.value) return
  void loadDirectory(parentPath(currentPath.value))
}

function confirmSelection() {
  if (props.mode === 'folder') {
    const path = selectedEntryPath.value ?? currentPath.value
    emit('select', path)
    return
  }
  if (selectedEntryPath.value) {
    emit('select', selectedEntryPath.value)
  }
}

// Create directory
const showCreateDir = ref(false)
const newDirName = ref('')
const isCreatingDir = ref(false)

function openCreateDir() {
  newDirName.value = ''
  showCreateDir.value = true
}

async function confirmCreateDir() {
  const name = newDirName.value.trim()
  if (!name || name.includes(' ')) {
    errorMessage.value = name ? "Directory name cannot contain spaces." : "Directory name is required."
    return
  }
  isCreatingDir.value = true
  try {
    await filesApi.createDirectory(currentPath.value, name)
    newDirName.value = ''
    showCreateDir.value = false
    await loadDirectory(currentPath.value)
    notifications.success(`Folder "${name}" created.`)
  } catch {
    errorMessage.value = `Unable to create folder ${name}.`
  } finally {
    isCreatingDir.value = false
  }
}

onMounted(() => {
  void loadDirectory(ROOT_PATH)
})
</script>

<template>
  <div
    class="fixed inset-0 z-50 flex items-center justify-center bg-black/40 px-4"
    @click.self="emit('close')"
  >
    <div class="flex w-full max-w-2xl flex-col rounded-xl border border-gray-200 bg-white shadow-lg">
      <!-- Header -->
      <div class="flex items-center justify-between border-b border-gray-200 px-5 py-4">
        <h2 class="text-lg font-semibold text-gray-900">
          {{ mode === 'folder' ? 'Select a directory' : 'Select a file' }}
        </h2>
        <button
          type="button"
          class="rounded-md px-3 py-1.5 text-sm font-medium text-gray-600 hover:bg-gray-100"
          @click="emit('close')"
        >
          Cancel
        </button>
      </div>

      <!-- Toolbar -->
      <div class="flex flex-wrap items-center gap-2 border-b border-gray-200 px-5 py-2.5">
        <button
          type="button"
          class="inline-flex items-center gap-1 rounded-md border border-gray-200 bg-white px-2.5 py-1.5 text-xs font-medium text-gray-700 hover:bg-gray-50 disabled:cursor-not-allowed disabled:opacity-50"
          :disabled="!canGoUp || isLoading"
          @click="goUp"
        >
          <FolderPlus class="h-3.5 w-3.5 rotate-180" />
          Up
        </button>
        <button
          type="button"
          class="inline-flex items-center gap-1 rounded-md border border-gray-200 bg-white px-2.5 py-1.5 text-xs font-medium text-gray-700 hover:bg-gray-50 disabled:cursor-not-allowed disabled:opacity-50"
          :disabled="isLoading"
          @click="loadDirectory(currentPath)"
        >
          <RefreshCw class="h-3.5 w-3.5" />
          Refresh
        </button>
        <button
          type="button"
          class="inline-flex items-center gap-1 rounded-md border border-gray-200 bg-white px-2.5 py-1.5 text-xs font-medium text-gray-700 hover:bg-gray-50 disabled:cursor-not-allowed disabled:opacity-50"
          :disabled="isLoading || isCreatingDir"
          @click="openCreateDir"
        >
          <FolderPlus class="h-3.5 w-3.5" />
          New folder
        </button>
      </div>

      <!-- Breadcrumbs -->
      <nav class="flex flex-wrap items-center gap-1 border-b border-gray-200 px-5 py-2 text-sm text-gray-600">
        <button
          type="button"
          class="inline-flex items-center gap-1 rounded-md px-2 py-1 hover:bg-gray-100"
          @click="loadDirectory(ROOT_PATH)"
        >
          <House class="h-3.5 w-3.5" />
          /vip
        </button>
        <template v-for="crumb in breadcrumbs.slice(1)" :key="crumb.path">
          <ChevronRight class="h-3.5 w-3.5 text-gray-400" />
          <button
            type="button"
            class="rounded-md px-2 py-1 hover:bg-gray-100"
            @click="loadDirectory(crumb.path)"
          >
            {{ crumb.label }}
          </button>
        </template>
      </nav>

      <!-- Create directory inline -->
      <div v-if="showCreateDir" class="mx-5 mt-3 flex items-center gap-2 rounded-lg border border-primary-200 bg-primary-50 px-3 py-2">
        <input
          v-model="newDirName"
          type="text"
          placeholder="New folder name"
          class="min-w-0 flex-1 rounded-md border border-gray-300 px-2.5 py-1.5 text-sm outline-none ring-primary-200 focus:border-primary-500 focus:ring-2"
          :disabled="isCreatingDir"
          @keydown.enter.prevent="confirmCreateDir"
        />
        <button
          type="button"
          class="inline-flex items-center gap-1 rounded-md bg-primary-600 px-3 py-1.5 text-xs font-medium text-white hover:bg-primary-700 disabled:cursor-not-allowed disabled:opacity-50"
          :disabled="isCreatingDir || !newDirName.trim()"
          @click="confirmCreateDir"
        >
          <LoaderCircle v-if="isCreatingDir" class="h-3 w-3 animate-spin" />
          Create
        </button>
        <button
          type="button"
          class="rounded-md px-2 py-1.5 text-xs font-medium text-gray-600 hover:bg-gray-200"
          :disabled="isCreatingDir"
          @click="showCreateDir = false"
        >
          Cancel
        </button>
      </div>

      <!-- Error -->
      <div
        v-if="errorMessage"
        class="mx-5 mt-3 rounded-lg border border-red-200 bg-red-50 px-3 py-2 text-sm text-red-700"
      >
        {{ errorMessage }}
      </div>

      <!-- Hints -->
      <p v-if="mode === 'folder'" class="mx-5 mt-3 text-xs text-gray-500">
        Only folders are shown. Click a folder to select it. Double-click to navigate.
      </p>
      <p v-else class="mx-5 mt-3 text-xs text-gray-500">
        Click a file to select it. Double-click a folder to navigate.
      </p>

      <!-- File listing -->
      <div class="min-h-0 flex-1 overflow-y-auto px-5 py-2">
        <div
          v-if="isLoading"
          class="flex items-center justify-center gap-2 py-16 text-sm text-gray-500"
        >
          <LoaderCircle class="h-4 w-4 animate-spin" />
          Loading...
        </div>

        <div v-else-if="sortedEntries.length === 0" class="py-12 text-center text-sm text-gray-500">
          Empty folder
        </div>

        <div v-else class="space-y-0.5">
          <div
            v-for="entry in sortedEntries"
            :key="entry.name"
            class="flex cursor-pointer items-center gap-3 rounded-lg px-3 py-2 text-sm transition"
            :class="[
              selectedEntryPath === entryPath(entry.name)
                ? 'bg-primary-50 ring-1 ring-primary-300'
                : 'hover:bg-gray-100',
            ]"
            @click="openEntry(entry)"
            @dblclick="onDblClick(entry)"
          >
            <Folder v-if="entry.type === 'folder'" class="h-4 w-4 shrink-0 text-amber-500" />
            <File v-else class="h-4 w-4 shrink-0 text-gray-400" />
            <span class="min-w-0 flex-1 truncate">{{ entry.name }}</span>
            <span v-if="entry.type === 'file'" class="shrink-0 text-xs text-gray-400">
              {{ entry.length > 0 ? `${(entry.length / 1024).toFixed(entry.length >= 10240 ? 0 : 1)} KB` : '-' }}
            </span>
          </div>
        </div>
      </div>

      <!-- Footer -->
      <div class="flex items-center justify-between border-t border-gray-200 px-5 py-3">
        <div class="text-xs text-gray-500">
          {{ selectedEntryPath ?? currentPath }}
        </div>
        <div class="flex gap-2">
          <button
            type="button"
            class="rounded-md border border-gray-200 bg-white px-4 py-2 text-sm font-medium text-gray-700 hover:bg-gray-50"
            @click="emit('close')"
          >
            Cancel
          </button>
          <button
            type="button"
            class="rounded-md bg-primary-600 px-4 py-2 text-sm font-medium text-white hover:bg-primary-700 disabled:cursor-not-allowed disabled:opacity-50"
            :disabled="!canConfirm"
            @click="confirmSelection"
          >
            {{ mode === 'folder' ? 'Select this folder' : 'Select' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>
