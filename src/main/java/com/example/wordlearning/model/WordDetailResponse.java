package com.example.wordlearning.model;

public class WordDetailResponse {
    private WordResponse word;
    private String status;
    private Integer correctCount;
    private Integer wrongCount;
    private Integer unknownCount;
    private Boolean mastered;

    public WordDetailResponse(WordResponse word, String status, Integer correctCount, Integer wrongCount,
                              Integer unknownCount, Boolean mastered) {
        this.word = word;
        this.status = status;
        this.correctCount = correctCount;
        this.wrongCount = wrongCount;
        this.unknownCount = unknownCount;
        this.mastered = mastered;
    }

    public WordResponse getWord() {
        return word;
    }

    public String getStatus() {
        return status;
    }

    public Integer getCorrectCount() {
        return correctCount;
    }

    public Integer getWrongCount() {
        return wrongCount;
    }

    public Integer getUnknownCount() {
        return unknownCount;
    }

    public Boolean getMastered() {
        return mastered;
    }
}
