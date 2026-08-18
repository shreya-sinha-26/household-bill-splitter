import { useEffect, useState } from 'react'
import { createGroup, deleteGroup, listGroups } from '../api/groupApi.js'
import ErrorBanner from '../components/ErrorBanner.jsx'
import GroupCard from '../components/GroupCard.jsx'
import GroupForm from '../components/GroupForm.jsx'
import Modal from '../components/Modal.jsx'
import Pagination from '../components/Pagination.jsx'

export default function GroupListPage() {
  const [page, setPage] = useState(0)
  const [searchInput, setSearchInput] = useState('')
  const [search, setSearch] = useState('')
  const [data, setData] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)
  const [createOpen, setCreateOpen] = useState(false)
  const [createErrors, setCreateErrors] = useState({})
  const [createError, setCreateError] = useState(null)
  const [submitting, setSubmitting] = useState(false)
  const [pendingDelete, setPendingDelete] = useState(null)

  useEffect(() => {
    let cancelled = false
    setLoading(true)
    setError(null)
    listGroups(page, 10, search)
      .then((result) => {
        if (!cancelled) {
          setData(result)
        }
      })
      .catch((err) => {
        if (!cancelled) {
          setError(err)
          setData(null)
        }
      })
      .finally(() => {
        if (!cancelled) {
          setLoading(false)
        }
      })
    return () => {
      cancelled = true
    }
  }, [page, search])

  function handleSearch(event) {
    event.preventDefault()
    setPage(0)
    setSearch(searchInput.trim())
  }

  function handleCreate(body) {
    setSubmitting(true)
    setCreateErrors({})
    setCreateError(null)
    createGroup(body)
      .then(() => {
        setCreateOpen(false)
        setPage(0)
        return listGroups(0, 10, search)
      })
      .then((result) => setData(result))
      .catch((err) => {
        setCreateError(err)
        setCreateErrors(err.fieldErrors || {})
      })
      .finally(() => setSubmitting(false))
  }

  function confirmDelete() {
    if (!pendingDelete) {
      return
    }
    deleteGroup(pendingDelete.id)
      .then(() => listGroups(page, 10, search))
      .then((result) => {
        setData(result)
        setPendingDelete(null)
      })
      .catch((err) => {
        setPendingDelete(null)
        setError(err)
      })
  }

  const groups = data && data.content ? data.content : []

  return (
    <section>
      <div className="page-header">
        <h1>Groups</h1>
        <button className="btn btn-primary" type="button" onClick={() => {
          setCreateOpen(true)
          setCreateErrors({})
          setCreateError(null)
        }}>
          New group
        </button>
      </div>

      <form className="toolbar" onSubmit={handleSearch}>
        <input
          value={searchInput}
          onChange={(event) => setSearchInput(event.target.value)}
          placeholder="Search by name"
        />
        <button className="btn" type="submit">Search</button>
      </form>

      <ErrorBanner error={error} />

      {loading && <p className="loading">Loading groups…</p>}

      {!loading && !error && groups.length === 0 && (
        <p className="empty">No groups yet</p>
      )}

      {!loading && groups.length > 0 && (
        <div className="card-grid">
          {groups.map((group) => (
            <GroupCard key={group.id} group={group} onDelete={setPendingDelete} />
          ))}
        </div>
      )}

      {data && (
        <Pagination
          page={data.page}
          totalPages={data.totalPages}
          first={data.first}
          last={data.last}
          onPageChange={setPage}
        />
      )}

      {createOpen && (
        <Modal title="Create group" onClose={() => setCreateOpen(false)}>
          <ErrorBanner error={createError} />
          <GroupForm
            serverErrors={createErrors}
            submitting={submitting}
            onSubmit={handleCreate}
            onCancel={() => setCreateOpen(false)}
          />
        </Modal>
      )}

      {pendingDelete && (
        <Modal title="Delete group?" onClose={() => setPendingDelete(null)}>
          <p>Delete “{pendingDelete.name}”? This also deletes its members and bills.</p>
          <div className="toolbar">
            <button className="btn btn-danger" type="button" onClick={confirmDelete}>
              Delete
            </button>
            <button className="btn" type="button" onClick={() => setPendingDelete(null)}>
              Cancel
            </button>
          </div>
        </Modal>
      )}
    </section>
  )
}
