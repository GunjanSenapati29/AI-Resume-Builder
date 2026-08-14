import { matchSeverity } from '../matchSeverity'
import { STATUS_COLORS } from '../statusColors'
import { useCountUp } from '../useCountUp'

const RADIUS = 52
const CIRCUMFERENCE = 2 * Math.PI * RADIUS

/**
 * The report's hero figure - design-reference.html's "summary-strip":
 * an animated donut for the overall match percentage, plus a dot+count
 * for each of the three categories below it (Matched/Missing/
 * Underemphasized), so the split is visible before reading a single chip.
 *
 * Phase 6c (original): a flat percentage + linear bar.
 * Phase 11: replaced with the donut, and the ring/number/three counts
 * all animate in via useCountUp - every render frame recomputes the
 * ring's stroke-dasharray from that same animated value, so the ring
 * and the "72%" text can never drift out of sync with each other.
 */
export default function MatchMeter({ percentage, matchedCount, missingCount, underemphasizedCount }) {
  const { hex, label } = matchSeverity(percentage)
  const rounded = Math.round(percentage)
  const animatedPct = useCountUp(rounded, 1100)
  const animatedMatched = useCountUp(matchedCount, 900)
  const animatedMissing = useCountUp(missingCount, 900)
  const animatedUnderemphasized = useCountUp(underemphasizedCount, 900)

  const dashLength = (animatedPct / 100) * CIRCUMFERENCE

  return (
    <section className="flex flex-wrap items-center gap-10 rounded-lg border border-border bg-gradient-to-br from-surface-1 to-[#f8fbff] p-7 shadow-sm">
      <div className="flex items-center gap-4">
        <svg
          width="104"
          height="104"
          viewBox="0 0 120 120"
          role="img"
          aria-label={`${rounded} percent match: ${matchedCount} of ${matchedCount + missingCount} required skills found`}
        >
          <circle cx="60" cy="60" r={RADIUS} fill="none" stroke="#e6e4dc" strokeWidth="12" />
          <circle
            cx="60"
            cy="60"
            r={RADIUS}
            fill="none"
            stroke={hex}
            strokeWidth="12"
            strokeLinecap="round"
            strokeDasharray={`${dashLength.toFixed(1)} ${CIRCUMFERENCE.toFixed(1)}`}
            transform="rotate(-90 60 60)"
          />
          <text
            x="60"
            y="68"
            textAnchor="middle"
            className="font-display"
            fontSize="24"
            fontWeight="800"
            fill="#12120f"
          >
            {animatedPct}%
          </text>
        </svg>
        <div className="text-sm text-text-secondary">
          Overall
          <br />
          <strong className="font-display text-lg text-text-primary">Match Score</strong>
          <div className="mt-1 flex items-center gap-1.5 font-sans text-xs font-semibold" style={{ color: hex }}>
            {label}
          </div>
        </div>
      </div>

      <div className="flex flex-wrap gap-7">
        <CountItem hex={STATUS_COLORS.good} value={animatedMatched} label="Matched" />
        <CountItem hex={STATUS_COLORS.critical} value={animatedMissing} label="Missing" />
        <CountItem hex={STATUS_COLORS.warning} value={animatedUnderemphasized} label="Underemphasized" />
      </div>
    </section>
  )
}

function CountItem({ hex, value, label }) {
  return (
    <div className="flex items-center gap-2.5 text-sm text-text-secondary">
      <span className="h-2.5 w-2.5 flex-none rounded-full" style={{ backgroundColor: hex }} aria-hidden="true" />
      <span className="font-display text-lg font-extrabold text-text-primary">{value}</span>
      {label}
    </div>
  )
}
