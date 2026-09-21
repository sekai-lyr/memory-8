<template>
  <main>
    <TopNav />
    <section class="page wide-page">
      <div class="toolbar hero-bar dictation-hero">
        <div>
          <h1>只听发音，拼出单词</h1>
          <p class="muted">系统优先抽取到期复习、薄弱词和自定义词，训练听音辨词与拼写记忆。</p>
        </div>
        <div class="hero-actions">
          <button class="primary" @click="startRound(roundSize)" :disabled="loading">
            {{ loading ? '加载中...' : '开始听写' }}
          </button>
          <router-link class="ghost-link" to="/pro-center">返回 Pro 教练</router-link>
        </div>
      </div>

      <p class="error" v-if="error">{{ error }}</p>

      <section class="panel pro-lock-panel" v-if="membership && !membership.pro">
        <div>
          <span class="badge">会员专属</span>
          <h2>听写训练需要会员</h2>
          <p class="muted">这是一个更容易卖出差异化的 VIP 模块：用户不是看词选答案，而是听音拼写。</p>
        </div>
        <router-link class="button-link" to="/membership">升级 Pro</router-link>
      </section>

      <section class="dictation-layout" v-if="membership?.pro">
        <div class="dictation-main">
          <section class="panel control-panel">
            <div class="toolbar">
              <h2>训练轮次</h2>
              <span class="badge pro">{{ roundSize }} 词</span>
            </div>
            <div class="round-controls">
              <button :class="{ active: roundSize === 8 }" @click="startRound(8)">8 个</button>
              <button :class="{ active: roundSize === 12 }" @click="startRound(12)">12 个</button>
              <button :class="{ active: roundSize === 20 }" @click="startRound(20)">20 个</button>
              <input class="small-input" v-model.number="customSize" type="number" min="5" max="50" placeholder="自定义" />
              <button class="primary" @click="startRound(customSize || roundSize)">重新生成</button>
            </div>
          </section>

          <section class="panel dictation-card-panel">
            <template v-if="!finished">
              <div class="dictation-card">
                <span class="badge">{{ currentIndex + 1 }}/{{ words.length || roundSize }}</span>
                <div class="sound-mark">听</div>
                <p class="muted">单词已隐藏，请先听发音再输入英文拼写。</p>
                <div class="listen-row">
                  <button @click="playCurrent" :disabled="!currentWord">播放发音</button>
                  <button class="ghost" @click="revealAnswer" :disabled="!currentWord || submitted">直接看答案</button>
                </div>
              </div>

              <form class="dictation-answer" @submit.prevent="submitAnswer">
                <input
                  v-model.trim="answer"
                  :disabled="!currentWord || submitted"
                  placeholder="输入听到的英文单词"
                  autocomplete="off"
                />
                <button class="primary" :disabled="!currentWord || submitted">提交</button>
              </form>

              <div class="dictation-result" v-if="submitted">
                <p :class="result.correct ? 'success' : 'error'">{{ result.message }}</p>
                <div class="detail-grid" v-if="wordDetail">
                  <div>
                    <strong>{{ wordDetail.word.english }}</strong>
                    <p>{{ wordDetail.word.chinese }}</p>
                    <small>{{ wordDetail.word.phonetic }} {{ wordDetail.word.category }} {{ wordDetail.word.difficulty }}</small>
                    <p v-if="wordDetail.word.exampleSentence">{{ wordDetail.word.exampleSentence }}</p>
                  </div>
                  <div class="status-grid">
                    <span>状态：{{ statusName(wordDetail.status) }}</span>
                    <span>正确：{{ wordDetail.correctCount || 0 }}</span>
                    <span>错误：{{ wordDetail.wrongCount || 0 }}</span>
                    <span>不认识：{{ wordDetail.unknownCount || 0 }}</span>
                  </div>
                </div>
                <button class="primary" @click="nextWord">{{ isLastWord ? '完成本轮' : '下一题' }}</button>
              </div>
            </template>

            <section class="result" v-else>
              <h2>听写完成</h2>
              <div class="result-grid">
                <span>总题数：{{ stats.total }}</span>
                <span>拼对：{{ stats.correct }}</span>
                <span>拼错：{{ stats.wrong }}</span>
                <span>正确率：{{ accuracy }}%</span>
              </div>
              <button class="primary" @click="startRound(roundSize)">再来一轮</button>
            </section>
          </section>
        </div>

        <aside class="dictation-side">
          <section class="panel side-section">
            <h2>本轮统计</h2>
            <div class="progress"><div :style="{ width: progress + '%' }"></div></div>
            <div class="mini-stats">
              <span>已听写 <strong>{{ stats.total }}</strong></span>
              <span>拼对 <strong>{{ stats.correct }}</strong></span>
              <span>拼错 <strong>{{ stats.wrong }}</strong></span>
              <span>剩余 <strong>{{ remaining }}</strong></span>
            </div>
          </section>

          <section class="panel side-section">
            <h2>训练规则</h2>
            <div class="privilege-list vertical-pills">
              <span>优先抽到期复习词</span>
              <span>薄弱词权重更高</span>
              <span>无数据时补充自定义词和公共词</span>
              <span>错误会回流到错题与复习状态</span>
            </div>
          </section>

          <section class="panel side-section">
            <h2>最近拼错</h2>
            <p class="empty" v-if="!wrongAnswers.length">本轮还没有拼错。</p>
            <ul class="weak-list" v-else>
              <li v-for="item in wrongAnswers" :key="item.key">
                <div>
                  <strong>{{ item.correct }}</strong>
                  <span>你的答案：{{ item.answer || '未输入' }}</span>
                </div>
              </li>
            </ul>
          </section>
        </aside>
      </section>
    </section>
  </main>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import {
  addStudyRecord,
  addWrongWord,
  getMembership,
  getProDictationWords,
  getWordDetail,
  recordLearningStatus
} from '../api'
import TopNav from './components/TopNav.vue'
import { speakWord } from '../utils/speech'

