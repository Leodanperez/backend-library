package dev.leo.library.application.service;

import dev.leo.library.application.dto.request.LoanRequest;
import dev.leo.library.domain.exception.BookCopyNotFoundException;
import dev.leo.library.domain.exception.LoanNotFoundException;
import dev.leo.library.domain.exception.LoanStatusNotFoundException;
import dev.leo.library.domain.model.CopyStatus;
import dev.leo.library.domain.port.input.LoanUseCase;
import dev.leo.library.infrastructure.adapter.output.persistence.adapter.LoanSpec;
import dev.leo.library.infrastructure.adapter.output.persistence.entity.BookCopyEntity;
import dev.leo.library.infrastructure.adapter.output.persistence.entity.LoanEntity;
import dev.leo.library.infrastructure.adapter.output.persistence.entity.LoanStatusEntity;
import dev.leo.library.infrastructure.adapter.output.persistence.repository.BookCopyJpaRepository;
import dev.leo.library.infrastructure.adapter.output.persistence.repository.LoanJpaRepository;
import dev.leo.library.infrastructure.adapter.output.persistence.repository.LoanStatusJpaRepository;
import dev.leo.library.shared.dto.PaginatedResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class LoanService implements LoanUseCase {

    private static final int MAX_RENEWALS = 3;
    private static final Set<String> TERMINAL_STATUSES = Set.of("RETURNED", "CANCELLED");

    private final LoanJpaRepository loanRepository;
    private final BookCopyJpaRepository bookCopyRepository;
    private final LoanStatusJpaRepository loanStatusRepository;
    private final UserService userService;

    private LoanStatusEntity getStatusByName(String name) {
        return loanStatusRepository.findByName(name)
                .orElseThrow(() -> new LoanStatusNotFoundException(name));
    }

    @Override
    public PaginatedResponse<LoanEntity> findAll(Long userId, Long bookCopyId, Long loanStatusId, int page, int perPage) {
        Page<LoanEntity> result = loanRepository.findAll(
                LoanSpec.filter(userId, bookCopyId, loanStatusId),
                PageRequest.of(page - 1, perPage, Sort.by("loanDate").descending())
        );
        return PaginatedResponse.of(result.getContent(), page, perPage, result.getTotalElements());
    }

    @Override
    public LoanEntity findById(Long id) {
        return loanRepository.findById(id).orElseThrow(() -> new LoanNotFoundException(id));
    }

    @Override
    @Transactional
    public LoanEntity save(LoanRequest dto) {
        BookCopyEntity copy = bookCopyRepository.findById(dto.bookCopyId())
                .orElseThrow(() -> new BookCopyNotFoundException(dto.bookCopyId()));
        if (copy.getStatus() != CopyStatus.AVAILABLE)
            throw new IllegalStateException("El ejemplar no está disponible para préstamo");

        LoanEntity loan = LoanEntity.builder()
                .bookCopy(copy).user(userService.findById(dto.userId()))
                .loanStatus(getStatusByName("PENDING"))
                .loanDate(dto.loanDate() != null ? dto.loanDate() : LocalDateTime.now())
                .dueDate(dto.dueDate()).renewalCount(0).observations(dto.observations())
                .build();

        copy.setStatus(CopyStatus.LOANED);
        bookCopyRepository.save(copy);
        return loanRepository.save(loan);
    }

    @Override
    @Transactional
    public LoanEntity returnLoan(Long id) {
        LoanEntity loan = findById(id);
        if ("RETURNED".equals(loan.getLoanStatus().getName()))
            throw new IllegalStateException("El préstamo ya fue devuelto");
        if ("CANCELLED".equals(loan.getLoanStatus().getName()))
            throw new IllegalStateException("No se puede devolver un préstamo cancelado");
        loan.setLoanStatus(getStatusByName("RETURNED"));
        loan.setReturnDate(LocalDateTime.now());
        BookCopyEntity copy = loan.getBookCopy();
        copy.setStatus(CopyStatus.AVAILABLE);
        bookCopyRepository.save(copy);
        return loanRepository.save(loan);
    }

    @Override
    @Transactional
    public LoanEntity renewLoan(Long id, int days) {
        LoanEntity loan = findById(id);
        String status = loan.getLoanStatus().getName();
        if (TERMINAL_STATUSES.contains(status))
            throw new IllegalStateException("No se puede renovar un préstamo con estado: " + status.toLowerCase());
        if (loan.getRenewalCount() >= MAX_RENEWALS)
            throw new IllegalStateException("Se alcanzó el máximo de renovaciones permitidas (" + MAX_RENEWALS + ")");
        loan.setDueDate(loan.getDueDate().plusDays(days));
        loan.setRenewalCount(loan.getRenewalCount() + 1);
        if ("OVERDUE".equals(status))
            loan.setLoanStatus(getStatusByName("PENDING"));
        return loanRepository.save(loan);
    }

    @Override
    @Transactional
    public LoanEntity cancelLoan(Long id) {
        LoanEntity loan = findById(id);
        String status = loan.getLoanStatus().getName();
        if (TERMINAL_STATUSES.contains(status))
            throw new IllegalStateException("El préstamo ya se encuentra en estado: " + status.toLowerCase());
        loan.setLoanStatus(getStatusByName("CANCELLED"));
        BookCopyEntity copy = loan.getBookCopy();
        if (copy.getStatus() == CopyStatus.LOANED) {
            copy.setStatus(CopyStatus.AVAILABLE);
            bookCopyRepository.save(copy);
        }
        return loanRepository.save(loan);
    }

    @Override
    @Transactional
    public LoanEntity update(Long id, LoanRequest dto) {
        LoanEntity loan = findById(id);
        loan.setDueDate(dto.dueDate());
        if (dto.returnDate() != null) loan.setReturnDate(dto.returnDate());
        if (dto.observations() != null) loan.setObservations(dto.observations());
        if (dto.loanStatusId() != null) {
            loan.setLoanStatus(loanStatusRepository.findById(dto.loanStatusId())
                    .orElseThrow(() -> new LoanStatusNotFoundException("id: " + dto.loanStatusId())));
        }
        return loanRepository.save(loan);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        loanRepository.delete(findById(id));
    }
}
