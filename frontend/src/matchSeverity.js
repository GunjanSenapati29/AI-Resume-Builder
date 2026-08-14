import { STATUS_COLORS } from './statusColors'

// Reserved for state (good/warning/serious), never reused as a plain
// categorical color. Shared between MatchMeter (the Gap Report hero
// figure) and HistoryScreen (the list's match % swatch) so both use the
// exact same thresholds and colors.
export function matchSeverity(percentage) {
  if (percentage >= 70) return { hex: STATUS_COLORS.good, label: 'Strong match' }
  if (percentage >= 40) return { hex: STATUS_COLORS.warning, label: 'Partial match' }
  return { hex: STATUS_COLORS.serious, label: 'Needs work' }
}
