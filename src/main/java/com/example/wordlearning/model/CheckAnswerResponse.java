package com.example.wordlearning.model;

public class CheckAnswerResponse {
    private boolean correct;
    private String message;
    private String correctAnswer;
    private Long wordId;
    private String wordType;
    private String english;

    public CheckAnswerResponse(boolean correct, String message, String correctAnswer, Long wordId, String wordType, String english) {
        this.correct = correct;
        this.message = message;
        this.correctAnswer = correctAnswer;
        this.wordId = wordId;
        this.wordType = wordType;
        this.english = english;
    }

    public boolean isCorrect() {
        return correct;
    }

    public String getMessage() {
        return message;
    }

    public String getCorrectAnswer() {
        return correctAnswer;
    }

    public Long getWordId() {
        return wordId;
    }

    public String getWordType() {
        return wordType;
    }

    public String getEnglish() {
        return english;
    }
}

