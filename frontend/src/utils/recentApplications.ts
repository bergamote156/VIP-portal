const RECENT_APPLICATIONS_STORAGE_KEY = 'vip.recentApplications'
const MAX_RECENT_APPLICATIONS = 20

export type RecentApplication = {
  name: string
  fullName?: string
  lastVersion?: string
  usedAt: string
}

function isRecentApplication(value: unknown): value is RecentApplication {
  if (!value || typeof value !== 'object') return false

  const candidate = value as Partial<RecentApplication>
  return typeof candidate.name === 'string' && typeof candidate.usedAt === 'string'
}

function readRecentApplications(): RecentApplication[] {
  if (typeof window === 'undefined') return []

  try {
    const rawValue = window.localStorage.getItem(RECENT_APPLICATIONS_STORAGE_KEY)
    if (!rawValue) return []

    const parsed = JSON.parse(rawValue)
    if (!Array.isArray(parsed)) return []

    return parsed.filter(isRecentApplication)
  } catch {
    return []
  }
}

function writeRecentApplications(applications: RecentApplication[]): void {
  if (typeof window === 'undefined') return

  window.localStorage.setItem(
    RECENT_APPLICATIONS_STORAGE_KEY,
    JSON.stringify(applications.slice(0, MAX_RECENT_APPLICATIONS)),
  )
}

export function getRecentApplications(limit = 4): RecentApplication[] {
  return readRecentApplications()
    .sort((a, b) => new Date(b.usedAt).getTime() - new Date(a.usedAt).getTime())
    .slice(0, Math.max(0, limit))
}

export function rememberRecentApplication(application: {
  name: string
  fullName?: string
  version?: string
}): void {
  const nowIso = new Date().toISOString()
  const existing = readRecentApplications()

  const deduplicated = existing.filter((item) => item.name !== application.name)
  deduplicated.unshift({
    name: application.name,
    fullName: application.fullName,
    lastVersion: application.version,
    usedAt: nowIso,
  })

  writeRecentApplications(deduplicated)
}
