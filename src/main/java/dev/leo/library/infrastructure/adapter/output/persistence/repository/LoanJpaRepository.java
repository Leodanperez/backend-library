package dev.leo.library.infrastructure.adapter.output.persistence.repository;

import dev.leo.library.infrastructure.adapter.output.persistence.entity.LoanEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import java.time.LocalDateTime;
import java.util.List;

public interface LoanJpaRepository extends JpaRepository<LoanEntity, Long>, JpaSpecificationExecutor<LoanEntity> {
    List<LoanEntity> findByUserId(Long userId);
    List<LoanEntity> findByLoanStatusId(Long loanStatusId);
    boolean existsByBookCopyIdAndLoanStatus_Name(Long bookCopyId, String statusName);
    List<LoanEntity> findByLoanStatus_NameAndDueDateBefore(String statusName, LocalDateTime date);
    List<LoanEntity> findByLoanStatus_NameAndLoanDateBefore(String statusName, LocalDateTime date);

    @org.springframework.data.jpa.repository.Query("""
        SELECT COUNT(l) > 0 FROM LoanEntity l
        WHERE l.user.id = :userId
          AND l.bookCopy.id = :bookCopyId
          AND l.loanStatus.name NOT IN ('RETURNED', 'CANCELLED')
        """)
    boolean existsActiveRequestByUserAndCopy(Long userId, Long bookCopyId);
}
