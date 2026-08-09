<template>
  <main>
    <TopNav />
    <section class="page">
      <div class="toolbar hero-bar">
        <div>
          <h1>错题本</h1>
          <p class="muted">答错后会自动保存，刷新页面不会丢失。可以导出表格，也可以直接进入错题复习。</p>
        </div>
        <div class="actions">
          <router-link class="button-link" to="/practice">去复习</router-link>
          <button @click="exportCsv" :disabled="!words.length">导出表格</button>
          <button @click="clearAll" :disabled="!words.length">清空全部</button>
        </div>
      </div>
      <p class="error" v-if="error">{{ error }}</p>
      <section class="panel">
        <p class="empty" v-if="!words.length">暂无错题。</p>
        <ul class="word-list">
          <li v-for="item in words" :key="item.id">
            <div>
              <strong>{{ item.english }}</strong>
              <span>{{ item.chinese }}</span>
              <small>错误次数：{{ item.wrongCount || 1 }} · {{ item.wordType }}</small>
            </div>
            <button @click="remove(item.id)">删除</button>
          </li>
        </ul>
      </section>
    </section>
  </main>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { clearWrongWords, deleteWrongWord, listWrongWords } from '../api'
import TopNav from './components/TopNav.vue'

const words = ref([])
const error = ref('')
onMounted(load)

async function load() {
  try {
    words.value = await listWrongWords()
  } catch (e) {
    error.value = e.message
  }
}

async function remove(id) {
  try {
    await deleteWrongWord(id)
    await load()
  } catch (e) {
    error.value = e.message
  }
}

async function clearAll() {
  try {
    await clearWrongWords()
    await load()
  } catch (e) {
    error.value = e.message
  }
}

function exportCsv() {
  const rows = ['english,chinese,wordType,wrongCount']
  words.value.forEach(item => {
    rows.push([item.english, item.chinese, item.wordType, item.wrongCount || 1].map(csvCell).join(','))
  })
  const blob = new Blob([rows.join('\n')], { type: 'text/csv;charset=utf-8' })
  const link = document.createElement('a')
  link.href = URL.createObjectURL(blob)
  link.download = 'wrong-words.csv'
  link.click()
  URL.revokeObjectURL(link.href)
}

function csvCell(value) {
  return `"${String(value ?? '').replace(/"/g, '""')}"`
}
</script>
