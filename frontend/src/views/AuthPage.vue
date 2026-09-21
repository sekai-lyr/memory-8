<template>
  <div class="auth" :class="{ 'no-motion': rm }">
    <div class="auth-layout">
      <section class="auth-hero" aria-labelledby="auth-title">
        <div class="auth-brandline">
          <span class="auth-brand-mark" aria-hidden="true">SK</span>
          <span>SEKAI / LANGUAGE LAB</span>
        </div>
        <p class="auth-kicker">English vocabulary, organized.</p>
        <h1 id="auth-title" class="auth-h1">
          <span>把今天的词</span>
          <span>变成明天的熟悉。</span>
        </h1>
        <p class="auth-desc">
          从一轮练习开始。系统会记住你的错题、词库和复习节奏，让每次打开都知道下一步。
        </p>
        <div class="auth-art">
          <img :src="studyHero" alt="夜间书桌上的英语词汇笔记、计时器和薄荷色索引标签" />
        </div>
        <div class="auth-ritual" aria-label="学习入口">
          <span><strong>01</strong> 练习</span>
          <span><strong>02</strong> 复习</span>
          <span><strong>03</strong> 记录</span>
        </div>
      </section>

      <aside class="auth-card" aria-label="账号入口">
        <div class="auth-card-head">
          <span class="auth-card-mark" aria-hidden="true">SK</span>
          <div>
            <h2 class="auth-card-title">进入学习空间</h2>
            <p class="auth-card-sub">你的词库和进度会保存在这里</p>
          </div>
        </div>

        <div class="auth-tabs" role="tablist" aria-label="账号操作">
          <button type="button" role="tab" :aria-selected="mode === 'login'" :class="{ on: mode === 'login' }" @click="switchMode('login')">登录</button>
          <button type="button" role="tab" :aria-selected="mode === 'register'" :class="{ on: mode === 'register' }" @click="switchMode('register')">注册</button>
        </div>

        <form class="auth-form" @submit.prevent="submit" novalidate>
          <label class="auth-field">
            <span>用户名</span>
            <input ref="uname" v-model.trim="form.username" type="text" autocomplete="username" />
          </label>

          <label class="auth-field">
            <span>密码</span>
            <div class="auth-pw-wrap">
              <input v-model="form.password" :type="showPw ? 'text' : 'password'" autocomplete="current-password" />
              <button type="button" class="auth-pw-btn" @click="showPw = !showPw">{{ showPw ? '隐藏' : '显示' }}</button>
            </div>
          </label>

          <label v-if="mode === 'register'" class="auth-field">
            <span>确认密码</span>
            <input v-model="form.confirmPassword" type="password" autocomplete="new-password" />
          </label>

          <p v-if="error" class="auth-msg e" role="alert">{{ error }}</p>
          <p v-if="success" class="auth-msg s" role="status">{{ success }}</p>

          <button class="auth-btn" type="submit" :disabled="loading">
            <span v-if="!loading">{{ mode === 'login' ? '进入学习空间' : '创建账号' }}</span>
            <span v-else class="auth-dots" aria-label="处理中"><i></i><i></i><i></i></span>
          </button>
        </form>
        <p class="auth-foot">SEKAI / 智能词汇记忆系统</p>
      </aside>
    </div>
  </div>
</template>

