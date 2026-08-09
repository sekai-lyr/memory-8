package com.example.wordlearning.service.impl;

import com.example.wordlearning.dataobject.LearningStatusDO;
import com.example.wordlearning.dataobject.UserWordDO;
import com.example.wordlearning.dataobject.WordDO;
import com.example.wordlearning.model.ImportResultResponse;
import com.example.wordlearning.model.ProReadingAnalyzeRequest;
import com.example.wordlearning.model.ProReadingImportRequest;
import com.example.wordlearning.model.ProReadingImportWordRequest;
import com.example.wordlearning.model.ProReadingResponse;
import com.example.wordlearning.model.ProReadingWordResponse;
import com.example.wordlearning.repository.LearningStatusRepository;
import com.example.wordlearning.repository.UserWordRepository;
import com.example.wordlearning.repository.WordRepository;
import com.example.wordlearning.service.MembershipService;
import com.example.wordlearning.service.ProReadingService;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class ProReadingServiceImpl implements ProReadingService {
    private static final Pattern WORD_PATTERN = Pattern.compile("[A-Za-z][A-Za-z'-]{1,40}");
    private static final Set<String> STOP_WORDS = Set.of(
            "the", "and", "for", "that", "this", "with", "from", "have", "has", "had", "was", "were",
            "are", "you", "your", "they", "their", "them", "she", "her", "his", "him", "our", "ours",
            "not", "but", "can", "could", "would", "should", "will", "shall", "may", "might", "must",
            "into", "onto", "over", "under", "than", "then", "there", "here", "what", "when", "where",
            "which", "while", "about", "after", "before", "because", "through", "during", "also", "only",
            "very", "more", "most", "some", "such", "each", "been", "being", "does", "did", "its"
    );

    private final MembershipService membershipService;
    private final WordRepository wordRepository;
    private final UserWordRepository userWordRepository;
    private final LearningStatusRepository learningStatusRepository;

    public ProReadingServiceImpl(MembershipService membershipService,
                                 WordRepository wordRepository,
                                 UserWordRepository userWordRepository,
                                 LearningStatusRepository learningStatusRepository) {
        this.membershipService = membershipService;
        this.wordRepository = wordRepository;
        this.userWordRepository = userWordRepository;
        this.learningStatusRepository = learningStatusRepository;
    }

    @Override
    public ProReadingResponse analyze(Long userId, ProReadingAnalyzeRequest request) {
        membershipService.requirePro(userId, "会员文章生词解析");
        String content = request == null ? "" : clean(request.content());
        String title = request == null ? "" : clean(request.title());
        if (content.isBlank()) {
            throw new IllegalArgumentException("请先粘贴英文文章或阅读材料");
        }
        int maxWords = clamp(request.maxWords() == null ? 50 : request.maxWords(), 20, 120);
        Map<String, Integer> frequencies = frequencies(content);
        Map<String, LearningStatusDO> statusMap = learningStatusRepository.findByUserIdOrderByUpdatedTimeDesc(userId).stream()
                .collect(Collectors.toMap(item -> normalize(item.getEnglish()), item -> item, (first, second) -> first));
        Set<String> userWordKeys = userWordRepository.findByUserIdOrderByIdDesc(userId).stream()
                .map(item -> normalize(item.getEnglish()))
                .collect(Collectors.toSet());

        List<ProReadingWordResponse> matched = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : frequencies.entrySet().stream()
                .sorted(Comparator.<Map.Entry<String, Integer>>comparingInt(Map.Entry::getValue).reversed()
                        .thenComparing(entry -> entry.getKey().length(), Comparator.reverseOrder()))
                .limit(700)
                .toList()) {
            wordRepository.findFirstByEnglishIgnoreCase(entry.getKey())
                    .map(word -> toReadingWord(word, entry.getValue(), statusMap.get(entry.getKey()), userWordKeys.contains(entry.getKey())))
                    .ifPresent(matched::add);
        }
        matched.sort(Comparator.comparingInt(ProReadingWordResponse::priorityScore).reversed()
                .thenComparing(ProReadingWordResponse::frequency, Comparator.reverseOrder())
                .thenComparing(ProReadingWordResponse::english));

        List<ProReadingWordResponse> words = matched.stream().limit(maxWords).toList();
        int knownWords = (int) matched.stream().filter(item -> item.mastered() || item.alreadyInUserWords()).count();
        int suggestedWords = (int) matched.stream().filter(item -> !item.mastered() && !item.alreadyInUserWords()).count();
        int difficultyScore = difficultyScore(words, frequencies.size());
        return new ProReadingResponse(
                title.isBlank() ? "未命名阅读材料" : title,
                totalTokens(content),
                frequencies.size(),
                matched.size(),
                suggestedWords,
                knownWords,
                difficultyScore,
                levelName(difficultyScore),
                summary(difficultyScore, words.size(), suggestedWords),
                words,
                highlights(frequencies.size(), matched.size(), suggestedWords, knownWords)
        );
    }

    @Override
    public ImportResultResponse importWords(Long userId, ProReadingImportRequest request) {
        membershipService.requirePro(userId, "文章生词一键加入词库");
        if (request == null || request.words() == null || request.words().isEmpty()) {
            throw new IllegalArgumentException("请选择要加入词库的生词");
        }
        String category = clean(request.category());
        if (category.isBlank()) {
            category = "文章生词";
        }
        int success = 0;
        int skipped = 0;
        for (ProReadingImportWordRequest word : request.words().stream().limit(200).toList()) {
            if (word == null || clean(word.english()).isBlank() || clean(word.chinese()).isBlank()) {
                skipped++;
                continue;
            }
            String english = clean(word.english());
            if (userWordRepository.existsByUserIdAndEnglishIgnoreCase(userId, english)) {
                skipped++;
                continue;
            }
            UserWordDO userWord = new UserWordDO();
            userWord.setUserId(userId);
            userWord.setEnglish(english);
            userWord.setChinese(clean(word.chinese()));
            userWord.setPhonetic(blankToNull(word.phonetic()));
            userWord.setExampleSentence(blankToNull(word.exampleSentence()));
            userWord.setCategory(category);
            userWord.setDifficulty(blankToNull(word.difficulty()));
            userWordRepository.save(userWord);
            success++;
        }
        return new ImportResultResponse(success, skipped, "已把文章生词加入我的词库，可去练习页定向复习");
    }

    private ProReadingWordResponse toReadingWord(WordDO word, int frequency, LearningStatusDO status, boolean alreadyInUserWords) {
        boolean mastered = status != null && "MASTERED".equals(status.getStatus());
        boolean alreadyLearning = status != null && !mastered;
        int priorityScore = priorityScore(word, frequency, status, alreadyInUserWords, mastered);
        return new ProReadingWordResponse(
                word.getId(),
                word.getEnglish(),
                word.getChinese(),
                word.getPhonetic(),
                word.getExampleSentence(),
                word.getCategory(),
                word.getDifficulty(),
                frequency,
                alreadyLearning,
                alreadyInUserWords,
                mastered,
                priorityScore,
                reason(frequency, word, status, alreadyInUserWords, mastered)
        );
    }

    private int priorityScore(WordDO word, int frequency, LearningStatusDO status, boolean alreadyInUserWords, boolean mastered) {
        int difficulty = switch (word.getDifficulty() == null ? "" : word.getDifficulty()) {
            case "hard" -> 50;
            case "medium" -> 30;
            default -> 15;
        };
        int statusBonus = status == null ? 35 : 0;
        int weakBonus = status == null ? 0 : safe(status.getWrongCount()) * 12 + safe(status.getUnknownCount()) * 14;
        int penalty = (alreadyInUserWords ? 70 : 0) + (mastered ? 90 : 0);
        return Math.max(1, frequency * 18 + difficulty + Math.min(24, word.getEnglish().length() * 2) + statusBonus + weakBonus - penalty);
    }

    private String reason(int frequency, WordDO word, LearningStatusDO status, boolean alreadyInUserWords, boolean mastered) {
        if (mastered) {
            return "你已经掌握，放低优先级";
        }
        if (alreadyInUserWords) {
            return "已经在我的词库里";
        }
        if (status != null && safe(status.getWrongCount()) + safe(status.getUnknownCount()) > 0) {
            return "文章里出现，又是你的薄弱词";
        }
        if (frequency >= 3) {
            return "文章中反复出现，适合优先背";
        }
        if ("hard".equals(word.getDifficulty())) {
            return "难度较高，适合加入学习包";
        }
        return "阅读理解可能用得上";
    }

    private Map<String, Integer> frequencies(String content) {
        Map<String, Integer> result = new LinkedHashMap<>();
        Matcher matcher = WORD_PATTERN.matcher(content);
        while (matcher.find()) {
            String token = normalize(matcher.group());
            if (token.length() < 3 || STOP_WORDS.contains(token)) {
                continue;
            }
            result.merge(token, 1, Integer::sum);
        }
        return result;
    }

    private int totalTokens(String content) {
        int count = 0;
        Matcher matcher = WORD_PATTERN.matcher(content);
        while (matcher.find()) {
            count++;
        }
        return count;
    }

    private int difficultyScore(List<ProReadingWordResponse> words, int uniqueWords) {
        if (words.isEmpty()) {
            return 0;
        }
        int hard = (int) words.stream().filter(item -> "hard".equals(item.difficulty())).count();
        int medium = (int) words.stream().filter(item -> "medium".equals(item.difficulty())).count();
        int base = (hard * 5 + medium * 3 + words.size()) * 100 / Math.max(1, words.size() * 5);
        int density = Math.min(28, uniqueWords / 8);
        return clamp(base + density, 10, 98);
    }

    private String levelName(int difficultyScore) {
        if (difficultyScore >= 78) {
            return "高难阅读";
        }
        if (difficultyScore >= 55) {
            return "进阶阅读";
        }
        if (difficultyScore >= 32) {
            return "日常阅读";
        }
        return "轻量阅读";
    }

    private String summary(int difficultyScore, int shownWords, int suggestedWords) {
        if (shownWords == 0) {
            return "这段材料没有匹配到足够的可学习词，可以换一段更长的英文文本。";
        }
        if (difficultyScore >= 78) {
            return "这段材料生词压力偏高，建议先挑 20 到 40 个重点词做学习包。";
        }
        if (suggestedWords >= 40) {
            return "这段材料很适合做专题词包，建议先导入高优先级生词。";
        }
        return "这段材料适合边读边补词，建议把未掌握词加入我的词库。";
    }

    private List<String> highlights(int uniqueWords, int matchedWords, int suggestedWords, int knownWords) {
        return List.of(
                "提取有效词形 " + uniqueWords + " 个",
                "词库命中 " + matchedWords + " 个",
                "建议学习 " + suggestedWords + " 个",
                "已掌握或已收藏 " + knownWords + " 个"
        );
    }

    private String normalize(String value) {
        if (value == null) {
            return "";
        }
        String result = value.trim().toLowerCase(Locale.ROOT);
        if (result.endsWith("'s")) {
            result = result.substring(0, result.length() - 2);
        }
        return result;
    }

    private String clean(String value) {
        return value == null ? "" : value.trim();
    }

    private String blankToNull(String value) {
        String clean = clean(value);
        return clean.isBlank() ? null : clean;
    }

    private int safe(Integer value) {
        return value == null ? 0 : value;
    }

    private int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}
