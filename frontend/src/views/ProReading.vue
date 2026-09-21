<template>
  <main>
    <TopNav />
    <section class="page wide-page">
      <div class="toolbar hero-bar reading-hero">
        <div>
          <h1>把一篇英文文章变成可练习词包</h1>
          <p class="muted">{{ heroText }}</p>
        </div>
        <div class="hero-actions">
          <router-link class="button-link" to="/user-words">我的词库</router-link>
          <router-link class="ghost-link" to="/practice">开始练习</router-link>
          <router-link class="ghost-link" to="/pro-review">复习中心</router-link>
        </div>
      </div>

      <p class="error" v-if="error">{{ error }}</p>
      <p class="success" v-if="message">{{ message }}</p>

      <section class="panel pro-lock-panel" v-if="membership && !membership.pro">
        <div>
          <span class="badge">会员专属</span>
          <h2>升级后解锁文章生词解析</h2>
          <p class="muted">普通背词很难坚持，文章解析能把试卷阅读、外刊、网页英文直接变成自己的词包。</p>
        </div>
        <router-link class="button-link" to="/membership">开通会员</router-link>
      </section>

      <section class="reading-layout" v-if="membership?.pro">
        <div class="reading-main">
          <section class="panel reading-input-panel">
            <div class="toolbar">
              <div>
                <h2>阅读材料</h2>
                <p class="muted">粘贴英文段落、试卷阅读、外刊文章或网页文本。</p>
              </div>
              <span class="badge pro">会员学习包</span>
            </div>
            <div class="reading-form">
              <input v-model.trim="form.title" placeholder="标题，例如：外刊精读 01" />
              <textarea v-model="form.content" placeholder="在这里粘贴英文文章..." />
              <div class="inline-fields">
                <label>
                  <span>最多提取</span>
                  <input v-model.number="form.maxWords" type="number" min="20" max="120" />
                </label>
                <button class="primary" @click="analyze" :disabled="loading">
                  {{ loading ? '分析中...' : '分析文章' }}
                </button>
              </div>
            </div>
          </section>

          <template v-if="result">
            <section class="summary-grid reading-summary">
              <div class="summary-card score-card"><span>阅读难度</span><strong>{{ result.difficultyScore }}</strong><small>{{ result.levelName }}</small></div>
              <div class="summary-card"><span>有效词形</span><strong>{{ result.uniqueWords }}</strong><small>总词数 {{ result.totalTokens }}</small></div>
              <div class="summary-card"><span>词库命中</span><strong>{{ result.matchedWords }}</strong><small>已识别释义</small></div>
              <div class="summary-card"><span>建议学习</span><strong>{{ result.suggestedWords }}</strong><small>已掌握 {{ result.knownWords }}</small></div>
            </section>

            <section class="panel reading-result-panel">
              <div class="toolbar">
                <div>
                  <h2>{{ result.title }}</h2>
                  <p class="muted">{{ result.summary }}</p>
                </div>
                <div class="hero-actions">
                  <input class="compact-input" v-model.trim="importCategory" placeholder="词包名称" />
                  <button class="primary" @click="importSelected" :disabled="importing || selectedCount === 0">
                    {{ importing ? '加入中...' : `加入 ${selectedCount} 个词` }}
                  </button>
                </div>
              </div>

              <div class="reading-highlights">
                <span v-for="item in result.highlights" :key="item">{{ item }}</span>
              </div>

              <ul class="reading-word-list">
                <li v-for="word in result.words" :key="word.english" :class="{ mutedWord: word.mastered || word.alreadyInUserWords }">
                  <label class="check-line">
                    <input
                      type="checkbox"
                      :checked="selectedWords.has(word.english)"
                      :disabled="word.mastered || word.alreadyInUserWords"
                      @change="toggleWord(word, $event.target.checked)"
                    />
                    <span></span>
                  </label>
                  <div class="reading-word-body">
                    <div class="word-title-row">
                      <strong>{{ word.english }}</strong>
                      <small>{{ word.frequency }} 次 · {{ difficultyLabel(word.difficulty) }}</small>
                    </div>
                    <p>{{ word.chinese }}</p>
                    <small class="muted">{{ word.reason }}</small>
                    <p class="example-line" v-if="word.exampleSentence">{{ word.exampleSentence }}</p>
                  </div>
                  <div class="reading-score">
                    <b>{{ word.priorityScore }}</b>
                    <span v-if="word.mastered">已掌握</span>
                    <span v-else-if="word.alreadyInUserWords">已在词库</span>
                    <span v-else-if="word.alreadyLearning">学习中</span>
                    <span v-else>新生词</span>
                  </div>
                </li>
              </ul>
            </section>
          </template>
        </div>

        <aside class="reading-side">
          <section class="panel side-section">
            <h2>更适合拿去卖的点</h2>
            <div class="privilege-list vertical-pills">
              <span>试卷阅读直接变词包</span>
              <span>外刊精读自动提词</span>
              <span>自动跳过已掌握词</span>
              <span>按优先级导入</span>
              <span>导入后可继续练习和复习</span>
            </div>
          </section>

          <section class="panel side-section">
            <h2>示例材料</h2>
            <button class="ghost full-width" @click="fillDemo">填入一段示例</button>
            <p class="muted">用户可以把课堂文章、考研阅读、四六级阅读、工作邮件和网页内容粘进来。</p>
          </section>

          <section class="panel side-section" v-if="result">
            <h2>学习包状态</h2>
            <div class="status-grid">
              <span>当前显示：{{ result.words.length }}</span>
              <span>已选择：{{ selectedCount }}</span>
              <span>已掌握：{{ result.knownWords }}</span>
              <span>难度：{{ result.levelName }}</span>
            </div>
          </section>
        </aside>
      </section>
    </section>
  </main>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { analyzeProReading, getMembership, importProReadingWords } from '../api'
