// Fixed status palette (never themed) - see the project's dataviz
// reference. Reserved for state (good/warning/serious), never reused as
// a plain categorical color.
function severity(percentage) {
  if (percentage >= 70) return { hex: '#0ca30c', label: 'Strong match' }
  if (percentage >= 40) return { hex: '#fab219', label: 'Partial match' }
  return { hex: '#ec835a', label: 'Needs work' }
}

/**
 * The report's hero figure: match percentage as a large number, plus a
 * meter bar whose fill color carries severity (good/warning/serious) -
 * the track is a flat neutral, not another hue, so the fill is the only
 * color doing work here.
 */
export default function MatchMeter({ percentage, matchedCount, totalCount }) {
  const { hex, label } = severity(percentage)
  const rounded = Math.round(percentage)

  return (
    <section className="rounded-lg border border-slate-200 bg-white p-5">
      <p className="text-5xl font-semibold text-slate-900">{rounded}%</p>

      {/* Identity comes from the dot swatch, not the text color - text
          stays in a neutral ink so it's never low-contrast. */}
      <p className="mt-1 flex items-center gap-1.5 text-sm font-medium text-slate-600">
        <span
          className="h-2 w-2 rounded-full"
          style={{ backgroundColor: hex }}
          aria-hidden="true"
        />
        {label}
      </p>

      <p className="mt-1 text-sm text-slate-500">
        {matchedCount} of {totalCount} required skills found in your resume.
      </p>

      <div
        role="img"
        aria-label={`${rounded} percent match: ${matchedCount} of ${totalCount} required skills found`}
        className="mt-4 h-3 w-full overflow-hidden rounded-full bg-slate-200"
      >
        <div
          className="h-full rounded-full"
          style={{ width: `${rounded}%`, backgroundColor: hex }}
        />
      </div>
    </section>
  )
}
