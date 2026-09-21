<template>
  <!--
    THESIS: SEKAI turns vocabulary practice into a clear next action, not a noisy dashboard.
    OWN-WORLD: midnight ink surfaces, paper-like dividers, and one mint signal accent.
    STORY: sign in, see the next useful study move, then practice or review with less guesswork.
    FIRST VIEWPORT: the brand and next action sit above the fold, with the study image anchoring the entry surface.
    FORM: user-pinned midnight study workspace, informed by seed c7a987b3 and built code-first.
    FINISH: unreviewed and undocumented is unfinished; this build ends with the finish review, the verdict, DESIGN.md, and every shipping raster carrying its provenance.
  -->
  <CyberBackground />
  <div class="app-shell">
    <RouterView v-slot="{ Component, route }">
      <Transition name="page-shell" mode="out-in">
        <component :is="Component" :key="route.fullPath" />
      </Transition>
    </RouterView>
  </div>
</template>

<script setup>
import { nextTick, onMounted, onUnmounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { stagger, waapi } from 'animejs'
import CyberBackground from './views/components/CyberBackground.vue'

const route = useRoute()
let entryAnimation = null
let revealObserver = null
const revealAnimations = new Set()

// These are the shared visual landmarks used across the app. Keeping the
// motion here means every route gets the same rhythm without every page
// needing its own animation component.
const revealSelector = [
  '.hero-bar', '.auth-hero', '.auth-card', '.auth-ritual',
  '.summary-card', '.panel', '.task-item', '.quick-guide-item', '.flow-item',
  '.guide-step', '.guide-row', '.daily-task', '.review-bucket', '.review-day',
  '.plan-step', '.mission-card', '.milestone-card', '.value-card', '.plan-card'
].join(', ')

function cancelMotion() {
  entryAnimation?.cancel()
  entryAnimation = null
  revealAnimations.forEach(animation => animation.cancel())
  revealAnimations.clear()
  revealObserver?.disconnect()
  revealObserver = null
}

function showWithoutMotion(nodes) {
  nodes.forEach(node => {
    node.style.opacity = '1'
    node.style.transform = 'none'
  })
}

function animateReveal(nodes) {
  if (!nodes.length) return
  const animation = waapi.animate(nodes, {
    opacity: [0, 1],
    translateY: [18, 0],
    delay: stagger(70),
    duration: 560,
    ease: 'outExpo'
  })
  revealAnimations.add(animation)
}

async function revealRoute() {
  await nextTick()
  cancelMotion()
  const target = document.querySelector('.page, .auth-layout')
  if (!target) return

  if (window.matchMedia('(prefers-reduced-motion: reduce)').matches) {
    target.style.opacity = '1'
    target.style.transform = 'none'
    showWithoutMotion([...target.querySelectorAll(revealSelector)])
    return
  }

  const nodes = [...target.querySelectorAll(revealSelector)]
  nodes.forEach(node => {
    node.style.opacity = '0'
    node.style.transform = 'translateY(18px)'
  })

  entryAnimation = waapi.animate(target, {
    opacity: [0.96, 1],
    translateY: [14, 0],
    duration: 650,
    ease: 'outExpo'
  })

  if (!('IntersectionObserver' in window)) {
    showWithoutMotion(nodes)
    return
  }

  const observer = new IntersectionObserver(entries => {
    const visibleNodes = entries
      .filter(entry => entry.isIntersecting)
      .map(entry => entry.target)

    if (!visibleNodes.length) return
    visibleNodes.forEach(node => observer.unobserve(node))
    animateReveal(visibleNodes)
  }, { threshold: 0.14, rootMargin: '0px 0px -8% 0px' })

  revealObserver = observer
  nodes.forEach(node => observer.observe(node))
}

onMounted(revealRoute)
watch(() => route.fullPath, revealRoute)
onUnmounted(cancelMotion)
</script>

<style>
.app-shell {
  position: relative;
  z-index: 1;
  min-height: 100dvh;
}

.page-shell-enter-active,
.page-shell-leave-active {
  transition: opacity 180ms ease;
}

.page-shell-enter-from,
.page-shell-leave-to {
  opacity: 0;
}

@media (prefers-reduced-motion: reduce) {
  .page-shell-enter-active,
  .page-shell-leave-active {
    transition: none;
  }
}
</style>
