package com.personal.finance.auth;

import java.time.Duration;

import com.personal.finance.common.ApiResponse;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final AuthProperties properties;

    public AuthController(AuthService authService, AuthProperties properties) {
        this.authService = authService;
        this.properties = properties;
    }

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest servletRequest,
            HttpServletResponse response) {
        LoginResponse loginResponse = authService.login(
                request.username(),
                request.password(),
                servletRequest.getRemoteAddr(),
                (token, expiresAt) -> {
                    ResponseCookie cookie = ResponseCookie.from(properties.cookieName(), token)
                            .httpOnly(true)
                            .secure(properties.cookieSecure())
                            .sameSite("Lax")
                            .path("/")
                            .maxAge(Duration.ofHours(properties.sessionHours()))
                            .build();
                    response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
                });

        return ApiResponse.success(loginResponse);
    }

    @GetMapping("/me")
    public ApiResponse<LoginResponse> me() {
        return ApiResponse.success(new LoginResponse(authService.username(), null));
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(HttpServletRequest request, HttpServletResponse response) {
        authService.logout(readCookie(request));
        ResponseCookie cookie = ResponseCookie.from(properties.cookieName(), "")
                .httpOnly(true)
                .secure(properties.cookieSecure())
                .sameSite("Lax")
                .path("/")
                .maxAge(0)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        return ApiResponse.success(null);
    }

    private String readCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }
        for (Cookie cookie : cookies) {
            if (properties.cookieName().equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }
}
