<template>
  <main>
    <TopNav />
    <section class="page wide-page">
      <div class="toolbar hero-bar compact-hero">
        <div>
          <h1>单词测试</h1>
          <p class="muted">先选择练习来源，再开始一轮。答题区默认只显示英文，提交后才显示中文释义。</p>
        </div>
        <button class="ghost" @click="logout">退出登录</button>
      </div>

      <p class="error" v-if="error">{{ error }}</p>

      <section class="practice-shell">
        <div class="practice-main">
          <section class="panel control-panel">
            <div class="mode-tabs">
              <button :class="{ active: sourceMode === 'public' }" @click="changeMode('public')">公共词库</button>
              <button :class="{ active: sourceMode === 'smart' }" @click="changeMode('smart')">
                智能复习 <span v-if="!isPro">会员</span>
              </button>
              <button :class="{ active: sourceMode === 'wrong' }" @click="changeMode('wrong')">只练错题</button>
              <button :class="{ active: sourceMode === 'user' }" @click="changeMode('user')">自定义词库</button>
            </div>

            <div class="practice-options">
              <select v-if="sourceMode === 'public'" v-model="selectedCategory" @change="setRound(roundSize)">
                <option value="">全部公共词库</option>
                <option v-for="item in categories" :key="item" :value="item">{{ item }}</option>
              </select>
              <div v-else class="source-note">{{ sourceTips[sourceMode] }}</div>
              <div class="round-controls">
                <button :class="{ active: roundSize === 10 }" @click="setRound(10)">10 个</button>
                <button :class="{ active: roundSize === 15 }" @click="setRound(15)">15 个</button>
                <button :class="{ active: roundSize === 20 }" @click="setRound(20)">20 个</button>
                <input class="small-input" v-model.number="customSize" type="number" min="1" placeholder="自定义" />
                <button class="primary" @click="setRound(customSize || roundSize)">开始一轮</button>
              </div>
            </div>
          </section>

          <section class="panel practice-card-panel">
            <template v-if="!finished">
              <div class="word-card focused">
                <span class="badge">{{ roundTitle }} · {{ Math.min(stats.total + 1, roundSize) }}/{{ roundSize }}</span>
                <h2>{{ currentWord?.english || '暂无单词' }}</h2>
                <div class="word-meta">
                  <span v-if="currentWord?.status">{{ statusName(currentWord.status) }}</span>
                  <span v-if="currentWord?.wrongCount">错误 {{ currentWord.wrongCount }} 次</span>
                  <span v-if="currentWord?.unknownCount">不认识 {{ currentWord.unknownCount }} 次</span>
                  <span v-if="currentWord?.reviewIntervalDays">间隔 {{ currentWord.reviewIntervalDays }} 天</span>
                  <span v-if="currentWord?.nextReviewTime">下次 {{ formatDate(currentWord.nextReviewTime) }}</span>
                </div>
                <p v-if="currentWord?.phonetic" class="phonetic">{{ currentWord.phonetic }}</p>
                <div class="listen-row">
                  <button @click="speakCurrent" :disabled="!currentWord">播放发音</button>
                  <label class="toggle-line">
                    <input type="checkbox" v-model="autoSpeak" />
                    自动发音
                  </label>
                </div>
                <p v-if="currentWord?.chinese" class="answer-preview">{{ currentWord.chinese }}</p>
              </div>

              <form class="answer-row" @submit.prevent="submitAnswer">
                <input v-model.trim="answer" :disabled="!currentWord" placeholder="输入中文释义，按 Enter 提交" />
                <button class="primary" :disabled="!currentWord">提交</button>
              </form>

              <div class="action-grid">
                <button @click="nextWord">下一题</button>
                <button @click="unknownCurrent" :disabled="!currentWord">不认识</button>
                <button @click="masterCurrent" :disabled="!currentWord">太简单，不再出现</button>
                <button @click="markCurrent" :disabled="!currentWord || !currentWord.chinese">标记/收藏</button>
                <button @click="loadWordDetail" :disabled="!currentWord">单词详情</button>
              </div>
              <p :class="messageClass" v-if="message">{{ message }}</p>
            </template>

            <section class="result" v-else>
              <h2>本轮完成</h2>
              <div class="result-grid">
                <span>总题数：{{ stats.total }}</span>
                <span>正确：{{ stats.correct }}</span>
                <span>错误：{{ stats.wrong }}</span>
                <span>不认识：{{ stats.unknown }}</span>
                <span>正确率：{{ accuracy }}%</span>
              </div>
              <h3>错题/生词列表</h3>
              <ul class="word-list compact">
                <li v-for="item in roundWrongWords" :key="item.wordType + item.wordId">
                  <div>
                    <strong>{{ item.english }}</strong>
                    <span>{{ item.chinese }}</span>
                    <small>累计错误：{{ item.wrongCount || 1 }} 次；不认识：{{ item.unknownCount || 0 }} 次</small>
                  </div>
                </li>
              </ul>
              <button class="primary" @click="setRound(roundSize)">重新开始一轮</button>
            </section>
          </section>

          <section class="panel" v-if="wordDetail">
            <div class="toolbar">
              <h2>单词详情</h2>
              <button @click="wordDetail = null">关闭</button>
            </div>
            <div class="detail-grid">
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
          </section>
        </div>

        <aside class="practice-side">
          <section class="panel side-section">
            <h2>本轮统计</h2>
            <div class="progress"><div :style="{ width: roundProgress + '%' }"></div></div>
            <div class="mini-stats">
              <span>已答 <strong>{{ stats.total }}</strong></span>
              <span>答对 <strong>{{ stats.correct }}</strong></span>
              <span>答错 <strong>{{ stats.wrong }}</strong></span>
              <span>不认识 <strong>{{ stats.unknown }}</strong></span>
            </div>
          </section>

          <section class="panel side-section">
            <h2>学习状态</h2>
            <div class="status-grid">
              <span>到期复习：{{ statusSummary.DUE || 0 }}</span>
              <span>学习中：{{ statusSummary.LEARNING || 0 }}</span>
              <span>不认识：{{ statusSummary.UNKNOWN || 0 }}</span>
              <span>高频错误：{{ statusSummary.HIGH_WRONG || 0 }}</span>
              <span>已掌握：{{ statusSummary.MASTERED || 0 }}</span>
            </div>
          </section>

          <section class="panel side-section">
            <h2>每日目标</h2>
            <div class="goal-box">
              <input class="small-input" v-model.number="dailyGoal" type="number" min="1" />
              <span>{{ todayDone }}/{{ dailyGoal }}</span>
            </div>
          </section>

          <section class="panel side-section">
            <h2>智能查询</h2>
            <input v-model="keyword" @input="doSearch" placeholder="输入英文或中文自动查询" />
            <ul class="search-list compact">
              <li v-for="item in searchResults" :key="item.wordType + item.id">
                <div>
                  <strong>{{ item.english }}</strong>
                  <span>{{ item.chinese }}</span>
                  <small>{{ item.phonetic }} {{ item.category }} {{ item.difficulty }}</small>
                </div>
              </li>
            </ul>
          </section>
        </aside>
      </section>

      <section class="panel">
        <div class="toolbar">
          <h2>本地标记单词</h2>
          <button @click="clearMarked" :disabled="!markedWords.length">清空全部</button>
        </div>
        <p class="empty" v-if="!markedWords.length">还没有标记单词。提交答案后点击“标记/收藏”即可加入。</p>
        <ul class="marked-list">
          <li v-for="item in markedWords" :key="item.wordType + item.wordId" @click="toggleMarked(item)">
            <div>
              <strong>{{ item.english }}</strong>
              <span class="hover-answer">{{ item.chinese }}</span>
              <p v-if="item.open">{{ item.chinese }}</p>
            </div>
            <button @click.stop="removeMarked(item)">删除</button>
          </li>
        </ul>
      </section>
    </section>
  </main>
