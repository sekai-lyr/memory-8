package com.example.wordlearning.service.impl;

import com.example.wordlearning.dataobject.LearningStatusDO;
import com.example.wordlearning.model.ProReviewBucketResponse;
import com.example.wordlearning.model.ProReviewDayResponse;
import com.example.wordlearning.model.ProReviewMetrics;
import com.example.wordlearning.model.ProReviewResponse;
import com.example.wordlearning.model.ProReviewWordResponse;
import com.example.wordlearning.repository.LearningStatusRepository;
import com.example.wordlearning.service.MembershipService;
import com.example.wordlearning.service.ProReviewService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.IntStream;
import org.springframework.stereotype.Service;

@Service
public class ProReviewServiceImpl implements ProReviewService {
    private static final String STATUS_HIGH_WRONG = "HIGH_WRONG";
    private static final String STATUS_UNKNOWN = "UNKNOWN";
    private static final String STATUS_LEARNING = "LEARNING";
    private static final String STATUS_MASTERED = "MASTERED";

    private final MembershipService membershipService;
    private final LearningStatusRepository learningStatusRepository;

    public ProReviewServiceImpl(MembershipService membershipService,
                                LearningStatusRepository learningStatusRepository) {
        this.membershipService = membershipService;
        this.learningStatusRepository = learningStatusRepository;
    }

    @Override
    public ProReviewResponse review(Long userId) {
        membershipService.requirePro(userId, "会员智能复习中心");
        LocalDateTime now = LocalDateTime.now();
        LocalDate today = LocalDate.now();
        List<LearningStatusDO> statuses = learningStatusRepository.findByUserIdOrderByUpdatedTimeDesc(userId);

        int dueReviews = (int) statuses.stream().filter(item -> isDue(item, now)).count();
        int overdueReviews = (int) statuses.stream()
                .filter(item -> !STATUS_MASTERED.equals(item.getStatus()))
                .filter(item -> item.getNextReviewTime() != null && item.getNextReviewTime().toLocalDate().isBefore(today))
                .count();
        int highWrong = countStatus(statuses, STATUS_HIGH_WRONG);
        int unknown = countStatus(statuses, STATUS_UNKNOWN);
        int learning = countStatus(statuses, STATUS_LEARNING);
        int mastered = countStatus(statuses, STATUS_MASTERED);
        int weakWords = (int) statuses.stream().filter(this::isWeak).count();
        int futureSevenDayReviews = (int) statuses.stream()
                .filter(item -> !STATUS_MASTERED.equals(item.getStatus()))
                .filter(item -> item.getNextReviewTime() != null)
                .filter(item -> {
                    LocalDate date = item.getNextReviewTime().toLocalDate();
                    return date.isAfter(today) && !date.isAfter(today.plusDays(7));
                })
                .count();
        int recommendedCount = clamp(12 + dueReviews + highWrong * 2 + unknown, 12, 60);
        int estimatedMinutes = Math.max(8, (int) Math.ceil(recommendedCount * 0.75));

        ProReviewMetrics metrics = new ProReviewMetrics(
                statuses.size(),
                dueReviews,
                overdueReviews,
                highWrong,
                unknown,
                learning,
                mastered,
                weakWords,
                futureSevenDayReviews,
                estimatedMinutes
        );

        return new ProReviewResponse(
                focusTitle(statuses.size(), dueReviews, highWrong, unknown),
                focusDetail(statuses.size(), dueReviews, highWrong, unknown, overdueReviews),
                nextAction(dueReviews, highWrong, unknown),
                recommendedMode(dueReviews, highWrong, unknown),
                recommendedCount,
                metrics,
                buckets(dueReviews, overdueReviews, highWrong, unknown, weakWords, futureSevenDayReviews),
                upcomingDays(statuses, today),
                priorityWords(statuses, now),
                vipReasons()
        );
    }

