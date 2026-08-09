package com.example.wordlearning.service.impl;

import com.example.wordlearning.dataobject.LearningStatusDO;
import com.example.wordlearning.dataobject.StudyRecordDO;
import com.example.wordlearning.dataobject.UserWordDO;
import com.example.wordlearning.dataobject.WordDO;
import com.example.wordlearning.dataobject.WrongWordDO;
import com.example.wordlearning.model.ProExamAnswerRequest;
import com.example.wordlearning.model.ProExamPaperResponse;
import com.example.wordlearning.model.ProExamQuestionResponse;
import com.example.wordlearning.model.ProExamQuestionResultResponse;
import com.example.wordlearning.model.ProExamSubmitRequest;
import com.example.wordlearning.model.ProExamSubmitResponse;
import com.example.wordlearning.repository.LearningStatusRepository;
import com.example.wordlearning.repository.StudyRecordRepository;
import com.example.wordlearning.repository.UserWordRepository;
import com.example.wordlearning.repository.WordRepository;
import com.example.wordlearning.repository.WrongWordRepository;
import com.example.wordlearning.service.MembershipService;
import com.example.wordlearning.service.ProExamService;
import com.example.wordlearning.util.AnswerJudgeUtil;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProExamServiceImpl implements ProExamService {
    private static final String STATUS_HIGH_WRONG = "HIGH_WRONG";
    private static final String STATUS_UNKNOWN = "UNKNOWN";
    private static final String STATUS_MASTERED = "MASTERED";

    private final MembershipService membershipService;
    private final LearningStatusRepository learningStatusRepository;
    private final WrongWordRepository wrongWordRepository;
    private final UserWordRepository userWordRepository;
    private final WordRepository wordRepository;
    private final StudyRecordRepository studyRecordRepository;

    public ProExamServiceImpl(MembershipService membershipService,
                              LearningStatusRepository learningStatusRepository,
                              WrongWordRepository wrongWordRepository,
                              UserWordRepository userWordRepository,
                              WordRepository wordRepository,
                              StudyRecordRepository studyRecordRepository) {
        this.membershipService = membershipService;
        this.learningStatusRepository = learningStatusRepository;
        this.wrongWordRepository = wrongWordRepository;
        this.userWordRepository = userWordRepository;
        this.wordRepository = wordRepository;
        this.studyRecordRepository = studyRecordRepository;
    }

    @Override
    public ProExamPaperResponse paper(Long userId, int count) {
        membershipService.requirePro(userId, "会员模考冲刺");
        int safeCount = clamp(count, 5, 60);
        List<ExamWord> words = selectExamWords(userId, safeCount);
        List<ProExamQuestionResponse> questions = words.stream()
                .map(item -> new ProExamQuestionResponse(
                        item.wordId(),
                        item.wordType(),
                        item.english(),
                        options(item),
                        item.status(),
                        item.wrongCount(),
                        item.unknownCount(),
                        item.sourceReason()
                ))
                .toList();
        return new ProExamPaperResponse(
                "PEX-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(),
                questions.size(),
                Math.max(6, (int) Math.ceil(questions.size() * 0.8)),
                questions,
                List.of("优先抽取到期复习和薄弱词", "每题只有一个最接近中文释义", "提交后错题会进入错题本和复习队列")
        );
    }

    @Override
    @Transactional
    public ProExamSubmitResponse submit(Long userId, ProExamSubmitRequest request) {
        membershipService.requirePro(userId, "会员模考冲刺");
        List<ProExamQuestionResultResponse> results = new ArrayList<>();
        int correct = 0;
        for (ProExamAnswerRequest answer : request.answers()) {
            ExamWord word = resolve(answer.wordId(), answer.wordType())
                    .orElseThrow(() -> new IllegalArgumentException("单词不存在：" + answer.wordId()));
            boolean right = AnswerJudgeUtil.isCorrect(answer.selectedAnswer(), word.chinese());
            if (right) {
                correct++;
            }
            recordLearning(userId, word, right);
            if (!right) {
                recordWrong(userId, word);
            }
            results.add(new ProExamQuestionResultResponse(
                    word.wordId(),
                    word.wordType(),
                    word.english(),
                    answer.selectedAnswer(),
                    word.chinese(),
                    right,
                    diagnosis(word, right)
            ));
        }
        int total = results.size();
        int wrong = Math.max(0, total - correct);
        BigDecimal accuracy = total == 0 ? BigDecimal.ZERO.setScale(2)
                : BigDecimal.valueOf(correct).multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(total), 2, RoundingMode.HALF_UP);
        int score = accuracy.setScale(0, RoundingMode.HALF_UP).intValue();
        saveStudyRecord(userId, total, correct, wrong, accuracy);
        return new ProExamSubmitResponse(
                total,
                correct,
                wrong,
                accuracy,
                score,
                levelName(score),
                recommendation(score, wrong),
                weakPoints(results),
                results
        );
    }

    private List<ExamWord> selectExamWords(Long userId, int safeCount) {
        List<ExamWord> result = new ArrayList<>();
        Set<String> keys = new HashSet<>();
        learningStatusRepository.findByUserIdOrderByUpdatedTimeDesc(userId).stream()
                .filter(item -> !STATUS_MASTERED.equals(item.getStatus()))
                .sorted(Comparator.comparingInt(this::priority).reversed())
                .forEach(item -> resolve(item.getWordId(), item.getWordType())
                        .map(word -> word.withStatus(item.getStatus(), value(item.getWrongCount()),
                                value(item.getUnknownCount()), sourceReason(item)))
                        .ifPresent(word -> addWord(result, keys, word, safeCount)));

        if (result.size() < safeCount) {
            wrongWordRepository.findByUserIdOrderByLastWrongTimeDesc(userId).stream()
                    .map(item -> new ExamWord(item.getWordId(), item.getWordType(), item.getEnglish(), item.getChinese(),
                            null, value(item.getWrongCount()), value(item.getUnknownCount()), "错题本重点"))
                    .forEach(item -> addWord(result, keys, item, safeCount));
        }
        if (result.size() < safeCount) {
            userWordRepository.findByUserIdOrderByIdDesc(userId).stream()
                    .map(item -> new ExamWord(item.getId(), "user", item.getEnglish(), item.getChinese(),
                            null, 0, 0, "自定义词库"))
                    .forEach(item -> addWord(result, keys, item, safeCount));
        }
        if (result.size() < safeCount) {
            wordRepository.findRandomList(safeCount - result.size()).stream()
                    .map(item -> new ExamWord(item.getId(), "public", item.getEnglish(), item.getChinese(),
                            null, 0, 0, "公共词库补充"))
                    .forEach(item -> addWord(result, keys, item, safeCount));
        }
        return result;
    }

    private void addWord(List<ExamWord> result, Set<String> keys, ExamWord word, int safeCount) {
        if (result.size() >= safeCount) {
            return;
        }
        String key = word.wordType() + ":" + word.wordId();
        if (keys.add(key)) {
            result.add(word);
        }
    }

    private Optional<ExamWord> resolve(Long wordId, String wordType) {
        if ("user".equalsIgnoreCase(wordType)) {
            return userWordRepository.findById(wordId)
                    .map(this::fromUserWord);
        }
        return wordRepository.findById(wordId)
                .map(this::fromPublicWord);
    }

    private ExamWord fromPublicWord(WordDO item) {
        return new ExamWord(item.getId(), "public", item.getEnglish(), item.getChinese(), null, 0, 0, "公共词库");
    }

    private ExamWord fromUserWord(UserWordDO item) {
        return new ExamWord(item.getId(), "user", item.getEnglish(), item.getChinese(), null, 0, 0, "自定义词库");
    }

    private List<String> options(ExamWord word) {
        List<String> options = new ArrayList<>();
        options.add(word.chinese());
        wordRepository.findRandomList(18).stream()
                .map(WordDO::getChinese)
                .filter(item -> item != null && !item.equals(word.chinese()))
                .distinct()
                .limit(3)
                .forEach(options::add);
        if (options.size() < 4) {
            userWordRepository.findAll().stream()
                    .map(UserWordDO::getChinese)
                    .filter(item -> item != null && !item.equals(word.chinese()) && !options.contains(item))
                    .limit(4 - options.size())
                    .forEach(options::add);
        }
        Collections.shuffle(options);
        return options;
    }

    private int priority(LearningStatusDO item) {
        LocalDateTime now = LocalDateTime.now();
        boolean due = item.getNextReviewTime() == null || !item.getNextReviewTime().isAfter(now);
        int base = switch (item.getStatus()) {
            case STATUS_HIGH_WRONG -> 520;
            case STATUS_UNKNOWN -> 430;
            default -> 220;
        };
        return (due ? 1000 : 0) + base + value(item.getWrongCount()) * 12 + value(item.getUnknownCount()) * 15;
    }

    private String sourceReason(LearningStatusDO item) {
        if (item.getNextReviewTime() == null || !item.getNextReviewTime().isAfter(LocalDateTime.now())) {
            return "到期复习";
        }
        if (STATUS_HIGH_WRONG.equals(item.getStatus())) {
            return "高频错误";
        }
        if (STATUS_UNKNOWN.equals(item.getStatus())) {
            return "不认识词";
        }
        return "学习中";
    }

    private void recordLearning(Long userId, ExamWord word, boolean correct) {
        LearningStatusDO status = learningStatusRepository
                .findByUserIdAndWordIdAndWordType(userId, word.wordId(), word.wordType())
                .orElseGet(() -> createLearningStatus(userId, word));
        status.setEnglish(word.english());
        status.setLastPracticedTime(LocalDateTime.now());
        if (correct) {
            status.setCorrectCount(value(status.getCorrectCount()) + 1);
        } else {
            status.setWrongCount(value(status.getWrongCount()) + 1);
        }
        status.setStatus(decideStatus(status));
        scheduleReview(status, correct);
        learningStatusRepository.save(status);
    }

    private LearningStatusDO createLearningStatus(Long userId, ExamWord word) {
        LearningStatusDO status = new LearningStatusDO();
        status.setUserId(userId);
        status.setWordId(word.wordId());
        status.setWordType(word.wordType());
        status.setEnglish(word.english());
        status.setStatus("NEW");
        status.setCorrectCount(0);
        status.setWrongCount(0);
        status.setUnknownCount(0);
        status.setReviewIntervalDays(0);
        status.setNextReviewTime(LocalDateTime.now());
        return status;
    }

    private String decideStatus(LearningStatusDO status) {
        if (value(status.getWrongCount()) >= 3) {
            return STATUS_HIGH_WRONG;
        }
        if (value(status.getUnknownCount()) > 0) {
            return STATUS_UNKNOWN;
        }
        return "LEARNING";
    }

    private void scheduleReview(LearningStatusDO status, boolean correct) {
        int nextInterval;
        if (correct) {
            int current = value(status.getReviewIntervalDays()) < 1 ? 1 : status.getReviewIntervalDays();
            nextInterval = value(status.getCorrectCount()) <= 1 ? 1 : Math.min(current * 2, 60);
        } else {
            nextInterval = 1;
        }
        status.setReviewIntervalDays(nextInterval);
        status.setNextReviewTime(LocalDateTime.now().plusDays(nextInterval));
    }

    private void recordWrong(Long userId, ExamWord word) {
        WrongWordDO wrongWord = wrongWordRepository.findByUserIdAndWordIdAndWordType(userId, word.wordId(), word.wordType())
                .orElseGet(WrongWordDO::new);
        if (wrongWord.getId() == null) {
            wrongWord.setUserId(userId);
            wrongWord.setWordId(word.wordId());
            wrongWord.setWordType(word.wordType());
            wrongWord.setWrongCount(0);
            wrongWord.setUnknownCount(0);
        }
        wrongWord.setEnglish(word.english());
        wrongWord.setChinese(word.chinese());
        wrongWord.setWrongCount(value(wrongWord.getWrongCount()) + 1);
        wrongWord.setLastWrongTime(LocalDateTime.now());
        wrongWordRepository.save(wrongWord);
    }

    private void saveStudyRecord(Long userId, int total, int correct, int wrong, BigDecimal accuracy) {
        StudyRecordDO record = new StudyRecordDO();
        record.setUserId(userId);
        record.setTotalCount(total);
        record.setCorrectCount(correct);
        record.setWrongCount(wrong);
        record.setAccuracy(accuracy);
        record.setMode("会员模考");
        studyRecordRepository.save(record);
    }

    private String diagnosis(ExamWord word, boolean correct) {
        if (correct) {
            return "掌握稳定，下一轮可降低出现频率。";
        }
        if (word.wrongCount() >= 3) {
            return "高频错误词，建议加入听写训练并明天复盘。";
        }
        if (word.unknownCount() > 0) {
            return "曾标记不认识，先用例句建立语境记忆。";
        }
        return "释义混淆，建议查看详情并加入错题本。";
    }

    private String levelName(int score) {
        if (score >= 90) {
            return "冲刺优秀";
        }
        if (score >= 75) {
            return "基本达标";
        }
        if (score >= 60) {
            return "需要复盘";
        }
        return "薄弱明显";
    }

    private String recommendation(int score, int wrong) {
        if (score >= 90) {
            return "本轮表现很好，可以增加新词量，并保留少量智能复习。";
        }
        if (score >= 75) {
            return "整体达标，建议把错题加入听写训练，减少释义混淆。";
        }
        if (wrong >= 8) {
            return "错题偏多，先暂停扩词，进入会员目标计划和错题复盘。";
        }
        return "建议立刻复盘错题，再做一轮 10 题小测确认是否掌握。";
    }

    private List<String> weakPoints(List<ProExamQuestionResultResponse> results) {
        return results.stream()
                .filter(item -> !item.correct())
                .limit(8)
                .map(ProExamQuestionResultResponse::english)
                .toList();
    }

    private int value(Integer value) {
        return value == null ? 0 : value;
    }

    private int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private record ExamWord(Long wordId, String wordType, String english, String chinese, String status,
                            int wrongCount, int unknownCount, String sourceReason) {
        private ExamWord withStatus(String status, int wrongCount, int unknownCount, String sourceReason) {
            return new ExamWord(wordId, wordType, english, chinese, status, wrongCount, unknownCount, sourceReason);
        }
    }
}
