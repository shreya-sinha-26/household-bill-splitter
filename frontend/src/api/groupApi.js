import client from './client.js'

export function listGroups(page, size, search) {
  return client.get('/api/groups', {
    params: { page, size, search }
  }).then((res) => res.data)
}

export function getGroup(id) {
  return client.get('/api/groups/' + id).then((res) => res.data)
}

export function createGroup(body) {
  return client.post('/api/groups', body).then((res) => res.data)
}

export function updateGroup(id, body) {
  return client.put('/api/groups/' + id, body).then((res) => res.data)
}

export function deleteGroup(id) {
  return client.delete('/api/groups/' + id)
}

export function getBalances(groupId) {
  return client.get('/api/groups/' + groupId + '/balances').then((res) => res.data)
}

export function getSettlements(groupId) {
  return client.get('/api/groups/' + groupId + '/settlements').then((res) => res.data)
}
