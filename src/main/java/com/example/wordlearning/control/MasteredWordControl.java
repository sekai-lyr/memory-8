package com.example.wordlearning.control;

import com.example.wordlearning.model.ApiResponse;
import com.example.wordlearning.model.WordBookRequest;
import com.example.wordlearning.service.MasteredWordService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/mastered-words")
public class MasteredWordControl extends BaseControl {
    private final MasteredWordService masteredWordService;

    public MasteredWordControl(MasteredWordService masteredWordService) {
        this.masteredWordService = masteredWordService;
    }

    @GetMapping("/keys")
    public ApiResponse<List<String>> keys() {
        return ApiResponse.success(masteredWordService.keys(requireUserId()));
    }

    @PostMapping
    public ApiResponse<Void> add(@Valid @RequestBody WordBookRequest request) {
        masteredWordService.add(requireUserId(), request);
        return ApiResponse.success(null);
    }

    @DeleteMapping
    public ApiResponse<Void> delete(@RequestParam Long wordId,
                                    @RequestParam String wordType) {
        masteredWordService.delete(requireUserId(), wordId, wordType);
        return ApiResponse.success(null);
    }
}
