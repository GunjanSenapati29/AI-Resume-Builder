import { useTheme } from '../ThemeContext'

function initials(name) {
  const parts = name.trim().split(/\s+/)
  const first = parts[0]?.[0] ?? ''
  const second = parts.length > 1 ? parts[parts.length - 1][0] : ''
  return (first + second).toUpperCase()
}

function IconButton({ children, ...props }) {
  return (
    <button
      type="button"
      className="flex h-9 w-9 flex-none items-center justify-center rounded-md text-text-secondary transition-colors hover:bg-surface-hover hover:text-text-primary focus:outline-none focus:ring-2 focus:ring-accent disabled:cursor-not-allowed disabled:opacity-40 disabled:hover:bg-transparent disabled:hover:text-text-secondary"
      {...props}
    >
      {children}
    </button>
  )
}

/**
 * Phase 12: the top bar living beside the sidebar. Search and
 * notifications are visual only for now (no search index or
 * notification source exists yet) - both render disabled rather than
 * pretending to work. Theme toggle is the one fully real control here.
 */
export default function TopBar({ collapsed, onToggleCollapse, user }) {
  const { theme, toggleTheme } = useTheme()

  return (
    <header className="flex h-16 flex-none items-center gap-3 border-b border-border bg-surface px-4 sm:px-6">
      <IconButton
        onClick={onToggleCollapse}
        aria-pressed={collapsed}
        title={collapsed ? 'Expand sidebar' : 'Collapse sidebar'}
      >
        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.2" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
          <rect x="3" y="4" width="18" height="16" rx="2" />
          <path d="M9 4v16" />
        </svg>
        <span className="sr-only">{collapsed ? 'Expand sidebar' : 'Collapse sidebar'}</span>
      </IconButton>

      <div className="relative flex-1 max-w-sm">
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.2" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true" className="pointer-events-none absolute left-3 top-1/2 -translate-y-1/2 text-text-muted">
          <circle cx="11" cy="11" r="7" />
          <path d="M21 21l-4.35-4.35" />
        </svg>
        <label htmlFor="topbar-search" className="sr-only">
          Search
        </label>
        <input
          id="topbar-search"
          type="text"
          disabled
          placeholder="Search (coming soon)"
          title="Search isn't available yet"
          className="w-full cursor-not-allowed rounded-md border border-border bg-surface-raised py-2 pl-9 pr-3 text-sm text-text-muted placeholder:text-text-muted focus:outline-none"
        />
      </div>

      <div className="ml-auto flex items-center gap-1.5">
        <IconButton disabled title="Notifications aren't available yet">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.2" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
            <path d="M18 8a6 6 0 00-12 0c0 7-3 9-3 9h18s-3-2-3-9" />
            <path d="M13.73 21a2 2 0 01-3.46 0" />
          </svg>
          <span className="sr-only">Notifications</span>
        </IconButton>

        <IconButton
          onClick={toggleTheme}
          aria-pressed={theme === 'light'}
          title={theme === 'dark' ? 'Switch to light theme' : 'Switch to dark theme'}
        >
          {theme === 'dark' ? (
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.2" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
              <circle cx="12" cy="12" r="4.5" />
              <path d="M12 2v2M12 20v2M4.2 4.2l1.4 1.4M18.4 18.4l1.4 1.4M2 12h2M20 12h2M4.2 19.8l1.4-1.4M18.4 5.6l1.4-1.4" />
            </svg>
          ) : (
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.2" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
              <path d="M21 12.79A9 9 0 1111.21 3 7 7 0 0021 12.79z" />
            </svg>
          )}
          <span className="sr-only">{theme === 'dark' ? 'Switch to light theme' : 'Switch to dark theme'}</span>
        </IconButton>

        <span
          className="ml-1 flex h-8 w-8 flex-none items-center justify-center rounded-full bg-surface-raised text-xs font-bold text-text-primary"
          title={user.name}
          aria-hidden="true"
        >
          {initials(user.name)}
        </span>
      </div>
    </header>
  )
}
