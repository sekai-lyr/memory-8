package com.example.wordlearning.control;

import com.example.wordlearning.model.ApiResponse;
import com.example.wordlearning.model.UserWordRequest;
import com.example.wordlearning.model.ImportResultResponse;
import com.example.wordlearning.model.WordResponse;
import com.example.wordlearning.service.UserWordService;
import jakarta.validation.Valid;
import java.io.IOException;
import java.util.List;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/user-words")
public class UserWordControl extends BaseControl {
    private final UserWordService userWordService;

    public UserWordControl(UserWordService userWordService) {
        this.userWordService = userWordService;
    }

    @GetMapping
    public ApiResponse<List<WordResponse>> list(@RequestParam(required = false) String keyword) {
        return ApiResponse.success(userWordService.list(requireUserId(), keyword));
    }

    @PostMapping
    public ApiResponse<WordResponse> add(@Valid @RequestBody UserWordRequest request) {
        return ApiResponse.success(userWordService.add(requireUserId(), request));
    }

    @PutMapping("/{id}")
    public ApiResponse<WordResponse> update(@PathVariable Long id,
                                            @Valid @RequestBody UserWordRequest request) {
        return ApiResponse.success(userWordService.update(requireUserId(), id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        userWordService.delete(requireUserId(), id);
        return ApiResponse.success(null);
    }

    @PostMapping("/import")
    public ApiResponse<ImportResultResponse> importCsv(@RequestParam("file") MultipartFile file) throws IOException {
        return ApiResponse.success(userWordService.importCsv(requireUserId(), file));
    }
}

