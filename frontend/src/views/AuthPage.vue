<template>
  <div class="auth" :class="{ 'no-motion': rm }">
    <canvas ref="cvs" class="auth-canvas"></canvas>

    <div class="auth-orbs">
      <div class="auth-orb a1"></div>
      <div class="auth-orb a2"></div>
      <div class="auth-orb a3"></div>
    </div>

    <div class="auth-grid"></div>
    <div class="auth-rings">
      <div class="auth-ring ar1"></div>
      <div class="auth-ring ar2"></div>
    </div>
    <div class="auth-vig"></div>

    <div class="auth-layout">
      <section class="auth-hero">
        <div class="auth-logo">
          <div class="auth-logo-dot"></div>
        </div>
        <p class="auth-eyebrow">
          <span class="auth-blink"></span>
          SEKAI PROJECT — AI LANGUAGE LAB
        </p>
        <h1 class="auth-h1">
          <span class="auth-h1-l">穿越语言次元</span>
          <span class="auth-h1-l">开启赛博学习之旅</span>
        </h1>
        <p class="auth-desc">
          智能背词系统 · 虚拟 AI 学习伴侣<br />
          用科技重新定义记忆的力量
        </p>
        <div class="auth-wave">
          <span v-for="i in 6" :key="i" class="auth-wave-bar" :style="{ animationDelay: i * .1 + 's' }"></span>
        </div>
      </section>

      <aside class="auth-card">
        <div class="auth-card-head">
          <div class="auth-avatar">
            <div class="auth-avatar-dot"></div>
          </div>
          <h2 class="auth-card-title">欢迎来到 SEKAI</h2>
          <p class="auth-card-sub">登录你的学习空间</p>
        </div>

        <div class="auth-tabs">
          <button :class="{ on: mode === 'login' }" @click="switchMode('login')">登录</button>
          <button :class="{ on: mode === 'register' }" @click="switchMode('register')">注册</button>
        </div>

        <form class="auth-form" @submit.prevent="submit" novalidate>
          <input ref="uname" v-model.trim="form.username" type="text" placeholder="用户名" autocomplete="username" />
          <div class="auth-pw-wrap">
            <input v-model="form.password" :type="showPw ? 'text' : 'password'" placeholder="密码" autocomplete="current-password" />
            <button type="button" class="auth-pw-btn" @click="showPw = !showPw" tabindex="-1">
              <svg v-if="!showPw" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"><path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/><circle cx="12" cy="12" r="3"/></svg>
              <svg v-else viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"><path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94"/><path d="M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19"/><path d="M14.12 14.12a3 3 0 1 1-4.24-4.24"/><line x1="1" y1="1" x2="23" y2="23"/></svg>
            </button>
          </div>
          <input v-if="mode === 'register'" v-model="form.confirmPassword" type="password" placeholder="确认密码" autocomplete="new-password" />
          <p v-if="error" class="auth-msg e">{{ error }}</p>
          <p v-if="success" class="auth-msg s">{{ success }}</p>
          <button class="auth-btn" type="submit" :disabled="loading">
            <span v-if="!loading">{{ mode === 'login' ? '进入学习空间' : '创建账号' }}</span>
            <span v-else class="auth-dots"><i></i><i></i><i></i></span>
          </button>
        </form>
        <p class="auth-foot">SEKAI PROJECT — 智能词汇记忆系统</p>
      </aside>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted, onUnmounted, nextTick } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { login, register as apiRegister } from '../api'

const router = useRouter()
const route = useRoute()
const mode = ref(route.path === '/register' ? 'register' : 'login')
const form = reactive({ username: '', password: '', confirmPassword: '' })
const error = ref(''); const success = ref('')
const loading = ref(false); const showPw = ref(false); const rm = ref(false)
const uname = ref(null); const cvs = ref(null)
let raf = null

