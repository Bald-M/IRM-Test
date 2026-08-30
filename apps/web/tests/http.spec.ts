import type { AxiosAdapter } from 'axios'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'

import http from '@/utils/http'

const adapter: AxiosAdapter = async (config) => ({
  data: null,
  status: 200,
  statusText: 'OK',
  headers: {},
  config
})

describe('HTTP authorization interceptor', () => {
  beforeEach(() => {
    vi.stubGlobal('localStorage', {
      getItem: vi.fn((key: string) => key === 'authKey' ? 'stored-token' : null)
    })
  })

  afterEach(() => {
    vi.unstubAllGlobals()
  })

  it('adds the authKey token to protected requests', async () => {
    const response = await http.get('/userProfileData', { adapter })

    expect(response.config.headers.get('Authorization')).toBe('Bearer stored-token')
  })

  it.each([
    '/login',
    '/registration',
    '/emailVerification',
    '/sendOTP',
    '/forgotPassRequest',
    '/forgotPassVerify',
    '/forgotPassChange'
  ])('does not add a token to public endpoint %s', async (endpoint) => {
    const response = await http.post(endpoint, undefined, { adapter })

    expect(response.config.headers.has('Authorization')).toBe(false)
  })
})
