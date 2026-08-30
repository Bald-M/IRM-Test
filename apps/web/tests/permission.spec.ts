import { describe, expect, it } from 'vitest'

import { resolveRouteAccess } from '@/utils/routeAccess'

const guest = {
  authenticated: false,
  role: '',
  serverRef: '',
  email: '',
  resetOtp: null
}

describe('route access decisions', () => {
  it('allows public and not-found routes', () => {
    expect(resolveRouteAccess({ path: '/home' }, guest)).toBe(true)
    expect(resolveRouteAccess({ path: '/missing', name: 'notFound' }, guest)).toBe(true)
  })

  it('preserves verification flows across a page refresh', () => {
    const verificationState = {
      ...guest,
      serverRef: 'server-ref',
      email: 'student@example.com',
      resetOtp: '123456'
    }

    expect(resolveRouteAccess({ path: '/emailVerification' }, verificationState)).toBe(true)
    expect(resolveRouteAccess({ path: '/resetPassVerification' }, verificationState)).toBe(true)
    expect(resolveRouteAccess({ path: '/resetPassword' }, verificationState)).toBe(true)
  })

  it('redirects an incomplete reset flow', () => {
    expect(resolveRouteAccess({ path: '/resetPassword' }, guest)).toEqual({
      name: 'requestResetPassword'
    })
  })

  it('enforces route metadata for authenticated roles', () => {
    const student = { ...guest, authenticated: true, role: 'Student' }

    expect(
      resolveRouteAccess(
        { path: '/student/profile', requiredRole: 'Student' },
        student
      )
    ).toBe(true)
    expect(
      resolveRouteAccess(
        { path: '/admin/studentsList', requiredRole: 'Admin' },
        student
      )
    ).toEqual({ name: 'unauthorized' })
  })
})
