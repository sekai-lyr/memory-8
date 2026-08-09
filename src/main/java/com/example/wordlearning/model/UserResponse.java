package com.example.wordlearning.model;

public class UserResponse {
    private Long id;
    private String username;
    private String token;

    public UserResponse(Long id, String username) {
        this.id = id;
        this.username = username;
    }

    public UserResponse(Long id, String username, String token) {
        this.id = id;
        this.username = username;
        this.token = token;
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getToken() {
        return token;
    }
}

