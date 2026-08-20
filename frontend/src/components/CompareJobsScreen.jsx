import { useEffect, useState } from 'react'
import { fetchReportComparison } from '../api'
import { matchSeverity } from '../matchSeverity'
import { formatDate } from '../dateFormat'
import { GoodIcon } from './StatusIcons'
import { STATUS_COLORS } from '../statusColors'

const MIN_COMPARE = 2

function MinusIcon(props) {
  return (
    <svg viewBox="0 0 20 20" fill="none" aria-hidden="true" {...props}>
      <path d="M4 10h12" stroke="currentColor" strokeWidth="2" strokeLinecap="round" />
    </svg>
  )
}

// How many of the compared reports each matched skill appears in -
// this is what lets a matched-skill chip say "every job wants this" vs
// "only this one does", which is the actual "which job am I better
// suited for and why" value, not just a bare percentage per report.
function countMatchesAcrossReports(reports) {
  const counts = {}
  reports.forEach((report) => {
    report.matchedSkills.forEach((skill) => {
      counts[skill] = (counts[skill] ?? 0) + 1
    })
  })
  return counts
}

function MatchedSkillPill({ skillName, matchCount, totalReports }) {
  const isCommon = totalReports > 1 && matchCount === totalReports
  const isUnique = totalReports > 1 && matchCount === 1

  return (
    <li className="flex items-center gap-1.5 rounded-full border border-border px-2.5 py-1 text-xs font-semibold text-text-primary">
      <GoodIcon className="h-3.5 w-3.5 flex-none" style={{ color: STATUS_COLORS.good }} />
      {skillName}
      {isCommon && (
        <span className="rounded-full bg-accent px-1.5 py-0.5 text-[9px] font-extrabold uppercase tracking-wide text-white">
          All
        </span>
      )}
      {isUnique && (
        <span className="rounded-full border border-border-strong px-1.5 py-0.5 text-[9px] font-extrabold uppercase tracking-wide text-text-muted">
          Unique
        </span>
      )}
    </li>
  )
}

function MissingSkillPill({ skillName }) {
  return (
    <li className="flex items-center gap-1.5 rounded-full border border-border px-2.5 py-1 text-xs font-semibold text-text-muted">
      <MinusIcon className="h-3.5 w-3.5 flex-none text-text-muted" />
      {skillName}
    </li>
  )
}

function JobCard({ report, rank, matchCounts, totalReports }) {
  const severity = matchSeverity(report.matchPercentage)
  const isTop = rank === 0

  return (
    <li
      className={`rounded-lg border bg-surface p-5 ${isTop ? 'border-accent' : 'border-border'}`}
    >
      <div className="flex flex-wrap items-start justify-between gap-3">
        <div className="min-w-0">
          <div className="flex flex-wrap items-center gap-2">
            <h3 className="text-sm font-bold text-text-primary">{report.label}</h3>
            {isTop && (
              <span className="flex-none rounded-full bg-accent px-2 py-0.5 text-[10px] font-extrabold uppercase tracking-wide text-white">
                Best Fit
              </span>
            )}
          </div>
          <p className="mt-0.5 text-xs text-text-muted">{formatDate(report.createdAt)}</p>
        </div>
        <div className="flex-none text-right">
          <span className="font-mono text-lg font-bold text-text-primary">
            {Math.round(report.matchPercentage)}
          </span>
          <span className="font-mono text-xs text-text-muted">%</span>
        </div>
      </div>

      <div className="mt-2 h-1.5 w-full overflow-hidden rounded-full bg-border">
        <div
          className="h-full rounded-full"
          style={{ width: `${report.matchPercentage}%`, backgroundColor: severity.hex }}
        />
      </div>
      <p className="mt-1 text-xs font-semibold" style={{ color: severity.hex }}>
        {severity.label}
      </p>

      <div className="mt-4">
        <h4 className="text-xs font-bold uppercase tracking-wide text-text-muted">
          Matched ({report.matchedSkills.length})
        </h4>
        {report.matchedSkills.length === 0 ? (
          <p className="mt-2 text-xs text-text-muted">None.</p>
        ) : (
          <ul className="mt-2 flex flex-wrap gap-1.5">
            {report.matchedSkills.map((skillName) => (
              <MatchedSkillPill
                key={skillName}
                skillName={skillName}
                matchCount={matchCounts[skillName]}
                totalReports={totalReports}
              />
            ))}
          </ul>
        )}
      </div>

      <div className="mt-4">
        <h4 className="text-xs font-bold uppercase tracking-wide text-text-muted">
          Missing ({report.missingSkills.length})
        </h4>
        {report.missingSkills.length === 0 ? (
          <p className="mt-2 text-xs text-text-muted">Every required skill matched.</p>
        ) : (
          <ul className="mt-2 flex flex-wrap gap-1.5">
            {report.missingSkills.map((skillName) => (
              <MissingSkillPill key={skillName} skillName={skillName} />
            ))}
          </ul>
        )}
      </div>
    </li>
  )
}

