import request from './request'

export const register = data => request.post('/auth/register', data)
export const login = data => request.post('/auth/login', data)

export const getMembership = () => request.get('/membership')
export const listMembershipPlans = () => request.get('/membership/plans')
export const redeemActivationCode = code => request.post('/membership/redeem', { code })
export const getProDashboard = () => request.get('/pro/dashboard')
export const getProReview = () => request.get('/pro-review')
export const analyzeProReading = data => request.post('/pro-reading/analyze', data)
export const importProReadingWords = data => request.post('/pro-reading/import', data)
export const getProDictationWords = count => request.get('/pro/dictation', { params: { count } })
export const getProGoal = () => request.get('/pro-goal')
export const saveProGoal = data => request.post('/pro-goal', data)
export const getProExamPaper = count => request.get('/pro-exam/paper', { params: { count } })
export const submitProExam = data => request.post('/pro-exam/submit', data)
export const getDailyPlan = () => request.get('/daily-plan')
export const updateDailyTask = (taskId, completed = true) => request.post(`/daily-plan/tasks/${taskId}`, { completed })
export const getMonetizationSummary = adminSecret => request.get('/admin/activation-codes/summary', adminHeaders(adminSecret))
export const listActivationCodes = adminSecret => request.get('/admin/activation-codes', adminHeaders(adminSecret))
export const createActivationCode = (adminSecret, data) => request.post('/admin/activation-codes', data, adminHeaders(adminSecret))

function adminHeaders(adminSecret) {
  return { headers: { 'X-Admin-Secret': adminSecret } }
}

export const getRandomWord = category => request.get('/words/random', { params: { category } })
export const getRandomWords = (count, category) => request.get('/words/random-list', { params: { count, category } })
export const getSmartQueue = count => request.get('/words/smart-queue', { params: { count } })
export const getWordDetail = (wordId, wordType) => request.get('/words/detail', { params: { wordId, wordType } })
export const checkAnswer = data => request.post('/words/check', data)
export const searchWords = (keyword, limit = 300) => request.get('/words/search', { params: { keyword, limit } })
export const listWords = (category, limit = 300) => request.get('/words/list', { params: { category, limit } })
export const listWordCategories = () => request.get('/words/categories')
export const getWordStats = () => request.get('/words/stats')

export const listWrongWords = () => request.get('/wrong-words')
export const addWrongWord = data => request.post('/wrong-words', data)
export const deleteWrongWord = id => request.delete(`/wrong-words/${id}`)
export const clearWrongWords = () => request.delete('/wrong-words/clear')

export const listFavoriteWords = () => request.get('/favorite-words')
export const addFavoriteWord = data => request.post('/favorite-words', data)
export const deleteFavoriteWord = id => request.delete(`/favorite-words/${id}`)
export const clearFavoriteWords = () => request.delete('/favorite-words/clear')

export const listMasteredWordKeys = () => request.get('/mastered-words/keys')
export const addMasteredWord = data => request.post('/mastered-words', data)
export const deleteMasteredWord = (wordId, wordType) => request.delete('/mastered-words', { params: { wordId, wordType } })
export const recordLearningStatus = data => request.post('/learning-status', data)
export const getLearningStatusSummary = () => request.get('/learning-status/summary')

export const listUserWords = keyword => request.get('/user-words', { params: { keyword } })
export const addUserWord = data => request.post('/user-words', data)
export const updateUserWord = (id, data) => request.put(`/user-words/${id}`, data)
export const deleteUserWord = id => request.delete(`/user-words/${id}`)
export const importUserWords = file => {
  const form = new FormData()
  form.append('file', file)
  return request.post('/user-words/import', form)
}

export const listStudyRecords = () => request.get('/study-records')
export const addStudyRecord = data => request.post('/study-records', data)
export const getDailySummary = () => request.get('/study-records/daily-summary')
export const getStudyHeatmap = days => request.get('/study-records/heatmap', { params: { days } })
