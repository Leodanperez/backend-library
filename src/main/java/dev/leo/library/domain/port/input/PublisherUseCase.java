package dev.leo.library.domain.port.input;

import dev.leo.library.application.dto.request.PublisherRequest;
import dev.leo.library.application.dto.response.PublisherResponse;
import dev.leo.library.infrastructure.adapter.output.persistence.entity.PublisherEntity;
import dev.leo.library.shared.dto.PaginatedResponse;

public interface PublisherUseCase {
    PaginatedResponse<PublisherResponse> findAll(String q, Boolean active, int page, int perPage);
    PublisherResponse findById(Long id);
    PublisherResponse save(PublisherRequest dto);
    void update(Long id, PublisherRequest dto);
    void activate(Long id);
    void deactivate(Long id);
    void delete(Long id);
    PublisherEntity findEntityById(Long id);
}
