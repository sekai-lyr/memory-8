<template>
  <main>
    <TopNav />
    <section class="page wide-page">
      <div class="toolbar hero-bar daily-hero">
        <div>
          <h1>今天照着清单做</h1>
          <p class="muted">系统把背词、错题、目标计划、听写和记录整理成一张清单。做完一项就打勾。</p>
        </div>
        <div class="hero-actions">
          <router-link class="button-link" to="/practice">先背词</router-link>
          <router-link class="ghost-link" to="/guide">看说明</router-link>
        </div>
      </div>

      <p class="error" v-if="error">{{ error }}</p>

      <section class="panel pro-lock-panel" v-if="membership && !membership.pro">
        <div>
          <span class="badge">会员专属</span>
          <h2>每日清单需要会员</h2>
          <p class="muted">它会把每天该做的事直接列出来，适合不想自己安排学习顺序的用户。</p>
        </div>
        <router-link class="button-link" to="/membership">开通会员</router-link>
      </section>

      <template v-if="membership?.pro && plan">
        <section class="summary-grid daily-summary">
          <div class="summary-card"><span>今日任务</span><strong>{{ plan.totalTasks }}</strong><small>{{ plan.taskDate }}</small></div>
          <div class="summary-card"><span>已完成</span><strong>{{ plan.completedTasks }}</strong><small>{{ plan.completionRate }}%</small></div>
          <div class="summary-card"><span>连续天数</span><strong>{{ plan.streakDays }}</strong><small>天</small></div>
          <div class="summary-card"><span>下一步</span><strong>{{ nextTaskIndex }}</strong><small>未完成任务序号</small></div>
        </section>

        <section class="daily-layout">
          <div class="daily-main">
            <section class="panel daily-progress-panel">
              <div class="toolbar">
                <div>
                  <h2>今日进度</h2>
                  <p class="muted">{{ plan.suggestion }}</p>
                </div>
                <span class="badge pro">{{ plan.completionRate }}%</span>
              </div>
              <div class="progress daily-progress">
                <div :style="{ width: plan.completionRate + '%' }"></div>
              </div>
            </section>

            <section class="panel">
              <div class="toolbar">
                <h2>任务清单</h2>
                <span class="muted">点勾选保存完成状态</span>
              </div>
              <div class="daily-task-list">
                <article v-for="(task, index) in plan.tasks" :key="task.id" class="daily-task" :class="{ done: task.completed }">
                  <button class="check-button" @click="toggleTask(task)" :disabled="savingId === task.id">
                    {{ task.completed ? '✓' : index + 1 }}
                  </button>
                  <div>
                    <span>{{ task.targetCount }} 项</span>
                    <strong>{{ task.title }}</strong>
                    <p>{{ task.detail }}</p>
                  </div>
                  <router-link class="ghost-link" :to="task.route">{{ task.completed ? '再看一次' : '去完成' }}</router-link>
                </article>
              </div>
            </section>
          </div>

          <aside class="daily-side">
            <section class="panel side-section">
              <h2>怎么用</h2>
              <div class="privilege-list vertical-pills">
                <span>每天打开这个页面</span>
                <span>按顺序完成任务</span>
                <span>做完回来打勾</span>
                <span>连续天数会自动统计</span>
              </div>
            </section>

            <section class="panel side-section">
              <h2>为什么适合会员</h2>
              <p class="muted">普通背词软件只给按钮，用户不知道先做什么。每日清单直接给学习顺序，更容易让用户觉得“有人在带我学”。</p>
            </section>
          </aside>
        </section>
      </template>
    </section>
  </main>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { getDailyPlan, getMembership, updateDailyTask } from '../api'
import TopNav from './components/TopNav.vue'

const membership = ref(null)
const plan = ref(null)
const error = ref('')
const savingId = ref(null)

const nextTaskIndex = computed(() => {
  const index = plan.value?.tasks?.findIndex(item => !item.completed) ?? -1
  return index < 0 ? '完成' : index + 1
})

onMounted(load)

async function load() {
  error.value = ''
  try {
    membership.value = await getMembership()
    if (membership.value.pro) {
      plan.value = await getDailyPlan()
    }
  } catch (e) {
    error.value = e.message
  }
}

async function toggleTask(task) {
  savingId.value = task.id
  error.value = ''
  try {
    plan.value = await updateDailyTask(task.id, !task.completed)
  } catch (e) {
    error.value = e.message
  } finally {
    savingId.value = null
  }
}
</script>
