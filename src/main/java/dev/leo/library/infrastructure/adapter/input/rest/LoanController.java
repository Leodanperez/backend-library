package dev.leo.library.infrastructure.adapter.input.rest;

import dev.leo.library.application.dto.request.LoanRequest;
import dev.leo.library.application.dto.request.LoanRequestDto;
import dev.leo.library.application.dto.response.ActiveLoanResponse;
import dev.leo.library.application.dto.response.LoanRequestSummaryResponse;
import dev.leo.library.application.dto.response.LoanSummaryResponse;
import dev.leo.library.application.dto.response.MyLoanResponse;
import dev.leo.library.application.dto.response.MyLoanSummaryResponse;
import dev.leo.library.domain.port.input.LoanUseCase;
import dev.leo.library.infrastructure.security.UserPrincipal;
import dev.leo.library.shared.dto.PaginatedResponse;
import dev.leo.library.shared.dto.SuccessResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/loans")
@RequiredArgsConstructor
public class LoanController {

    private final LoanUseCase useCase;

    @GetMapping("/active")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public PaginatedResponse<ActiveLoanResponse> findActiveLoans(
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int perPage) {
        return useCase.findActiveLoans(q, page, perPage);
    }

    @GetMapping("/requests")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public LoanRequestSummaryResponse findRequests(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int perPage) {
        return useCase.findRequests(status, q, page, perPage);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public PaginatedResponse<LoanSummaryResponse> findAll(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long bookCopyId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int perPage) {
        return useCase.findAll(userId, bookCopyId, status, q, page, perPage);
    }

    @GetMapping("/my/summary")
    @PreAuthorize("hasRole('STUDENT')")
    public MyLoanSummaryResponse mySummary(@AuthenticationPrincipal UserPrincipal principal) {
        return useCase.getSummary(principal.user().getId());
    }

    @GetMapping("/my/history")
    @PreAuthorize("hasRole('STUDENT')")
    public PaginatedResponse<MyLoanResponse> myHistory(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int perPage) {
        return useCase.getHistory(principal.user().getId(), page, perPage);
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('STUDENT')")
    public PaginatedResponse<MyLoanResponse> myLoans(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(required = false) Long loanStatusId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int perPage) {
        return useCase.getMyLoans(principal.user().getId(), page, perPage);
    }

    @GetMapping("/{id}")
    public LoanSummaryResponse findById(@PathVariable Long id) {
        return useCase.findById(id);
    }

    // Estudiante solicita un préstamo online → queda en estado REQUESTED
    @PostMapping("/request")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<SuccessResponse> requestLoan(
            @Valid @RequestBody LoanRequestDto dto,
            @AuthenticationPrincipal UserPrincipal principal) {
        useCase.requestLoan(dto, principal.user().getId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(SuccessResponse.of(HttpStatus.CREATED.value(), "Solicitud de préstamo enviada correctamente"));
    }

    // Bibliotecario aprueba la solicitud → pasa a PENDING y el ejemplar queda LOANED
    @PatchMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public ResponseEntity<SuccessResponse> approveLoan(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        useCase.approveLoan(id, principal.user().getId());
        return ResponseEntity.ok(SuccessResponse.of(HttpStatus.OK.value(), "Solicitud aprobada correctamente"));
    }

    // Bibliotecario crea un préstamo directo (presencial) → queda en PENDING
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public ResponseEntity<SuccessResponse> save(@Valid @RequestBody LoanRequest dto) {
        useCase.save(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(SuccessResponse.of(HttpStatus.CREATED.value(), "Préstamo registrado correctamente"));
    }

    @PatchMapping("/{id}/return")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public ResponseEntity<SuccessResponse> returnLoan(
            @PathVariable Long id,
            @RequestBody(required = false) ReturnRequest body) {
        useCase.returnLoan(id, body != null ? body.observations() : null);
        return ResponseEntity.ok(SuccessResponse.of(HttpStatus.OK.value(), "Préstamo devuelto correctamente"));
    }

    record ReturnRequest(String observations) {}

    @PatchMapping("/{id}/renew")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public ResponseEntity<SuccessResponse> renewLoan(
            @PathVariable Long id,
            @RequestParam(defaultValue = "14") int days) {
        useCase.renewLoan(id, days);
        return ResponseEntity.ok(SuccessResponse.of(HttpStatus.OK.value(), "Préstamo renovado por " + days + " días"));
    }

    @PatchMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public ResponseEntity<SuccessResponse> cancelLoan(@PathVariable Long id) {
        useCase.cancelLoan(id);
        return ResponseEntity.ok(SuccessResponse.of(HttpStatus.OK.value(), "Préstamo cancelado correctamente"));
    }

    // Estudiante cancela su propia solicitud (solo si está en REQUESTED)
    @PatchMapping("/{id}/cancel/my")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<SuccessResponse> cancelMyLoan(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        useCase.cancelLoanByStudent(id, principal.user().getId());
        return ResponseEntity.ok(SuccessResponse.of(HttpStatus.OK.value(), "Solicitud cancelada correctamente"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public ResponseEntity<SuccessResponse> update(@PathVariable Long id, @Valid @RequestBody LoanRequest dto) {
        useCase.update(id, dto);
        return ResponseEntity.ok(SuccessResponse.of(HttpStatus.OK.value(), "Préstamo actualizado correctamente"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SuccessResponse> delete(@PathVariable Long id) {
        useCase.delete(id);
        return ResponseEntity.ok(SuccessResponse.of(HttpStatus.OK.value(), "Préstamo eliminado correctamente"));
    }
}
