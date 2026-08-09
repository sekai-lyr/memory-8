<template>
  <main>
    <TopNav />
    <section class="page wide-page">
      <div class="toolbar hero-bar membership-hero">
        <div>
          <span class="eyebrow">高级会员</span>
          <h1>把背单词从“靠自觉”变成“有安排”</h1>
          <p class="muted">高级会员不只解锁容量，还提供每日清单、目标计划、模考冲刺、学习报告、听写训练、智能复习和表格批量导入。</p>
        </div>
        <div class="membership-status">
          <span class="badge" :class="{ pro: membership?.pro }">
            {{ membership?.pro ? `会员到期：${formatDate(membership.expireTime)}` : '当前普通用户' }}
          </span>
          <router-link class="button-link" to="/pro-center" v-if="membership?.pro">进入会员教练</router-link>
        </div>
      </div>

      <p class="error" v-if="error">{{ error }}</p>
      <p class="success" v-if="message">{{ message }}</p>

      <section class="membership-layout">
        <section class="panel conversion-panel">
          <div class="toolbar">
            <div>
              <h2>激活会员</h2>
              <p class="muted">收款后在变现后台生成激活码，用户在这里兑换即可开通。</p>
            </div>
            <span class="badge pro" v-if="membership?.pro">{{ membership.daysLeft }} 天</span>
          </div>
          <form class="form" @submit.prevent="redeem">
            <input v-model.trim="activationCode" placeholder="输入会员激活码" />
            <button class="primary" type="submit" :disabled="redeeming">
              {{ redeeming ? '兑换中...' : '兑换会员' }}
            </button>
          </form>
          <div class="pay-note">
            <strong>建议售卖方式</strong>
            <p>先用微信、支付宝或线下收款出售 30 天/365 天激活码；跑通付费后再接入自动支付。</p>
          </div>
        </section>

        <section class="panel">
          <div class="toolbar">
            <h2>会员状态</h2>
            <span class="badge" :class="{ pro: membership?.pro }">{{ membership?.planType || '-' }}</span>
          </div>
          <div class="status-grid">
            <span>状态：{{ membership?.status || '-' }}</span>
            <span>剩余：{{ membership?.daysLeft || 0 }} 天</span>
            <span>{{ membership?.pro ? '已解锁会员权益' : '普通用户容量限制中' }}</span>
            <span>自定义词：普通用户 20 个 / 会员不限</span>
          </div>
          <h3>已开放</h3>
          <ul class="feature-list">
            <li v-for="item in membership?.enabledFeatures || []" :key="item">{{ item }}</li>
          </ul>
          <h3 v-if="membership?.lockedFeatures?.length">升级后解锁</h3>
          <ul class="feature-list locked" v-if="membership?.lockedFeatures?.length">
            <li v-for="item in membership.lockedFeatures" :key="item">{{ item }}</li>
          </ul>
        </section>
      </section>

      <section class="pro-value-grid">
        <article class="value-card">
          <span>01</span>
          <h2>每日学习清单</h2>
          <p>每天自动列出该背什么、该复习什么，做完可以打勾，适合让用户持续回来。</p>
        </article>
        <article class="value-card">
          <span>02</span>
          <h2>专属学习报告</h2>
          <p>根据错题、复习状态和练习记录生成今日重点，让用户一打开就知道该学什么。</p>
        </article>
        <article class="value-card">
          <span>03</span>
          <h2>智能复习中心</h2>
          <p>按遗忘时间、错误次数和未来 7 天压力排序，用户每天照着做，不用自己判断。</p>
        </article>
        <article class="value-card">
          <span>04</span>
          <h2>文章生词解析</h2>
          <p>粘贴外刊、试卷阅读或网页英文，自动提取高价值生词，一键生成自己的学习包。</p>
        </article>
        <article class="value-card">
          <span>05</span>
          <h2>7 天复习计划</h2>
          <p>把到期复习、错题返工、自定义词库和周复盘排成连续任务，提升留存。</p>
        </article>
        <article class="value-card">
          <span>06</span>
          <h2>薄弱词优先级</h2>
          <p>把错得多、不会次数高的词排到前面，付费点从“词库更多”升级为“省时间”。</p>
        </article>
        <article class="value-card">
          <span>07</span>
          <h2>听写训练</h2>
          <p>隐藏单词只播放发音，让用户拼写英文，强化声音记忆和拼写准确度。</p>
        </article>
        <article class="value-card">
          <span>08</span>
          <h2>目标计划</h2>
          <p>按考试日期、每日时间和目标词量倒推出任务，让备考用户每天知道该做什么。</p>
        </article>
        <article class="value-card">
          <span>09</span>
          <h2>模考冲刺</h2>
          <p>自动组卷、限时答题、提交评分，错题回流复习队列，更适合考前付费场景。</p>
        </article>
      </section>

      <section class="panel pro-preview" v-if="membership?.pro && proReport">
        <div>
          <span class="badge pro">已解锁</span>
          <h2>{{ proReport.focusTitle }}</h2>
          <p class="muted">{{ proReport.focusDetail }}</p>
        </div>
        <div class="mini-stats">
          <span>稳定度 <strong>{{ proReport.retentionScore }}</strong></span>
          <span>今日目标 <strong>{{ proReport.dailyTarget }}</strong></span>
          <span>到期复习 <strong>{{ proReport.metrics.dueReviews }}</strong></span>
          <span>7 天题量 <strong>{{ proReport.metrics.sevenDayTotal }}</strong></span>
        </div>
      </section>

      <section class="plans-grid">
        <article class="panel plan-card" v-for="plan in plans" :key="plan.code">
          <div>
            <span class="badge">{{ plan.code }}</span>
            <h2>{{ plan.name }}</h2>
            <strong class="price">￥{{ plan.priceYuan }}</strong>
            <p class="muted">{{ plan.durationDays }} 天有效</p>
          </div>
          <ul class="feature-list">
            <li v-for="feature in plan.features" :key="feature">{{ feature }}</li>
          </ul>
        </article>
      </section>
    </section>
  </main>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { getMembership, getProDashboard, listMembershipPlans, redeemActivationCode } from '../api'
import TopNav from './components/TopNav.vue'

const membership = ref(null)
const plans = ref([])
const proReport = ref(null)
const activationCode = ref('')
const error = ref('')
const message = ref('')
const redeeming = ref(false)

onMounted(load)

async function load() {
  error.value = ''
  try {
    const [membershipData, planData] = await Promise.all([
      getMembership(),
      listMembershipPlans()
    ])
    membership.value = membershipData
    plans.value = planData
    if (membership.value.pro) {
      proReport.value = await getProDashboard()
    }
  } catch (e) {
    error.value = e.message
  }
}

async function redeem() {
  error.value = ''
  message.value = ''
  if (!activationCode.value) {
    error.value = '请输入激活码'
    return
  }
  redeeming.value = true
  try {
    membership.value = await redeemActivationCode(activationCode.value)
    activationCode.value = ''
    message.value = '会员已开通'
    if (membership.value.pro) {
      proReport.value = await getProDashboard()
    }
  } catch (e) {
    error.value = e.message
  } finally {
    redeeming.value = false
  }
}

function formatDate(value) {
  return value ? value.replace('T', ' ').slice(0, 16) : '-'
}
</script>
