<template>
  <main>
    <TopNav />
    <section class="page">
      <div class="toolbar hero-bar coach-hero">
        <div>
          <span class="eyebrow">{{ membership?.pro ? '会员中文模式' : '中文模式' }}</span>
          <h1>今天该怎么背？</h1>
          <p class="muted">{{ coachHeadline }}</p>
        </div>
        <div class="hero-actions">
          <router-link class="button-link" :to="primaryAction.to">{{ primaryAction.label }}</router-link>
          <router-link class="ghost-link" to="/daily-plan">今日清单</router-link>
          <router-link class="ghost-link" to="/guide">看功能说明</router-link>
          <button class="ghost" @click="logout">退出登录</button>
        </div>
      </div>

      <p class="error" v-if="error">{{ error }}</p>

      <section class="summary-grid">
        <div class="summary-card"><span>错题</span><strong>{{ counts.wrong }}</strong></div>
        <div class="summary-card"><span>收藏</span><strong>{{ counts.favorite }}</strong></div>
        <div class="summary-card"><span>自定义词</span><strong>{{ counts.userWord }}</strong></div>
        <div class="summary-card"><span>练习轮次</span><strong>{{ counts.record }}</strong></div>
      </section>

      <section class="coach-layout">
        <section class="panel coach-panel">
          <div class="toolbar">
            <h2>今天按这个顺序做</h2>
            <span class="badge">{{ dailyTarget }} 词</span>
          </div>
          <div class="task-stack">
            <router-link v-for="task in coachTasks" :key="task.title" class="task-item" :to="task.to">
              <span>{{ task.kicker }}</span>
              <strong>{{ task.title }}</strong>
              <small>{{ task.detail }}</small>
            </router-link>
          </div>
        </section>

        <section class="panel coach-panel">
          <div class="toolbar">
            <h2>系统给你的建议</h2>
            <span class="badge" :class="{ pro: membership?.pro }">{{ membership?.pro ? '已开通会员' : '普通用户' }}</span>
          </div>
          <div class="coach-copy">
            <p>{{ coachAdvice }}</p>
            <p class="muted">{{ proAdvice }}</p>
          </div>
        </section>
      </section>

      <section class="panel guide-panel">
        <div class="toolbar">
          <div>
            <h2>看不懂功能？先看这个</h2>
            <p class="muted">如果你是第一次用，只需要记住下面 4 个入口。高级会员功能等熟悉后再用。</p>
          </div>
          <router-link class="button-link" to="/guide">完整说明</router-link>
        </div>
        <div class="quick-guide-grid">
          <router-link class="quick-guide-item" to="/practice">
            <span>最常用</span>
            <strong>开始背词</strong>
            <p>每天打开先点这里，系统出题，你输入中文意思。</p>
          </router-link>
          <router-link class="quick-guide-item" to="/wrong-words">
            <span>提分快</span>
            <strong>错题本</strong>
            <p>答错的词都在这里，先复习错题比刷新词更有用。</p>
          </router-link>
          <router-link class="quick-guide-item" to="/user-words">
            <span>背自己的</span>
            <strong>我的词库</strong>
            <p>把考试、课堂、工作里遇到的词加进来。</p>
          </router-link>
          <router-link class="quick-guide-item" to="/membership">
            <span>付费功能</span>
            <strong>开通会员</strong>
            <p>想用目标计划、听写训练、模考冲刺，就在这里开通。</p>
          </router-link>
        </div>
      </section>

      <section class="flow-grid product-grid">
        <router-link class="flow-item" to="/daily-plan"><strong>今日清单</strong><span>会员专用：每天自动列出该做的任务，做完可以打勾。</span></router-link>
        <router-link class="flow-item" to="/practice"><strong>开始背词</strong><span>最基础入口：看英文，输入中文意思，错了自动进错题本。</span></router-link>
        <router-link class="flow-item" to="/public-words"><strong>公共词库</strong><span>想查单词、看例句、听发音时用。</span></router-link>
        <router-link class="flow-item" to="/user-words"><strong>我的词库</strong><span>把你自己的考试词、课堂词、工作词加进来。会员可以批量导入表格。</span></router-link>
        <router-link class="flow-item" to="/study-records"><strong>学习记录</strong><span>看今天背了多少、正确率多少、有没有坚持。</span></router-link>
        <router-link class="flow-item" to="/pro-center"><strong>会员学习教练</strong><span>会员专用：系统告诉你今天最该复习什么。</span></router-link>
        <router-link class="flow-item" to="/pro-review"><strong>智能复习中心</strong><span>会员专用：按遗忘时间、错题和未来 7 天压力安排复习。</span></router-link>
        <router-link class="flow-item" to="/pro-reading"><strong>文章生词解析</strong><span>会员专用：粘贴阅读材料，自动提取生词并生成学习包。</span></router-link>
        <router-link class="flow-item" to="/pro-goal"><strong>目标计划</strong><span>会员专用：有考试日期时，它会倒推每天背多少。</span></router-link>
        <router-link class="flow-item" to="/pro-dictation"><strong>听写训练</strong><span>会员专用：只听发音，拼出英文，适合练拼写。</span></router-link>
        <router-link class="flow-item" to="/pro-exam"><strong>模考冲刺</strong><span>会员专用：像考试一样限时做题，提交后给分数和错题分析。</span></router-link>
        <router-link class="flow-item" to="/membership"><strong>开通会员</strong><span>输入激活码，开通目标计划、听写、模考和学习报告。</span></router-link>
        <router-link class="flow-item" to="/admin-monetization"><strong>变现后台</strong><span>给你自己用：生成激活码、查看兑换次数和收入估算。</span></router-link>
      </section>
    </section>
  </main>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import {
  getLearningStatusSummary,
  getMembership,
  listFavoriteWords,
  listStudyRecords,
  listUserWords,
  listWrongWords
} from '../api'
import TopNav from './components/TopNav.vue'

