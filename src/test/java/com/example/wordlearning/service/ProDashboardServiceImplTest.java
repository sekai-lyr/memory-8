package com.example.wordlearning.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.wordlearning.dataobject.FavoriteWordDO;
import com.example.wordlearning.dataobject.LearningStatusDO;
import com.example.wordlearning.dataobject.StudyRecordDO;
import com.example.wordlearning.dataobject.WrongWordDO;
import com.example.wordlearning.model.ProDashboardResponse;
import com.example.wordlearning.repository.FavoriteWordRepository;
import com.example.wordlearning.repository.LearningStatusRepository;
import com.example.wordlearning.repository.StudyRecordRepository;
import com.example.wordlearning.repository.UserWordRepository;
import com.example.wordlearning.repository.WordRepository;
import com.example.wordlearning.repository.WrongWordRepository;
import com.example.wordlearning.service.impl.ProDashboardServiceImpl;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProDashboardServiceImplTest {
    @Mock
    private MembershipService membershipService;

    @Mock
    private LearningStatusRepository learningStatusRepository;

    @Mock
    private WrongWordRepository wrongWordRepository;

    @Mock
    private UserWordRepository userWordRepository;

    @Mock
    private WordRepository wordRepository;

    @Mock
    private FavoriteWordRepository favoriteWordRepository;

    @Mock
    private StudyRecordRepository studyRecordRepository;

    private ProDashboardService proDashboardService;

    @BeforeEach
    void setUp() {
        proDashboardService = new ProDashboardServiceImpl(
                membershipService,
                learningStatusRepository,
                wrongWordRepository,
                userWordRepository,
                wordRepository,
                favoriteWordRepository,
                studyRecordRepository
        );
    }

    @Test
    void dashboardBuildsPersonalProPlanFromLearningData() {
        Long userId = 7L;
        when(learningStatusRepository.findByUserIdOrderByUpdatedTimeDesc(userId))
                .thenReturn(List.of(
                        status("HIGH_WRONG", 1, 4, 1, LocalDateTime.now().minusDays(1)),
                        status("UNKNOWN", 0, 1, 2, LocalDateTime.now().plusDays(1)),
                        status("MASTERED", 5, 0, 0, LocalDateTime.now().plusDays(30))
                ));
        when(wrongWordRepository.findByUserIdOrderByLastWrongTimeDesc(userId))
                .thenReturn(List.of(
                        wrongWord(1L, "fragile", "易碎的", 2, 0),
                        wrongWord(2L, "ambiguous", "模糊的", 5, 1)
                ));
        when(studyRecordRepository.findByUserIdOrderByCreatedTimeDesc(userId))
                .thenReturn(List.of(record(10, 8), record(20, 15)));
        when(userWordRepository.countByUserId(userId)).thenReturn(36L);
        when(favoriteWordRepository.findByUserIdOrderByCreatedTimeDesc(userId)).thenReturn(List.of(new FavoriteWordDO()));

        ProDashboardResponse response = proDashboardService.dashboard(userId);

        verify(membershipService).requirePro(userId, "会员学习教练");
        assertThat(response.focusTitle()).isEqualTo("今天优先清空到期复习");
        assertThat(response.dailyTarget()).isEqualTo(16);
        assertThat(response.metrics().averageAccuracy()).isEqualByComparingTo("76.67");
        assertThat(response.weakWords()).extracting("english").containsExactly("ambiguous", "fragile");
        assertThat(response.weeklyPlan()).hasSize(7);
        assertThat(response.privileges()).contains("会员学习报告", "7 天复习计划", "会员听写训练");
    }

    private LearningStatusDO status(String status, int correct, int wrong, int unknown, LocalDateTime nextReviewTime) {
        LearningStatusDO item = new LearningStatusDO();
        item.setUserId(7L);
        item.setWordId((long) status.hashCode());
        item.setWordType("public");
        item.setEnglish(status.toLowerCase());
        item.setStatus(status);
        item.setCorrectCount(correct);
        item.setWrongCount(wrong);
        item.setUnknownCount(unknown);
        item.setNextReviewTime(nextReviewTime);
        return item;
    }

    private WrongWordDO wrongWord(Long wordId, String english, String chinese, int wrongCount, int unknownCount) {
        WrongWordDO item = new WrongWordDO();
        item.setUserId(7L);
        item.setWordId(wordId);
        item.setWordType("public");
        item.setEnglish(english);
        item.setChinese(chinese);
        item.setWrongCount(wrongCount);
        item.setUnknownCount(unknownCount);
        item.setLastWrongTime(LocalDateTime.now());
        return item;
    }

    private StudyRecordDO record(int total, int correct) {
        StudyRecordDO item = new StudyRecordDO();
        item.setUserId(7L);
        item.setTotalCount(total);
        item.setCorrectCount(correct);
        item.setWrongCount(total - correct);
        item.setCreatedTime(LocalDateTime.now());
        return item;
    }
}
