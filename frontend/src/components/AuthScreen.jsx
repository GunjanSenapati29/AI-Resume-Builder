import { useState } from 'react'
import { login, signup } from '../api'

/**
 * Phase 6e: replaces the guest-user placeholder. One screen, two modes -
 * login and signup share the same email/password fields, signup adds a
 * name field. On success, hands the {token, name, email} back up to App
 * via onAuthSuccess, which is what actually unlocks the rest of the app.
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

  return (
    <div className="mx-auto mt-12 max-w-sm">
      <div className="mb-6 text-center">
        <h1 className="text-xl font-semibold text-slate-900">SkillGap AI</h1>
        <p className="mt-1 text-sm text-slate-500">
          {mode === 'login' ? 'Log in to see your reports' : 'Create an account to get started'}
        </p>
      </div>

      <form
        onSubmit={handleSubmit}
        className="space-y-4 rounded-lg border border-slate-200 bg-white p-5"
      >
        <div className="flex gap-2 text-sm" role="group" aria-label="Auth mode">
          <button
            type="button"
            aria-pressed={mode === 'login'}
            onClick={() => switchMode('login')}
            className={`flex-1 rounded-md px-3 py-1.5 font-medium focus:outline-none focus:ring-2 focus:ring-inset ${
              mode === 'login'
                ? 'bg-blue-600 text-white focus:ring-white'
                : 'bg-slate-100 text-slate-700 hover:bg-slate-200 focus:ring-blue-500'
            }`}
          >
            Log in
          </button>
          <button
            type="button"
            aria-pressed={mode === 'signup'}
            onClick={() => switchMode('signup')}
            className={`flex-1 rounded-md px-3 py-1.5 font-medium focus:outline-none focus:ring-2 focus:ring-inset ${
              mode === 'signup'
                ? 'bg-blue-600 text-white focus:ring-white'
                : 'bg-slate-100 text-slate-700 hover:bg-slate-200 focus:ring-blue-500'
            }`}
          >
            Sign up
          </button>
        </div>

        {mode === 'signup' && (
          <div>
            <label htmlFor="auth-name" className="mb-1 block text-sm font-semibold text-slate-700">
              Name
            </label>
            <input
              id="auth-name"
              type="text"
              value={name}
              onChange={(event) => setName(event.target.value)}
              autoComplete="name"
              className="w-full rounded-md border border-slate-300 p-2.5 text-sm text-slate-800 focus:border-blue-500 focus:outline-none focus:ring-2 focus:ring-blue-200"
            />
          </div>
        )}

        <div>
          <label htmlFor="auth-email" className="mb-1 block text-sm font-semibold text-slate-700">
            Email
          </label>
          <input
            id="auth-email"
            type="email"
            value={email}
            onChange={(event) => setEmail(event.target.value)}
            autoComplete="email"
            className="w-full rounded-md border border-slate-300 p-2.5 text-sm text-slate-800 focus:border-blue-500 focus:outline-none focus:ring-2 focus:ring-blue-200"
          />
        </div>

        <div>
          <label htmlFor="auth-password" className="mb-1 block text-sm font-semibold text-slate-700">
            Password
          </label>
          <input
            id="auth-password"
            type="password"
            value={password}
            onChange={(event) => setPassword(event.target.value)}
            autoComplete={mode === 'signup' ? 'new-password' : 'current-password'}
            className="w-full rounded-md border border-slate-300 p-2.5 text-sm text-slate-800 focus:border-blue-500 focus:outline-none focus:ring-2 focus:ring-blue-200"
          />
          {mode === 'signup' && (
            <p className="mt-1 text-xs text-slate-500">At least 8 characters.</p>
          )}
        </div>

        {error && (
          <p className="text-sm text-red-600" role="alert">
            {error}
          </p>
        )}

        <button
          type="submit"
          disabled={submitting}
          className="w-full rounded-md bg-blue-600 px-4 py-2.5 font-medium text-white hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-inset focus:ring-white disabled:cursor-not-allowed disabled:bg-blue-300"
        >
          {submitting
            ? mode === 'login' ? 'Logging in...' : 'Creating account...'
            : mode === 'login' ? 'Log in' : 'Create account'}
        </button>
      </form>
    </div>
  )
}
