import { useEffect, useState } from 'react'
import { fetchRoleRecommendations } from '../api'
import { GoodIcon } from './StatusIcons'
import { STATUS_COLORS } from '../statusColors'

// Same 80/50 thresholds JobReadinessSection.jsx uses for its own
// component score bars, reused here so a role's fit score reads with
// the same good/warning/critical meaning everywhere else in the app.
function scoreColor(score) {
  if (score >= 80) return STATUS_COLORS.good
  if (score >= 50) return STATUS_COLORS.warning
  return STATUS_COLORS.critical
}

function MinusIcon(props) {
  return (
    <svg viewBox="0 0 20 20" fill="none" aria-hidden="true" {...props}>
      <path d="M4 10h12" stroke="currentColor" strokeWidth="2" strokeLinecap="round" />
    </svg>
  )
}

function SkillPill({ skillName, matched }) {
  return (
    <li
      className={`flex items-center gap-1.5 rounded-full border px-2.5 py-1 text-xs font-semibold ${
        matched ? 'border-border text-text-primary' : 'border-border text-text-muted'
      }`}
    >
      {matched ? (
        <GoodIcon className="h-3.5 w-3.5 flex-none" style={{ color: STATUS_COLORS.good }} />
      ) : (
        <MinusIcon className="h-3.5 w-3.5 flex-none text-text-muted" />
      )}
      {skillName}
    </li>
  )
}

function RoleCard({ role }) {
  const hex = scoreColor(role.score)

  return (
    <li className="rounded-lg border border-border bg-surface p-5">
      <div className="flex flex-wrap items-center justify-between gap-3">
        <h3 className="text-sm font-bold text-text-primary">{role.roleName}</h3>
        <div className="flex items-baseline gap-1">
          <span className="font-mono text-lg font-bold text-text-primary">{role.score}</span>
          <span className="font-mono text-xs text-text-muted">% fit</span>
        </div>
      </div>

      <div className="mt-2 h-1.5 w-full overflow-hidden rounded-full bg-border">
        <div className="h-full rounded-full" style={{ width: `${role.score}%`, backgroundColor: hex }} />
      </div>

      <div className="mt-4 grid grid-cols-1 gap-4 sm:grid-cols-2">
        <div>
          <h4 className="text-xs font-bold uppercase tracking-wide text-text-muted">
            Matched ({role.matchedSkills.length})
          </h4>
          {role.matchedSkills.length === 0 ? (
            <p className="mt-2 text-xs text-text-muted">None yet.</p>
          ) : (
            <ul className="mt-2 flex flex-wrap gap-1.5">
              {role.matchedSkills.map((skillName) => (
                <SkillPill key={skillName} skillName={skillName} matched />
              ))}
            </ul>
          )}
        </div>
        <div>
          <h4 className="text-xs font-bold uppercase tracking-wide text-text-muted">
            Missing ({role.missingSkills.length})
          </h4>
          {role.missingSkills.length === 0 ? (
            <p className="mt-2 text-xs text-text-muted">Every required skill matched.</p>
          ) : (
            <ul className="mt-2 flex flex-wrap gap-1.5">
              {role.missingSkills.map((skillName) => (
                <SkillPill key={skillName} skillName={skillName} matched={false} />
              ))}
            </ul>
          )}
        </div>
      </div>
    </li>
  )
}

/**
 * Phase 20: the Career Fit page - scores the user's aggregate skill
 * profile (every skill that has ever appeared as MATCHED across ALL of
 * their GapReports, not just the most recent one - unlike Skill Roadmap
 * and Interview Prep) against a fixed catalog of role categories
 * (CareerRoleService). Each role shows its fit percentage plus exactly
 * which required skills are matched vs. missing, so the score is never
 * shown without the reasoning behind it.
 *
 * `roles` state: `undefined` while loading, `null` when the user has no
 * analyzed reports yet (a normal empty state, not an error - see
 * fetchRoleRecommendations's 204 handling), otherwise the real payload.
 */
export default function CareerFitScreen({ onGoToAnalyze }) {
  const [roles, setRoles] = useState(undefined)
  const [error, setError] = useState('')

  useEffect(() => {
    fetchRoleRecommendations()
      .then(setRoles)
      .catch((err) => setError(err.message))
  }, [])

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-xl font-bold text-text-primary">Career Fit</h1>
        <p className="mt-0.5 text-sm text-text-muted">
          How your demonstrated skills, across every resume you've analyzed, line up against common role categories
        </p>
      </div>

      {error && (
        <p className="text-sm font-medium text-critical" role="alert">
          Could not load your career fit: {error}
        </p>
      )}

      {!error && roles === undefined && <p className="text-sm text-text-muted">Loading your career fit...</p>}

      {!error && roles === null && (
        <div className="rounded-lg border border-border bg-surface p-8 text-center">
          <p className="text-sm text-text-muted">Analyze a resume first to see your career fit.</p>
          <button
            type="button"
            onClick={onGoToAnalyze}
            className="mt-4 inline-flex items-center gap-2 rounded-md bg-accent px-5 py-2.5 text-sm font-bold text-white transition-colors hover:bg-accent-hover focus:outline-none focus:ring-2 focus:ring-inset focus:ring-white active:scale-[0.97]"
          >
            Go to Analyze Resume
          </button>
        </div>
      )}

      {!error && roles && (
        <ul className="grid grid-cols-1 gap-4 lg:grid-cols-2">
          {roles.roles.map((role) => (
            <RoleCard key={role.roleName} role={role} />
          ))}
        </ul>
      )}
    </div>
  )
}
