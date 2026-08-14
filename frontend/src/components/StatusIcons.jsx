/**
 * Small status icons for the Gap Report sections. Plain inline SVG (no
 * icon library dependency) - each is paired with a text label wherever
 * it's used, per the "status color never carries meaning alone" rule:
 * the icon reinforces, the text label is what actually says it.
 */
export function GoodIcon(props) {
  return (
    <svg viewBox="0 0 20 20" fill="none" aria-hidden="true" {...props}>
      <circle cx="10" cy="10" r="8.5" stroke="currentColor" strokeWidth="1.5" />
      <path
        d="M6.5 10.5l2.2 2.2 4.8-5.4"
        stroke="currentColor"
        strokeWidth="1.5"
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
        d="M10 3.2l7.8 13.5a1 1 0 01-.87 1.5H3.07a1 1 0 01-.87-1.5L10 3.2z"
        stroke="currentColor"
        strokeWidth="1.5"
        strokeLinejoin="round"
      />
      <path d="M10 8.2v3.6" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" />
      <circle cx="10" cy="14.4" r="0.9" fill="currentColor" />
    </svg>
  )
}

/** Neutral "not present" mark - deliberately a dash, not an X, so a
 * missing skill reads as "to do" rather than "error". */
export function SeriousIcon(props) {
  return (
    <svg viewBox="0 0 20 20" fill="none" aria-hidden="true" {...props}>
      <circle cx="10" cy="10" r="8.5" stroke="currentColor" strokeWidth="1.5" />
      <path d="M6.5 10h7" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" />
    </svg>
  )
}
