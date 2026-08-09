package com.example.wordlearning.service.impl;

import com.example.wordlearning.dataobject.UserDO;
import com.example.wordlearning.model.LoginRequest;
import com.example.wordlearning.model.RegisterRequest;
import com.example.wordlearning.model.UserResponse;
import com.example.wordlearning.repository.UserRepository;
import com.example.wordlearning.service.AuthService;
import com.example.wordlearning.service.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Override
    public UserResponse register(RegisterRequest request) {
        String username = request.getUsername().trim();
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("两次密码必须一致");
        }
        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("用户名已存在");
        }
        UserDO userDO = new UserDO();
        userDO.setUsername(username);
        userDO.setPassword(passwordEncoder.encode(request.getPassword()));
        UserDO saved = userRepository.save(userDO);
        return toResponse(saved);
    }

    @Override
    public UserResponse login(LoginRequest request) {
        UserDO userDO = userRepository.findByUsername(request.getUsername().trim())
                .orElseThrow(() -> new IllegalArgumentException("用户名或密码错误"));
        if (!passwordEncoder.matches(request.getPassword(), userDO.getPassword())) {
            throw new IllegalArgumentException("用户名或密码错误");
        }
        return toResponse(userDO);
    }

    private UserResponse toResponse(UserDO userDO) {
        return new UserResponse(userDO.getId(), userDO.getUsername(), jwtService.generateToken(userDO));
    }
}
