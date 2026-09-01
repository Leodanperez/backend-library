package dev.leo.library.infrastructure.adapter.input.rest;

import dev.leo.library.application.dto.request.LoginRequest;
import dev.leo.library.application.dto.response.UserResponse;
import dev.leo.library.domain.exception.UserNotFoundException;
import dev.leo.library.domain.port.input.UserUseCase;
import dev.leo.library.infrastructure.adapter.output.persistence.entity.UserEntity;
import dev.leo.library.infrastructure.security.CookieTokenManager;
import dev.leo.library.infrastructure.security.JwtUtil;
import dev.leo.library.infrastructure.security.TokenBlacklistService;
import dev.leo.library.infrastructure.security.UserPrincipal;
import dev.leo.library.shared.dto.SuccessResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserUseCase userUseCase;
    private final JwtUtil jwtUtil;
    private final CookieTokenManager cookieTokenManager;
    private final TokenBlacklistService tokenBlacklistService;

    @GetMapping("/me")
    public ResponseEntity<UserResponse> me(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(UserResponse.from(principal.user()));
    }

    @PostMapping("/login")
    public ResponseEntity<SuccessResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletResponse response) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password()));
        UserEntity user;
        try {
            user = userUseCase.findByEmail(request.email());
        } catch (UserNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
        cookieTokenManager.addTokenCookies(response, jwtUtil.generateAccessToken(user), jwtUtil.generateRefreshToken(user));
        return ResponseEntity.ok(SuccessResponse.of(200, "Sesión iniciada correctamente"));
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<SuccessResponse> refreshToken(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = cookieTokenManager.extractCookie(request, "refresh_token");
        if (refreshToken == null)
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token de refresco no encontrado");
        try {
            String tokenType = jwtUtil.validateToken(refreshToken).get("token_type", String.class);
            if (!"refresh".equals(tokenType))
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Tipo de token no válido");
            String email = jwtUtil.extractUsername(refreshToken);
            UserEntity user;
            try {
                user = userUseCase.findByEmail(email);
            } catch (UserNotFoundException e) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
            }
            cookieTokenManager.addTokenCookies(response, jwtUtil.generateAccessToken(user), jwtUtil.generateRefreshToken(user));
            return ResponseEntity.ok(SuccessResponse.of(200, "Token renovado correctamente"));
        } catch (ResponseStatusException e) {
            cookieTokenManager.clearTokenCookies(response);
            throw e;
        } catch (Exception e) {
            cookieTokenManager.clearTokenCookies(response);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token de refresco inválido");
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<SuccessResponse> logout(HttpServletRequest request, HttpServletResponse response) {
        String accessToken = cookieTokenManager.extractCookie(request, "access_token");
        if (accessToken != null) {
            try { tokenBlacklistService.blacklist(jwtUtil.extractJti(accessToken)); } catch (Exception ignored) {}
        }
        cookieTokenManager.clearTokenCookies(response);
        return ResponseEntity.ok(SuccessResponse.of(200, "Sesión cerrada correctamente"));
    }
}
