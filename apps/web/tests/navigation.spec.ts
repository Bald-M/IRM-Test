import { afterEach, describe, expect, it, vi } from 'vitest'

import { openExternalUrl } from '@/utils/navigation'

describe('external navigation', () => {
  afterEach(() => {
    vi.unstubAllGlobals()
  })

  it('opens an absolute HTTPS URL without opener access', () => {
    const open = vi.fn()
    vi.stubGlobal('window', { open })

    expect(openExternalUrl('https://example.com/profile')).toBe(true)
    expect(open).toHaveBeenCalledWith(
      'https://example.com/profile',
      '_blank',
      'noopener,noreferrer'
    )
  })

  it.each(['javascript:alert(1)', 'data:text/html,unsafe', '/relative']) (
    'rejects unsafe or non-absolute URL %s',
    (url) => {
      const open = vi.fn()
      vi.stubGlobal('window', { open })

      expect(openExternalUrl(url)).toBe(false)
      expect(open).not.toHaveBeenCalled()
    }
  )
})
