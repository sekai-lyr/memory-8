<template>
  <main class="auth-page">
    <section class="auth-panel">
      <div class="auth-logo">未来电音背词</div>
      <h1>注册账号</h1>
      <p class="muted">每个用户拥有独立的错题本、收藏单词、自定义词库和学习记录。</p>
      <form @submit.prevent="submit" class="form">
        <input v-model.trim="form.username" placeholder="用户名" />
        <input v-model="form.password" type="password" placeholder="密码" />
        <input v-model="form.confirmPassword" type="password" placeholder="确认密码" />
        <button class="primary" type="submit" :disabled="loading">{{ loading ? '注册中...' : '注册' }}</button>
      </form>
      <p class="error" v-if="error">{{ error }}</p>
      <p class="success" v-if="success">{{ success }}</p>
      <router-link to="/login">已有账号？去登录</router-link>
    </section>
  </main>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { register } from '../api'

const router = useRouter()
const form = reactive({ username: '', password: '', confirmPassword: '' })
const error = ref('')
const success = ref('')
const loading = ref(false)

async function submit() {
  error.value = ''
  success.value = ''
  if (!form.username || !form.password || !form.confirmPassword) {
    error.value = '请完整填写注册信息'
    return
  }
  if (form.password !== form.confirmPassword) {
    error.value = '两次密码必须一致'
    return
  }
  loading.value = true
  try {
    await register(form)
    success.value = '注册成功，正在跳转登录页'
    setTimeout(() => router.push('/login'), 800)
  } catch (e) {
    error.value = e.message
  } finally {
    loading.value = false
  }
}
</script>
