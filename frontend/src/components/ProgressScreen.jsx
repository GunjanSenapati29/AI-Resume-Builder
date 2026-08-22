import { useEffect, useState } from 'react'
import { fetchProgressTrend } from '../api'
import { formatDate } from '../dateFormat'
import { STATUS_COLORS } from '../statusColors'

// Same label/color pairing JobReadinessSection.jsx and DashboardScreen.jsx
// use for the same JobReadinessLabel values.
const LABEL_VARIANTS = {
  EXCELLENT: { label: 'Excellent', hex: STATUS_COLORS.good },
  STRONG: { label: 'Strong', hex: STATUS_COLORS.good },
  NEEDS_WORK: { label: 'Needs Work', hex: STATUS_COLORS.warning },
  NOT_READY: { label: 'Not Ready', hex: STATUS_COLORS.critical },
}

function variantFor(label) {
  return LABEL_VARIANTS[label] ?? LABEL_VARIANTS.NOT_READY
}

// Score-band reference lines, so the trend is readable against the same
// bands the Dashboard/Gap Report screens already use (85/70/50).
const BANDS = [
  { score: 85, label: 'Excellent' },
  { score: 70, label: 'Strong' },
  { score: 50, label: 'Needs Work' },
]

const CHART_WIDTH = 640
const CHART_HEIGHT = 240
const PADDING = { top: 16, right: 16, bottom: 16, left: 40 }
const PLOT_WIDTH = CHART_WIDTH - PADDING.left - PADDING.right
const PLOT_HEIGHT = CHART_HEIGHT - PADDING.top - PADDING.bottom

function yFor(score) {
  return PADDING.top + PLOT_HEIGHT - (score / 100) * PLOT_HEIGHT
}

function xFor(index, count) {
  if (count <= 1) {
    return PADDING.left + PLOT_WIDTH / 2
  }
  return PADDING.left + (index / (count - 1)) * PLOT_WIDTH
}

function TrendChart({ points }) {
  const coords = points.map((point, index) => ({
    x: xFor(index, points.length),
    y: yFor(point.jobReadinessScore),
    point,
  }))
  const linePath = coords.map((c) => `${c.x},${c.y}`).join(' ')

  return (
    <svg
      viewBox={`0 0 ${CHART_WIDTH} ${CHART_HEIGHT}`}
      role="img"
      aria-label="Job Readiness Score over time. See the table below for exact figures."
      className="h-auto w-full min-w-[480px]"
    >
      {BANDS.map((band) => (
        <g key={band.score}>
          <line
            x1={PADDING.left}
            x2={CHART_WIDTH - PADDING.right}
            y1={yFor(band.score)}
            y2={yFor(band.score)}
            className="stroke-border"
            strokeDasharray="4 4"
            strokeWidth={1}
          />
          <text
            x={PADDING.left - 6}
            y={yFor(band.score)}
            textAnchor="end"
            dominantBaseline="middle"
            className="fill-text-muted font-mono"
            fontSize={9}
          >
            {band.score}
          </text>
        </g>
      ))}

      {coords.length > 1 && (
        <polyline points={linePath} fill="none" className="stroke-accent" strokeWidth={2} />
      )}

      {coords.map(({ x, y, point }) => (
        <circle
          key={point.reportId}
          cx={x}
          cy={y}
          r={5}
          fill={variantFor(point.jobReadinessLabel).hex}
          className="stroke-surface"
          strokeWidth={2}
        >
          <title>
            {formatDate(point.createdAt)}: {point.jobReadinessScore}/100 ({variantFor(point.jobReadinessLabel).label})
          </title>
        </circle>
      ))}
    </svg>
  )
}

