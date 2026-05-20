package com.personal.finance.auth;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.auth")
public record AuthProperties(
        String username,
        String password,
        String passwordHash,
        String cookieName,
        long sessionHours,
        boolean cookieSecure,
        int maxFailedAttempts,
        long lockMinutes) {

    public AuthProperties {
        if (username == null || username.isBlank()) {
            username = "admin";
        }
        boolean missingPassword = password == null || password.isBlank();
        boolean missingPasswordHash = passwordHash == null || passwordHash.isBlank();
        if (missingPassword && missingPasswordHash) {
            throw new IllegalStateException("请配置 APP_AUTH_PASSWORD_HASH、APP_AUTH_PASSWORD，或使用 local profile 的本地登录配置");
        }
        if (cookieName == null || cookieName.isBlank()) {
            cookieName = "finance_auth";
        }
        if (sessionHours <= 0) {
            sessionHours = 12;
        }
        if (maxFailedAttempts <= 0) {
            maxFailedAttempts = 5;
        }
        if (lockMinutes <= 0) {
            lockMinutes = 10;
        }
    }
}
