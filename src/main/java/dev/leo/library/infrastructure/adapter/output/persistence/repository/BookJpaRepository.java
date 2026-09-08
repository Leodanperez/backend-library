package dev.leo.library.infrastructure.adapter.output.persistence.repository;

import dev.leo.library.infrastructure.adapter.output.persistence.entity.BookEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface BookJpaRepository extends JpaRepository<BookEntity, Long>, JpaSpecificationExecutor<BookEntity> {
    Optional<BookEntity> findByIsbn(String isbn);
    boolean existsByIsbn(String isbn);

    @Query("""
            SELECT DISTINCT bc.book.id FROM BookCopyEntity bc
            WHERE bc.book.id IN :ids
              AND bc.status = 'AVAILABLE'
              AND NOT EXISTS (
                SELECT l FROM LoanEntity l
                WHERE l.bookCopy.id = bc.id
                  AND l.loanStatus.name = 'REQUESTED'
              )
            """)
    Set<Long> findBookIdsWithAvailableCopies(@Param("ids") List<Long> ids);
}
