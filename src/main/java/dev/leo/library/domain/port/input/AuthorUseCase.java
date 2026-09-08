package dev.leo.library.domain.port.input;

import dev.leo.library.application.dto.request.AuthorRequest;
import dev.leo.library.application.dto.response.AuthorSelectResponse;
import dev.leo.library.infrastructure.adapter.output.persistence.entity.AuthorEntity;
import dev.leo.library.shared.dto.PaginatedResponse;

import java.util.List;

public interface AuthorUseCase {
    PaginatedResponse<AuthorEntity> findAll(String q, String nationality, Boolean active, int page, int perPage);
    List<AuthorSelectResponse> findAllForSelect();
    AuthorEntity findById(Long id);
    AuthorEntity save(AuthorRequest dto);
    AuthorEntity update(Long id, AuthorRequest dto);
    AuthorEntity activate(Long id);
    AuthorEntity deactivate(Long id);
    void delete(Long id);
}
