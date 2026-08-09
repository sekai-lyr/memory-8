package com.example.wordlearning.service;

import com.example.wordlearning.model.LoginRequest;
import com.example.wordlearning.model.RegisterRequest;
import com.example.wordlearning.model.UserResponse;

public interface AuthService {
    UserResponse register(RegisterRequest request);

    UserResponse login(LoginRequest request);
}

