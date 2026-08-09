<template>
  <div class="cbg" aria-hidden="true">
    <canvas ref="cvs" class="cbg-canvas"></canvas>
    <div class="cbg-stage">
      <span></span>
      <span></span>
      <span></span>
    </div>
    <div class="cbg-eq">
      <span
        v-for="bar in bars"
        :key="bar.index"
        :style="{ '--h': bar.height, '--d': bar.delay }"
      ></span>
    </div>
    <div class="cbg-grid"></div>
    <div class="cbg-wave"></div>
    <div class="cbg-scan"></div>
    <div class="cbg-vig"></div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'

const bars = Array.from({ length: 28 }, (_, index) => ({
  index,
  height: `${28 + (index % 7) * 9}px`,
  delay: `${(index % 9) * -0.13}s`
}))

const cvs = ref(null)
let raf = null
let pts = []
let W = 0, H = 0
let mx = 0, my = 0, smx = 0, smy = 0

class Pt {
  constructor() { this.init(true) }
  init(first) {
    this.x = Math.random() * W
    this.y = first ? Math.random() * H : -8
    this.r = Math.random() * 1.5 + .4
    this.vy = Math.random() * .28 + .05
    this.vx = (Math.random() - .5) * .15
    this.a = Math.random() * .32 + .04
    this.c = Math.random() > .84 ? '255,79,216' : '0,245,224'
  }
  upd() {
    this.y += this.vy
    this.x += this.vx + (smx - W/2) * .00003
    if (this.y > H + 12 || this.x < -12 || this.x > W + 12) this.init(false)
  }
  draw(ctx) {
    ctx.beginPath()
    ctx.moveTo(this.x, this.y - this.r * 8)
    ctx.lineTo(this.x + this.vx * 22, this.y)
    ctx.strokeStyle = `rgba(${this.c},${(this.a * .32).toFixed(2)})`
    ctx.lineWidth = 1
    ctx.stroke()
    ctx.beginPath(); ctx.arc(this.x, this.y, this.r, 0, Math.PI*2)
    ctx.fillStyle = `rgba(${this.c},${this.a.toFixed(2)})`; ctx.fill()
  }
}

onMounted(() => {
  const el = cvs.value; if (!el) return
  const ctx = el.getContext('2d')
  const dpr = Math.min(devicePixelRatio || 1, 2)

  function rs() {
    W = innerWidth; H = innerHeight
    el.width = W * dpr; el.height = H * dpr
    el.style.width = W + 'px'; el.style.height = H + 'px'
    ctx.setTransform(dpr, 0, 0, dpr, 0, 0)
    pts = Array.from({ length: 70 }, () => new Pt())
  }
  rs(); addEventListener('resize', rs)

  function frame() {
    smx += (mx - smx) * .04; smy += (my - smy) * .04
    ctx.clearRect(0, 0, W, H)
    for (const p of pts) { p.upd(); p.draw(ctx) }
    raf = requestAnimationFrame(frame)
  }
  frame()

  const mv = e => { mx = e.clientX; my = e.clientY }
  addEventListener('mousemove', mv, { passive: true })
  addEventListener('touchmove', e => { mx = e.touches[0].clientX; my = e.touches[0].clientY }, { passive: true })

  onUnmounted(() => { cancelAnimationFrame(raf); removeEventListener('resize', rs); removeEventListener('mousemove', mv) })
})
</script>

