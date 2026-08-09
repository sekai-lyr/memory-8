<template>
  <CyberBackground />
  <div class="app-shell">
    <router-view />
  </div>
</template>

<script setup>
import CyberBackground from './views/components/CyberBackground.vue'
</script>

<style>
.app-shell {
  position: relative;
  z-index: 1;
  min-height: 100vh;
}
</style>
