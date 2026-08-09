package com.example.wordlearning.control;

import com.example.wordlearning.model.ApiResponse;
import com.example.wordlearning.model.CheckAnswerRequest;
import com.example.wordlearning.model.CheckAnswerResponse;
import com.example.wordlearning.model.RandomWordResponse;
import com.example.wordlearning.model.WordDetailResponse;
import com.example.wordlearning.model.WordResponse;
import com.example.wordlearning.model.WordStatsResponse;
import com.example.wordlearning.service.WordService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/words")
public class WordControl extends BaseControl {
    private final WordService wordService;

    public WordControl(WordService wordService) {
        this.wordService = wordService;
    }

    @GetMapping("/random")
    public ApiResponse<RandomWordResponse> random(@RequestParam(required = false) String category) {
        return ApiResponse.success(wordService.randomWord(category));
    }

    @GetMapping("/random-list")
    public ApiResponse<List<RandomWordResponse>> randomList(@RequestParam(defaultValue = "10") int count,
                                                            @RequestParam(required = false) String category) {
        return ApiResponse.success(wordService.randomWords(count, category));
    }

    @GetMapping("/smart-queue")
    public ApiResponse<List<RandomWordResponse>> smartQueue(@RequestParam(defaultValue = "10") int count) {
        return ApiResponse.success(wordService.smartQueue(requireUserId(), count));
    }

    @PostMapping("/check")
    public ApiResponse<CheckAnswerResponse> check(@Valid @RequestBody CheckAnswerRequest request) {
        return ApiResponse.success(wordService.checkAnswer(request));
    }

    @GetMapping("/search")
    public ApiResponse<List<WordResponse>> search(@RequestParam String keyword,
                                                  @RequestParam(defaultValue = "100") int limit) {
        return ApiResponse.success(wordService.search(keyword, requireUserId(), limit));
    }

    @GetMapping("/list")
    public ApiResponse<List<WordResponse>> list(@RequestParam(required = false) String category,
                                                @RequestParam(defaultValue = "300") int limit) {
        return ApiResponse.success(wordService.list(category, limit));
    }

    @GetMapping("/categories")
    public ApiResponse<List<String>> categories() {
        return ApiResponse.success(wordService.categories());
    }

    @GetMapping("/stats")
    public ApiResponse<WordStatsResponse> stats() {
        return ApiResponse.success(wordService.stats());
    }

    @GetMapping("/detail")
    public ApiResponse<WordDetailResponse> detail(@RequestParam Long wordId,
                                                  @RequestParam(defaultValue = "public") String wordType) {
        return ApiResponse.success(wordService.detail(requireUserId(), wordId, wordType));
    }
}

