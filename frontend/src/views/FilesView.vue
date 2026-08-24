<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import axios from 'axios'
import {
  ArrowDown,
  ArrowUp,
  ChevronRight,
  File,
  Folder,
  FolderPlus,
  House,
  RefreshCw,
  Trash2,
  LoaderCircle,
} from 'lucide-vue-next'
import { filesApi, type BackendData } from '@/api/files.api'
import { useNotificationsStore } from '@/stores/notifications.store'

interface ApiErrorPayload {
  errorCode?: number
  errorMessage?: string
}

const ROOT_PATH = '/vip'
const FILE_TOO_LARGE_ERROR_CODE = 1003

const currentPath = ref(ROOT_PATH)
const entries = ref<BackendData[]>([])
const isLoading = ref(false)
const errorMessage = ref<string | null>(null)
const activeActionId = ref<string | null>(null)
const uploadInput = ref<HTMLInputElement | null>(null)
const isDeleteModalOpen = ref(false)
const pendingDeleteEntry = ref<BackendData | null>(null)
const isCreateDirectoryModalOpen = ref(false)
const newDirectoryName = ref('')
const notificationsStore = useNotificationsStore()

const breadcrumbs = computed(() => {
  const segments = currentPath.value.split('/').filter(Boolean)
  const crumbs = [{ label: 'vip', path: ROOT_PATH }]

  let path = ''
  for (const segment of segments) {
    path += `/${segment}`
    if (path === ROOT_PATH) {
      continue
    }
    crumbs.push({ label: segment, path })
  }

  return crumbs
})

const canGoUp = computed(() => currentPath.value !== ROOT_PATH)

const sortedEntries = computed(() => {
  return [...entries.value].sort((a, b) => {
    if (a.type !== b.type) {
      return a.type === 'folder' ? -1 : 1
    }
    return (a.name || '').localeCompare(b.name || '')
  })
})

function entryPath(name: string): string {
  const base = currentPath.value.endsWith('/') ? currentPath.value.slice(0, -1) : currentPath.value
  return `${base}/${name}`.replace(/\/{2,}/g, '/')
}

function formatSize(bytes: number): string {
  if (bytes <= 0) return '-'
  const units = ['B', 'KB', 'MB', 'GB', 'TB']
  let size = bytes
  let unitIndex = 0
  while (size >= 1024 && unitIndex < units.length - 1) {
    size /= 1024
    unitIndex++
  }
  return `${size.toFixed(size >= 10 || unitIndex === 0 ? 0 : 1)} ${units[unitIndex]}`
}

function fileNameFromPath(path: string): string {
  const parts = path.split('/').filter(Boolean)
  return parts[parts.length - 1] ?? 'download'
}

function parentPath(path: string): string {
  if (path === ROOT_PATH) return ROOT_PATH
  const parts = path.split('/').filter(Boolean)
  if (parts.length <= 1) return ROOT_PATH
  return `/${parts.slice(0, -1).join('/')}`
}

async function loadDirectory(path = currentPath.value): Promise<void> {
  isLoading.value = true
  errorMessage.value = null
  try {
    const data = await filesApi.listChildren(path)
    currentPath.value = path
    entries.value = data
  } catch {
    errorMessage.value = `Unable to load folder ${path}.`
    notificationsStore.error(`Unable to load folder ${path}.`)
  } finally {
    isLoading.value = false
  }
}

function openEntry(entry: BackendData): void {
  if (entry.type === 'folder') {
    void loadDirectory(entryPath(entry.name))
  }
}

function goUp(): void {
  if (!canGoUp.value) return
  void loadDirectory(parentPath(currentPath.value))
}

function refreshDirectory(): void {
  void loadDirectory(currentPath.value)
}

function triggerUploadPicker(): void {
  if (isLoading.value || activeActionId.value !== null) return
  uploadInput.value?.click()
}

