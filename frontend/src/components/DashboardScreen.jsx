import { useEffect, useState } from 'react'
import { fetchCareerReportPdf, fetchDashboardSummary } from '../api'
import { formatDate } from '../dateFormat'
import { STATUS_COLORS } from '../statusColors'

// Same label/color pairing JobReadinessSection.jsx uses for the same
// JobReadinessLabel values, so the score reads the same everywhere it
// appears in the app.
const LABEL_VARIANTS = {
  EXCELLENT: { label: 'Excellent', hex: STATUS_COLORS.good },
  STRONG: { label: 'Strong', hex: STATUS_COLORS.good },
  NEEDS_WORK: { label: 'Needs Work', hex: STATUS_COLORS.warning },
  NOT_READY: { label: 'Not Ready', hex: STATUS_COLORS.critical },
}

function StatTile({ label, value }) {
  return (
    <div className="rounded-lg border border-border bg-surface p-5">
      <p className="text-xs font-bold uppercase tracking-wide text-text-muted">{label}</p>
      <p className="mt-2 font-mono text-2xl font-bold text-text-primary">{value}</p>
    </div>
  )
}

/**
 * Phase 24: the Dashboard screen - the user's latest Job Readiness Score
 * as a hero number, plus how many analyses they've run and when the
 * latest one was. Pure read of GET /api/reports/dashboard-summary, which
 * itself is just an aggregation of columns GapReport already stores (see
 * ReportService.getDashboardSummary) - no new scoring logic here either.
 *
 * `summary` state: `undefined` while loading, `null` when the user has no
 * analyzed reports yet (204 - a normal empty state, not an error),
 * otherwise the real payload.
 *
 * Phase 25: "Download Career Report" fetches GET
 * /api/reports/latest/career-report-pdf (Job Readiness Score, Skill
 * Gaps, Learning Roadmap, and Interview Prep for the same most-recent
 * report, combined into one PDF) as a blob, same download-via-blob
 * approach GapReportScreen's existing PDF button already uses. Kept
 * separate from that button (and from GapReportScreen entirely) since
 * they're different documents at different scopes - one report's Gap
 * Report vs. this account-wide career snapshot.
 */
export default function DashboardScreen({ onGoToAnalyze, onGoToHistory }) {
  const [summary, setSummary] = useState(undefined)
  const [error, setError] = useState('')

  const [downloadingReport, setDownloadingReport] = useState(false)
  const [downloadError, setDownloadError] = useState('')

  useEffect(() => {
    fetchDashboardSummary()
      .then(setSummary)
      .catch((err) => setError(err.message))
  }, [])

  async function handleDownloadCareerReport() {
    setDownloadingReport(true)
    setDownloadError('')
    try {
      const blob = await fetchCareerReportPdf()
      const url = URL.createObjectURL(blob)
      const link = document.createElement('a')
      link.href = url
      link.download = 'career-report.pdf'
      document.body.appendChild(link)
      link.click()
      document.body.removeChild(link)
      URL.revokeObjectURL(url)
    } catch (err) {
      setDownloadError(err.message)
    } finally {
      setDownloadingReport(false)
    }
  }

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-xl font-bold text-text-primary">Dashboard</h1>
        <p className="mt-0.5 text-sm text-text-muted">Your job-readiness snapshot, at a glance</p>
      </div>

      {error && (
        <p className="text-sm font-medium text-critical" role="alert">
          Could not load your dashboard: {error}
        </p>
      )}

      {!error && summary === undefined && <p className="text-sm text-text-muted">Loading your dashboard...</p>}

      {!error && summary === null && (
        <div className="rounded-lg border border-border bg-surface p-8 text-center">
          <p className="text-sm text-text-muted">Analyze a resume first to see your dashboard.</p>
          <button
            type="button"
            onClick={onGoToAnalyze}
            className="mt-4 inline-flex items-center gap-2 rounded-md bg-accent px-5 py-2.5 text-sm font-bold text-white transition-colors hover:bg-accent-hover focus:outline-none focus:ring-2 focus:ring-inset focus:ring-white active:scale-[0.97]"
          >
            Go to Analyze Resume
          </button>
        </div>
      )}

      {!error && summary && (
        <>
          <section className="rounded-lg border border-border bg-surface p-6">
            <p className="text-xs font-bold uppercase tracking-wide text-text-muted">
              Latest Job Readiness Score
            </p>
            <div className="mt-3 flex flex-wrap items-center gap-3">
              <span className="font-mono text-4xl font-bold text-text-primary">{summary.jobReadinessScore}</span>
              <span className="font-mono text-base text-text-muted">/100</span>
              <span
                className="ml-1 rounded-full border px-3 py-1 text-xs font-bold"
                style={{
                  borderColor: (LABEL_VARIANTS[summary.jobReadinessLabel] ?? LABEL_VARIANTS.NOT_READY).hex,
                  color: (LABEL_VARIANTS[summary.jobReadinessLabel] ?? LABEL_VARIANTS.NOT_READY).hex,
                }}
              >
                {(LABEL_VARIANTS[summary.jobReadinessLabel] ?? LABEL_VARIANTS.NOT_READY).label}
              </span>
            </div>
            <div className="mt-4 h-2 w-full overflow-hidden rounded-full bg-border">
              <div
                className="h-full rounded-full"
                style={{
                  width: `${summary.jobReadinessScore}%`,
                  backgroundColor: (LABEL_VARIANTS[summary.jobReadinessLabel] ?? LABEL_VARIANTS.NOT_READY).hex,
                }}
              />
            </div>
          </section>

          <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
            <StatTile label="Total Analyses" value={summary.totalReports} />
            <StatTile label="Last Analyzed" value={formatDate(summary.lastAnalyzedAt)} />
          </div>

          <div className="flex flex-wrap items-center gap-3">
            <button
              type="button"
              onClick={onGoToAnalyze}
              className="inline-flex items-center gap-2 rounded-md bg-accent px-5 py-2.5 text-sm font-bold text-white transition-colors hover:bg-accent-hover focus:outline-none focus:ring-2 focus:ring-inset focus:ring-white active:scale-[0.97]"
            >
              Analyze Resume
            </button>
            <button
              type="button"
              onClick={onGoToHistory}
              className="inline-flex items-center gap-2 rounded-md border border-border bg-surface px-5 py-2.5 text-sm font-bold text-text-primary transition-colors hover:bg-surface-hover focus:outline-none focus:ring-2 focus:ring-accent active:scale-[0.97]"
            >
              View History
            </button>
            <button
              type="button"
              onClick={handleDownloadCareerReport}
              disabled={downloadingReport}
              className="inline-flex items-center gap-2 rounded-md border border-border bg-surface px-5 py-2.5 text-sm font-bold text-text-primary transition-colors hover:bg-surface-hover focus:outline-none focus:ring-2 focus:ring-accent active:scale-[0.97] disabled:cursor-not-allowed disabled:opacity-60"
            >
              {downloadingReport && (
                <span
                  aria-hidden="true"
                  className="h-4 w-4 flex-none animate-spin rounded-full border-2 border-border-strong border-t-accent"
                />
              )}
              {downloadingReport ? 'Preparing report...' : 'Download Career Report'}
            </button>
          </div>
          {downloadError && (
            <p className="text-sm font-medium text-critical" role="alert">
              {downloadError}
            </p>
          )}
        </>
      )}
    </div>
  )
}
