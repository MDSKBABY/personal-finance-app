<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'

import { useAuthStore } from '../stores/authStore'

const router = useRouter()
const authStore = useAuthStore()

const username = ref('admin')
const password = ref('')
const errorMessage = ref('')
const loading = ref(false)

async function handleLogin() {
  errorMessage.value = ''
  loading.value = true

  try {
    await authStore.login({
      username: username.value.trim(),
      password: password.value,
    })
    await router.replace('/')
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '登录失败，请稍后再试'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <main class="login-page">
    <section class="login-card">
      <p class="eyebrow">Finance</p>
      <h1>登录个人记账</h1>
      <p class="login-subtitle">请输入本地账号密码后继续查看你的记账数据。</p>

      <form class="login-form" @submit.prevent="handleLogin">
        <label>
          <span>账号</span>
          <input v-model="username" autocomplete="username" placeholder="请输入账号" required />
        </label>

        <label>
          <span>密码</span>
          <input
            v-model="password"
            autocomplete="current-password"
            placeholder="请输入密码"
            required
            type="password"
          />
        </label>

        <p v-if="errorMessage" class="form-error">{{ errorMessage }}</p>

        <button class="primary-button login-button" :disabled="loading" type="submit">
          {{ loading ? '登录中...' : '登录' }}
        </button>
      </form>
    </section>
  </main>
</template>
