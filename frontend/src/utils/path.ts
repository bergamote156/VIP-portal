export function getFrontendBase(): string {
  if (typeof window === 'undefined') return '/'
  const path = window.location.pathname
  const idx = path.indexOf('/new_front')
  if (idx === -1) return '/'
  const end = path.indexOf('/', idx + 1)
  return end !== -1 ? path.slice(0, end + 1) : path.slice(0, idx + '/new_front'.length) + '/'
}

export function getBackendBase(): string {
  return getFrontendBase().replace(/new_front\/$/, '')
}
