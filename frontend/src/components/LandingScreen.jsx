const STEPS = [
  {
    title: 'Upload Resume',
    body: "PDF upload, or paste your resume text directly - whichever's easier.",
    icon: (
      <path d="M12 3v12m0-12l-4 4m4-4l4 4M4 17v2a2 2 0 002 2h12a2 2 0 002-2v-2" />
    ),
  },
  {
    title: 'Paste Job Description',
    body: "Drop in the JD for the exact role you're targeting.",
    icon: (
      <path d="M9 12h6m-6 4h6M9 8h1M5 21h14a2 2 0 002-2V7l-5-5H5a2 2 0 00-2 2v15a2 2 0 002 2z" />
    ),
  },
  {
    title: 'Get Your Gap Report',
    body: 'Matched, missing, and underemphasized skills - ranked and explained.',
    icon: <path d="M9 19V6l7 6-7 7z" />,
  },
]

/**
 * Phase 11: the marketing/entry screen shown to a logged-out visitor
 * before Auth. Purely static/illustrative - the "72% Match Score" card
 * is decorative, not real data.
 *
 * Phase 12: stays outside the app shell (no sidebar/top bar for a
 * logged-out visitor) - restyled to the Design System v2 tokens, with
 * the gradient button/icon fills replaced by solid accent per the
 * "no gradients" rule.
 */
export default function LandingScreen({ onStart }) {
  return (
    <div className="mx-auto max-w-3xl px-4 pb-16 pt-14 text-center">
      <span
        className="animate-pop-in mb-6 inline-flex items-center gap-1.5 rounded-full border border-border px-3.5 py-1.5 text-xs font-bold text-accent opacity-0"
        style={{ animationDelay: '30ms' }}
      >
        ✦ Built entirely in Java — Spring Boot + React
      </span>

      <h1
        className="animate-pop-in mb-4 text-[32px] font-extrabold leading-[1.15] tracking-tight text-text-primary opacity-0 sm:text-[42px]"
        style={{ animationDelay: '100ms' }}
      >
        See exactly which skills stand
        <br />
        between you and <span className="text-accent">the job.</span>
      </h1>

      <p
        className="animate-pop-in mx-auto mb-8 max-w-xl text-base leading-relaxed text-text-secondary opacity-0"
        style={{ animationDelay: '170ms' }}
      >
        Upload your resume and paste a job description — get a clear, explainable report on
        what matches, what's missing, and what to fix first.
      </p>

      <button
        type="button"
        onClick={onStart}
        className="group animate-pop-in inline-flex items-center gap-2 rounded-md bg-accent px-6 py-3.5 text-sm font-bold text-white opacity-0 transition-colors hover:bg-accent-hover focus:outline-none focus:ring-2 focus:ring-inset focus:ring-white active:scale-[0.97]"
        style={{ animationDelay: '240ms' }}
      >
        <svg
          className="h-4 w-4 transition-transform group-hover:translate-x-0.5"
          viewBox="0 0 24 24"
          fill="none"
          stroke="currentColor"
          strokeWidth="2.4"
          strokeLinecap="round"
          aria-hidden="true"
        >
          <path d="M13 5l7 7-7 7M5 12h15" />
        </svg>
        Start Free Analysis
      </button>

      <div
        className="animate-pop-in animate-float-y mx-auto mt-11 flex max-w-md items-center gap-4 rounded-lg border border-border bg-surface p-5 text-left opacity-0"
        style={{ animationDelay: '310ms' }}
      >
        <svg width="52" height="52" viewBox="0 0 120 120" aria-hidden="true">
          <circle cx="60" cy="60" r="52" fill="none" stroke="var(--color-border-strong)" strokeWidth="12" />
          <circle
            cx="60"
            cy="60"
            r="52"
            fill="none"
            stroke="var(--color-accent)"
            strokeWidth="12"
            strokeLinecap="round"
            strokeDasharray="235.2 326.7"
            transform="rotate(-90 60 60)"
          />
        </svg>
        <div>
          <div className="text-xs font-semibold text-text-muted">Backend Developer @ Acme Corp</div>
          <div className="text-xl font-extrabold text-text-primary">72% Match Score</div>
        </div>
      </div>

      <div className="mx-auto mt-14 grid max-w-3xl grid-cols-1 gap-4 sm:grid-cols-3">
        {STEPS.map((step, index) => (
          <div
            key={step.title}
            className="group/step animate-pop-in rounded-lg border border-border bg-surface p-6 text-left opacity-0 transition-colors hover:border-border-strong"
            style={{ animationDelay: `${50 + index * 90}ms` }}
          >
            <div className="mb-4 flex h-10 w-10 items-center justify-center rounded-md border border-border text-accent transition-transform duration-200 group-hover/step:-rotate-6 group-hover/step:scale-110">
              <svg
                width="18"
                height="18"
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                strokeWidth="2.2"
                strokeLinecap="round"
                strokeLinejoin="round"
                aria-hidden="true"
              >
                {step.icon}
              </svg>
            </div>
            <h3 className="mb-1.5 text-sm font-semibold text-text-primary">{step.title}</h3>
            <p className="text-xs leading-relaxed text-text-muted">{step.body}</p>
          </div>
        ))}
      </div>
    </div>
  )
}
