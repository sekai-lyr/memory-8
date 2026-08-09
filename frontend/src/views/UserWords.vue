<template>
  <main>
    <TopNav />
    <section class="page wide-page">
      <div class="toolbar hero-bar compact-hero">
        <div>
          <h1>自定义词库</h1>
          <p class="muted">手动维护少量单词，批量导入大量词表。导入后保存到 MySQL，刷新不会丢失。</p>
        </div>
        <router-link class="button-link" to="/practice">去练自定义词</router-link>
      </div>
      <p class="error" v-if="error">{{ error }}</p>
      <p class="success" v-if="message">{{ message }}</p>

      <section class="words-layout">
        <div class="words-tools">
          <section class="panel">
            <h2>{{ editingId ? '修改单词' : '新增单词' }}</h2>
            <form class="form" @submit.prevent="save">
              <input v-model.trim="form.english" placeholder="英文 english，必填" />
              <input v-model.trim="form.chinese" placeholder="中文 chinese，必填；多个释义用中文分号分隔" />
              <input v-model.trim="form.phonetic" placeholder="音标 phonetic，可选，例如 [di-teyld]" />
              <input v-model.trim="form.exampleSentence" placeholder="例句 exampleSentence，可选" />
              <div class="inline-fields">
                <input v-model.trim="form.category" placeholder="分类 category，例如 CET-6 我的词库" />
                <select v-model="form.difficulty">
                  <option value="">选择难度</option>
                  <option>easy</option>
                  <option>medium</option>
                  <option>hard</option>
                </select>
              </div>
              <div class="actions">
                <button class="primary" type="submit" :disabled="saving">{{ saving ? '保存中...' : '保存' }}</button>
                <button type="button" @click="reset">取消</button>
              </div>
            </form>
          </section>

          <section class="panel import-panel">
            <div class="toolbar">
              <h2>批量导入</h2>
              <button @click="downloadTemplate">下载模板</button>
            </div>
            <div class="template-box">
              <strong>支持两种格式</strong>
              <code>english,chinese,phonetic,exampleSentence,category,difficulty</code>
              <code>序号,单词,音标,释义</code>
              <p class="muted">英文和中文必填。例句中如果有逗号，请用英文双引号包起来。</p>
            </div>
            <label class="upload-zone" :class="{ dragging }" @dragover.prevent="dragging = true" @dragleave.prevent="dragging = false" @drop.prevent="dropFile">
              <input type="file" accept=".csv" @change="chooseFile" />
              <strong>{{ selectedFile?.name || '点击或拖拽表格文件到这里' }}</strong>
              <span>仅支持 .csv 表格文件</span>
            </label>
            <div class="actions">
              <button class="primary" @click="upload" :disabled="!selectedFile || importing || !isPro">{{ importing ? '导入中...' : '开始导入' }}</button>
              <button @click="selectedFile = null" :disabled="!selectedFile">清除文件</button>
            </div>
            <p class="error" v-if="membership && !isPro">表格批量导入是会员功能，升级后可一次导入大量词表。</p>
            <div class="import-result" v-if="importResult">
              <strong>导入结果</strong>
              <span>成功：{{ importResult.successCount }} 条</span>
              <span>跳过：{{ importResult.skippedCount }} 条</span>
            </div>
          </section>
        </div>

        <section class="panel words-list-panel">
          <div class="toolbar">
            <h2>我的单词</h2>
            <span class="badge">{{ words.length }} 个</span>
          </div>
          <input v-model="keyword" @input="load" placeholder="输入英文或中文查询自己的单词" />
          <p class="empty" v-if="!words.length">暂无自定义单词。</p>
          <ul class="word-list">
            <li v-for="item in words" :key="item.id">
              <div>
                <strong>{{ item.english }}</strong>
                <span>{{ item.chinese }}</span>
                <small>{{ item.phonetic }} {{ item.category }} {{ item.difficulty }}</small>
                <p v-if="item.exampleSentence">{{ item.exampleSentence }}</p>
              </div>
              <div class="actions">
                <button @click="edit(item)">修改</button>
                <button @click="remove(item.id)">删除</button>
              </div>
            </li>
          </ul>
        </section>
      </section>
    </section>
  </main>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { addUserWord, deleteUserWord, getMembership, importUserWords, listUserWords, updateUserWord } from '../api'
import TopNav from './components/TopNav.vue'

const emptyForm = () => ({ english: '', chinese: '', phonetic: '', exampleSentence: '', category: '', difficulty: '' })
const form = reactive(emptyForm())
const words = ref([])
const keyword = ref('')
const editingId = ref(null)
const message = ref('')
const error = ref('')
const saving = ref(false)
const importing = ref(false)
const dragging = ref(false)
const selectedFile = ref(null)
const importResult = ref(null)
const membership = ref(null)
const isPro = computed(() => Boolean(membership.value?.pro))

onMounted(async () => {
  membership.value = await getMembership().catch(() => null)
  await load()
})

async function load() {
  try {
    words.value = await listUserWords(keyword.value)
  } catch (e) {
    error.value = e.message
  }
}

async function save() {
  error.value = ''
  message.value = ''
  if (!form.english || !form.chinese) {
    error.value = '英文和中文释义不能为空'
    return
  }
  saving.value = true
  try {
    if (editingId.value) await updateUserWord(editingId.value, form)
    else await addUserWord(form)
    message.value = '保存成功'
    reset()
    await load()
  } catch (e) {
    error.value = e.message
  } finally {
    saving.value = false
  }
}

function edit(item) {
  editingId.value = item.id
  Object.assign(form, item)
}

function reset() {
  editingId.value = null
  Object.assign(form, emptyForm())
}

async function remove(id) {
  try {
    await deleteUserWord(id)
    message.value = '删除成功'
    await load()
  } catch (e) {
    error.value = e.message
  }
}

function chooseFile(event) {
  selectedFile.value = event.target.files[0] || null
  importResult.value = null
}

function dropFile(event) {
  dragging.value = false
  selectedFile.value = event.dataTransfer.files[0] || null
  importResult.value = null
}

async function upload() {
  if (!selectedFile.value) return
  if (!isPro.value) {
    error.value = '表格批量导入需要会员'
    return
  }
  error.value = ''
  message.value = ''
  importing.value = true
  try {
    const result = await importUserWords(selectedFile.value)
    importResult.value = result
    message.value = `${result.message} 成功 ${result.successCount} 条，跳过 ${result.skippedCount} 条`
    selectedFile.value = null
    await load()
  } catch (e) {
    error.value = e.message
  } finally {
    importing.value = false
  }
}

function downloadTemplate() {
  const rows = [
    ['english', 'chinese', 'phonetic', 'exampleSentence', 'category', 'difficulty'],
    ['example', '例子；示例', '[ig-ZAM-puhl]', 'This is an example sentence.', 'custom', 'easy'],
    ['elaborate', '详细的；详尽的；精心制作的', '[i-LAB-er-uht]', 'She gave an elaborate explanation.', 'CET-6', 'medium']
  ]
  const csv = rows.map(row => row.map(csvCell).join(',')).join('\n')
  const blob = new Blob(['\uFEFF' + csv], { type: 'text/csv;charset=utf-8' })
  const link = document.createElement('a')
  link.href = URL.createObjectURL(blob)
  link.download = 'word-import-template.csv'
  link.click()
  URL.revokeObjectURL(link.href)
}

function csvCell(value) {
  return `"${String(value ?? '').replace(/"/g, '""')}"`
}
</script>
