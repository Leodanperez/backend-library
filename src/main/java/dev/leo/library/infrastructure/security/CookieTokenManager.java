package dev.leo.library.infrastructure.security;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Arrays;

@Component
public class CookieTokenManager {

    @Value("${jwt.access-expiration:1800000}")
    private long accessExpMs;

    @Value("${jwt.refresh-expiration:86400000}")
    private long refreshExpMs;

    @Value("${app.cookie.secure:false}")
    private boolean secureCookie;

    private static final String ACCESS_TOKEN = "access_token";
    private static final String REFRESH_TOKEN = "refresh_token";

    public void addTokenCookies(HttpServletResponse response, String accessToken, String refreshToken) {
        response.addHeader(HttpHeaders.SET_COOKIE, buildCookie(ACCESS_TOKEN, accessToken, accessExpMs).toString());
        response.addHeader(HttpHeaders.SET_COOKIE, buildCookie(REFRESH_TOKEN, refreshToken, refreshExpMs).toString());
    }

    public void clearTokenCookies(HttpServletResponse response) {
        response.addHeader(HttpHeaders.SET_COOKIE, buildCookie(ACCESS_TOKEN, "", 0).toString());
        response.addHeader(HttpHeaders.SET_COOKIE, buildCookie(REFRESH_TOKEN, "", 0).toString());
    }

    public String extractCookie(HttpServletRequest request, String name) {
        if (request.getCookies() == null) return null;
        return Arrays.stream(request.getCookies())
                .filter(c -> name.equals(c.getName()))
                .map(Cookie::getValue)
                .findFirst().orElse(null);
    }

    private ResponseCookie buildCookie(String name, String value, long maxAgeMs) {
        String path = REFRESH_TOKEN.equals(name) ? "/api/v1/auth" : "/";
        return ResponseCookie.from(name, value)
                .httpOnly(true).secure(secureCookie).sameSite("Strict")
                .path(path).maxAge(Duration.ofMillis(maxAgeMs)).build();
    }
}
