import { useEffect, useState } from 'react'
import {
  createResumeVersion,
  deleteResumeVersion,
  duplicateResumeVersion,
  fetchResumeVersion,
  fetchResumeVersionPdf,
  fetchResumeVersions,
  updateResumeVersion,
} from '../api'
import { formatDate } from '../dateFormat'

const BLANK_CONTACT = { name: '', email: '', phone: '', location: '', portfolioUrl: '', githubUrl: '', linkedinUrl: '' }

function blankProject() {
  return { name: '', description: '', tech: '', link: '' }
}
function blankEducation() {
  return { institution: '', degree: '', dates: '', gpa: '' }
}
function blankExperience() {
  return { company: '', role: '', dates: '', bullets: [] }
}
function blankCertification() {
  return { name: '', issuer: '', date: '' }
}

function updateAt(list, index, field, value) {
  return list.map((item, i) => (i === index ? { ...item, [field]: value } : item))
}
function removeAt(list, index) {
  return list.filter((_, i) => i !== index)
}
function hasAnyText(...values) {
  return values.some((value) => value && value.trim())
}

function slugify(title) {
  const slug = (title ?? '').trim().toLowerCase().replace(/[^a-z0-9]+/g, '-').replace(/(^-|-$)/g, '')
  return slug || 'resume'
}

// ---- shared form-building blocks ----

function Section({ title, hint, children, onAdd, addLabel }) {
  return (
    <div className="rounded-lg border border-border bg-surface p-6">
      <div className="mb-4">
        <h3 className="text-sm font-bold text-text-primary">{title}</h3>
        {hint && <p className="mt-0.5 text-xs text-text-muted">{hint}</p>}
      </div>
      <div className="space-y-4">{children}</div>
      {onAdd && (
        <button
          type="button"
          onClick={onAdd}
          className="mt-4 w-full rounded-md border border-dashed border-border-strong px-4 py-2.5 text-sm font-semibold text-text-secondary transition-colors hover:border-accent hover:text-accent focus:outline-none focus:ring-2 focus:ring-accent"
        >
          {addLabel}
        </button>
      )}
    </div>
  )
}

function EntryCard({ onRemove, children }) {
  return (
    <div className="relative rounded-md border border-border bg-surface-raised p-4">
      <button
        type="button"
        onClick={onRemove}
        className="absolute right-3 top-3 rounded-sm text-xs font-bold text-critical hover:underline focus:outline-none focus:ring-2 focus:ring-accent"
      >
        Remove
      </button>
      <div className="grid grid-cols-1 gap-3 pr-16 sm:grid-cols-2">{children}</div>
    </div>
  )
}

function Field({ label, value, onChange, placeholder, wide }) {
  return (
    <label className={`block ${wide ? 'sm:col-span-2' : ''}`}>
      <span className="mb-1.5 block text-xs font-bold text-text-secondary">{label}</span>
      <input
        type="text"
        value={value}
        onChange={(event) => onChange(event.target.value)}
        placeholder={placeholder}
        className="w-full rounded-md border border-border bg-surface p-2.5 text-sm text-text-primary transition-[border-color,box-shadow] focus:border-accent focus:outline-none focus:ring-2 focus:ring-accent"
      />
    </label>
  )
}

function TextAreaField({ label, value, onChange, placeholder }) {
  return (
    <label className="block sm:col-span-2">
      <span className="mb-1.5 block text-xs font-bold text-text-secondary">{label}</span>
      <textarea
        rows={3}
        value={value}
        onChange={(event) => onChange(event.target.value)}
        placeholder={placeholder}
        className="w-full rounded-md border border-border bg-surface p-2.5 text-sm text-text-primary transition-[border-color,box-shadow] focus:border-accent focus:outline-none focus:ring-2 focus:ring-accent"
      />
    </label>
  )
}

// ---- section blocks ----

