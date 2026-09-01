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
}
