const ALLOWED_EXTERNAL_PROTOCOLS = new Set(['https:', 'http:'])

export const openExternalUrl = (value: string): boolean => {
  try {
    const url = new URL(value)
    if (!ALLOWED_EXTERNAL_PROTOCOLS.has(url.protocol)) return false

    window.open(url.href, '_blank', 'noopener,noreferrer')
    return true
  } catch {
    return false
  }
}