import TopNav from './components/TopNav.vue'

const membership = ref(null)
const result = ref(null)
const selectedWords = ref(new Set())
const error = ref('')
const message = ref('')
const loading = ref(false)
const importing = ref(false)
const importCategory = ref('文章生词')
const form = reactive({
  title: '外刊精读',
  content: '',
  maxWords: 50
})

const selectedCount = computed(() => selectedWords.value.size)
const heroText = computed(() => {
  if (!membership.value) return '正在读取会员状态。'
  if (!membership.value.pro) return '开通会员后，可以把英文文章自动变成学习包。'
  if (!result.value) return '粘贴一段英文，系统会提取高价值生词并排序。'
  return `${result.value.levelName}，建议学习 ${result.value.suggestedWords} 个生词。`
})

onMounted(load)

async function load() {
  error.value = ''
  try {
    membership.value = await getMembership()
  } catch (e) {
    error.value = e.message
  }
}

async function analyze() {
  error.value = ''
  message.value = ''
  loading.value = true
  try {
    result.value = await analyzeProReading({ ...form })
    selectedWords.value = new Set(result.value.words
      .filter(word => !word.mastered && !word.alreadyInUserWords)
      .slice(0, 40)
      .map(word => word.english))
    importCategory.value = result.value.title || '文章生词'
  } catch (e) {
    error.value = e.message
  } finally {
    loading.value = false
  }
}

async function importSelected() {
  if (!result.value || selectedCount.value === 0) return
  error.value = ''
  message.value = ''
  importing.value = true
  try {
    const words = result.value.words
      .filter(word => selectedWords.value.has(word.english))
      .map(word => ({
        english: word.english,
        chinese: word.chinese,
        phonetic: word.phonetic,
        exampleSentence: word.exampleSentence,
        category: word.sourceCategory,
        difficulty: word.difficulty
      }))
    const imported = await importProReadingWords({ category: importCategory.value, words })
    message.value = `${imported.message}：成功 ${imported.successCount} 个，跳过 ${imported.skippedCount} 个`
    selectedWords.value = new Set()
  } catch (e) {
    error.value = e.message
  } finally {
    importing.value = false
  }
}

function toggleWord(word, checked) {
  const next = new Set(selectedWords.value)
  if (checked) next.add(word.english)
  else next.delete(word.english)
  selectedWords.value = next
}

function difficultyLabel(value) {
  const labels = {
    easy: '简单',
    medium: '中等',
    hard: '较难'
  }
  return labels[value] || '普通'
}

function fillDemo() {
  form.title = 'AI 教育外刊片段'
  form.content = `Artificial intelligence is reshaping education by providing personalized feedback, adaptive exercises, and immediate access to explanations. However, effective learning still requires attention, repetition, and meaningful context. Students who combine digital tools with deliberate practice are more likely to retain vocabulary and transfer knowledge to real communication.`
}
</script>