function switchMode(m) {
  if (loading.value) return
  mode.value = m; error.value = ''; success.value = ''; form.confirmPassword = ''
  router.replace(m === 'login' ? '/login' : '/register')
  nextTick(() => uname.value?.focus())
}
async function submit() {
  error.value = ''; success.value = ''
  if (!form.username.trim() || !form.password) { error.value = '请填写用户名和密码'; return }
  if (mode.value === 'register') {
    if (!form.confirmPassword) { error.value = '请确认密码'; return }
    if (form.password !== form.confirmPassword) { error.value = '两次密码必须一致'; return }
  }
  loading.value = true
  try {
    if (mode.value === 'login') {
      const u = await login({ username: form.username.trim(), password: form.password })
      if (!u.token) throw new Error('登录状态缺失')
      localStorage.setItem('user', JSON.stringify(u))
      localStorage.setItem('userId', u.id); localStorage.setItem('username', u.username); localStorage.setItem('token', u.token)
      router.push('/practice')
    } else {
      await apiRegister({ username: form.username.trim(), password: form.password, confirmPassword: form.confirmPassword })
      success.value = '注册成功，即将跳转...'
      setTimeout(() => switchMode('login'), 800)
    }
  } catch (e) { error.value = e.message } finally { loading.value = false }
}

onMounted(() => {
  rm.value = matchMedia('(prefers-reduced-motion: reduce)').matches
  nextTick(() => uname.value?.focus())

  const el = cvs.value; if (!el) return
  const ctx = el.getContext('2d')
  const dpr = Math.min(devicePixelRatio || 1, 2)
  let W, H, pts = []

  function rs() {
    W = innerWidth; H = innerHeight
    el.width = W * dpr; el.height = H * dpr
    el.style.width = W + 'px'; el.style.height = H + 'px'
    ctx.setTransform(dpr, 0, 0, dpr, 0, 0)
    pts = Array.from({ length: 35 }, () => ({ x: Math.random()*W, y: Math.random()*H, r: Math.random()*1.6+.4, vx: (Math.random()-.5)*.18, vy: Math.random()*.22+.04, a: Math.random()*.28+.04 }))
  }
  rs(); addEventListener('resize', rs)

  function frame() {
    ctx.clearRect(0, 0, W, H)
    for (const p of pts) {
      p.x += p.vx; p.y -= p.vy
      if (p.y < -8) { p.y = H+8; p.x = Math.random()*W }
      if (p.x<-8) p.x = W+8; if (p.x>W+8) p.x = -8
      ctx.beginPath(); ctx.arc(p.x, p.y, p.r, 0, Math.PI*2)
      ctx.fillStyle = `rgba(0,229,217,${p.a.toFixed(2)})`; ctx.fill()
    }
    raf = requestAnimationFrame(frame)
  }
  frame()
  onUnmounted(() => cancelAnimationFrame(raf))
})
</script>

<style scoped>
.auth {
  --c: #00e5d9; --c2: #5ef7ef;
  position: fixed; inset: 0; overflow: hidden;
  font-family: "HarmonyOS Sans SC","Noto Sans SC","Microsoft YaHei",system-ui,sans-serif;
  color: #dceeff; background: #080c1a;
}
.auth-canvas { position: absolute; inset: 0; z-index: 2; pointer-events: none; }

.auth-orbs { position: absolute; inset: 0; z-index: 0; }
.auth-orb { position: absolute; border-radius: 50%; filter: blur(120px); opacity: .09; }
.a1 { width: 600px; height: 600px; top: -22%; left: -12%; background: radial-gradient(circle, rgba(0,229,217,.65), transparent 65%); }
.a2 { width: 450px; height: 450px; bottom: -16%; right: -8%; background: radial-gradient(circle, rgba(77,171,247,.35), transparent 65%); }
.a3 { width: 280px; height: 280px; top: 38%; left: 52%; background: radial-gradient(circle, rgba(177,151,252,.25), transparent 65%); }

