const PLANNED_NAV = [
  {
    label: 'Dashboard',
    icon: <path d="M3 12l2-2m0 0l7-7 7 7M5 10v10a1 1 0 001 1h3m10-11l2 2m-2-2v10a1 1 0 01-1 1h-3m-6 0a1 1 0 001-1v-4a1 1 0 011-1h2a1 1 0 011 1v4a1 1 0 001 1m-6 0h6" />,
  },
  {
    label: 'Interview Prep',
    icon: <path d="M8 12h.01M12 12h.01M16 12h.01M21 12c0 4.418-4.03 8-9 8a9.86 9.86 0 01-4.255-.949L3 20l1.395-3.72C3.512 15.042 3 13.574 3 12c0-4.418 4.03-8 9-8s9 3.582 9 8z" />,
  },
  {
    label: 'Progress',
    icon: <path d="M18 20V10M12 20V4M6 20v-6" />,
  },
  {
    label: 'Compare Jobs',
    icon: <path d="M8 7h12m0 0l-4-4m4 4l-4 4M16 17H4m0 0l4 4m-4-4l4-4" />,
  },
  {
    label: 'My Resumes',
    icon: <path d="M14 2H6a2 2 0 00-2 2v16a2 2 0 002 2h12a2 2 0 002-2V8z M14 2v6h6 M9 13h6 M9 17h6" />,
  },
]

function AnalyzeIcon(props) {
  return (
    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.2" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true" {...props}>
      <circle cx="11" cy="11" r="7" />
      <path d="M21 21l-4.35-4.35" />
    </svg>
  )
}

function NavIcon({ children }) {
  return (
    <svg
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      strokeWidth="2"
      strokeLinecap="round"
      strokeLinejoin="round"
      aria-hidden="true"
      className="h-[18px] w-[18px] flex-none"
    >
      {children}
    </svg>
  )
}

function initials(name) {
  const parts = name.trim().split(/\s+/)
  const first = parts[0]?.[0] ?? ''
  const second = parts.length > 1 ? parts[parts.length - 1][0] : ''
  return (first + second).toUpperCase()
}

// Phase 17: the real (non-"Soon") nav items - Analyze Resume and Skill
// Roadmap now both drive App.jsx's `page` state via `onNavigate`, so
// both need active-state styling instead of Analyze Resume's old
// always-on hardcoded style. The rest of PLANNED_NAV below stays
// disabled, so the shape of the eventual product is still visible
// without pretending those pages exist yet.
function NavButton({ active, collapsed, label, icon, onClick }) {
  return (
    <li>
      <button
        type="button"
        onClick={onClick}
        aria-current={active ? 'page' : undefined}
        title={collapsed ? label : undefined}
        className={`flex w-full items-center gap-3 rounded-md px-3 py-2.5 text-sm font-semibold focus:outline-none focus:ring-2 focus:ring-accent ${
          active
            ? 'bg-surface-raised text-accent'
            : 'text-text-secondary transition-colors hover:bg-surface-hover hover:text-text-primary'
        } ${collapsed ? 'justify-center' : ''}`}
      >
        {icon}
        {!collapsed && <span>{label}</span>}
      </button>
    </li>
  )
}

/**
 * Phase 12: primary app navigation, replacing the old header pill nav.
 * Phase 17: Analyze Resume and Skill Roadmap are both wired to real
 * pages now (see NavButton above) - the rest of the planned nav is still
 * visually present but disabled ("Soon"), so the shape of the eventual
 * product is visible without pretending those pages exist yet. Collapses
 * to icon-only via the `collapsed` prop, which AppShell owns and
 * TopBar's toggle button flips.
 */
export default function Sidebar({ collapsed, user, onLogout, activePage, onNavigate }) {
  return (
    <aside
      className={`flex h-full flex-col border-r border-border bg-sidebar transition-[width] duration-200 ${
        collapsed ? 'w-[68px]' : 'w-64'
      }`}
    >
      <div className={`flex h-16 flex-none items-center border-b border-border ${collapsed ? 'justify-center px-0' : 'gap-2.5 px-5'}`}>
        <span className="flex h-8 w-8 flex-none items-center justify-center rounded-md bg-accent text-sm font-extrabold text-white">
          SG
        </span>
        {!collapsed && <span className="text-lg font-extrabold text-text-primary">SkillGap AI</span>}
      </div>

      <nav className="flex-1 overflow-y-auto px-3 py-4" aria-label="Primary">
        <ul className="space-y-1">
          <NavButton
            active={activePage === 'analyze'}
            collapsed={collapsed}
            label="Analyze Resume"
            icon={<AnalyzeIcon className="h-[18px] w-[18px] flex-none" />}
            onClick={() => onNavigate('analyze')}
          />

          <NavButton
            active={activePage === 'roadmap'}
            collapsed={collapsed}
            label="Skill Roadmap"
            icon={
              <NavIcon>
                <path d="M9 20l-5.447-2.724A1 1 0 013 16.382V5.618a1 1 0 011.447-.894L9 7m0 13l6-3m-6 3V7m6 10l4.553 2.276A1 1 0 0021 18.382V7.618a1 1 0 00-.553-.894L15 4m0 13V4m0 0L9 7" />
              </NavIcon>
            }
            onClick={() => onNavigate('roadmap')}
          />

          {PLANNED_NAV.map((item) => (
            <li key={item.label}>
              <button
                type="button"
                disabled
                title={collapsed ? `${item.label} (Soon)` : undefined}
                className={`flex w-full cursor-not-allowed items-center gap-3 rounded-md px-3 py-2.5 text-sm font-medium text-text-muted ${
                  collapsed ? 'justify-center' : 'justify-between'
                }`}
              >
                <span className={`flex items-center gap-3 ${collapsed ? '' : ''}`}>
                  <NavIcon>{item.icon}</NavIcon>
                  {!collapsed && <span>{item.label}</span>}
                </span>
                {!collapsed && (
                  <span className="rounded-full border border-border-strong px-1.5 py-0.5 text-[10px] font-bold uppercase tracking-wide text-text-muted">
                    Soon
                  </span>
                )}
              </button>
            </li>
          ))}
        </ul>
      </nav>

      <div className={`flex flex-none flex-col gap-2 border-t border-border py-3 ${collapsed ? 'items-center px-0' : 'px-3'}`}>
        <div className="flex min-w-0 items-center gap-2.5" title={collapsed ? user.name : undefined}>
          <span className="flex h-8 w-8 flex-none items-center justify-center rounded-full bg-surface-raised text-xs font-bold text-text-primary">
            {initials(user.name)}
          </span>
          {!collapsed && (
            <span className="min-w-0 truncate text-sm font-semibold text-text-primary">{user.name}</span>
          )}
        </div>
        <button
          type="button"
          onClick={onLogout}
          title="Log out"
          className={`flex items-center gap-2 rounded-md p-2 text-sm font-semibold text-text-secondary transition-colors hover:bg-surface-hover hover:text-text-primary focus:outline-none focus:ring-2 focus:ring-accent ${
            collapsed ? 'justify-center' : 'w-full'
          }`}
        >
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.2" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true" className="flex-none">
            <path d="M9 21H5a2 2 0 01-2-2V5a2 2 0 012-2h4" />
            <path d="M16 17l5-5-5-5" />
            <path d="M21 12H9" />
          </svg>
          {collapsed ? <span className="sr-only">Log out</span> : 'Log out'}
        </button>
      </div>
    </aside>
  )
}
