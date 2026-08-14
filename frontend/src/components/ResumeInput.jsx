import { useState } from 'react'
import { extractResumeText } from '../api'

/**
 * Lets the user provide resume text either by uploading a PDF (extracted
 * via the backend's PDFBox endpoint) or by pasting text directly. After a
 * successful PDF extraction the text still lands in the same editable
 * textarea, so a bad extraction can always be fixed by hand instead of
 * blocking the user - that's the "manual-paste fallback" from the roadmap.
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
    <fieldset className="space-y-3">
      <legend className="text-sm font-semibold text-slate-700">Resume</legend>

      <div className="flex gap-2 text-sm" role="group" aria-label="Resume input method">
        <button
          type="button"
          aria-pressed={mode === 'upload'}
          onClick={() => setMode('upload')}
          className={`rounded-md px-3 py-1.5 font-medium focus:outline-none focus:ring-2 focus:ring-inset ${
            mode === 'upload'
              ? 'bg-blue-600 text-white focus:ring-white'
              : 'bg-slate-100 text-slate-700 hover:bg-slate-200 focus:ring-blue-500'
          }`}
        >
          Upload PDF
        </button>
        <button
          type="button"
          aria-pressed={mode === 'paste'}
          onClick={() => setMode('paste')}
          className={`rounded-md px-3 py-1.5 font-medium focus:outline-none focus:ring-2 focus:ring-inset ${
            mode === 'paste'
              ? 'bg-blue-600 text-white focus:ring-white'
              : 'bg-slate-100 text-slate-700 hover:bg-slate-200 focus:ring-blue-500'
          }`}
        >
          Paste text
        </button>
      </div>

      {mode === 'upload' && (
        <div>
          <label htmlFor="resume-file" className="sr-only">
            Resume PDF file
          </label>
          <input
            id="resume-file"
            type="file"
            accept="application/pdf"
            onChange={handleFileChange}
            disabled={extracting}
            className="block w-full text-sm text-slate-700 file:mr-3 file:rounded-md file:border-0 file:bg-blue-600 file:px-3 file:py-1.5 file:font-medium file:text-white hover:file:bg-blue-700"
          />
          {extracting && (
            <p className="mt-2 text-sm text-slate-500" role="status">
              Extracting text from your PDF...
            </p>
          )}
          {extractError && (
            <p className="mt-2 text-sm text-red-600" role="alert">
              {extractError}
            </p>
          )}
          {extractedInfo && !extractError && (
            <p className="mt-2 text-sm text-green-700" role="status">
              {extractedInfo}
            </p>
          )}
        </div>
      )}

      <div>
        <label htmlFor="resume-text" className="mb-1 block text-sm text-slate-600">
          {mode === 'upload' ? 'Extracted text (edit if needed)' : 'Paste your resume text'}
        </label>
        <textarea
          id="resume-text"
          rows={10}
          value={resumeText}
          onChange={(event) => onResumeTextChange(event.target.value)}
          placeholder="Paste your resume text here..."
          className="w-full rounded-md border border-slate-300 p-3 text-sm text-slate-800 focus:border-blue-500 focus:outline-none focus:ring-2 focus:ring-blue-200"
        />
      </div>
    </fieldset>
  )
}
