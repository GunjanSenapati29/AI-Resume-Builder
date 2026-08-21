const JSON_HEADERS = { 'Content-Type': 'application/json' }
const TOKEN_KEY = 'skillgap_token'

// Phase 6e: any component that wants to react to "the token was rejected
// (missing/expired/invalid) so the user needs to log in again" registers
// one callback here via onUnauthorized(). Keeps every protected fetch
// call below from having to know about App's auth state directly.
let unauthorizedHandler = null

export function onUnauthorized(handler) {
  unauthorizedHandler = handler
}

export function getToken() {
  return localStorage.getItem(TOKEN_KEY)
}

export function setToken(token) {
  localStorage.setItem(TOKEN_KEY, token)
}

export function clearToken() {
  localStorage.removeItem(TOKEN_KEY)
}

function authHeaders() {
  const token = getToken()
  return token ? { Authorization: `Bearer ${token}` } : {}
}

async function readErrorMessage(response) {
  const text = await response.text()
  return text || `Request failed with status ${response.status}.`
}

// Phase 8: fetch() itself rejects (doesn't give you a response at all)
// when the request never reaches a server - backend down, no network,
// DNS failure. Left alone, that surfaces as a raw browser string like
// "Failed to fetch", which fails the "clear, human message" bar just as
// much as a blank screen would. Every network call in this file goes
// through this instead of calling fetch() directly, so that case always
// reads the same way.
async function safeFetch(url, options) {
  try {
    return await fetch(url, options)
  } catch {
    throw new Error('Could not reach the server. Check your connection and try again.')
  }
}

// Every protected endpoint (everything except /api/auth/*) goes through
// this so a 401 (token missing/expired/invalid - see SecurityConfig on
// the backend) always triggers the same "go back to login" behavior
// instead of each caller having to check for it separately.
async function protectedFetch(url, options = {}) {
  const response = await safeFetch(url, {
    ...options,
    headers: { ...options.headers, ...authHeaders() },
  })
  if (response.status === 401) {
    clearToken()
    unauthorizedHandler?.()
    throw new Error('Your session has expired. Please log in again.')
  }
  if (!response.ok) {
    throw new Error(await readErrorMessage(response))
  }
  return response
}

export async function signup({ name, email, password }) {
  const response = await safeFetch('/api/auth/signup', {
    method: 'POST',
    headers: JSON_HEADERS,
    body: JSON.stringify({ name, email, password }),
  })
  if (!response.ok) {
    throw new Error(await readErrorMessage(response))
  }
  return response.json()
}

export async function login({ email, password }) {
  const response = await safeFetch('/api/auth/login', {
    method: 'POST',
    headers: JSON_HEADERS,
    body: JSON.stringify({ email, password }),
  })
  if (!response.ok) {
    throw new Error(await readErrorMessage(response))
  }
  return response.json()
}

export async function fetchSkills() {
  const response = await protectedFetch('/api/skills')
  return response.json()
}

export async function extractResumeText(file) {
  const formData = new FormData()
  formData.append('file', file)

  const response = await protectedFetch('/api/resumes/extract-text', {
    method: 'POST',
    body: formData,
  })
  return response.json()
}

export async function submitMatch({ resumeText, jdText, requiredSkills }) {
  const response = await protectedFetch('/api/match', {
    method: 'POST',
    headers: JSON_HEADERS,
    body: JSON.stringify({ resumeText, jdText, requiredSkills }),
  })
  return response.json()
}

export async function fetchReportHistory() {
  const response = await protectedFetch('/api/reports')
  return response.json()
}

export async function fetchReportById(id) {
  const response = await protectedFetch(`/api/reports/${id}`)
  return response.json()
}

// Phase 17: the Learning Roadmap for the user's most recent report.
// 204 means "no analyzed reports yet" - a normal empty state, not an
// error, so this returns null for that case instead of throwing.
export async function fetchLearningRoadmap() {
  const response = await protectedFetch('/api/reports/latest/learning-roadmap')
  if (response.status === 204) {
    return null
  }
  return response.json()
}

// Phase 19: interview questions for the matched skills on the user's
// most recent report. Same 204-means-null empty-state convention as
// fetchLearningRoadmap above.
export async function fetchInterviewQuestions() {
  const response = await protectedFetch('/api/reports/latest/interview-questions')
  if (response.status === 204) {
    return null
  }
  return response.json()
}

// Phase 20: role fit scores aggregated across ALL of the user's
// reports (not just the most recent one - see CareerRoleService). Same
// 204-means-null empty-state convention as fetchLearningRoadmap/
// fetchInterviewQuestions above.
export async function fetchRoleRecommendations() {
  const response = await protectedFetch('/api/reports/role-recommendations')
  if (response.status === 204) {
    return null
  }
  return response.json()
}

// Phase 21: side-by-side comparison of 2-5 of the user's own past
// reports. No 204 case here (unlike the "latest report" endpoints
// above) - the Compare Jobs page only ever calls this once the user has
// picked reports from History, so there's always at least 2 ids.
export async function fetchReportComparison(ids) {
  const response = await protectedFetch(`/api/reports/compare?ids=${ids.join(',')}`)
  return response.json()
}

// Phase 9: the PDF export. Can't just be a plain <a href> link since the
// route needs the Authorization header like every other protected
// route - so this fetches it as a blob and the caller turns that into a
// download (see GapReportScreen).
export async function fetchReportPdf(id) {
  const response = await protectedFetch(`/api/reports/${id}/pdf`)
  return response.blob()
}

// Phase 22: Resume Builder CRUD. create/update send the same
// ResumeVersionRequest shape the form builds; a 400 (missing title or
// contact.name) surfaces through protectedFetch's existing
// readErrorMessage handling, same as every other validated endpoint.
export async function fetchResumeVersions() {
  const response = await protectedFetch('/api/resume-versions')
  return response.json()
}

export async function fetchResumeVersion(id) {
  const response = await protectedFetch(`/api/resume-versions/${id}`)
  return response.json()
}

export async function createResumeVersion(payload) {
  const response = await protectedFetch('/api/resume-versions', {
    method: 'POST',
    headers: JSON_HEADERS,
    body: JSON.stringify(payload),
  })
  return response.json()
}

export async function updateResumeVersion(id, payload) {
  const response = await protectedFetch(`/api/resume-versions/${id}`, {
    method: 'PUT',
    headers: JSON_HEADERS,
    body: JSON.stringify(payload),
  })
  return response.json()
}

export async function deleteResumeVersion(id) {
  await protectedFetch(`/api/resume-versions/${id}`, { method: 'DELETE' })
}

export async function duplicateResumeVersion(id) {
  const response = await protectedFetch(`/api/resume-versions/${id}/duplicate`, { method: 'POST' })
  return response.json()
}

// Same blob-download pattern as fetchReportPdf above.
export async function fetchResumeVersionPdf(id) {
  const response = await protectedFetch(`/api/resume-versions/${id}/pdf`)
  return response.blob()
}

// Phase 23: compares exactly 2 of the user's own resume versions against
// one ad-hoc jdText/requiredSkills pair - computed live, never saved as
// a History entry (unlike POST /api/match).
export async function compareResumeVersions({ resumeVersionIds, jdText, requiredSkills }) {
  const response = await protectedFetch('/api/resume-versions/compare', {
    method: 'POST',
    headers: JSON_HEADERS,
    body: JSON.stringify({ resumeVersionIds, jdText, requiredSkills }),
  })
  return response.json()
}
