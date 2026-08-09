package com.example.wordlearning.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.wordlearning.dataobject.LearningStatusDO;
import com.example.wordlearning.dataobject.ProStudyGoalDO;
import com.example.wordlearning.dataobject.StudyRecordDO;
import com.example.wordlearning.model.ProGoalRequest;
import com.example.wordlearning.model.ProGoalResponse;
import com.example.wordlearning.repository.LearningStatusRepository;
import com.example.wordlearning.repository.ProStudyGoalRepository;
import com.example.wordlearning.repository.StudyRecordRepository;
import com.example.wordlearning.repository.UserWordRepository;
import com.example.wordlearning.repository.WordRepository;
import com.example.wordlearning.repository.WrongWordRepository;
import com.example.wordlearning.service.impl.ProGoalServiceImpl;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProGoalServiceImplTest {
    @Mock
    private MembershipService membershipService;

    @Mock
    private ProStudyGoalRepository proStudyGoalRepository;

    @Mock
    private LearningStatusRepository learningStatusRepository;

    @Mock
    private StudyRecordRepository studyRecordRepository;

    @Mock
    private UserWordRepository userWordRepository;

    @Mock
    private WrongWordRepository wrongWordRepository;

    @Mock
    private WordRepository wordRepository;

    private ProGoalService proGoalService;

    @BeforeEach
    void setUp() {
        proGoalService = new ProGoalServiceImpl(
                membershipService,
                proStudyGoalRepository,
                learningStatusRepository,
                studyRecordRepository,
                userWordRepository,
                wrongWordRepository,
                wordRepository
        );
    }

    @Test
    void saveCreatesGoalAndBuildsActionablePlan() {
        Long userId = 9L;
        when(proStudyGoalRepository.findByUserId(userId)).thenReturn(Optional.empty());
        when(proStudyGoalRepository.save(any(ProStudyGoalDO.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(learningStatusRepository.findByUserIdOrderByUpdatedTimeDesc(userId)).thenReturn(List.of(
                status("MASTERED", LocalDateTime.now().plusDays(20)),
                status("LEARNING", LocalDateTime.now().minusDays(1)),
                status("HIGH_WRONG", LocalDateTime.now().minusHours(2))
        ));
        when(studyRecordRepository.findByUserIdOrderByCreatedTimeDesc(userId)).thenReturn(List.of(record(30)));
        when(userWordRepository.countByUserId(userId)).thenReturn(42L);
        when(wrongWordRepository.findByUserIdOrderByLastWrongTimeDesc(userId)).thenReturn(List.of());
        when(wordRepository.count()).thenReturn(1000L);

        ProGoalResponse response = proGoalService.save(userId,
                new ProGoalRequest("四级冲刺", LocalDate.now().plusDays(30), 35, 300));

        verify(membershipService).requirePro(userId, "会员目标计划");
        assertThat(response.hasGoal()).isTrue();
        assertThat(response.targetName()).isEqualTo("四级冲刺");
        assertThat(response.daysLeft()).isEqualTo(30);
        assertThat(response.todayMissions()).extracting("route").contains("/practice", "/pro-dictation");
        assertThat(response.weeklyMilestones()).isNotEmpty();
        assertThat(response.metrics().customWords()).isEqualTo(42);
    }

    @Test
    void currentWithoutGoalReturnsSetupPrompt() {
        Long userId = 9L;
        when(proStudyGoalRepository.findByUserId(userId)).thenReturn(Optional.empty());
        when(learningStatusRepository.findByUserIdOrderByUpdatedTimeDesc(userId)).thenReturn(List.of());
        when(studyRecordRepository.findByUserIdOrderByCreatedTimeDesc(userId)).thenReturn(List.of());
        when(userWordRepository.countByUserId(userId)).thenReturn(0L);
        when(wrongWordRepository.findByUserIdOrderByLastWrongTimeDesc(userId)).thenReturn(List.of());
        when(wordRepository.count()).thenReturn(20L);

        ProGoalResponse response = proGoalService.current(userId);

        assertThat(response.hasGoal()).isFalse();
        assertThat(response.paceStatus()).isEqualTo("未设置目标");
        assertThat(response.recommendation()).contains("设置考试日期");
    }

    private LearningStatusDO status(String status, LocalDateTime nextReviewTime) {
        LearningStatusDO item = new LearningStatusDO();
        item.setUserId(9L);
        item.setWordId((long) status.hashCode());
        item.setWordType("public");
        item.setEnglish(status.toLowerCase());
        item.setStatus(status);
        item.setCorrectCount("MASTERED".equals(status) ? 3 : 0);
        item.setWrongCount("HIGH_WRONG".equals(status) ? 4 : 0);
        item.setUnknownCount(0);
        item.setNextReviewTime(nextReviewTime);
        return item;
    }

    private StudyRecordDO record(int total) {
        StudyRecordDO item = new StudyRecordDO();
        item.setUserId(9L);
        item.setTotalCount(total);
        item.setCorrectCount(total - 3);
        item.setWrongCount(3);
        item.setCreatedTime(LocalDateTime.now());
        return item;
    }
}
