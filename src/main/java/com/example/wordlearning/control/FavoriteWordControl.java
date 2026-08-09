package com.example.wordlearning.control;

import com.example.wordlearning.model.ApiResponse;
import com.example.wordlearning.model.WordBookRequest;
import com.example.wordlearning.model.WordResponse;
import com.example.wordlearning.service.FavoriteWordService;
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
@RequestMapping("/api/favorite-words")
public class FavoriteWordControl extends BaseControl {
    private final FavoriteWordService favoriteWordService;

    public FavoriteWordControl(FavoriteWordService favoriteWordService) {
        this.favoriteWordService = favoriteWordService;
    }

    @GetMapping
    public ApiResponse<List<WordResponse>> list() {
        return ApiResponse.success(favoriteWordService.list(requireUserId()));
    }

    @PostMapping
    public ApiResponse<WordResponse> add(@Valid @RequestBody WordBookRequest request) {
        return ApiResponse.success(favoriteWordService.add(requireUserId(), request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        favoriteWordService.delete(requireUserId(), id);
        return ApiResponse.success(null);
    }

    @DeleteMapping("/clear")
    public ApiResponse<Void> clear() {
        favoriteWordService.clear(requireUserId());
        return ApiResponse.success(null);
    }
}

