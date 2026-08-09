<template>
  <main>
    <TopNav />
    <section class="page wide-page">
      <div class="toolbar hero-bar compact-hero">
        <div>
          <h1>变现后台</h1>
          <p class="muted">生成激活码、标记售价、查看兑换次数和估算收入。</p>
        </div>
        <router-link class="button-link" to="/membership">查看会员页</router-link>
      </div>

      <p class="error" v-if="error">{{ error }}</p>
      <p class="success" v-if="message">{{ message }}</p>

      <section class="summary-grid">
        <div class="summary-card"><span>激活码</span><strong>{{ summary?.totalCodes || 0 }}</strong></div>
        <div class="summary-card"><span>有效会员</span><strong>{{ summary?.activeSubscriptions || 0 }}</strong></div>
        <div class="summary-card"><span>已兑换</span><strong>{{ summary?.redeemedUses || 0 }}</strong></div>
        <div class="summary-card"><span>估算收入</span><strong>¥{{ summary?.estimatedRevenueYuan || '0.00' }}</strong></div>
      </section>

      <section class="admin-layout">
        <section class="panel">
          <h2>管理员密钥</h2>
          <form class="form" @submit.prevent="saveSecret">
            <input v-model="adminSecret" type="password" placeholder="输入 ADMIN_SECRET" />
            <div class="actions">
              <button class="primary" type="submit">保存并刷新</button>
              <button type="button" @click="clearSecret">清除</button>
            </div>
          </form>
          <p class="muted">密钥只保存在当前浏览器，用于请求后台发码接口。</p>
        </section>

        <section class="panel">
          <h2>生成会员激活码</h2>
          <form class="form" @submit.prevent="createCode">
            <input v-model.trim="form.code" placeholder="自定义激活码，可留空自动生成" />
            <div class="inline-fields">
              <input v-model.number="form.durationDays" type="number" min="1" placeholder="有效天数" />
              <input v-model.number="form.maxUses" type="number" min="1" placeholder="可兑换次数" />
            </div>
            <input v-model.number="form.priceYuan" type="number" min="0" step="0.01" placeholder="售价，单位元" />
            <input v-model.trim="form.note" placeholder="备注，例如 客户名/渠道/订单号" />
            <button class="primary" type="submit" :disabled="creating || !adminSecret">
              {{ creating ? '生成中...' : '生成激活码' }}
            </button>
          </form>
        </section>
      </section>

      <section class="panel">
        <div class="toolbar">
          <h2>激活码列表</h2>
          <button @click="load" :disabled="!adminSecret">刷新</button>
        </div>
        <p class="empty" v-if="!codes.length">暂无激活码。</p>
        <table v-else>
          <thead>
            <tr>
              <th>激活码</th>
              <th>天数</th>
              <th>售价</th>
              <th>兑换</th>
              <th>收入</th>
              <th>备注</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="item in codes" :key="item.code">
              <td><strong class="code-cell">{{ item.code }}</strong></td>
              <td>{{ item.durationDays }}</td>
              <td>¥{{ item.priceYuan || '0.00' }}</td>
              <td>{{ item.usedCount }}/{{ item.maxUses }}</td>
              <td>¥{{ item.redeemedRevenueYuan || '0.00' }}</td>
              <td>{{ item.note || '-' }}</td>
              <td><button @click="copyCode(item.code)">复制</button></td>
            </tr>
          </tbody>
        </table>
      </section>
    </section>
  </main>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { createActivationCode, getMonetizationSummary, listActivationCodes } from '../api'
import TopNav from './components/TopNav.vue'

const adminSecret = ref(localStorage.getItem('adminSecret') || '')
const summary = ref(null)
const codes = ref([])
const error = ref('')
const message = ref('')
const creating = ref(false)
const form = reactive({ code: '', durationDays: 30, maxUses: 1, priceYuan: 19.9, note: '' })

onMounted(() => {
  if (adminSecret.value) load()
})

async function load() {
  if (!adminSecret.value) return
  error.value = ''
  try {
    const [summaryData, codeData] = await Promise.all([
      getMonetizationSummary(adminSecret.value),
      listActivationCodes(adminSecret.value)
    ])
    summary.value = summaryData
    codes.value = codeData
  } catch (e) {
    error.value = e.message
  }
}

async function createCode() {
  error.value = ''
  message.value = ''
  creating.value = true
  try {
    const created = await createActivationCode(adminSecret.value, {
      code: form.code || null,
      durationDays: form.durationDays || 30,
      maxUses: form.maxUses || 1,
      priceYuan: form.priceYuan || 0,
      note: form.note || null
    })
    message.value = `已生成：${created.code}`
    form.code = ''
    await load()
  } catch (e) {
    error.value = e.message
  } finally {
    creating.value = false
  }
}

function saveSecret() {
  localStorage.setItem('adminSecret', adminSecret.value)
  load()
}

function clearSecret() {
  localStorage.removeItem('adminSecret')
  adminSecret.value = ''
  summary.value = null
  codes.value = []
}

async function copyCode(code) {
  await navigator.clipboard.writeText(code)
  message.value = `已复制：${code}`
}
</script>
