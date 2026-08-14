import { useEffect, useState } from 'react'
import ResumeInput from './components/ResumeInput'
import SkillsChecklist from './components/SkillsChecklist'
import GapReportScreen from './components/GapReportScreen'
import ProcessingScreen from './components/ProcessingScreen'
import HistoryScreen from './components/HistoryScreen'
import AuthScreen from './components/AuthScreen'
import LandingScreen from './components/LandingScreen'
import { submitMatch, getToken, setToken, clearToken, onUnauthorized } from './api'

// A local POST /api/match usually finishes in well under this time.
// Without a floor, the Processing screen would flash by too fast to
// register as a real state change - this guarantees it stays on screen
// long enough to actually be seen, even on a fast connection.
const MIN_PROCESSING_MS = 1200

function wait(ms) {
  return new Promise((resolve) => setTimeout(resolve, ms))
}

const USER_KEY = 'skillgap_user'

function loadStoredUser() {
  const stored = localStorage.getItem(USER_KEY)
  return stored ? JSON.parse(stored) : null
}

export default function App() {
  // No token stored (or a stale one) both collapse to "logged out" -
  // whichever the token turns out to be, the first protected request
  // will 401 and onUnauthorized clears the rest.
  const [user, setUser] = useState(() => (getToken() ? loadStoredUser() : null))
  const [preAuthScreen, setPreAuthScreen] = useState('landing') // 'landing' | 'auth'
  const [view, setView] = useState('compare') // 'compare' | 'history'
  const [resumeText, setResumeText] = useState('')
  const [jdText, setJdText] = useState('')
  const [selectedSkills, setSelectedSkills] = useState({})
  const [stage, setStage] = useState('form') // 'form' | 'processing' | 'result'
  const [submitError, setSubmitError] = useState('')
  const [report, setReport] = useState(null)

  useEffect(() => {
    onUnauthorized(() => {
      setUser(null)
      localStorage.removeItem(USER_KEY)
    })
  }, [])

  function handleAuthSuccess({ token, name, email }) {
    setToken(token)
    localStorage.setItem(USER_KEY, JSON.stringify({ name, email }))
    setUser({ name, email })
  }

  function handleLogout() {
    clearToken()
    localStorage.removeItem(USER_KEY)
    setUser(null)
    setPreAuthScreen('landing')
    setView('compare')
    setStage('form')
    setReport(null)
  }

  async function handleSubmit(event) {
    event.preventDefault()
    setSubmitError('')

    if (!resumeText.trim()) {
      setSubmitError('Please provide your resume text (upload a PDF or paste it).')
      return
    }
    if (!jdText.trim()) {
      setSubmitError('Please paste the job description.')
      return
    }
    const requiredSkills = Object.entries(selectedSkills).map(([skillName, { core }]) => ({
      skillName,
      core,
    }))
    if (requiredSkills.length === 0) {
      setSubmitError('Please tick at least one required skill.')
      return
    }

    setStage('processing')
    try {
      const [result] = await Promise.all([
        submitMatch({ resumeText, jdText, requiredSkills }),
        wait(MIN_PROCESSING_MS),
      ])
      setReport(result)
      setStage('result')
    } catch (error) {
      setSubmitError(error.message)
      setStage('form')
    }
  }

  function handleStartOver() {
    setReport(null)
    setStage('form')
  }

  if (!user) {
    return (
      <div className="min-h-screen">
        {preAuthScreen === 'landing' ? (
          <LandingScreen onStart={() => setPreAuthScreen('auth')} />
        ) : (
          <AuthScreen onAuthSuccess={handleAuthSuccess} />
        )}
      </div>
    )
  }

  const navPillActive = 'bg-surface-1 text-accent-dark shadow-sm'
  const navPillInactive = 'text-text-secondary hover:text-accent-dark'
  const navPillBase =
    'rounded-full px-4 py-1.5 font-semibold transition-colors focus:outline-none focus:ring-2 focus:ring-inset focus:ring-accent'

  return (
    <div className="min-h-screen">
      <header className="border-b border-border bg-surface-1">
        <div className="mx-auto flex max-w-5xl flex-wrap items-center justify-between gap-4 px-4 py-4 sm:px-6">
          <div className="flex items-center gap-2.5">
            <span className="flex h-8 w-8 items-center justify-center rounded-md bg-gradient-to-br from-accent to-violet text-sm font-extrabold text-white shadow-[0_4px_10px_rgba(42,110,224,0.35)]">
              SG
            </span>
            <span className="font-display text-lg font-extrabold text-text-primary">SkillGap AI</span>
          </div>

          <div className="flex items-center gap-4">
            <nav
              className="flex gap-1 rounded-full border border-border bg-surface-2 p-1 text-sm"
              aria-label="Screens"
            >
              <button
                type="button"
                onClick={() => setView('compare')}
                aria-current={view === 'compare' ? 'page' : undefined}
                className={`${navPillBase} ${view === 'compare' ? navPillActive : navPillInactive}`}
              >
                Upload &amp; Compare
              </button>
              <button
                type="button"
                onClick={() => setView('history')}
                aria-current={view === 'history' ? 'page' : undefined}
                className={`${navPillBase} ${view === 'history' ? navPillActive : navPillInactive}`}
              >
                History
              </button>
            </nav>

            <div className="flex items-center gap-3 text-sm">
              <span className="hidden text-text-muted sm:inline">{user.name}</span>
              <button
                type="button"
                onClick={handleLogout}
                className="rounded-md border border-border px-3.5 py-1.5 font-bold text-text-primary transition-colors hover:border-accent hover:text-accent-dark focus:outline-none focus:ring-2 focus:ring-inset focus:ring-accent"
              >
                Log out
              </button>
            </div>
          </div>
        </div>
      </header>

      <main className="mx-auto max-w-5xl space-y-6 px-4 py-10 sm:px-6">
        {view === 'history' && <HistoryScreen />}

        {view === 'compare' && stage === 'form' && (
          <form onSubmit={handleSubmit} className="space-y-5">
            <div>
              <h2 className="text-xl font-bold text-text-primary">Upload &amp; Compare</h2>
              <p className="mt-0.5 text-sm text-text-muted">Both sides are needed to run an analysis</p>
            </div>

            <div className="grid grid-cols-1 gap-5 lg:grid-cols-2">
              <ResumeInput resumeText={resumeText} onResumeTextChange={setResumeText} />

              <div className="rounded-lg border border-border bg-surface-1 p-6 shadow-sm">
                <div className="mb-4 flex items-center gap-3">
                  <span className="flex h-9 w-9 flex-none items-center justify-center rounded-md bg-accent-tint text-accent-dark">
                    <svg
                      width="18"
                      height="18"
                      viewBox="0 0 24 24"
                      fill="none"
                      stroke="currentColor"
                      strokeWidth="2.2"
                      strokeLinecap="round"
                      strokeLinejoin="round"
                      aria-hidden="true"
                    >
                      <path d="M9 12h6m-6 4h6M9 8h1M5 21h14a2 2 0 002-2V7l-5-5H5a2 2 0 00-2 2v15a2 2 0 002 2z" />
                    </svg>
                  </span>
                  <div>
                    <h3 className="text-sm font-bold text-text-primary">Job Description</h3>
                    <p className="text-xs text-text-muted">Paste the full job posting text</p>
                  </div>
                </div>
                <label htmlFor="jd-text" className="sr-only">
                  Job description
                </label>
                <textarea
                  id="jd-text"
                  rows={12}
                  value={jdText}
                  onChange={(event) => setJdText(event.target.value)}
                  placeholder="Paste the job description here..."
                  className="w-full rounded-md border border-border p-3.5 text-sm text-text-primary transition-[border-color,box-shadow] focus:border-accent focus:outline-none focus:ring-2 focus:ring-accent-tint"
                />
              </div>
            </div>

            <SkillsChecklist selectedSkills={selectedSkills} onChange={setSelectedSkills} />

            <div className="flex flex-wrap items-center gap-4">
              <button
                type="submit"
                className="inline-flex items-center gap-2 rounded-md bg-gradient-to-br from-accent to-violet px-6 py-3 text-sm font-bold text-white shadow-[0_8px_18px_rgba(42,110,224,0.28)] transition-[transform,box-shadow] hover:-translate-y-0.5 hover:shadow-[0_12px_24px_rgba(42,110,224,0.36)] focus:outline-none focus:ring-2 focus:ring-inset focus:ring-white active:scale-[0.97]"
              >
                <svg
                  className="h-4 w-4"
                  viewBox="0 0 24 24"
                  fill="none"
                  stroke="currentColor"
                  strokeWidth="2.4"
                  strokeLinecap="round"
                  aria-hidden="true"
                >
                  <path d="M13 5l7 7-7 7M5 12h15" />
                </svg>
                Analyze
              </button>
              {submitError && (
                <p className="text-sm font-medium text-critical" role="alert">
                  {submitError}
                </p>
              )}
            </div>
          </form>
        )}

        {view === 'compare' && stage === 'processing' && <ProcessingScreen />}

        {view === 'compare' && stage === 'result' && report && (
          <GapReportScreen report={report} onStartOver={handleStartOver} />
        )}
      </main>
    </div>
  )
}
