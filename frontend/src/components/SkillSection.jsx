import { GoodIcon, WarningIcon, SeriousIcon } from './StatusIcons'

// Fixed status palette (never themed), reserved for state - see
// palette.md. Used only for the icon swatch and its tint, never as text
// color (warning/serious are sub-3:1 on a light surface by design; the
// icon + adjacent text label is the accessibility mitigation).
const VARIANTS = {
  good: { Icon: GoodIcon, hex: '#0ca30c' },
  warning: { Icon: WarningIcon, hex: '#fab219' },
  serious: { Icon: SeriousIcon, hex: '#ec835a' },
}

const MATCH_TYPE_LABEL = {
  EXACT: 'exact match',
  SYNONYM: 'synonym match',
  FUZZY: 'possible typo',
}

function Pill({ children }) {
  return (
    <span className="rounded-full bg-slate-100 px-2 py-0.5 text-xs font-medium text-slate-600">
      {children}
    </span>
  )
}

function SkillItem({ item }) {
  return (
    <li className="py-3">
      <div className="flex flex-wrap items-center gap-2">
        <span className="font-medium text-slate-900">{item.skillName}</span>
        {item.matchType && item.matchType !== 'NOT_FOUND' && (
          <Pill>{MATCH_TYPE_LABEL[item.matchType] ?? item.matchType.toLowerCase()}</Pill>
        )}
        <Pill>{item.difficulty.toLowerCase()}</Pill>
        {item.occurrenceCount > 1 && (
          <span className="text-xs text-slate-400">mentioned {item.occurrenceCount} times</span>
        )}
      </div>
      {/* The "why" - every result shows a reason, never just a verdict. */}
      <p className="mt-1 text-sm text-slate-600">{item.reason}</p>
    </li>
  )
}

/**
 * One card of the Gap Report: a status icon + title, an optional
 * subtitle (e.g. explaining the ranking), and the list of skills with
 * their reasons. `ordered` renders a numbered list for Missing, where
 * the order itself (easiest to close first) is part of the meaning.
 */
export default function SkillSection({ variant, title, subtitle, items, emptyText, ordered }) {
  const { Icon, hex } = VARIANTS[variant]
  const ListTag = ordered ? 'ol' : 'ul'

  return (
    <section className="rounded-lg border border-slate-200 bg-white p-5">
      <div className="flex items-start gap-3">
        <span
          className="flex h-8 w-8 flex-none items-center justify-center rounded-full"
          style={{ backgroundColor: `${hex}1a` }}
        >
          <Icon className="h-5 w-5" style={{ color: hex }} />
        </span>
        <div>
          <h2 className="text-base font-semibold text-slate-900">
            {title} <span className="font-normal text-slate-400">({items.length})</span>
          </h2>
          {subtitle && <p className="text-sm text-slate-500">{subtitle}</p>}
        </div>
      </div>

      {items.length === 0 ? (
        <p className="mt-3 text-sm text-slate-400">{emptyText}</p>
      ) : (
        <ListTag
          className={`mt-3 divide-y divide-slate-100 ${
            ordered ? 'list-decimal pl-9 marker:text-sm marker:text-slate-400' : ''
          }`}
        >
          {items.map((item) => (
            <SkillItem key={item.skillName} item={item} />
          ))}
        </ListTag>
      )}
    </section>
  )
}
