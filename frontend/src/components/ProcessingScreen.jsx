/**
 * Phase 6b: the "Processing" screen from the roadmap - shown in place of
 * the form while POST /api/match is in flight, so submitting always gets
 * a visible state change instead of the form just sitting there.
 */
export default function ProcessingScreen() {
  return (
    <section
      role="status"
      aria-live="polite"
      className="flex flex-col items-center justify-center gap-4 rounded-lg border border-slate-200 bg-white p-12 text-center"
    >
      <span
        aria-hidden="true"
        className="h-10 w-10 animate-spin rounded-full border-4 border-slate-200 border-t-blue-600"
      />
      <div>
        <p className="text-base font-medium text-slate-800">
          Comparing your resume against the job description...
        </p>
        <p className="mt-1 text-sm text-slate-500">This usually takes a few seconds.</p>
      </div>
    </section>
  )
}
