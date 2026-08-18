function formatMoney(value) {
  return new Intl.NumberFormat('en-IN', {
    style: 'currency',
    currency: 'INR',
    minimumFractionDigits: 2,
    maximumFractionDigits: 2
  }).format(Number(value))
}

export default function SettlementList({ settlements }) {
  if (!settlements.length) {
    return <p className="empty">No transfers needed — everyone is settled.</p>
  }

  return (
    <div>
      {settlements.map((item, index) => (
        <div className="settlement-row" key={index}>
          <span>
            {item.from.name} pays {item.to.name}
          </span>
          <strong>{formatMoney(item.amount)}</strong>
        </div>
      ))}
    </div>
  )
}