function ContactSection({ contact, onChange }) {
  return (
    <Section title="Contact" hint="Required - at least your name">
      <div className="grid grid-cols-1 gap-3 sm:grid-cols-2">
        <Field label="Name" value={contact.name} onChange={(v) => onChange('name', v)} placeholder="Your full name" />
        <Field label="Email" value={contact.email} onChange={(v) => onChange('email', v)} placeholder="you@example.com" />
        <Field label="Phone" value={contact.phone} onChange={(v) => onChange('phone', v)} />
        <Field label="Location" value={contact.location} onChange={(v) => onChange('location', v)} placeholder="City, Country" />
        <Field label="Portfolio URL" value={contact.portfolioUrl} onChange={(v) => onChange('portfolioUrl', v)} />
        <Field label="GitHub URL" value={contact.githubUrl} onChange={(v) => onChange('githubUrl', v)} />
        <Field label="LinkedIn URL" value={contact.linkedinUrl} onChange={(v) => onChange('linkedinUrl', v)} wide />
      </div>
    </Section>
  )
}

function SummarySection({ summary, onChange }) {
  return (
    <Section title="Summary" hint="A short paragraph introducing yourself - optional">
      <textarea
        rows={4}
        value={summary}
        onChange={(event) => onChange(event.target.value)}
        placeholder="e.g. CSE undergrad focused on backend development with Java and Spring Boot."
        className="w-full rounded-md border border-border bg-surface p-2.5 text-sm text-text-primary transition-[border-color,box-shadow] focus:border-accent focus:outline-none focus:ring-2 focus:ring-accent"
      />
    </Section>
  )
}

function SkillsSection({ skills, onAdd, onRemove }) {
  const [draft, setDraft] = useState('')

  function submitAdd() {
    const trimmed = draft.trim()
    if (trimmed) {
      onAdd(trimmed)
      setDraft('')
    }
  }

  return (
    <Section title="Skills" hint="Skills you want to highlight on this resume - optional">
      <div className="flex flex-wrap gap-2">
        {skills.length === 0 && <p className="text-xs text-text-muted">No skills added yet.</p>}
        {skills.map((skill, index) => (
          <span
            key={`${skill}-${index}`}
            className="inline-flex items-center gap-1.5 rounded-full border border-border bg-surface-raised px-3 py-1.5 text-xs font-semibold text-text-primary"
          >
            {skill}
            <button
              type="button"
              onClick={() => onRemove(index)}
              aria-label={`Remove ${skill}`}
              className="rounded-sm text-text-muted hover:text-critical focus:outline-none focus:ring-2 focus:ring-accent"
            >
              ×
            </button>
          </span>
        ))}
      </div>
      <div className="mt-4 flex gap-2">
        <label className="flex-1">
          <span className="sr-only">Add a skill</span>
          <input
            value={draft}
            onChange={(event) => setDraft(event.target.value)}
            onKeyDown={(event) => {
              if (event.key === 'Enter') {
                event.preventDefault()
                submitAdd()
              }
            }}
            placeholder="e.g. Java"
            className="w-full rounded-md border border-border bg-surface p-2.5 text-sm text-text-primary transition-[border-color,box-shadow] focus:border-accent focus:outline-none focus:ring-2 focus:ring-accent"
          />
        </label>
        <button
          type="button"
          onClick={submitAdd}
          className="rounded-md border border-border-strong px-4 py-2.5 text-sm font-semibold text-text-secondary transition-colors hover:border-accent hover:text-accent focus:outline-none focus:ring-2 focus:ring-accent"
        >
          Add
        </button>
      </div>
    </Section>
  )
}

function ProjectsSection({ projects, onAdd, onUpdate, onRemove }) {
  return (
    <Section title="Projects" hint="Projects that demonstrate real, working skills - optional" onAdd={onAdd} addLabel="+ Add project">
      {projects.length === 0 && <p className="text-xs text-text-muted">No projects added yet.</p>}
      {projects.map((project, index) => (
        <EntryCard key={index} onRemove={() => onRemove(index)}>
          <Field label="Project name" value={project.name} onChange={(v) => onUpdate(index, 'name', v)} />
          <Field label="Tech used" value={project.tech} onChange={(v) => onUpdate(index, 'tech', v)} placeholder="e.g. Java, Spring Boot, MySQL" />
          <TextAreaField label="Description" value={project.description} onChange={(v) => onUpdate(index, 'description', v)} />
          <Field label="Link" value={project.link} onChange={(v) => onUpdate(index, 'link', v)} wide />
        </EntryCard>
      ))}
    </Section>
  )
}

