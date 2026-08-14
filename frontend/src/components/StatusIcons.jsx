/**
 * Small status icons for the Gap Report sections. Plain inline SVG (no
 * icon library dependency) - each is paired with a text label wherever
 * it's used, per the "status color never carries meaning alone" rule:
 * the icon reinforces, the text label is what actually says it.
 *
 * Phase 11: icon choices now match design-reference.html - WarningIcon
 * is an eye (Underemphasized = "look again, you undersold this"), and
 * CriticalIcon (was SeriousIcon, a deliberately neutral dash) is now a
 * triangle-exclamation, matching the reference's Missing-skills icon.
 */
export function GoodIcon(props) {
  return (
    <svg viewBox="0 0 20 20" fill="none" aria-hidden="true" {...props}>
      <path
        d="M4 10.5l4 4 8-9"
        stroke="currentColor"
        strokeWidth="2"
        strokeLinecap="round"
        strokeLinejoin="round"
      />
    </svg>
  )
}

export function WarningIcon(props) {
  return (
    <svg viewBox="0 0 20 20" fill="none" aria-hidden="true" {...props}>
      <path
        d="M1 10s3.3-5.8 9-5.8S19 10 19 10s-3.3 5.8-9 5.8S1 10 1 10z"
        stroke="currentColor"
        strokeWidth="1.6"
        strokeLinejoin="round"
      />
      <circle cx="10" cy="10" r="2.4" stroke="currentColor" strokeWidth="1.6" />
    </svg>
  )
}

export function CriticalIcon(props) {
  return (
    <svg viewBox="0 0 20 20" fill="none" aria-hidden="true" {...props}>
      <path
        d="M10 7v3.4m0 3.2h.008M8.57 2.9L1.4 15a1.6 1.6 0 001.37 2.4h14.46A1.6 1.6 0 0018.6 15L11.43 2.9a1.6 1.6 0 00-2.86 0z"
        stroke="currentColor"
        strokeWidth="1.6"
        strokeLinecap="round"
        strokeLinejoin="round"
      />
    </svg>
  )
}
