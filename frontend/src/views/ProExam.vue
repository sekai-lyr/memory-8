<template>
  <main>
    <TopNav />
    <section class="page wide-page">
      <div class="toolbar hero-bar exam-hero">
        <div>
          <h1>像考试一样检测词汇掌握度</h1>
          <p class="muted">自动抽取到期复习、薄弱词和自定义词，生成限时选择题，提交后给分数和复盘建议。</p>
        </div>
        <div class="hero-actions">
          <button class="primary" @click="startPaper(roundSize)" :disabled="loading">
            {{ loading ? '组卷中...' : '生成试卷' }}
          </button>
          <router-link class="ghost-link" to="/pro-goal">目标计划</router-link>
        </div>
      </div>

      <p class="error" v-if="error">{{ error }}</p>

      <section class="panel pro-lock-panel" v-if="membership && !membership.pro">
        <div>
          <span class="badge">会员专属</span>
          <h2>模考冲刺需要会员</h2>
          <p class="muted">这个模块适合备考用户付费：按真实答题压力检测薄弱点，错题会自动回流复习队列。</p>
        </div>
        <router-link class="button-link" to="/membership">升级 Pro</router-link>
      </section>

      <section class="exam-layout" v-if="membership?.pro">
        <div class="exam-main">
          <section class="panel control-panel" v-if="!report">
            <div class="toolbar">
              <h2>试卷设置</h2>
              <span class="badge pro">{{ paper?.paperId || '未组卷' }}</span>
            </div>
            <div class="round-controls">
              <button :class="{ active: roundSize === 10 }" @click="startPaper(10)">10 题</button>
              <button :class="{ active: roundSize === 20 }" @click="startPaper(20)">20 题</button>
              <button :class="{ active: roundSize === 30 }" @click="startPaper(30)">30 题</button>
              <input class="small-input" v-model.number="customSize" type="number" min="5" max="60" placeholder="自定义" />
              <button class="primary" @click="startPaper(customSize || roundSize)">重新组卷</button>
            </div>
          </section>

          <section class="panel exam-paper-panel" v-if="paper && !report">
            <div class="toolbar">
              <div>
                <h2>{{ currentQuestion?.english || '等待组卷' }}</h2>
                <p class="muted">选择最接近的中文释义</p>
              </div>
              <div class="exam-timer" :class="{ urgent: remainingSeconds < 60 }">{{ timeText }}</div>
            </div>

            <div class="exam-meta-row">
              <span class="badge">{{ currentIndex + 1 }}/{{ paper.questions.length }}</span>
              <span v-if="currentQuestion?.sourceReason">{{ currentQuestion.sourceReason }}</span>
              <span v-if="currentQuestion?.status">{{ statusName(currentQuestion.status) }}</span>
              <span v-if="currentQuestion?.wrongCount">错 {{ currentQuestion.wrongCount }} 次</span>
            </div>

            <div class="option-grid">
              <button
                v-for="option in currentQuestion?.options || []"
                :key="option"
                :class="{ active: answers[questionKey(currentQuestion)] === option }"
                @click="selectAnswer(option)"
              >
                {{ option }}
              </button>
            </div>

            <div class="exam-actions">
              <button class="ghost" @click="prevQuestion" :disabled="currentIndex === 0">上一题</button>
              <button @click="nextQuestion" :disabled="currentIndex >= paper.questions.length - 1">下一题</button>
              <button class="primary" @click="submitPaper" :disabled="submitting">{{ submitting ? '提交中...' : '提交试卷' }}</button>
            </div>
          </section>

          <section class="panel exam-report-panel" v-if="report">
            <div class="toolbar">
              <div>
                <h2>{{ report.levelName }}</h2>
                <p class="muted">{{ report.recommendation }}</p>
              </div>
              <strong class="exam-score">{{ report.score }}</strong>
            </div>
            <div class="summary-grid exam-summary">
              <div class="summary-card"><span>总题数</span><strong>{{ report.totalCount }}</strong></div>
              <div class="summary-card"><span>正确</span><strong>{{ report.correctCount }}</strong></div>
              <div class="summary-card"><span>错误</span><strong>{{ report.wrongCount }}</strong></div>
              <div class="summary-card"><span>正确率</span><strong>{{ report.accuracy }}%</strong></div>
            </div>
            <div class="exam-actions">
              <button class="primary" @click="startPaper(roundSize)">再考一轮</button>
              <router-link class="ghost-link" to="/pro-dictation">错题听写</router-link>
              <router-link class="ghost-link" to="/study-records">看学习记录</router-link>
            </div>
          </section>

          <section class="panel" v-if="report">
            <div class="toolbar">
              <h2>错题复盘</h2>
              <span class="muted">已自动回流错题本</span>
            </div>
            <ul class="exam-result-list">
              <li v-for="item in report.results" :key="`${item.wordType}-${item.wordId}`" :class="{ wrong: !item.correct }">
                <div>
                  <strong>{{ item.english }}</strong>
                  <span>你的答案：{{ item.selectedAnswer || '未作答' }}</span>
                  <span>正确答案：{{ item.correctAnswer }}</span>
                  <p>{{ item.diagnosis }}</p>
                </div>
                <span class="badge" :class="{ pro: item.correct }">{{ item.correct ? '正确' : '错误' }}</span>
              </li>
            </ul>
          </section>
        </div>

        <aside class="exam-side">
          <section class="panel side-section">
            <h2>答题进度</h2>
            <div class="progress"><div :style="{ width: progress + '%' }"></div></div>
            <div class="mini-stats">
              <span>已答 <strong>{{ answeredCount }}</strong></span>
              <span>总题 <strong>{{ paper?.questions?.length || 0 }}</strong></span>
              <span>剩余 <strong>{{ Math.max(0, (paper?.questions?.length || 0) - answeredCount) }}</strong></span>
              <span>限时 <strong>{{ paper?.durationMinutes || 0 }}</strong></span>
            </div>
          </section>

          <section class="panel side-section" v-if="paper && !report">
            <h2>题号</h2>
            <div class="question-map">
              <button
                v-for="(question, index) in paper.questions"
                :key="questionKey(question)"
                :class="{ active: index === currentIndex, done: answers[questionKey(question)] }"
                @click="currentIndex = index"
              >
                {{ index + 1 }}
              </button>
            </div>
          </section>

          <section class="panel side-section">
            <h2>模考规则</h2>
            <div class="privilege-list vertical-pills">
              <span v-for="rule in paper?.rules || defaultRules" :key="rule">{{ rule }}</span>
            </div>
          </section>

          <section class="panel side-section" v-if="report?.weakPoints?.length">
            <h2>薄弱词</h2>
            <div class="privilege-list vertical-pills danger-pills">
              <span v-for="item in report.weakPoints" :key="item">{{ item }}</span>
            </div>
          </section>
        </aside>
      </section>
    </section>
  </main>
