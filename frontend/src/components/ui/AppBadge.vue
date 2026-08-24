<script setup lang="ts">
export type BadgeVariant =
  | 'primary'
  | 'success'
  | 'warning'
  | 'danger'
  | 'gray'
  | 'info'
  | 'red'
  | 'orange'
  | 'amber'
  | 'yellow'
  | 'lime'
  | 'green'
  | 'emerald'
  | 'teal'
  | 'cyan'
  | 'sky'
  | 'blue'
  | 'indigo'
  | 'violet'
  | 'purple'
  | 'fuchsia'
  | 'pink'
  | 'rose'

const props = withDefaults(
  defineProps<{ variant?: BadgeVariant }>(),
  { variant: 'gray' as BadgeVariant },
)

const variantClasses: Record<BadgeVariant, string> = {
  primary: 'bg-primary-50 text-primary-700 ring-primary-600/20',
  success: 'bg-emerald-50 text-emerald-700 ring-emerald-600/20',
  warning: 'bg-amber-50 text-amber-700 ring-amber-600/20',
  danger: 'bg-red-50 text-red-700 ring-red-600/20',
  gray: 'bg-gray-50 text-gray-600 ring-gray-500/20',
  info: 'bg-sky-50 text-sky-700 ring-sky-600/20',

  red: 'bg-red-50 text-red-700 ring-red-600/20',
  orange: 'bg-orange-50 text-orange-700 ring-orange-600/20',
  amber: 'bg-amber-50 text-amber-700 ring-amber-600/20',
  yellow: 'bg-yellow-50 text-yellow-700 ring-yellow-600/20',
  lime: 'bg-lime-50 text-lime-700 ring-lime-600/20',
  green: 'bg-green-50 text-green-700 ring-green-600/20',
  emerald: 'bg-emerald-50 text-emerald-700 ring-emerald-600/20',
  teal: 'bg-teal-50 text-teal-700 ring-teal-600/20',
  cyan: 'bg-cyan-50 text-cyan-700 ring-cyan-600/20',
  sky: 'bg-sky-50 text-sky-700 ring-sky-600/20',
  blue: 'bg-blue-50 text-blue-700 ring-blue-600/20',
  indigo: 'bg-indigo-50 text-indigo-700 ring-indigo-600/20',
  violet: 'bg-violet-50 text-violet-700 ring-violet-600/20',
  purple: 'bg-purple-50 text-purple-700 ring-purple-600/20',
  fuchsia: 'bg-fuchsia-50 text-fuchsia-700 ring-fuchsia-600/20',
  pink: 'bg-pink-50 text-pink-700 ring-pink-600/20',
  rose: 'bg-rose-50 text-rose-700 ring-rose-600/20',
}
</script>

<template>
  <span
    :class="[
      'inline-flex items-center rounded-full px-2.5 py-0.5 text-xs font-medium ring-1 ring-inset',
      variantClasses[variant],
    ]"
  >
    <slot />
  </span>
</template>