function TrendTable({ points }) {
  return (
    <div className="overflow-hidden rounded-lg border border-border bg-surface">
      <div className="overflow-x-auto">
        <table className="w-full border-collapse text-left text-sm">
          <caption className="sr-only">Job Readiness Score by analysis date</caption>
          <thead>
            <tr>
              <th className="border-b border-border px-4 py-3 text-xs font-bold uppercase tracking-wide text-text-muted">
                Date
              </th>
              <th className="border-b border-border px-4 py-3 text-xs font-bold uppercase tracking-wide text-text-muted">
                Score
              </th>
              <th className="border-b border-border px-4 py-3 text-xs font-bold uppercase tracking-wide text-text-muted">
                Label
              </th>
            </tr>
          </thead>
          <tbody>
            {points.map((point) => {
              const variant = variantFor(point.jobReadinessLabel)
              return (
                <tr key={point.reportId} className="transition-colors hover:bg-surface-hover">
                  <td className="border-b border-border px-4 py-3 font-mono text-text-muted">
                    {formatDate(point.createdAt)}
                  </td>
                  <td className="border-b border-border px-4 py-3 font-mono font-bold text-text-primary">
                    {point.jobReadinessScore}/100
                  </td>
                  <td className="border-b border-border px-4 py-3">
                    <span
                      className="inline-block rounded-full border px-2.5 py-1 text-xs font-bold"
                      style={{ borderColor: variant.hex, color: variant.hex }}
                    >
                      {variant.label}
                    </span>
                  </td>
                </tr>
              )
            })}
          </tbody>
        </table>
      </div>
    </div>
  )
}

/**
 * Phase 24: the Progress screen - Job Readiness Score plotted over time
 * via a hand-rolled inline SVG line chart (no charting library, per the
 * project's tech-stack constraint), reading GET /api/reports/progress-trend
 * (already sorted oldest-to-newest by the backend). A plain data table
 * below the chart is the screen-reader-friendly alternative, since a
 * chart alone isn't accessible.
 *
 * `points` state: `undefined` while loading, `null` when the user has no
 * analyzed reports yet (204), otherwise the chronological list. Exactly 1
 * report renders a single point (no line, per TrendChart's coords.length
 * check) rather than a broken/invisible line.
 */
export default function ProgressScreen({ onGoToAnalyze }) {
  const [points, setPoints] = useState(undefined)
  const [error, setError] = useState('')

  useEffect(() => {
    fetchProgressTrend()
      .then(setPoints)
      .catch((err) => setError(err.message))
  }, [])

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-xl font-bold text-text-primary">Progress</h1>
        <p className="mt-0.5 text-sm text-text-muted">Your Job Readiness Score across every analysis you've run</p>
      </div>

      {error && (
        <p className="text-sm font-medium text-critical" role="alert">
          Could not load your progress: {error}
        </p>
      )}

      {!error && points === undefined && <p className="text-sm text-text-muted">Loading your progress...</p>}

      {!error && points === null && (
        <div className="rounded-lg border border-border bg-surface p-8 text-center">
          <p className="text-sm text-text-muted">Analyze a resume first to start tracking your progress.</p>
          <button
            type="button"
            onClick={onGoToAnalyze}
            className="mt-4 inline-flex items-center gap-2 rounded-md bg-accent px-5 py-2.5 text-sm font-bold text-white transition-colors hover:bg-accent-hover focus:outline-none focus:ring-2 focus:ring-inset focus:ring-white active:scale-[0.97]"
          >
            Go to Analyze Resume
          </button>
        </div>
      )}

      {!error && points && points.length > 0 && (
        <>
          <section className="rounded-lg border border-border bg-surface p-5">
            <div className="overflow-x-auto">
              <TrendChart points={points} />
            </div>
            <div className="mt-3 flex flex-wrap gap-x-5 gap-y-1.5 text-xs text-text-muted">
              {['EXCELLENT', 'NEEDS_WORK', 'NOT_READY'].map((key) => (
                <span key={key} className="inline-flex items-center gap-1.5">
                  <span
                    className="inline-block h-2.5 w-2.5 rounded-full"
                    style={{ backgroundColor: variantFor(key).hex }}
                  />
                  {variantFor(key).label}
                </span>
              ))}
            </div>
          </section>

          <TrendTable points={points} />
        </>
      )}
    </div>
  )
}
