import { useEffect, useState } from 'react'
import { fetchReportById, fetchReportHistory } from '../api'
import { matchSeverity } from '../matchSeverity'
import GapReportScreen from './GapReportScreen'

const dateFormatter = new Intl.DateTimeFormat(undefined, {
  dateStyle: 'medium',
  timeStyle: 'short',
})

function ReportRow({ summary, onSelect }) {
  const { hex } = matchSeverity(summary.matchPercentage)
  return (
    <li>
      <button
        type="button"
        onClick={() => onSelect(summary.id)}
        className="flex w-full items-start justify-between gap-4 px-5 py-4 text-left hover:bg-slate-50 focus:outline-none focus:ring-2 focus:ring-inset focus:ring-blue-500"
      >
        <div className="min-w-0">
          <p className="text-sm text-slate-500">{dateFormatter.format(new Date(summary.createdAt))}</p>
          <p className="mt-0.5 truncate text-sm text-slate-700">{summary.jdSnippet}</p>
        </div>
        <div className="flex flex-none items-center gap-1.5 text-sm font-semibold text-slate-900">
          <span className="h-2 w-2 rounded-full" style={{ backgroundColor: hex }} aria-hidden="true" />
          {Math.round(summary.matchPercentage)}%
        </div>
      </button>
    </li>
  )
}

/**
 * Phase 6d: lists past GapReports (most recent first) via GET
 * /api/reports, and shows the full Gap Report for one via GET
 * /api/reports/{id} when clicked - the same GapReportScreen used right
 * after a fresh Compare, so a past report looks identical to a new one.
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
          className="text-sm font-medium text-blue-600 hover:text-blue-700"
        >
          &larr; Back to history
        </button>

        {detailLoading && <p className="text-sm text-slate-500">Loading report...</p>}
        {detailError && (
          <p className="text-sm text-red-600" role="alert">
            {detailError}
          </p>
        )}
        {selectedReport && <GapReportScreen report={selectedReport} />}
      </div>
    )
  }

  if (listLoading) {
    return <p className="text-sm text-slate-500">Loading history...</p>
  }

  if (listError) {
    return (
      <p className="text-sm text-red-600" role="alert">
        Could not load history: {listError}
      </p>
    )
  }

  if (summaries.length === 0) {
    return (
      <p className="rounded-lg border border-slate-200 bg-white p-5 text-sm text-slate-500">
        No reports yet - run a comparison from Upload &amp; Compare to see it here.
      </p>
    )
  }

  return (
    <ul className="divide-y divide-slate-100 overflow-hidden rounded-lg border border-slate-200 bg-white">
      {summaries.map((summary) => (
        <ReportRow key={summary.id} summary={summary} onSelect={handleSelect} />
      ))}
    </ul>
  )
}
