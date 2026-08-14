import { useEffect, useState } from 'react'
import { fetchSkills } from '../api'

/**
 * Fetches the real taxonomy from GET /api/skills and lets the user tick
 * which skills the JD requires, marking each as core or nice-to-have.
 * selectedSkills is a { [skillName]: { core: boolean } } map, owned by
 * the parent screen so it can be read out on submit.
 */
export default function SkillsChecklist({ selectedSkills, onChange }) {
  const [skills, setSkills] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    fetchSkills()
      .then(setSkills)
      .catch((err) => setError(err.message))
      .finally(() => setLoading(false))
  }, [])

  function toggleIncluded(name) {
    if (selectedSkills[name]) {
      const next = { ...selectedSkills }
      delete next[name]
      onChange(next)
    } else {
      onChange({ ...selectedSkills, [name]: { core: true } })
    }
  }

  function toggleCore(name) {
    onChange({
      ...selectedSkills,
      [name]: { core: !selectedSkills[name].core },
    })
  }

  if (loading) {
    return <p className="text-sm text-text-muted">Loading skill list...</p>
  }

  if (error) {
    return (
      <p className="text-sm font-medium text-critical" role="alert">
        Could not load the skill list: {error}
      </p>
    )
  }

  if (skills.length === 0) {
    return (
      <p className="text-sm text-text-muted">
        No skills are available to check against yet - the skill taxonomy is empty.
      </p>
    )
  }

  return (
    <fieldset className="rounded-lg border border-border bg-surface-1 p-6 shadow-sm">
      <legend className="mb-3 text-sm font-bold text-text-primary">
        Required skills{' '}
        <span className="font-normal text-text-muted">(tick what the JD asks for)</span>
      </legend>
      <ul className="grid grid-cols-1 gap-2 sm:grid-cols-2">
        {skills.map((skill) => {
          const selected = selectedSkills[skill.name]
          return (
            <li
              key={skill.name}
              className="flex items-center justify-between rounded-md border border-border px-3 py-2.5"
            >
              <label className="flex items-center gap-2 text-sm text-text-primary">
                <input
                  type="checkbox"
                  checked={Boolean(selected)}
                  onChange={() => toggleIncluded(skill.name)}
                  className="h-4 w-4 rounded border-border text-accent focus:ring-accent"
                />
                {skill.name}
                <span className="text-xs text-text-muted">
                  ({skill.difficulty.toLowerCase()})
                </span>
              </label>
              {selected && (
                <label className="flex items-center gap-1 text-xs text-text-secondary">
                  <input
                    type="checkbox"
                    checked={selected.core}
                    onChange={() => toggleCore(skill.name)}
                    className="h-3.5 w-3.5 rounded border-border text-accent focus:ring-accent"
                  />
                  core
                </label>
              )}
            </li>
          )
        })}
      </ul>
    </fieldset>
  )
}