    private List<ProReviewBucketResponse> buckets(int dueReviews, int overdueReviews, int highWrong,
                                                  int unknown, int weakWords, int futureSevenDayReviews) {
        return List.of(
                new ProReviewBucketResponse("due", "今天到期复习", dueReviews,
                        dueReviews > 0 ? "必须处理" : "暂无积压",
                        overdueReviews > 0 ? "其中 " + overdueReviews + " 个已经逾期，建议先清掉。" : "按遗忘曲线安排，避免明天堆积。",
                        "/practice"),
                new ProReviewBucketResponse("weak", "薄弱词返工", weakWords,
                        weakWords > 0 ? "优先级高" : "状态稳定",
                        "统计错误次数和不会次数，把最拖后腿的词排前面。",
                        "/wrong-words"),
                new ProReviewBucketResponse("unknown", "不认识词巩固", unknown,
                        unknown > 0 ? "先建印象" : "暂无",
                        "这类词先看释义和例句，再进入听写或模考。",
                        "/pro-dictation"),
                new ProReviewBucketResponse("future", "未来 7 天压力", futureSevenDayReviews,
                        futureSevenDayReviews >= 40 ? "压力偏高" : "压力可控",
                        "提前看到接下来一周会回来的复习量，方便安排每天时间。",
                        "/daily-plan"),
                new ProReviewBucketResponse("highWrong", "高频错误词", highWrong,
                        highWrong > 0 ? "今天重点" : "暂无高危",
                        "连续答错的词不适合继续刷量，先做短轮复盘。",
                        "/practice")
        );
    }

    private List<ProReviewDayResponse> upcomingDays(List<LearningStatusDO> statuses, LocalDate today) {
        return IntStream.range(0, 7)
                .mapToObj(offset -> {
                    LocalDate date = today.plusDays(offset);
                    int dueCount = countForDate(statuses, today, date, offset);
                    return new ProReviewDayResponse(date, dayLabel(offset), dueCount, loadLevel(dueCount), daySuggestion(dueCount, offset));
                })
                .toList();
    }

    private int countForDate(List<LearningStatusDO> statuses, LocalDate today, LocalDate date, int offset) {
        return (int) statuses.stream()
                .filter(item -> !STATUS_MASTERED.equals(item.getStatus()))
                .filter(item -> item.getNextReviewTime() != null)
                .filter(item -> {
                    LocalDate reviewDate = item.getNextReviewTime().toLocalDate();
                    return offset == 0 ? !reviewDate.isAfter(today) : reviewDate.equals(date);
                })
                .count();
    }

