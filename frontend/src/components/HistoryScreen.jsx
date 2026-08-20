import { useEffect, useState } from 'react'
import { fetchReportById, fetchReportHistory } from '../api'
import { matchSeverity } from '../matchSeverity'
import { formatDate } from '../dateFormat'
import GapReportScreen from './GapReportScreen'

const MAX_COMPARE = 5

// Same 70/40 thresholds as matchSeverity(), extended to a third (low)
// tier the sample data used while building this never needed to show,
// since it happened to only include high/mid scores.
function badgeClasses(percentage) {
  if (percentage >= 70) return 'text-good'
  if (percentage >= 40) return 'text-warning'
  return 'text-critical'
}

function ReportRow({ summary, onSelect, compareEnabled, checked, checkDisabled, onToggleCompare }) {
  return (
    <tr className="transition-colors hover:bg-surface-hover">
      {compareEnabled && (
        <td className="border-b border-border px-4 py-4">
          <input
            type="checkbox"
            checked={checked}
            disabled={checkDisabled}
            onChange={() => onToggleCompare(summary.id)}
            aria-label={`Select report from ${formatDate(summary.createdAt)} for comparison`}
            className="h-4 w-4 rounded-sm border-border-strong text-accent focus:outline-none focus:ring-2 focus:ring-accent disabled:cursor-not-allowed disabled:opacity-40"
          />
        </td>
      )}
      <td className="border-b border-border px-4 py-4 text-text-primary">
        <span className="block max-w-xs truncate sm:max-w-sm">{summary.jdSnippet}</span>
      </td>
      <td className="border-b border-border px-4 py-4 font-mono text-text-muted">
        {formatDate(summary.createdAt)}
      </td>
      <td className="border-b border-border px-4 py-4">
        <span className={`inline-block rounded-full border border-border-strong px-2.5 py-1 font-mono text-xs font-extrabold ${badgeClasses(summary.matchPercentage)}`}>
          {Math.round(summary.matchPercentage)}%
        </span>
      </td>
      <td className="border-b border-border px-4 py-4 text-right">
        <button
          type="button"
          onClick={() => onSelect(summary.id)}
          className="rounded-sm text-sm font-bold text-accent hover:text-accent-hover focus:outline-none focus:ring-2 focus:ring-accent"
        >
          View →
        </button>
      </td>
    </tr>
  )
}

/**
 * Phase 6d: lists past GapReports (most recent first) via GET
 * /api/reports, and shows the full Gap Report for one via GET
 * /api/reports/{id} when clicked - the same GapReportScreen used right
 * after a fresh Compare, so a past report looks identical to a new one.
 *
 * Phase 11: table layout with a JD snippet, date, and match badge per
 * row - the data model doesn't collect a role/company field (a JD is
 * just pasted text), so this uses the snippet already returned by
 * GapReportSummary instead of inventing fields we don't have.
 *
 * Phase 12: now reached via the "History" tab inside the Analyze
 * Resume page (was its own top-level nav item pre-shell).
 *
 * Phase 21: an optional `onCompare` prop turns on a checkbox column so
 * the user can pick 2-5 past reports and jump to the Compare Jobs page
 * with their ids - selection is local UI state here, the actual
 * comparison data comes from GET /api/reports/compare on that page.
 */
