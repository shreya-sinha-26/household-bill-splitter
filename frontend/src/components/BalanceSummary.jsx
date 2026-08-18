function formatMoney(value) {
  return new Intl.NumberFormat('en-IN', {
    style: 'currency',
    currency: 'INR',
    minimumFractionDigits: 2,
    maximumFractionDigits: 2
  }).format(Number(value))
}

export default function BalanceSummary({ balances }) {
  let netTotal = 0
  balances.forEach((row) => {
    netTotal += Number(row.netBalance)
  })

  return (
    <div className="balance-list">
      {balances.map((row) => {
        const net = Number(row.netBalance)
        let className = 'balance-zero'
        let label = 'settled'
        if (net > 0) {
          className = 'balance-owed'
          label = 'is owed'
        } else if (net < 0) {
          className = 'balance-owes'
          label = 'owes'
        }
        return (
          <div className="balance-card" key={row.memberId}>
            <span>{row.memberName}</span>
            <span className={className}>
              {label} <span className="money">{formatMoney(Math.abs(net))}</span>
            </span>
          </div>
        )
      })}
      <p className="net-proof">
        Net total = <span className="money">{formatMoney(netTotal)}</span>
      </p>
    </div>
  )
}
