import axios from 'axios'

const api = axios.create({
  baseURL: '/api',
  timeout: 60000
})

export function createSession(url) {
  return api.post('/playwright/sessions', { url })
}

export function closeSession(sessionId) {
  return api.delete(`/playwright/sessions/${sessionId}`)
}

export function pingSession(sessionId) {
  return api.post(`/playwright/sessions/${sessionId}/ping`)
}

export function navigate(sessionId, url) {
  return api.post(`/playwright/sessions/${sessionId}/navigate`, { url })
}

export function getScreenshot(sessionId, selector = null) {
  return api.post(`/playwright/sessions/${sessionId}/screenshot`, { selector })
}

export function getElementAt(sessionId, x, y) {
  return api.post(`/playwright/sessions/${sessionId}/element`, { x, y })
}

export function testSelector(sessionId, selector, type) {
  return api.post(`/playwright/sessions/${sessionId}/test-selector`, { selector, type })
}

export default {
  createSession,
  closeSession,
  pingSession,
  navigate,
  getScreenshot,
  getElementAt,
  testSelector
}