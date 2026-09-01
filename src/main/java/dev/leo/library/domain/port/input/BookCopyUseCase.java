package dev.leo.library.domain.port.input;

import dev.leo.library.application.dto.request.BookCopyRequest;
import dev.leo.library.domain.model.CopyCondition;
import dev.leo.library.domain.model.CopyStatus;
import dev.leo.library.infrastructure.adapter.output.persistence.entity.BookCopyEntity;
import dev.leo.library.shared.dto.PaginatedResponse;

public interface BookCopyUseCase {
    PaginatedResponse<BookCopyEntity> findAll(String q, Long bookId, CopyStatus status, CopyCondition condition, int page, int perPage);
    BookCopyEntity findById(Long id);
    BookCopyEntity save(BookCopyRequest dto);
    BookCopyEntity update(Long id, BookCopyRequest dto);
    BookCopyEntity markAsLost(Long id);
    BookCopyEntity markAsDamaged(Long id, CopyCondition condition);
    BookCopyEntity restore(Long id);
    void delete(Long id);
}
