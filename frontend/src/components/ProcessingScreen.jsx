import { useEffect, useState } from 'react'

const STEP_LABELS = ['Reading resume', 'Comparing against job description', 'Ranking gaps by priority']

/**
 * Phase 6b: the "Processing" screen from the roadmap - shown in place of
 * the form while POST /api/match is in flight, so submitting always gets
 * a visible state change instead of the form just sitting there.
 *
 * Phase 11: restyled with a spinning ring and a 3-step narration list.
 * The steps are illustrative, not driven by real
 * backend progress events - the actual call is one atomic request, so
 * "done"/"now"/"pending" here just narrates roughly what a match
 * involves, timed independently of when the real response arrives.
 * "Reading resume" starts already done since text extraction already
 * happened before this screen ever shows.
 */
export default function ProcessingScreen() {
  const [activeIndex, setActiveIndex] = useState(1)

  useEffect(() => {
    const timer = setTimeout(() => setActiveIndex(2), 700)
    return () => clearTimeout(timer)
  }, [])

  return (
    <section
      role="status"
      aria-live="polite"
      className="mx-auto max-w-md rounded-lg border border-border bg-surface p-12 text-center"
    >
      <span
        aria-hidden="true"
        className="mx-auto mb-6 block h-[76px] w-[76px] animate-spin rounded-full border-[5px] border-border-strong border-t-accent"
      />
      <h3 className="text-base font-bold text-text-primary">Analyzing your match…</h3>
      <p className="mt-1 text-sm text-text-muted">This usually takes a few seconds</p>

      <ul className="mt-6 text-left text-sm">
        <StepRow state="done" label={STEP_LABELS[0]} />
        <StepRow state={activeIndex === 1 ? 'now' : 'done'} label={STEP_LABELS[1]} />
        <StepRow state={activeIndex === 2 ? 'now' : 'pending'} label={STEP_LABELS[2]} />
      </ul>
    </section>
  )
}

function StepRow({ state, label }) {
  if (state === 'done') {
    return (
      <li className="flex items-center gap-2.5 py-2 font-semibold text-good">
        <svg
          className="h-4 w-4 flex-none"
          viewBox="0 0 24 24"
          fill="none"
          stroke="currentColor"
          strokeWidth="2.5"
          strokeLinecap="round"
          strokeLinejoin="round"
          aria-hidden="true"
        >
          <path d="M20 6L9 17l-5-5" />
        </svg>
        {label}
      </li>
    )
  }

  if (state === 'now') {
    return (
      <li className="flex items-center gap-2.5 py-2 font-bold text-text-primary">
        <svg
          className="h-4 w-4 flex-none"
          viewBox="0 0 24 24"
          fill="none"
          stroke="currentColor"
          strokeWidth="2.5"
          strokeLinecap="round"
          aria-hidden="true"
        >
          <circle cx="12" cy="12" r="9" />
          <path d="M12 7v5l3 3" />
        </svg>
        <span className="animate-pulse-soft">{label}</span>
      </li>
    )
  }

  return (
    <li className="flex items-center gap-2.5 py-2 text-text-secondary">
      <svg
        className="h-4 w-4 flex-none text-border-strong"
        viewBox="0 0 24 24"
        fill="none"
        stroke="currentColor"
        strokeWidth="2"
        aria-hidden="true"
      >
        <circle cx="12" cy="12" r="9" />
      </svg>
      {label}
    </li>
  )
}
