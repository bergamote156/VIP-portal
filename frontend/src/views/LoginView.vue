<script setup lang="ts">
import { reactive } from 'vue'
import { useRouter, RouterLink } from 'vue-router'
import AppInput from '@/components/ui/AppInput.vue'
import AppButton from '@/components/ui/AppButton.vue'
import { useAuthStore } from '@/stores/auth.store'
import { useNotificationsStore } from '@/stores/notifications.store'
import axios from 'axios'

const router = useRouter()
const authStore = useAuthStore()
const notificationsStore = useNotificationsStore()

const form = reactive({
  email: '',
  password: '',
})

async function onSubmit() {
  try {
    const session = await authStore.login({ username: form.email, password: form.password })
    if (session.confirmed === false) {
      notificationsStore.error('Votre compte n\'est pas encore activé.')
      router.push({ name: 'activate', params: { id: form.email } })
      return
    }
    notificationsStore.success('Vous êtes maintenant connecté.')
    console.log('Connected')
    router.push('/dashboard')
  } catch (error: unknown) {
    let errorMessage = 'Error during login'

    if (axios.isAxiosError(error)) {
      if (error.response) {
        console.error('Login error response:', error.response)
        switch (error.response.status) {
          case 401:
            errorMessage = 'Invalid email or password.'
            break
          case 403:
            errorMessage = 'Your account is not activated yet.'
            break
          default:
            errorMessage = `Login failed with status ${error.response.status}.`
            break
        }
      }
    }
    notificationsStore.error(errorMessage)
  }
}
</script>

<template>
  <div class="space-y-8">
    <div class="lg:hidden flex justify-center">
      <img
        src="@/assets/vip-logo-without-text.png"
        alt="VIP Logo"
        class="h-24 w-auto rounded-sm object-cover"
      />
    </div>

    <div>
      <h1 class="text-2xl font-bold text-gray-900">
        Welcome back
      </h1>
      <p class="mt-1 text-sm text-gray-500">Log in to your VIP Portal account</p>
    </div>

    <form class="space-y-5" @submit.prevent="onSubmit">
      <AppInput
        v-model="form.email"
        label="Email"
        type="email"
        placeholder="you@example.com"
        required
      />
      <AppInput
        v-model="form.password"
        label="Password"
        type="password"
        placeholder="••••••••"
        required
      />
      <AppButton type="submit" variant="primary" :loading="authStore.isLoading">
        Log in
      </AppButton>
    </form>

    <p class="text-center text-sm text-gray-600">
      Don't have an account?
      <RouterLink to="/register" class="font-medium text-primary-600 hover:text-primary-700">
        Create an account
      </RouterLink>
    </p>
  </div>
</template>
