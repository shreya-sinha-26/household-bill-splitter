export default function ErrorBanner({ error }) {
  if (!error) {
    return null
  }

  const fieldErrors = error.fieldErrors || {}
  const fields = Object.keys(fieldErrors)

  return (
    <div className="error-banner" role="alert">
      <strong>{error.message || 'Something went wrong'}</strong>
      {fields.length > 0 && (
        <ul>
          {fields.map((field) => (
            <li key={field}>
              <strong>{field}:</strong> {fieldErrors[field]}
            </li>
          ))}
        </ul>
      )}
    </div>
  )
}
