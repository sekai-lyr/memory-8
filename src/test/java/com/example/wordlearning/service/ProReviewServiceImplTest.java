package com.example.wordlearning.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.wordlearning.dataobject.LearningStatusDO;
import com.example.wordlearning.model.ProReviewResponse;
import com.example.wordlearning.repository.LearningStatusRepository;
import com.example.wordlearning.service.impl.ProReviewServiceImpl;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProReviewServiceImplTest {
    @Mock
    private MembershipService membershipService;

    @Mock
    private LearningStatusRepository learningStatusRepository;

    private ProReviewService proReviewService;

    @BeforeEach
    void setUp() {
        proReviewService = new ProReviewServiceImpl(membershipService, learningStatusRepository);
    }

    @Test
    void reviewBuildsActionablePriorityPlanForProUser() {
        Long userId = 12L;
        when(learningStatusRepository.findByUserIdOrderByUpdatedTimeDesc(userId)).thenReturn(List.of(
                status("HIGH_WRONG", "fragile", 4, 1, 0, LocalDateTime.now().minusDays(2)),
                status("UNKNOWN", "implicit", 1, 3, 0, LocalDateTime.now().plusDays(1)),
                status("LEARNING", "stable", 0, 0, 2, LocalDateTime.now().minusHours(1)),
                status("MASTERED", "basic", 0, 0, 5, LocalDateTime.now().plusDays(20))
        ));

        ProReviewResponse response = proReviewService.review(userId);

        verify(membershipService).requirePro(userId, "会员智能复习中心");
        assertThat(response.focusTitle()).isEqualTo("今天先清空到期复习");
        assertThat(response.metrics().dueReviews()).isEqualTo(2);
        assertThat(response.metrics().overdueReviews()).isEqualTo(1);
        assertThat(response.metrics().highWrong()).isEqualTo(1);
        assertThat(response.metrics().unknown()).isEqualTo(1);
        assertThat(response.priorityWords()).extracting("english").containsExactly("fragile", "stable", "implicit");
        assertThat(response.buckets()).extracting("key").contains("due", "weak", "future");
        assertThat(response.upcomingDays()).hasSize(7);
        assertThat(response.vipReasons()).contains("提前看到未来 7 天复习压力");
    }

    @Test
    void reviewExplainsSetupWhenNoLearningDataExists() {
        Long userId = 12L;
        when(learningStatusRepository.findByUserIdOrderByUpdatedTimeDesc(userId)).thenReturn(List.of());

        ProReviewResponse response = proReviewService.review(userId);

        assertThat(response.focusTitle()).isEqualTo("先做一轮练习，建立诊断数据");
        assertThat(response.metrics().totalTracked()).isZero();
        assertThat(response.priorityWords()).isEmpty();
        assertThat(response.recommendedCount()).isEqualTo(12);
    }

    private LearningStatusDO status(String status, String english, int wrong, int unknown,
                                    int correct, LocalDateTime nextReviewTime) {
        LearningStatusDO item = new LearningStatusDO();
        item.setUserId(12L);
        item.setWordId((long) english.hashCode());
        item.setWordType("public");
        item.setEnglish(english);
        item.setStatus(status);
        item.setWrongCount(wrong);
        item.setUnknownCount(unknown);
        item.setCorrectCount(correct);
        item.setReviewIntervalDays(1);
        item.setNextReviewTime(nextReviewTime);
        item.setLastPracticedTime(LocalDateTime.now());
        return item;
    }
}
