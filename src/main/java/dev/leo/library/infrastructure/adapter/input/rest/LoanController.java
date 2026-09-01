package dev.leo.library.infrastructure.adapter.input.rest;

import dev.leo.library.application.dto.request.LoanRequest;
import dev.leo.library.application.dto.request.LoanRequestDto;
import dev.leo.library.domain.port.input.LoanUseCase;
import dev.leo.library.infrastructure.adapter.output.persistence.entity.LoanEntity;
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
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/v1/loans")
@RequiredArgsConstructor
public class LoanController {

    private final LoanUseCase useCase;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public PaginatedResponse<LoanEntity> findAll(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long bookCopyId,
            @RequestParam(required = false) Long loanStatusId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int perPage) {
        return useCase.findAll(userId, bookCopyId, loanStatusId, page, perPage);
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('STUDENT')")
    public PaginatedResponse<LoanEntity> myLoans(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(required = false) Long loanStatusId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int perPage) {
        return useCase.findAll(principal.user().getId(), null, loanStatusId, page, perPage);
    }

    @GetMapping("/{id}")
    public LoanEntity findById(@PathVariable Long id) {
        return useCase.findById(id);
    }

    // Estudiante solicita un préstamo online → queda en estado REQUESTED
    @PostMapping("/request")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<SuccessResponse> requestLoan(
            @Valid @RequestBody LoanRequestDto dto,
            @AuthenticationPrincipal UserPrincipal principal) {
        LoanEntity saved = useCase.requestLoan(dto, principal.user().getId());
        var location = ServletUriComponentsBuilder.fromCurrentRequest()
                .replacePath("/api/v1/loans/{id}").buildAndExpand(saved.getId()).toUri();
        return ResponseEntity.created(location)
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
        LoanEntity saved = useCase.save(dto);
        var location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(saved.getId()).toUri();
        return ResponseEntity.created(location)
                .body(SuccessResponse.of(HttpStatus.CREATED.value(), "Préstamo registrado correctamente"));
    }

    @PatchMapping("/{id}/return")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public ResponseEntity<SuccessResponse> returnLoan(@PathVariable Long id) {
        useCase.returnLoan(id);
        return ResponseEntity.ok(SuccessResponse.of(HttpStatus.OK.value(), "Préstamo devuelto correctamente"));
    }

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
