import { afterEach, vi } from 'vitest'

// Keep tests isolated from each other.
afterEach(() => {
  vi.restoreAllMocks()
})
