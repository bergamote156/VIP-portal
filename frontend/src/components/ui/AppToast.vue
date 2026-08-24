<script setup lang="ts">
import { useNotificationsStore } from '@/stores/notifications.store'
import {
  CheckCircle2,
  XCircle,
  AlertTriangle,
  Info,
  X,
} from 'lucide-vue-next'

const notifications = useNotificationsStore()

const iconMap = {
  success: CheckCircle2,
  error: XCircle,
  warning: AlertTriangle,
  info: Info,
}

const colorMap: Record<string, string> = {
  success: 'text-emerald-500',
  error: 'text-red-500',
  warning: 'text-amber-500',
  info: 'text-primary-500',
}

const toastVariantMap: Record<string, string> = {
  success: 'border-emerald-200 bg-emerald-50/80',
  error: 'border-red-200 bg-red-50/80',
  warning: 'border-amber-200 bg-amber-50/80',
  info: 'border-primary-200 bg-primary-50/80',
}

const titleColorMap: Record<string, string> = {
  success: 'text-emerald-900',
  error: 'text-red-900',
  warning: 'text-amber-900',
  info: 'text-primary-900',
}

const messageColorMap: Record<string, string> = {
  success: 'text-emerald-700',
  error: 'text-red-700',
  warning: 'text-amber-700',
  info: 'text-primary-700',
}
</script>

<template>
  <div class="fixed top-4 right-4 z-[100] flex flex-col gap-3 w-96">
    <TransitionGroup name="toast">
      <div
        v-for="toast in notifications.toasts"
        :key="toast.id"
        :data-toast-id="toast.id"
        :class="[
          'flex items-start gap-3 rounded-lg border p-4 shadow-lg',
          toastVariantMap[toast.type] ?? 'border-gray-200 bg-white',
        ]"
      >
        <component :is="iconMap[toast.type]" :class="['h-5 w-5 shrink-0 mt-0.5', colorMap[toast.type]]" />
        <div class="flex-1 min-w-0">
          <p :class="['text-sm font-medium', titleColorMap[toast.type] ?? 'text-gray-900']">{{ toast.title }}</p>
          <p :class="['mt-0.5 text-sm', messageColorMap[toast.type] ?? 'text-gray-500']">{{ toast.message }}</p>
        </div>
        <button
          class="shrink-0 rounded p-0.5 text-gray-400 hover:text-gray-600 transition-colors"
          @click="notifications.removeToast(toast.id)"
        >
          <X class="h-4 w-4" />
        </button>
      </div>
    </TransitionGroup>
  </div>
</template>

<style scoped>
.toast-enter-active {
  transition: all 0.3s ease-out;
}
.toast-leave-active {
  transition: all 0.2s ease-in;
}
.toast-enter-from {
  opacity: 0;
  transform: translateX(100%);
}
.toast-leave-to {
  opacity: 0;
  transform: translateX(100%);
}
</style>
