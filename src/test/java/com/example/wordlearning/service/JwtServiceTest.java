package com.example.wordlearning.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.wordlearning.dataobject.UserDO;
import com.example.wordlearning.model.AuthenticatedUser;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class JwtServiceTest {
    @Test
    void generatedTokenCanBeParsed() {
        JwtService jwtService = new JwtService("unit-test-secret-with-enough-entropy", 30);
        UserDO user = new UserDO();
        user.setId(7L);
        user.setUsername("alice");

        Optional<AuthenticatedUser> parsed = jwtService.parseToken(jwtService.generateToken(user));

        assertThat(parsed).isPresent();
        assertThat(parsed.get().id()).isEqualTo(7L);
        assertThat(parsed.get().username()).isEqualTo("alice");
    }

    @Test
    void tamperedTokenIsRejected() {
        JwtService jwtService = new JwtService("unit-test-secret-with-enough-entropy", 30);
        UserDO user = new UserDO();
        user.setId(7L);
        user.setUsername("alice");
        String token = jwtService.generateToken(user);
        String tampered = token.substring(0, token.length() - 1) + "x";

        Optional<AuthenticatedUser> parsed = jwtService.parseToken(tampered);

        assertThat(parsed).isEmpty();
    }
}
