export default function Pagination({ page, totalPages, first, last, onPageChange }) {
  if (!totalPages || totalPages <= 1) {
    return null
  }

  return (
    <div className="pagination">
      <button className="btn" disabled={first} onClick={() => onPageChange(page - 1)}>
        Previous
      </button>
      <span>
        Page {page + 1} of {totalPages}
      </span>
      <button className="btn" disabled={last} onClick={() => onPageChange(page + 1)}>
        Next
      </button>
    </div>
  )
}
