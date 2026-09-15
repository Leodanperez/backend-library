package dev.leo.library.application.service;

import dev.leo.library.application.dto.request.LoanRequest;
import dev.leo.library.application.dto.request.LoanRequestDto;
import dev.leo.library.application.dto.response.ActiveLoanResponse;
import dev.leo.library.application.dto.response.LoanRequestSummaryResponse;
import dev.leo.library.application.dto.response.LoanRequestItemResponse;
import dev.leo.library.application.dto.response.LoanSummaryResponse;
import dev.leo.library.application.dto.response.MyLoanResponse;
import dev.leo.library.application.dto.response.MyLoanSummaryResponse;
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
import java.util.List;
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
    public PaginatedResponse<ActiveLoanResponse> findActiveLoans(String q, int page, int perPage) {
        Page<LoanEntity> result = loanRepository.findActiveLoans(
                q, PageRequest.of(page - 1, perPage)
        );
        return PaginatedResponse.of(result.getContent().stream().map(ActiveLoanResponse::from).toList(),
                page, perPage, result.getTotalElements());
    }

    @Override
    public LoanRequestSummaryResponse findRequests(String status, String q, int page, int perPage) {
        Page<LoanEntity> result = loanRepository.findRequests(
                status, q,
                PageRequest.of(page - 1, perPage)
        );
        List<LoanRequestItemResponse> items = result.getContent().stream().map(loan -> {
            Long userId = loan.getUser().getId();
            long activeLoans = loanRepository.countActiveByUserId(userId);
            long overdueLoans = loanRepository.countOverdueByUserId(userId);
            long available = loanRepository.countAvailableCopiesByBookId(loan.getBookCopy().getBook().getId());
            return LoanRequestItemResponse.from(loan, (int) available, activeLoans, overdueLoans);
        }).toList();
        return new LoanRequestSummaryResponse(
                loanRepository.countByStatus("REQUESTED"),
                loanRepository.countByStatusToday("PENDING"),
                loanRepository.countByStatusToday("CANCELLED"),
                PaginatedResponse.of(items, page, perPage, result.getTotalElements())
        );
    }

    @Override
    public List<Long> findRequestedCopyIds(List<Long> copyIds) {
        return loanRepository.findRequestedCopyIds(copyIds);
    }

    @Override
    public MyLoanSummaryResponse getSummary(Long userId) {
        int year = LocalDateTime.now().getYear();
        List<MyLoanResponse> requests = loanRepository
                .findByUserIdAndLoanStatus_NameIn(userId, List.of("REQUESTED", "CANCELLED"))
                .stream().map(MyLoanResponse::from).toList();
        List<MyLoanResponse> active = loanRepository
                .findByUserIdAndLoanStatus_NameIn(userId, List.of("PENDING", "OVERDUE"))
                .stream().map(MyLoanResponse::from).toList();
        return new MyLoanSummaryResponse(
                loanRepository.countByUserIdAndStatus(userId, "REQUESTED"),
                loanRepository.countByUserIdAndStatus(userId, "PENDING"),
                loanRepository.countRenewalsAvailable(userId),
                loanRepository.countReturnedThisYear(userId, year),
                requests,
                active
        );
    }

    @Override
    public PaginatedResponse<MyLoanResponse> getMyLoans(Long userId, int page, int perPage) {
        Page<LoanEntity> result = loanRepository.findAll(
                LoanSpec.filter(userId, null, null),
                PageRequest.of(page - 1, perPage, Sort.by("loanDate").descending())
        );
        return PaginatedResponse.of(result.getContent().stream().map(MyLoanResponse::from).toList(),
                page, perPage, result.getTotalElements());
    }

    @Override
    public PaginatedResponse<MyLoanResponse> getHistory(Long userId, int page, int perPage) {
        Page<LoanEntity> result = loanRepository.findByUserIdAndLoanStatus_Name(
                userId, "RETURNED",
                PageRequest.of(page - 1, perPage, Sort.by("returnDate").descending())
        );
        return PaginatedResponse.of(result.getContent().stream().map(MyLoanResponse::from).toList(),
                page, perPage, result.getTotalElements());
    }

    @Override
    public PaginatedResponse<LoanSummaryResponse> findAll(Long userId, Long bookCopyId, String status, String q, int page, int perPage) {
        Page<LoanEntity> result = loanRepository.findAllLoans(
                userId, bookCopyId, status, q,
                PageRequest.of(page - 1, perPage, Sort.by("loanDate").descending())
        );
        var mapped = result.getContent().stream().map(LoanSummaryResponse::from).toList();
        return PaginatedResponse.of(mapped, page, perPage, result.getTotalElements());
    }

    @Override
    public LoanEntity findEntityById(Long id) {
        return loanRepository.findById(id).orElseThrow(() -> new LoanNotFoundException(id));
    }

    @Override
    public LoanSummaryResponse findById(Long id) {
        return LoanSummaryResponse.from(findEntityById(id));
    }

    @Override
    @Transactional
    public void requestLoanFromCatalog(Long bookId, LoanRequestDto dto, Long studentId) {
        BookCopyEntity copy = bookCopyRepository.findById(dto.bookCopyId())
                .orElseThrow(() -> new BookCopyNotFoundException(dto.bookCopyId()));
        if (!copy.getBook().getId().equals(bookId))
            throw new IllegalArgumentException("El ejemplar no pertenece al libro indicado");
        if (copy.getStatus() != CopyStatus.AVAILABLE)
            throw new IllegalStateException("El ejemplar no está disponible para préstamo");
        if (loanRepository.existsActiveRequestByUserAndBook(studentId, bookId))
            throw new IllegalStateException("Ya tienes una solicitud activa para este libro");
        loanRepository.save(LoanEntity.builder()
                .bookCopy(copy).user(userService.findEntityById(studentId))
                .loanStatus(getStatusByName("REQUESTED"))
                .loanDate(LocalDateTime.now()).dueDate(dto.dueDate())
                .renewalCount(0).observations(dto.observations()).build());
    }

    @Override
    @Transactional
    public void requestLoan(LoanRequestDto dto, Long studentId) {
        BookCopyEntity copy = bookCopyRepository.findById(dto.bookCopyId())
                .orElseThrow(() -> new BookCopyNotFoundException(dto.bookCopyId()));
        if (copy.getStatus() != CopyStatus.AVAILABLE)
            throw new IllegalStateException("El ejemplar no está disponible para préstamo");
        if (loanRepository.existsActiveRequestByUserAndBook(studentId, copy.getBook().getId()))
            throw new IllegalStateException("Ya tienes una solicitud activa para este libro");
        loanRepository.save(LoanEntity.builder()
                .bookCopy(copy).user(userService.findEntityById(studentId))
                .loanStatus(getStatusByName("REQUESTED"))
                .loanDate(LocalDateTime.now()).dueDate(dto.dueDate())
                .renewalCount(0).observations(dto.observations()).build());
    }

    @Override
    @Transactional
    public void approveLoan(Long id, Long librarianId) {
        LoanEntity loan = findEntityById(id);
        if (!"REQUESTED".equals(loan.getLoanStatus().getName()))
            throw new IllegalStateException("Solo se pueden aprobar solicitudes en estado SOLICITADO");
        BookCopyEntity copy = loan.getBookCopy();
        if (copy.getStatus() != CopyStatus.AVAILABLE)
            throw new IllegalStateException("El ejemplar ya no está disponible");
        loan.setLoanStatus(getStatusByName("PENDING"));
        copy.setStatus(CopyStatus.LOANED);
        bookCopyRepository.save(copy);
        loanRepository.save(loan);
    }

    @Override
    @Transactional
    public void save(LoanRequest dto) {
        BookCopyEntity copy = bookCopyRepository.findById(dto.bookCopyId())
                .orElseThrow(() -> new BookCopyNotFoundException(dto.bookCopyId()));
        if (copy.getStatus() != CopyStatus.AVAILABLE)
            throw new IllegalStateException("El ejemplar no está disponible para préstamo");
        LoanEntity loan = LoanEntity.builder()
                .bookCopy(copy).user(userService.findEntityById(dto.userId()))
                .loanStatus(getStatusByName("PENDING"))
                .loanDate(dto.loanDate() != null ? dto.loanDate() : LocalDateTime.now())
                .dueDate(dto.dueDate()).renewalCount(0).observations(dto.observations())
                .build();
        copy.setStatus(CopyStatus.LOANED);
        bookCopyRepository.save(copy);
        loanRepository.save(loan);
    }

    @Override
    @Transactional
    public void returnLoan(Long id, String observations) {
        LoanEntity loan = findEntityById(id);
        if ("RETURNED".equals(loan.getLoanStatus().getName()))
            throw new IllegalStateException("El préstamo ya fue devuelto");
        if ("CANCELLED".equals(loan.getLoanStatus().getName()))
            throw new IllegalStateException("No se puede devolver un préstamo cancelado");
        loan.setLoanStatus(getStatusByName("RETURNED"));
        loan.setReturnDate(LocalDateTime.now());
        if (observations != null) loan.setObservations(observations);
        BookCopyEntity copy = loan.getBookCopy();
        copy.setStatus(CopyStatus.AVAILABLE);
        bookCopyRepository.save(copy);
        loanRepository.save(loan);
    }

    @Override
    @Transactional
    public void renewLoan(Long id, int days) {
        LoanEntity loan = findEntityById(id);
        String status = loan.getLoanStatus().getName();
        if (TERMINAL_STATUSES.contains(status))
            throw new IllegalStateException("No se puede renovar un préstamo con estado: " + status.toLowerCase());
        if (loan.getRenewalCount() >= MAX_RENEWALS)
            throw new IllegalStateException("Se alcanzó el máximo de renovaciones permitidas (" + MAX_RENEWALS + ")");
        loan.setDueDate(loan.getDueDate().plusDays(days));
        loan.setRenewalCount(loan.getRenewalCount() + 1);
        if ("OVERDUE".equals(status))
            loan.setLoanStatus(getStatusByName("PENDING"));
        loanRepository.save(loan);
    }

    @Override
    @Transactional
    public void cancelLoan(Long id) {
        LoanEntity loan = findEntityById(id);
        String status = loan.getLoanStatus().getName();
        if (TERMINAL_STATUSES.contains(status))
            throw new IllegalStateException("El préstamo ya se encuentra en estado: " + status.toLowerCase());
        loan.setLoanStatus(getStatusByName("CANCELLED"));
        BookCopyEntity copy = loan.getBookCopy();
        if (copy.getStatus() == CopyStatus.LOANED) {
            copy.setStatus(CopyStatus.AVAILABLE);
            bookCopyRepository.save(copy);
        }
        loanRepository.save(loan);
    }

    @Override
    @Transactional
    public void cancelLoanByStudent(Long id, Long studentId) {
        LoanEntity loan = findEntityById(id);
        if (!loan.getUser().getId().equals(studentId))
            throw new IllegalStateException("No tienes permiso para cancelar este préstamo");
        if (!"REQUESTED".equals(loan.getLoanStatus().getName()))
            throw new IllegalStateException("Solo puedes cancelar solicitudes en estado SOLICITADO");
        loan.setLoanStatus(getStatusByName("CANCELLED"));
        loanRepository.save(loan);
    }

    @Override
    @Transactional
    public void update(Long id, LoanRequest dto) {
        LoanEntity loan = findEntityById(id);
        loan.setDueDate(dto.dueDate());
        if (dto.returnDate() != null) loan.setReturnDate(dto.returnDate());
        if (dto.observations() != null) loan.setObservations(dto.observations());
        if (dto.loanStatusId() != null) {
            loan.setLoanStatus(loanStatusRepository.findById(dto.loanStatusId())
                    .orElseThrow(() -> new LoanStatusNotFoundException("id: " + dto.loanStatusId())));
        }
        loanRepository.save(loan);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        loanRepository.delete(findEntityById(id));
    }
}