async function onUploadChange(event: Event): Promise<void> {
  const input = event.target as HTMLInputElement
  const file = input.files?.[0]
  if (!file) {
    return
  }

  const id = `upload:${file.name}`
  activeActionId.value = id
  errorMessage.value = null
  const uploadToastId = notificationsStore.addToast({
    type: 'info',
    title: 'Upload in progress',
    message: `Uploading ${file.name}...`,
    duration: 60000,
  })

  try {
    await filesApi.uploadFile(currentPath.value, file)
    await loadDirectory(currentPath.value)
    notificationsStore.updateToast(uploadToastId, {
      type: 'success',
      title: 'Upload completed',
      message: `${file.name} uploaded successfully.`,
      duration: 5000,
    })
  } catch (error: unknown) {
    const uploadErrorMessage = getUploadErrorMessage(error, file.name)
    errorMessage.value = uploadErrorMessage
    notificationsStore.updateToast(uploadToastId, {
      type: 'error',
      title: 'Upload failed',
      message: uploadErrorMessage,
      duration: 7000,
    })
  } finally {
    input.value = ''
    activeActionId.value = null
  }
}

function openCreateDirectoryModal(): void {
  if (isLoading.value || activeActionId.value !== null) return
  newDirectoryName.value = ''
  isCreateDirectoryModalOpen.value = true
}

function closeCreateDirectoryModal(): void {
  if (activeActionId.value === 'create-directory') return
  isCreateDirectoryModalOpen.value = false
  newDirectoryName.value = ''
}

async function confirmCreateDirectory(): Promise<void> {
  const name = newDirectoryName.value.trim()
  if (name.length === 0) {
    errorMessage.value = 'Le nom du dossier est requis.'
    return
  }
  if (name.includes(' ')) {
    errorMessage.value = "Le nom du dossier ne peut pas contenir d'espaces."
    return
  }

  activeActionId.value = 'create-directory'
  errorMessage.value = null

  try {
    const createDirectoryToastId = notificationsStore.addToast({
      type: 'info',
      title: 'Creating folder',
      message: `Creating ${name}...`,
      duration: 60000,
    })
    await filesApi.createDirectory(currentPath.value, name)
    await loadDirectory(currentPath.value)
    notificationsStore.updateToast(createDirectoryToastId, {
      type: 'success',
      title: 'Folder created',
      message: `${name} created successfully.`,
      duration: 5000,
    })
    isCreateDirectoryModalOpen.value = false
    newDirectoryName.value = ''
  } catch {
    errorMessage.value = `Unable to create folder ${name}.`
    notificationsStore.error(`Unable to create folder ${name}.`)
  } finally {
    activeActionId.value = null
  }
}

async function downloadEntry(entry: BackendData): Promise<void> {
  if (entry.type !== 'file') return

  const id = `download:${entry.name}`
  activeActionId.value = id
  errorMessage.value = null

  const path = entryPath(entry.name)
  let objectUrl: string | null = null
  const downloadToastId = notificationsStore.addToast({
    type: 'info',
    title: 'Download in progress',
    message: `Preparing ${entry.name}...`,
    duration: 60000,
  })

  try {
    const blob = await filesApi.downloadFile(path)
    objectUrl = window.URL.createObjectURL(blob)

    const link = document.createElement('a')
    link.href = objectUrl
    link.download = fileNameFromPath(path)
    document.body.appendChild(link)
    link.click()
    link.remove()
    notificationsStore.updateToast(downloadToastId, {
      type: 'success',
      title: 'Download ready',
      message: `${entry.name} download started.`,
      duration: 5000,
    })
  } catch {
    errorMessage.value = `Unable to download file ${entry.name}.`
    notificationsStore.updateToast(downloadToastId, {
      type: 'error',
      title: 'Download failed',
      message: `Unable to download file ${entry.name}.`,
      duration: 7000,
    })
  } finally {
    if (objectUrl !== null) {
      window.URL.revokeObjectURL(objectUrl)
    }
    activeActionId.value = null
  }
}

