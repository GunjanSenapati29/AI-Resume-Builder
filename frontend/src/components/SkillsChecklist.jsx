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
    return <p className="text-sm text-slate-500">Loading skill list...</p>
  }

  if (error) {
    return (
      <p className="text-sm text-red-600" role="alert">
        Could not load the skill list: {error}
      </p>
    )
  }

  if (skills.length === 0) {
    return (
      <p className="text-sm text-slate-500">
        No skills are available to check against yet - the skill taxonomy is empty.
      </p>
    )
  }

  return (
    <fieldset className="space-y-2">
      <legend className="text-sm font-semibold text-slate-700">
        Required skills{' '}
        <span className="font-normal text-slate-500">(tick what the JD asks for)</span>
      </legend>
      <ul className="grid grid-cols-1 gap-2 sm:grid-cols-2">
        {skills.map((skill) => {
          const selected = selectedSkills[skill.name]
          return (
            <li
              key={skill.name}
              className="flex items-center justify-between rounded-md border border-slate-200 px-3 py-2"
            >
              <label className="flex items-center gap-2 text-sm text-slate-800">
                <input
                  type="checkbox"
                  checked={Boolean(selected)}
                  onChange={() => toggleIncluded(skill.name)}
                  className="h-4 w-4 rounded border-slate-300 text-blue-600 focus:ring-blue-500"
                />
                {skill.name}
                <span className="text-xs text-slate-500">
                  ({skill.difficulty.toLowerCase()})
                </span>
              </label>
              {selected && (
                <label className="flex items-center gap-1 text-xs text-slate-600">
                  <input
                    type="checkbox"
                    checked={selected.core}
                    onChange={() => toggleCore(skill.name)}
                    className="h-3.5 w-3.5 rounded border-slate-300 text-blue-600 focus:ring-blue-500"
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