</template>

<script setup>
import { computed, onMounted, onUnmounted, ref } from 'vue'
import { getMembership, getProExamPaper, submitProExam } from '../api'
import TopNav from './components/TopNav.vue'

const membership = ref(null)
const paper = ref(null)
const report = ref(null)
const answers = ref({})
const currentIndex = ref(0)
const roundSize = ref(20)
const customSize = ref(null)
const loading = ref(false)
const submitting = ref(false)
const error = ref('')
const remainingSeconds = ref(0)
let timer = null

const defaultRules = ['优先抽取薄弱词', '限时完成', '错题自动回流']
const currentQuestion = computed(() => paper.value?.questions?.[currentIndex.value] || null)
const answeredCount = computed(() => Object.keys(answers.value).length)
const progress = computed(() => paper.value?.questions?.length
  ? Math.round((answeredCount.value / paper.value.questions.length) * 100)
  : 0)
const timeText = computed(() => {
  const minutes = Math.floor(remainingSeconds.value / 60)
  const seconds = remainingSeconds.value % 60
  return `${String(minutes).padStart(2, '0')}:${String(seconds).padStart(2, '0')}`
})

onMounted(load)
onUnmounted(stopTimer)

async function load() {
  error.value = ''
  try {
    membership.value = await getMembership()
    if (membership.value.pro) {
      await startPaper(roundSize.value)
    }
  } catch (e) {
    error.value = e.message
  }
}

async function startPaper(size) {
  if (!membership.value?.pro) return
  loading.value = true
  error.value = ''
  report.value = null
  answers.value = {}
  currentIndex.value = 0
  roundSize.value = Math.max(5, Math.min(60, Number(size) || 20))
  try {
    paper.value = await getProExamPaper(roundSize.value)
    remainingSeconds.value = (paper.value.durationMinutes || 10) * 60
    startTimer()
  } catch (e) {
    error.value = e.message
  } finally {
    loading.value = false
  }
}

function startTimer() {
  stopTimer()
  timer = setInterval(() => {
    if (remainingSeconds.value <= 0) {
      stopTimer()
      submitPaper()
      return
    }
    remainingSeconds.value--
  }, 1000)
}

function stopTimer() {
  if (timer) clearInterval(timer)
  timer = null
}

function questionKey(question) {
  return question ? `${question.wordType}:${question.wordId}` : ''
}

function selectAnswer(option) {
  if (!currentQuestion.value) return
  answers.value = { ...answers.value, [questionKey(currentQuestion.value)]: option }
}

function prevQuestion() {
  currentIndex.value = Math.max(0, currentIndex.value - 1)
}

function nextQuestion() {
  currentIndex.value = Math.min((paper.value?.questions?.length || 1) - 1, currentIndex.value + 1)
}

async function submitPaper() {
  if (!paper.value || submitting.value) return
  submitting.value = true
  error.value = ''
  stopTimer()
  try {
    report.value = await submitProExam({
      durationSeconds: (paper.value.durationMinutes * 60) - remainingSeconds.value,
      answers: paper.value.questions.map(question => ({
        wordId: question.wordId,
        wordType: question.wordType,
        selectedAnswer: answers.value[questionKey(question)] || ''
      }))
    })
  } catch (e) {
    error.value = e.message
  } finally {
    submitting.value = false
  }
}

function statusName(status) {
  return { NEW: '未学习', LEARNING: '学习中', UNKNOWN: '不认识', HIGH_WRONG: '高频错误', MASTERED: '已掌握' }[status] || status
}
</script>
