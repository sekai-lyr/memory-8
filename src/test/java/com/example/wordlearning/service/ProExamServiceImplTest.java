package com.example.wordlearning.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.wordlearning.dataobject.LearningStatusDO;
import com.example.wordlearning.dataobject.StudyRecordDO;
import com.example.wordlearning.dataobject.WordDO;
import com.example.wordlearning.dataobject.WrongWordDO;
import com.example.wordlearning.model.ProExamAnswerRequest;
import com.example.wordlearning.model.ProExamPaperResponse;
import com.example.wordlearning.model.ProExamSubmitRequest;
import com.example.wordlearning.model.ProExamSubmitResponse;
import com.example.wordlearning.repository.LearningStatusRepository;
import com.example.wordlearning.repository.StudyRecordRepository;
import com.example.wordlearning.repository.UserWordRepository;
import com.example.wordlearning.repository.WordRepository;
import com.example.wordlearning.repository.WrongWordRepository;
import com.example.wordlearning.service.impl.ProExamServiceImpl;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProExamServiceImplTest {
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
    private StudyRecordRepository studyRecordRepository;

    private ProExamService proExamService;

    @BeforeEach
    void setUp() {
        proExamService = new ProExamServiceImpl(
                membershipService,
                learningStatusRepository,
                wrongWordRepository,
                userWordRepository,
                wordRepository,
                studyRecordRepository
        );
    }

    @Test
    void paperPrefersDueLearningStatusAndBuildsOptions() {
        Long userId = 11L;
        WordDO target = word(1L, "accurate", "准确的");
        when(learningStatusRepository.findByUserIdOrderByUpdatedTimeDesc(userId))
                .thenReturn(List.of(status(1L, "accurate", "HIGH_WRONG")));
        when(wordRepository.findById(1L)).thenReturn(Optional.of(target));
        when(wrongWordRepository.findByUserIdOrderByLastWrongTimeDesc(userId)).thenReturn(List.of());
        when(userWordRepository.findByUserIdOrderByIdDesc(userId)).thenReturn(List.of());
        when(wordRepository.findRandomList(4)).thenReturn(List.of());
        when(wordRepository.findRandomList(18)).thenReturn(List.of(
                word(2L, "benefit", "好处"),
                word(3L, "challenge", "挑战"),
                word(4L, "wisdom", "智慧")
        ));

        ProExamPaperResponse response = proExamService.paper(userId, 5);

        verify(membershipService).requirePro(userId, "会员模考冲刺");
        assertThat(response.questions()).hasSize(1);
        assertThat(response.questions().get(0).english()).isEqualTo("accurate");
        assertThat(response.questions().get(0).options()).contains("准确的", "好处", "挑战", "智慧");
    }

    @Test
    void submitScoresAnswersAndRecordsWrongWords() {
        Long userId = 11L;
        WordDO accurate = word(1L, "accurate", "准确的");
        WordDO wisdom = word(2L, "wisdom", "智慧");
        when(wordRepository.findById(1L)).thenReturn(Optional.of(accurate));
        when(wordRepository.findById(2L)).thenReturn(Optional.of(wisdom));
        when(learningStatusRepository.findByUserIdAndWordIdAndWordType(userId, 1L, "public"))
                .thenReturn(Optional.empty());
        when(learningStatusRepository.findByUserIdAndWordIdAndWordType(userId, 2L, "public"))
                .thenReturn(Optional.empty());
        when(wrongWordRepository.findByUserIdAndWordIdAndWordType(userId, 2L, "public"))
                .thenReturn(Optional.empty());
        when(learningStatusRepository.save(any(LearningStatusDO.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(wrongWordRepository.save(any(WrongWordDO.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(studyRecordRepository.save(any(StudyRecordDO.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ProExamSubmitResponse response = proExamService.submit(userId, new ProExamSubmitRequest(300, List.of(
                new ProExamAnswerRequest(1L, "public", "准确的"),
                new ProExamAnswerRequest(2L, "public", "挑战")
        )));

        assertThat(response.totalCount()).isEqualTo(2);
        assertThat(response.correctCount()).isEqualTo(1);
        assertThat(response.wrongCount()).isEqualTo(1);
        assertThat(response.weakPoints()).containsExactly("wisdom");
        assertThat(response.results()).extracting("correct").containsExactly(true, false);
    }

    private LearningStatusDO status(Long wordId, String english, String status) {
        LearningStatusDO item = new LearningStatusDO();
        item.setUserId(11L);
        item.setWordId(wordId);
        item.setWordType("public");
        item.setEnglish(english);
        item.setStatus(status);
        item.setWrongCount(4);
        item.setUnknownCount(1);
        item.setCorrectCount(0);
        item.setNextReviewTime(LocalDateTime.now().minusDays(1));
        return item;
    }

    private WordDO word(Long id, String english, String chinese) {
        WordDO item = new WordDO();
        item.setId(id);
        item.setEnglish(english);
        item.setChinese(chinese);
        return item;
    }
}
