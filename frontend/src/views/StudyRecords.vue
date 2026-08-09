<template>
  <main>
    <TopNav />
    <section class="page wide-page">
      <div class="toolbar hero-bar compact-hero">
        <div>
          <h1>学习记录</h1>
          <p class="muted">汇总每轮练习、累计正确率和会员学习分析。</p>
        </div>
        <router-link class="button-link" to="/membership" v-if="membership && !isPro">开通会员</router-link>
      </div>
      <p class="error" v-if="error">{{ error }}</p>
      <p class="error" v-if="membership && !isPro">热力图、今日总结和状态分析是会员功能。</p>

      <section class="summary-grid">
        <div class="summary-card"><span>总轮数</span><strong>{{ records.length }}</strong></div>
        <div class="summary-card"><span>累计题数</span><strong>{{ totalCount }}</strong></div>
        <div class="summary-card"><span>累计正确</span><strong>{{ correctCount }}</strong></div>
        <div class="summary-card"><span>平均正确率</span><strong>{{ averageAccuracy }}%</strong></div>
      </section>

      <section class="records-layout">
        <div class="records-main">
          <section class="panel" v-if="isPro">
            <div class="toolbar">
              <h2>学习热力图</h2>
              <span class="muted">最近 30 天</span>
            </div>
            <div class="heatmap">
              <div
                v-for="day in heatmap"
                :key="day.date"
                class="heat-cell"
                :class="heatClass(day.totalCount)"
                :title="`${day.date}: ${day.totalCount} 题`"
              >
                <span>{{ new Date(day.date).getDate() }}</span>
              </div>
            </div>
          </section>

          <section class="panel" v-else>
            <h2>会员学习分析</h2>
            <p class="muted">升级后解锁热力图、今日总结、复习状态分布和长期学习趋势。</p>
            <router-link class="button-link" to="/membership">查看会员</router-link>
          </section>

          <section class="panel">
            <h2>每轮记录</h2>
            <p class="empty" v-if="!records.length">暂无学习记录。</p>
            <table v-else>
              <thead>
                <tr>
                  <th>时间</th>
                  <th>模式</th>
                  <th>总题数</th>
                  <th>正确</th>
                  <th>错误</th>
                  <th>正确率</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="item in records" :key="item.id">
                  <td>{{ item.createdTime?.replace('T', ' ') }}</td>
                  <td>{{ item.mode }}</td>
                  <td>{{ item.totalCount }}</td>
                  <td>{{ item.correctCount }}</td>
                  <td>{{ item.wrongCount }}</td>
                  <td>{{ item.accuracy }}%</td>
                </tr>
              </tbody>
            </table>
          </section>
        </div>

        <aside class="records-side" v-if="isPro">
          <section class="panel side-section">
            <h2>今日学习总结</h2>
            <div class="mini-stats" v-if="dailySummary">
              <span>完成 <strong>{{ dailySummary.totalCount }}</strong></span>
              <span>正确 <strong>{{ dailySummary.correctCount }}</strong></span>
              <span>错误 <strong>{{ dailySummary.wrongCount }}</strong></span>
              <span>正确率 <strong>{{ dailySummary.accuracy }}%</strong></span>
            </div>
            <p class="success" v-if="dailySummary?.suggestion">{{ dailySummary.suggestion }}</p>
          </section>

          <section class="panel side-section">
            <h2>学习状态分布</h2>
            <div class="status-grid">
              <span>到期复习：{{ dailySummary?.statusSummary?.DUE || 0 }}</span>
              <span>学习中：{{ dailySummary?.statusSummary?.LEARNING || 0 }}</span>
              <span>不认识：{{ dailySummary?.statusSummary?.UNKNOWN || 0 }}</span>
              <span>高频错误：{{ dailySummary?.statusSummary?.HIGH_WRONG || 0 }}</span>
              <span>已掌握：{{ dailySummary?.statusSummary?.MASTERED || 0 }}</span>
            </div>
          </section>
        </aside>

        <aside class="records-side" v-else>
          <section class="panel side-section">
            <h2>会员卖点</h2>
            <p class="muted">学习分析是最容易让用户付费的功能：它能证明进步，也能帮机构展示学习结果。</p>
          </section>
        </aside>
      </section>
    </section>
  </main>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { getDailySummary, getMembership, getStudyHeatmap, listStudyRecords } from '../api'
import TopNav from './components/TopNav.vue'

const records = ref([])
const dailySummary = ref(null)
const heatmap = ref([])
const error = ref('')
const membership = ref(null)
const isPro = computed(() => Boolean(membership.value?.pro))
const totalCount = computed(() => records.value.reduce((sum, item) => sum + Number(item.totalCount || 0), 0))
const correctCount = computed(() => records.value.reduce((sum, item) => sum + Number(item.correctCount || 0), 0))
const averageAccuracy = computed(() => totalCount.value === 0 ? '0.00' : ((correctCount.value / totalCount.value) * 100).toFixed(2))

onMounted(load)

async function load() {
  try {
    membership.value = await getMembership()
    records.value = await listStudyRecords()
    if (isPro.value) {
      const [summaryData, heatmapData] = await Promise.all([
        getDailySummary(),
        getStudyHeatmap(30)
      ])
      dailySummary.value = summaryData
      heatmap.value = heatmapData
    }
  } catch (e) {
    error.value = e.message
  }
}

function heatClass(total) {
  if (!total) return 'level-0'
  if (total < 10) return 'level-1'
  if (total < 20) return 'level-2'
  if (total < 40) return 'level-3'
  return 'level-4'
}
</script>
