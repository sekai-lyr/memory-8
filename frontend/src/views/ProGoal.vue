<template>
  <main>
    <TopNav />
    <section class="page wide-page">
      <div class="toolbar hero-bar goal-hero">
        <div>
          <h1>把考试目标拆成每天能做的任务</h1>
          <p class="muted">设置目标日期、每日学习时间和词量后，系统会生成今日任务、周里程碑和进度诊断。</p>
        </div>
        <div class="hero-actions">
          <router-link class="button-link" to="/practice" v-if="goal?.hasGoal">执行今日任务</router-link>
          <router-link class="ghost-link" to="/pro-center">会员教练</router-link>
        </div>
      </div>

      <p class="error" v-if="error">{{ error }}</p>
      <p class="success" v-if="message">{{ message }}</p>

      <section class="panel pro-lock-panel" v-if="membership && !membership.pro">
        <div>
          <span class="badge">会员专属</span>
          <h2>目标计划需要会员</h2>
          <p class="muted">这个功能适合卖给备考用户：他们不是只想背词，而是想知道每天做什么才能赶上目标。</p>
        </div>
        <router-link class="button-link" to="/membership">升级 Pro</router-link>
      </section>

      <section class="goal-layout" v-if="membership?.pro">
        <div class="goal-main">
          <section class="panel goal-form-panel">
            <div class="toolbar">
              <div>
                <h2>{{ goal?.hasGoal ? '调整目标' : '创建目标' }}</h2>
                <p class="muted">目标越具体，系统拆出来的任务越有执行价值。</p>
              </div>
              <span class="badge pro" v-if="goal?.hasGoal">{{ goal.paceStatus }}</span>
            </div>
            <form class="goal-form" @submit.prevent="saveGoal">
              <label>
                <span>目标名称</span>
                <input v-model.trim="form.targetName" placeholder="例如：四级冲刺、考研英语、工作词汇" />
              </label>
              <label>
                <span>目标日期</span>
                <input v-model="form.targetDate" type="date" />
              </label>
              <label>
                <span>每天学习时间</span>
                <input v-model.number="form.dailyMinutes" type="number" min="5" max="240" />
              </label>
              <label>
                <span>目标词量</span>
                <input v-model.number="form.targetWordCount" type="number" min="20" max="20000" />
              </label>
              <button class="primary" type="submit" :disabled="saving">{{ saving ? '保存中...' : '保存目标计划' }}</button>
            </form>
          </section>

          <template v-if="goal?.hasGoal">
            <section class="summary-grid goal-summary">
              <div class="summary-card"><span>剩余天数</span><strong>{{ goal.daysLeft }}</strong><small>到 {{ goal.targetDate }}</small></div>
              <div class="summary-card"><span>每日建议</span><strong>{{ goal.expectedDailyWords }}</strong><small>词/天</small></div>
              <div class="summary-card"><span>已推进</span><strong>{{ goal.progressWords }}</strong><small>{{ goal.progressPercent }}%</small></div>
              <div class="summary-card"><span>7 天练习</span><strong>{{ goal.metrics.practicedSevenDays }}</strong><small>题量</small></div>
            </section>

            <section class="panel goal-progress-panel">
              <div class="toolbar">
                <div>
                  <h2>{{ goal.targetName }}</h2>
                  <p class="muted">{{ goal.recommendation }}</p>
                </div>
                <span class="badge" :class="{ pro: goal.paceStatus === '节奏领先' || goal.paceStatus === '节奏正常' }">
                  {{ goal.paceStatus }}
                </span>
              </div>
              <div class="progress goal-progress">
                <div :style="{ width: goal.progressPercent + '%' }"></div>
              </div>
            </section>

            <section class="panel">
              <div class="toolbar">
                <h2>今日任务</h2>
                <span class="muted">按目标压力和薄弱点拆分</span>
              </div>
              <div class="mission-grid">
                <router-link v-for="mission in goal.todayMissions" :key="mission.title" class="mission-card" :to="mission.route">
                  <span>{{ mission.targetCount }} 项</span>
                  <strong>{{ mission.title }}</strong>
                  <p>{{ mission.detail }}</p>
                </router-link>
              </div>
            </section>

            <section class="panel">
              <div class="toolbar">
                <h2>周里程碑</h2>
                <span class="muted">最多展示 6 周</span>
              </div>
              <div class="milestone-grid">
                <article v-for="item in goal.weeklyMilestones" :key="item.weekLabel" class="milestone-card">
                  <span>{{ item.weekLabel }}</span>
                  <strong>{{ item.targetWords }} 词</strong>
                  <p>{{ item.focus }}</p>
                </article>
              </div>
            </section>
          </template>
        </div>

        <aside class="goal-side">
          <section class="panel side-section">
            <h2>目标诊断</h2>
            <div class="status-grid">
              <span>到期复习：{{ goal?.metrics?.dueReviews || 0 }}</span>
              <span>薄弱词：{{ goal?.metrics?.weakWords || 0 }}</span>
              <span>学习中：{{ goal?.metrics?.learningWords || 0 }}</span>
              <span>已掌握：{{ goal?.metrics?.masteredWords || 0 }}</span>
              <span>自定义词：{{ goal?.metrics?.customWords || 0 }}</span>
              <span>可用词库：{{ goal?.metrics?.availableWords || 0 }}</span>
            </div>
          </section>

          <section class="panel side-section">
            <h2>实用付费点</h2>
            <div class="privilege-list vertical-pills">
              <span>目标日期倒推</span>
              <span>每日词量自动拆分</span>
              <span>复习积压提醒</span>
              <span>薄弱词优先</span>
              <span>周里程碑复盘</span>
            </div>
          </section>

          <section class="panel side-section">
            <h2>建议用法</h2>
            <p class="muted">先导入自己的目标词表，再设置考试日期。每天按“今日任务”完成后去学习记录看热力图。</p>
          </section>
        </aside>
      </section>
    </section>
  </main>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { getMembership, getProGoal, saveProGoal } from '../api'
import TopNav from './components/TopNav.vue'

const membership = ref(null)
const goal = ref(null)
const error = ref('')
const message = ref('')
const saving = ref(false)
const form = reactive({
  targetName: '考试冲刺',
  targetDate: defaultDate(),
  dailyMinutes: 30,
  targetWordCount: 300
})

onMounted(load)

async function load() {
  error.value = ''
  try {
    membership.value = await getMembership()
    if (membership.value.pro) {
      goal.value = await getProGoal()
      fillForm(goal.value)
    }
  } catch (e) {
    error.value = e.message
  }
}

async function saveGoal() {
  error.value = ''
  message.value = ''
  if (!form.targetName || !form.targetDate) {
    error.value = '请填写目标名称和目标日期'
    return
  }
  saving.value = true
  try {
    goal.value = await saveProGoal({ ...form })
    fillForm(goal.value)
    message.value = '会员目标计划已保存'
  } catch (e) {
    error.value = e.message
  } finally {
    saving.value = false
  }
}

function fillForm(data) {
  if (!data?.hasGoal) return
  form.targetName = data.targetName
  form.targetDate = data.targetDate
  form.dailyMinutes = data.dailyMinutes
  form.targetWordCount = data.targetWordCount
}

function defaultDate() {
  const date = new Date()
  date.setDate(date.getDate() + 30)
  return date.toISOString().slice(0, 10)
}
</script>
