/**
 * Plain preview of a GapReportView, just to prove POST /api/match works
 * end-to-end from the browser. The real "hero" Gap Report screen (with
 * proper visual design) is a later phase - this deliberately looks
 * unstyled/basic so it isn't mistaken for that finished screen.
 */
function SkillList({ title, items, emptyText }) {
  return (
    <div>
      <h3 className="text-sm font-semibold text-slate-700">
        {title} ({items.length})
      </h3>
      {items.length === 0 ? (
        <p className="text-sm text-slate-400">{emptyText}</p>
      ) : (
        <ul className="mt-1 space-y-1">
          {items.map((item) => (
            <li key={item.skillName} className="text-sm text-slate-700">
              <span className="font-medium">{item.skillName}</span>{' '}
              <span className="text-slate-500">— {item.reason}</span>
            </li>
          ))}
        </ul>
      )}
    </div>
  )
}

export default function MatchResultSummary({ report }) {
  return (
    <section
      aria-label="Gap report result"
      className="space-y-4 rounded-lg border border-slate-200 bg-white p-4"
    >
      <p className="text-sm text-slate-500">
        Full report screen comes in a later phase - this is a plain preview to confirm the
        backend call worked.
      </p>
      <p className="text-2xl font-semibold text-slate-900">
        {report.matchPercentage.toFixed(0)}% match
      </p>
      <SkillList title="Matched" items={report.matched} emptyText="None matched." />
      <SkillList title="Missing" items={report.missing} emptyText="Nothing missing." />
      <SkillList
        title="Underemphasized"
        items={report.underemphasized}
        emptyText="Nothing underemphasized."
      />
    </section>
  )
}