</template>

<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import {
  addFavoriteWord,
  addMasteredWord,
  addStudyRecord,
  addWrongWord,
  checkAnswer,
  getLearningStatusSummary,
  getMembership,
  getRandomWords,
  getSmartQueue,
  getWordDetail,
  listMasteredWordKeys,
  listUserWords,
  listWordCategories,
  listWrongWords,
  recordLearningStatus,
  searchWords
} from '../api'
import TopNav from './components/TopNav.vue'
import { speakWord } from '../utils/speech'

const router = useRouter()
const currentWord = ref(null)
const queue = ref([])
const answer = ref('')
const message = ref('')
const messageClass = ref('')
const error = ref('')
const roundWrongWords = ref([])
const finished = ref(false)
const roundSize = ref(10)
const customSize = ref(null)
const sourceMode = ref(localStorage.getItem('practiceSourceMode') || 'public')
const selectedCategory = ref(localStorage.getItem('practiceCategory') || '')
const categories = ref([])
const keyword = ref('')
const searchResults = ref([])
const wordDetail = ref(null)
const markedWords = ref(JSON.parse(localStorage.getItem('markedWords') || '[]'))
const dailyGoal = ref(Number(localStorage.getItem('dailyGoal') || 30))
const todayDone = ref(Number(localStorage.getItem(todayKey()) || 0))
const masteredKeys = ref([])
const statusSummary = ref({})
const stats = ref({ total: 0, correct: 0, wrong: 0, unknown: 0 })
const membership = ref(null)
const autoSpeak = ref(localStorage.getItem('autoSpeak') === 'true')