/**
 * Phase 21: Compare Jobs - 2 to 5 of the user's own past reports, side
 * by side, ranked by match % (highest first, the way GET
 * /api/reports/compare already returns them - see ReportService.
 * compareReports). No new scoring logic here or on the backend; this
 * reshapes data every report already has. The one thing computed
 * client-side is countMatchesAcrossReports, which drives the "All"/
 * "Unique" badges - that's the actual "which job am I better suited for
 * and why" value, not just a bare number per report.
 *
 * `reportIds` normally arrives from HistoryScreen's checkbox selection
 * (see App.jsx). Reached directly from the sidebar with nothing
 * selected yet, this shows an instructional empty state instead of
 * attempting a request that the backend would 400 on.
 */
export default function CompareJobsScreen({ reportIds, onGoToHistory }) {
  const [comparison, setComparison] = useState(undefined)
  const [error, setError] = useState('')

  useEffect(() => {
    if (!reportIds || reportIds.length < MIN_COMPARE) {
      setComparison(undefined)
      return
    }
    setComparison(undefined)
    setError('')
    fetchReportComparison(reportIds)
      .then(setComparison)
      .catch((err) => setError(err.message))
  }, [reportIds])

  const needsSelection = !reportIds || reportIds.length < MIN_COMPARE
  const matchCounts = comparison ? countMatchesAcrossReports(comparison) : {}

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-xl font-bold text-text-primary">Compare Jobs</h1>
        <p className="mt-0.5 text-sm text-text-muted">
          See which job you're the strongest fit for, and exactly why
        </p>
      </div>

      {error && (
        <p className="text-sm font-medium text-critical" role="alert">
          Could not load the comparison: {error}
        </p>
      )}

      {!error && needsSelection && (
        <div className="rounded-lg border border-border bg-surface p-8 text-center">
          <p className="text-sm text-text-muted">
            Select {MIN_COMPARE} to 5 past reports from History to compare them side by side.
          </p>
          <button
            type="button"
            onClick={onGoToHistory}
            className="mt-4 inline-flex items-center gap-2 rounded-md bg-accent px-5 py-2.5 text-sm font-bold text-white transition-colors hover:bg-accent-hover focus:outline-none focus:ring-2 focus:ring-inset focus:ring-white active:scale-[0.97]"
          >
            Go to History
          </button>
        </div>
      )}

      {!error && !needsSelection && comparison === undefined && (
        <p className="text-sm text-text-muted">Loading your comparison...</p>
      )}

      {!error && !needsSelection && comparison && (
        <ul className="grid grid-cols-1 gap-4 lg:grid-cols-2">
          {comparison.map((report, index) => (
            <JobCard
              key={report.reportId}
              report={report}
              rank={index}
              matchCounts={matchCounts}
              totalReports={comparison.length}
            />
          ))}
        </ul>
      )}
    </div>
  )
}
