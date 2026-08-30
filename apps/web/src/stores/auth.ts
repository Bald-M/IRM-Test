import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useAuthStore = defineStore('user', () => {
  const authKey = ref('')
  const name = ref('')
  const email = ref('')
  const uid = ref()
  const user_type = ref('')
  const server_ref = ref('')

  // Save token and user info
  function setAuthData(token: string, uName: string, uEmail: string, appUID: number, role: string) {
    authKey.value = token
    name.value = uName
    email.value = uEmail
    uid.value = appUID
    user_type.value = role
    localStorage.setItem('authKey', token)
    localStorage.setItem('name', uName)
    localStorage.setItem('email', uEmail)
    localStorage.setItem('app_uid', appUID.toString())
    localStorage.setItem('user_type', role)
  }

  // Save Server Ref
  // Also need to save the email from user input
  function setServerRef(ref: string, uEmail: string) {
    server_ref.value = ref
    email.value = uEmail
    localStorage.setItem('server_ref', ref)
    localStorage.setItem('email', uEmail)
  }

  // Clear token and user info
  function clearAuthData() {
    authKey.value = ''
    name.value = ''
    email.value = ''
    uid.value = ''
    user_type.value = ''
    server_ref.value = ''
    // profileData.value = {}
    localStorage.removeItem('authKey')
    localStorage.removeItem('name')
    localStorage.removeItem('email')
    localStorage.removeItem('app_uid')
    localStorage.removeItem('user_type')
    localStorage.removeItem('server_ref')
    localStorage.removeItem('otp')
  }

  // Restore the independent login and verification state after a refresh.
  function restoreAuthData() {
    const storedToken = localStorage.getItem('authKey')
    // const storedProfileData = localStorage.getItem('profileData')
    const storedName = localStorage.getItem('name')
    const storedEmail = localStorage.getItem('email')
    const storedAppUID = localStorage.getItem('app_uid')
    const storedUserType = localStorage.getItem('user_type')
    const storedServerRef = localStorage.getItem('server_ref')
    if (storedToken && storedName && storedAppUID && storedUserType) {
      authKey.value = storedToken
      name.value = storedName
      uid.value = storedAppUID
      user_type.value = storedUserType
    }
    if (storedEmail) {
      email.value = storedEmail
    }
    if (storedServerRef) {
      server_ref.value = storedServerRef
    }
  }

  return { 
    authKey,
    name,
    email,
    uid,
    user_type,
    server_ref,
    setAuthData,
    setServerRef,
    clearAuthData,
    restoreAuthData
  }
})