    private List<ProReviewWordResponse> priorityWords(List<LearningStatusDO> statuses, LocalDateTime now) {
        return statuses.stream()
                .filter(item -> !STATUS_MASTERED.equals(item.getStatus()))
                .sorted(Comparator.comparingInt((LearningStatusDO item) -> priorityScore(item, now)).reversed()
                        .thenComparing(LearningStatusDO::getLastPracticedTime, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(12)
                .map(item -> new ProReviewWordResponse(
                        item.getWordId(),
                        item.getWordType(),
                        item.getEnglish(),
                        item.getStatus(),
                        priorityScore(item, now),
                        value(item.getWrongCount()),
                        value(item.getUnknownCount()),
                        value(item.getCorrectCount()),
                        value(item.getReviewIntervalDays()),
                        item.getNextReviewTime(),
                        reason(item, now)
                ))
                .toList();
    }

    private int priorityScore(LearningStatusDO item, LocalDateTime now) {
        int base = switch (item.getStatus()) {
            case STATUS_HIGH_WRONG -> 520;
            case STATUS_UNKNOWN -> 430;
            case STATUS_LEARNING -> 240;
            default -> 120;
        };
        int dueBonus = isDue(item, now) ? 700 : 0;
        int overdueBonus = item.getNextReviewTime() != null && item.getNextReviewTime().isBefore(now)
                ? Math.min(240, Math.max(0, (int) java.time.Duration.between(item.getNextReviewTime(), now).toHours()) * 3)
                : 0;
        return base
                + dueBonus
                + overdueBonus
                + value(item.getWrongCount()) * 18
                + value(item.getUnknownCount()) * 22
                - value(item.getCorrectCount()) * 3;
    }

    private boolean isDue(LearningStatusDO item, LocalDateTime now) {
        return !STATUS_MASTERED.equals(item.getStatus())
                && (item.getNextReviewTime() == null || !item.getNextReviewTime().isAfter(now));
    }

    private boolean isWeak(LearningStatusDO item) {
        return STATUS_HIGH_WRONG.equals(item.getStatus())
                || STATUS_UNKNOWN.equals(item.getStatus())
                || value(item.getWrongCount()) + value(item.getUnknownCount()) >= 2;
    }

    private int countStatus(List<LearningStatusDO> statuses, String status) {
        return (int) statuses.stream().filter(item -> status.equals(item.getStatus())).count();
    }

    private String focusTitle(int total, int dueReviews, int highWrong, int unknown) {
        if (total == 0) {
            return "先做一轮练习，建立诊断数据";
        }
        if (dueReviews > 0) {
            return "今天先清空到期复习";
        }
        if (highWrong > 0) {
            return "今天先修复高频错误词";
        }
        if (unknown > 0) {
            return "今天先把不认识词变熟";
        }
        return "节奏不错，继续轻量维护";
    }

    private String focusDetail(int total, int dueReviews, int highWrong, int unknown, int overdueReviews) {
        if (total == 0) {
            return "系统需要至少一轮答题数据，才能判断哪些词该提前复习、哪些词可以延后。";
        }
        if (dueReviews > 0) {
            return overdueReviews > 0
                    ? "有 " + overdueReviews + " 个词已经超过复习时间，先做短轮智能复习，别让遗忘继续扩大。"
                    : "这些词刚好到复习点，现在复习最省时间。";
        }
        if (highWrong > 0) {
            return "高频错误词会反复拖低正确率，建议先看释义和例句，再做一轮听写。";
        }
        if (unknown > 0) {
            return "不认识的词先不要硬刷，先建立中文意思和发音印象，再进入测验。";
        }
        return "目前没有明显积压，可以用混合练习保持手感，同时少量加入新词。";
    }

    private String nextAction(int dueReviews, int highWrong, int unknown) {
        if (dueReviews > 0) {
            return "开始到期复习";
        }
        if (highWrong > 0) {
            return "返工高频错题";
        }
        if (unknown > 0) {
            return "巩固不认识词";
        }
        return "开始混合练习";
    }

    private String recommendedMode(int dueReviews, int highWrong, int unknown) {
        if (dueReviews > 0 || highWrong > 0) {
            return "smart";
        }
        if (unknown > 0) {
            return "dictation";
        }
        return "mixed";
    }

    private String reason(LearningStatusDO item, LocalDateTime now) {
        if (isDue(item, now) && STATUS_HIGH_WRONG.equals(item.getStatus())) {
            return "已到复习时间，而且错误次数高";
        }
        if (isDue(item, now)) {
            return "已到复习时间";
        }
        if (STATUS_HIGH_WRONG.equals(item.getStatus())) {
            return "高频错误，优先返工";
        }
        if (STATUS_UNKNOWN.equals(item.getStatus())) {
            return "标记为不认识，需要先建立印象";
        }
        if (value(item.getWrongCount()) > 0) {
            return "近期答错过，适合短轮复盘";
        }
        return "学习中，保持间隔复习";
    }

    private String dayLabel(int offset) {
        return switch (offset) {
            case 0 -> "今天";
            case 1 -> "明天";
            default -> "第 " + (offset + 1) + " 天";
        };
    }

    private String loadLevel(int dueCount) {
        if (dueCount >= 40) {
            return "偏重";
        }
        if (dueCount >= 15) {
            return "正常";
        }
        if (dueCount > 0) {
            return "轻量";
        }
        return "空闲";
    }

    private String daySuggestion(int dueCount, int offset) {
        if (dueCount == 0) {
            return offset == 0 ? "可以补一轮新词或整理自己的词库。" : "这天压力很低，适合安排新词。";
        }
        if (dueCount >= 40) {
            return "建议拆成早晚两轮，避免一次刷太久。";
        }
        if (dueCount >= 15) {
            return "一轮智能复习基本能处理完。";
        }
        return "用 10 分钟轻量过一遍即可。";
    }

    private List<String> vipReasons() {
        return List.of(
                "按遗忘时间自动排优先级",
                "提前看到未来 7 天复习压力",
                "把错题、不认识词和学习中单词分开处理",
                "给出今天建议词量和预计耗时",
                "适合备考用户每天照着执行"
        );
    }

    private int value(Integer value) {
        return value == null ? 0 : value;
    }

    private int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}
