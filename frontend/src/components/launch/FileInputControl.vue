<script setup lang="ts">
import { ref } from 'vue'
import { Upload } from 'lucide-vue-next'
import { filesApi } from '@/api/files.api'
import { useAuthStore } from '@/stores/auth.store'
import { useNotificationsStore } from '@/stores/notifications.store'
import FilePickerModal from './FilePickerModal.vue'

const props = withDefaults(defineProps<{
  modelValue: string
  disabled?: boolean
  mode?: 'file' | 'folder'
}>(), {
  mode: 'file',
})

const emit = defineEmits<{
  'update:modelValue': [value: string]
}>()

const authStore = useAuthStore()
const notifications = useNotificationsStore()

const isBrowserOpen = ref(false)
const uploadInput = ref<HTMLInputElement | null>(null)
const isUploading = ref(false)

function openBrowser() {
  isBrowserOpen.value = true
}

function onFileSelected(path: string) {
  emit('update:modelValue', path)
  isBrowserOpen.value = false
}

function triggerUpload() {
  if (props.disabled || isUploading.value) return
  uploadInput.value?.click()
}

async function ensureDirectory(path: string, name: string) {
  const children = await filesApi.listChildren(path)
  if (!children.some((c) => c.name === name)) {
    await filesApi.createDirectory(path, name)
  }
}

async function onUploadChange(event: Event) {
  const input = event.target as HTMLInputElement
  const file = input.files?.[0]
  if (!file) return

  if (file.name.includes(' ')) {
    notifications.error('Filename cannot contain spaces.')
    input.value = ''
    return
  }

  const timestamp = Date.now()
  const userHome = `/vip/Home/`

  isUploading.value = true
  try {
    await ensureDirectory(userHome, 'direct-uploads')
    await filesApi.createDirectory(`${userHome}direct-uploads`, `${timestamp}`)
    await filesApi.uploadFile(`${userHome}direct-uploads/${timestamp}`, file)
    const uploadedPath = `${userHome}direct-uploads/${timestamp}/${file.name}`
    emit('update:modelValue', uploadedPath)
    notifications.success(`File uploaded to ${uploadedPath}`)
  } catch {
    notifications.error('Upload failed.')
  } finally {
    isUploading.value = false
    input.value = ''
  }
}
</script>

<template>
  <div class="flex items-start gap-2">
    <div class="min-w-0 flex-1">
      <input
        :value="modelValue"
        type="text"
        :placeholder="mode === 'folder' ? 'e.g. /vip/Home/user/results' : 'e.g. /vip/Home/user/file.txt'"
        :disabled="disabled"
        class="block w-full rounded-lg border border-gray-300 px-3 py-2 text-sm disabled:bg-gray-50 disabled:text-gray-500"
        @input="emit('update:modelValue', ($event.target as HTMLInputElement).value)"
      />
    </div>
    <button
      type="button"
      :title="mode === 'folder' ? 'Browse VIP storage' : 'Browse VIP storage'"
      class="inline-flex h-9 w-9 shrink-0 items-center justify-center rounded-md border border-gray-300 text-gray-600 transition hover:border-primary-300 hover:text-primary-600 disabled:cursor-not-allowed disabled:opacity-50"
      :disabled="disabled"
      @click="openBrowser"
    >
      <svg class="h-4 w-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
        <path d="M22 19a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h5l2 3h9a2 2 0 0 1 2 2z" />
        <line x1="12" y1="11" x2="12" y2="17" />
        <line x1="9" y1="14" x2="15" y2="14" />
      </svg>
    </button>
    <button
      v-if="mode === 'file'"
      type="button"
      title="Upload file to VIP"
      class="inline-flex h-9 w-9 shrink-0 items-center justify-center rounded-md border border-gray-300 text-gray-600 transition hover:border-primary-300 hover:text-primary-600 disabled:cursor-not-allowed disabled:opacity-50"
      :disabled="disabled || isUploading"
      @click="triggerUpload"
    >
      <Upload v-if="!isUploading" class="h-4 w-4" />
      <svg v-else class="h-4 w-4 animate-spin" viewBox="0 0 24 24" fill="none">
        <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4" />
        <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z" />
      </svg>
    </button>
    <input
      ref="uploadInput"
      type="file"
      class="hidden"
      :disabled="disabled || isUploading"
      @change="onUploadChange"
    />
  </div>

  <FilePickerModal
    v-if="isBrowserOpen"
    :mode="mode"
    @select="onFileSelected"
    @close="isBrowserOpen = false"
  />
</template>