<script setup>
import { nextTick, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { login, register as apiRegister } from '../api'
import studyHero from '../assets/study-hero.png'

const router = useRouter()
const route = useRoute()
const mode = ref(route.path === '/register' ? 'register' : 'login')
const form = reactive({ username: '', password: '', confirmPassword: '' })
const error = ref('')
const success = ref('')
const loading = ref(false)
const showPw = ref(false)
const rm = ref(false)
const uname = ref(null)

function switchMode(nextMode) {
  if (loading.value) return
  mode.value = nextMode
  error.value = ''
  success.value = ''
  form.confirmPassword = ''
  router.replace(nextMode === 'login' ? '/login' : '/register')
  nextTick(() => uname.value?.focus())
}

async function submit() {
  error.value = ''
  success.value = ''
  if (!form.username.trim() || !form.password) {
    error.value = '请填写用户名和密码'
    return
  }
  if (mode.value === 'register') {
    if (!form.confirmPassword) {
      error.value = '请确认密码'
      return
    }
    if (form.password !== form.confirmPassword) {
      error.value = '两次密码必须一致'
      return
    }
  }

  loading.value = true
  try {
    if (mode.value === 'login') {
      const user = await login({ username: form.username.trim(), password: form.password })
      if (!user.token) throw new Error('登录状态缺失')
      localStorage.setItem('user', JSON.stringify(user))
      localStorage.setItem('userId', user.id)
      localStorage.setItem('username', user.username)
      localStorage.setItem('token', user.token)
      router.push('/practice')
    } else {
      await apiRegister({
        username: form.username.trim(),
        password: form.password,
        confirmPassword: form.confirmPassword
      })
      success.value = '注册成功，即将跳转...'
      setTimeout(() => switchMode('login'), 800)
    }
  } catch (e) {
    error.value = e.message
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  rm.value = matchMedia('(prefers-reduced-motion: reduce)').matches
  nextTick(() => uname.value?.focus())
})
</script>

<style scoped>
.auth {
  --auth-bg: #0a1020;
  --auth-surface: rgba(17, 28, 47, .86);
  --auth-text: #edf4f1;
  --auth-muted: #a5b6bf;
  --auth-line: rgba(212, 232, 227, .16);
  --auth-accent: #72e4c6;
  min-height: 100dvh;
  overflow: hidden;
  color: var(--auth-text);
  background:
    linear-gradient(90deg, rgba(8, 13, 25, .62), rgba(8, 13, 25, .3)),
    url('../assets/study-hero.png') right center / cover no-repeat,
    var(--auth-bg);
}

.auth::before {
  content: '';
  position: fixed;
  inset: 0;
  pointer-events: none;
  background: radial-gradient(circle at 12% 18%, rgba(114, 228, 198, .13), transparent 28%);
}

.auth-layout {
  position: relative;
  z-index: 1;
  display: grid;
  grid-template-columns: minmax(0, 1.1fr) minmax(360px, 430px);
  gap: clamp(48px, 9vw, 150px);
  align-items: center;
  width: min(1320px, calc(100% - 64px));
  min-height: 100dvh;
  margin: 0 auto;
  padding: 52px 0;
}

.auth-hero { max-width: 700px; }
.auth-brandline { display: inline-flex; align-items: center; gap: 11px; margin-bottom: 72px; color: rgba(225, 242, 237, .72); font-size: .72rem; font-weight: 700; letter-spacing: .16em; }
.auth-brand-mark, .auth-card-mark { display: inline-grid; place-items: center; width: 38px; height: 38px; color: #07131a; background: var(--auth-accent); border-radius: 9px; font-size: .72rem; font-weight: 900; letter-spacing: -.04em; }
.auth-kicker { margin: 0 0 12px; color: var(--auth-accent); font-size: .78rem; font-weight: 700; letter-spacing: .11em; text-transform: uppercase; }
.auth-h1 { max-width: 680px; margin: 0; color: #f0f6f2; font-size: clamp(2.8rem, 6vw, 5.9rem); font-weight: 650; letter-spacing: -.065em; line-height: .98; }
.auth-h1 span { display: block; }
.auth-h1 span:last-child { color: var(--auth-accent); }
.auth-desc { max-width: 480px; margin: 26px 0 0; color: var(--auth-muted); font-size: 1rem; line-height: 1.8; }
.auth-art { display: none; }
.auth-ritual { display: flex; gap: 22px; margin-top: 56px; padding-top: 16px; border-top: 1px solid rgba(225, 242, 237, .18); color: rgba(225, 242, 237, .54); font-size: .76rem; letter-spacing: .04em; }
.auth-ritual span { display: inline-flex; align-items: center; gap: 7px; }
.auth-ritual strong { color: var(--auth-accent); font-size: .65rem; }

.auth-card { width: 100%; padding: 34px; border: 1px solid var(--auth-line); border-radius: 16px; background: var(--auth-surface); box-shadow: 0 24px 70px rgba(0, 0, 0, .3), inset 0 1px 0 rgba(255, 255, 255, .05); backdrop-filter: blur(24px); -webkit-backdrop-filter: blur(24px); }
.auth-card-head { display: flex; align-items: center; gap: 13px; margin-bottom: 30px; }
.auth-card-title { margin: 0 0 3px; color: var(--auth-text); font-size: 1.35rem; letter-spacing: -.03em; }
.auth-card-sub { margin: 0; color: var(--auth-muted); font-size: .82rem; }
.auth-tabs { display: flex; gap: 21px; margin-bottom: 22px; border-bottom: 1px solid var(--auth-line); }
.auth-tabs button { position: relative; padding: 0 0 11px; border: 0; color: rgba(225, 242, 237, .52); background: transparent; font: inherit; font-size: .86rem; }
.auth-tabs button::after { content: ''; position: absolute; right: 0; bottom: -1px; left: 0; height: 2px; background: transparent; }
.auth-tabs button.on { color: var(--auth-text); }
.auth-tabs button.on::after { background: var(--auth-accent); }
.auth-form { display: grid; gap: 16px; }
.auth-field { display: grid; gap: 7px; color: var(--auth-muted); font-size: .75rem; font-weight: 700; }
.auth-form input { width: 100%; min-height: 46px; padding: 0 13px; border: 1px solid var(--auth-line); border-radius: 8px; outline: 0; color: var(--auth-text); background: rgba(7, 14, 26, .5); font: inherit; font-size: .92rem; transition: border-color .2s ease, box-shadow .2s ease, background .2s ease; }
.auth-form input:focus { border-color: var(--auth-accent); box-shadow: 0 0 0 3px rgba(114, 228, 198, .12); background: rgba(7, 14, 26, .72); }
.auth-pw-wrap { position: relative; }
.auth-pw-wrap input { padding-right: 58px; }
.auth-pw-btn { position: absolute; top: 50%; right: 10px; min-width: auto; padding: 4px; border: 0; color: var(--auth-accent); background: transparent; font-size: .72rem; transform: translateY(-50%); }
.auth-msg { margin: 0; padding: 10px 12px; border-radius: 8px; font-size: .78rem; line-height: 1.5; }
.auth-msg.e { border: 1px solid rgba(255, 133, 131, .28); color: #ffb1a8; background: rgba(115, 35, 42, .25); }
.auth-msg.s { border: 1px solid rgba(114, 228, 198, .3); color: var(--auth-accent); background: rgba(37, 110, 92, .2); }
.auth-btn { width: 100%; min-height: 48px; border: 0; border-radius: 8px; color: #07131a; background: var(--auth-accent); font: inherit; font-weight: 800; letter-spacing: .02em; }
.auth-btn:hover:not(:disabled) { background: #91f0d7; }
.auth-btn:disabled { cursor: wait; opacity: .62; }
.auth-dots { display: inline-flex; gap: 4px; }
.auth-dots i { width: 5px; height: 5px; border-radius: 50%; background: #07131a; animation: authDot 900ms ease-in-out infinite alternate; }
.auth-dots i:nth-child(2) { animation-delay: 130ms; }
.auth-dots i:nth-child(3) { animation-delay: 260ms; }
.auth-foot { margin: 24px 0 0; color: rgba(225, 242, 237, .36); font-size: .68rem; text-align: center; }

@keyframes authDot { to { transform: translateY(-4px); opacity: .46; } }

@media (max-width: 900px) {
  .auth { min-height: 100dvh; background-position: 60% center; }
  .auth-layout { grid-template-columns: 1fr; width: min(560px, calc(100% - 36px)); gap: 32px; padding: 32px 0; }
  .auth-hero { max-width: none; }
  .auth-brandline { margin-bottom: 44px; }
  .auth-h1 { font-size: clamp(2.7rem, 10vw, 4.8rem); }
  .auth-desc { max-width: 560px; }
  .auth-ritual { margin-top: 32px; }
  .auth-card { max-width: 520px; }
}
@media (max-width: 520px) {
  .auth-layout { width: min(100% - 28px, 430px); padding: 22px 0; }
  .auth-brandline { margin-bottom: 34px; font-size: .62rem; }
  .auth-h1 { font-size: clamp(2.45rem, 13vw, 3.8rem); }
  .auth-desc { font-size: .9rem; }
  .auth-ritual { gap: 14px; margin-top: 28px; }
  .auth-card { padding: 25px 20px; }
}
@media (prefers-reduced-motion: reduce) {
  .auth.no-motion *, .auth.no-motion *::before, .auth.no-motion *::after { animation: none !important; transition: none !important; }
}
</style>
