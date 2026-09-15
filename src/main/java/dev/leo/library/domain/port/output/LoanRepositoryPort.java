package dev.leo.library.domain.port.output;

import dev.leo.library.infrastructure.adapter.output.persistence.entity.LoanEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface LoanRepositoryPort {
    Optional<LoanEntity> findById(Long id);
    Page<LoanEntity> findActiveLoans(String q, Pageable pageable);
    Page<LoanEntity> findRequests(String status, String q, Pageable pageable);
    Page<LoanEntity> findAll(Long userId, Long bookCopyId, String status, String q, Pageable pageable);
    Page<LoanEntity> findByUserIdAndStatus(Long userId, String status, Pageable pageable);
    List<LoanEntity> findByUserIdAndStatusIn(Long userId, List<String> statuses);
    List<LoanEntity> findByStatusAndDueDateBefore(String status, LocalDateTime date);
    List<LoanEntity> findByStatusAndLoanDateBefore(String status, LocalDateTime date);
    List<Long> findRequestedCopyIds(List<Long> copyIds);
    boolean existsActiveRequestByUserAndBook(Long userId, Long bookId);
    long countByUserIdAndStatus(Long userId, String status);
    long countByStatus(String status);
    long countByStatusToday(String status);
    long countActiveByUserId(Long userId);
    long countOverdueByUserId(Long userId);
    long countRenewalsAvailable(Long userId);
    long countReturnedThisYear(Long userId, int year);
    long countAvailableCopiesByBookId(Long bookId);
    LoanEntity save(LoanEntity loan);
    List<LoanEntity> saveAll(List<LoanEntity> loans);
    void delete(LoanEntity loan);
}
