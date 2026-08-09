import { createRouter, createWebHistory } from 'vue-router'
import AuthPage from '../views/AuthPage.vue'
import Dashboard from '../views/Dashboard.vue'
import FeatureGuide from '../views/FeatureGuide.vue'
import DailyPlan from '../views/DailyPlan.vue'
import PublicWords from '../views/PublicWords.vue'
import WordPractice from '../views/WordPractice.vue'
import WrongWords from '../views/WrongWords.vue'
import FavoriteWords from '../views/FavoriteWords.vue'
import UserWords from '../views/UserWords.vue'
import StudyRecords from '../views/StudyRecords.vue'
import ProCenter from '../views/ProCenter.vue'
import ProReview from '../views/ProReview.vue'
import ProReading from '../views/ProReading.vue'
import ProGoal from '../views/ProGoal.vue'
import ProDictation from '../views/ProDictation.vue'
import ProExam from '../views/ProExam.vue'
import Membership from '../views/Membership.vue'
import AdminMonetization from '../views/AdminMonetization.vue'

const routes = [
  { path: '/', redirect: '/dashboard' },
  { path: '/login', component: AuthPage },
  { path: '/register', component: AuthPage },
  { path: '/dashboard', component: Dashboard },
  { path: '/guide', component: FeatureGuide },
  { path: '/daily-plan', component: DailyPlan },
  { path: '/public-words', component: PublicWords },
  { path: '/practice', component: WordPractice },
  { path: '/wrong-words', component: WrongWords },
  { path: '/favorite-words', component: FavoriteWords },
  { path: '/user-words', component: UserWords },
  { path: '/study-records', component: StudyRecords },
  { path: '/pro-center', component: ProCenter },
  { path: '/pro-review', component: ProReview },
  { path: '/pro-reading', component: ProReading },
  { path: '/pro-goal', component: ProGoal },
  { path: '/pro-dictation', component: ProDictation },
  { path: '/pro-exam', component: ProExam },
  { path: '/membership', component: Membership },
  { path: '/admin-monetization', component: AdminMonetization }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

function hasAuthToken() {
  try {
    const raw = localStorage.getItem('user')
    if (raw && JSON.parse(raw)?.token) return true
  } catch (e) {
    return false
  }
  return Boolean(localStorage.getItem('token'))
}

router.beforeEach((to, from, next) => {
  const publicPages = ['/login', '/register']
  if (!publicPages.includes(to.path) && !hasAuthToken()) {
    next('/login')
    return
  }
  next()
})

export default router
