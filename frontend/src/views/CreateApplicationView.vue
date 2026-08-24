<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import AppCard from '@/components/ui/AppCard.vue'
import AppInput from '@/components/ui/AppInput.vue'
import AppButton from '@/components/ui/AppButton.vue'
import { appVersionsApi } from '@/api/appVersions.api'
import { applicationsApi } from '@/api/applications.api'
import { boutiquesApi } from '@/api/boutiques.api'
import { useNotificationsStore } from '@/stores/notifications.store'
import { useGroupsStore } from '@/stores/groups.store'
import { useResourcesStore } from '@/stores/resources.store'
import { useTagsStore } from '@/stores/tags.store'
import {
  addCustomTag as addCustomTagToLists,
  buildGroupSelectionItems,
  buildResourceSelectionItems,
  filterSelectableGroupNames,
  parseBoutiquesIdentity,
  toAppVersionResourcesPayload,
  toAppVersionTagsPayload,
  toApplicationGroupsPayload,
  toggleItemSelection,
} from '@/utils/createApplication'

const router = useRouter()
const notificationsStore = useNotificationsStore()
const groupsStore = useGroupsStore()
const resourcesStore = useResourcesStore()
const tagsStore = useTagsStore()

const form = reactive({
  descriptorFile: null as File | null,
  doi: '',
  visible: true,
  source: '',
  note: '',
})

const step = ref<1 | 2>(1)
const isSubmitting = ref(false)
const isChecking = ref(false)
const errorMessage = ref('')
const descriptorCheckErrors = ref<string[]>([])

const parsedAppName = ref('')
const parsedVersion = ref('')
const descriptorText = ref('')
const checksDone = ref(false)
const appExists = ref(false)
const versionExists = ref(false)
const shouldOverwrite = ref(false)

const availableTags = ref<string[]>([])
const selectedTags = ref<string[]>([])
const customTagInput = ref('')

const selectedGroupNames = ref<string[]>([])
const selectedResourceNames = ref<string[]>([])

const canProceedToStep2 = computed(
  () => checksDone.value && (!versionExists.value || shouldOverwrite.value),
)

const parsedGroups = computed(() => {
  return filterSelectableGroupNames(groupsStore.groups, selectedGroupNames.value)
})

const groupSelectionItems = computed(() => {
  return buildGroupSelectionItems(groupsStore.groups)
})

const resourceSelectionItems = computed(() => {
  return buildResourceSelectionItems(resourcesStore.resources)
})

onMounted(async () => {
  try {
    await tagsStore.fetchTags(0, 50)
    availableTags.value = [...tagsStore.tagKeys]
  } catch {
    availableTags.value = []
  }

  try {
    await groupsStore.fetchApplicationGroups(0, 50)
  } catch {
    // Keep an empty list if groups cannot be loaded.
  }

  try {
    await resourcesStore.fetchResources(0, 50)
  } catch {
    // Keep an empty list if resources cannot be loaded.
  }
})

function validate(): boolean {
  errorMessage.value = ''
  if (!form.descriptorFile) {
    errorMessage.value = 'Le fichier Boutiques (.json) est requis.'
    return false
  }
  return true
}

function onFileChange(event: Event) {
  const input = event.target as HTMLInputElement
  const file = input.files?.[0] ?? null
  form.descriptorFile = file
  checksDone.value = false
  descriptorCheckErrors.value = []
  parsedAppName.value = ''
  parsedVersion.value = ''
  descriptorText.value = ''
  appExists.value = false
  versionExists.value = false
  shouldOverwrite.value = false
  selectedGroupNames.value = []
  selectedResourceNames.value = []
}

