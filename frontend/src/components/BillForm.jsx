import { useEffect, useState } from 'react'

function todayIso() {
  const now = new Date()
  const month = String(now.getMonth() + 1).padStart(2, '0')
  const day = String(now.getDate()).padStart(2, '0')
  return now.getFullYear() + '-' + month + '-' + day
}

function validateBill(description, amount, paidById, date) {
  const fieldErrors = {}
  const trimmed = description.trim()
  if (!trimmed) {
    fieldErrors.description = 'must not be blank'
  } else if (trimmed.length < 2 || trimmed.length > 140) {
    fieldErrors.description = 'size must be between 2 and 140'
  }

  if (!amount) {
    fieldErrors.amount = 'must not be null'
  } else if (!/^\d{1,9}(\.\d{1,2})?$/.test(amount) || Number(amount) < 0.01) {
    fieldErrors.amount = 'must be greater than 0 and have at most 2 decimal places'
  }

  if (!paidById) {
    fieldErrors.paidById = 'must not be null'
  }

  if (!date) {
    fieldErrors.date = 'must not be null'
  } else if (date > todayIso()) {
    fieldErrors.date = 'must be a date in the past or in the present'
  }

  return fieldErrors
}

export default function BillForm({ members, initial, serverErrors, submitting, onSubmit, onCancel }) {
  const [description, setDescription] = useState('')
  const [amount, setAmount] = useState('')
  const [paidById, setPaidById] = useState('')
  const [date, setDate] = useState(todayIso())
  const [clientErrors, setClientErrors] = useState({})

  useEffect(() => {
    setDescription(initial && initial.description ? initial.description : '')
    setAmount(initial && initial.amount != null ? String(initial.amount) : '')
    setPaidById(initial && initial.paidBy ? String(initial.paidBy.id) : '')
    setDate(initial && initial.date ? initial.date : todayIso())
    setClientErrors({})
  }, [initial])

  const fieldErrors = Object.assign({}, clientErrors, serverErrors || {})

  function handleSubmit(event) {
    event.preventDefault()
    const errors = validateBill(description, amount, paidById, date)
    setClientErrors(errors)
    if (Object.keys(errors).length > 0) {
      return
    }
    onSubmit({
      description: description.trim(),
      amount: Number(amount).toFixed(2),
      paidById: Number(paidById),
      date
    })
  }

  return (
    <form onSubmit={handleSubmit}>
      <div className="field">
        <label htmlFor="bill-description">Description</label>
        <input
          id="bill-description"
          value={description}
          onChange={(event) => setDescription(event.target.value)}
          maxLength={140}
        />
        {fieldErrors.description && <span className="field-error">{fieldErrors.description}</span>}
      </div>

      <div className="field">
        <label htmlFor="bill-amount">Amount (₹)</label>
        <input
          id="bill-amount"
          value={amount}
          onChange={(event) => setAmount(event.target.value)}
          inputMode="decimal"
        />
        {fieldErrors.amount && <span className="field-error">{fieldErrors.amount}</span>}
      </div>

      <div className="field">
        <label htmlFor="bill-paid-by">Paid by</label>
        <select
          id="bill-paid-by"
          value={paidById}
          onChange={(event) => setPaidById(event.target.value)}
        >
          <option value="">Select member</option>
          {members.map((member) => (
            <option key={member.id} value={member.id}>
              {member.name}
            </option>
          ))}
        </select>
        {fieldErrors.paidById && <span className="field-error">{fieldErrors.paidById}</span>}
      </div>

      <div className="field">
        <label htmlFor="bill-date">Date</label>
        <input
          id="bill-date"
          type="date"
          value={date}
          max={todayIso()}
          onChange={(event) => setDate(event.target.value)}
        />
        {fieldErrors.date && <span className="field-error">{fieldErrors.date}</span>}
      </div>

      <div className="toolbar">
        <button className="btn btn-primary" type="submit" disabled={submitting}>
          {submitting ? 'Saving…' : 'Save bill'}
        </button>
        <button className="btn" type="button" onClick={onCancel}>
          Cancel
        </button>
      </div>
    </form>
  )
}
