<template>
  <main class="auth-page">
    <section class="auth-panel">
      <div class="auth-logo">未来电音背词</div>
      <h1>登录</h1>
      <p class="muted">登录后进入首页，错题、收藏、我的词库和学习记录都会保存。</p>
      <form @submit.prevent="submit" class="form">
        <input v-model.trim="form.username" placeholder="用户名" autocomplete="username" />
        <input v-model="form.password" type="password" placeholder="密码" autocomplete="current-password" />
        <button class="primary" type="submit" :disabled="loading">{{ loading ? '登录中...' : '登录' }}</button>
      </form>
      <p class="error" v-if="error">{{ error }}</p>
      <router-link to="/register">没有账号？去注册</router-link>
    </section>
  </main>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { login } from '../api'

const router = useRouter()
const form = reactive({ username: '', password: '' })
const error = ref('')
const loading = ref(false)

async function submit() {
  error.value = ''
  if (!form.username || !form.password) {
    error.value = '请输入用户名和密码'
    return
  }
  loading.value = true
  try {
    const user = await login(form)
    if (!user.token) {
      throw new Error('登录状态缺失，请重新登录')
    }
    localStorage.setItem('user', JSON.stringify(user))
    localStorage.setItem('userId', user.id)
    localStorage.setItem('username', user.username)
    localStorage.setItem('token', user.token)
    router.push('/practice')
  } catch (e) {
    error.value = e.message
  } finally {
    loading.value = false
  }
}
</script>
