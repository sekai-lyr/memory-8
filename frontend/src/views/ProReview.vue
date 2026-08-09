<template>
  <main>
    <TopNav />
    <section class="page wide-page">
      <div class="toolbar hero-bar pro-review-hero">
        <div>
          <span class="eyebrow">会员智能复习中心</span>
          <h1>今天先复习哪些词，系统直接排好</h1>
          <p class="muted">{{ heroText }}</p>
        </div>
        <div class="hero-actions">
          <router-link class="button-link" to="/practice">{{ review?.nextAction || '开始练习' }}</router-link>
          <router-link class="ghost-link" to="/pro-center">学习教练</router-link>
          <router-link class="ghost-link" to="/daily-plan">今日清单</router-link>
        </div>
      </div>

      <p class="error" v-if="error">{{ error }}</p>

      <section class="panel pro-lock-panel" v-if="membership && !membership.pro">
        <div>
          <span class="badge">会员专属</span>
          <h2>升级后解锁智能复习中心</h2>
          <p class="muted">它会按遗忘时间、错误次数、不认识次数和未来 7 天压力排出复习顺序，比普通刷词更省时间。</p>
        </div>
        <router-link class="button-link" to="/membership">开通会员</router-link>
      </section>

      <template v-if="membership?.pro && review">
        <section class="summary-grid pro-metrics">
          <div class="summary-card score-card">
            <span>建议词量</span>
            <strong>{{ review.recommendedCount }}</strong>
            <small>预计 {{ review.metrics.estimatedMinutes }} 分钟</small>
          </div>
          <div class="summary-card"><span>今天到期</span><strong>{{ review.metrics.dueReviews }}</strong><small>逾期 {{ review.metrics.overdueReviews }}</small></div>
          <div class="summary-card"><span>薄弱词</span><strong>{{ review.metrics.weakWords }}</strong><small>高频错 {{ review.metrics.highWrong }}</small></div>
          <div class="summary-card"><span>未来 7 天</span><strong>{{ review.metrics.futureSevenDayReviews }}</strong><small>即将回流</small></div>
        </section>

        <section class="review-layout">
          <div class="review-main">
            <section class="panel pro-focus-panel">
              <div class="toolbar">
                <div>
                  <h2>{{ review.focusTitle }}</h2>
                  <p class="muted">{{ review.focusDetail }}</p>
                </div>
                <span class="badge pro">{{ modeLabel(review.recommendedMode) }}</span>
              </div>
              <div class="pro-action-strip">
                <router-link class="button-link" to="/practice">{{ review.nextAction }}</router-link>
                <router-link class="ghost-link" to="/pro-dictation">听写巩固</router-link>
                <router-link class="ghost-link" to="/wrong-words">错题本</router-link>
                <router-link class="ghost-link" to="/pro-exam">模考冲刺</router-link>
              </div>
            </section>

            <section class="review-bucket-grid">
              <article v-for="bucket in review.buckets" :key="bucket.key" class="review-bucket">
                <span>{{ bucket.level }}</span>
                <strong>{{ bucket.title }}</strong>
                <b>{{ bucket.count }}</b>
                <p>{{ bucket.detail }}</p>
                <router-link :to="bucket.route">去处理</router-link>
              </article>
            </section>

            <section class="panel">
              <div class="toolbar">
                <h2>未来 7 天复习压力</h2>
                <span class="muted">提前知道哪天会堆词</span>
              </div>
              <div class="review-calendar">
                <article v-for="day in review.upcomingDays" :key="day.date" class="review-day">
                  <span>{{ day.dayLabel }}</span>
                  <strong>{{ day.dueCount }} 词</strong>
                  <small>{{ day.loadLevel }}</small>
                  <p>{{ day.suggestion }}</p>
                </article>
              </div>
            </section>

            <section class="panel">
              <div class="toolbar">
                <h2>今天优先处理的词</h2>
                <span class="muted">按优先级排序，最多展示 12 个</span>
              </div>
              <p class="empty" v-if="!review.priorityWords.length">还没有复习数据，先去完成一轮练习。</p>
              <ul class="priority-word-list" v-else>
                <li v-for="word in review.priorityWords" :key="`${word.wordType}-${word.wordId}`">
                  <div>
                    <strong>{{ word.english }}</strong>
                    <span>{{ statusLabel(word.status) }} · {{ word.reason }}</span>
                  </div>
                  <div class="priority-word-meta">
                    <b>{{ word.priorityScore }}</b>
                    <small>错 {{ word.wrongCount }} / 不会 {{ word.unknownCount }}</small>
                    <small>{{ formatTime(word.nextReviewTime) }}</small>
                  </div>
                </li>
              </ul>
            </section>
          </div>

          <aside class="review-side">
            <section class="panel side-section">
              <h2>复习资产</h2>
              <div class="status-grid">
                <span>跟踪词：{{ review.metrics.totalTracked }}</span>
                <span>学习中：{{ review.metrics.learning }}</span>
                <span>不认识：{{ review.metrics.unknown }}</span>
                <span>已掌握：{{ review.metrics.mastered }}</span>
              </div>
            </section>

            <section class="panel side-section">
              <h2>为什么这是会员功能</h2>
              <div class="privilege-list vertical-pills">
                <span v-for="item in review.vipReasons" :key="item">{{ item }}</span>
              </div>
            </section>

            <section class="panel side-section">
              <h2>实用卖点</h2>
              <p class="muted">用户不是缺词库，而是缺每天该做什么。这个页面把“判断成本”变成会员价值，适合放在付费权益里。</p>
            </section>
          </aside>
        </section>
      </template>
    </section>
  </main>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { getMembership, getProReview } from '../api'
import TopNav from './components/TopNav.vue'

const membership = ref(null)
const review = ref(null)
const error = ref('')

const heroText = computed(() => {
  if (!membership.value) return '正在读取会员状态。'
  if (!membership.value.pro) return '普通用户可以背词，会员可以看到系统排好的复习优先级。'
  if (!review.value) return '正在计算你的复习压力和薄弱词。'
  return `${review.value.focusTitle}，建议本轮完成 ${review.value.recommendedCount} 个词。`
})

onMounted(load)

async function load() {
  error.value = ''
  try {
    membership.value = await getMembership()
    if (membership.value.pro) {
      review.value = await getProReview()
    }
  } catch (e) {
    error.value = e.message
  }
}

function modeLabel(mode) {
  const labels = {
    smart: '智能复习',
    dictation: '听写巩固',
    mixed: '混合练习'
  }
  return labels[mode] || '会员复习'
}

function statusLabel(status) {
  const labels = {
    HIGH_WRONG: '高频错误',
    UNKNOWN: '不认识',
    LEARNING: '学习中',
    MASTERED: '已掌握',
    NEW: '新词'
  }
  return labels[status] || status || '未记录'
}

function formatTime(value) {
  if (!value) return '随时复习'
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return '随时复习'
  return `${date.getMonth() + 1}/${date.getDate()} ${String(date.getHours()).padStart(2, '0')}:00`
}
</script>