async function checkDescriptorAndExistence() {
  if (!validate() || !form.descriptorFile) return

  isChecking.value = true
  errorMessage.value = ''
  descriptorCheckErrors.value = []
  checksDone.value = false

  try {
    const raw = await form.descriptorFile.text()
    const identity = parseBoutiquesIdentity(raw)
    if (!identity.name || !identity.version) {
      descriptorCheckErrors.value = [
        "Cannot extract application name and version from the descriptor. Ensure it contains 'name' and 'version' fields.",
      ]
      return
    }

    const checkResult = await boutiquesApi.checkDescriptor(form.descriptorFile)
    if (!checkResult.valid) {
      console.error('Descriptor validation errors:', checkResult.errors)
      descriptorCheckErrors.value = checkResult.errors
      return
    }

    parsedAppName.value = identity.name
    parsedVersion.value = identity.version
    descriptorText.value = raw

    try {
      await applicationsApi.getById(identity.name)
      appExists.value = true
      selectedGroupNames.value = []
    } catch {
      appExists.value = false
    }

    try {
      versionExists.value = await appVersionsApi.exists(identity.name, identity.version)
    } catch {
      versionExists.value = false
    }

    // If the version already exists, try to fetch its data and prefill tags
    if (versionExists.value) {
      try {
        const existing = await appVersionsApi.getByVersion(identity.name, identity.version)
        const existingTagKeys = (existing.tags ?? []).map((t) => t.key).filter(Boolean)

        // Ensure available tags include existing keys
        for (const k of existingTagKeys) {
          if (!availableTags.value.includes(k)) {
            availableTags.value.push(k)
          }
        }

        // Keep available tags sorted and unique
        availableTags.value = Array.from(new Set(availableTags.value)).sort((a, b) => a.localeCompare(b))

        // Prefill selected tags with existing version's tags
        selectedTags.value = existingTagKeys
      } catch {
        // Ignore errors while fetching existing version; user can still proceed
      }
    }

    checksDone.value = true
  } catch {
    errorMessage.value = 'Impossible de vérifier le fichier Boutiques.'
  } finally {
    isChecking.value = false
  }
}

function addCustomTag() {
  const result = addCustomTagToLists(availableTags.value, selectedTags.value, customTagInput.value)
  availableTags.value = result.availableTags
  selectedTags.value = result.selectedTags
  customTagInput.value = result.normalizedInput
}

function toggleTag(name: string, checked: boolean) {
  selectedTags.value = toggleItemSelection(selectedTags.value, name, checked)
}

function toggleGroupSelection(name: string, checked: boolean) {
  selectedGroupNames.value = toggleItemSelection(selectedGroupNames.value, name, checked)
}

function toggleResourceSelection(name: string, checked: boolean) {
  selectedResourceNames.value = toggleItemSelection(selectedResourceNames.value, name, checked)
}

function goToStep2() {
  if (!canProceedToStep2.value) return
  step.value = 2
}

function backToStep1() {
  step.value = 1
}

async function onSubmit() {
  if (!checksDone.value || !parsedAppName.value || !parsedVersion.value) {
    errorMessage.value = 'First step not completed correctly.'
    return
  }

  if (versionExists.value && !shouldOverwrite.value) {
    errorMessage.value = 'The version already exists: confirm the overwrite to continue.'
    return
  }

  isSubmitting.value = true
  errorMessage.value = ''

  try {
    if (!appExists.value) {
      await applicationsApi.createOrUpdate({
        name: parsedAppName.value,
        fullName: null,
        citation: null,
        note: form.note || null,
        groups: toApplicationGroupsPayload(parsedGroups.value),
      })
    }

    const payload = {
      applicationName: parsedAppName.value,
      version: parsedVersion.value,
      descriptor: descriptorText.value,
      doi: form.doi || null,
      visible: form.visible,
      source: form.source || null,
      note: form.note || null,
      tags: toAppVersionTagsPayload(
        selectedTags.value,
        tagsStore.tags,
        parsedAppName.value,
        parsedVersion.value,
      ),
      resources: toAppVersionResourcesPayload(resourcesStore.resources, selectedResourceNames.value),
      settings: {},
    }

    if (versionExists.value) {
      await appVersionsApi.createOrUpdate(payload)
    } else {
      await appVersionsApi.create(payload)
    }

    notificationsStore.success("Application / version created successfully.")

    router.push({ name: 'application-detail', params: { name: parsedAppName.value } })
  } catch {
    errorMessage.value = "Impossible de créer l'application / version."
  } finally {
    isSubmitting.value = false
  }
}
</script>

