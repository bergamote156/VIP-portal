<script setup lang="ts">
import { ref } from 'vue'
import TheSidebar from './Sidebar.vue'
import TheNavbar from './Navbar.vue'

const sidebarOpen = ref(false)
</script>

<template>
  <div class="min-h-screen bg-gray-50">
    <TheSidebar :open="sidebarOpen" @close="sidebarOpen = false" />
    <div class="lg:pl-64">
      <TheNavbar @open-sidebar="sidebarOpen = true" />
      <main class="p-4 sm:p-6">
        <slot />
      </main>
    </div>
  </div>
</template>