function EducationSection({ education, onAdd, onUpdate, onRemove }) {
  return (
    <Section title="Education" hint="Optional" onAdd={onAdd} addLabel="+ Add education">
      {education.length === 0 && <p className="text-xs text-text-muted">No education added yet.</p>}
      {education.map((entry, index) => (
        <EntryCard key={index} onRemove={() => onRemove(index)}>
          <Field label="Institution" value={entry.institution} onChange={(v) => onUpdate(index, 'institution', v)} />
          <Field label="Degree" value={entry.degree} onChange={(v) => onUpdate(index, 'degree', v)} />
          <Field label="Dates" value={entry.dates} onChange={(v) => onUpdate(index, 'dates', v)} placeholder="e.g. 2023-2027" />
          <Field label="GPA" value={entry.gpa} onChange={(v) => onUpdate(index, 'gpa', v)} />
        </EntryCard>
      ))}
    </Section>
  )
}

function ExperienceSection({ experience, onAdd, onUpdate, onRemove, onAddBullet, onUpdateBullet, onRemoveBullet }) {
  return (
    <Section title="Experience" hint="Roles, internships, or part-time work - optional" onAdd={onAdd} addLabel="+ Add experience">
      {experience.length === 0 && <p className="text-xs text-text-muted">No experience added yet.</p>}
      {experience.map((entry, index) => (
        <EntryCard key={index} onRemove={() => onRemove(index)}>
          <Field label="Role" value={entry.role} onChange={(v) => onUpdate(index, 'role', v)} />
          <Field label="Company" value={entry.company} onChange={(v) => onUpdate(index, 'company', v)} />
          <Field label="Dates" value={entry.dates} onChange={(v) => onUpdate(index, 'dates', v)} placeholder="e.g. Jun 2025 - Aug 2025" wide />
          <div className="sm:col-span-2">
            <span className="mb-1.5 block text-xs font-bold text-text-secondary">Bullet points</span>
            <div className="space-y-2">
              {entry.bullets.map((bullet, bulletIndex) => (
                <div key={bulletIndex} className="flex gap-2">
                  <label className="flex-1">
                    <span className="sr-only">Bullet point</span>
                    <input
                      value={bullet}
                      onChange={(event) => onUpdateBullet(index, bulletIndex, event.target.value)}
                      placeholder="e.g. Built REST APIs with Spring Boot"
                      className="w-full rounded-md border border-border bg-surface p-2.5 text-sm text-text-primary transition-[border-color,box-shadow] focus:border-accent focus:outline-none focus:ring-2 focus:ring-accent"
                    />
                  </label>
                  <button
                    type="button"
                    onClick={() => onRemoveBullet(index, bulletIndex)}
                    aria-label="Remove bullet point"
                    className="rounded-md border border-border-strong px-3 text-xs font-bold text-critical transition-colors hover:border-critical focus:outline-none focus:ring-2 focus:ring-accent"
                  >
                    ×
                  </button>
                </div>
              ))}
            </div>
            <button
              type="button"
              onClick={() => onAddBullet(index)}
              className="mt-2 rounded-sm text-xs font-bold text-accent hover:text-accent-hover focus:outline-none focus:ring-2 focus:ring-accent"
            >
              + Add bullet
            </button>
          </div>
        </EntryCard>
      ))}
    </Section>
  )
}

function CertificationsSection({ certifications, onAdd, onUpdate, onRemove }) {
  return (
    <Section title="Certifications" hint="Optional" onAdd={onAdd} addLabel="+ Add certification">
      {certifications.length === 0 && <p className="text-xs text-text-muted">No certifications added yet.</p>}
      {certifications.map((entry, index) => (
        <EntryCard key={index} onRemove={() => onRemove(index)}>
          <Field label="Name" value={entry.name} onChange={(v) => onUpdate(index, 'name', v)} />
          <Field label="Issuer" value={entry.issuer} onChange={(v) => onUpdate(index, 'issuer', v)} />
          <Field label="Date" value={entry.date} onChange={(v) => onUpdate(index, 'date', v)} wide />
        </EntryCard>
      ))}
    </Section>
  )
}

