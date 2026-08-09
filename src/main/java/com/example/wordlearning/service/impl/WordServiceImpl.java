package com.example.wordlearning.service.impl;

import com.example.wordlearning.dataobject.LearningStatusDO;
import com.example.wordlearning.dataobject.UserWordDO;
import com.example.wordlearning.dataobject.WordDO;
import com.example.wordlearning.model.CheckAnswerRequest;
import com.example.wordlearning.model.CheckAnswerResponse;
import com.example.wordlearning.model.RandomWordResponse;
import com.example.wordlearning.model.WordCategoryStatsResponse;
import com.example.wordlearning.model.WordDetailResponse;
import com.example.wordlearning.model.WordResponse;
import com.example.wordlearning.model.WordStatsResponse;
import com.example.wordlearning.repository.LearningStatusRepository;
import com.example.wordlearning.repository.MasteredWordRepository;
import com.example.wordlearning.repository.UserWordRepository;
import com.example.wordlearning.repository.WordRepository;
import com.example.wordlearning.repository.WrongWordRepository;
import com.example.wordlearning.service.MembershipService;
import com.example.wordlearning.service.WordService;
import com.example.wordlearning.util.AnswerJudgeUtil;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
public class WordServiceImpl implements WordService {
    private final WordRepository wordRepository;
    private final UserWordRepository userWordRepository;
    private final LearningStatusRepository learningStatusRepository;
    private final MasteredWordRepository masteredWordRepository;
    private final WrongWordRepository wrongWordRepository;
    private final MembershipService membershipService;

    public WordServiceImpl(WordRepository wordRepository, UserWordRepository userWordRepository,
                           LearningStatusRepository learningStatusRepository,
                           MasteredWordRepository masteredWordRepository,
                           WrongWordRepository wrongWordRepository,
                           MembershipService membershipService) {
        this.wordRepository = wordRepository;
        this.userWordRepository = userWordRepository;
        this.learningStatusRepository = learningStatusRepository;
        this.masteredWordRepository = masteredWordRepository;
        this.wrongWordRepository = wrongWordRepository;
        this.membershipService = membershipService;
    }

    @Override
    public RandomWordResponse randomWord(String category) {
        WordDO wordDO = hasText(category) ? wordRepository.findRandomOneByCategory(category.trim()) : wordRepository.findRandomOne();
        if (wordDO == null) {
            throw new IllegalStateException("公共词库为空，请先导入单词");
        }
        return new RandomWordResponse(wordDO.getId(), wordDO.getEnglish(), "public");
    }

    @Override
    public List<RandomWordResponse> randomWords(int count, String category) {
        int limit = Math.max(1, count);
        List<WordDO> words = hasText(category)
                ? wordRepository.findRandomListByCategory(category.trim(), limit)
                : wordRepository.findRandomList(limit);
        return words.stream()
                .map(wordDO -> new RandomWordResponse(wordDO.getId(), wordDO.getEnglish(), "public"))
                .toList();
    }

    @Override
    public List<RandomWordResponse> smartQueue(Long userId, int count) {
        membershipService.requirePro(userId, "智能复习队列");
        return learningStatusRepository.findByUserIdOrderByUpdatedTimeDesc(userId).stream()
                .filter(item -> !"MASTERED".equals(item.getStatus()))
                .sorted((a, b) -> Integer.compare(priority(b), priority(a)))
                .limit(Math.max(1, count))
                .map(this::toQueueResponse)
                .toList();
    }

    @Override
    public CheckAnswerResponse checkAnswer(CheckAnswerRequest request) {
        String wordType = request.getWordType() == null ? "public" : request.getWordType();
        String english;
        String chinese;
        if ("user".equalsIgnoreCase(wordType)) {
            UserWordDO userWordDO = userWordRepository.findById(request.getWordId())
                    .orElseThrow(() -> new IllegalArgumentException("单词不存在"));
            english = userWordDO.getEnglish();
            chinese = userWordDO.getChinese();
            wordType = "user";
        } else {
            WordDO wordDO = wordRepository.findById(request.getWordId())
                    .orElseThrow(() -> new IllegalArgumentException("单词不存在"));
            english = wordDO.getEnglish();
            chinese = wordDO.getChinese();
            wordType = "public";
        }
        boolean correct = AnswerJudgeUtil.isCorrect(request.getAnswer(), chinese);
        String message = correct ? "回答正确！" : "回答错误，正确答案是：" + chinese;
        return new CheckAnswerResponse(correct, message, chinese, request.getWordId(), wordType, english);
    }

    @Override
    public List<WordResponse> search(String keyword, Long userId, int limit) {
        if (!hasText(keyword)) {
            return List.of();
        }
        String trimmed = keyword.trim();
        PageRequest pageRequest = PageRequest.of(0, safeLimit(limit, 500));
        boolean hasChinese = trimmed.codePoints().anyMatch(code -> Character.UnicodeScript.of(code) == Character.UnicodeScript.HAN);
        List<WordResponse> result = new ArrayList<>();
        List<WordDO> publicWords = hasChinese
                ? wordRepository.findByChineseContaining(trimmed, pageRequest)
                : wordRepository.findByEnglishIgnoreCaseContaining(trimmed, pageRequest);
        publicWords.forEach(wordDO -> result.add(toResponse(wordDO)));
        if (userId != null) {
            userWordRepository.findByUserIdAndEnglishIgnoreCaseContainingOrUserIdAndChineseContaining(userId, trimmed, userId, trimmed)
                    .forEach(wordDO -> result.add(toResponse(wordDO)));
        }
        return result;
    }

