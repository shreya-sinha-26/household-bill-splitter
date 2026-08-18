import { useEffect, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { createBill, deleteBill, listBills, updateBill } from '../api/billApi.js'
import { getBalances, getGroup, getSettlements } from '../api/groupApi.js'
import BalanceSummary from '../components/BalanceSummary.jsx'
import BillForm from '../components/BillForm.jsx'
import BillTable from '../components/BillTable.jsx'
import ErrorBanner from '../components/ErrorBanner.jsx'
import Modal from '../components/Modal.jsx'
import Pagination from '../components/Pagination.jsx'
import SettlementList from '../components/SettlementList.jsx'

export default function GroupDetailPage() {
  const { id } = useParams()
  const groupId = Number(id)

  const [group, setGroup] = useState(null)
  const [groupLoading, setGroupLoading] = useState(true)
  const [groupError, setGroupError] = useState(null)

  const [balances, setBalances] = useState([])
  const [balancesLoading, setBalancesLoading] = useState(true)
  const [balancesError, setBalancesError] = useState(null)

  const [settlements, setSettlements] = useState([])
  const [settlementsLoading, setSettlementsLoading] = useState(true)
  const [settlementsError, setSettlementsError] = useState(null)

  const [billsPage, setBillsPage] = useState(null)
  const [billsLoading, setBillsLoading] = useState(true)
  const [billsError, setBillsError] = useState(null)
  const [billPage, setBillPage] = useState(0)
  const [paidById, setPaidById] = useState('')

  const [billModalOpen, setBillModalOpen] = useState(false)
  const [editingBill, setEditingBill] = useState(null)
  const [billErrors, setBillErrors] = useState({})
  const [submitting, setSubmitting] = useState(false)
  const [pendingDelete, setPendingDelete] = useState(null)

  function loadGroup() {
    setGroupLoading(true)
    setGroupError(null)
    getGroup(groupId)
      .then(setGroup)
      .catch(setGroupError)
      .finally(() => setGroupLoading(false))
  }

  function loadBalances() {
    setBalancesLoading(true)
    setBalancesError(null)
    getBalances(groupId)
      .then(setBalances)
      .catch(setBalancesError)
      .finally(() => setBalancesLoading(false))
  }

  function loadSettlements() {
    setSettlementsLoading(true)
    setSettlementsError(null)
    getSettlements(groupId)
      .then(setSettlements)
      .catch(setSettlementsError)
      .finally(() => setSettlementsLoading(false))
  }

  function loadBills(page, payer) {
    setBillsLoading(true)
    setBillsError(null)
    const params = {
      page,
      size: 10,
      sort: 'date,desc'
    }
    if (payer) {
      params.paidById = payer
    }
    listBills(groupId, params)
      .then(setBillsPage)
      .catch(setBillsError)
      .finally(() => setBillsLoading(false))
  }

  function reloadMoneyAndBills() {
    loadBalances()
    loadSettlements()
    loadBills(billPage, paidById)
  }

  useEffect(() => {
    loadGroup()
    loadBalances()
    loadSettlements()
  }, [groupId])

  useEffect(() => {
    loadBills(billPage, paidById)
  }, [groupId, billPage, paidById])

  function openCreateBill() {
    setEditingBill(null)
    setBillErrors({})
    setBillModalOpen(true)
  }

  function openEditBill(bill) {
    setEditingBill(bill)
    setBillErrors({})
    setBillModalOpen(true)
  }

  function handleSaveBill(body) {
    setSubmitting(true)
    setBillErrors({})
    const request = editingBill
      ? updateBill(editingBill.id, body)
      : createBill(groupId, body)
    request
      .then(() => {
        setBillModalOpen(false)
        setEditingBill(null)
        reloadMoneyAndBills()
      })
      .catch((err) => {
        if (err.fieldErrors && Object.keys(err.fieldErrors).length) {
          setBillErrors(err.fieldErrors)
        } else {
          setBillsError(err)
          setBillModalOpen(false)
        }
      })
      .finally(() => setSubmitting(false))
  }

  function confirmDeleteBill() {
    if (!pendingDelete) {
      return
    }
    deleteBill(pendingDelete.id)
      .then(() => {
        setPendingDelete(null)
        reloadMoneyAndBills()
      })
      .catch((err) => {
        setPendingDelete(null)
        setBillsError(err)
      })
  }

  const bills = billsPage && billsPage.content ? billsPage.content : []
  const members = group && group.members ? group.members : []

  return (
    <section>
      <Link className="back-link" to="/">← All groups</Link>

      {groupLoading && <p className="loading">Loading group…</p>}
      <ErrorBanner error={groupError} />

      {group && (
        <>
          <div className="page-header">
            <h1>{group.name}</h1>
            <button className="btn btn-primary" type="button" onClick={openCreateBill}>
              Add bill
            </button>
          </div>
          <div className="chips">
            {members.map((member) => (
              <span className="chip" key={member.id}>{member.name}</span>
            ))}
          </div>
        </>
      )}

      <section className="section">
        <h2>Balances</h2>
        {balancesLoading && <p className="loading">Loading balances…</p>}
        <ErrorBanner error={balancesError} />
        {!balancesLoading && !balancesError && balances.length === 0 && (
          <p className="empty">No members yet</p>
        )}
        {!balancesLoading && !balancesError && balances.length > 0 && (
          <BalanceSummary balances={balances} />
        )}
      </section>

      <section className="section">
        <h2>Suggested settlements</h2>
        {settlementsLoading && <p className="loading">Loading settlements…</p>}
        <ErrorBanner error={settlementsError} />
        {!settlementsLoading && !settlementsError && (
          <SettlementList settlements={settlements} />
        )}
      </section>

      <section className="section">
        <h2>Bills</h2>
        <div className="toolbar">
          <label htmlFor="paid-by-filter">Paid by</label>
          <select
            id="paid-by-filter"
            value={paidById}
            onChange={(event) => {
              setBillPage(0)
              setPaidById(event.target.value)
            }}
          >
            <option value="">All members</option>
            {members.map((member) => (
              <option key={member.id} value={member.id}>{member.name}</option>
            ))}
          </select>
        </div>

        {billsLoading && <p className="loading">Loading bills…</p>}
        <ErrorBanner error={billsError} />
        {!billsLoading && !billsError && bills.length === 0 && (
          <p className="empty">No bills yet</p>
        )}
        {!billsLoading && !billsError && bills.length > 0 && (
          <BillTable bills={bills} onEdit={openEditBill} onDelete={setPendingDelete} />
        )}
        {billsPage && (
          <Pagination
            page={billsPage.page}
            totalPages={billsPage.totalPages}
            first={billsPage.first}
            last={billsPage.last}
            onPageChange={setBillPage}
          />
        )}
      </section>

      {billModalOpen && (
        <Modal title={editingBill ? 'Edit bill' : 'Add bill'} onClose={() => setBillModalOpen(false)}>
          <BillForm
            members={members}
            initial={editingBill}
            serverErrors={billErrors}
            submitting={submitting}
            onSubmit={handleSaveBill}
            onCancel={() => setBillModalOpen(false)}
          />
        </Modal>
      )}

      {pendingDelete && (
        <Modal title="Delete bill?" onClose={() => setPendingDelete(null)}>
          <p>Delete “{pendingDelete.description}”?</p>
          <div className="toolbar">
            <button className="btn btn-danger" type="button" onClick={confirmDeleteBill}>
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
