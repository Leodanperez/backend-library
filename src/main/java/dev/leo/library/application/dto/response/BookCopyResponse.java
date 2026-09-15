package dev.leo.library.application.dto.response;

import dev.leo.library.domain.model.CopyCondition;
import dev.leo.library.domain.model.CopyStatus;
import dev.leo.library.infrastructure.adapter.output.persistence.entity.BookCopyEntity;
import java.math.BigDecimal;
import java.time.Instant;

public record BookCopyResponse(
        Long id,
        String code,
        CopyStatus status,
        CopyCondition condition,
        Instant acquisitionDate,
        BigDecimal price,
        String location,
        Long bookId,
        String bookTitle,
        Instant createdAt,
        Instant updatedAt
) {
    public static BookCopyResponse from(BookCopyEntity e) {
        return new BookCopyResponse(
                e.getId(), e.getCode(), e.getStatus(), e.getCondition(),
                e.getAcquisitionDate(), e.getPrice(), e.getLocation(),
                e.getBook().getId(), e.getBook().getTitle(),
                e.getCreatedAt(), e.getUpdatedAt()
        );
    }
}
