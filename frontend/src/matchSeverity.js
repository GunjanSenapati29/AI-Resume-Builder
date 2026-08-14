// Fixed status palette (never themed) - see the project's dataviz
// reference. Reserved for state (good/warning/serious), never reused as
// a plain categorical color. Shared between MatchMeter (the Gap Report
// hero figure) and HistoryScreen (the list's match % swatch) so both
// use the exact same thresholds and colors.
export function matchSeverity(percentage) {
  if (percentage >= 70) return { hex: '#0ca30c', label: 'Strong match' }
  if (percentage >= 40) return { hex: '#fab219', label: 'Partial match' }
  return { hex: '#ec835a', label: 'Needs work' }
}
