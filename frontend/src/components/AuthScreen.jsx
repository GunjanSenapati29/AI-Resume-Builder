import { useState } from 'react'
import { login, signup } from '../api'

/**
 * Phase 6e: replaces the guest-user placeholder. One screen, two modes -
 * login and signup share the same email/password fields, signup adds a
 * name field. On success, hands the {token, name, email} back up to App
 * via onAuthSuccess, which is what actually unlocks the rest of the app.
 *
 * Phase 12: restyled to Design System v2 tokens - stays outside the app
 * shell (no sidebar/top bar pre-login), solid accent button instead of
 * the old gradient.
 */
export default function AuthScreen({ onAuthSuccess }) {
  const [mode, setMode] = useState('login') // 'login' | 'signup'
  const [name, setName] = useState('')
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState('')
  const [submitting, setSubmitting] = useState(false)

  function switchMode(nextMode) {
    setMode(nextMode)
    setError('')
  }

  async function handleSubmit(event) {
    event.preventDefault()
    setError('')

    if (!email.trim() || !password) {
      setError('Please fill in email and password.')
      return
    }
    if (mode === 'signup' && !name.trim()) {
      setError('Please enter your name.')
      return
    }
    if (mode === 'signup' && password.length < 8) {
      setError('Password must be at least 8 characters.')
      return
    }

    setSubmitting(true)
    try {
      const result = mode === 'signup'
        ? await signup({ name, email, password })
        : await login({ email, password })
      onAuthSuccess(result)
    } catch (err) {
      setError(err.message)
    } finally {
      setSubmitting(false)
    }
  }

  const fieldClasses =
    'w-full rounded-md border border-border bg-surface p-3 text-sm text-text-primary transition-[border-color,box-shadow] focus:border-accent focus:outline-none focus:ring-2 focus:ring-accent'
  const labelClasses = 'mb-1.5 block text-xs font-bold text-text-secondary'

  return (
    <div className="animate-pop-in mx-auto mt-16 w-full max-w-sm px-4 opacity-0">
      <div className="rounded-lg border border-border bg-surface p-10">
        <div className="mx-auto mb-4 flex h-[52px] w-[52px] items-center justify-center rounded-md bg-accent">
          <svg
            width="24"
            height="24"
            viewBox="0 0 24 24"
            fill="none"
            stroke="#fff"
            strokeWidth="2.2"
            strokeLinecap="round"
            strokeLinejoin="round"
            aria-hidden="true"
          >
            <path d="M20 21v-2a4 4 0 00-4-4H8a4 4 0 00-4 4v2" />
            <circle cx="12" cy="7" r="4" />
          </svg>
        </div>

        <h1 className="mb-1 text-center text-xl font-bold text-text-primary">
          {mode === 'login' ? 'Welcome back' : 'Create your account'}
        </h1>
        <p className="mb-6 text-center text-sm text-text-muted">
          {mode === 'login' ? 'Log in to see your saved reports' : 'Start comparing resumes to job descriptions'}
        </p>

        <div
          className="mb-5 flex gap-1 rounded-md border border-border bg-surface-raised p-1 text-sm"
          role="group"
          aria-label="Auth mode"
        >
          <button
            type="button"
            aria-pressed={mode === 'login'}
            onClick={() => switchMode('login')}
            className={`flex-1 rounded-md px-3 py-1.5 font-semibold transition-colors focus:outline-none focus:ring-2 focus:ring-inset ${
              mode === 'login'
                ? 'bg-surface text-accent shadow-sm focus:ring-accent'
                : 'text-text-secondary hover:text-text-primary focus:ring-accent'
            }`}
          >
            Log in
          </button>
          <button
            type="button"
            aria-pressed={mode === 'signup'}
            onClick={() => switchMode('signup')}
            className={`flex-1 rounded-md px-3 py-1.5 font-semibold transition-colors focus:outline-none focus:ring-2 focus:ring-inset ${
              mode === 'signup'
                ? 'bg-surface text-accent shadow-sm focus:ring-accent'
                : 'text-text-secondary hover:text-text-primary focus:ring-accent'
            }`}
          >
            Sign up
          </button>
        </div>

        <form onSubmit={handleSubmit} className="space-y-4">
          {mode === 'signup' && (
            <div>
              <label htmlFor="auth-name" className={labelClasses}>
                Name
              </label>
              <input
                id="auth-name"
                type="text"
                value={name}
                onChange={(event) => setName(event.target.value)}
                autoComplete="name"
                className={fieldClasses}
              />
            </div>
          )}

          <div>
            <label htmlFor="auth-email" className={labelClasses}>
              Email
            </label>
            <input
              id="auth-email"
              type="email"
              value={email}
              onChange={(event) => setEmail(event.target.value)}
              autoComplete="email"
              className={fieldClasses}
            />
          </div>

          <div>
            <label htmlFor="auth-password" className={labelClasses}>
              Password
            </label>
            <input
              id="auth-password"
              type="password"
              value={password}
              onChange={(event) => setPassword(event.target.value)}
              autoComplete={mode === 'signup' ? 'new-password' : 'current-password'}
              className={fieldClasses}
            />
            {mode === 'signup' && (
              <p className="mt-1.5 text-xs text-text-muted">At least 8 characters.</p>
            )}
          </div>

          {error && (
            <p className="text-sm font-medium text-critical" role="alert">
              {error}
            </p>
          )}

          <button
            type="submit"
            disabled={submitting}
            className="mt-2 flex w-full items-center justify-center gap-2 rounded-md bg-accent px-4 py-3 text-sm font-bold text-white transition-colors hover:bg-accent-hover focus:outline-none focus:ring-2 focus:ring-inset focus:ring-white active:scale-[0.97] disabled:cursor-not-allowed disabled:opacity-50"
          >
            {submitting
              ? mode === 'login' ? 'Logging in...' : 'Creating account...'
              : mode === 'login' ? 'Log in' : 'Create account'}
          </button>
        </form>
      </div>
    </div>
  )
}
