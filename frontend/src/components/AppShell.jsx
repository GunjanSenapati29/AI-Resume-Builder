import { useEffect, useState } from 'react'
import Sidebar from './Sidebar'
import TopBar from './TopBar'

// Below this width the sidebar starts collapsed by default (icon-only)
// so it doesn't eat the content column on a phone/small tablet.
const SMALL_SCREEN_QUERY = '(max-width: 1023px)'

/**
 * Phase 12: the post-auth app shell - sidebar + top bar + a scrollable
 * main content area, replacing the old header-with-pill-nav layout.
 * `collapsed` starts from whatever the screen size implies, and is
 * re-applied whenever the viewport crosses the small-screen breakpoint
 * (mobile window resized, device rotated, etc.) - but a manual toggle
 * via TopBar in between two such crossings is left alone, so opening
 * the sidebar on a phone doesn't get silently undone by an unrelated
 * resize event.
 */
export default function AppShell({ user, onLogout, children }) {
  const [collapsed, setCollapsed] = useState(
    () => typeof window !== 'undefined' && window.matchMedia(SMALL_SCREEN_QUERY).matches,
  )

  useEffect(() => {
    const mediaQuery = window.matchMedia(SMALL_SCREEN_QUERY)
    function handleChange(event) {
      setCollapsed(event.matches)
    }
    mediaQuery.addEventListener('change', handleChange)
    return () => mediaQuery.removeEventListener('change', handleChange)
  }, [])

  return (
    <div className="flex h-screen overflow-hidden bg-bg">
      <Sidebar collapsed={collapsed} user={user} onLogout={onLogout} />
      <div className="flex min-w-0 flex-1 flex-col">
        <TopBar collapsed={collapsed} onToggleCollapse={() => setCollapsed((current) => !current)} user={user} />
        <main className="flex-1 overflow-y-auto">
          <div className="mx-auto max-w-5xl space-y-6 px-4 py-10 sm:px-6">{children}</div>
        </main>
      </div>
    </div>
  )
}
