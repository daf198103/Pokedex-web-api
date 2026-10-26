import { describe, expect, it } from 'vitest'
import { AxiosError, type AxiosResponse } from 'axios'
import { shouldRetry, toApiError } from '@/api/errors'

function axiosError(status: number, data?: unknown): AxiosError {
  const response = { status, data, statusText: '', headers: {}, config: {} } as AxiosResponse
  return new AxiosError('failed', 'ERR_BAD_REQUEST', undefined, undefined, response)
}

describe('toApiError', () => {
  it('reads RFC 7807 detail and field errors', () => {
    const err = toApiError(axiosError(400, { detail: 'Request body is invalid', errors: { region: 'too long' } }))
    expect(err.status).toBe(400)
    expect(err.message).toBe('Request body is invalid')
    expect(err.fieldErrors).toEqual({ region: 'too long' })
  })

  it('falls back to a friendly message when the body is empty', () => {
    const err = toApiError(axiosError(401))
    expect(err.status).toBe(401)
    expect(err.message).toMatch(/not signed in/i)
  })

  it('handles network errors without a response', () => {
    const err = toApiError(new AxiosError('Network Error', 'ERR_NETWORK'))
    expect(err.status).toBeNull()
    expect(err.message).toMatch(/cannot reach the server/i)
  })
})

describe('shouldRetry', () => {
  it('never retries client errors', () => {
    expect(shouldRetry(0, axiosError(404))).toBe(false)
    expect(shouldRetry(0, axiosError(409))).toBe(false)
  })

  it('retries server errors a limited number of times', () => {
    expect(shouldRetry(0, axiosError(502))).toBe(true)
    expect(shouldRetry(2, axiosError(502))).toBe(false)
  })
})
