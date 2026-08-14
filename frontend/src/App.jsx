import { useEffect, useState } from 'react'
import ResumeInput from './components/ResumeInput'
import SkillsChecklist from './components/SkillsChecklist'
import GapReportScreen from './components/GapReportScreen'
import ProcessingScreen from './components/ProcessingScreen'
import HistoryScreen from './components/HistoryScreen'
import AuthScreen from './components/AuthScreen'
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
      <div className="min-h-screen bg-slate-50 px-4">
        <AuthScreen onAuthSuccess={handleAuthSuccess} />
      </div>
    )
  }

  return (
    <div className="min-h-screen bg-slate-50">
      <header className="border-b border-slate-200 bg-white">
        <div className="mx-auto max-w-3xl px-4 py-4">
          <div className="flex items-start justify-between gap-4">
            <h1 className="text-xl font-semibold text-slate-900">SkillGap AI</h1>
            <div className="flex items-center gap-3 text-sm text-slate-500">
              <span>{user.name}</span>
              <button
                type="button"
                onClick={handleLogout}
                className="rounded-md border border-slate-300 px-2.5 py-1 font-medium text-slate-700 hover:bg-slate-50"
              >
                Log out
              </button>
            </div>
          </div>
          <p className="text-sm text-slate-500">
            {view === 'history'
              ? 'History'
              : stage === 'processing'
                ? 'Processing'
                : stage === 'result'
                  ? 'Gap Report'
                  : 'Upload & Compare'}
          </p>
          <nav className="mt-3 flex gap-2 text-sm" aria-label="Screens">
            <button
              type="button"
              onClick={() => setView('compare')}
              aria-current={view === 'compare' ? 'page' : undefined}
              className={`rounded-md px-3 py-1.5 font-medium ${
                view === 'compare'
                  ? 'bg-blue-600 text-white'
                  : 'bg-slate-100 text-slate-700 hover:bg-slate-200'
              }`}
            >
              Upload &amp; Compare
            </button>
            <button
              type="button"
              onClick={() => setView('history')}
              aria-current={view === 'history' ? 'page' : undefined}
              className={`rounded-md px-3 py-1.5 font-medium ${
                view === 'history'
                  ? 'bg-blue-600 text-white'
                  : 'bg-slate-100 text-slate-700 hover:bg-slate-200'
              }`}
            >
              History
            </button>
          </nav>
        </div>
      </header>

      <main className="mx-auto max-w-3xl space-y-6 px-4 py-6">
        {view === 'history' && <HistoryScreen />}

        {view === 'compare' && stage === 'form' && (
          <form
            onSubmit={handleSubmit}
            className="space-y-6 rounded-lg border border-slate-200 bg-white p-5"
          >
            <ResumeInput resumeText={resumeText} onResumeTextChange={setResumeText} />

            <div>
              <label htmlFor="jd-text" className="mb-1 block text-sm font-semibold text-slate-700">
                Job description
              </label>
              <textarea
                id="jd-text"
                rows={6}
                value={jdText}
                onChange={(event) => setJdText(event.target.value)}
                placeholder="Paste the job description here..."
                className="w-full rounded-md border border-slate-300 p-3 text-sm text-slate-800 focus:border-blue-500 focus:outline-none focus:ring-2 focus:ring-blue-200"
              />
            </div>

            <SkillsChecklist selectedSkills={selectedSkills} onChange={setSelectedSkills} />

            {submitError && (
              <p className="text-sm text-red-600" role="alert">
                {submitError}
              </p>
            )}

            <button
              type="submit"
              className="w-full rounded-md bg-blue-600 px-4 py-2.5 font-medium text-white hover:bg-blue-700"
            >
              Compare
            </button>
          </form>
        )}

        {view === 'compare' && stage === 'processing' && <ProcessingScreen />}

        {view === 'compare' && stage === 'result' && report && (
          <>
            <GapReportScreen report={report} />
            <button
              type="button"
              onClick={handleStartOver}
              className="w-full rounded-md border border-slate-300 bg-white px-4 py-2.5 font-medium text-slate-700 hover:bg-slate-50"
            >
              Start a new comparison
            </button>
          </>
        )}
      </main>
    </div>
  )
}
