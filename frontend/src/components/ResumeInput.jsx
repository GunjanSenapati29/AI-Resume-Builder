import { useState } from 'react'
import { extractResumeText } from '../api'

/**
 * Lets the user provide resume text either by uploading a PDF (extracted
 * via the backend's PDFBox endpoint) or by pasting text directly. After a
 * successful PDF extraction the text still lands in the same editable
 * textarea, so a bad extraction can always be fixed by hand instead of
 * blocking the user - that's the "manual-paste fallback" from the roadmap.
 *
 * Phase 11: the upload mode is styled as a dropzone, but stays
 * click-to-browse only - real drag-and-drop would be a new interaction,
 * not a visual change, so the copy says "click" instead of promising
 * something that doesn't work.
 *
 * Phase 12: restyled to Design System v2 tokens.
 */
export default function ResumeInput({ resumeText, onResumeTextChange }) {
  const [mode, setMode] = useState('paste')
  const [extracting, setExtracting] = useState(false)
  const [extractError, setExtractError] = useState('')
  const [extractedInfo, setExtractedInfo] = useState('')

  async function handleFileChange(event) {
    const file = event.target.files?.[0]
    if (!file) {
      return
    }

    setExtractError('')
    setExtractedInfo('')

    // Checked client-side (not just left to the backend's PDFBox error)
    // so a wrong file type fails immediately with a plain-language
    // message instead of waiting on a round trip first.
    if (file.type !== 'application/pdf') {
      setExtractError('Please upload a PDF file.')
      event.target.value = ''
      return
    }

    setExtracting(true)

    try {
      const result = await extractResumeText(file)
      onResumeTextChange(result.resumeText)
      setExtractedInfo(
        `Extracted ${result.characterCount} characters from "${file.name}". Review and edit below if needed.`,
      )
    } catch (error) {
      setExtractError(error.message)
    } finally {
      setExtracting(false)
    }
  }

  return (
    <fieldset className="rounded-lg border border-border bg-surface p-6">
      <div className="mb-4 flex items-center gap-3">
        <span className="flex h-9 w-9 flex-none items-center justify-center rounded-md border border-border text-text-secondary">
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
            <path d="M14 2H6a2 2 0 00-2 2v16a2 2 0 002 2h12a2 2 0 002-2V8z" />
            <path d="M14 2v6h6" />
          </svg>
        </span>
        <div>
          <legend className="text-sm font-bold text-text-primary">Your Resume</legend>
          <p className="text-xs text-text-muted">Upload a PDF, or paste text instead</p>
        </div>
      </div>

      <div className="mb-4 flex gap-1 rounded-md border border-border bg-surface-raised p-1 text-sm" role="group" aria-label="Resume input method">
        <button
          type="button"
          aria-pressed={mode === 'upload'}
          onClick={() => setMode('upload')}
          className={`flex-1 rounded-md px-3 py-1.5 font-semibold transition-colors focus:outline-none focus:ring-2 focus:ring-inset focus:ring-accent ${
            mode === 'upload'
              ? 'bg-surface text-accent shadow-sm'
              : 'text-text-secondary hover:text-text-primary'
          }`}
        >
          Upload PDF
        </button>
        <button
          type="button"
          aria-pressed={mode === 'paste'}
          onClick={() => setMode('paste')}
          className={`flex-1 rounded-md px-3 py-1.5 font-semibold transition-colors focus:outline-none focus:ring-2 focus:ring-inset focus:ring-accent ${
            mode === 'paste'
              ? 'bg-surface text-accent shadow-sm'
              : 'text-text-secondary hover:text-text-primary'
          }`}
        >
          Paste text
        </button>
      </div>

      {mode === 'upload' && (
        <div className="mb-4">
          <input
            id="resume-file"
            type="file"
            accept="application/pdf"
            onChange={handleFileChange}
            disabled={extracting}
            className="peer sr-only"
          />
          <label
            htmlFor="resume-file"
            className="block cursor-pointer rounded-md border-2 border-dashed border-border p-8 text-center text-sm text-text-muted transition-colors hover:border-accent hover:bg-surface-hover peer-focus-visible:border-accent peer-focus-visible:bg-surface-hover peer-focus-visible:ring-2 peer-focus-visible:ring-accent peer-disabled:cursor-not-allowed peer-disabled:opacity-60"
          >
            <svg
              className="mx-auto mb-2.5"
              width="26"
              height="26"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              strokeWidth="2"
              strokeLinecap="round"
              strokeLinejoin="round"
              aria-hidden="true"
            >
              <path d="M12 16V4m0 0l-4 4m4-4l4 4" />
              <path d="M4 16v2a2 2 0 002 2h12a2 2 0 002-2v-2" />
            </svg>
            <div>
              Click to choose your resume PDF
              <br />
              <span className="text-xs">up to 5MB</span>
            </div>
          </label>
          {extracting && (
            <p className="mt-2 text-sm text-text-muted" role="status">
              Extracting text from your PDF...
            </p>
          )}
          {extractError && (
            <p className="mt-2 text-sm font-medium text-critical" role="alert">
              {extractError}
            </p>
          )}
          {extractedInfo && !extractError && (
            <p className="mt-2 text-sm font-medium text-good" role="status">
              {extractedInfo}
            </p>
          )}
        </div>
      )}

      <div>
        <label htmlFor="resume-text" className="mb-1.5 block text-xs font-bold text-text-secondary">
          {mode === 'upload' ? 'Extracted text (edit if needed)' : 'Paste your resume text'}
        </label>
        <textarea
          id="resume-text"
          rows={10}
          value={resumeText}
          onChange={(event) => onResumeTextChange(event.target.value)}
          placeholder="Paste your resume text here..."
          className="w-full rounded-md border border-border bg-surface p-3.5 text-sm text-text-primary transition-[border-color,box-shadow] focus:border-accent focus:outline-none focus:ring-2 focus:ring-accent"
        />
      </div>
    </fieldset>
  )
}