const sourceTips = {
  smart: '按到期时间、错误次数和不认识次数自动排序。',
  wrong: '只从你的错题本中抽题。',
  user: '只练习你自己导入或新增的单词。'
}
const modeName = computed(() => ({ public: '公共词库', smart: '智能复习', wrong: '错题复习', user: '自定义词库' }[sourceMode.value]))
const roundTitle = computed(() => sourceMode.value === 'public' && selectedCategory.value ? selectedCategory.value : modeName.value)
const accuracy = computed(() => stats.value.total === 0 ? '0.00' : ((stats.value.correct / stats.value.total) * 100).toFixed(2))
const roundProgress = computed(() => Math.min(100, Math.round((stats.value.total / roundSize.value) * 100)))
const isPro = computed(() => Boolean(membership.value?.pro))

watch(dailyGoal, value => localStorage.setItem('dailyGoal', value || 30))
watch(sourceMode, value => localStorage.setItem('practiceSourceMode', value))
watch(selectedCategory, value => localStorage.setItem('practiceCategory', value))
watch(autoSpeak, value => localStorage.setItem('autoSpeak', value ? 'true' : 'false'))

onMounted(async () => {
  await loadMembership()
  if (sourceMode.value === 'smart' && !isPro.value) {
    sourceMode.value = 'public'
  }
  await loadCategories()
  await loadMasteredKeys()
  await loadStatusSummary()
  await setRound(10)
})

function todayKey() {
  return `dailyDone:${new Date().toISOString().slice(0, 10)}`
}

async function loadCategories() {
  categories.value = await listWordCategories().catch(() => [])
}

async function loadMasteredKeys() {
  masteredKeys.value = await listMasteredWordKeys().catch(() => [])
}

async function loadStatusSummary() {
  statusSummary.value = await getLearningStatusSummary().catch(() => ({}))
}

async function loadMembership() {
  membership.value = await getMembership().catch(() => null)
}

function resetRound() {
  stats.value = { total: 0, correct: 0, wrong: 0, unknown: 0 }
  roundWrongWords.value = []
  finished.value = false
  message.value = ''
  error.value = ''
}

async function changeMode(mode) {
  if (mode === 'smart' && !isPro.value) {
    router.push('/membership')
    return
  }
  sourceMode.value = mode
  await setRound(roundSize.value)
}

