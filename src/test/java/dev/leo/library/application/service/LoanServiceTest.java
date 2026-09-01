package dev.leo.library.application.service;

import dev.leo.library.application.dto.request.LoanRequest;
import dev.leo.library.domain.exception.BookCopyNotFoundException;
import dev.leo.library.domain.exception.LoanNotFoundException;
import dev.leo.library.domain.model.CopyStatus;
import dev.leo.library.infrastructure.adapter.output.persistence.entity.BookCopyEntity;
import dev.leo.library.infrastructure.adapter.output.persistence.entity.LoanEntity;
import dev.leo.library.infrastructure.adapter.output.persistence.entity.LoanStatusEntity;
import dev.leo.library.infrastructure.adapter.output.persistence.entity.UserEntity;
import dev.leo.library.infrastructure.adapter.output.persistence.repository.BookCopyJpaRepository;
import dev.leo.library.infrastructure.adapter.output.persistence.repository.LoanJpaRepository;
import dev.leo.library.infrastructure.adapter.output.persistence.repository.LoanStatusJpaRepository;
import dev.leo.library.shared.dto.PaginatedResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoanServiceTest {

    @Mock
    private LoanJpaRepository loanRepository;

    @Mock
    private BookCopyJpaRepository bookCopyRepository;

    @Mock
    private LoanStatusJpaRepository loanStatusRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private LoanService service;

    private BookCopyEntity copy;
    private UserEntity user;
    private LoanStatusEntity pendingStatus;
    private LoanStatusEntity returnedStatus;
    private LoanStatusEntity cancelledStatus;
    private LoanStatusEntity overdueStatus;
    private LoanEntity loan;
    private LoanRequest request;

    @BeforeEach
    void setUp() {
        copy = BookCopyEntity.builder().id(1L).code("COPY-001").status(CopyStatus.AVAILABLE).build();
        user = UserEntity.builder().id(1L).email("john@example.com").active(true).build();

        pendingStatus   = LoanStatusEntity.builder().id(1L).name("PENDING").build();
        returnedStatus  = LoanStatusEntity.builder().id(2L).name("RETURNED").build();
        cancelledStatus = LoanStatusEntity.builder().id(3L).name("CANCELLED").build();
        overdueStatus   = LoanStatusEntity.builder().id(4L).name("OVERDUE").build();

        loan = LoanEntity.builder()
                .id(1L).bookCopy(copy).user(user).loanStatus(pendingStatus)
                .loanDate(LocalDateTime.now()).dueDate(LocalDateTime.now().plusDays(14))
                .renewalCount(0).build();

        request = new LoanRequest(1L, 1L, null, LocalDateTime.now().plusDays(14), null, null, null);
    }

    // ── findAll ──────────────────────────────────────────────────────────────

    @Test
    void findAll_returnsPagedResponse() {
        when(loanRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(loan)));

        PaginatedResponse<LoanEntity> result = service.findAll(null, null, null, 1, 10);

        assertThat(result.data()).hasSize(1);
        assertThat(result.total()).isEqualTo(1);
    }

    // ── findById ─────────────────────────────────────────────────────────────

    @Test
    void findById_returnsLoan_whenExists() {
        when(loanRepository.findById(1L)).thenReturn(Optional.of(loan));

        LoanEntity result = service.findById(1L);

        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    void findById_throwsLoanNotFoundException_whenNotFound() {
        when(loanRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(99L))
                .isInstanceOf(LoanNotFoundException.class);
    }

    // ── save ─────────────────────────────────────────────────────────────────

    @Test
    void save_createsLoan_whenCopyIsAvailable() {
        when(bookCopyRepository.findById(1L)).thenReturn(Optional.of(copy));
        when(userService.findById(1L)).thenReturn(user);
        when(loanStatusRepository.findByName("PENDING")).thenReturn(Optional.of(pendingStatus));
        when(loanRepository.save(any(LoanEntity.class))).thenReturn(loan);

        LoanEntity result = service.save(request);

        assertThat(result.getLoanStatus().getName()).isEqualTo("PENDING");
        verify(bookCopyRepository).save(copy);
        assertThat(copy.getStatus()).isEqualTo(CopyStatus.LOANED);
    }

    @Test
    void save_throwsIllegalStateException_whenCopyNotAvailable() {
        copy.setStatus(CopyStatus.LOANED);
        when(bookCopyRepository.findById(1L)).thenReturn(Optional.of(copy));

        assertThatThrownBy(() -> service.save(request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no está disponible para préstamo");
    }

    @Test
    void save_throwsBookCopyNotFoundException_whenCopyNotFound() {
        when(bookCopyRepository.findById(99L)).thenReturn(Optional.empty());
        LoanRequest badRequest = new LoanRequest(99L, 1L, null, LocalDateTime.now().plusDays(7), null, null, null);

        assertThatThrownBy(() -> service.save(badRequest))
                .isInstanceOf(BookCopyNotFoundException.class);
    }

    // ── returnLoan ───────────────────────────────────────────────────────────

    @Test
    void returnLoan_setsReturnedStatus_andFreesTheCopy() {
        when(loanRepository.findById(1L)).thenReturn(Optional.of(loan));
        when(loanStatusRepository.findByName("RETURNED")).thenReturn(Optional.of(returnedStatus));
        when(loanRepository.save(loan)).thenReturn(loan);

        LoanEntity result = service.returnLoan(1L);

        assertThat(result.getLoanStatus().getName()).isEqualTo("RETURNED");
        assertThat(result.getReturnDate()).isNotNull();
        assertThat(copy.getStatus()).isEqualTo(CopyStatus.AVAILABLE);
        verify(bookCopyRepository).save(copy);
    }

    @Test
    void returnLoan_throwsIllegalStateException_whenAlreadyReturned() {
        loan.setLoanStatus(returnedStatus);
        when(loanRepository.findById(1L)).thenReturn(Optional.of(loan));

        assertThatThrownBy(() -> service.returnLoan(1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("ya fue devuelto");
    }

    @Test
    void returnLoan_throwsIllegalStateException_whenCancelled() {
        loan.setLoanStatus(cancelledStatus);
        when(loanRepository.findById(1L)).thenReturn(Optional.of(loan));

        assertThatThrownBy(() -> service.returnLoan(1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No se puede devolver un préstamo cancelado");
    }

    // ── renewLoan ────────────────────────────────────────────────────────────

    @Test
    void renewLoan_extendsDueDate_andIncrementsRenewalCount() {
        when(loanRepository.findById(1L)).thenReturn(Optional.of(loan));
        when(loanRepository.save(loan)).thenReturn(loan);
        LocalDateTime originalDue = loan.getDueDate();

        LoanEntity result = service.renewLoan(1L, 7);

        assertThat(result.getDueDate()).isEqualTo(originalDue.plusDays(7));
        assertThat(result.getRenewalCount()).isEqualTo(1);
    }

    @Test
    void renewLoan_resetsStatusToPending_whenOverdue() {
        loan.setLoanStatus(overdueStatus);
        when(loanRepository.findById(1L)).thenReturn(Optional.of(loan));
        when(loanStatusRepository.findByName("PENDING")).thenReturn(Optional.of(pendingStatus));
        when(loanRepository.save(loan)).thenReturn(loan);

        LoanEntity result = service.renewLoan(1L, 7);

        assertThat(result.getLoanStatus().getName()).isEqualTo("PENDING");
    }

    @Test
    void renewLoan_throwsIllegalStateException_whenMaxRenewalsReached() {
        loan.setRenewalCount(3);
        when(loanRepository.findById(1L)).thenReturn(Optional.of(loan));

        assertThatThrownBy(() -> service.renewLoan(1L, 7))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("máximo de renovaciones");
    }

    @Test
    void renewLoan_throwsIllegalStateException_whenReturned() {
        loan.setLoanStatus(returnedStatus);
        when(loanRepository.findById(1L)).thenReturn(Optional.of(loan));

        assertThatThrownBy(() -> service.renewLoan(1L, 7))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void renewLoan_throwsIllegalStateException_whenCancelled() {
        loan.setLoanStatus(cancelledStatus);
        when(loanRepository.findById(1L)).thenReturn(Optional.of(loan));

        assertThatThrownBy(() -> service.renewLoan(1L, 7))
                .isInstanceOf(IllegalStateException.class);
    }

    // ── cancelLoan ───────────────────────────────────────────────────────────

    @Test
    void cancelLoan_setsCancelledStatus_andFreesTheCopy() {
        copy.setStatus(CopyStatus.LOANED);
        when(loanRepository.findById(1L)).thenReturn(Optional.of(loan));
        when(loanStatusRepository.findByName("CANCELLED")).thenReturn(Optional.of(cancelledStatus));
        when(loanRepository.save(loan)).thenReturn(loan);

        LoanEntity result = service.cancelLoan(1L);

        assertThat(result.getLoanStatus().getName()).isEqualTo("CANCELLED");
        assertThat(copy.getStatus()).isEqualTo(CopyStatus.AVAILABLE);
        verify(bookCopyRepository).save(copy);
    }

    @Test
    void cancelLoan_throwsIllegalStateException_whenAlreadyCancelled() {
        loan.setLoanStatus(cancelledStatus);
        when(loanRepository.findById(1L)).thenReturn(Optional.of(loan));

        assertThatThrownBy(() -> service.cancelLoan(1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("ya se encuentra en estado");
    }

    @Test
    void cancelLoan_throwsIllegalStateException_whenAlreadyReturned() {
        loan.setLoanStatus(returnedStatus);
        when(loanRepository.findById(1L)).thenReturn(Optional.of(loan));

        assertThatThrownBy(() -> service.cancelLoan(1L))
                .isInstanceOf(IllegalStateException.class);
    }

    // ── delete ───────────────────────────────────────────────────────────────

    @Test
    void delete_callsRepositoryDelete_whenFound() {
        when(loanRepository.findById(1L)).thenReturn(Optional.of(loan));

        service.delete(1L);

        verify(loanRepository).delete(loan);
    }
}
