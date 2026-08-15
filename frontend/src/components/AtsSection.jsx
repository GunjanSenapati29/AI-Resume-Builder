import { GoodIcon, WarningIcon, CriticalIcon } from './StatusIcons'
import { STATUS_COLORS } from '../statusColors'

// Reuses the exact same icon/color/row-list pattern SkillSection.jsx
// uses for skill chips (Phase 12) - same StatusIcons, same
// STATUS_COLORS, same bordered-row-with-colored-left-edge shape - so an
// ATS issue and a skill result read as the same visual language.
const SEVERITY_VARIANTS = {
  PASSED: { Icon: GoodIcon, hex: STATUS_COLORS.good, label: 'Passed' },
  MEDIUM: { Icon: WarningIcon, hex: STATUS_COLORS.warning, label: 'Medium' },
  HIGH: { Icon: CriticalIcon, hex: STATUS_COLORS.critical, label: 'High' },
}

function scoreStatus(score) {
  if (score >= 80) return { hex: STATUS_COLORS.good, label: 'Strong' }
  if (score >= 50) return { hex: STATUS_COLORS.warning, label: 'Needs work' }
  return { hex: STATUS_COLORS.critical, label: 'Poor' }
}

function IssueRow({ issue }) {
  const { Icon, hex, label } = SEVERITY_VARIANTS[issue.severity]

  return (
    <li
      style={{ borderLeftColor: hex }}
      className="flex items-start gap-2.5 rounded-sm border border-border border-l-[3px] bg-surface px-3.5 py-2.5 text-sm"
    >
      <Icon className="mt-0.5 h-[18px] w-[18px] flex-none" style={{ color: hex }} />
      <div className="min-w-0 flex-1">
        <div className="flex flex-wrap items-center gap-2">
          <span className="font-semibold text-text-primary">{issue.title}</span>
          <span
            className="rounded-full border px-2 py-0.5 text-[10px] font-bold uppercase tracking-wide"
            style={{ borderColor: hex, color: hex }}
          >
            {label}
          </span>
        </div>
        <p className="mt-0.5 text-xs font-normal text-text-muted">{issue.description}</p>
        {issue.fixSuggestion && (
          <p className="mt-1 text-xs font-normal text-text-secondary">
            <span className="font-semibold text-text-primary">Fix: </span>
            {issue.fixSuggestion}
          </p>
        )}
      </div>
    </li>
  )
}

/**
 * Phase 13: the ATS Compatibility section of the Gap Report - a 0-100
 * score plus every rule-based check AtsAnalyzerService ran (passed ones
 * included), each with its own plain-language "why" and, for a failing
 * check, a suggested fix. Sits below the existing Matched/Missing/
 * Underemphasized columns; doesn't read or change anything about them.
 */
export default function AtsSection({ atsScore, atsIssues }) {
  const status = scoreStatus(atsScore)

  return (
    <section className="rounded-lg border border-border bg-surface p-5">
      <div className="mb-4 flex flex-wrap items-center justify-between gap-3">
        <div>
          <h2 className="text-sm font-bold text-text-primary">ATS Compatibility</h2>
          <p className="text-xs text-text-muted">How easily an applicant tracking system can read this resume</p>
        </div>
        <div className="flex items-center gap-2">
          <span className="font-mono text-2xl font-bold text-text-primary">{atsScore}</span>
          <span className="font-mono text-sm text-text-muted">/100</span>
          <span
            className="ml-1 rounded-full border px-2.5 py-1 text-xs font-bold"
            style={{ borderColor: status.hex, color: status.hex }}
          >
            {status.label}
          </span>
        </div>
      </div>

      {!atsIssues || atsIssues.length === 0 ? (
        <p className="text-sm text-text-muted">No ATS checks are available for this report.</p>
      ) : (
        <ul className="flex flex-col gap-2">
          {atsIssues.map((issue) => (
            <IssueRow key={issue.title} issue={issue} />
          ))}
        </ul>
      )}
    </section>
  )
}
