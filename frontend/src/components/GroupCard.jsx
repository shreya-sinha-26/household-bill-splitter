import { Link } from 'react-router-dom'

export default function GroupCard({ group, onDelete }) {
  const created = group.createdAt ? String(group.createdAt).replace('T', ' ').slice(0, 16) : ''

  return (
    <article className="group-card">
      <h3>{group.name}</h3>
      <p>
        {group.memberCount} {group.memberCount === 1 ? 'member' : 'members'}
        {created ? ' · ' + created : ''}
      </p>
      <div className="group-card-actions">
        <Link className="btn btn-primary" to={'/groups/' + group.id}>
          Open
        </Link>
        <button className="btn btn-danger" type="button" onClick={() => onDelete(group)}>
          Delete
        </button>
      </div>
    </article>
  )
}
