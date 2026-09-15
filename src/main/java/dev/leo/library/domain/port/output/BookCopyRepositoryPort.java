package dev.leo.library.domain.port.output;

import dev.leo.library.domain.model.CopyCondition;
import dev.leo.library.domain.model.CopyStatus;
import dev.leo.library.infrastructure.adapter.output.persistence.entity.BookCopyEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;

public interface BookCopyRepositoryPort {
    Optional<BookCopyEntity> findById(Long id);
    List<BookCopyEntity> findByBookId(Long bookId);
    List<BookCopyEntity> findAllSortedByCode();
    Page<BookCopyEntity> search(String q, Long bookId, CopyStatus status, CopyCondition condition, Pageable pageable);
    boolean existsByCode(String code);
    BookCopyEntity save(BookCopyEntity copy);
    void delete(BookCopyEntity copy);
}