// ---- form view ----

function ResumeForm({ editingId, onSaved, onCancel }) {
  const [title, setTitle] = useState('')
  const [contact, setContact] = useState({ ...BLANK_CONTACT })
  const [summary, setSummary] = useState('')
  const [skills, setSkills] = useState([])
  const [projects, setProjects] = useState([])
  const [education, setEducation] = useState([])
  const [experience, setExperience] = useState([])
  const [certifications, setCertifications] = useState([])

  const [loading, setLoading] = useState(Boolean(editingId))
  const [loadError, setLoadError] = useState('')
  const [saving, setSaving] = useState(false)
  const [saveError, setSaveError] = useState('')

  useEffect(() => {
    if (!editingId) {
      return
    }
    fetchResumeVersion(editingId)
      .then((view) => {
        setTitle(view.title ?? '')
        setContact({
          name: view.contact?.name ?? '',
          email: view.contact?.email ?? '',
          phone: view.contact?.phone ?? '',
          location: view.contact?.location ?? '',
          portfolioUrl: view.contact?.portfolioUrl ?? '',
          githubUrl: view.contact?.githubUrl ?? '',
          linkedinUrl: view.contact?.linkedinUrl ?? '',
        })
        setSummary(view.summary ?? '')
        setSkills(view.skills ?? [])
        setProjects((view.projects ?? []).map((p) => ({
          name: p.name ?? '', description: p.description ?? '', tech: p.tech ?? '', link: p.link ?? '',
        })))
        setEducation((view.education ?? []).map((e) => ({
          institution: e.institution ?? '', degree: e.degree ?? '', dates: e.dates ?? '', gpa: e.gpa ?? '',
        })))
        setExperience((view.experience ?? []).map((e) => ({
          company: e.company ?? '', role: e.role ?? '', dates: e.dates ?? '', bullets: e.bullets ?? [],
        })))
        setCertifications((view.certifications ?? []).map((c) => ({
          name: c.name ?? '', issuer: c.issuer ?? '', date: c.date ?? '',
        })))
      })
      .catch((error) => setLoadError(error.message))
      .finally(() => setLoading(false))
  }, [editingId])

  async function handleSave(event) {
    event.preventDefault()
    setSaveError('')

    if (!title.trim()) {
      setSaveError('Please enter a resume title.')
      return
    }
    if (!contact.name.trim()) {
      setSaveError('Please enter a name in Contact.')
      return
    }

    const payload = {
      title: title.trim(),
      contact: { ...contact, name: contact.name.trim() },
      summary,
      skills: skills.filter((skill) => skill && skill.trim()),
      projects: projects.filter((p) => hasAnyText(p.name, p.description, p.tech, p.link)),
      education: education.filter((e) => hasAnyText(e.institution, e.degree, e.dates, e.gpa)),
      experience: experience
        .map((e) => ({ ...e, bullets: e.bullets.filter((b) => b && b.trim()) }))
        .filter((e) => hasAnyText(e.company, e.role, e.dates) || e.bullets.length > 0),
      certifications: certifications.filter((c) => hasAnyText(c.name, c.issuer, c.date)),
    }

    setSaving(true)
    try {
      if (editingId) {
        await updateResumeVersion(editingId, payload)
      } else {
        await createResumeVersion(payload)
      }
      onSaved()
    } catch (error) {
      setSaveError(error.message)
    } finally {
      setSaving(false)
    }
  }

  if (loading) {
    return <p className="text-sm text-text-muted">Loading resume...</p>
  }
  if (loadError) {
    return (
      <p className="text-sm font-medium text-critical" role="alert">
        Could not load this resume: {loadError}
      </p>
    )
  }

  return (
    <form onSubmit={handleSave} className="space-y-5">
      <div>
        <label htmlFor="resume-title" className="mb-1.5 block text-xs font-bold text-text-secondary">
          Resume title
        </label>
        <input
          id="resume-title"
          value={title}
          onChange={(event) => setTitle(event.target.value)}
          placeholder="e.g. Backend Developer Resume"
          className="w-full max-w-md rounded-md border border-border bg-surface p-2.5 text-sm text-text-primary transition-[border-color,box-shadow] focus:border-accent focus:outline-none focus:ring-2 focus:ring-accent"
        />
      </div>

      <ContactSection contact={contact} onChange={(field, value) => setContact((c) => ({ ...c, [field]: value }))} />

      <SummarySection summary={summary} onChange={setSummary} />

      <SkillsSection
        skills={skills}
        onAdd={(skill) => setSkills((list) => [...list, skill])}
        onRemove={(index) => setSkills((list) => removeAt(list, index))}
      />

      <ProjectsSection
        projects={projects}
        onAdd={() => setProjects((list) => [...list, blankProject()])}
        onUpdate={(index, field, value) => setProjects((list) => updateAt(list, index, field, value))}
        onRemove={(index) => setProjects((list) => removeAt(list, index))}
      />

      <EducationSection
        education={education}
        onAdd={() => setEducation((list) => [...list, blankEducation()])}
        onUpdate={(index, field, value) => setEducation((list) => updateAt(list, index, field, value))}
        onRemove={(index) => setEducation((list) => removeAt(list, index))}
      />

      <ExperienceSection
        experience={experience}
        onAdd={() => setExperience((list) => [...list, blankExperience()])}
        onUpdate={(index, field, value) => setExperience((list) => updateAt(list, index, field, value))}
        onRemove={(index) => setExperience((list) => removeAt(list, index))}
        onAddBullet={(index) =>
          setExperience((list) => updateAt(list, index, 'bullets', [...list[index].bullets, '']))
        }
        onUpdateBullet={(index, bulletIndex, value) =>
          setExperience((list) =>
            updateAt(list, index, 'bullets', list[index].bullets.map((b, i) => (i === bulletIndex ? value : b))),
          )
        }
        onRemoveBullet={(index, bulletIndex) =>
          setExperience((list) =>
            updateAt(list, index, 'bullets', removeAt(list[index].bullets, bulletIndex)),
          )
        }
      />

      <CertificationsSection
        certifications={certifications}
        onAdd={() => setCertifications((list) => [...list, blankCertification()])}
        onUpdate={(index, field, value) => setCertifications((list) => updateAt(list, index, field, value))}
        onRemove={(index) => setCertifications((list) => removeAt(list, index))}
      />

      <div className="flex flex-wrap items-center gap-4">
        <button
          type="submit"
          disabled={saving}
          className="inline-flex items-center gap-2 rounded-md bg-accent px-6 py-3 text-sm font-bold text-white transition-colors hover:bg-accent-hover focus:outline-none focus:ring-2 focus:ring-inset focus:ring-white active:scale-[0.97] disabled:cursor-not-allowed disabled:opacity-50"
        >
          {saving ? 'Saving...' : editingId ? 'Save changes' : 'Create resume'}
        </button>
        <button
          type="button"
          onClick={onCancel}
          className="rounded-md border border-border bg-surface px-5 py-3 text-sm font-bold text-text-primary transition-colors hover:border-accent hover:text-accent focus:outline-none focus:ring-2 focus:ring-inset focus:ring-accent"
        >
          Cancel
        </button>
        {saveError && (
          <p className="text-sm font-medium text-critical" role="alert">
            {saveError}
          </p>
        )}
      </div>
    </form>
  )
}

