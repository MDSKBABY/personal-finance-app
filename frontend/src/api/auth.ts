import { request } from './http'

export interface LoginPayload {
  username: string
  password: string
}

export interface LoginUser {
  username: string
  expiresAt: string | null
}

export function login(payload: LoginPayload) {
  return request<LoginUser>('/api/auth/login', {
    method: 'POST',
    body: JSON.stringify(payload),
  })
}

export function getCurrentUser() {
  return request<LoginUser>('/api/auth/me')
}

export function logout() {
  return request<void>('/api/auth/logout', {
    method: 'POST',
  })
}
