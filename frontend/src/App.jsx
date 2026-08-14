import { useState } from 'react'
import ResumeInput from './components/ResumeInput'
import SkillsChecklist from './components/SkillsChecklist'
import MatchResultSummary from './components/MatchResultSummary'
import { submitMatch } from './api'

export default function App() {
  const [resumeText, setResumeText] = useState('')
  const [jdText, setJdText] = useState('')
  const [selectedSkills, setSelectedSkills] = useState({})
  const [submitting, setSubmitting] = useState(false)
  const [submitError, setSubmitError] = useState('')
  const [report, setReport] = useState(null)

  async function handleSubmit(event) {
    event.preventDefault()
    setSubmitError('')
    setReport(null)

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

    setSubmitting(true)
    try {
      const result = await submitMatch({ resumeText, jdText, requiredSkills })
      setReport(result)
    } catch (error) {
      setSubmitError(error.message)
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <div className="min-h-screen bg-slate-50">
      <header className="border-b border-slate-200 bg-white">
        <div className="mx-auto max-w-3xl px-4 py-4">
          <h1 className="text-xl font-semibold text-slate-900">SkillGap AI</h1>
          <p className="text-sm text-slate-500">Upload &amp; Compare</p>
        </div>
      </header>

      <main className="mx-auto max-w-3xl space-y-6 px-4 py-6">
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
            disabled={submitting}
            className="w-full rounded-md bg-blue-600 px-4 py-2.5 font-medium text-white hover:bg-blue-700 disabled:cursor-not-allowed disabled:bg-slate-300"
          >
            {submitting ? 'Comparing...' : 'Compare'}
          </button>
        </form>

        {report && <MatchResultSummary report={report} />}
      </main>
    </div>
  )
}
