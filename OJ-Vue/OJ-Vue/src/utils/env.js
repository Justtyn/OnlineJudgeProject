const DEFAULT_BASE_URL = 'http://localhost:9090'
const DEFAULT_DEEPSEEK_API_URL = 'https://api.deepseek.com'
const env = import.meta.env || {}

/**
 * Normalize base URL so switching between environments only requires changing VITE_BASE_URL.
 */
const normalizeBaseUrl = (url) => {
  const target = url || DEFAULT_BASE_URL
  return target.endsWith('/') ? target.slice(0, -1) : target
}

const getEnvValue = (key, defaultValue = '') => {
  const raw = env[key]
  if (typeof raw === 'string') {
    const trimmed = raw.trim()
    return trimmed || defaultValue
  }
  return raw ?? defaultValue
}

export const BASE_URL = normalizeBaseUrl(getEnvValue('VITE_BASE_URL', DEFAULT_BASE_URL))
export const API_BASE_URL = BASE_URL
export const UPLOADS_BASE_URL = `${BASE_URL}/uploads`

export const DEEPSEEK_API_URL = normalizeBaseUrl(
  getEnvValue('VITE_DEEPSEEK_API_URL', DEFAULT_DEEPSEEK_API_URL)
)
export const DEEPSEEK_CHAT_COMPLETIONS_URL = `${DEEPSEEK_API_URL}/chat/completions`
export const DEEPSEEK_API_KEY = getEnvValue('VITE_DEEPSEEK_API_KEY', '')
export const isDeepSeekConfigured = () => Boolean(DEEPSEEK_API_KEY)

/**
 * Helper to build upload asset URLs without repeating the host prefix.
 */
export const getUploadUrl = (fileName = '') => {
  if (!fileName) return UPLOADS_BASE_URL
  return `${UPLOADS_BASE_URL}/${fileName}`
}
