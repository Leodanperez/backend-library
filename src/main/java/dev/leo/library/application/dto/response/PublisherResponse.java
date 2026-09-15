package dev.leo.library.application.dto.response;

import dev.leo.library.infrastructure.adapter.output.persistence.entity.PublisherEntity;
import java.time.Instant;

public record PublisherResponse(
        Long id,
        String name,
        String country,
        Integer foundedYear,
        String website,
        boolean active,
        Instant createdAt,
        Instant updatedAt
) {
    public static PublisherResponse from(PublisherEntity e) {
        return new PublisherResponse(
                e.getId(), e.getName(), e.getCountry(), e.getFoundedYear(),
                e.getWebsite(), e.isActive(), e.getCreatedAt(), e.getUpdatedAt()
        );
    }
}
