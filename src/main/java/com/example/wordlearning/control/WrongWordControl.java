package com.example.wordlearning.control;

import com.example.wordlearning.model.ApiResponse;
import com.example.wordlearning.model.WordBookRequest;
import com.example.wordlearning.model.WordResponse;
import com.example.wordlearning.service.WrongWordService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/wrong-words")
public class WrongWordControl extends BaseControl {
    private final WrongWordService wrongWordService;

    public WrongWordControl(WrongWordService wrongWordService) {
        this.wrongWordService = wrongWordService;
    }

    @GetMapping
    public ApiResponse<List<WordResponse>> list() {
        return ApiResponse.success(wrongWordService.list(requireUserId()));
    }

    @PostMapping
    public ApiResponse<WordResponse> add(@Valid @RequestBody WordBookRequest request) {
        return ApiResponse.success(wrongWordService.add(requireUserId(), request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        wrongWordService.delete(requireUserId(), id);
        return ApiResponse.success(null);
    }

    @DeleteMapping("/clear")
    public ApiResponse<Void> clear() {
        wrongWordService.clear(requireUserId());
        return ApiResponse.success(null);
    }
}

