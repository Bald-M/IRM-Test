import type { RouteLocationRaw } from 'vue-router'

interface RouteAccessRequest {
  path: string
  name?: string | symbol | null
  requiredRole?: string
}

interface RouteAccessState {
  authenticated: boolean
  role: string
  serverRef: string
  email: string
  resetOtp: string | null
}

type RouteAccessDecision = true | RouteLocationRaw

const PUBLIC_PATHS = new Set([
  '/',
  '/home',
  '/login',
  '/registration',
  '/requestResetPassword'
])

export const resolveRouteAccess = (
  request: RouteAccessRequest,
  state: RouteAccessState
): RouteAccessDecision => {
  if (PUBLIC_PATHS.has(request.path) || request.name === 'notFound') return true

  if (request.path === '/emailVerification') {
    return state.serverRef ? true : { name: 'login' }
  }

  if (request.path === '/resetPassVerification') {
    return state.serverRef && state.email ? true : { name: 'requestResetPassword' }
  }

  if (request.path === '/resetPassword') {
    return state.serverRef && state.email && state.resetOtp
      ? true
      : { name: 'requestResetPassword' }
  }

  if (!state.authenticated) return { name: 'login' }

  if (request.requiredRole && request.requiredRole !== state.role) {
    return { name: 'unauthorized' }
  }

  return true
}
