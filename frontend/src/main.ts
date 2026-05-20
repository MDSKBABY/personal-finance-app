import { createApp } from 'vue'
import { createPinia } from 'pinia'

import App from './App.vue'
import { router } from './router'
import { useAuthStore } from './stores/authStore'
import './style.css'

const app = createApp(App)
const pinia = createPinia()

app.use(pinia)

window.addEventListener('finance:unauthorized', () => {
  const authStore = useAuthStore(pinia)
  authStore.clearSession()

  if (router.currentRoute.value.name !== 'login') {
    router.replace('/login')
  }
})

router.beforeEach(async (to) => {
  const authStore = useAuthStore(pinia)

  if (to.name === 'login') {
    if (await authStore.ensureSession()) {
      return { name: 'dashboard' }
    }
    return true
  }

  if (await authStore.ensureSession()) {
    return true
  }

  return { name: 'login' }
})

app.use(router)
app.mount('#app')
