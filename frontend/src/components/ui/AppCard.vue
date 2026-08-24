<script setup lang="ts">
interface Props {
  hoverable?: boolean
  padding?: boolean
  marginTop?: boolean
}

withDefaults(defineProps<Props>(), {
  hoverable: false,
  padding: true,
  marginTop: false,
})
</script>

<template>
  <div
    :class="[
      'rounded-xl border border-gray-200 bg-white shadow-sm',
      hoverable && 'transition-all duration-200 hover:shadow-md hover:border-gray-300 cursor-pointer',
      padding && 'p-6',
      marginTop && 'mt-6',
    ]"
  >
    <slot />
  </div>
</template>
