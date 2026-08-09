package com.example.wordlearning.service;

import com.example.wordlearning.dataobject.UserDO;
import com.example.wordlearning.model.AuthenticatedUser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtService {
    private static final String HMAC_ALGORITHM = "HmacSHA256";
    private static final Base64.Encoder ENCODER = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder DECODER = Base64.getUrlDecoder();

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final byte[] secret;
    private final long expirationMinutes;

    public JwtService(@Value("${app.security.jwt-secret}") String secret,
                      @Value("${app.security.jwt-expiration-minutes:1440}") long expirationMinutes) {
        this.secret = secret.getBytes(StandardCharsets.UTF_8);
        this.expirationMinutes = expirationMinutes;
    }

    public String generateToken(UserDO user) {
        Instant now = Instant.now();
        Map<String, Object> header = Map.of("alg", "HS256", "typ", "JWT");
        Map<String, Object> payload = Map.of(
                "sub", user.getId().toString(),
                "username", user.getUsername(),
                "iat", now.getEpochSecond(),
                "exp", now.plusSeconds(expirationMinutes * 60).getEpochSecond()
        );
        String body = encodeJson(header) + "." + encodeJson(payload);
        return body + "." + sign(body);
    }

    public Optional<AuthenticatedUser> parseToken(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                return Optional.empty();
            }
            String body = parts[0] + "." + parts[1];
            if (!MessageDigest.isEqual(sign(body).getBytes(StandardCharsets.UTF_8),
                    parts[2].getBytes(StandardCharsets.UTF_8))) {
                return Optional.empty();
            }
            JsonNode payload = objectMapper.readTree(DECODER.decode(parts[1]));
            long exp = payload.path("exp").asLong(0L);
            if (exp <= Instant.now().getEpochSecond()) {
                return Optional.empty();
            }
            Long userId = Long.valueOf(payload.path("sub").asText());
            String username = payload.path("username").asText();
            return Optional.of(new AuthenticatedUser(userId, username));
        } catch (Exception ex) {
            return Optional.empty();
        }
    }

    private String encodeJson(Map<String, Object> value) {
        try {
            return ENCODER.encodeToString(objectMapper.writeValueAsBytes(value));
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to encode token", ex);
        }
    }

    private String sign(String value) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(new SecretKeySpec(secret, HMAC_ALGORITHM));
            return ENCODER.encodeToString(mac.doFinal(value.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to sign token", ex);
        }
    }
}
