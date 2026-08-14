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
 * before Auth - not part of the original build order (Phases 1-10
 * never had a Landing screen; App.jsx went straight to Auth). Purely
 * static/illustrative - the "72% Match Score" card is decorative, not
 * real data, matching design-reference.html's own mockup.
 */
export default function LandingScreen({ onStart }) {
  return (
    <div className="mx-auto max-w-3xl px-4 pb-16 pt-14 text-center">
      <span
        className="animate-pop-in mb-6 inline-flex items-center gap-1.5 rounded-full bg-accent-tint px-3.5 py-1.5 text-xs font-bold text-accent-dark opacity-0"
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
        between you and{' '}
        <span className="bg-gradient-to-br from-accent to-violet bg-clip-text text-transparent">
          the job.
        </span>
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
        className="group animate-pop-in inline-flex items-center gap-2 rounded-md bg-gradient-to-br from-accent to-violet px-6 py-3.5 text-sm font-bold text-white opacity-0 shadow-[0_8px_18px_rgba(42,110,224,0.28)] transition-[transform,box-shadow] hover:-translate-y-0.5 hover:shadow-[0_12px_24px_rgba(42,110,224,0.36)] focus:outline-none focus:ring-2 focus:ring-inset focus:ring-white active:scale-[0.97]"
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
        className="animate-pop-in animate-float-y mx-auto mt-11 flex max-w-md items-center gap-4 rounded-lg border border-border bg-surface-1 p-5 text-left opacity-0 shadow-sm transition-shadow hover:shadow-md"
        style={{ animationDelay: '310ms' }}
      >
        <svg width="52" height="52" viewBox="0 0 120 120" aria-hidden="true">
          <circle cx="60" cy="60" r="52" fill="none" stroke="#e6e4dc" strokeWidth="12" />
          <circle
            cx="60"
            cy="60"
            r="52"
            fill="none"
            stroke="#2a6ee0"
            strokeWidth="12"
            strokeLinecap="round"
            strokeDasharray="235.2 326.7"
            transform="rotate(-90 60 60)"
          />
        </svg>
        <div>
          <div className="text-xs font-semibold text-text-muted">Backend Developer @ Acme Corp</div>
          <div className="font-display text-xl font-extrabold text-text-primary">72% Match Score</div>
        </div>
      </div>

      <div className="mx-auto mt-14 grid max-w-3xl grid-cols-1 gap-4 sm:grid-cols-3">
        {STEPS.map((step, index) => (
          <div
            key={step.title}
            className="group/step animate-pop-in rounded-lg border border-border bg-surface-1 p-6 text-left opacity-0 shadow-sm transition-shadow hover:shadow-md"
            style={{ animationDelay: `${50 + index * 90}ms` }}
          >
            <div className="mb-4 flex h-10 w-10 items-center justify-center rounded-md bg-accent-tint text-accent-dark transition-transform duration-200 group-hover/step:-rotate-6 group-hover/step:scale-110">
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
