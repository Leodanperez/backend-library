package dev.leo.library.shared.dto;

import java.time.LocalDateTime;

public record SuccessResponse(
        int statusCode,
        String message,
        LocalDateTime timestamp
) {
    public static SuccessResponse of(int statusCode, String message) {
        return new SuccessResponse(statusCode, message, LocalDateTime.now());
    }
}
