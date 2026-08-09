package com.example.wordlearning.service.impl;

import com.example.wordlearning.dataobject.LearningStatusDO;
import com.example.wordlearning.dataobject.ProStudyGoalDO;
import com.example.wordlearning.dataobject.StudyRecordDO;
import com.example.wordlearning.model.ProGoalMetricsResponse;
import com.example.wordlearning.model.ProGoalMilestoneResponse;
import com.example.wordlearning.model.ProGoalMissionResponse;
import com.example.wordlearning.model.ProGoalRequest;
import com.example.wordlearning.model.ProGoalResponse;
import com.example.wordlearning.repository.LearningStatusRepository;
import com.example.wordlearning.repository.ProStudyGoalRepository;
import com.example.wordlearning.repository.StudyRecordRepository;
import com.example.wordlearning.repository.UserWordRepository;
import com.example.wordlearning.repository.WordRepository;
import com.example.wordlearning.repository.WrongWordRepository;
import com.example.wordlearning.service.MembershipService;
import com.example.wordlearning.service.ProGoalService;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProGoalServiceImpl implements ProGoalService {
    private static final String STATUS_HIGH_WRONG = "HIGH_WRONG";
    private static final String STATUS_UNKNOWN = "UNKNOWN";
    private static final String STATUS_LEARNING = "LEARNING";
    private static final String STATUS_MASTERED = "MASTERED";

    private final MembershipService membershipService;
    private final ProStudyGoalRepository proStudyGoalRepository;
    private final LearningStatusRepository learningStatusRepository;
    private final StudyRecordRepository studyRecordRepository;
    private final UserWordRepository userWordRepository;
    private final WrongWordRepository wrongWordRepository;
    private final WordRepository wordRepository;

    public ProGoalServiceImpl(MembershipService membershipService,
                              ProStudyGoalRepository proStudyGoalRepository,
                              LearningStatusRepository learningStatusRepository,
                              StudyRecordRepository studyRecordRepository,
                              UserWordRepository userWordRepository,
                              WrongWordRepository wrongWordRepository,
                              WordRepository wordRepository) {
        this.membershipService = membershipService;
        this.proStudyGoalRepository = proStudyGoalRepository;
        this.learningStatusRepository = learningStatusRepository;
        this.studyRecordRepository = studyRecordRepository;
        this.userWordRepository = userWordRepository;
        this.wrongWordRepository = wrongWordRepository;
        this.wordRepository = wordRepository;
    }

    @Override
    public ProGoalResponse current(Long userId) {
        membershipService.requirePro(userId, "会员目标计划");
        return proStudyGoalRepository.findByUserId(userId)
                .map(goal -> toResponse(userId, goal))
                .orElseGet(() -> emptyResponse(userId));
    }

    @Override
    @Transactional
    public ProGoalResponse save(Long userId, ProGoalRequest request) {
        membershipService.requirePro(userId, "会员目标计划");
        ProStudyGoalDO goal = proStudyGoalRepository.findByUserId(userId)
                .orElseGet(() -> newGoal(userId));
        goal.setTargetName(clean(request.targetName()));
        goal.setTargetDate(request.targetDate());
        goal.setDailyMinutes(clamp(request.dailyMinutes(), 5, 240));
        goal.setTargetWordCount(clamp(request.targetWordCount(), 20, 20000));
        goal.setStatus("ACTIVE");
        return toResponse(userId, proStudyGoalRepository.save(goal));
    }

    private ProStudyGoalDO newGoal(Long userId) {
        ProStudyGoalDO goal = new ProStudyGoalDO();
        goal.setUserId(userId);
        return goal;
    }

    private ProGoalResponse emptyResponse(Long userId) {
        ProGoalMetricsResponse metrics = metrics(userId);
        return new ProGoalResponse(
                false,
                null,
                null,
                null,
                30,
                Math.max(100, metrics.learningWords() + metrics.weakWords() + metrics.customWords() + 80),
                0,
                0,
                progressWords(metrics),
                BigDecimal.ZERO.setScale(2),
                "未设置目标",
                "设置考试日期、目标词量和每日时间后，会员功能会自动拆出今日任务和周计划。",
                metrics,
                List.of(),
                List.of()
        );
    }

    private ProGoalResponse toResponse(Long userId, ProStudyGoalDO goal) {
        ProGoalMetricsResponse metrics = metrics(userId);
        int progressWords = progressWords(metrics);
        int targetWordCount = value(goal.getTargetWordCount());
        int daysLeft = daysLeft(goal.getTargetDate());
        int remainingWords = Math.max(0, targetWordCount - progressWords);
        int expectedDailyWords = targetWordCount == 0 ? 0
                : clamp((int) Math.ceil(remainingWords / (double) Math.max(1, daysLeft)), 6, 120);
        BigDecimal progressPercent = targetWordCount == 0 ? BigDecimal.ZERO.setScale(2)
                : BigDecimal.valueOf(progressWords)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(targetWordCount), 2, RoundingMode.HALF_UP)
                .min(BigDecimal.valueOf(100).setScale(2));

        return new ProGoalResponse(
                true,
                goal.getId(),
                goal.getTargetName(),
                goal.getTargetDate(),
                value(goal.getDailyMinutes()),
                targetWordCount,
                daysLeft,
                expectedDailyWords,
                progressWords,
                progressPercent,
                paceStatus(goal, metrics, expectedDailyWords, progressPercent),
                recommendation(goal, metrics, expectedDailyWords),
                metrics,
                todayMissions(goal, metrics, expectedDailyWords),
                weeklyMilestones(goal, progressWords, targetWordCount)
        );
    }

    private ProGoalMetricsResponse metrics(Long userId) {
        LocalDateTime now = LocalDateTime.now();
        List<LearningStatusDO> statuses = learningStatusRepository.findByUserIdOrderByUpdatedTimeDesc(userId);
        int due = (int) statuses.stream()
                .filter(item -> !STATUS_MASTERED.equals(item.getStatus()))
                .filter(item -> item.getNextReviewTime() == null || !item.getNextReviewTime().isAfter(now))
                .count();
        int highWrong = countStatus(statuses, STATUS_HIGH_WRONG);
        int unknown = countStatus(statuses, STATUS_UNKNOWN);
        int learning = countStatus(statuses, STATUS_LEARNING);
        int mastered = countStatus(statuses, STATUS_MASTERED);
        int weak = Math.max(wrongWordRepository.findByUserIdOrderByLastWrongTimeDesc(userId).size(), highWrong + unknown);
        int custom = (int) Math.min(Integer.MAX_VALUE, userWordRepository.countByUserId(userId));
        int practicedSevenDays = practicedSevenDays(userId);
        long availableWords = wordRepository.count() + custom;
        return new ProGoalMetricsResponse(due, weak, learning, mastered, custom, practicedSevenDays, availableWords);
    }

    private int countStatus(List<LearningStatusDO> statuses, String status) {
        return (int) statuses.stream().filter(item -> status.equals(item.getStatus())).count();
    }

    private int practicedSevenDays(Long userId) {
        LocalDate start = LocalDate.now().minusDays(6);
        return studyRecordRepository.findByUserIdOrderByCreatedTimeDesc(userId).stream()
                .filter(item -> item.getCreatedTime() != null && !item.getCreatedTime().toLocalDate().isBefore(start))
                .mapToInt(item -> value(item.getTotalCount()))
                .sum();
    }

    private int progressWords(ProGoalMetricsResponse metrics) {
        return metrics.masteredWords() + metrics.learningWords();
    }

    private String paceStatus(ProStudyGoalDO goal, ProGoalMetricsResponse metrics,
                              int expectedDailyWords, BigDecimal progressPercent) {
        if (progressPercent.compareTo(BigDecimal.valueOf(100)) >= 0) {
            return "已完成目标";
        }
        if (daysLeft(goal.getTargetDate()) == 0) {
            return "目标日已到";
        }
        int weeklyNeed = expectedDailyWords * 5;
        if (metrics.practicedSevenDays() >= weeklyNeed) {
            return "节奏领先";
        }
        if (expectedDailyWords > value(goal.getDailyMinutes()) * 2) {
            return "压力偏高";
        }
        if (metrics.dueReviews() > expectedDailyWords) {
            return "复习积压";
        }
        return "节奏正常";
    }

    private String recommendation(ProStudyGoalDO goal, ProGoalMetricsResponse metrics, int expectedDailyWords) {
        if (metrics.dueReviews() > 0) {
            return "今天先清到期复习，再补新词。到期词如果积压，会直接影响后面的目标进度。";
        }
        if (metrics.weakWords() > expectedDailyWords) {
            return "薄弱词数量偏高，建议把今天一半时间放在错题和听写训练上。";
        }
        if (expectedDailyWords > value(goal.getDailyMinutes()) * 2) {
            return "当前目标有些紧，可以增加每日学习时间，或延后目标日期。";
        }
        if (metrics.customWords() < 20) {
            return "建议先导入自己的考试、课程或工作词表，会员计划会更贴合真实目标。";
        }
        return "当前计划可执行，按今日任务完成后再查看会员教练报告微调节奏。";
    }

    private List<ProGoalMissionResponse> todayMissions(ProStudyGoalDO goal, ProGoalMetricsResponse metrics,
                                                       int expectedDailyWords) {
        int reviewCount = clamp(Math.max(6, Math.min(expectedDailyWords, metrics.dueReviews() + metrics.weakWords())), 6, 40);
        int newCount = clamp(expectedDailyWords - reviewCount / 2, 6, 50);
        int dictationCount = clamp(Math.max(5, Math.min(20, metrics.weakWords())), 5, 20);
        return List.of(
                new ProGoalMissionResponse("清理到期复习", reviewCount, "/practice", "优先处理到期词和高频错误词，稳定长期记忆。"),
                new ProGoalMissionResponse("推进目标新词", newCount, "/public-words", "围绕 " + goal.getTargetName() + " 补充新词，保证目标词量进度。"),
                new ProGoalMissionResponse("听写巩固", dictationCount, "/pro-dictation", "隐藏拼写只听发音，检查是否真正记住单词声音。"),
                new ProGoalMissionResponse("查看学习记录", 1, "/study-records", "用热力图和正确率判断今天是否需要降低词量。")
        );
    }

    private List<ProGoalMilestoneResponse> weeklyMilestones(ProStudyGoalDO goal, int progressWords, int targetWordCount) {
        int remaining = Math.max(0, targetWordCount - progressWords);
        int daysLeft = Math.max(1, daysLeft(goal.getTargetDate()));
        int weeks = clamp((int) Math.ceil(daysLeft / 7.0), 1, 6);
        int weeklyTarget = clamp((int) Math.ceil(remaining / (double) weeks), 20, 600);
        return java.util.stream.IntStream.rangeClosed(1, weeks)
                .mapToObj(week -> new ProGoalMilestoneResponse(
                        "第 " + week + " 周",
                        weeklyTarget,
                        week == 1 ? "建立节奏，先清复习积压和薄弱词"
                                : week == weeks ? "冲刺复盘，减少新词，集中查漏补缺"
                                : "新词推进和错题返工并行"
                ))
                .toList();
    }

    private int daysLeft(LocalDate targetDate) {
        if (targetDate == null) {
            return 0;
        }
        return Math.max(0, (int) ChronoUnit.DAYS.between(LocalDate.now(), targetDate));
    }

    private String clean(String value) {
        if (value == null || value.trim().isBlank()) {
            throw new IllegalArgumentException("目标名称不能为空");
        }
        return value.trim();
    }

    private int value(Integer value) {
        return value == null ? 0 : value;
    }

    private int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}
