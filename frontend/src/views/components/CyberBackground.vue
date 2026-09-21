<template>
  <div class="cbg" aria-hidden="true">
    <div class="cbg-field cbg-field-one"></div>
    <div class="cbg-field cbg-field-two"></div>
    <div class="cbg-line cbg-line-one"></div>
    <div class="cbg-line cbg-line-two"></div>
    <div class="cbg-vig"></div>
  </div>
</template>

<style scoped>
.cbg {
  position: fixed;
  inset: 0;
  z-index: 0;
  pointer-events: none;
  overflow: hidden;
  background:
    radial-gradient(ellipse at 10% 10%, rgba(98, 224, 192, .1), transparent 28%),
    radial-gradient(ellipse at 92% 78%, rgba(93, 122, 170, .13), transparent 32%),
    linear-gradient(180deg, #0a1020 0%, #0c1527 54%, #080d19 100%);
}
.cbg-field {
  position: absolute;
  width: 42vw;
  height: 42vw;
  max-width: 640px;
  max-height: 640px;
  border-radius: 50%;
  filter: blur(60px);
  opacity: .2;
  animation: cbgDrift 18s ease-in-out infinite alternate;
}
.cbg-field-one { top: -24%; left: -14%; background: rgba(98, 224, 192, .18); }
.cbg-field-two { right: -18%; bottom: -22%; background: rgba(62, 92, 146, .22); animation-delay: -8s; }
.cbg-line {
  position: absolute;
  width: 58vw;
  height: 1px;
  background: linear-gradient(90deg, transparent, rgba(160, 205, 199, .18), transparent);
  transform: rotate(-18deg);
  opacity: .4;
}
.cbg-line-one { top: 22%; left: -14%; }
.cbg-line-two { bottom: 18%; right: -16%; transform: rotate(16deg); opacity: .22; }
.cbg-vig {
  position: absolute;
  inset: 0;
  background:
    linear-gradient(180deg, rgba(8, 13, 25, .08), transparent 34%, rgba(8, 13, 25, .28)),
    radial-gradient(ellipse at 50% 50%, transparent 42%, rgba(4, 7, 14, .46) 100%);
}
@keyframes cbgDrift { from { transform: translate3d(-2%, -1%, 0); } to { transform: translate3d(4%, 3%, 0); } }

@media (prefers-reduced-motion: reduce) {
  .cbg-field {
    animation: none;
  }
}
</style>
