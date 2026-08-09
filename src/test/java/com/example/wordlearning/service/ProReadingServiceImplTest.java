package com.example.wordlearning.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.wordlearning.dataobject.LearningStatusDO;
import com.example.wordlearning.dataobject.UserWordDO;
import com.example.wordlearning.dataobject.WordDO;
import com.example.wordlearning.model.ImportResultResponse;
import com.example.wordlearning.model.ProReadingAnalyzeRequest;
import com.example.wordlearning.model.ProReadingImportRequest;
import com.example.wordlearning.model.ProReadingImportWordRequest;
import com.example.wordlearning.model.ProReadingResponse;
import com.example.wordlearning.repository.LearningStatusRepository;
import com.example.wordlearning.repository.UserWordRepository;
import com.example.wordlearning.repository.WordRepository;
import com.example.wordlearning.service.impl.ProReadingServiceImpl;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProReadingServiceImplTest {
    @Mock
    private MembershipService membershipService;

    @Mock
    private WordRepository wordRepository;

    @Mock
    private UserWordRepository userWordRepository;

    @Mock
    private LearningStatusRepository learningStatusRepository;

    private ProReadingService proReadingService;

    @BeforeEach
    void setUp() {
        proReadingService = new ProReadingServiceImpl(
                membershipService,
                wordRepository,
                userWordRepository,
                learningStatusRepository
        );
    }

    @Test
    void analyzeTurnsArticleIntoPrioritizedWordPack() {
        Long userId = 18L;
        when(learningStatusRepository.findByUserIdOrderByUpdatedTimeDesc(userId))
                .thenReturn(List.of(status("feedback", "MASTERED", 0, 0)));
        when(userWordRepository.findByUserIdOrderByIdDesc(userId))
                .thenReturn(List.of(userWord("intelligence")));
        when(wordRepository.findFirstByEnglishIgnoreCase("adaptive")).thenReturn(Optional.of(word("adaptive", "适应性的", "hard")));
        when(wordRepository.findFirstByEnglishIgnoreCase("feedback")).thenReturn(Optional.of(word("feedback", "反馈", "medium")));
        when(wordRepository.findFirstByEnglishIgnoreCase("improves")).thenReturn(Optional.of(word("improves", "改进；提升", "medium")));
        when(wordRepository.findFirstByEnglishIgnoreCase("intelligence")).thenReturn(Optional.of(word("intelligence", "智能；智力", "hard")));

        ProReadingResponse response = proReadingService.analyze(userId,
                new ProReadingAnalyzeRequest("AI 阅读", "Adaptive feedback improves adaptive intelligence.", 20));

        verify(membershipService).requirePro(userId, "会员文章生词解析");
        assertThat(response.title()).isEqualTo("AI 阅读");
        assertThat(response.totalTokens()).isEqualTo(5);
        assertThat(response.uniqueWords()).isEqualTo(4);
        assertThat(response.matchedWords()).isEqualTo(4);
        assertThat(response.words()).extracting("english").contains("adaptive", "feedback", "intelligence");
        assertThat(response.words().get(0).english()).isEqualTo("adaptive");
        assertThat(response.words()).filteredOn("alreadyInUserWords", true).extracting("english").containsExactly("intelligence");
        assertThat(response.words()).filteredOn("mastered", true).extracting("english").containsExactly("feedback");
    }

    @Test
    void importWordsSavesSelectedWordsAndSkipsDuplicates() {
        Long userId = 18L;
        when(userWordRepository.existsByUserIdAndEnglishIgnoreCase(userId, "adaptive")).thenReturn(false);
        when(userWordRepository.existsByUserIdAndEnglishIgnoreCase(userId, "feedback")).thenReturn(true);

        ImportResultResponse response = proReadingService.importWords(userId,
                new ProReadingImportRequest("AI 阅读", List.of(
                        new ProReadingImportWordRequest("adaptive", "适应性的", null, "Adaptive learning works.", null, "hard"),
                        new ProReadingImportWordRequest("feedback", "反馈", null, null, null, "medium")
                )));

        verify(membershipService).requirePro(userId, "文章生词一键加入词库");
        verify(userWordRepository, times(1)).save(any(UserWordDO.class));
        assertThat(response.getSuccessCount()).isEqualTo(1);
        assertThat(response.getSkippedCount()).isEqualTo(1);
    }

    private WordDO word(String english, String chinese, String difficulty) {
        WordDO word = new WordDO();
        word.setId((long) english.hashCode());
        word.setEnglish(english);
        word.setChinese(chinese);
        word.setDifficulty(difficulty);
        word.setCategory("ECDICT 常用词");
        word.setExampleSentence("Example for " + english + ".");
        return word;
    }

    private LearningStatusDO status(String english, String status, int wrongCount, int unknownCount) {
        LearningStatusDO item = new LearningStatusDO();
        item.setUserId(18L);
        item.setWordId((long) english.hashCode());
        item.setWordType("public");
        item.setEnglish(english);
        item.setStatus(status);
        item.setWrongCount(wrongCount);
        item.setUnknownCount(unknownCount);
        item.setCorrectCount("MASTERED".equals(status) ? 5 : 0);
        return item;
    }

    private UserWordDO userWord(String english) {
        UserWordDO item = new UserWordDO();
        item.setUserId(18L);
        item.setEnglish(english);
        item.setChinese("用户词");
        return item;
    }
}
