package dev.leo.library.domain.port.input;

import dev.leo.library.application.dto.request.BookRequest;
import dev.leo.library.infrastructure.adapter.output.persistence.entity.BookEntity;
import dev.leo.library.shared.dto.PaginatedResponse;

public interface BookUseCase {
    PaginatedResponse<BookEntity> findAll(String q, Long authorId, Long categoryId, String language, Boolean active, int page, int perPage);
    BookEntity findById(Long id);
    BookEntity save(BookRequest dto);
    BookEntity update(Long id, BookRequest dto);
    BookEntity activate(Long id);
    BookEntity deactivate(Long id);
    void delete(Long id);
}
