package dev.leo.library.infrastructure.adapter.output.persistence.repository;

import dev.leo.library.infrastructure.adapter.output.persistence.entity.LoanEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;

public interface LoanJpaRepository extends JpaRepository<LoanEntity, Long>, JpaSpecificationExecutor<LoanEntity> {
    List<LoanEntity> findByUserId(Long userId);
    List<LoanEntity> findByLoanStatusId(Long loanStatusId);
    boolean existsByBookCopyIdAndLoanStatus_Name(Long bookCopyId, String statusName);
    List<LoanEntity> findByLoanStatus_NameAndDueDateBefore(String statusName, LocalDateTime date);
    List<LoanEntity> findByLoanStatus_NameAndLoanDateBefore(String statusName, LocalDateTime date);

    @Query("""
        SELECT COUNT(l) > 0 FROM LoanEntity l
        WHERE l.user.id = :userId
          AND l.bookCopy.id = :bookCopyId
          AND l.loanStatus.name NOT IN ('RETURNED', 'CANCELLED')
        """)
    boolean existsActiveRequestByUserAndCopy(Long userId, Long bookCopyId);

    @Query("""
        SELECT COUNT(l) > 0 FROM LoanEntity l
        WHERE l.user.id = :userId
          AND l.bookCopy.book.id = :bookId
          AND l.loanStatus.name NOT IN ('RETURNED', 'CANCELLED')
        """)
    boolean existsActiveRequestByUserAndBook(@Param("userId") Long userId, @Param("bookId") Long bookId);

    @Query("SELECT COUNT(l) FROM LoanEntity l WHERE l.user.id = :userId AND l.loanStatus.name = :status")
    long countByUserIdAndStatus(@Param("userId") Long userId, @Param("status") String status);

    @Query("SELECT COUNT(l) FROM LoanEntity l WHERE l.loanStatus.name = :status")
    long countByStatus(@Param("status") String status);

    @Query("SELECT COUNT(bc) FROM BookCopyEntity bc WHERE bc.book.id = :bookId AND bc.status = 'AVAILABLE' AND NOT EXISTS (SELECT l FROM LoanEntity l WHERE l.bookCopy.id = bc.id AND l.loanStatus.name = 'REQUESTED')")
    long countAvailableCopiesByBookId(@Param("bookId") Long bookId);

    @Query("SELECT COUNT(l) FROM LoanEntity l WHERE l.user.id = :userId AND l.loanStatus.name = 'PENDING' AND l.renewalCount < 3")
    long countRenewalsAvailable(@Param("userId") Long userId);

    @Query("SELECT COUNT(l) FROM LoanEntity l WHERE l.user.id = :userId AND l.loanStatus.name = 'RETURNED' AND EXTRACT(YEAR FROM l.returnDate) = :year")
    long countReturnedThisYear(@Param("userId") Long userId, @Param("year") int year);

    List<LoanEntity> findByUserIdAndLoanStatus_NameIn(Long userId, List<String> statuses);

    Page<LoanEntity> findByUserIdAndLoanStatus_Name(Long userId, String status, Pageable pageable);

    Page<LoanEntity> findByLoanStatus_Name(String status, Pageable pageable);

    @Query("""
        SELECT l FROM LoanEntity l
        WHERE l.loanStatus.name IN ('PENDING', 'OVERDUE')
          AND (:q IS NULL OR LOWER(l.bookCopy.code) LIKE LOWER(CONCAT('%', CAST(:q AS string), '%'))
               OR LOWER(l.user.firstName) LIKE LOWER(CONCAT('%', CAST(:q AS string), '%'))
               OR LOWER(l.user.lastName) LIKE LOWER(CONCAT('%', CAST(:q AS string), '%')))
        ORDER BY l.dueDate ASC
        """)
    Page<LoanEntity> findActiveLoans(@Param("q") String q, Pageable pageable);

    @Query("""
        SELECT l.bookCopy.id FROM LoanEntity l
        WHERE l.bookCopy.id IN :copyIds
          AND l.loanStatus.name = 'REQUESTED'
        """)
    List<Long> findRequestedCopyIds(@Param("copyIds") List<Long> copyIds);

    @Query("""
        SELECT COUNT(l) FROM LoanEntity l
        WHERE l.user.id = :userId
          AND l.loanStatus.name IN ('PENDING', 'OVERDUE')
        """)
    long countActiveByUserId(@Param("userId") Long userId);

    @Query("""
        SELECT COUNT(l) FROM LoanEntity l
        WHERE l.user.id = :userId
          AND l.loanStatus.name = 'OVERDUE'
        """)
    long countOverdueByUserId(@Param("userId") Long userId);

    @Query("""
        SELECT COUNT(l) FROM LoanEntity l
        WHERE l.loanStatus.name = :status
          AND CAST(l.updatedAt AS date) = CURRENT_DATE
        """)
    long countByStatusToday(@Param("status") String status);

    @Query("""
        SELECT l FROM LoanEntity l
        WHERE l.loanStatus.name IN ('REQUESTED', 'PENDING', 'CANCELLED')
          AND (:status IS NULL OR l.loanStatus.name = :status)
          AND (:q IS NULL OR LOWER(l.user.firstName) LIKE LOWER(CONCAT('%', CAST(:q AS string), '%'))
               OR LOWER(l.user.lastName) LIKE LOWER(CONCAT('%', CAST(:q AS string), '%'))
               OR LOWER(l.bookCopy.book.title) LIKE LOWER(CONCAT('%', CAST(:q AS string), '%')))
        ORDER BY l.loanDate ASC
        """)
    Page<LoanEntity> findRequests(@Param("status") String status, @Param("q") String q, Pageable pageable);
}
