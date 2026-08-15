// Phase 7: single source for the app's fixed status palette (never
// themed, reserved for good/warning/critical state - see the project's
// dataviz reference). Used by matchSeverity.js (score meter/dot) and
// SkillSection.jsx (per-result icon) so both stay in sync.
//
// Phase 12: values now match Design System v2's status palette exactly
// (also registered as --color-good/warning/critical in index.css, same
// in both themes) so the same hue is used whether it's a status dot, an
// icon, or a history match-percent badge.
export const STATUS_COLORS = {
  good: '#0e9f6e',
  warning: '#c2850c',
  critical: '#dc2626',
}
