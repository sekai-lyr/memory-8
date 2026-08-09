<template>
  <main>
    <TopNav />
    <section class="page">
      <div class="toolbar hero-bar">
        <div>
          <h1>收藏单词</h1>
          <p class="muted">收藏词会保存到数据库。鼠标悬停显示中文释义，点击卡片可展开或收起。</p>
        </div>
        <button @click="clearAll" :disabled="!words.length">清空全部</button>
      </div>
      <p class="error" v-if="error">{{ error }}</p>
      <section class="panel">
        <p class="empty" v-if="!words.length">暂无收藏。</p>
        <ul class="marked-list">
          <li v-for="item in words" :key="item.id" @click="item.open = !item.open">
            <div>
              <strong>{{ item.english }}</strong>
              <span class="hover-answer">{{ item.chinese }}</span>
              <p v-if="item.open">{{ item.chinese }}</p>
            </div>
            <button @click.stop="remove(item.id)">删除</button>
          </li>
        </ul>
      </section>
    </section>
  </main>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { clearFavoriteWords, deleteFavoriteWord, listFavoriteWords } from '../api'
import TopNav from './components/TopNav.vue'

const words = ref([])
const error = ref('')
onMounted(load)

async function load() {
  try {
    words.value = (await listFavoriteWords()).map(item => ({ ...item, open: false }))
  } catch (e) {
    error.value = e.message
  }
}

async function remove(id) {
  try {
    await deleteFavoriteWord(id)
    await load()
  } catch (e) {
    error.value = e.message
  }
}

async function clearAll() {
  try {
    await clearFavoriteWords()
    await load()
  } catch (e) {
    error.value = e.message
  }
}
</script>
