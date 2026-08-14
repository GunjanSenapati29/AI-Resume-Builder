const JSON_HEADERS = { 'Content-Type': 'application/json' }

async function readErrorMessage(response) {
  const text = await response.text()
  return text || `Request failed with status ${response.status}.`
}

export async function fetchSkills() {
  const response = await fetch('/api/skills')
  if (!response.ok) {
    throw new Error(await readErrorMessage(response))
  }
  return response.json()
}

export async function extractResumeText(file) {
  const formData = new FormData()
  formData.append('file', file)

  const response = await fetch('/api/resumes/extract-text', {
    method: 'POST',
    body: formData,
  })
  if (!response.ok) {
    throw new Error(await readErrorMessage(response))
  }
  return response.json()
}

export async function submitMatch({ resumeText, jdText, requiredSkills }) {
  const response = await fetch('/api/match', {
    method: 'POST',
    headers: JSON_HEADERS,
    body: JSON.stringify({ resumeText, jdText, requiredSkills }),
  })
  if (!response.ok) {
    throw new Error(await readErrorMessage(response))
  }
  return response.json()
}
