export function canSpeak() {
  return typeof window !== 'undefined' && 'speechSynthesis' in window
}

export function speakWord(word) {
  if (!word || !canSpeak()) return false
  window.speechSynthesis.cancel()
  const utterance = new SpeechSynthesisUtterance(word)
  utterance.lang = 'en-US'
  utterance.rate = 0.86
  utterance.pitch = 1
  window.speechSynthesis.speak(utterance)
  return true
}

export function stopSpeaking() {
  if (canSpeak()) window.speechSynthesis.cancel()
}

