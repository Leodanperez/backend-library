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

    private static final int REQUESTED_EXPIRY_HOURS = 48;

    private final LoanJpaRepository loanRepository;
    private final LoanStatusJpaRepository loanStatusRepository;

    // Cada hora: marca como VENCIDO los préstamos PENDING cuya fecha límite ya pasó
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
        log.info("Se marcaron {} préstamo(s) como VENCIDO", overdueLoans.size());
    }

    // Cada hora: cancela automáticamente solicitudes REQUESTED con más de 48hs sin aprobación
    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void cancelExpiredRequests() {
        LoanStatusEntity cancelledStatus = loanStatusRepository.findByName("CANCELLED")
                .orElseThrow(() -> new LoanStatusNotFoundException("CANCELLED"));

        LocalDateTime expiryThreshold = LocalDateTime.now().minusHours(REQUESTED_EXPIRY_HOURS);
        List<LoanEntity> expiredRequests = loanRepository
                .findByLoanStatus_NameAndLoanDateBefore("REQUESTED", expiryThreshold);

        if (expiredRequests.isEmpty()) return;

        expiredRequests.forEach(loan -> loan.setLoanStatus(cancelledStatus));
        loanRepository.saveAll(expiredRequests);
        log.info("Se cancelaron {} solicitud(es) SOLICITADA(S) por vencimiento de {} horas",
                expiredRequests.size(), REQUESTED_EXPIRY_HOURS);
    }
}
