<template>
  <main>
    <TopNav />
    <section class="page">
      <div class="toolbar hero-bar compact-hero">
        <div>
          <span class="eyebrow">公共词库</span>
          <h1>公共词库</h1>
          <p class="muted">这里放系统内置单词。词库会继续扩，先查词、听发音、看例句，再去练习。</p>
        </div>
        <router-link class="button-link" to="/practice">去练习</router-link>
      </div>
      <p class="error" v-if="error">{{ error }}</p>

      <section class="panel word-browser">
        <div class="filter-row">
          <input v-model="keyword" @input="load" placeholder="输入英文或中文查词" />
          <select v-model="category" @change="load">
            <option value="">全部词库</option>
            <option v-for="item in categories" :key="item" :value="item">{{ item }}</option>
          </select>
        </div>

        <div class="library-summary">
          <span>词库总量 <strong>{{ stats.totalWords }}</strong> 个词</span>
          <span>当前显示 <strong>{{ visibleWords.length }}</strong> 个词</span>
          <span>分类 <strong>{{ stats.categories.length || categories.length }}</strong> 个</span>
          <span>{{ speechSupported ? '支持浏览器发音' : '当前浏览器不支持发音' }}</span>
        </div>

        <div class="category-cloud" v-if="stats.categories.length">
          <button
            v-for="item in stats.categories"
            :key="item.category"
            type="button"
            :class="{ active: category === item.category }"
            @click="selectCategory(item.category)"
          >
            {{ item.category }} {{ item.count }}
          </button>
        </div>

        <ul class="word-list library-list">
          <li v-for="item in visibleWords" :key="item.id">
            <div>
              <div class="word-title-row">
                <strong>{{ item.english }}</strong>
                <button class="icon-button" @click="speak(item.english)" :disabled="!speechSupported" title="播放发音">听</button>
              </div>
              <span>{{ item.chinese }}</span>
              <small>
                <template v-if="item.phonetic">音标：{{ item.phonetic }}</template>
                <template v-if="item.category"> · {{ item.category }}</template>
                <template v-if="item.difficulty"> · {{ item.difficulty }}</template>
              </small>
              <p v-if="item.exampleSentence">{{ item.exampleSentence }}</p>
              <p class="memory-hint">{{ memoryHint(item.english) }}</p>
            </div>
          </li>
        </ul>

        <p class="muted result-limit-note" v-if="filteredWords.length > displayLimit">
          当前先展示前 {{ displayLimit }} 个，输入英文或中文可以继续精确查找。
        </p>
      </section>
    </section>
  </main>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { getWordStats, listWordCategories, listWords, searchWords } from '../api'
import TopNav from './components/TopNav.vue'
import { canSpeak, speakWord } from '../utils/speech'

const displayLimit = 300
const keyword = ref('')
const category = ref('')
const categories = ref([])
const words = ref([])
const error = ref('')
const stats = ref({ totalWords: 0, categories: [] })
const speechSupported = canSpeak()
const filteredWords = computed(() => words.value.filter(item => !category.value || item.category === category.value))
const visibleWords = computed(() => filteredWords.value.slice(0, displayLimit))

onMounted(async () => {
  await loadCategories()
  await loadStats()
  await load()
})

async function loadCategories() {
  try {
    categories.value = await listWordCategories()
  } catch (e) {
    error.value = e.message
  }
}

async function loadStats() {
  try {
    stats.value = await getWordStats()
  } catch (e) {
    error.value = e.message
  }
}

async function load() {
  try {
    const result = keyword.value.trim() ? await searchWords(keyword.value.trim()) : await listWords(category.value || undefined)
    words.value = result.filter(item => item.wordType === 'public')
  } catch (e) {
    error.value = e.message
  }
}

function selectCategory(value) {
  category.value = category.value === value ? '' : value
  load()
}

function speak(word) {
  speakWord(word)
}

function memoryHint(word) {
  if (!word) return ''
  const lower = word.toLowerCase()
  const chunks = lower.match(/[aeiouy]*[^aeiouy]+|[aeiouy]+/g)?.filter(Boolean).slice(0, 4) || [lower]
  if (lower.length <= 5) return `记忆提示：短词先听准发音，再用例句固定含义。`
  return `记忆提示：拆成 ${chunks.join(' · ')}，先读顺，再回到完整拼写。`
}
</script>