const membership = ref(null)
const words = ref([])
const currentIndex = ref(0)
const currentWord = ref(null)
const wordDetail = ref(null)
const answer = ref('')
const submitted = ref(false)
const finished = ref(false)
const loading = ref(false)
const error = ref('')
const roundSize = ref(12)
const customSize = ref(null)
const stats = ref({ total: 0, correct: 0, wrong: 0 })
const result = ref({ correct: false, message: '' })
const wrongAnswers = ref([])

const accuracy = computed(() => stats.value.total === 0 ? '0.00' : ((stats.value.correct / stats.value.total) * 100).toFixed(2))
const progress = computed(() => words.value.length ? Math.round((stats.value.total / words.value.length) * 100) : 0)
const remaining = computed(() => Math.max(0, words.value.length - stats.value.total))
const isLastWord = computed(() => currentIndex.value >= words.value.length - 1)

onMounted(load)

async function load() {
  error.value = ''
  try {
    membership.value = await getMembership()
    if (membership.value.pro) {
      await startRound(roundSize.value)
    }
  } catch (e) {
    error.value = e.message
  }
}

async function startRound(size) {
  if (!membership.value?.pro) return
  loading.value = true
  error.value = ''
  roundSize.value = Math.max(5, Math.min(50, Number(size) || 12))
  stats.value = { total: 0, correct: 0, wrong: 0 }
  wrongAnswers.value = []
  finished.value = false
  try {
    words.value = await getProDictationWords(roundSize.value)
    currentIndex.value = 0
    setCurrentWord()
  } catch (e) {
    error.value = e.message
  } finally {
    loading.value = false
  }
}

function setCurrentWord() {
  currentWord.value = words.value[currentIndex.value] || null
  wordDetail.value = null
  answer.value = ''
  submitted.value = false
  result.value = { correct: false, message: '' }
  if (currentWord.value) {
    setTimeout(playCurrent, 180)
  }
}

function playCurrent() {
  if (currentWord.value?.english) speakWord(currentWord.value.english)
}

async function submitAnswer() {
  if (!currentWord.value || !answer.value) {
    error.value = '请先输入听到的英文'
    return
  }
  await judge(false)
}

async function revealAnswer() {
  if (!currentWord.value) return
  await judge(true)
}

async function judge(revealed) {
  error.value = ''
  submitted.value = true
  const correctText = currentWord.value.english || ''
  const isCorrect = !revealed && normalize(answer.value) === normalize(correctText)
  result.value = {
    correct: isCorrect,
    message: isCorrect
      ? `拼写正确：${correctText}`
      : `正确拼写是：${correctText}`
  }
  stats.value.total++
  if (isCorrect) {
    stats.value.correct++
  } else {
    stats.value.wrong++
    wrongAnswers.value.unshift({
      key: `${currentWord.value.wordType}-${currentWord.value.id}-${stats.value.total}`,
      correct: correctText,
      answer: answer.value
    })
  }
  await saveLearningResult(isCorrect)
}

async function saveLearningResult(isCorrect) {
  try {
    wordDetail.value = await getWordDetail(currentWord.value.id, currentWord.value.wordType || 'public')
    await recordLearningStatus({
      wordId: currentWord.value.id,
      wordType: currentWord.value.wordType || 'public',
      english: currentWord.value.english,
      result: isCorrect ? 'CORRECT' : 'WRONG'
    })
    if (!isCorrect && wordDetail.value?.word) {
      await addWrongWord({
        wordId: currentWord.value.id,
        wordType: currentWord.value.wordType || 'public',
        english: currentWord.value.english,
        chinese: wordDetail.value.word.chinese,
        unknown: false
      })
    }
  } catch (e) {
    error.value = e.message
  }
}

async function nextWord() {
  if (isLastWord.value) {
    await finishRound()
    return
  }
  currentIndex.value++
  setCurrentWord()
}

async function finishRound() {
  finished.value = true
  try {
    await addStudyRecord({
      totalCount: stats.value.total,
      correctCount: stats.value.correct,
      wrongCount: stats.value.wrong,
      accuracy: accuracy.value,
      mode: '会员听写'
    })
  } catch (e) {
    error.value = e.message
  }
}

function normalize(value) {
  return String(value || '').trim().toLowerCase().replace(/[^a-z]/g, '')
}

function statusName(status) {
  return { NEW: '未学习', LEARNING: '学习中', UNKNOWN: '不认识', HIGH_WRONG: '高频错误', MASTERED: '已掌握' }[status] || status
}
</script>
