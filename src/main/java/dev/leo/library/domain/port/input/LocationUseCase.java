package dev.leo.library.domain.port.input;

import dev.leo.library.application.dto.request.LocationRequest;
import dev.leo.library.application.dto.response.LocationResponse;
import dev.leo.library.infrastructure.adapter.output.persistence.entity.LocationEntity;
import dev.leo.library.shared.dto.PaginatedResponse;

public interface LocationUseCase {
    PaginatedResponse<LocationResponse> findAll(String q, Boolean active, int page, int perPage);
    LocationResponse findById(Long id);
    LocationResponse save(LocationRequest dto);
    void update(Long id, LocationRequest dto);
    void activate(Long id);
    void deactivate(Long id);
    void delete(Long id);
    LocationEntity findEntityById(Long id);
}
