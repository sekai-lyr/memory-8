package com.example.wordlearning.service.impl;

import com.example.wordlearning.dataobject.LearningStatusDO;
import com.example.wordlearning.dataobject.StudyRecordDO;
import com.example.wordlearning.dataobject.UserWordDO;
import com.example.wordlearning.dataobject.WordDO;
import com.example.wordlearning.dataobject.WrongWordDO;
import com.example.wordlearning.model.ProDashboardMetrics;
import com.example.wordlearning.model.ProDashboardResponse;
import com.example.wordlearning.model.ProPlanDayResponse;
import com.example.wordlearning.model.ProWeakWordResponse;
import com.example.wordlearning.model.RandomWordResponse;
import com.example.wordlearning.repository.FavoriteWordRepository;
import com.example.wordlearning.repository.LearningStatusRepository;
import com.example.wordlearning.repository.StudyRecordRepository;
import com.example.wordlearning.repository.UserWordRepository;
import com.example.wordlearning.repository.WordRepository;
import com.example.wordlearning.repository.WrongWordRepository;
import com.example.wordlearning.service.MembershipService;
import com.example.wordlearning.service.ProDashboardService;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;

@Service
public class ProDashboardServiceImpl implements ProDashboardService {
    private static final String STATUS_HIGH_WRONG = "HIGH_WRONG";
    private static final String STATUS_UNKNOWN = "UNKNOWN";
    private static final String STATUS_LEARNING = "LEARNING";
    private static final String STATUS_MASTERED = "MASTERED";

    private final MembershipService membershipService;
    private final LearningStatusRepository learningStatusRepository;
    private final WrongWordRepository wrongWordRepository;
    private final UserWordRepository userWordRepository;
    private final WordRepository wordRepository;
    private final FavoriteWordRepository favoriteWordRepository;
    private final StudyRecordRepository studyRecordRepository;

    public ProDashboardServiceImpl(MembershipService membershipService,
                                   LearningStatusRepository learningStatusRepository,
                                   WrongWordRepository wrongWordRepository,
                                   UserWordRepository userWordRepository,
                                   WordRepository wordRepository,
                                   FavoriteWordRepository favoriteWordRepository,
                                   StudyRecordRepository studyRecordRepository) {
        this.membershipService = membershipService;
        this.learningStatusRepository = learningStatusRepository;
        this.wrongWordRepository = wrongWordRepository;
        this.userWordRepository = userWordRepository;
        this.wordRepository = wordRepository;
        this.favoriteWordRepository = favoriteWordRepository;
        this.studyRecordRepository = studyRecordRepository;
    }

    @Override
    public ProDashboardResponse dashboard(Long userId) {
        membershipService.requirePro(userId, "会员学习教练");

        LocalDateTime now = LocalDateTime.now();
        List<LearningStatusDO> statuses = learningStatusRepository.findByUserIdOrderByUpdatedTimeDesc(userId);
        List<WrongWordDO> wrongWords = wrongWordRepository.findByUserIdOrderByLastWrongTimeDesc(userId);
        List<StudyRecordDO> records = studyRecordRepository.findByUserIdOrderByCreatedTimeDesc(userId);

        int dueReviews = (int) statuses.stream()
                .filter(item -> !STATUS_MASTERED.equals(item.getStatus()))
                .filter(item -> item.getNextReviewTime() == null || !item.getNextReviewTime().isAfter(now))
                .count();
        int highWrong = countStatus(statuses, STATUS_HIGH_WRONG);
        int unknown = countStatus(statuses, STATUS_UNKNOWN);
        int learning = countStatus(statuses, STATUS_LEARNING);
        int mastered = countStatus(statuses, STATUS_MASTERED);
        int wrongWordCount = wrongWords.size();
        int customWords = (int) Math.min(Integer.MAX_VALUE, userWordRepository.countByUserId(userId));
        int favoriteWords = favoriteWordRepository.findByUserIdOrderByCreatedTimeDesc(userId).size();
        int sevenDayTotal = sevenDayTotal(records);
        BigDecimal averageAccuracy = averageAccuracy(records);
        int dailyTarget = clamp(12 + dueReviews + highWrong * 2 + unknown, 12, 45);

        ProDashboardMetrics metrics = new ProDashboardMetrics(
                dueReviews,
                highWrong,
                unknown,
                learning,
                mastered,
                wrongWordCount,
                customWords,
                favoriteWords,
                sevenDayTotal,
                averageAccuracy
        );

        return new ProDashboardResponse(
                levelName(statuses.size(), mastered, sevenDayTotal),
                retentionScore(statuses.size(), mastered, highWrong, unknown, sevenDayTotal),
                dailyTarget,
                focusTitle(dueReviews, highWrong, unknown, customWords),
                focusDetail(dueReviews, highWrong, unknown, customWords),
                recommendedMode(dueReviews, highWrong, unknown),
                nextAction(dueReviews, highWrong, unknown),
                metrics,
                weeklyPlan(dailyTarget, dueReviews, highWrong, unknown),
                weakWords(wrongWords),
                privileges()
        );
    }

