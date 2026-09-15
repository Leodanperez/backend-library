package dev.leo.library.infrastructure.adapter.output.persistence.adapter;

import dev.leo.library.domain.port.output.LoanRepositoryPort;
import dev.leo.library.infrastructure.adapter.output.persistence.entity.LoanEntity;
import dev.leo.library.infrastructure.adapter.output.persistence.repository.LoanJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class LoanPersistenceAdapter implements LoanRepositoryPort {

    private final LoanJpaRepository loanRepository;

    @Override
    public Optional<LoanEntity> findById(Long id) {
        return loanRepository.findById(id);
    }

    @Override
    public Page<LoanEntity> findActiveLoans(String q, Pageable pageable) {
        return loanRepository.findActiveLoans(q, pageable);
    }

    @Override
    public Page<LoanEntity> findRequests(String status, String q, Pageable pageable) {
        return loanRepository.findRequests(status, q, pageable);
    }

    @Override
    public Page<LoanEntity> findAll(Long userId, Long bookCopyId, String status, String q, Pageable pageable) {
        return loanRepository.findAllLoans(userId, bookCopyId, status, q, pageable);
    }

    @Override
    public Page<LoanEntity> findByUserIdAndStatus(Long userId, String status, Pageable pageable) {
        return loanRepository.findByUserIdAndLoanStatus_Name(userId, status, pageable);
    }

    @Override
    public List<LoanEntity> findByUserIdAndStatusIn(Long userId, List<String> statuses) {
        return loanRepository.findByUserIdAndLoanStatus_NameIn(userId, statuses);
    }

    @Override
    public List<LoanEntity> findByStatusAndDueDateBefore(String status, LocalDateTime date) {
        return loanRepository.findByLoanStatus_NameAndDueDateBefore(status, date);
    }

    @Override
    public List<LoanEntity> findByStatusAndLoanDateBefore(String status, LocalDateTime date) {
        return loanRepository.findByLoanStatus_NameAndLoanDateBefore(status, date);
    }

    @Override
    public List<Long> findRequestedCopyIds(List<Long> copyIds) {
        return loanRepository.findRequestedCopyIds(copyIds);
    }

    @Override
    public boolean existsActiveRequestByUserAndBook(Long userId, Long bookId) {
        return loanRepository.existsActiveRequestByUserAndBook(userId, bookId);
    }

    @Override
    public long countByUserIdAndStatus(Long userId, String status) {
        return loanRepository.countByUserIdAndStatus(userId, status);
    }

    @Override
    public long countByStatus(String status) {
        return loanRepository.countByStatus(status);
    }

    @Override
    public long countByStatusToday(String status) {
        return loanRepository.countByStatusToday(status);
    }

    @Override
    public long countActiveByUserId(Long userId) {
        return loanRepository.countActiveByUserId(userId);
    }

    @Override
    public long countOverdueByUserId(Long userId) {
        return loanRepository.countOverdueByUserId(userId);
    }

    @Override
    public long countRenewalsAvailable(Long userId) {
        return loanRepository.countRenewalsAvailable(userId);
    }

    @Override
    public long countReturnedThisYear(Long userId, int year) {
        return loanRepository.countReturnedThisYear(userId, year);
    }

    @Override
    public long countAvailableCopiesByBookId(Long bookId) {
        return loanRepository.countAvailableCopiesByBookId(bookId);
    }

    @Override
    public LoanEntity save(LoanEntity loan) {
        return loanRepository.save(loan);
    }

    @Override
    public List<LoanEntity> saveAll(List<LoanEntity> loans) {
        return loanRepository.saveAll(loans);
    }

    @Override
    public void delete(LoanEntity loan) {
        loanRepository.delete(loan);
    }
}
