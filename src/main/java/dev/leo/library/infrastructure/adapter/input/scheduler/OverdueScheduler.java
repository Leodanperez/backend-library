package dev.leo.library.infrastructure.adapter.input.scheduler;

import dev.leo.library.domain.exception.LoanStatusNotFoundException;
import dev.leo.library.infrastructure.adapter.output.persistence.entity.LoanEntity;
import dev.leo.library.infrastructure.adapter.output.persistence.entity.LoanStatusEntity;
import dev.leo.library.infrastructure.adapter.output.persistence.repository.LoanJpaRepository;
import dev.leo.library.infrastructure.adapter.output.persistence.repository.LoanStatusJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class OverdueScheduler {

    private final LoanJpaRepository loanRepository;
    private final LoanStatusJpaRepository loanStatusRepository;

    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void markOverdueLoans() {
        LoanStatusEntity overdueStatus = loanStatusRepository.findByName("OVERDUE")
                .orElseThrow(() -> new LoanStatusNotFoundException("OVERDUE"));

        List<LoanEntity> overdueLoans = loanRepository
                .findByLoanStatus_NameAndDueDateBefore("PENDING", LocalDateTime.now());

        if (overdueLoans.isEmpty()) return;

        overdueLoans.forEach(loan -> loan.setLoanStatus(overdueStatus));
        loanRepository.saveAll(overdueLoans);
        log.info("Marked {} loan(s) as OVERDUE", overdueLoans.size());
    }
}
