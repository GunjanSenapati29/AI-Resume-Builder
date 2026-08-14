import MatchMeter from './MatchMeter'
import SkillSection from './SkillSection'

/**
 * Phase 6c - the hero screen. Shows the overall match, then matched,
 * missing (ranked easiest to close first), and underemphasized skills,
 * each with the backend's "why" reason - never a verdict on its own.
 */
export default function GapReportScreen({ report }) {
  const matchedCount = report.matched.length
  const missingCount = report.missing.length
  const totalCount = matchedCount + missingCount

  return (
    <div className="space-y-4">
      <MatchMeter
        percentage={report.matchPercentage}
        matchedCount={matchedCount}
        totalCount={totalCount}
      />

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
