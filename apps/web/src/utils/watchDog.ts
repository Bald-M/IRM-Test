import { useAuthStore } from '../stores/auth'
import axios from './http'
import { isAxiosError } from 'axios'


// Ask the server to validate the current token and return its expiration time
const checkTokenExpiration = async (authStore: ReturnType<typeof useAuthStore>) => {
  try {
    const app_uid = authStore.uid
    if (!app_uid) {
      return
    }

    await axios.post('/getTokenExpirationDate', { app_user_id: app_uid })

  } catch (error: unknown) {
    // Handle failures reported by the server-side token expiration check
    if (isAxiosError(error) && error.response?.status === 401) {
      alert(error.response.data?.error ?? 'Authentication failed')
      authStore.clearAuthData()
      window.location.reload()
    } else if (!isAxiosError(error) || !error.response) {
      alert('Network Error')
      authStore.clearAuthData()
      window.location.reload()
    }
  }
}


// The watcher function checks token expiration at intervals
const watcher = async (intervalMinutes: number) => {
  const authStore = useAuthStore()
  // Restore authentication data from the store
  authStore.restoreAuthData()

  // Immediately check the token status on initialization
  await checkTokenExpiration(authStore)

  // Convert the interval time from minutes to milliseconds
  const intervalMillis = intervalMinutes * 60 * 1000

  // Set an interval to check the token status periodically
  setInterval(async () => {
    await checkTokenExpiration(authStore)
  }, intervalMillis)

}

// Export the function for use in other module
export default watcher