    @Override
    public List<WordResponse> list(String category, int limit) {
        PageRequest pageRequest = PageRequest.of(0, safeLimit(limit, 500));
        if (hasText(category)) {
            return wordRepository.findByCategoryOrderByEnglishAsc(category.trim(), pageRequest).stream().map(this::toResponse).toList();
        }
        return wordRepository.findAllByOrderByEnglishAsc(pageRequest).stream().map(this::toResponse).toList();
    }

    @Override
    public List<String> categories() {
        return wordRepository.findCategories();
    }

    @Override
    public WordStatsResponse stats() {
        List<WordCategoryStatsResponse> categories = wordRepository.countByCategory().stream()
                .map(row -> new WordCategoryStatsResponse(formatCategory(row[0]), (Long) row[1]))
                .toList();
        return new WordStatsResponse(wordRepository.count(), categories);
    }

    @Override
    public WordDetailResponse detail(Long userId, Long wordId, String wordType) {
        String type = wordType == null ? "public" : wordType;
        WordResponse word = "user".equalsIgnoreCase(type)
                ? userWordRepository.findById(wordId).map(this::toResponse)
                    .orElseThrow(() -> new IllegalArgumentException("单词不存在"))
                : wordRepository.findById(wordId).map(this::toResponse)
                    .orElseThrow(() -> new IllegalArgumentException("单词不存在"));
        LearningStatusDO statusDO = learningStatusRepository
                .findByUserIdAndWordIdAndWordType(userId, wordId, type)
                .orElse(null);
        Integer wrongCount = wrongWordRepository.findByUserIdAndWordIdAndWordType(userId, wordId, type)
                .map(item -> item.getWrongCount() == null ? 0 : item.getWrongCount())
                .orElse(0);
        Integer unknownCount = wrongWordRepository.findByUserIdAndWordIdAndWordType(userId, wordId, type)
                .map(item -> item.getUnknownCount() == null ? 0 : item.getUnknownCount())
                .orElse(0);
        boolean mastered = masteredWordRepository.existsByUserIdAndWordIdAndWordType(userId, wordId, type);
        return new WordDetailResponse(word,
                statusDO == null ? "NEW" : statusDO.getStatus(),
                statusDO == null ? 0 : statusDO.getCorrectCount(),
                wrongCount,
                unknownCount,
                mastered);
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isBlank();
    }

    private int safeLimit(int limit, int max) {
        return Math.max(1, Math.min(limit, max));
    }

    private String formatCategory(Object category) {
        return category == null || category.toString().isBlank() ? "未分类" : category.toString();
    }

    private int priority(LearningStatusDO item) {
        LocalDateTime now = LocalDateTime.now();
        boolean due = item.getNextReviewTime() == null || !item.getNextReviewTime().isAfter(now);
        int overdue = due && item.getNextReviewTime() != null
                ? (int) Math.min(60, Math.max(0, ChronoUnit.DAYS.between(item.getNextReviewTime(), now)))
                : 0;
        int base = switch (item.getStatus()) {
            case "HIGH_WRONG" -> 500;
            case "UNKNOWN" -> 400;
            case "LEARNING" -> 200;
            default -> 100;
        };
        return (due ? 1000 : 0) + base + overdue * 8 + safeCount(item.getWrongCount()) * 10 + safeCount(item.getUnknownCount()) * 12;
    }

    private int safeCount(Integer value) {
        return value == null ? 0 : value;
    }

    private RandomWordResponse toQueueResponse(LearningStatusDO item) {
        RandomWordResponse response = new RandomWordResponse(item.getWordId(), item.getEnglish(), item.getWordType());
        response.setWrongCount(item.getWrongCount());
        response.setUnknownCount(item.getUnknownCount());
        response.setStatus(item.getStatus());
        response.setReviewIntervalDays(item.getReviewIntervalDays());
        response.setNextReviewTime(item.getNextReviewTime());
        return response;
    }

    private WordResponse toResponse(WordDO wordDO) {
        return new WordResponse(wordDO.getId(), wordDO.getEnglish(), wordDO.getChinese(), wordDO.getPhonetic(),
                wordDO.getExampleSentence(), wordDO.getCategory(), wordDO.getDifficulty(), "public");
    }

    private WordResponse toResponse(UserWordDO wordDO) {
        return new WordResponse(wordDO.getId(), wordDO.getEnglish(), wordDO.getChinese(), wordDO.getPhonetic(),
                wordDO.getExampleSentence(), wordDO.getCategory(), wordDO.getDifficulty(), "user");
    }
}
