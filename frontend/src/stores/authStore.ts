import { defineStore } from 'pinia'

import { getCurrentUser, login, logout, type LoginPayload, type LoginUser } from '../api/auth'

interface AuthState {
  user: LoginUser | null
  checked: boolean
}

export const useAuthStore = defineStore('auth', {
  state: (): AuthState => ({
    user: null,
    checked: false,
  }),
  actions: {
    async login(payload: LoginPayload) {
      this.user = await login(payload)
      this.checked = true
    },
    async ensureSession() {
      if (this.checked) {
        return Boolean(this.user)
      }

      try {
        this.user = await getCurrentUser()
      } catch {
        this.user = null
      } finally {
        this.checked = true
      }

      return Boolean(this.user)
    },
    async logout() {
      try {
        await logout()
      } finally {
        this.clearSession()
      }
    },
    clearSession() {
      this.user = null
      this.checked = true
    },
  },
})
