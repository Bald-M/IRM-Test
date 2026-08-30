import axios from 'axios'
import type { AxiosInstance, InternalAxiosRequestConfig, AxiosError } from 'axios'

const PUBLIC_ENDPOINTS = new Set([
  '/login',
  '/registration',
  '/emailVerification',
  '/sendOTP',
  '/forgotPassRequest',
  '/forgotPassVerify',
  '/forgotPassChange'
])

const normalizeApiPath = (url?: string) => {
  if (!url) return ''

  const pathname = new URL(url, 'http://localhost').pathname.replace(/\/$/, '')
  return pathname.startsWith('/api/') ? pathname.slice('/api'.length) : pathname
}

// create an axios instance with custom configuration
const service: AxiosInstance = axios.create({
  baseURL: import.meta.env.VITE_APP_API_URL || '/api',
  timeout: 20000 // Request timeout set to 20 seconds
})

// Request Interceptors
service.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const endpoint = normalizeApiPath(config.url)

    if (!PUBLIC_ENDPOINTS.has(endpoint)) {
      const token = localStorage.getItem('authKey')
      if (token) {
        config.headers.set('Authorization', `Bearer ${token}`)
      }
    }

    return config
  },
  (error: AxiosError) => {
    return Promise.reject(error) // Rejects the promise on request error
  }
)
// Exports the Axios instance with interceptors for use in other modules
export default service