async function setRound(size) {
  resetRound()
  roundSize.value = Math.max(1, Number(size) || 10)
  await loadMasteredKeys()
  try {
    if (sourceMode.value === 'smart') {
      queue.value = await getSmartQueue(roundSize.value)
    } else if (sourceMode.value === 'wrong') {
      const wrongs = await listWrongWords()
      queue.value = shuffle(wrongs)
        .filter(item => !isMastered(item.wordId, item.wordType || 'public'))
        .slice(0, roundSize.value)
        .map(item => ({ ...item, id: item.wordId, wordId: item.wordId, wordType: item.wordType || 'public' }))
    } else if (sourceMode.value === 'user') {
      const userWords = await listUserWords('')
      queue.value = shuffle(userWords)
        .filter(item => !isMastered(item.id, 'user'))
        .slice(0, roundSize.value)
        .map(item => ({ id: item.id, wordId: item.id, english: item.english, wordType: 'user' }))
    } else {
      const candidates = await getRandomWords(roundSize.value * 3, selectedCategory.value || undefined)
      queue.value = candidates.filter(item => !isMastered(item.id, 'public')).slice(0, roundSize.value)
    }
    if (!queue.value.length) {
      currentWord.value = null
      message.value = '当前模式没有可练习的单词，可以换一个词库或先添加单词。'
      messageClass.value = 'error'
      return
    }
    nextWord()
  } catch (e) {
    error.value = e.message
  }
}

async function loadWordDetail() {
  if (!currentWord.value) return
  wordDetail.value = await getWordDetail(currentWord.value.wordId || currentWord.value.id, currentWord.value.wordType || 'public')
    .catch(e => { error.value = e.message; return null })
}

function statusName(status) {
  return { NEW: '未学习', LEARNING: '学习中', UNKNOWN: '不认识', HIGH_WRONG: '高频错误', MASTERED: '已掌握' }[status] || status
}

function formatDate(value) {
  return value ? value.replace('T', ' ').slice(0, 16) : ''
}

function isMastered(wordId, wordType) {
  return masteredKeys.value.includes(`${wordType}:${wordId}`)
}

function shuffle(list) {
  return [...list].sort(() => Math.random() - 0.5)
}

function nextWord() {
  answer.value = ''
  message.value = ''
  currentWord.value = queue.value.shift() || null
  if (autoSpeak.value && currentWord.value) {
    setTimeout(speakCurrent, 120)
  }
  if (!currentWord.value && stats.value.total > 0) finishRound()
}

function speakCurrent() {
  if (currentWord.value?.english) speakWord(currentWord.value.english)
}

async function submitAnswer() {
  if (!currentWord.value || !answer.value.trim()) {
    message.value = '请先输入答案'
    messageClass.value = 'error'
    return
  }
  try {
    const result = await checkAnswer({
      wordId: currentWord.value.wordId || currentWord.value.id,
      wordType: currentWord.value.wordType || 'public',
      answer: answer.value
    })
    currentWord.value = { ...currentWord.value, chinese: result.correctAnswer, english: result.english }
    stats.value.total++
    todayDone.value++
    localStorage.setItem(todayKey(), todayDone.value)
    if (result.correct) {
      stats.value.correct++
      await recordStatus('CORRECT')
      await loadStatusSummary()
      message.value = '回答正确！'
      messageClass.value = 'success'
      setTimeout(nextWord, 700)
    } else {
      await saveWrong(result, false)
    }
  } catch (e) {
    error.value = e.message
  }
}

async function unknownCurrent() {
  if (!currentWord.value) return
  try {
    const result = await checkAnswer({
      wordId: currentWord.value.wordId || currentWord.value.id,
      wordType: currentWord.value.wordType || 'public',
      answer: '__UNKNOWN__'
    })
    currentWord.value = { ...currentWord.value, chinese: result.correctAnswer, english: result.english }
    stats.value.total++
    stats.value.unknown++
    todayDone.value++
    localStorage.setItem(todayKey(), todayDone.value)
    await saveWrong(result, true)
  } catch (e) {
    error.value = e.message
  }
}

