package com.example.wordlearning.model;

public class WordResponse {
    private Long id;
    private Long wordId;
    private String english;
    private String chinese;
    private String phonetic;
    private String exampleSentence;
    private String category;
    private String difficulty;
    private String wordType;
    private Integer wrongCount;
    private Integer unknownCount;

    public WordResponse(Long id, String english, String chinese, String phonetic, String exampleSentence,
                        String category, String difficulty, String wordType) {
        this(id, id, english, chinese, phonetic, exampleSentence, category, difficulty, wordType);
    }

    public WordResponse(Long id, Long wordId, String english, String chinese, String phonetic, String exampleSentence,
                        String category, String difficulty, String wordType) {
        this.id = id;
        this.wordId = wordId;
        this.english = english;
        this.chinese = chinese;
        this.phonetic = phonetic;
        this.exampleSentence = exampleSentence;
        this.category = category;
        this.difficulty = difficulty;
        this.wordType = wordType;
    }

    public Long getId() {
        return id;
    }

    public Long getWordId() {
        return wordId;
    }

    public String getEnglish() {
        return english;
    }

    public String getChinese() {
        return chinese;
    }

    public String getPhonetic() {
        return phonetic;
    }

    public String getExampleSentence() {
        return exampleSentence;
    }

    public String getCategory() {
        return category;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public String getWordType() {
        return wordType;
    }

    public Integer getWrongCount() {
        return wrongCount;
    }

    public void setWrongCount(Integer wrongCount) {
        this.wrongCount = wrongCount;
    }

    public Integer getUnknownCount() {
        return unknownCount;
    }

    public void setUnknownCount(Integer unknownCount) {
        this.unknownCount = unknownCount;
    }
}

