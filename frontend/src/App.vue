<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'

import { useAuthStore } from './stores/authStore'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const isLoginPage = computed(() => route.name === 'login')

async function handleLogout() {
  await authStore.logout()
  await router.replace('/login')
}
</script>

<template>
  <RouterView v-if="isLoginPage" />

  <div v-else class="app-layout">
    <aside class="sidebar">
      <div>
        <p class="eyebrow">Finance</p>
        <h1>个人记账</h1>
      </div>
      <nav>
        <RouterLink to="/">首页</RouterLink>
        <RouterLink to="/transactions">流水</RouterLink>
      </nav>
      <button class="logout-button" type="button" @click="handleLogout">退出登录</button>
    </aside>

    <main class="content">
      <RouterView />
    </main>
  </div>
</template>
