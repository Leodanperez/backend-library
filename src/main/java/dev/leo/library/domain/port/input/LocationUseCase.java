package dev.leo.library.domain.port.input;

import dev.leo.library.application.dto.request.LocationRequest;
import dev.leo.library.infrastructure.adapter.output.persistence.entity.LocationEntity;
import dev.leo.library.shared.dto.PaginatedResponse;

public interface LocationUseCase {
    PaginatedResponse<LocationEntity> findAll(String q, Boolean active, int page, int perPage);
    LocationEntity findById(Long id);
    LocationEntity save(LocationRequest dto);
    LocationEntity update(Long id, LocationRequest dto);
    LocationEntity activate(Long id);
    LocationEntity deactivate(Long id);
    void delete(Long id);
}
