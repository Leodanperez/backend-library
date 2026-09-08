package dev.leo.library.domain.port.input;

import dev.leo.library.application.dto.request.PublisherRequest;
import dev.leo.library.infrastructure.adapter.output.persistence.entity.PublisherEntity;
import dev.leo.library.shared.dto.PaginatedResponse;

public interface PublisherUseCase {
    PaginatedResponse<PublisherEntity> findAll(String q, Boolean active, int page, int perPage);
    PublisherEntity findById(Long id);
    PublisherEntity save(PublisherRequest dto);
    PublisherEntity update(Long id, PublisherRequest dto);
    PublisherEntity activate(Long id);
    PublisherEntity deactivate(Long id);
    void delete(Long id);
}
