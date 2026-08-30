import { createPinia, setActivePinia } from 'pinia'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'

import { useAuthStore } from '@/stores/auth'

const installLocalStorage = () => {
  const values = new Map<string, string>()

  vi.stubGlobal('localStorage', {
    getItem: vi.fn((key: string) => values.get(key) ?? null),
    setItem: vi.fn((key: string, value: string) => values.set(key, value)),
    removeItem: vi.fn((key: string) => values.delete(key)),
    clear: vi.fn(() => values.clear())
  })
}

describe('auth store', () => {
  beforeEach(() => {
    installLocalStorage()
    setActivePinia(createPinia())
  })

  afterEach(() => {
    vi.unstubAllGlobals()
  })

  it('clears persisted and reactive authentication state', () => {
    const authStore = useAuthStore()
    authStore.setAuthData('token', 'Student', 'student@example.com', 42, 'Student')
    authStore.setServerRef('server-ref', 'student@example.com')

    authStore.clearAuthData()

    expect(authStore.authKey).toBe('')
    expect(authStore.server_ref).toBe('')
    expect(localStorage.getItem('authKey')).toBeNull()
    expect(localStorage.getItem('server_ref')).toBeNull()
    expect(localStorage.getItem('otp')).toBeNull()
  })

  it('restores password-reset state without requiring a login token', () => {
    localStorage.setItem('server_ref', 'reset-ref')
    localStorage.setItem('email', 'student@example.com')

    const authStore = useAuthStore()
    authStore.restoreAuthData()

    expect(authStore.server_ref).toBe('reset-ref')
    expect(authStore.email).toBe('student@example.com')
  })
})
