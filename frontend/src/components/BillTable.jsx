function formatMoney(value) {
  return new Intl.NumberFormat('en-IN', {
    style: 'currency',
    currency: 'INR',
    minimumFractionDigits: 2,
    maximumFractionDigits: 2
  }).format(Number(value))
}

export default function BillTable({ bills, onEdit, onDelete }) {
  return (
    <table className="bill-table">
      <thead>
        <tr>
          <th>Date</th>
          <th>Description</th>
          <th>Paid by</th>
          <th>Amount</th>
          <th></th>
        </tr>
      </thead>
      <tbody>
        {bills.map((bill) => (
          <tr key={bill.id}>
            <td data-label="Date">{bill.date}</td>
            <td data-label="Description">{bill.description}</td>
            <td data-label="Paid by">{bill.paidBy ? bill.paidBy.name : ''}</td>
            <td data-label="Amount">
              <span className="money">{formatMoney(bill.amount)}</span>
            </td>
            <td className="bill-actions">
              <button className="btn" type="button" onClick={() => onEdit(bill)}>
                Edit
              </button>
              <button className="btn btn-danger" type="button" onClick={() => onDelete(bill)}>
                Delete
              </button>
            </td>
          </tr>
        ))}
      </tbody>
    </table>
  )
}
