import { useEffect, useState } from 'react'
import { fetchInterviewQuestions } from '../api'

function SkillQuestionsCard({ skill }) {
  return (
    <li className="rounded-lg border border-border bg-surface p-4">
      <span className="text-sm font-bold text-text-primary">{skill.skillName}</span>

      <ol className="mt-3 flex flex-col gap-2">
        {skill.questions.map((question, index) => (
          <li key={index} className="flex gap-2 text-sm text-text-secondary">
            <span className="flex-none font-mono text-xs font-bold text-text-muted">{index + 1}.</span>
            <span>{question}</span>
          </li>
        ))}
      </ol>
    </li>
  )
}

/**
 * Phase 19: the Interview Prep page - 3 practice questions for every
 * MATCHED skill on the user's MOST RECENT Gap Report. Matched (not
 * missing/underemphasized) because those are the skills the resume
 * actually claims, which is what an interviewer would probe - the
 * Skill Roadmap (Phase 17) already covers the gaps. Every skill gets
 * either a curated 3-question set or a generic fallback with the skill
 * name filled in (InterviewQuestionService); no invented specifics.
 *
 * `questions` state: `undefined` while loading, `null` when the user
 * has no analyzed reports yet (a normal empty state, not an error - see
 * fetchInterviewQuestions's 204 handling), otherwise the real payload.
 */
export default function InterviewPrepScreen({ onGoToAnalyze }) {
  const [questions, setQuestions] = useState(undefined)
  const [error, setError] = useState('')

  useEffect(() => {
    fetchInterviewQuestions()
      .then(setQuestions)
      .catch((err) => setError(err.message))
  }, [])

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-xl font-bold text-text-primary">Interview Prep</h1>
        <p className="mt-0.5 text-sm text-text-muted">
          Practice questions for the skills matched on your most recent Gap Report
        </p>
      </div>

      {error && (
        <p className="text-sm font-medium text-critical" role="alert">
          Could not load your interview questions: {error}
        </p>
      )}

      {!error && questions === undefined && (
        <p className="text-sm text-text-muted">Loading your interview questions...</p>
      )}

      {!error && questions === null && (
        <div className="rounded-lg border border-border bg-surface p-8 text-center">
          <p className="text-sm text-text-muted">Analyze a resume first to get your interview questions.</p>
          <button
            type="button"
            onClick={onGoToAnalyze}
            className="mt-4 inline-flex items-center gap-2 rounded-md bg-accent px-5 py-2.5 text-sm font-bold text-white transition-colors hover:bg-accent-hover focus:outline-none focus:ring-2 focus:ring-inset focus:ring-white active:scale-[0.97]"
          >
            Go to Analyze Resume
          </button>
        </div>
      )}

      {!error && questions && questions.skills.length === 0 && (
        <div className="rounded-lg border border-border bg-surface p-8 text-center">
          <p className="text-sm text-text-muted">No matched skills to prep yet.</p>
        </div>
      )}

      {!error && questions && questions.skills.length > 0 && (
        <ul className="grid grid-cols-1 gap-3 md:grid-cols-2">
          {questions.skills.map((skill) => (
            <SkillQuestionsCard key={skill.skillName} skill={skill} />
          ))}
        </ul>
      )}
    </div>
  )
}