.auth-grid {
  position: absolute; inset: 0; z-index: 1;
  background-image: linear-gradient(rgba(0,229,217,.016) 1px, transparent 1px), linear-gradient(90deg, rgba(0,229,217,.016) 1px, transparent 1px);
  background-size: 54px 54px;
  mask-image: radial-gradient(ellipse at 50% 50%, #000 28%, transparent 72%);
  -webkit-mask-image: radial-gradient(ellipse at 50% 50%, #000 28%, transparent 72%);
}
.auth-rings { position: absolute; inset: 0; z-index: 1; }
.auth-ring { position: absolute; border-radius: 50%; border: 1px solid rgba(0,229,217,.035); }
.ar1 { width: 260px; height: 260px; top: 10%; left: 5%; }
.ar2 { width: 200px; height: 200px; bottom: 16%; right: 6%; }
.auth-vig { position: absolute; inset: 0; z-index: 3; background: radial-gradient(ellipse at 50% 50%, transparent 54%, rgba(8,12,26,.6) 100%); pointer-events: none; }

/* layout */
.auth-layout {
  position: relative; z-index: 10;
  display: grid; grid-template-columns: 56% 5% 39%;
  align-items: center; width: 100%; height: 100%;
  padding: 0 5vw; max-width: 1500px; margin: 0 auto;
}

/* hero */
.auth-hero { grid-column: 1; }
.auth-logo { margin-bottom: 26px; animation: fu .7s .1s both; }
.auth-logo-dot { width: 44px; height: 44px; border-radius: 50%; background: var(--c); box-shadow: 0 0 22px rgba(0,229,217,.3), 0 0 55px rgba(0,229,217,.08); }

.auth-eyebrow { font-size: .6rem; font-weight: 500; letter-spacing: .3em; color: rgba(0,229,217,.55); margin: 0 0 22px; display: flex; align-items: center; gap: 8px; animation: fu .7s .2s both; }
.auth-blink { width: 5px; height: 5px; border-radius: 50%; background: var(--c); display: inline-block; animation: bl 2.5s ease-in-out infinite; }
@keyframes bl { 0%,100% { opacity: 1 } 50% { opacity: .15 } }

.auth-h1 { font-family: "Noto Serif SC","Source Han Serif SC","STSong","SimSun",serif; font-weight: 500; font-size: clamp(32px,3.6vw,48px); line-height: 1.22; color: #eaf4ff; margin: 0 0 20px; }
.auth-h1-l { display: block; }
.auth-h1-l:first-child { animation: fu .7s .3s both; }
.auth-h1-l:last-child { animation: fu .7s .5s both; }

.auth-desc { font-size: clamp(.78rem,.9vw,.88rem); line-height: 1.85; color: rgba(180,210,235,.5); max-width: 380px; margin: 0; animation: fu .7s .6s both; }

.auth-wave { display: flex; align-items: flex-end; gap: 2.5px; margin-top: 30px; height: 28px; animation: fu .7s .75s both; }
.auth-wave-bar { width: 2.5px; border-radius: 2px; background: var(--c); animation: wv .85s ease-in-out infinite; height: 5px; }
@keyframes wv { 0%,100% { height: 5px; opacity: .25 } 50% { height: 24px; opacity: .65 } }

/* card */
.auth-card {
  grid-column: 3; justify-self: end;
  width: min(390px, 100%); padding: 34px 30px;
  background: rgba(11,18,36,.66);
  backdrop-filter: blur(20px); -webkit-backdrop-filter: blur(20px);
  border: 1px solid rgba(255,255,255,.04);
  border-radius: 20px; box-shadow: 0 24px 60px rgba(0,0,0,.5);
  position: relative; overflow: hidden;
  animation: si .7s .4s both;
}
.auth-card::before { content: ''; position: absolute; top: 0; left: 0; right: 0; height: 1px; background: linear-gradient(90deg, transparent, rgba(0,229,217,.18), transparent); }
@keyframes si { from { opacity: 0; transform: translateX(20px) } to { opacity: 1; transform: translateX(0) } }

.auth-card-head { text-align: center; margin-bottom: 20px; }
.auth-avatar { width: 42px; height: 42px; margin: 0 auto 12px; border-radius: 50%; display: grid; place-items: center; border: 1px solid rgba(0,229,217,.1); }
.auth-avatar-dot { width: 12px; height: 12px; border-radius: 50%; background: var(--c); box-shadow: 0 0 10px rgba(0,229,217,.35); }
.auth-card-title { font-family: "Noto Serif SC","Source Han Serif SC","STSong","SimSun",serif; font-size: 1.3rem; font-weight: 500; color: #eaf4ff; margin: 0 0 2px; }
.auth-card-sub { font-size: .8rem; color: rgba(180,210,235,.35); margin: 0; }

.auth-tabs { display: flex; gap: 22px; margin-bottom: 16px; padding-bottom: 7px; border-bottom: 1px solid rgba(255,255,255,.035); }
.auth-tabs button { background: none; border: none; font-size: .9rem; cursor: pointer; font-family: inherit; color: rgba(180,210,235,.28); padding: 1px 0; position: relative; transition: color .25s; letter-spacing: .02em; }
.auth-tabs button:hover { color: rgba(180,210,235,.55); }
.auth-tabs button.on { color: var(--c); }
.auth-tabs button.on::after { content: ''; position: absolute; bottom: -8px; left: 0; right: 0; height: 2px; background: var(--c); border-radius: 1px; }

.auth-form { display: grid; gap: 11px; }
.auth-form input {
  width: 100%; height: 46px; padding: 0 13px; font-size: .88rem; font-family: inherit; color: #dceeff;
  background: rgba(8,14,28,.48); border: 1px solid rgba(255,255,255,.045); border-radius: 10px; outline: none;
  transition: border-color .25s, box-shadow .25s;
}
.auth-form input::placeholder { color: rgba(110,150,190,.18); }
.auth-form input:focus { border-color: rgba(0,229,217,.3); box-shadow: 0 0 0 3px rgba(0,229,217,.025); }

.auth-pw-wrap { position: relative; }
.auth-pw-btn { position: absolute; right: 6px; top: 50%; transform: translateY(-50%); background: none; border: none; cursor: pointer; color: rgba(180,210,235,.16); padding: 4px; width: 26px; height: 26px; display: grid; place-items: center; transition: color .2s; }
.auth-pw-btn:hover { color: rgba(180,210,235,.35); }
.auth-pw-btn svg { width: 17px; height: 17px; }

.auth-msg { font-size: .8rem; margin: 0; padding: 9px 11px; line-height: 1.5; border-radius: 8px; }
.auth-msg.e { color: #ff9999; background: rgba(255,107,107,.04); border: 1px solid rgba(255,107,107,.09); }
.auth-msg.s { color: #7ee0a0; background: rgba(105,219,124,.04); border: 1px solid rgba(105,219,124,.09); }

.auth-btn {
  width: 100%; height: 46px; border: none; border-radius: 10px;
  font-size: .9rem; font-weight: 600; font-family: inherit; letter-spacing: .04em;
  color: #060e1a; cursor: pointer;
  background: var(--c);
  box-shadow: 0 3px 18px rgba(0,229,217,.18);
  transition: transform .15s, box-shadow .15s, filter .15s;
}
.auth-btn:hover:not(:disabled) { transform: translateY(-1px); filter: brightness(1.08); box-shadow: 0 5px 24px rgba(0,229,217,.25); }
.auth-btn:active:not(:disabled) { transform: translateY(0); }
.auth-btn:disabled { opacity: .35; cursor: not-allowed; }

.auth-dots { display: inline-flex; gap: 3px; }
.auth-dots i { width: 4px; height: 4px; background: #060e1a; border-radius: 50%; animation: db 1s ease-in-out infinite; }
.auth-dots i:nth-child(2) { animation-delay: .15s; }
.auth-dots i:nth-child(3) { animation-delay: .3s; }
@keyframes db { 0%,100% { transform: translateY(0) } 50% { transform: translateY(-4px) } }

.auth-foot { font-size: .68rem; color: rgba(120,160,200,.14); text-align: center; margin: 22px 0 0; letter-spacing: .04em; }

@keyframes fu { from { opacity: 0; transform: translateY(14px) } to { opacity: 1; transform: translateY(0) } }

@media (max-width: 1100px) {
  .auth-layout { grid-template-columns: 1fr; place-items: center; padding: 44px 20px; overflow-y: auto; align-content: start; gap: 26px; }
  .auth-hero { grid-column: 1; text-align: center; }
  .auth-desc { max-width: 100%; }
  .auth-wave { justify-content: center; }
  .auth-card { grid-column: 1; justify-self: center; animation: fu .7s .4s both; }
}
@media (max-width: 600px) {
  .auth-layout { padding: 24px 12px; gap: 18px; }
  .auth-h1 { font-size: 24px; }
  .auth-card { padding: 24px 16px; border-radius: 16px; }
  .auth-card-title { font-size: 1.1rem; }
}
@media (prefers-reduced-motion: reduce) {
  .auth.no-motion *, .auth.no-motion *::before, .auth.no-motion *::after { animation-duration: .01ms !important; animation-iteration-count: 1 !important; transition-duration: .01ms !important; }
}
</style>