const router = useRouter()
const counts = reactive({ wrong: 0, favorite: 0, userWord: 0, record: 0 })
const statusSummary = ref({})
const membership = ref(null)
const error = ref('')

const dailyTarget = computed(() => Math.min(30, Math.max(10, counts.wrong * 2 + 10)))
const coachHeadline = computed(() => {
  if (counts.wrong > 0) return `你现在有 ${counts.wrong} 个错题，先复盘这些词，比盲目刷词更有效。`
  if (counts.userWord > 0) return `你的自定义词库已有 ${counts.userWord} 个词，适合做一轮定向练习。`
  return '从 10 个高频词开始，先建立今天的学习手感。'
})
const primaryAction = computed(() => counts.wrong > 0
  ? { label: '开始错题复习', to: '/practice' }
  : { label: '开始今日练习', to: '/practice' })
const coachTasks = computed(() => [
  {
    kicker: '第 1 步',
    title: counts.wrong > 0 ? '先复习错题' : '先背 10 个词',
    detail: counts.wrong > 0 ? '先消灭最近暴露的薄弱词。' : '让系统记录你的第一轮状态。',
    to: '/practice'
  },
  {
    kicker: '第 2 步',
    title: membership.value?.pro ? '看会员推荐' : '整理自己的词',
    detail: membership.value?.pro ? '系统会按错误次数和复习时间排序。' : '把考试、课堂、工作里遇到的词加进来。',
    to: membership.value?.pro ? '/practice' : '/user-words'
  },
  {
    kicker: '第 3 步',
    title: '查看学习记录',
    detail: '用完成轮次和正确率判断是否需要降低词量。',
    to: '/study-records'
  }
])
const coachAdvice = computed(() => {
  const due = statusSummary.value.DUE || 0
  if (due > 0) return `有 ${due} 个词到了复习时间，今天不要只学新词。`
  if (counts.record === 0) return '还没有练习记录，先跑一轮小测试，系统才有依据给你安排任务。'
  if (counts.wrong > counts.favorite) return '错题多于收藏词，说明当前重点应放在纠错而不是扩词。'
  return '目前节奏不错，可以增加少量新词，同时保留错题复盘。'
})
const proAdvice = computed(() => membership.value?.pro
  ? '会员已开启，建议优先使用目标计划、模考冲刺和学习报告。'
  : '如果要卖给用户，会员的核心卖点应该是省时间：目标计划、模考、听写、学习分析。')

onMounted(load)

async function load() {
  const [wrong, favorite, userWords, records, member, summary] = await Promise.allSettled([
    listWrongWords(),
    listFavoriteWords(),
    listUserWords(''),
    listStudyRecords(),
    getMembership(),
    getLearningStatusSummary()
  ])
  counts.wrong = wrong.status === 'fulfilled' ? wrong.value.length : 0
  counts.favorite = favorite.status === 'fulfilled' ? favorite.value.length : 0
  counts.userWord = userWords.status === 'fulfilled' ? userWords.value.length : 0
  counts.record = records.status === 'fulfilled' ? records.value.length : 0
  membership.value = member.status === 'fulfilled' ? member.value : null
  statusSummary.value = summary.status === 'fulfilled' ? summary.value : {}
  const failed = [wrong, favorite, userWords, records].find(item => item.status === 'rejected')
  error.value = failed ? failed.reason.message : ''
}

function logout() {
  localStorage.removeItem('user')
  localStorage.removeItem('userId')
  localStorage.removeItem('username')
  localStorage.removeItem('token')
  router.push('/login')
}
</script>
