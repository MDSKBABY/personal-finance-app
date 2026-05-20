package com.personal.finance.auth;

import java.time.Instant;

public record LoginResponse(String username, Instant expiresAt) {
}
