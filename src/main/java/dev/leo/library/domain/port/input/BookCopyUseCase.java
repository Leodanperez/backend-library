package dev.leo.library.domain.port.input;

import dev.leo.library.application.dto.request.BookCopyRequest;
import dev.leo.library.application.dto.response.BookCopyResponse;
import dev.leo.library.domain.model.CopyCondition;
import dev.leo.library.domain.model.CopyStatus;
import dev.leo.library.infrastructure.adapter.output.persistence.entity.BookCopyEntity;
import dev.leo.library.shared.dto.PaginatedResponse;
import dev.leo.library.application.dto.response.SelectOptionsResponse.SelectItem;
import java.util.List;

public interface BookCopyUseCase {
    List<SelectItem> findAllForSelect();
    PaginatedResponse<BookCopyResponse> findAll(String q, Long bookId, CopyStatus status, CopyCondition condition, int page, int perPage);
    BookCopyResponse findById(Long id);
    BookCopyResponse save(BookCopyRequest dto);
    void update(Long id, BookCopyRequest dto);
    void markAsLost(Long id);
    void markAsDamaged(Long id, CopyCondition condition);
    void restore(Long id);
    void delete(Long id);
    BookCopyEntity findEntityById(Long id);
}
