<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter, useRoute, RouterLink } from 'vue-router'
import AppInput from '@/components/ui/AppInput.vue'
import AppButton from '@/components/ui/AppButton.vue'
import { useAuthStore } from '@/stores/auth.store'
import { useNotificationsStore } from '@/stores/notifications.store'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()
const notificationsStore = useNotificationsStore()

const code = ref('')
const errorMessage = ref('')
// URL is http://localhost:5173/activate/z@z.z, email is z@z.z
const email = computed(() => {
  const idParam = route.params.id
  return typeof idParam === 'string' ? idParam : ''
})

async function onSubmit() {
  if (!code.value) return
  errorMessage.value = ''
  try {

    console.log("email: ", email.value, "code: ", code.value)
    await authStore.activate(email.value, code.value)
    // If successfully activated and session exists
    notificationsStore.success('Your account has been activated and you are now logged in.')
    router.push('/dashboard')
  } catch (e: any) {
    console.error('Error during activation', e)
    errorMessage.value = "The verification code is invalid or expired. Please check your email and try again."
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
        Email Verification
      </h1>
      <p class="mt-2 text-sm text-gray-500">
        An email with a verification code has been sent to <strong>{{ email }}</strong>.
        Please enter the code below to activate your account.
      </p>
    </div>

    <form class="space-y-6" @submit.prevent="onSubmit">
      <AppInput
        v-model="code"
        label="Verification Code"
        placeholder="e.g., 123456"
        required
      />

      <div v-if="errorMessage" class="text-sm text-red-600 bg-red-50 p-3 rounded-md border border-red-200">
        {{ errorMessage }}
      </div>

      <AppButton
        type="submit"
        variant="primary"
        class="w-full"
        :loading="authStore.isLoading"
      >
        Validate and Login
      </AppButton>
    </form>

    <p class="text-center text-sm text-gray-600">
      <RouterLink to="/login" class="font-medium text-primary-600 hover:text-primary-700">
        Return to Login Page
      </RouterLink>
    </p>
  </div>
</template>
