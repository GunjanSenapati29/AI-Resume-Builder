import { STATUS_COLORS } from '../statusColors'

// Phase 14: same bordered-row-with-colored-left-edge + badge pattern as
// AtsSection.jsx's IssueRow, so a skill's evidence result reads as the
// same visual language as an ATS check. Moderate isn't one of the fixed
// good/warning/critical status colors (see STATUS_COLORS), so it uses
// the theme's accent token directly via CSS var - same approach
// LandingScreen.jsx already uses for accent-colored inline styles - and
// No Evidence uses the muted text token rather than a status color,
// since it isn't meant to alarm the way a critical/warning color would.
const EVIDENCE_VARIANTS = {
  STRONG: { label: 'Strong', hex: STATUS_COLORS.good },
  MODERATE: { label: 'Moderate', hex: 'var(--color-accent)' },
  WEAK: { label: 'Weak', hex: STATUS_COLORS.warning },
  NO_EVIDENCE: { label: 'No Evidence', hex: 'var(--color-text-muted)' },
}

const SECTION_LABELS = [
  { key: 'inSkillsSection', label: 'Skills' },
  { key: 'inProjectsSection', label: 'Projects' },
  { key: 'inExperienceSection', label: 'Experience' },
]

function sectionsFoundIn(evidence) {
  const found = SECTION_LABELS.filter(({ key }) => evidence[key]).map(({ label }) => label)
  return found.length > 0 ? found.join(', ') : 'Not found in any detected section'
}

function EvidenceRow({ evidence }) {
  const { label, hex } = EVIDENCE_VARIANTS[evidence.evidenceLevel]

  return (
    <li
      style={{ borderLeftColor: hex }}
      className="flex items-start gap-2.5 rounded-sm border border-border border-l-[3px] bg-surface px-3.5 py-2.5 text-sm"
    >
      <div className="min-w-0 flex-1">
        <div className="flex flex-wrap items-center gap-2">
          <span className="font-semibold text-text-primary">{evidence.skillName}</span>
          <span
            className="rounded-full border px-2 py-0.5 text-[10px] font-bold uppercase tracking-wide"
            style={{ borderColor: hex, color: hex }}
          >
            {label}
          </span>
        </div>
        {/* The "why" - which sections actually back this skill up, not just the label. */}
        <p className="mt-0.5 text-xs font-normal text-text-muted">Found in: {sectionsFoundIn(evidence)}</p>
      </div>
    </li>
  )
}

/**
 * Phase 14: the Skill Evidence section of the Gap Report - one row per
 * matched skill showing how well it's backed up across the resume's
 * Skills/Projects/Experience sections (Strong = all three, Moderate =
 * two, Weak = one, No Evidence = none). Sits below ATS Compatibility;
 * doesn't read or change anything about the matched/missing/
 * underemphasized columns or the ATS section above it.
 */
export default function SkillEvidenceSection({ skillEvidence }) {
  return (
    <section className="rounded-lg border border-border bg-surface p-5">
      <div className="mb-4">
        <h2 className="text-sm font-bold text-text-primary">Skill Evidence</h2>
        <p className="text-xs text-text-muted">
          How well each matched skill is backed up across your Skills, Projects, and Experience sections
        </p>
      </div>

      {!skillEvidence || skillEvidence.length === 0 ? (
        <p className="text-sm text-text-muted">No matched skills to evaluate for evidence.</p>
      ) : (
        <ul className="flex flex-col gap-2">
          {skillEvidence.map((evidence) => (
            <EvidenceRow key={evidence.skillName} evidence={evidence} />
          ))}
        </ul>
      )}
    </section>
  )
}
