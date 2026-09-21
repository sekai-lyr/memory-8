<template>
  <main>
    <TopNav />
    <section class="page wide-page">
      <div class="toolbar hero-bar pro-hero">
        <div>
          <h1>你的专属复习报告</h1>
          <p class="muted">{{ heroText }}</p>
        </div>
        <div class="hero-actions">
          <router-link class="button-link" to="/practice">{{ report?.nextAction || '开始练习' }}</router-link>
          <router-link class="ghost-link" to="/study-records">查看记录</router-link>
        </div>
      </div>

      <p class="error" v-if="error">{{ error }}</p>

      <section class="panel pro-lock-panel" v-if="membership && !membership.pro">
        <div>
          <span class="badge">会员专属</span>
          <h2>升级后打开学习诊断、7 天计划和薄弱词优先级</h2>
          <p class="muted">这部分是会员最核心的价值：用户每次打开都能看到下一步该学什么，而不是自己猜。</p>
        </div>
        <router-link class="button-link" to="/membership">开通会员</router-link>
      </section>

      <template v-if="membership?.pro && report">
        <section class="summary-grid pro-metrics">
          <div class="summary-card score-card">
            <span>记忆稳定度</span>
            <strong>{{ report.retentionScore }}</strong>
            <small>{{ report.levelName }}</small>
          </div>
          <div class="summary-card"><span>今日目标</span><strong>{{ report.dailyTarget }}</strong><small>建议练习词数</small></div>
          <div class="summary-card"><span>到期复习</span><strong>{{ report.metrics.dueReviews }}</strong><small>优先处理</small></div>
          <div class="summary-card"><span>7 天题量</span><strong>{{ report.metrics.sevenDayTotal }}</strong><small>正确率 {{ report.metrics.averageAccuracy }}%</small></div>
        </section>

        <section class="pro-dashboard-layout">
          <div class="pro-main">
            <section class="panel pro-focus-panel">
              <div class="toolbar">
                <div>
                  <h2>{{ report.focusTitle }}</h2>
                  <p class="muted">{{ report.focusDetail }}</p>
                </div>
                <span class="badge pro">{{ modeLabel(report.recommendedMode) }}</span>
              </div>
              <div class="pro-action-strip">
                <router-link class="button-link" to="/practice">{{ report.nextAction }}</router-link>
                <router-link class="ghost-link" to="/pro-review">复习中心</router-link>
                <router-link class="ghost-link" to="/pro-reading">文章解析</router-link>
                <router-link class="ghost-link" to="/daily-plan">今日清单</router-link>
                <router-link class="ghost-link" to="/pro-goal">目标计划</router-link>
                <router-link class="ghost-link" to="/pro-exam">模考冲刺</router-link>
                <router-link class="ghost-link" to="/pro-dictation">听写训练</router-link>
                <router-link class="ghost-link" to="/user-words">整理词库</router-link>
                <router-link class="ghost-link" to="/study-records">看热力图</router-link>
              </div>
            </section>

            <section class="panel">
              <div class="toolbar">
                <h2>7 天复习计划</h2>
                <span class="muted">按当前薄弱点生成</span>
              </div>
              <div class="plan-timeline">
                <article v-for="day in report.weeklyPlan" :key="day.dayLabel" class="plan-step">
                  <span>{{ day.dayLabel }}</span>
                  <strong>{{ day.title }}</strong>
                  <small>{{ day.targetCount }} 词</small>
                  <p>{{ day.focus }}</p>
                </article>
              </div>
            </section>
          </div>

          <aside class="pro-side">
            <section class="panel side-section">
              <h2>薄弱词优先级</h2>
              <p class="empty" v-if="!report.weakWords.length">暂时没有错题，先做一轮练习让系统积累诊断数据。</p>
              <ul class="weak-list" v-else>
                <li v-for="word in report.weakWords" :key="`${word.wordType}-${word.wordId}`">
                  <div>
                    <strong>{{ word.english }}</strong>
                    <span>{{ word.chinese }}</span>
                  </div>
                  <small>错 {{ word.wrongCount }} / 不会 {{ word.unknownCount }}</small>
                </li>
              </ul>
            </section>

            <section class="panel side-section">
              <h2>会员数据</h2>
              <div class="status-grid">
                <span>高频错误：{{ report.metrics.highWrong }}</span>
                <span>不认识：{{ report.metrics.unknown }}</span>
                <span>学习中：{{ report.metrics.learning }}</span>
                <span>已掌握：{{ report.metrics.mastered }}</span>
                <span>错题本：{{ report.metrics.wrongWords }}</span>
                <span>自定义词：{{ report.metrics.customWords }}</span>
              </div>
            </section>

            <section class="panel side-section">
              <h2>已解锁权益</h2>
              <div class="privilege-list">
                <span v-for="item in report.privileges" :key="item">{{ item }}</span>
              </div>
            </section>
          </aside>
        </section>
      </template>
    </section>
  </main>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { getMembership, getProDashboard } from '../api'
import TopNav from './components/TopNav.vue'

const membership = ref(null)
const report = ref(null)
const error = ref('')

const heroText = computed(() => {
  if (!membership.value) return '正在读取你的会员状态和学习数据。'
  if (!membership.value.pro) return '开通会员后可以看到复习优先级、薄弱词和 7 天学习计划。'
  if (!report.value) return '正在生成你的会员学习报告。'
  return `${report.value.focusTitle}，今日建议完成 ${report.value.dailyTarget} 个词。`
})

onMounted(load)

async function load() {
  error.value = ''
  try {
    membership.value = await getMembership()
    if (membership.value.pro) {
      report.value = await getProDashboard()
    }
  } catch (e) {
    error.value = e.message
  }
}

function modeLabel(mode) {
  const labels = {
    smart: '智能复习',
    wrong: '错题复盘',
    mixed: '混合练习'
  }
  return labels[mode] || '会员练习'
}
</script>
