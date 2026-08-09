package com.example.wordlearning.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.wordlearning.dataobject.ProDailyTaskDO;
import com.example.wordlearning.model.DailyPlanResponse;
import com.example.wordlearning.repository.LearningStatusRepository;
import com.example.wordlearning.repository.ProDailyTaskRepository;
import com.example.wordlearning.repository.ProStudyGoalRepository;
import com.example.wordlearning.repository.StudyRecordRepository;
import com.example.wordlearning.repository.UserWordRepository;
import com.example.wordlearning.repository.WrongWordRepository;
import com.example.wordlearning.service.impl.DailyPlanServiceImpl;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DailyPlanServiceImplTest {
    @Mock
    private MembershipService membershipService;

    @Mock
    private ProDailyTaskRepository proDailyTaskRepository;

    @Mock
    private LearningStatusRepository learningStatusRepository;

    @Mock
    private WrongWordRepository wrongWordRepository;

    @Mock
    private UserWordRepository userWordRepository;

    @Mock
    private StudyRecordRepository studyRecordRepository;

    @Mock
    private ProStudyGoalRepository proStudyGoalRepository;

    private DailyPlanService dailyPlanService;

    @BeforeEach
    void setUp() {
        dailyPlanService = new DailyPlanServiceImpl(
                membershipService,
                proDailyTaskRepository,
                learningStatusRepository,
                wrongWordRepository,
                userWordRepository,
                studyRecordRepository,
                proStudyGoalRepository
        );
    }

    @Test
    void todayGeneratesTasksWhenMissing() {
        Long userId = 15L;
        LocalDate today = LocalDate.now();
        List<ProDailyTaskDO> saved = new ArrayList<>();
        when(proDailyTaskRepository.findByUserIdAndTaskDateOrderBySortOrderAsc(userId, today))
                .thenReturn(List.of())
                .thenAnswer(invocation -> saved);
        when(learningStatusRepository.countByUserIdAndNextReviewTimeLessThanEqualAndStatusNot(
                any(), any(LocalDateTime.class), any())).thenReturn(3L);
        when(wrongWordRepository.findByUserIdOrderByLastWrongTimeDesc(userId)).thenReturn(List.of());
        when(userWordRepository.countByUserId(userId)).thenReturn(5L);
        when(studyRecordRepository.findByUserIdOrderByCreatedTimeDesc(userId)).thenReturn(List.of());
        when(proStudyGoalRepository.findByUserId(userId)).thenReturn(Optional.empty());
        when(proDailyTaskRepository.save(any(ProDailyTaskDO.class))).thenAnswer(invocation -> {
            ProDailyTaskDO task = invocation.getArgument(0);
            saved.add(task);
            return task;
        });
        when(proDailyTaskRepository.findCompletedDates(userId)).thenReturn(List.of());

        DailyPlanResponse response = dailyPlanService.today(userId);

        verify(membershipService).requirePro(userId, "每日学习清单");
        assertThat(response.totalTasks()).isEqualTo(6);
        assertThat(response.tasks()).extracting("title").contains("完成一轮背词", "设置学习目标", "补充我的词库");
    }

    @Test
    void updateTaskMarksTaskCompleted() {
        Long userId = 15L;
        LocalDate today = LocalDate.now();
        ProDailyTaskDO task = task(userId, today, 99L);
        when(proDailyTaskRepository.findByIdAndUserId(99L, userId)).thenReturn(Optional.of(task));
        when(proDailyTaskRepository.save(any(ProDailyTaskDO.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(proDailyTaskRepository.findByUserIdAndTaskDateOrderBySortOrderAsc(userId, today)).thenReturn(List.of(task));
        when(proDailyTaskRepository.findCompletedDates(userId)).thenReturn(List.of(today));

        DailyPlanResponse response = dailyPlanService.updateTask(userId, 99L, true);

        assertThat(response.completedTasks()).isEqualTo(1);
        assertThat(response.streakDays()).isEqualTo(1);
        assertThat(task.getCompleted()).isTrue();
        assertThat(task.getCompletedTime()).isNotNull();
    }

    private ProDailyTaskDO task(Long userId, LocalDate date, Long id) {
        ProDailyTaskDO task = new ProDailyTaskDO();
        task.setUserId(userId);
        task.setTaskDate(date);
        task.setTaskKey("review");
        task.setTitle("完成一轮背词");
        task.setDetail("先练一轮。");
        task.setRoute("/practice");
        task.setTargetCount(10);
        task.setCompleted(false);
        task.setSortOrder(1);
        try {
            java.lang.reflect.Field field = ProDailyTaskDO.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(task, id);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
        return task;
    }
}
