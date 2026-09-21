<template>
  <nav class="top-nav" aria-label="主导航">
    <div class="nav-inner">
      <router-link class="brand" to="/dashboard">
        <span class="brand-mark" aria-hidden="true">SK</span>
        <span class="brand-copy"><strong>SEKAI</strong><small>词汇学习</small></span>
      </router-link>

      <div id="primary-navigation" class="nav-links nav-primary" :class="{ 'is-open': menuOpen }">
        <router-link to="/dashboard" @click="menuOpen = false">首页</router-link>
        <router-link to="/daily-plan" @click="menuOpen = false">今日清单</router-link>
        <router-link to="/practice" @click="menuOpen = false">开始背词</router-link>
        <router-link to="/public-words" @click="menuOpen = false">公共词库</router-link>
        <router-link to="/user-words" @click="menuOpen = false">我的词库</router-link>
        <router-link to="/study-records" @click="menuOpen = false">学习记录</router-link>
      </div>

      <div class="nav-actions">
        <button
          class="nav-toggle"
          type="button"
          :aria-expanded="menuOpen"
          aria-controls="primary-navigation"
          @click="menuOpen = !menuOpen"
        >
          <span class="nav-toggle-lines" aria-hidden="true"><i></i><i></i></span>
          菜单
        </button>
        <details class="nav-menu">
          <summary>更多</summary>
          <div class="nav-menu-panel">
            <router-link to="/guide">功能说明</router-link>
            <router-link to="/pro-center">会员教练</router-link>
            <router-link to="/pro-review">复习中心</router-link>
            <router-link to="/pro-reading">文章解析</router-link>
            <router-link to="/pro-goal">目标计划</router-link>
            <router-link to="/pro-dictation">听写训练</router-link>
            <router-link to="/pro-exam">模考冲刺</router-link>
            <router-link to="/admin-monetization">变现后台</router-link>
          </div>
        </details>
        <router-link class="nav-pro" to="/membership" @click="menuOpen = false">开通会员</router-link>
      </div>
    </div>
  </nav>
</template>

<script setup>
import { ref, watch } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()
const menuOpen = ref(false)

watch(() => route.fullPath, () => {
  menuOpen.value = false
})
</script>
