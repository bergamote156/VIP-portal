import axios from 'axios'
import { getBackendBase, getFrontendBase } from '@/utils/path'

const BASE_URL = import.meta.env.PROD ? getBackendBase() : ''

function readCookie(name: string): string | null {
  const match = document.cookie.match(new RegExp(`(?:^|;\\s*)${name}=([^;]*)`))
  return match?.[1] ? decodeURIComponent(match[1]) : null
}

/**
 * Spring Security 6 utilise XorCsrfTokenRequestAttributeHandler (protection BREACH).
 * Le token brut du cookie doit être XOR-encodé avant d'être envoyé dans le header.
 */
function encodeXsrfToken(rawToken: string): string {
  const encoder = new TextEncoder()
  const tokenBytes = encoder.encode(rawToken)

  const randomBytes = new Uint8Array(tokenBytes.length)
  crypto.getRandomValues(randomBytes)

  const xored = new Uint8Array(tokenBytes.length)
  for (let i = 0; i < tokenBytes.length; i++) {
    xored[i] = randomBytes[i]! ^ tokenBytes[i]!
  }

  const combined = new Uint8Array(randomBytes.length + xored.length)
  combined.set(randomBytes)
  combined.set(xored, randomBytes.length)
  
  return btoa(String.fromCharCode(...combined))
    .replace(/\+/g, '-')
    .replace(/\//g, '_')
    .replace(/=+$/, '')
}


export const backendClient = axios.create({
  baseURL: BASE_URL,
  timeout: 30000,
  withCredentials: true,
  withXSRFToken: false,
})

function isLoginRequest(url?: string, method?: string): boolean {
  return (method || '').toUpperCase() === 'POST' && (url || '').includes('/internal/session')
}

function isSessionProbeRequest(url?: string, method?: string): boolean {
  return (method || '').toUpperCase() === 'GET' && (url || '').includes('/internal/session')
}

function isPublicGetRequest(url?: string, method?: string): boolean {
  return (method || '').toUpperCase() === 'GET' && (
    (url || '').includes('/internal/applications/public') ||
    (url || '').includes('/internal/publications/public')
  )
}

function redirectToLogin(): void {
  const base = getFrontendBase()
  const loginPath = `${base}login`

  if (window.location.pathname !== loginPath) {
    window.location.assign(loginPath)
  }
}

backendClient.interceptors.request.use((config) => {
  const method = (config.method || 'get').toUpperCase()

  const hasFormDataPayload = typeof FormData !== 'undefined' && config.data instanceof FormData
  if (hasFormDataPayload) {
    // Browser must set multipart boundary automatically for FormData.
    if (config.headers) {
      delete config.headers['Content-Type']
    }
  } else if (config.data !== undefined) {
    config.headers = config.headers || {}
    if (!config.headers['Content-Type']) {
      config.headers['Content-Type'] = 'application/json'
    }
  }

  if (['POST', 'PUT', 'DELETE', 'PATCH'].includes(method)) {
    const raw = readCookie('XSRF-TOKEN')
    if (raw) {
      config.headers['X-XSRF-TOKEN'] = encodeXsrfToken(raw)
    }
  }
  return config
})

backendClient.interceptors.response.use(
  (response) => response,
  (error) => {
    const status = error?.response?.status as number | undefined
    const url = error?.config?.url as string | undefined
    const method = error?.config?.method as string | undefined

    // If session is no longer valid (expired/missing cookies), force user back to login.
    if (status === 401 && !isLoginRequest(url, method) && !isSessionProbeRequest(url, method) && !isPublicGetRequest(url, method)) {
      redirectToLogin()
    }

    return Promise.reject(error)
  },
)