import axios from 'axios'

const request = axios.create({
  baseURL: '/api',
  timeout: 10000
})

export function getLoginUser() {
  try {
    const raw = localStorage.getItem('user')
    return raw ? JSON.parse(raw) : null
  } catch (e) {
    return null
  }
}

export function getAuthToken() {
  const user = getLoginUser()
  return user?.token || localStorage.getItem('token')
}

export function clearLoginUser() {
  localStorage.removeItem('user')
  localStorage.removeItem('userId')
  localStorage.removeItem('username')
  localStorage.removeItem('token')
}

request.interceptors.request.use(config => {
  const token = getAuthToken()
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

request.interceptors.response.use(
  response => {
    const body = response.data
    if (body.code !== 200) {
      if (body.code === 401) clearLoginUser()
      if (body.code === 402 && window.location.pathname !== '/membership') {
        window.location.href = '/membership'
      }
      return Promise.reject(new Error(body.message || 'Request failed'))
    }
    return body.data
  },
  error => {
    if (error.response?.status === 401 || error.response?.data?.code === 401) {
      clearLoginUser()
      if (!['/login', '/register'].includes(window.location.pathname)) {
        window.location.href = '/login'
      }
    }
    if ((error.response?.status === 402 || error.response?.data?.code === 402)
        && window.location.pathname !== '/membership') {
      window.location.href = '/membership'
    }
    return Promise.reject(new Error(error.response?.data?.message || error.message || 'Network error'))
  }
)

export default request
