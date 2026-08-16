import { STATUS_COLORS } from '../statusColors'

// Phase 16: same score-badge header shape as AtsSection.jsx, plus a
// bordered-row list per component (reusing AtsSection/SkillEvidenceSection's
// row pattern) so the composite score is never shown without its "why".
const LABEL_VARIANTS = {
  EXCELLENT: { label: 'Excellent', hex: STATUS_COLORS.good },
  STRONG: { label: 'Strong', hex: STATUS_COLORS.good },
  NEEDS_WORK: { label: 'Needs Work', hex: STATUS_COLORS.warning },
  NOT_READY: { label: 'Not Ready', hex: STATUS_COLORS.critical },
}

const COMPONENTS = [
  { key: 'skillMatchScore', label: 'Skill Match', weight: '40%' },
  { key: 'atsScore', label: 'ATS Compatibility', weight: '20%' },
  { key: 'evidenceStrengthScore', label: 'Evidence Strength', weight: '20%' },
  { key: 'gapSeverityScore', label: 'Gap Severity', weight: '20%' },
]

// Same 80/50 thresholds AtsSection.jsx uses for its own score badge -
// reused here per component so a weak dimension is visibly flagged, not
// just a number lost among three others.
function componentColor(score) {
  if (score >= 80) return STATUS_COLORS.good
  if (score >= 50) return STATUS_COLORS.warning
  return STATUS_COLORS.critical
}

function ComponentRow({ label, weight, score }) {
  const hex = componentColor(score)

  return (
    <li className="flex items-center gap-3 rounded-sm border border-border bg-surface px-3.5 py-2.5 text-sm">
      <div className="min-w-0 flex-1">
        <div className="flex items-center justify-between gap-2">
          <span className="font-semibold text-text-primary">{label}</span>
          <span className="text-xs text-text-muted">weight {weight}</span>
        </div>
        <div className="mt-1.5 h-1.5 w-full overflow-hidden rounded-full bg-border">
          <div className="h-full rounded-full" style={{ width: `${score}%`, backgroundColor: hex }} />
        </div>
      </div>
      <span className="w-10 flex-none text-right font-mono text-sm font-bold text-text-primary">{score}</span>
    </li>
  )
}

/**
 * Phase 16: the Job Readiness Score section of the Gap Report - one
 * composite 0-100 score (Skill Match 40% + ATS Compatibility 20% +
 * Evidence Strength 20% + Gap Severity 20%) plus every weighted
 * component behind it, so the number is never shown without its "why".
 * Sits near the top, right below the existing Match Score hero figure;
 * doesn't read or change anything else on the report.
 */
export default function JobReadinessSection({ readiness }) {
  if (!readiness) {
    return null
  }

  const variant = LABEL_VARIANTS[readiness.label] ?? LABEL_VARIANTS.NOT_READY

  return (
    <section className="rounded-lg border border-border bg-surface p-5">
      <div className="mb-4 flex flex-wrap items-center justify-between gap-3">
        <div>
          <h2 className="text-sm font-bold text-text-primary">Job Readiness Score</h2>
          <p className="text-xs text-text-muted">
            A weighted composite of skill match, ATS compatibility, evidence strength, and gap severity
          </p>
        </div>
        <div className="flex items-center gap-2">
          <span className="font-mono text-2xl font-bold text-text-primary">{readiness.overallScore}</span>
          <span className="font-mono text-sm text-text-muted">/100</span>
          <span
            className="ml-1 rounded-full border px-2.5 py-1 text-xs font-bold"
            style={{ borderColor: variant.hex, color: variant.hex }}
          >
            {variant.label}
          </span>
        </div>
      </div>

      <ul className="flex flex-col gap-2">
        {COMPONENTS.map(({ key, label, weight }) => (
          <ComponentRow key={key} label={label} weight={weight} score={readiness[key]} />
        ))}
      </ul>
    </section>
  )
}