// ---- list view ----

function ResumeRow({ summary, onEdit, onDuplicate, onDelete, onDownload, confirmingDelete, rowBusy, rowError }) {
  return (
    <tr className="transition-colors hover:bg-surface-hover">
      <td className="border-b border-border px-4 py-4 text-text-primary">
        <span className="block max-w-xs truncate font-semibold sm:max-w-sm">{summary.title}</span>
        {rowError && (
          <span className="mt-1 block text-xs font-medium text-critical" role="alert">
            {rowError}
          </span>
        )}
      </td>
      <td className="border-b border-border px-4 py-4 font-mono text-text-muted">{formatDate(summary.updatedAt)}</td>
      <td className="border-b border-border px-4 py-4 text-right">
        <div className="flex flex-wrap items-center justify-end gap-3">
          {confirmingDelete ? (
            <>
              <span className="text-xs font-semibold text-text-secondary">Delete this resume?</span>
              <button
                type="button"
                disabled={rowBusy}
                onClick={() => onDelete(true)}
                className="rounded-sm text-sm font-bold text-critical hover:underline focus:outline-none focus:ring-2 focus:ring-accent disabled:opacity-50"
              >
                Confirm
              </button>
              <button
                type="button"
                disabled={rowBusy}
                onClick={() => onDelete(false)}
                className="rounded-sm text-sm font-bold text-text-secondary hover:text-text-primary focus:outline-none focus:ring-2 focus:ring-accent disabled:opacity-50"
              >
                Cancel
              </button>
            </>
          ) : (
            <>
              <button
                type="button"
                disabled={rowBusy}
                onClick={onEdit}
                className="rounded-sm text-sm font-bold text-accent hover:text-accent-hover focus:outline-none focus:ring-2 focus:ring-accent disabled:opacity-50"
              >
                Edit
              </button>
              <button
                type="button"
                disabled={rowBusy}
                onClick={onDuplicate}
                className="rounded-sm text-sm font-bold text-text-secondary hover:text-text-primary focus:outline-none focus:ring-2 focus:ring-accent disabled:opacity-50"
              >
                Duplicate
              </button>
              <button
                type="button"
                disabled={rowBusy}
                onClick={onDownload}
                className="rounded-sm text-sm font-bold text-text-secondary hover:text-text-primary focus:outline-none focus:ring-2 focus:ring-accent disabled:opacity-50"
              >
                Download PDF
              </button>
              <button
                type="button"
                disabled={rowBusy}
                onClick={() => onDelete(true)}
                className="rounded-sm text-sm font-bold text-critical hover:underline focus:outline-none focus:ring-2 focus:ring-accent disabled:opacity-50"
              >
                Delete
              </button>
            </>
          )}
        </div>
      </td>
    </tr>
  )
}

