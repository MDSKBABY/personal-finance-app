package com.personal.finance.auth;

import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final AuthProperties properties;
    private final SecureRandom secureRandom = new SecureRandom();
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final Clock clock = Clock.systemUTC();
    private final Map<String, Instant> sessions = new ConcurrentHashMap<>();
    private final Map<String, LoginAttempt> loginAttempts = new ConcurrentHashMap<>();

    public AuthService(AuthProperties properties) {
        this.properties = properties;
    }

    public LoginResponse login(String username, String password, String clientIp, TokenConsumer tokenConsumer) {
        assertNotLocked(clientIp);

        if (!properties.username().equals(username) || !matchesPassword(password)) {
            recordFailure(clientIp);
            throw new IllegalArgumentException("账号或密码不正确");
        }

        loginAttempts.remove(clientIp);
        cleanupExpiredSessions();
        String token = generateToken();
        Instant expiresAt = now().plus(Duration.ofHours(properties.sessionHours()));
        sessions.put(token, expiresAt);
        tokenConsumer.accept(token, expiresAt);
        return new LoginResponse(properties.username(), expiresAt);
    }

    public boolean isValidToken(String token) {
        if (token == null || token.isBlank()) {
            return false;
        }

        Instant expiresAt = sessions.get(token);
        if (expiresAt == null) {
            return false;
        }
        if (expiresAt.isBefore(now())) {
            sessions.remove(token);
            return false;
        }
        return true;
    }

    public void logout(String token) {
        if (token != null) {
            sessions.remove(token);
        }
    }

    public String username() {
        return properties.username();
    }

    private String generateToken() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private void cleanupExpiredSessions() {
        Instant now = now();
        sessions.entrySet().removeIf(entry -> entry.getValue().isBefore(now));
    }

    private boolean matchesPassword(String password) {
        if (properties.passwordHash() != null && !properties.passwordHash().isBlank()) {
            return passwordEncoder.matches(password, properties.passwordHash());
        }
        return properties.password().equals(password);
    }

    private void assertNotLocked(String clientIp) {
        LoginAttempt attempt = loginAttempts.get(clientIp);
        if (attempt == null || attempt.lockedUntil() == null) {
            return;
        }

        if (attempt.lockedUntil().isAfter(now())) {
            throw new TooManyLoginAttemptsException("登录失败次数过多，请稍后再试");
        }
        loginAttempts.remove(clientIp);
    }

    private void recordFailure(String clientIp) {
        LoginAttempt current = loginAttempts.get(clientIp);
        int failedCount = current == null ? 1 : current.failedCount() + 1;
        Instant lockedUntil = failedCount >= properties.maxFailedAttempts()
                ? now().plus(Duration.ofMinutes(properties.lockMinutes()))
                : null;
        loginAttempts.put(clientIp, new LoginAttempt(failedCount, lockedUntil));
    }

    private Instant now() {
        return Instant.now(clock);
    }

    private record LoginAttempt(int failedCount, Instant lockedUntil) {
    }

    @FunctionalInterface
    public interface TokenConsumer {
        void accept(String token, Instant expiresAt);
    }
}