export default function HistoryScreen({ onCompare }) {
  const [summaries, setSummaries] = useState([])
  const [listLoading, setListLoading] = useState(true)
  const [listError, setListError] = useState('')

  const [selectedId, setSelectedId] = useState(null)
  const [selectedReport, setSelectedReport] = useState(null)
  const [detailLoading, setDetailLoading] = useState(false)
  const [detailError, setDetailError] = useState('')

  const [compareIds, setCompareIds] = useState([])
  const compareEnabled = Boolean(onCompare)

  useEffect(() => {
    fetchReportHistory()
      .then(setSummaries)
      .catch((error) => setListError(error.message))
      .finally(() => setListLoading(false))
  }, [])

  function handleToggleCompare(id) {
    setCompareIds((current) =>
      current.includes(id) ? current.filter((existing) => existing !== id) : [...current, id],
    )
  }

  async function handleSelect(id) {
    setSelectedId(id)
    setSelectedReport(null)
    setDetailError('')
    setDetailLoading(true)
    try {
      const report = await fetchReportById(id)
      setSelectedReport(report)
    } catch (error) {
      setDetailError(error.message)
    } finally {
      setDetailLoading(false)
    }
  }

  function handleBack() {
    setSelectedId(null)
    setSelectedReport(null)
    setDetailError('')
  }

  if (selectedId !== null) {
    return (
      <div className="space-y-4">
        <button
          type="button"
          onClick={handleBack}
          className="rounded-sm text-sm font-bold text-accent hover:text-accent-hover focus:outline-none focus:ring-2 focus:ring-accent"
        >
          &larr; Back to history
        </button>

        {detailLoading && <p className="text-sm text-text-muted">Loading report...</p>}
        {detailError && (
          <p className="text-sm font-medium text-critical" role="alert">
            {detailError}
          </p>
        )}
        {selectedReport && <GapReportScreen report={selectedReport} />}
      </div>
    )
  }

  return (
    <div>
      <div className="mb-6 flex flex-wrap items-center justify-between gap-3">
        <div>
          <h2 className="text-xl font-bold text-text-primary">History</h2>
          <p className="mt-0.5 text-sm text-text-muted">
            {compareEnabled
              ? `Every comparison you've run, saved automatically - tick 2 to ${MAX_COMPARE} to compare them side by side`
              : "Every comparison you've run, saved automatically"}
          </p>
        </div>
        {compareEnabled && compareIds.length > 0 && (
          <div className="flex items-center gap-3">
            <span className="text-xs font-semibold text-text-muted">
              {compareIds.length} of {MAX_COMPARE} selected
            </span>
            {compareIds.length >= 2 && (
              <button
                type="button"
                onClick={() => onCompare(compareIds)}
                className="inline-flex items-center gap-2 rounded-md bg-accent px-4 py-2 text-sm font-bold text-white transition-colors hover:bg-accent-hover focus:outline-none focus:ring-2 focus:ring-inset focus:ring-white active:scale-[0.97]"
              >
                Compare Selected
              </button>
            )}
          </div>
        )}
      </div>

      {listLoading && <p className="text-sm text-text-muted">Loading history...</p>}

      {listError && (
        <p className="text-sm font-medium text-critical" role="alert">
          Could not load history: {listError}
        </p>
      )}

      {!listLoading && !listError && summaries.length === 0 && (
        <p className="rounded-lg border border-border bg-surface p-6 text-sm text-text-muted">
          No reports yet - run a comparison from New Analysis to see it here.
        </p>
      )}

      {!listLoading && !listError && summaries.length > 0 && (
        <div className="overflow-hidden rounded-lg border border-border bg-surface">
          <div className="overflow-x-auto">
            <table className="w-full border-collapse text-left text-sm">
              <thead>
                <tr>
                  {compareEnabled && <th className="border-b border-border px-4 py-3.5" />}
                  <th className="border-b border-border px-4 py-3.5 text-xs font-bold uppercase tracking-wide text-text-muted">
                    Job description
                  </th>
                  <th className="border-b border-border px-4 py-3.5 text-xs font-bold uppercase tracking-wide text-text-muted">
                    Date
                  </th>
                  <th className="border-b border-border px-4 py-3.5 text-xs font-bold uppercase tracking-wide text-text-muted">
                    Match
                  </th>
                  <th className="border-b border-border px-4 py-3.5" />
                </tr>
              </thead>
              <tbody>
                {summaries.map((summary) => (
                  <ReportRow
                    key={summary.id}
                    summary={summary}
                    onSelect={handleSelect}
                    compareEnabled={compareEnabled}
                    checked={compareIds.includes(summary.id)}
                    checkDisabled={compareIds.length >= MAX_COMPARE && !compareIds.includes(summary.id)}
                    onToggleCompare={handleToggleCompare}
                  />
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}
    </div>
  )
}
