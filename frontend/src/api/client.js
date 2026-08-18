import axios from 'axios'

const client = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
  headers: {
    'Content-Type': 'application/json'
  }
})

client.interceptors.response.use(
  (response) => response,
  (error) => {
    const data = error.response && error.response.data
    const normalised = {
      status: (data && data.status) || (error.response && error.response.status) || 0,
      message: (data && data.message) || error.message || 'Request failed',
      fieldErrors: (data && data.fieldErrors) || {},
      path: data && data.path
    }
    return Promise.reject(normalised)
  }
)

export default client
