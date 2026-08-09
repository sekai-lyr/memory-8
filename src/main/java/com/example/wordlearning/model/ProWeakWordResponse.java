package com.example.wordlearning.model;

import java.time.LocalDateTime;

public record ProWeakWordResponse(
        Long wordId,
        String wordType,
        String english,
        String chinese,
        int wrongCount,
        int unknownCount,
        LocalDateTime lastWrongTime
) {
}