async function deleteEntry(entry: BackendData): Promise<boolean> {
  const id = `delete:${entry.name}`
  activeActionId.value = id
  errorMessage.value = null

  try {
    if (entry.type === 'folder') {
      await filesApi.deleteDirectory(entryPath(entry.name))
    } else {
      await filesApi.deleteFile(entryPath(entry.name))
    }
    await loadDirectory(currentPath.value)
    return true
  } catch {
    const targetLabel = entry.type === 'folder' ? 'folder' : 'file'
    errorMessage.value = `Unable to delete ${targetLabel} ${entry.name}.`
    notificationsStore.error(`Unable to delete ${targetLabel} ${entry.name}.`)
    return false
  } finally {
    activeActionId.value = null
  }
}

function requestDelete(entry: BackendData): void {
  if (isLoading.value || activeActionId.value !== null) return
  pendingDeleteEntry.value = entry
  isDeleteModalOpen.value = true
}

function closeDeleteModal(): void {
  if (activeActionId.value?.startsWith('delete:')) return
  isDeleteModalOpen.value = false
  pendingDeleteEntry.value = null
}

async function confirmDelete(): Promise<void> {
  const entry = pendingDeleteEntry.value
  if (!entry) return
  const deleted = await deleteEntry(entry)
  isDeleteModalOpen.value = false
  pendingDeleteEntry.value = null
  if (deleted) {
    notificationsStore.info('Deletion request has been taken into account.')
  }
}

function getUploadErrorMessage(error: unknown, fileName: string): string {
  const apiError = extractApiError(error)
  if (apiError.errorCode === FILE_TOO_LARGE_ERROR_CODE) {
    return apiError.errorMessage || 'File is too large for upload.'
  }
  return `Unable to upload file ${fileName}.`
}

function extractApiError(error: unknown): ApiErrorPayload {
  if (!axios.isAxiosError(error)) {
    return {}
  }
  const data = error.response?.data
  if (!data || typeof data !== 'object') {
    return {}
  }
  return {
    errorCode: typeof (data as ApiErrorPayload).errorCode === 'number'
      ? (data as ApiErrorPayload).errorCode
      : undefined,
    errorMessage: typeof (data as ApiErrorPayload).errorMessage === 'string'
      ? (data as ApiErrorPayload).errorMessage
      : undefined,
  }
}

onMounted(() => {
  void loadDirectory(ROOT_PATH)
})
</script>

