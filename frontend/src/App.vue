<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import AuthLayout from '@/components/layout/AuthLayout.vue'
import MainLayout from '@/components/layout/MainLayout.vue'
import AppToast from '@/components/ui/AppToast.vue'

const route = useRoute()
const routeName = computed(() => (typeof route.name === 'string' ? route.name : ''))
const isRouteResolved = computed(() => routeName.value.length > 0)
const isLandingRoute = computed(() => ['landing', 'home'].includes(routeName.value))
const isAuthRoute = computed(() => ['login', 'register', 'activate'].includes(routeName.value))
</script>

<template>
  <AppToast />

  <template v-if="!isRouteResolved || isLandingRoute">
    <RouterView />
  </template>
  <component v-else :is="isAuthRoute ? AuthLayout : MainLayout">
    <RouterView />
  </component>
</template>
