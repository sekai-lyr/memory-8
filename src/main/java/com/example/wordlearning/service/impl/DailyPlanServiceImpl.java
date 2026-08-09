package com.example.wordlearning.service.impl;

import com.example.wordlearning.dataobject.ProDailyTaskDO;
import com.example.wordlearning.model.DailyPlanResponse;
import com.example.wordlearning.model.DailyTaskResponse;
import com.example.wordlearning.repository.LearningStatusRepository;
import com.example.wordlearning.repository.ProDailyTaskRepository;
import com.example.wordlearning.repository.ProStudyGoalRepository;
import com.example.wordlearning.repository.StudyRecordRepository;
import com.example.wordlearning.repository.UserWordRepository;
import com.example.wordlearning.repository.WrongWordRepository;
import com.example.wordlearning.service.DailyPlanService;
import com.example.wordlearning.service.MembershipService;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DailyPlanServiceImpl implements DailyPlanService {
    private static final String MASTERED = "MASTERED";

    private final MembershipService membershipService;
    private final ProDailyTaskRepository proDailyTaskRepository;
    private final LearningStatusRepository learningStatusRepository;
    private final WrongWordRepository wrongWordRepository;
    private final UserWordRepository userWordRepository;
    private final StudyRecordRepository studyRecordRepository;
    private final ProStudyGoalRepository proStudyGoalRepository;

    public DailyPlanServiceImpl(MembershipService membershipService,
                                ProDailyTaskRepository proDailyTaskRepository,
                                LearningStatusRepository learningStatusRepository,
                                WrongWordRepository wrongWordRepository,
                                UserWordRepository userWordRepository,
                                StudyRecordRepository studyRecordRepository,
                                ProStudyGoalRepository proStudyGoalRepository) {
        this.membershipService = membershipService;
        this.proDailyTaskRepository = proDailyTaskRepository;
        this.learningStatusRepository = learningStatusRepository;
        this.wrongWordRepository = wrongWordRepository;
        this.userWordRepository = userWordRepository;
        this.studyRecordRepository = studyRecordRepository;
        this.proStudyGoalRepository = proStudyGoalRepository;
    }

    @Override
    @Transactional
    public DailyPlanResponse today(Long userId) {
        membershipService.requirePro(userId, "每日学习清单");
        LocalDate today = LocalDate.now();
        ensureTodayTasks(userId, today);
        return response(userId, today);
    }

    @Override
    @Transactional
    public DailyPlanResponse updateTask(Long userId, Long taskId, boolean completed) {
        membershipService.requirePro(userId, "每日学习清单");
        ProDailyTaskDO task = proDailyTaskRepository.findByIdAndUserId(taskId, userId)
                .orElseThrow(() -> new IllegalArgumentException("任务不存在"));
        task.setCompleted(completed);
        task.setCompletedTime(completed ? LocalDateTime.now() : null);
        proDailyTaskRepository.save(task);
        return response(userId, task.getTaskDate());
    }

    private void ensureTodayTasks(Long userId, LocalDate today) {
        if (!proDailyTaskRepository.findByUserIdAndTaskDateOrderBySortOrderAsc(userId, today).isEmpty()) {
            return;
        }
        buildTasks(userId, today).forEach(proDailyTaskRepository::save);
    }

    private List<ProDailyTaskDO> buildTasks(Long userId, LocalDate today) {
        long due = learningStatusRepository.countByUserIdAndNextReviewTimeLessThanEqualAndStatusNot(
                userId, LocalDateTime.now(), MASTERED);
        int wrong = wrongWordRepository.findByUserIdOrderByLastWrongTimeDesc(userId).size();
        int custom = (int) Math.min(Integer.MAX_VALUE, userWordRepository.countByUserId(userId));
        int todayDone = studyRecordRepository.findByUserIdOrderByCreatedTimeDesc(userId).stream()
                .filter(item -> item.getCreatedTime() != null && item.getCreatedTime().toLocalDate().equals(today))
                .mapToInt(item -> item.getTotalCount() == null ? 0 : item.getTotalCount())
                .sum();

        List<ProDailyTaskDO> tasks = new ArrayList<>();
        int reviewTarget = clamp((int) due + wrong, 10, 35);
        tasks.add(task(userId, today, "review", "完成一轮背词", "先练 " + reviewTarget + " 个词，系统会自动记录错题。", "/practice", reviewTarget, 1));
        tasks.add(task(userId, today, "wrong", "复习错题本", wrong > 0 ? "你有 " + wrong + " 个错题，今天至少复盘一遍。" : "现在错题不多，保持这个状态。", "/wrong-words", Math.max(1, wrong), 2));
        if (proStudyGoalRepository.findByUserId(userId).isPresent()) {
            tasks.add(task(userId, today, "goal", "推进目标计划", "按你的考试目标检查今天是否落后。", "/pro-goal", 1, 3));
        } else {
            tasks.add(task(userId, today, "goal", "设置学习目标", "填一个考试日期和目标词量，系统会倒推每日任务。", "/pro-goal", 1, 3));
        }
        tasks.add(task(userId, today, "dictation", "做听写训练", "听发音拼写英文，检查是否真的记住。", "/pro-dictation", 8, 4));
        tasks.add(task(userId, today, "record", "看学习记录", todayDone > 0 ? "今天已经练了 " + todayDone + " 题，看一下正确率。" : "练完后回来查看正确率和完成情况。", "/study-records", 1, 5));
        if (custom < 20) {
            tasks.add(task(userId, today, "words", "补充我的词库", "你的个人词库还不多，建议加入课堂、考试或工作里遇到的词。", "/user-words", 5, 6));
        } else {
            tasks.add(task(userId, today, "exam", "做一次模考", "用限时选择题检查今天的真实掌握情况。", "/pro-exam", 1, 6));
        }
        return tasks;
    }

    private ProDailyTaskDO task(Long userId, LocalDate date, String key, String title, String detail,
                                String route, int targetCount, int sortOrder) {
        ProDailyTaskDO task = new ProDailyTaskDO();
        task.setUserId(userId);
        task.setTaskDate(date);
        task.setTaskKey(key);
        task.setTitle(title);
        task.setDetail(detail);
        task.setRoute(route);
        task.setTargetCount(Math.max(1, targetCount));
        task.setCompleted(false);
        task.setSortOrder(sortOrder);
        return task;
    }

    private DailyPlanResponse response(Long userId, LocalDate date) {
        List<ProDailyTaskDO> tasks = proDailyTaskRepository.findByUserIdAndTaskDateOrderBySortOrderAsc(userId, date);
        int total = tasks.size();
        int completed = (int) tasks.stream().filter(item -> Boolean.TRUE.equals(item.getCompleted())).count();
        BigDecimal completionRate = total == 0 ? BigDecimal.ZERO.setScale(2)
                : BigDecimal.valueOf(completed).multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(total), 2, RoundingMode.HALF_UP);
        return new DailyPlanResponse(
                date,
                total,
                completed,
                completionRate,
                streakDays(userId),
                suggestion(completed, total),
                tasks.stream().map(this::toResponse).toList()
        );
    }

    private int streakDays(Long userId) {
        List<LocalDate> completedDates = proDailyTaskRepository.findCompletedDates(userId);
        int streak = 0;
        LocalDate cursor = LocalDate.now();
        for (LocalDate date : completedDates) {
            if (date.equals(cursor)) {
                streak++;
                cursor = cursor.minusDays(1);
            } else if (date.isBefore(cursor)) {
                break;
            }
        }
        return streak;
    }

    private String suggestion(int completed, int total) {
        if (total == 0) {
            return "今天还没有生成学习清单。";
        }
        if (completed == 0) {
            return "先完成第一项背词任务，今天就算启动了。";
        }
        if (completed < total) {
            return "已经完成一部分了，继续把剩下的任务打勾。";
        }
        return "今天清单已完成，明天回来继续保持连续学习。";
    }

    private DailyTaskResponse toResponse(ProDailyTaskDO task) {
        return new DailyTaskResponse(
                task.getId(),
                task.getTaskKey(),
                task.getTitle(),
                task.getDetail(),
                task.getRoute(),
                task.getTargetCount(),
                Boolean.TRUE.equals(task.getCompleted()),
                task.getCompletedTime()
        );
    }

    private int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}
