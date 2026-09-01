package dev.leo.library.infrastructure.adapter.output.persistence.repository;

import dev.leo.library.domain.model.CopyStatus;
import dev.leo.library.infrastructure.adapter.output.persistence.entity.BookCopyEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import java.util.List;
import java.util.Optional;

public interface BookCopyJpaRepository extends JpaRepository<BookCopyEntity, Long>, JpaSpecificationExecutor<BookCopyEntity> {
    List<BookCopyEntity> findByBookId(Long bookId);
    List<BookCopyEntity> findByStatus(CopyStatus status);
    Optional<BookCopyEntity> findByCode(String code);
    boolean existsByCode(String code);
}
