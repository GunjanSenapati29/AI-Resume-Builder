import { useState } from 'react'
import MatchMeter from './MatchMeter'
import SkillSection from './SkillSection'
import { fetchReportPdf } from '../api'
import { formatDate } from '../dateFormat'

/**
 * Phase 6c - the hero screen. Shows the overall match, then matched,
 * missing (ranked easiest to close first), and underemphasized skills,
 * each with the backend's "why" reason - never a verdict on its own.
 *
 * Phase 9 - "Download PDF" fetches the same report rendered as a PDF
 * (GET /api/reports/{id}/pdf) as a blob and hands it to the browser as
 * a download; can't be a plain <a href> link since the route needs the
 * Authorization header like every other protected route.
 *
 * Phase 11 - restyled to design-reference.html's report-header + summary-
 * strip + report-columns layout. `onStartOver` is optional: App.jsx's
 * fresh-Compare flow passes it (renders "Compare Another JD" up top,
 * where the old "Start a new comparison" button used to sit below);
 * HistoryScreen doesn't, since its own "Back to history" link already
 * covers that.
 */
export default function GapReportScreen({ report, onStartOver }) {
  const matchedCount = report.matched.length
  const missingCount = report.missing.length
  const underemphasizedCount = report.underemphasized.length

  const [downloading, setDownloading] = useState(false)
  const [downloadError, setDownloadError] = useState('')

  async function handleDownload() {
    setDownloading(true)
    setDownloadError('')
    try {
      const blob = await fetchReportPdf(report.id)
      const url = URL.createObjectURL(blob)
      const link = document.createElement('a')
      link.href = url
      link.download = `gap-report-${report.id}.pdf`
      document.body.appendChild(link)
      link.click()
      document.body.removeChild(link)
      URL.revokeObjectURL(url)
    } catch (error) {
      setDownloadError(error.message)
    } finally {
      setDownloading(false)
    }
  }

  return (
    <div className="space-y-5">
      <div className="flex flex-wrap items-start justify-between gap-3">
        <div>
          <h2 className="text-xl font-bold text-text-primary">Your Gap Report</h2>
          <p className="mt-0.5 text-sm text-text-muted">Analyzed {formatDate(report.createdAt)}</p>
        </div>
        {onStartOver && (
          <button
            type="button"
            onClick={onStartOver}
            className="inline-flex items-center gap-2 rounded-md border border-border bg-surface-1 px-4 py-2.5 text-sm font-bold text-text-primary transition-colors hover:border-accent hover:text-accent-dark focus:outline-none focus:ring-2 focus:ring-inset focus:ring-accent"
          >
            <svg
              className="h-4 w-4"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              strokeWidth="2.2"
              strokeLinecap="round"
              strokeLinejoin="round"
              aria-hidden="true"
            >
              <path d="M23 4v6h-6M1 20v-6h6" />
              <path d="M3.51 9a9 9 0 0114.85-3.36L23 10M1 14l4.64 4.36A9 9 0 0020.49 15" />
            </svg>
            Compare Another JD
          </button>
        )}
      </div>

      <MatchMeter
        percentage={report.matchPercentage}
        matchedCount={matchedCount}
        missingCount={missingCount}
        underemphasizedCount={underemphasizedCount}
      />

      <div className="grid grid-cols-1 gap-4 lg:grid-cols-3">
        <SkillSection
          variant="good"
          title="Matched"
          items={report.matched}
          emptyText="None of the required skills were found in this resume."
        />

        <SkillSection
          variant="critical"
          title="Missing"
          subtitle="Ranked easiest to close first"
          items={report.missing}
          ordered
          emptyText="Every required skill was found - nothing missing."
        />

        <SkillSection
          variant="warning"
          title="Underemphasized"
          subtitle="Present, but only mentioned once for a core requirement"
          items={report.underemphasized}
          emptyText="Nothing underemphasized."
        />
      </div>

      <div className="flex flex-col items-end gap-2">
        <button
          type="button"
          onClick={handleDownload}
          disabled={downloading}
          className="inline-flex items-center gap-2 rounded-md bg-gradient-to-br from-accent to-violet px-6 py-3 text-sm font-bold text-white shadow-[0_8px_18px_rgba(42,110,224,0.28)] transition-[transform,box-shadow,opacity] hover:-translate-y-0.5 hover:shadow-[0_12px_24px_rgba(42,110,224,0.36)] focus:outline-none focus:ring-2 focus:ring-inset focus:ring-white active:scale-[0.97] disabled:cursor-not-allowed disabled:translate-y-0 disabled:opacity-50 disabled:shadow-none"
        >
          <svg
            className="h-4 w-4"
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            strokeWidth="2.2"
            strokeLinecap="round"
            strokeLinejoin="round"
            aria-hidden="true"
          >
            <path d="M21 15v4a2 2 0 01-2 2H5a2 2 0 01-2-2v-4M7 10l5 5 5-5M12 15V3" />
          </svg>
          {downloading ? 'Preparing PDF...' : 'Download Report (PDF)'}
        </button>
        {downloadError && (
          <p className="text-sm font-medium text-critical" role="alert">
            {downloadError}
          </p>
        )}
      </div>
    </div>
  )
}