function ResumeList({ onCreate, onEdit }) {
  const [versions, setVersions] = useState([])
  const [loading, setLoading] = useState(true)
  const [listError, setListError] = useState('')

  const [busyId, setBusyId] = useState(null)
  const [confirmDeleteId, setConfirmDeleteId] = useState(null)
  const [rowErrors, setRowErrors] = useState({})

  function load() {
    setLoading(true)
    fetchResumeVersions()
      .then(setVersions)
      .catch((error) => setListError(error.message))
      .finally(() => setLoading(false))
  }

  useEffect(load, [])

  function setRowError(id, message) {
    setRowErrors((current) => ({ ...current, [id]: message }))
  }

  async function handleDuplicate(id) {
    setRowError(id, '')
    setBusyId(id)
    try {
      await duplicateResumeVersion(id)
      load()
    } catch (error) {
      setRowError(id, error.message)
    } finally {
      setBusyId(null)
    }
  }

  async function handleDelete(id, confirmed) {
    if (!confirmed) {
      setConfirmDeleteId(null)
      return
    }
    setRowError(id, '')
    setBusyId(id)
    try {
      await deleteResumeVersion(id)
      setConfirmDeleteId(null)
      load()
    } catch (error) {
      setRowError(id, error.message)
    } finally {
      setBusyId(null)
    }
  }

  async function handleDownload(id, title) {
    setRowError(id, '')
    setBusyId(id)
    try {
      const blob = await fetchResumeVersionPdf(id)
      const url = URL.createObjectURL(blob)
      const link = document.createElement('a')
      link.href = url
      link.download = `${slugify(title)}.pdf`
      document.body.appendChild(link)
      link.click()
      document.body.removeChild(link)
      URL.revokeObjectURL(url)
    } catch (error) {
      setRowError(id, error.message)
    } finally {
      setBusyId(null)
    }
  }

  return (
    <div>
      <div className="mb-6 flex flex-wrap items-center justify-between gap-3">
        <div>
          <h1 className="text-xl font-bold text-text-primary">My Resumes</h1>
          <p className="mt-0.5 text-sm text-text-muted">Build and manage your own resume versions</p>
        </div>
        <button
          type="button"
          onClick={onCreate}
          className="inline-flex items-center gap-2 rounded-md bg-accent px-5 py-2.5 text-sm font-bold text-white transition-colors hover:bg-accent-hover focus:outline-none focus:ring-2 focus:ring-inset focus:ring-white active:scale-[0.97]"
        >
          + New Resume
        </button>
      </div>

      {loading && <p className="text-sm text-text-muted">Loading your resumes...</p>}

      {listError && (
        <p className="text-sm font-medium text-critical" role="alert">
          Could not load your resumes: {listError}
        </p>
      )}

      {!loading && !listError && versions.length === 0 && (
        <div className="rounded-lg border border-border bg-surface p-8 text-center">
          <p className="text-sm text-text-muted">You haven't built a resume yet.</p>
          <button
            type="button"
            onClick={onCreate}
            className="mt-4 inline-flex items-center gap-2 rounded-md bg-accent px-5 py-2.5 text-sm font-bold text-white transition-colors hover:bg-accent-hover focus:outline-none focus:ring-2 focus:ring-inset focus:ring-white active:scale-[0.97]"
          >
            Create your first resume
          </button>
        </div>
      )}

      {!loading && !listError && versions.length > 0 && (
        <div className="overflow-hidden rounded-lg border border-border bg-surface">
          <div className="overflow-x-auto">
            <table className="w-full border-collapse text-left text-sm">
              <thead>
                <tr>
                  <th className="border-b border-border px-4 py-3.5 text-xs font-bold uppercase tracking-wide text-text-muted">
                    Title
                  </th>
                  <th className="border-b border-border px-4 py-3.5 text-xs font-bold uppercase tracking-wide text-text-muted">
                    Last updated
                  </th>
                  <th className="border-b border-border px-4 py-3.5" />
                </tr>
              </thead>
              <tbody>
                {versions.map((summary) => (
                  <ResumeRow
                    key={summary.id}
                    summary={summary}
                    onEdit={() => onEdit(summary.id)}
                    onDuplicate={() => handleDuplicate(summary.id)}
                    onDownload={() => handleDownload(summary.id, summary.title)}
                    onDelete={(confirmed) =>
                      confirmed && confirmDeleteId !== summary.id
                        ? setConfirmDeleteId(summary.id)
                        : handleDelete(summary.id, confirmed)
                    }
                    confirmingDelete={confirmDeleteId === summary.id}
                    rowBusy={busyId === summary.id}
                    rowError={rowErrors[summary.id]}
                  />
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}
    </div>
  )
}

/**
 * Phase 22: the Resume Builder - a list view of the user's own
 * ResumeVersions (title, last-updated date, Edit/Duplicate/Delete/
 * Download PDF) plus a form view for creating/editing one. Every
 * section but Contact is optional and rendered as its own bordered
 * block the user can add/remove entries in (Section/EntryCard above),
 * matching the row-list/bordered-panel layout the rest of the app uses.
 *
 * Deleting uses an inline "Delete this resume? Confirm/Cancel" row
 * state instead of a native window.confirm() - consistent with the
 * rest of the app never using browser dialogs.
 */
export default function ResumeBuilderScreen() {
  const [mode, setMode] = useState('list') // 'list' | 'form'
  const [editingId, setEditingId] = useState(null)
  const [listKey, setListKey] = useState(0)

  function handleCreate() {
    setEditingId(null)
    setMode('form')
  }

  function handleEdit(id) {
    setEditingId(id)
    setMode('form')
  }

  function handleSaved() {
    setMode('list')
    setListKey((key) => key + 1)
  }

  function handleCancel() {
    setMode('list')
  }

  if (mode === 'form') {
    return (
      <div className="space-y-5">
        <div>
          <h1 className="text-xl font-bold text-text-primary">{editingId ? 'Edit Resume' : 'New Resume'}</h1>
          <p className="mt-0.5 text-sm text-text-muted">Every section but Contact is optional</p>
        </div>
        <ResumeForm editingId={editingId} onSaved={handleSaved} onCancel={handleCancel} />
      </div>
    )
  }

  return <ResumeList key={listKey} onCreate={handleCreate} onEdit={handleEdit} />
}
