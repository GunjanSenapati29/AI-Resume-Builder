// Phase 7: single source for the app's fixed status palette (never
// themed, reserved for good/warning/critical state - see the project's
// dataviz reference). Used by matchSeverity.js (score meter/dot) and
// SkillSection.jsx (per-result icon) so both stay in sync.
//
// Phase 11: values now match design-reference.html's palette exactly
// (also registered as --color-good/warning/critical in index.css) so
// the same "warning" hue is used whether it's a status dot, a chip
// tint, or a history match-percent badge - previously this file had
// its own slightly different oranges/yellows than the rest of the app.
export const STATUS_COLORS = {
  good: '#0ca30c',
  warning: '#c98500',
  critical: '#d03b3b',
}
