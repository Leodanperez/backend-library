package dev.leo.library.application.dto.response;

import dev.leo.library.infrastructure.adapter.output.persistence.entity.ExamEntity;
import java.time.Instant;
import java.time.LocalDate;

public record ExamResponse(
        Long id,
        String subject,
        LocalDate date,
        String classroom,
        Integer durationMinutes,
        boolean active,
        Instant createdAt,
        Instant updatedAt
) {
    public static ExamResponse from(ExamEntity e) {
        return new ExamResponse(e.getId(), e.getSubject(), e.getDate(), e.getClassroom(),
                e.getDurationMinutes(), e.isActive(), e.getCreatedAt(), e.getUpdatedAt());
    }
}
