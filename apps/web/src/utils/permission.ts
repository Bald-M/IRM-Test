import router from '../router'
import { useAuthStore } from '../stores/auth'
import { resolveRouteAccess } from './routeAccess'

const setupRouterGuard = () => {
  router.beforeEach((to) => {
    const authStore = useAuthStore()
    authStore.restoreAuthData()

    const requiredRole = to.matched
      .map((record) => record.meta.role)
      .find((role): role is string => typeof role === 'string')

    return resolveRouteAccess(
      {
        path: to.path,
        name: to.name,
        requiredRole
      },
      {
        authenticated: Boolean(authStore.authKey),
        role: authStore.user_type,
        serverRef: authStore.server_ref,
        email: authStore.email,
        resetOtp: localStorage.getItem('otp')
      }
    )
  })
}

export default setupRouterGuard
