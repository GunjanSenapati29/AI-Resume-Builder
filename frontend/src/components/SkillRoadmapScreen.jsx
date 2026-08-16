import { useEffect, useState } from 'react'
import { fetchLearningRoadmap } from '../api'
import { STATUS_COLORS } from '../statusColors'

// Same Critical/Important/Optional badge mapping SkillSection.jsx uses
// for the Missing list's priority badge - kept as its own local copy
// (same convention every other section file in this app follows) rather
// than a shared import, since each section is meant to be readable on
// its own.
const PRIORITY_VARIANTS = {
  CRITICAL: { label: 'Critical', hex: STATUS_COLORS.critical },
  IMPORTANT: { label: 'Important', hex: STATUS_COLORS.warning },
  OPTIONAL: { label: 'Optional', hex: 'var(--color-accent)' },
}

function RoadmapCard({ item }) {
  const priority = item.priorityTier ? PRIORITY_VARIANTS[item.priorityTier] : null

  return (
    <li className="rounded-lg border border-border bg-surface p-4">
      <div className="flex flex-wrap items-center gap-2">
        <span className="text-sm font-bold text-text-primary">{item.skillName}</span>
        {priority && (
          <span
            className="rounded-full border px-2 py-0.5 text-[10px] font-bold uppercase tracking-wide"
            style={{ borderColor: priority.hex, color: priority.hex }}
          >
            {priority.label}
          </span>
        )}
      </div>

      <ol className="mt-3 flex flex-col gap-1.5">
        {item.steps.map((step, index) => (
          <li key={index} className="flex gap-2 text-sm text-text-secondary">
            <span className="flex-none font-mono text-xs font-bold text-text-muted">{index + 1}.</span>
            <span>{step}</span>
          </li>
        ))}
      </ol>

      {item.officialDocUrl && (
        <a
          href={item.officialDocUrl}
          target="_blank"
          rel="noopener noreferrer"
          className="mt-3 inline-flex items-center gap-1 rounded-sm text-xs font-bold text-accent hover:text-accent-hover focus:outline-none focus:ring-2 focus:ring-accent"
        >
          Official documentation
          <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
            <path d="M7 17L17 7M7 7h10v10" />
          </svg>
        </a>
      )}
    </li>
  )
}

function RoadmapSection({ title, subtitle, items, emptyText }) {
  return (
    <section>
      <h2 className="text-sm font-bold text-text-primary">
        {title} <span className="font-normal text-text-muted">({items.length})</span>
      </h2>
      <p className="mt-0.5 text-xs text-text-muted">{subtitle}</p>

      {items.length === 0 ? (
        <p className="mt-3 text-sm text-text-muted">{emptyText}</p>
      ) : (
        <ul className="mt-3 grid grid-cols-1 gap-3 md:grid-cols-2">
          {items.map((item) => (
            <RoadmapCard key={item.skillName} item={item} />
          ))}
        </ul>
      )}
    </section>
  )
}

/**
 * Phase 17: the Skill Roadmap page - a concrete, step-by-step learning
 * plan for every missing/underemphasized skill on the user's MOST RECENT
 * Gap Report (not aggregated across reports - that's Phase 24
 * territory). Every skill gets the same fixed 4-step template
 * (LearningRoadmapService); the only per-skill variation is the skill
 * name itself and, for a small fixed list of skills, a link to that
 * skill's official documentation homepage.
 *
 * `roadmap` state: `undefined` while loading, `null` when the user has
 * no analyzed reports yet (a normal empty state, not an error - see
 * fetchLearningRoadmap's 204 handling), otherwise the real payload.
 */
export default function SkillRoadmapScreen({ onGoToAnalyze }) {
  const [roadmap, setRoadmap] = useState(undefined)
  const [error, setError] = useState('')

  useEffect(() => {
    fetchLearningRoadmap()
      .then(setRoadmap)
      .catch((err) => setError(err.message))
  }, [])

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-xl font-bold text-text-primary">Skill Roadmap</h1>
        <p className="mt-0.5 text-sm text-text-muted">
          A step-by-step plan for closing the skill gaps found in your most recent Gap Report
        </p>
      </div>

      {error && (
        <p className="text-sm font-medium text-critical" role="alert">
          Could not load your roadmap: {error}
        </p>
      )}

      {!error && roadmap === undefined && <p className="text-sm text-text-muted">Loading your roadmap...</p>}

      {!error && roadmap === null && (
        <div className="rounded-lg border border-border bg-surface p-8 text-center">
          <p className="text-sm text-text-muted">Analyze a resume first to get your learning roadmap.</p>
          <button
            type="button"
            onClick={onGoToAnalyze}
            className="mt-4 inline-flex items-center gap-2 rounded-md bg-accent px-5 py-2.5 text-sm font-bold text-white transition-colors hover:bg-accent-hover focus:outline-none focus:ring-2 focus:ring-inset focus:ring-white active:scale-[0.97]"
          >
            Go to Analyze Resume
          </button>
        </div>
      )}

      {!error && roadmap && (
        <>
          <RoadmapSection
            title="Missing Skills"
            subtitle="Ordered Critical to Optional"
            items={roadmap.missingSkills}
            emptyText="Every required skill was found - nothing missing."
          />
          <RoadmapSection
            title="Underemphasized Skills"
            subtitle="Present, but worth reinforcing with stronger evidence"
            items={roadmap.underemphasizedSkills}
            emptyText="Nothing underemphasized."
          />
        </>
      )}
    </div>
  )
}