<template>
  <section class="space-y-6">
    <header class="space-y-4">
      <div>
        <h1 class="text-2xl font-semibold text-gray-900">Fichiers</h1>
        <p class="mt-1 text-sm text-gray-600">Naviguez dans votre espace VIP et gerez vos donnees.</p>
      </div>

      <div class="flex flex-wrap items-center gap-2">
        <button
          type="button"
          class="inline-flex items-center gap-2 rounded-lg border border-gray-200 bg-white px-3 py-2 text-sm font-medium text-gray-700 hover:bg-gray-50 disabled:cursor-not-allowed disabled:opacity-50"
          :disabled="!canGoUp || isLoading"
          @click="goUp"
        >
          <ArrowUp class="h-4 w-4" />
          Monter
        </button>
        <button
          type="button"
          class="inline-flex items-center gap-2 rounded-lg border border-gray-200 bg-white px-3 py-2 text-sm font-medium text-gray-700 hover:bg-gray-50 disabled:cursor-not-allowed disabled:opacity-50"
          :disabled="isLoading"
          @click="refreshDirectory"
        >
          <RefreshCw class="h-4 w-4" />
          Rafraichir
        </button>
        <button
          type="button"
          class="inline-flex items-center gap-2 rounded-lg border border-gray-200 bg-white px-3 py-2 text-sm font-medium text-gray-700 hover:bg-gray-50 disabled:cursor-not-allowed disabled:opacity-50"
          :disabled="isLoading || activeActionId !== null"
          @click="openCreateDirectoryModal"
        >
          <LoaderCircle v-if="activeActionId === 'create-directory'" class="h-4 w-4 animate-spin" />
          <FolderPlus v-else class="h-4 w-4" />
          Nouveau dossier
        </button>
        <button
          type="button"
          class="inline-flex items-center gap-2 rounded-lg border border-gray-200 bg-white px-3 py-2 text-sm font-medium text-gray-700 hover:bg-gray-50 disabled:cursor-not-allowed disabled:opacity-50"
          :disabled="isLoading || activeActionId !== null"
          @click="triggerUploadPicker"
        >
          <LoaderCircle v-if="activeActionId?.startsWith('upload:')" class="h-4 w-4 animate-spin" />
          <ArrowUp v-else class="h-4 w-4" />
          Upload
        </button>
        <input
          ref="uploadInput"
          type="file"
          class="hidden"
          :disabled="isLoading || activeActionId !== null"
          @change="onUploadChange"
        />
      </div>

      <nav class="flex flex-wrap items-center gap-2 text-sm text-gray-600">
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
    </header>

    <div v-if="errorMessage" class="rounded-lg border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700">
      {{ errorMessage }}
    </div>

    <div class="overflow-hidden rounded-xl border border-gray-200 bg-white">
      <div
        v-if="isLoading"
        class="flex items-center justify-center gap-2 px-4 py-16 text-sm font-medium text-gray-500"
      >
        <LoaderCircle class="h-4 w-4 animate-spin" />
        Chargement du dossier...
      </div>

      <table v-else class="min-w-full divide-y divide-gray-200">
        <thead class="bg-gray-50">
          <tr>
            <th class="px-4 py-3 text-left text-xs font-semibold uppercase tracking-wide text-gray-500">
              Nom
            </th>
            <th class="px-4 py-3 text-left text-xs font-semibold uppercase tracking-wide text-gray-500">
              Taille
            </th>
            <th class="px-4 py-3 text-left text-xs font-semibold uppercase tracking-wide text-gray-500">
              Modifie
            </th>
            <th class="px-4 py-3 text-left text-xs font-semibold uppercase tracking-wide text-gray-500">
              Permissions
            </th>
            <th class="px-4 py-3 text-right text-xs font-semibold uppercase tracking-wide text-gray-500">
              Actions
            </th>
          </tr>
        </thead>

        <tbody class="divide-y divide-gray-100">
          <tr
            v-for="entry in sortedEntries"
            :key="entry.name"
            class="group hover:bg-gray-50"
            @dblclick="openEntry(entry)"
          >
            <td class="px-4 py-3">
              <button
                type="button"
                class="inline-flex items-center gap-2 text-left text-sm font-medium text-gray-800 hover:text-primary-700"
                @click="openEntry(entry)"
              >
                <Folder v-if="entry.type === 'folder'" class="h-4 w-4 text-amber-500" />
                <File v-else class="h-4 w-4 text-gray-400" />
                {{ entry.name }}
              </button>
            </td>
            <td class="px-4 py-3 text-sm text-gray-600">
              {{ entry.type === 'folder' ? '-' : formatSize(entry.length) }}
            </td>
            <td class="px-4 py-3 text-sm text-gray-600">
              {{ entry.modificationDate || '-' }}
            </td>
            <td class="px-4 py-3 text-sm text-gray-600">
              {{ entry.permissions || '-' }}
            </td>
            <td class="px-4 py-3">
              <div class="flex justify-end gap-2">
                <button
                  v-if="entry.type === 'file'"
                  type="button"
                  class="inline-flex items-center gap-1 rounded-md border border-gray-200 bg-white px-2.5 py-1.5 text-xs font-medium text-gray-700 hover:bg-gray-50 disabled:cursor-not-allowed disabled:opacity-50"
                  :disabled="isLoading || activeActionId !== null"
                  @click="downloadEntry(entry)"
                >
                  <LoaderCircle v-if="activeActionId === `download:${entry.name}`" class="h-3.5 w-3.5 animate-spin" />
                  <ArrowDown v-else class="h-3.5 w-3.5" />
                  Download
                </button>
                <button
                  type="button"
                  class="inline-flex items-center gap-1 rounded-md border border-red-200 bg-white px-2.5 py-1.5 text-xs font-medium text-red-700 hover:bg-red-50 disabled:cursor-not-allowed disabled:opacity-50"
                  :disabled="isLoading || activeActionId !== null"
                  @click="requestDelete(entry)"
                >
                  <LoaderCircle v-if="activeActionId === `delete:${entry.name}`" class="h-3.5 w-3.5 animate-spin" />
                  <Trash2 v-else class="h-3.5 w-3.5" />
                  Delete
                </button>
              </div>
            </td>
          </tr>
          <tr v-if="sortedEntries.length === 0">
            <td colspan="5" class="px-4 py-12 text-center text-sm text-gray-500">Dossier vide</td>
          </tr>
        </tbody>
      </table>
    </div>

    <div
      v-if="isDeleteModalOpen"
      class="fixed inset-0 z-50 flex items-center justify-center bg-black/40 px-4"
      @click.self="closeDeleteModal"
    >
      <div class="w-full max-w-md rounded-xl border border-gray-200 bg-white p-5 shadow-lg">
        <h2 class="text-lg font-semibold text-gray-900">Confirmer la suppression</h2>
        <p class="mt-2 text-sm text-gray-600">
          Voulez-vous vraiment supprimer
          <span class="font-medium text-gray-900">{{ pendingDeleteEntry?.name }}</span>
          ?
        </p>
        <div class="mt-5 flex justify-end gap-2">
          <button
            type="button"
            class="inline-flex items-center rounded-md border border-gray-200 bg-white px-3 py-2 text-sm font-medium text-gray-700 hover:bg-gray-50 disabled:cursor-not-allowed disabled:opacity-50"
            :disabled="activeActionId?.startsWith('delete:')"
            @click="closeDeleteModal"
          >
            Annuler
          </button>
          <button
            type="button"
            class="inline-flex items-center gap-2 rounded-md border border-red-200 bg-red-600 px-3 py-2 text-sm font-medium text-white hover:bg-red-700 disabled:cursor-not-allowed disabled:opacity-50"
            :disabled="activeActionId?.startsWith('delete:')"
            @click="confirmDelete"
          >
            <LoaderCircle v-if="activeActionId?.startsWith('delete:')" class="h-4 w-4 animate-spin" />
            Supprimer
          </button>
        </div>
      </div>
    </div>

    <div
      v-if="isCreateDirectoryModalOpen"
      class="fixed inset-0 z-50 flex items-center justify-center bg-black/40 px-4"
      @click.self="closeCreateDirectoryModal"
    >
      <div class="w-full max-w-md rounded-xl border border-gray-200 bg-white p-5 shadow-lg">
        <h2 class="text-lg font-semibold text-gray-900">Creer un dossier</h2>
        <p class="mt-2 text-sm text-gray-600">
          Entrez le nom du nouveau dossier dans
          <span class="font-medium text-gray-900">{{ currentPath }}</span>.
        </p>
        <div class="mt-4">
          <label for="new-directory-name" class="mb-1 block text-sm font-medium text-gray-700">
            Nom du dossier
          </label>
          <input
            id="new-directory-name"
            v-model="newDirectoryName"
            type="text"
            class="w-full rounded-md border border-gray-300 px-3 py-2 text-sm text-gray-800 outline-none ring-primary-200 focus:border-primary-500 focus:ring-2"
            :disabled="activeActionId === 'create-directory'"
            @keydown.enter.prevent="confirmCreateDirectory"
          />
        </div>
        <div class="mt-5 flex justify-end gap-2">
          <button
            type="button"
            class="inline-flex items-center rounded-md border border-gray-200 bg-white px-3 py-2 text-sm font-medium text-gray-700 hover:bg-gray-50 disabled:cursor-not-allowed disabled:opacity-50"
            :disabled="activeActionId === 'create-directory'"
            @click="closeCreateDirectoryModal"
          >
            Annuler
          </button>
          <button
            type="button"
            class="inline-flex items-center gap-2 rounded-md border border-primary-200 bg-primary-600 px-3 py-2 text-sm font-medium text-white hover:bg-primary-700 disabled:cursor-not-allowed disabled:opacity-50"
            :disabled="activeActionId === 'create-directory'"
            @click="confirmCreateDirectory"
          >
            <LoaderCircle v-if="activeActionId === 'create-directory'" class="h-4 w-4 animate-spin" />
            Creer
          </button>
        </div>
      </div>
    </div>
  </section>
</template>
