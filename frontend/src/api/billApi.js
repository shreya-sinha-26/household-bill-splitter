import client from './client.js'

export function listBills(groupId, params) {
  return client.get('/api/groups/' + groupId + '/bills', { params }).then((res) => res.data)
}

export function getBill(id) {
  return client.get('/api/bills/' + id).then((res) => res.data)
}

export function createBill(groupId, body) {
  return client.post('/api/groups/' + groupId + '/bills', body).then((res) => res.data)
}

export function updateBill(id, body) {
  return client.put('/api/bills/' + id, body).then((res) => res.data)
}

export function deleteBill(id) {
  return client.delete('/api/bills/' + id)
}
