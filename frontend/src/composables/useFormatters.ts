export function useFormatters() {
  const relativeFormatter = new Intl.RelativeTimeFormat('en', { numeric: 'auto' })

  function formatRelativeTime(input: string | number | Date): string {
    const date = input instanceof Date ? input : new Date(input)
    const diffMs = date.getTime() - Date.now()
    const diffSeconds = Math.round(diffMs / 1000)

    const absSeconds = Math.abs(diffSeconds)
    if (absSeconds < 60) {
      return relativeFormatter.format(diffSeconds, 'second')
    }

    const diffMinutes = Math.round(diffSeconds / 60)
    const absMinutes = Math.abs(diffMinutes)
    if (absMinutes < 60) {
      return relativeFormatter.format(diffMinutes, 'minute')
    }

    const diffHours = Math.round(diffMinutes / 60)
    const absHours = Math.abs(diffHours)
    if (absHours < 24) {
      return relativeFormatter.format(diffHours, 'hour')
    }

    const diffDays = Math.round(diffHours / 24)
    if (Math.abs(diffDays) < 7) {
      return relativeFormatter.format(diffDays, 'day')
    }

    return date.toLocaleDateString('en-US')
  }

  return { formatRelativeTime }
}