<template>
  <div class="mx-auto max-w-3xl space-y-6">
    <h1 class="text-2xl font-bold text-gray-900">
      Create a new application
    </h1>
    <p class="text-sm text-gray-500">
      First step: upload your Boutiques descriptor and check its validity and the existence of the application/version. Second step: provide additional metadata and confirm the creation.
    </p>

    <AppCard v-if="step === 1" padding>
      <div class="space-y-5">
        <div>
          <label class="block text-sm font-medium text-gray-700">
            Boutiques descriptor (.json)
          </label>
          <input
            type="file"
            accept="application/json,.json"
            class="mt-1 block w-full rounded-lg border border-gray-300 px-3 py-2 text-sm"
            @change="onFileChange"
          />
        </div>

        <AppButton variant="primary" :loading="isChecking" @click="checkDescriptorAndExistence">
          Check descriptor & existence
        </AppButton>

        <div v-if="descriptorCheckErrors.length" class="rounded-lg border border-red-200 bg-red-50 p-3 text-sm text-red-700">
          <p class="font-semibold">Validation failed</p>
          <ul class="mt-2 list-disc pl-5">
            <li v-for="err in descriptorCheckErrors" :key="err">{{ err }}</li>
          </ul>
        </div>

        <div v-if="checksDone" class="space-y-3 rounded-lg border border-gray-200 bg-gray-50 p-4 text-sm text-gray-700">
          <p><span class="font-semibold">Application:</span> {{ parsedAppName }}</p>
          <p><span class="font-semibold">Version:</span> {{ parsedVersion }}</p>
          <p v-if="!appExists" class="text-blue-700">
            The application does not exist yet: a new application will be created with this version.
          </p>
          <p v-else class="text-green-700">
            The application exists already: the version will be associated with this application.
          </p>
          <p v-if="versionExists" class="text-red-700">
            This version already exists. Confirm the overwrite to continue.
          </p>

          <label v-if="versionExists" class="flex items-center gap-2 text-sm">
            <input
              v-model="shouldOverwrite"
              type="checkbox"
              class="h-4 w-4 rounded border-gray-300 text-primary-600"
            />
            I accept to overwrite the existing version.
          </label>
        </div>

        <p v-if="errorMessage" class="text-sm text-red-600">
          {{ errorMessage }}
        </p>

        <div class="flex justify-end">
          <AppButton variant="primary" :disabled="!canProceedToStep2" @click="goToStep2">
            Next step
          </AppButton>
        </div>
      </div>
    </AppCard>

    <AppCard v-else padding>
      <form class="space-y-5" @submit.prevent="onSubmit">
        <div class="rounded-lg border border-gray-200 bg-gray-50 p-4 text-sm text-gray-700">
          <p><span class="font-semibold">Application:</span> {{ parsedAppName }}</p>
          <p><span class="font-semibold">Version:</span> {{ parsedVersion }}</p>
        </div>

        <AppInput v-model="form.doi" label="DOI (optionnel)" placeholder="10.xxxx/xxxxx" />
        <AppInput v-model="form.source" label="Source (optionnel)" placeholder="Ex : GitHub, Zenodo..." />

        <div class="flex items-center gap-2">
          <input
            id="visible"
            v-model="form.visible"
            type="checkbox"
            class="h-4 w-4 rounded border-gray-300 text-primary-600 focus:ring-primary-500"
          />
          <label for="visible" class="text-sm text-gray-700">
            Public application (visible to all users, not only to the creator and specified groups)
          </label>
        </div>

        <div>
          <label class="block text-sm font-medium text-gray-700">Tags</label>
          <div class="mt-2 grid grid-cols-1 gap-2 sm:grid-cols-2">
            <label
              v-for="tag in availableTags"
              :key="tag"
              class="flex items-center gap-2 rounded-md border border-gray-200 bg-white px-3 py-2 text-sm"
            >
              <input
                :checked="selectedTags.includes(tag)"
                type="checkbox"
                class="h-4 w-4 rounded border-gray-300 text-primary-600"
                @change="toggleTag(tag, ($event.target as HTMLInputElement).checked)"
              />
              {{ tag }}
            </label>
          </div>

          <div class="mt-3 flex gap-2">
            <AppInput v-model="customTagInput" placeholder="Ajouter un tag personnalisé" />
            <AppButton type="button" variant="secondary" @click="addCustomTag">
              Ajouter
            </AppButton>
          </div>
        </div>

        <div v-if="!appExists">
          <label class="block text-sm font-medium text-gray-700">
            Groups
          </label>

          <div v-if="groupSelectionItems.length" class="mt-2 space-y-2">
            <label
              v-for="group in groupSelectionItems"
              :key="group.name"
              class="flex items-center justify-between gap-3 rounded-md border border-gray-200 px-3 py-2"
            >
              <span class="flex items-center gap-2 text-sm text-gray-700">
                <input
                  :checked="selectedGroupNames.includes(group.name)"
                  type="checkbox"
                  class="h-4 w-4 rounded border-gray-300 text-primary-600"
                  :disabled="!group.selectable"
                  @change="toggleGroupSelection(group.name, ($event.target as HTMLInputElement).checked)"
                />
                <span>{{ group.name }}</span>
              </span>

              <span
                v-if="!group.selectable"
                class="text-xs font-medium text-gray-500"
              >
                {{ group.disabledReason }}
              </span>
            </label>
          </div>

          <p v-else class="mt-2 text-sm text-gray-500">
            No groups available from API.
          </p>
        </div>

        <div>
          <label class="block text-sm font-medium text-gray-700">
            Resources
          </label>

          <div v-if="resourceSelectionItems.length" class="mt-2 space-y-2">
            <label
              v-for="resource in resourceSelectionItems"
              :key="resource.name"
              class="flex items-center justify-between gap-3 rounded-md border border-gray-200 px-3 py-2"
            >
              <span class="flex items-center gap-2 text-sm text-gray-700">
                <input
                  :checked="selectedResourceNames.includes(resource.name)"
                  type="checkbox"
                  class="h-4 w-4 rounded border-gray-300 text-primary-600"
                  :disabled="!resource.selectable"
                  @change="toggleResourceSelection(resource.name, ($event.target as HTMLInputElement).checked)"
                />
                <span>{{ resource.name }}</span>
              </span>

              <span
                v-if="!resource.selectable"
                class="text-xs font-medium text-gray-500"
              >
                {{ resource.disabledReason }}
              </span>
            </label>
          </div>

          <p v-else class="mt-2 text-sm text-gray-500">
            No resources available from API.
          </p>
        </div>

        <div>
          <label class="block text-sm font-medium text-gray-700">
            Note (optionnelle)
          </label>
          <textarea
            v-model="form.note"
            rows="3"
            class="mt-1 block w-full rounded-lg border border-gray-300 px-3 py-2 text-sm focus:border-primary-500 focus:outline-none focus:ring-2 focus:ring-primary-500 focus:ring-offset-0"
            placeholder="Informations complémentaires sur cette version"
          />
        </div>

        <p v-if="errorMessage" class="text-sm text-red-600">
          {{ errorMessage }}
        </p>

        <div class="flex justify-between">
          <AppButton type="button" variant="ghost" @click="backToStep1">
            Retour à l'étape 1
          </AppButton>
          <AppButton
            type="submit"
            variant="primary"
            :loading="isSubmitting"
          >
            Valider la création
          </AppButton>
        </div>
      </form>
    </AppCard>
  </div>
</template>

