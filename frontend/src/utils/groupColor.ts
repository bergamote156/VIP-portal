export const GROUP_BADGE_COLORS = [
  'red',
  'orange',
  'amber',
  'yellow',
  'lime',
  'green',
  'emerald',
  'teal',
  'cyan',
  'sky',
  'blue',
  'indigo',
  'violet',
  'purple',
  'fuchsia',
  'pink',
  'rose',
] as const

export type GroupBadgeColor = (typeof GROUP_BADGE_COLORS)[number]

export function getGroupBadgeColor(text: string): GroupBadgeColor {
  const hash = Array.from(text).reduce((acc, char) => acc + char.charCodeAt(0), 0)
  return GROUP_BADGE_COLORS[hash % GROUP_BADGE_COLORS.length] ?? 'blue'
}
