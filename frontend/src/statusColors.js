// Phase 7: single source for the app's fixed status palette (never
// themed, reserved for good/warning/serious state - see the project's
// dataviz reference). Previously the same three hex values were
// hand-typed separately in matchSeverity.js (score meter/dot) and
// SkillSection.jsx (per-result icon) - same colors, two places to keep
// in sync by hand. Both now import from here instead.
export const STATUS_COLORS = {
  good: '#0ca30c',
  warning: '#fab219',
  serious: '#ec835a',
}