    @Override
    public List<RandomWordResponse> dictationWords(Long userId, int count) {
        membershipService.requirePro(userId, "会员听写训练");
        int safeCount = clamp(count, 5, 50);
        List<RandomWordResponse> result = new java.util.ArrayList<>();
        Set<String> keys = new HashSet<>();

        learningStatusRepository.findByUserIdOrderByUpdatedTimeDesc(userId).stream()
                .filter(item -> !STATUS_MASTERED.equals(item.getStatus()))
                .sorted(Comparator.comparingInt(this::dictationPriority).reversed())
                .limit(safeCount)
                .forEach(item -> addDictationWord(result, keys, toQueueWord(item), safeCount));

        if (result.size() < safeCount) {
            userWordRepository.findByUserIdOrderByIdDesc(userId).stream()
                    .limit(safeCount - result.size())
                    .forEach(item -> addDictationWord(result, keys, toUserWord(item), safeCount));
        }
        if (result.size() < safeCount) {
            wordRepository.findRandomList(safeCount - result.size()).stream()
                    .forEach(item -> addDictationWord(result, keys, toPublicWord(item), safeCount));
        }
        return result;
    }

    private int countStatus(List<LearningStatusDO> statuses, String status) {
        return (int) statuses.stream().filter(item -> status.equals(item.getStatus())).count();
    }

    private int sevenDayTotal(List<StudyRecordDO> records) {
        LocalDate start = LocalDate.now().minusDays(6);
        return records.stream()
                .filter(item -> item.getCreatedTime() != null && !item.getCreatedTime().toLocalDate().isBefore(start))
                .mapToInt(item -> value(item.getTotalCount()))
                .sum();
    }

