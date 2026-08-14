import { useState } from 'react'
import MatchMeter from './MatchMeter'
import SkillSection from './SkillSection'
import { fetchReportPdf } from '../api'

/**
 * Phase 6c - the hero screen. Shows the overall match, then matched,
 * missing (ranked easiest to close first), and underemphasized skills,
 * each with the backend's "why" reason - never a verdict on its own.
 *
 * Phase 9 - "Download PDF" fetches the same report rendered as a PDF
 * (GET /api/reports/{id}/pdf) as a blob and hands it to the browser as
 * a download; can't be a plain <a href> link since the route needs the
 * Authorization header like every other protected route.
 */
export default function GapReportScreen({ report }) {
  const matchedCount = report.matched.length
  const missingCount = report.missing.length
  const totalCount = matchedCount + missingCount

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
    <div className="space-y-4">
      <MatchMeter
        percentage={report.matchPercentage}
        matchedCount={matchedCount}
        totalCount={totalCount}
      />

      <div>
        <button
          type="button"
          onClick={handleDownload}
          disabled={downloading}
          className="rounded-md border border-slate-300 bg-white px-3 py-1.5 text-sm font-medium text-slate-700 hover:bg-slate-50 focus:outline-none focus:ring-2 focus:ring-inset focus:ring-blue-500 disabled:cursor-not-allowed disabled:text-slate-400"
        >
          {downloading ? 'Preparing PDF...' : 'Download PDF'}
        </button>
        {downloadError && (
          <p className="mt-2 text-sm text-red-600" role="alert">
            {downloadError}
          </p>
        )}
      </div>

      <SkillSection
        variant="good"
        title="Matched"
        items={report.matched}
        emptyText="None of the required skills were found in this resume."
      />

      <SkillSection
        variant="serious"
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
  )
}
