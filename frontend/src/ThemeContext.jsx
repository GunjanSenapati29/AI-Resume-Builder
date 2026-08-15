import { createContext, useContext, useEffect, useState } from 'react'

const THEME_KEY = 'skillgap_theme'
const ThemeContext = createContext(null)

function loadStoredTheme() {
  return localStorage.getItem(THEME_KEY) === 'light' ? 'light' : 'dark'
}

/**
 * Phase 12: app-wide dark/light theme, dark by default (see
 * index.css's @theme block, which holds the dark values and needs no
 * attribute to apply). Persisted to localStorage so it survives
 * reloads and stays put across in-app navigation, and applied by
 * setting data-theme on <html> - index.css's [data-theme="light"]
 * block is what actually swaps every token.
 *
 * index.html has a tiny inline script that applies a stored "light"
 * preference before React mounts, so this effect only ever needs to
 * keep the attribute in sync after that - it doesn't cause the first-
 * paint flash it would otherwise.
 */
export function ThemeProvider({ children }) {
  const [theme, setTheme] = useState(loadStoredTheme)

  useEffect(() => {
    document.documentElement.dataset.theme = theme
    localStorage.setItem(THEME_KEY, theme)
  }, [theme])

  function toggleTheme() {
    setTheme((current) => (current === 'dark' ? 'light' : 'dark'))
  }

  return <ThemeContext.Provider value={{ theme, toggleTheme }}>{children}</ThemeContext.Provider>
}

export function useTheme() {
  const context = useContext(ThemeContext)
  if (!context) {
    throw new Error('useTheme must be used within a ThemeProvider')
  }
  return context
}
