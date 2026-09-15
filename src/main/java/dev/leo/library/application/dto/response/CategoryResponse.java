package dev.leo.library.application.dto.response;

import dev.leo.library.infrastructure.adapter.output.persistence.entity.CategoryEntity;
import java.time.Instant;

public record CategoryResponse(
        Long id,
        String name,
        String description,
        boolean active,
        long bookCount,
        Instant createdAt,
        Instant updatedAt
) {
    public static CategoryResponse from(CategoryEntity c, long bookCount) {
        return new CategoryResponse(c.getId(), c.getName(), c.getDescription(),
                c.isActive(), bookCount, c.getCreatedAt(), c.getUpdatedAt());
    }
}
