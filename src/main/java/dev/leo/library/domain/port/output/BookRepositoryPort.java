package dev.leo.library.domain.port.output;

import dev.leo.library.infrastructure.adapter.output.persistence.entity.BookEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface BookRepositoryPort {
    Optional<BookEntity> findById(Long id);
    List<BookEntity> findAllSortedByTitle();
    Page<BookEntity> search(String q, Long authorId, Long categoryId, String language, Boolean active, Pageable pageable);
    boolean existsByIsbn(String isbn);
    BookEntity save(BookEntity book);
    void delete(BookEntity book);
    Set<Long> findBookIdsWithAvailableCopies(List<Long> bookIds);
    long countActiveByAuthorId(Long authorId);
    long countActiveByCategoryId(Long categoryId);
}
