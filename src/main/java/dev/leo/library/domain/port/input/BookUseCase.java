package dev.leo.library.domain.port.input;

import dev.leo.library.application.dto.request.BookRequest;
import dev.leo.library.application.dto.response.BookCatalogResponse;
import dev.leo.library.application.dto.response.BookDetailResponse;
import dev.leo.library.application.dto.response.BookResponse;
import dev.leo.library.application.dto.response.SelectOptionsResponse.SelectItem;
import dev.leo.library.infrastructure.adapter.output.persistence.entity.BookEntity;
import dev.leo.library.shared.dto.PaginatedResponse;
import java.util.List;
import java.util.Set;

public interface BookUseCase {
    List<SelectItem> findAllForSelect();
    PaginatedResponse<BookResponse> findAll(String q, Long authorId, Long categoryId, String language, Boolean active, int page, int perPage);
    PaginatedResponse<BookCatalogResponse> searchCatalog(String q, Long authorId, Long categoryId, String language, int page, int perPage);
    BookDetailResponse detailCatalog(Long id);
    Set<Long> findAvailableBookIds(List<Long> bookIds);
    BookResponse findById(Long id);
    BookResponse save(BookRequest dto);
    void update(Long id, BookRequest dto);
    void activate(Long id);
    void deactivate(Long id);
    void delete(Long id);
    BookEntity findEntityById(Long id);
}
