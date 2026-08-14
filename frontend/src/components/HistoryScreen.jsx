import { useEffect, useState } from 'react'
import { fetchReportById, fetchReportHistory } from '../api'
import { matchSeverity } from '../matchSeverity'
import { formatDate } from '../dateFormat'
import GapReportScreen from './GapReportScreen'

// Same 70/40 thresholds as matchSeverity(), extended to a third (low)
// tier design-reference.html's own two example rows never needed to
// show, since its mock data happened to only include high/mid scores.
function badgeClasses(percentage) {
  if (percentage >= 70) return 'bg-good-tint text-[#0b3d0b]'
  if (percentage >= 40) return 'bg-warning-tint text-[#5c3d00]'
  return 'bg-critical-tint text-[#5c1613]'
}

function ReportRow({ summary, onSelect }) {
  return (
    <tr className="transition-colors hover:bg-surface-2">
      <td className="border-b border-gridline px-4 py-4 text-text-primary">
        <span className="block max-w-xs truncate sm:max-w-sm">{summary.jdSnippet}</span>
      </td>
      <td className="border-b border-gridline px-4 py-4 text-text-muted">
        {formatDate(summary.createdAt)}
      </td>
      <td className="border-b border-gridline px-4 py-4">
        <span className={`inline-block rounded-full px-2.5 py-1 text-xs font-extrabold ${badgeClasses(summary.matchPercentage)}`}>
          {Math.round(summary.matchPercentage)}%
        </span>
      </td>
      <td className="border-b border-gridline px-4 py-4 text-right">
        <button
          type="button"
          onClick={() => onSelect(summary.id)}
          className="rounded-sm text-sm font-bold text-accent hover:text-accent-dark focus:outline-none focus:ring-2 focus:ring-accent"
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
 * Phase 11: table layout per design-reference.html. The reference's
 * mock table has Role/Company columns - our data model doesn't collect
 * either (a JD is just pasted text), so this uses the JD snippet
 * already returned by GapReportSummary instead of inventing fields we
 * don't have.
 */
export default function HistoryScreen() {
  const [summaries, setSummaries] = useState([])
  const [listLoading, setListLoading] = useState(true)
  const [listError, setListError] = useState('')

  const [selectedId, setSelectedId] = useState(null)
  const [selectedReport, setSelectedReport] = useState(null)
  const [detailLoading, setDetailLoading] = useState(false)
  const [detailError, setDetailError] = useState('')

  useEffect(() => {
    fetchReportHistory()
      .then(setSummaries)
      .catch((error) => setListError(error.message))
      .finally(() => setListLoading(false))
  }, [])

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
          className="rounded-sm text-sm font-bold text-accent hover:text-accent-dark focus:outline-none focus:ring-2 focus:ring-accent"
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
      <div className="mb-6">
        <h2 className="text-xl font-bold text-text-primary">History</h2>
        <p className="mt-0.5 text-sm text-text-muted">Every comparison you've run, saved automatically</p>
      </div>

      {listLoading && <p className="text-sm text-text-muted">Loading history...</p>}

      {listError && (
        <p className="text-sm font-medium text-critical" role="alert">
          Could not load history: {listError}
        </p>
      )}

      {!listLoading && !listError && summaries.length === 0 && (
        <p className="rounded-lg border border-border bg-surface-1 p-6 text-sm text-text-muted shadow-sm">
          No reports yet - run a comparison from Upload &amp; Compare to see it here.
        </p>
      )}

      {!listLoading && !listError && summaries.length > 0 && (
        <div className="overflow-hidden rounded-lg border border-border bg-surface-1 shadow-sm">
          <div className="overflow-x-auto">
            <table className="w-full border-collapse text-left text-sm">
              <thead>
                <tr>
                  <th className="border-b border-gridline px-4 py-3.5 text-xs font-bold uppercase tracking-wide text-text-muted">
                    Job description
                  </th>
                  <th className="border-b border-gridline px-4 py-3.5 text-xs font-bold uppercase tracking-wide text-text-muted">
                    Date
                  </th>
                  <th className="border-b border-gridline px-4 py-3.5 text-xs font-bold uppercase tracking-wide text-text-muted">
                    Match
                  </th>
                  <th className="border-b border-gridline px-4 py-3.5" />
                </tr>
              </thead>
              <tbody>
                {summaries.map((summary) => (
                  <ReportRow key={summary.id} summary={summary} onSelect={handleSelect} />
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}
    </div>
  )
}