    private BigDecimal averageAccuracy(List<StudyRecordDO> records) {
        int total = records.stream().mapToInt(item -> value(item.getTotalCount())).sum();
        int correct = records.stream().mapToInt(item -> value(item.getCorrectCount())).sum();
        if (total == 0) {
            return BigDecimal.ZERO.setScale(2);
        }
        return BigDecimal.valueOf(correct)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(total), 2, RoundingMode.HALF_UP);
    }

    private int retentionScore(int statusCount, int mastered, int highWrong, int unknown, int sevenDayTotal) {
        if (statusCount == 0) {
            return sevenDayTotal > 0 ? 58 : 45;
        }
        int score = 52
                + mastered * 6
                + Math.min(18, sevenDayTotal / 8)
                - highWrong * 5
                - unknown * 4;
        return clamp(score, 35, 98);
    }

    private String levelName(int statusCount, int mastered, int sevenDayTotal) {
        if (statusCount == 0 && sevenDayTotal == 0) {
            return "待启动";
        }
        if (mastered >= 80) {
            return "稳定掌握";
        }
        if (sevenDayTotal >= 120) {
            return "高强度冲刺";
        }
        if (mastered >= 20) {
            return "持续进步";
        }
        return "建立节奏";
    }

    private String focusTitle(int dueReviews, int highWrong, int unknown, int customWords) {
        if (dueReviews > 0) {
            return "今天优先清空到期复习";
        }
        if (highWrong > 0) {
            return "先处理高频错误词";
        }
        if (unknown > 0) {
            return "把不认识的词变成熟词";
        }
        if (customWords < 20) {
            return "扩充你的专属词库";
        }
        return "保持复习节奏并增加新词";
    }

    private String focusDetail(int dueReviews, int highWrong, int unknown, int customWords) {
        if (dueReviews > 0) {
            return "智能队列会把已到复习时间、错误次数高的词排到前面，适合先做一轮 15 到 30 词。";
        }
        if (highWrong > 0) {
            return "高频错误词会拖慢长期记忆，建议用错题复盘和听读跟读各过一遍。";
        }
        if (unknown > 0) {
            return "不认识的词不要急着刷量，先用例句和中文释义建立第一层印象。";
        }
        if (customWords < 20) {
            return "会员适合导入课堂、考试、工作场景词表，系统再按练习结果排复习顺序。";
        }
        return "当前没有明显卡点，可以继续混合新词、收藏词和智能复习队列。";
    }

    private String recommendedMode(int dueReviews, int highWrong, int unknown) {
        if (dueReviews > 0 || highWrong > 0) {
            return "smart";
        }
        if (unknown > 0) {
            return "wrong";
        }
        return "mixed";
    }

    private String nextAction(int dueReviews, int highWrong, int unknown) {
        if (dueReviews > 0) {
            return "进入智能复习队列";
        }
        if (highWrong > 0 || unknown > 0) {
            return "复盘薄弱词";
        }
        return "开始今日会员练习";
    }

    private List<ProPlanDayResponse> weeklyPlan(int dailyTarget, int dueReviews, int highWrong, int unknown) {
        int reviewTarget = clamp(dailyTarget, 12, 45);
        int repairTarget = clamp(10 + highWrong * 2 + unknown, 10, 35);
        return List.of(
                new ProPlanDayResponse("今天", "智能复习", reviewTarget, dueReviews > 0 ? "清空到期词，避免遗忘曲线继续下滑" : "用智能队列保持记忆间隔"),
                new ProPlanDayResponse("明天", "错题返工", repairTarget, highWrong > 0 ? "集中处理高频错误词" : "复查最近答错和犹豫的词"),
                new ProPlanDayResponse("第 3 天", "自定义词库", 18, "导入或整理自己的考试、课堂、工作词表"),
                new ProPlanDayResponse("第 4 天", "听读跟读", 16, "用发音按钮强化单词声音印象"),
                new ProPlanDayResponse("第 5 天", "混合测验", 24, "混合新词、错题和收藏词检查真实掌握度"),
                new ProPlanDayResponse("第 6 天", "轻量回顾", 12, "只复盘低信心词，保持连续学习"),
                new ProPlanDayResponse("第 7 天", "周复盘", 20, "查看热力图和正确率，调整下周目标")
        );
    }

    private int dictationPriority(LearningStatusDO item) {
        LocalDateTime now = LocalDateTime.now();
        boolean due = item.getNextReviewTime() == null || !item.getNextReviewTime().isAfter(now);
        int base = switch (item.getStatus()) {
            case STATUS_HIGH_WRONG -> 500;
            case STATUS_UNKNOWN -> 420;
            case STATUS_LEARNING -> 230;
            default -> 120;
        };
        return (due ? 1000 : 0)
                + base
                + value(item.getWrongCount()) * 12
                + value(item.getUnknownCount()) * 16
                + value(item.getReviewIntervalDays());
    }

    private void addDictationWord(List<RandomWordResponse> result, Set<String> keys,
                                  RandomWordResponse word, int safeCount) {
        if (word == null || result.size() >= safeCount) {
            return;
        }
        String key = word.getWordType() + ":" + word.getId();
        if (keys.add(key)) {
            result.add(word);
        }
    }

    private RandomWordResponse toQueueWord(LearningStatusDO item) {
        RandomWordResponse response = new RandomWordResponse(item.getWordId(), item.getEnglish(), item.getWordType());
        response.setWrongCount(item.getWrongCount());
        response.setUnknownCount(item.getUnknownCount());
        response.setStatus(item.getStatus());
        response.setReviewIntervalDays(item.getReviewIntervalDays());
        response.setNextReviewTime(item.getNextReviewTime());
        return response;
    }

    private RandomWordResponse toUserWord(UserWordDO item) {
        return new RandomWordResponse(item.getId(), item.getEnglish(), "user");
    }

    private RandomWordResponse toPublicWord(WordDO item) {
        return new RandomWordResponse(item.getId(), item.getEnglish(), "public");
    }

    private List<ProWeakWordResponse> weakWords(List<WrongWordDO> wrongWords) {
        return wrongWords.stream()
                .sorted(Comparator.comparingInt(this::weakScore).reversed()
                        .thenComparing(WrongWordDO::getLastWrongTime, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(8)
                .map(item -> new ProWeakWordResponse(
                        item.getWordId(),
                        item.getWordType(),
                        item.getEnglish(),
                        item.getChinese(),
                        value(item.getWrongCount()),
                        value(item.getUnknownCount()),
                        item.getLastWrongTime()
                ))
                .toList();
    }

    private int weakScore(WrongWordDO item) {
        return value(item.getWrongCount()) * 3 + value(item.getUnknownCount()) * 4;
    }

    private List<String> privileges() {
        return List.of(
                "会员学习报告",
                "会员智能复习中心",
                "文章生词解析",
                "每日学习清单",
                "会员目标计划",
                "会员模考冲刺",
                "7 天复习计划",
                "薄弱词优先级诊断",
                "会员听写训练",
                "智能复习队列",
                "表格批量导入",
                "学习热力图与今日总结"
        );
    }

    private int value(Integer value) {
        return value == null ? 0 : value;
    }

    private int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}
