const DEFAULT_BASE_URL = 'http://localhost:9090'

/**
 * Normalize base URL so switching between environments only requires changing VITE_BASE_URL.
 */
const normalizeBaseUrl = (url) => {
  const target = url || DEFAULT_BASE_URL
  return target.endsWith('/') ? target.slice(0, -1) : target
}

export const BASE_URL = normalizeBaseUrl(import.meta.env.VITE_BASE_URL)
export const API_BASE_URL = BASE_URL
export const UPLOADS_BASE_URL = `${BASE_URL}/uploads`

/**
 * Helper to build upload asset URLs without repeating the host prefix.
 */
export const getUploadUrl = (fileName = '') => {
  if (!fileName) return UPLOADS_BASE_URL
  return `${UPLOADS_BASE_URL}/${fileName}`
}