<style scoped>
.cbg {
  position: fixed;
  inset: 0;
  z-index: 0;
  pointer-events: none;
  overflow: hidden;
  background:
    radial-gradient(ellipse at 20% 14%, rgba(0,245,224,.15), transparent 35%),
    radial-gradient(ellipse at 82% 74%, rgba(255,79,216,.1), transparent 34%),
    linear-gradient(180deg, #050712 0%, #081225 46%, #040712 100%);
}
.cbg-canvas { position: absolute; inset: 0; z-index: 2; }

.cbg-stage {
  position: absolute;
  inset: 0;
  z-index: 0;
}

.cbg-stage span {
  position: absolute;
  width: 46vw;
  height: 2px;
  left: -12vw;
  background: linear-gradient(90deg, transparent, rgba(0,245,224,.34), rgba(255,79,216,.22), transparent);
  box-shadow: 0 0 22px rgba(0,245,224,.18);
  transform: rotate(-18deg);
  animation: stageSweep 9s linear infinite;
}

.cbg-stage span:nth-child(1) { top: 18%; animation-delay: -3s; }
.cbg-stage span:nth-child(2) { top: 52%; left: 56vw; transform: rotate(18deg); animation-delay: -6s; }
.cbg-stage span:nth-child(3) { top: 76%; width: 60vw; animation-delay: -1s; }

.cbg-grid {
  position: absolute; inset: 0; z-index: 1;
  background-image:
    linear-gradient(rgba(0,245,224,.04) 1px, transparent 1px),
    linear-gradient(90deg, rgba(0,245,224,.04) 1px, transparent 1px),
    linear-gradient(rgba(255,79,216,.018) 1px, transparent 1px);
  background-size: 54px 54px, 54px 54px, 18px 18px;
  transform-origin: 50% 100%;
  transform: perspective(620px) rotateX(58deg) translateY(12vh) scale(1.3);
  animation: gridPulse 7s ease-in-out infinite;
  mask-image: linear-gradient(to bottom, transparent 0%, #000 30%, #000 74%, transparent 100%);
  -webkit-mask-image: linear-gradient(to bottom, transparent 0%, #000 30%, #000 74%, transparent 100%);
}

.cbg-eq {
  position: absolute;
  right: 4vw;
  bottom: 6vh;
  z-index: 2;
  display: flex;
  align-items: end;
  gap: 5px;
  height: 96px;
  opacity: .46;
}

.cbg-eq span {
  width: 4px;
  height: var(--h);
  border-radius: 999px;
  background: linear-gradient(180deg, rgba(255,79,216,.92), rgba(0,245,224,.72));
  box-shadow: 0 0 14px rgba(0,245,224,.25);
  animation: equalize .9s ease-in-out infinite alternate;
  animation-delay: var(--d);
}

.cbg-wave {
  position: absolute;
  left: 0;
  right: 0;
  top: 50%;
  z-index: 1;
  height: 160px;
  opacity: .16;
  background:
    repeating-linear-gradient(90deg,
      transparent 0 18px,
      rgba(0,245,224,.32) 18px 20px,
      transparent 20px 38px),
    linear-gradient(90deg, transparent, rgba(255,79,216,.28), transparent);
  transform: skewY(-8deg);
  animation: waveSlide 6s linear infinite;
}

.cbg-scan {
  position: absolute;
  inset: 0;
  z-index: 4;
  opacity: .12;
  background:
    repeating-linear-gradient(180deg, transparent 0 7px, rgba(255,255,255,.18) 7px 8px),
    linear-gradient(90deg, transparent, rgba(0,245,224,.2), transparent);
  mix-blend-mode: screen;
  animation: scanMove 8s linear infinite;
}

.cbg-vig {
  position: absolute;
  inset: 0;
  z-index: 5;
  background:
    linear-gradient(180deg, rgba(5,7,18,.18), transparent 38%, rgba(5,7,18,.34)),
    radial-gradient(ellipse at 50% 50%, transparent 48%, rgba(5,7,18,.64) 100%);
}

@keyframes equalize {
  0% { transform: scaleY(.45); opacity: .45; }
  100% { transform: scaleY(1.32); opacity: .95; }
}

@keyframes gridPulse {
  0%, 100% { opacity: .62; filter: hue-rotate(0deg); }
  50% { opacity: .86; filter: hue-rotate(18deg); }
}

@keyframes waveSlide {
  from { background-position: 0 0, 0 0; }
  to { background-position: 220px 0, 0 0; }
}

@keyframes scanMove {
  from { background-position: 0 0, -40vw 0; }
  to { background-position: 0 90px, 120vw 0; }
}

@keyframes stageSweep {
  0% { transform: translateX(-16vw) rotate(-18deg); opacity: 0; }
  18% { opacity: .75; }
  100% { transform: translateX(126vw) rotate(-18deg); opacity: 0; }
}

@media (prefers-reduced-motion: reduce) {
  .cbg-stage span,
  .cbg-grid,
  .cbg-eq span,
  .cbg-wave,
  .cbg-scan {
    animation: none;
  }
}
</style>