async function saveWrong(result, unknown) {
  stats.value.wrong++
  const wrongItem = { wordId: result.wordId, wordType: result.wordType, english: result.english, chinese: result.correctAnswer, unknown }
  const savedWrong = await addWrongWord(wrongItem)
  await recordStatus(unknown ? 'UNKNOWN' : 'WRONG')
  await loadStatusSummary()
  wrongItem.wrongCount = savedWrong?.wrongCount
  wrongItem.unknownCount = savedWrong?.unknownCount
  currentWord.value.wrongCount = savedWrong?.wrongCount
  currentWord.value.unknownCount = savedWrong?.unknownCount
  roundWrongWords.value.push(wrongItem)
  message.value = unknown
    ? `已记录为不认识，正确答案是：${result.correctAnswer}（累计不认识 ${wrongItem.unknownCount || 1} 次）`
    : `${result.message || `回答错误，正确答案是：${result.correctAnswer}`}（累计错误 ${wrongItem.wrongCount || 1} 次）`
  messageClass.value = 'error'
}

async function masterCurrent() {
  if (!currentWord.value) return
  const item = {
    wordId: currentWord.value.wordId || currentWord.value.id,
    wordType: currentWord.value.wordType || 'public',
    english: currentWord.value.english,
    chinese: currentWord.value.chinese || currentWord.value.english
  }
  try {
    await addMasteredWord(item)
    await recordStatus('MASTERED')
    await loadStatusSummary()
    masteredKeys.value.push(`${item.wordType}:${item.wordId}`)
    message.value = '已标记为简单词，后续练习不会再出现。'
    messageClass.value = 'success'
    nextWord()
  } catch (e) {
    error.value = e.message
  }
}

async function recordStatus(result) {
  if (!currentWord.value) return
  await recordLearningStatus({
    wordId: currentWord.value.wordId || currentWord.value.id,
    wordType: currentWord.value.wordType || 'public',
    english: currentWord.value.english,
    result
  }).catch(e => { error.value = e.message })
}

async function finishRound() {
  finished.value = true
  try {
    await addStudyRecord({
      totalCount: stats.value.total,
      correctCount: stats.value.correct,
      wrongCount: stats.value.wrong,
      accuracy: accuracy.value,
      mode: roundTitle.value
    })
  } catch (e) {
    error.value = e.message
  }
}

async function doSearch() {
  searchResults.value = keyword.value.trim() ? await searchWords(keyword.value.trim()).catch(e => { error.value = e.message; return [] }) : []
}

async function markCurrent() {
  const item = {
    wordId: currentWord.value.wordId || currentWord.value.id,
    wordType: currentWord.value.wordType || 'public',
    english: currentWord.value.english,
    chinese: currentWord.value.chinese,
    open: false
  }
  if (!markedWords.value.some(existing => existing.wordId === item.wordId && existing.wordType === item.wordType)) {
    markedWords.value.unshift(item)
    saveMarked()
  }
  await addFavoriteWord(item).catch(e => { error.value = e.message })
  message.value = '已收藏到数据库，并加入本地标记列表。'
  messageClass.value = 'success'
}

function toggleMarked(item) {
  item.open = !item.open
  saveMarked()
}

function removeMarked(item) {
  markedWords.value = markedWords.value.filter(existing => !(existing.wordId === item.wordId && existing.wordType === item.wordType))
  saveMarked()
}

function clearMarked() {
  markedWords.value = []
  saveMarked()
}

function saveMarked() {
  localStorage.setItem('markedWords', JSON.stringify(markedWords.value))
}

function logout() {
  localStorage.removeItem('user')
  localStorage.removeItem('userId')
  localStorage.removeItem('username')
  localStorage.removeItem('token')
  router.push('/login')
}
</script>
