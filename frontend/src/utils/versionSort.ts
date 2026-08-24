import semver from 'semver'

type Versioned = {
  version: string
}

function normalizeVersion(version: string): string {
  if (semver.valid(version)) return version

  const parts = version.split('.')

  if (parts.length === 2 && /^\d+$/.test(parts[0]!) && /^\d+$/.test(parts[1]!)) {
    return `${parts[0]}.${parts[1]}.0`
  }

  if (parts.length === 1 && /^\d+$/.test(parts[0]!)) {
    return `${parts[0]}.0.0`
  }

  return version
}

export function compareVersionDesc(a: string, b: string): number {
  const normalizedA = normalizeVersion(a)
  const normalizedB = normalizeVersion(b)

  const validA = semver.valid(normalizedA)
  const validB = semver.valid(normalizedB)

  if (validA && validB) {
    return semver.rcompare(validA, validB)
  }

  return b.localeCompare(a, undefined, { numeric: true, sensitivity: 'base' })
}

export function sortByVersionDesc<T extends Versioned>(versions: T[]): T[] {
  return [...versions].sort((a, b) => compareVersionDesc(a.version, b.version))
}
