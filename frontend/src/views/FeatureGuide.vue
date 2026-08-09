<template>
  <main>
    <TopNav />
    <section class="page wide-page">
      <div class="toolbar hero-bar guide-hero">
        <div>
          <span class="eyebrow">中文说明模式</span>
          <h1>不知道点哪个？看这里</h1>
          <p class="muted">这里把所有功能翻译成普通话：什么时候用、点进去能做什么、是不是会员功能。</p>
        </div>
        <div class="hero-actions">
          <router-link class="button-link" to="/practice">我先开始背词</router-link>
          <router-link class="ghost-link" to="/dashboard">回首页</router-link>
        </div>
      </div>

      <section class="panel guide-panel">
        <div class="toolbar">
          <h2>新手只按这 3 步走</h2>
          <span class="badge">最简单路线</span>
        </div>
        <div class="guide-steps">
          <router-link class="guide-step" to="/practice">
            <span>第 1 步</span>
            <strong>开始背词</strong>
            <p>不知道做什么就点这里。系统会出单词，你输入中文意思，错了会自动记下来。</p>
          </router-link>
          <router-link class="guide-step" to="/wrong-words">
            <span>第 2 步</span>
            <strong>看错题本</strong>
            <p>这里放你答错或不认识的词。每天先复习错题，比一直刷新词更有效。</p>
          </router-link>
          <router-link class="guide-step" to="/study-records">
            <span>第 3 步</span>
            <strong>看学习记录</strong>
            <p>这里看你背了多少、正确率多少。会员可以看到更完整的学习分析。</p>
          </router-link>
        </div>
      </section>

      <section class="guide-layout">
        <section class="panel">
          <div class="toolbar">
            <h2>普通功能</h2>
            <span class="muted">不花钱也能用</span>
          </div>
          <div class="guide-list">
            <router-link v-for="item in freeItems" :key="item.title" class="guide-row" :to="item.to">
              <div>
                <strong>{{ item.title }}</strong>
                <p>{{ item.when }}</p>
              </div>
              <span>{{ item.action }}</span>
            </router-link>
          </div>
        </section>

        <section class="panel">
          <div class="toolbar">
            <h2>会员功能</h2>
            <span class="badge pro">适合卖钱</span>
          </div>
          <div class="guide-list">
            <router-link v-for="item in paidItems" :key="item.title" class="guide-row paid" :to="item.to">
              <div>
                <strong>{{ item.title }}</strong>
                <p>{{ item.when }}</p>
              </div>
              <span>{{ item.action }}</span>
            </router-link>
          </div>
        </section>
      </section>

      <section class="panel guide-panel">
        <div class="toolbar">
          <h2>如果你要靠它赚钱</h2>
          <router-link class="button-link" to="/membership">去开通/兑换会员</router-link>
        </div>
        <div class="seller-guide">
          <div>
            <strong>普通用户看到的是学习工具</strong>
            <p>他们只需要：首页、开始背词、我的词库、学习记录。</p>
          </div>
          <div>
            <strong>付费用户买的是省时间</strong>
            <p>会员功能要主推：文章生词解析、智能复习中心、目标计划、模考冲刺、听写训练、学习报告。</p>
          </div>
          <div>
            <strong>你自己用变现后台</strong>
            <p>变现后台是给你生成激活码、看兑换次数和估算收入的，不建议普通学生使用。</p>
          </div>
        </div>
      </section>
    </section>
  </main>
</template>

<script setup>
import TopNav from './components/TopNav.vue'

const freeItems = [
  {
    title: '开始背词',
    when: '每天不知道做什么，就先点这里练 10 个词。',
    action: '去练习',
    to: '/practice'
  },
  {
    title: '公共词库',
    when: '想随便找单词、看中文意思、例句和发音时用。',
    action: '去看词',
    to: '/public-words'
  },
  {
    title: '我的词库',
    when: '想背自己的考试词、课堂词、工作词时，把词加到这里。',
    action: '去添加',
    to: '/user-words'
  },
  {
    title: '错题本',
    when: '答错的词会进这里。复习错题是提分最快的入口。',
    action: '去复习',
    to: '/wrong-words'
  },
  {
    title: '收藏单词',
    when: '看到重要单词，可以收藏起来，后面集中复习。',
    action: '去查看',
    to: '/favorite-words'
  }
]

const paidItems = [
  {
    title: '今日清单',
    when: '开通会员后每天先看这里，它会把今天该做的事列成待办清单。',
    action: '看清单',
    to: '/daily-plan'
  },
  {
    title: '会员学习教练',
    when: '系统告诉你今天最该学什么，适合每天打开先看一眼。',
    action: '看报告',
    to: '/pro-center'
  },
  {
    title: '智能复习中心',
    when: '想知道今天先复习哪些词、未来几天会不会堆词时用。',
    action: '看优先级',
    to: '/pro-review'
  },
  {
    title: '文章生词解析',
    when: '把试卷阅读、外刊、工作邮件粘进来，自动提取生词并生成学习包。',
    action: '解析文章',
    to: '/pro-reading'
  },
  {
    title: '目标计划',
    when: '有考试日期或学习目标时用，它会倒推出每天要背多少。',
    action: '设目标',
    to: '/pro-goal'
  },
  {
    title: '听写训练',
    when: '想练发音、拼写、听到单词能不能写出来时用。',
    action: '去听写',
    to: '/pro-dictation'
  },
  {
    title: '模考冲刺',
    when: '考前或阶段复盘时用，像考试一样限时做题，然后给分数。',
    action: '去模考',
    to: '/pro-exam'
  },
  {
    title: '开通会员',
    when: '输入激活码、查看会员还剩几天、看会员权益。',
    action: '去开通',
    to: '/membership'
  }
]
</script>
