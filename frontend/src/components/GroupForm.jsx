import { useEffect, useState } from 'react'

function validateGroup(name, members) {
  const fieldErrors = {}
  const trimmedName = name.trim()
  if (!trimmedName) {
    fieldErrors.name = 'must not be blank'
  } else if (trimmedName.length < 2 || trimmedName.length > 60) {
    fieldErrors.name = 'size must be between 2 and 60'
  }

  if (!members.length) {
    fieldErrors.members = 'must not be empty'
  } else if (members.length > 50) {
    fieldErrors.members = 'size must be between 1 and 50'
  }

  const seen = {}
  members.forEach((member, index) => {
    const value = member.trim()
    if (!value) {
      fieldErrors['members[' + index + ']'] = 'must not be blank'
    } else if (value.length > 40) {
      fieldErrors['members[' + index + ']'] = 'size must be between 0 and 40'
    } else if (seen[value.toLowerCase()]) {
      fieldErrors['members[' + index + ']'] = 'names must be unique within the group'
    } else {
      seen[value.toLowerCase()] = true
    }
  })
  return fieldErrors
}

export default function GroupForm({ initialName, initialMembers, serverErrors, submitting, onSubmit, onCancel }) {
  const [name, setName] = useState(initialName || '')
  const [members, setMembers] = useState(initialMembers && initialMembers.length ? initialMembers : [''])
  const [clientErrors, setClientErrors] = useState({})

  useEffect(() => {
    setName(initialName || '')
    setMembers(initialMembers && initialMembers.length ? initialMembers : [''])
    setClientErrors({})
  }, [initialName, initialMembers])

  const fieldErrors = Object.assign({}, clientErrors, serverErrors || {})

  function updateMember(index, value) {
    const next = members.slice()
    next[index] = value
    setMembers(next)
  }

  function addMember() {
    if (members.length < 50) {
      setMembers(members.concat(['']))
    }
  }

  function removeMember(index) {
    if (members.length === 1) {
      return
    }
    setMembers(members.filter((_, i) => i !== index))
  }

  function handleSubmit(event) {
    event.preventDefault()
    const nextMembers = members.map((member) => member.trim()).filter((member) => member.length > 0)
    const errors = validateGroup(name, nextMembers.length ? nextMembers : members)
    setClientErrors(errors)
    if (Object.keys(errors).length > 0) {
      return
    }
    onSubmit({
      name: name.trim(),
      members: nextMembers
    })
  }

  return (
    <form onSubmit={handleSubmit}>
      <div className="field">
        <label htmlFor="group-name">Group name</label>
        <input
          id="group-name"
          value={name}
          onChange={(event) => setName(event.target.value)}
          maxLength={60}
        />
        {fieldErrors.name && <span className="field-error">{fieldErrors.name}</span>}
      </div>

      <div className="field">
        <label>Members</label>
        {fieldErrors.members && <span className="field-error">{fieldErrors.members}</span>}
        {members.map((member, index) => (
          <div className="member-row" key={index}>
            <input
              value={member}
              onChange={(event) => updateMember(index, event.target.value)}
              maxLength={40}
              placeholder={'Member ' + (index + 1)}
            />
            <button className="btn" type="button" onClick={() => removeMember(index)}>
              Remove
            </button>
          </div>
        ))}
        {members.map((_, index) => (
          fieldErrors['members[' + index + ']'] ? (
            <span className="field-error" key={'err-' + index}>
              {fieldErrors['members[' + index + ']']}
            </span>
          ) : null
        ))}
        <button className="btn" type="button" onClick={addMember}>
          Add member
        </button>
      </div>

      <div className="toolbar">
        <button className="btn btn-primary" type="submit" disabled={submitting}>
          {submitting ? 'Saving…' : 'Save group'}
        </button>
        <button className="btn" type="button" onClick={onCancel}>
          Cancel
        </button>
      </div>
    </form>
  )
}
