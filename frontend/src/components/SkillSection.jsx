import { GoodIcon, WarningIcon, CriticalIcon } from './StatusIcons'
import { STATUS_COLORS } from '../statusColors'

// Status color is carried by the icon and a colored left border only,
// never as the chip's text color - text stays theme-aware
// (text-primary/text-muted) so it stays legible in both dark and light,
// unlike a fixed hex tuned for one background. The icon + adjacent text
// label is what actually conveys the status; the color reinforces it.
const VARIANTS = {
  good: { Icon: GoodIcon, hex: STATUS_COLORS.good },
  warning: { Icon: WarningIcon, hex: STATUS_COLORS.warning },
  critical: { Icon: CriticalIcon, hex: STATUS_COLORS.critical },
}

const MATCH_TYPE_LABEL = {
  EXACT: 'exact match',
  SYNONYM: 'synonym match',
  FUZZY: 'possible typo',
}

// Phase 15: priority badge for missing-skill rows only (see
// GapReportScreen, which is the only caller that attaches `priority` to
// an item). Same colored-badge pattern AtsSection/SkillEvidenceSection
// use - Critical reuses the fixed critical status color, Important
// reuses warning, and Optional uses the theme's accent token directly
// via CSS var since accent isn't one of the fixed status colors.
const PRIORITY_VARIANTS = {
  CRITICAL: { label: 'Critical', hex: STATUS_COLORS.critical },
  IMPORTANT: { label: 'Important', hex: STATUS_COLORS.warning },
  OPTIONAL: { label: 'Optional', hex: 'var(--color-accent)' },
}

const LEARN_ORDER_LABELS = {
  LEARN_FIRST: 'Learn First',
  LEARN_NEXT: 'Learn Next',
  LEARN_LATER: 'Learn Later',
}

// The "why" behind the priority - how many other reports also needed
// this skill, and how heavily this JD emphasizes it - same "never show
// a result without showing why" principle as the reason text below it.
function priorityReason(priority) {
  const parts = []
  if (priority.crossReportCount > 0) {
    parts.push(`required in ${priority.crossReportCount} other report${priority.crossReportCount === 1 ? '' : 's'}`)
  }
  if (priority.inJdMentionCount > 0) {
    parts.push(`mentioned ${priority.inJdMentionCount}× in this JD`)
  }
  return parts.length > 0 ? parts.join(', ') : 'first time seen for this skill'
}

function tagsFor(item) {
  const tags = []
  if (item.matchType && item.matchType !== 'NOT_FOUND') {
    tags.push(MATCH_TYPE_LABEL[item.matchType] ?? item.matchType.toLowerCase())
  }
  tags.push(item.difficulty.toLowerCase())
  if (item.occurrenceCount > 1) {
    tags.push(`mentioned ${item.occurrenceCount} times`)
  }
  return tags.join(' · ')
}

function Chip({ variant, item, index, ordered }) {
  const { Icon, hex } = VARIANTS[variant]
  const tags = tagsFor(item)
  const isTopPriority = ordered && index === 0

  return (
    <li
      style={{ borderLeftColor: hex }}
      className="flex items-start gap-2.5 rounded-sm border border-border border-l-[3px] bg-surface px-3.5 py-2.5 text-sm font-semibold text-text-primary transition-transform hover:translate-x-0.5"
    >
      <Icon className="mt-0.5 h-[18px] w-[18px] flex-none" style={{ color: hex }} />
      <div className="min-w-0 flex-1">
        <div className="flex flex-wrap items-center gap-2">
          {ordered && <span className="text-xs font-bold opacity-70">{index + 1}.</span>}
          <span>{item.skillName}</span>
          {isTopPriority && (
            <span className="rounded-full bg-critical px-2 py-0.5 text-[9.5px] font-extrabold tracking-wide text-white">
              TOP
            </span>
          )}
          {item.priority && (
            <span
              className="rounded-full border px-2 py-0.5 text-[9.5px] font-extrabold uppercase tracking-wide"
              style={{
                borderColor: PRIORITY_VARIANTS[item.priority.priorityTier].hex,
                color: PRIORITY_VARIANTS[item.priority.priorityTier].hex,
              }}
            >
              {PRIORITY_VARIANTS[item.priority.priorityTier].label}
            </span>
          )}
        </div>
        {/* The "why" - every result shows a reason, never just a verdict. */}
        <span className="mt-0.5 block text-xs font-normal text-text-muted">
          {tags}
          {tags && item.reason ? ' — ' : ''}
          {item.reason}
        </span>
        {item.priority && (
          <span className="mt-0.5 block text-[11px] font-semibold text-text-secondary">
            {LEARN_ORDER_LABELS[item.priority.learnOrder]} — {priorityReason(item.priority)}
          </span>
        )}
      </div>
    </li>
  )
}

/**
 * One card of the Gap Report: a status icon + title, an optional
 * subtitle (e.g. explaining the ranking), and the list of skills as
 * tinted chips. `ordered` numbers the list for Missing, where the order
 * itself (easiest to close first) is part of the meaning, and marks the
 * first item TOP priority.
 *
 * Phase 15: an item's optional `priority` field (attached only to the
 * Missing section's items, by GapReportScreen) adds a Critical/Important/
 * Optional badge and a Learn First/Next/Later line to that chip - see
 * SkillGapPriorityService for where the data comes from.
 */
export default function SkillSection({ variant, title, subtitle, items, emptyText, ordered }) {
  const { Icon, hex } = VARIANTS[variant]

  return (
    <section className="rounded-lg border border-border bg-surface p-5">
      <div className="mb-4 flex items-start gap-2.5">
        <span
          className="flex h-7 w-7 flex-none items-center justify-center rounded-full"
          style={{ backgroundColor: `${hex}1a` }}
        >
          <Icon className="h-4 w-4" style={{ color: hex }} />
        </span>
        <div>
          <h2 className="text-sm font-bold text-text-primary">
            {title} <span className="font-normal text-text-muted">({items.length})</span>
          </h2>
          {subtitle && <p className="text-xs text-text-muted">{subtitle}</p>}
        </div>
      </div>

      {items.length === 0 ? (
        <p className="text-sm text-text-muted">{emptyText}</p>
      ) : (
        <ul className="flex flex-col gap-2">
          {items.map((item, index) => (
            <Chip key={item.skillName} variant={variant} item={item} index={index} ordered={ordered} />
          ))}
        </ul>
      )}
    </section>
  )
}
