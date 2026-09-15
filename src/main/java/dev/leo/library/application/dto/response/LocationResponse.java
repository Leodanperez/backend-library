package dev.leo.library.application.dto.response;

import dev.leo.library.infrastructure.adapter.output.persistence.entity.LocationEntity;
import java.time.Instant;

public record LocationResponse(
        Long id,
        String name,
        Integer floor,
        Integer capacity,
        String description,
        boolean active,
        Instant createdAt,
        Instant updatedAt
) {
    public static LocationResponse from(LocationEntity e) {
        return new LocationResponse(
                e.getId(), e.getName(), e.getFloor(), e.getCapacity(),
                e.getDescription(), e.isActive(), e.getCreatedAt(), e.getUpdatedAt()
        );
    }
}
