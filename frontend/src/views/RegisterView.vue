<script setup lang="ts">
import { reactive, ref, computed } from 'vue'
import { useRouter, RouterLink } from 'vue-router'
import AppInput from '@/components/ui/AppInput.vue'
import AppButton from '@/components/ui/AppButton.vue'
import { useAuthStore } from '@/stores/auth.store'
import type { RegisterPayload } from '@/types/auth.types'

const router = useRouter()
const authStore = useAuthStore()

const form = reactive({
  firstName: '',
  lastName: '',
  email: '',
  password: '',
  passwordConfirm: '',
  countryCode: '',
  institution: '',
  comments: '',
  acceptTerms: false,
})

const passwordMismatch = computed(() =>
  form.password && form.passwordConfirm && form.password !== form.passwordConfirm,
)

async function onSubmit() {
  if (passwordMismatch.value) {
    console.log('Passwords do not match')
    return
  }
  if (!form.acceptTerms) {
    console.log('Terms must be accepted')
    return
  }

  const payload: RegisterPayload = {
    firstName: form.firstName,
    lastName: form.lastName,
    email: form.email,
    password: form.password,
    countryCode: form.countryCode,
    institution: form.institution,
    comments: form.comments,
  }

  try {
    await authStore.register(payload)
    console.log('Account created successfully')
    router.push({ name: 'activate', params: { id: form.email } })
  } catch {
    console.log('Error during registration')
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
        Create your account
      </h1>
      <p class="mt-1 text-sm text-gray-500">
        Join the VIP community and start using the portal to manage and launch applications for your research projects.
      </p>
    </div>

    <form class="space-y-4" @submit.prevent="onSubmit">
      <div class="grid grid-cols-2 gap-4">
        <AppInput
          v-model="form.firstName"
          label="First Name"
          placeholder="Isaac"
          required
        />
        <AppInput
          v-model="form.lastName"
          label="Last Name"
          placeholder="Asimov"
          required
        />
      </div>

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

      <AppInput
        v-model="form.passwordConfirm"
        label="Confirm Password"
        type="password"
        placeholder="••••••••"
        :error="passwordMismatch ? 'Passwords do not match' : undefined"
        required
      />

      <AppInput
        v-model="form.countryCode"
        label="Country Code"
        placeholder="fr"
        required
      />

      <AppInput
        v-model="form.institution"
        label="Institution"
        placeholder="Université Paris-Saclay"
        required
      />

      <div class="space-y-1">
        <label class="block text-sm font-medium text-gray-700">
          Comments for the VIP team
        </label>
        <textarea
          v-model="form.comments"
          rows="4"
          placeholder="Describe your research project..."
          class="block w-full rounded-lg border border-gray-300 px-3 py-2 text-sm placeholder:text-gray-400 focus:outline-none focus:ring-2 focus:ring-primary-500 focus:ring-offset-0 focus:border-primary-500 transition-colors duration-150"
        />
      </div>

      <label class="flex items-start gap-3 cursor-pointer">
        <input
          v-model="form.acceptTerms"
          type="checkbox"
          class="mt-1 h-4 w-4 rounded border-gray-300 text-primary-600 focus:ring-primary-500"
        />
        <span class="text-sm text-gray-600">
          I accept the terms and conditions
        </span>
      </label>

      <AppButton
        type="submit"
        variant="primary"
        :loading="authStore.isLoading"
      >
        Create my account
      </AppButton>
    </form>

    <p class="text-center text-sm text-gray-600">
      Already have an account?
      <RouterLink to="/login" class="font-medium text-primary-600 hover:text-primary-700">
        Log in
      </RouterLink>
    </p>
  </div>
</template>
