package dev.leo.library.infrastructure.adapter.input.rest;

import dev.leo.library.application.dto.request.LoanRequest;
import dev.leo.library.domain.port.input.LoanUseCase;
import dev.leo.library.infrastructure.adapter.output.persistence.entity.LoanEntity;
import dev.leo.library.shared.dto.PaginatedResponse;
import dev.leo.library.shared.dto.SuccessResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/v1/loans")
@RequiredArgsConstructor
public class LoanController {

    private final LoanUseCase useCase;

    @GetMapping
    public PaginatedResponse<LoanEntity> findAll(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long bookCopyId,
            @RequestParam(required = false) Long loanStatusId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int perPage) {
        return useCase.findAll(userId, bookCopyId, loanStatusId, page, perPage);
    }

    @GetMapping("/{id}")
    public LoanEntity findById(@PathVariable Long id) {
        return useCase.findById(id);
    }

    @PostMapping
    public ResponseEntity<SuccessResponse> save(@Valid @RequestBody LoanRequest dto) {
        LoanEntity saved = useCase.save(dto);
        var location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(saved.getId()).toUri();
        return ResponseEntity.created(location)
                .body(SuccessResponse.of(HttpStatus.CREATED.value(), "Préstamo registrado correctamente"));
    }

    @PatchMapping("/{id}/return")
    public ResponseEntity<SuccessResponse> returnLoan(@PathVariable Long id) {
        useCase.returnLoan(id);
        return ResponseEntity.ok(SuccessResponse.of(HttpStatus.OK.value(), "Préstamo devuelto correctamente"));
    }

    @PatchMapping("/{id}/renew")
    public ResponseEntity<SuccessResponse> renewLoan(
            @PathVariable Long id,
            @RequestParam(defaultValue = "14") int days) {
        useCase.renewLoan(id, days);
        return ResponseEntity.ok(SuccessResponse.of(HttpStatus.OK.value(), "Préstamo renovado por " + days + " días"));
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<SuccessResponse> cancelLoan(@PathVariable Long id) {
        useCase.cancelLoan(id);
        return ResponseEntity.ok(SuccessResponse.of(HttpStatus.OK.value(), "Préstamo cancelado correctamente"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SuccessResponse> update(@PathVariable Long id, @Valid @RequestBody LoanRequest dto) {
        useCase.update(id, dto);
        return ResponseEntity.ok(SuccessResponse.of(HttpStatus.OK.value(), "Préstamo actualizado correctamente"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<SuccessResponse> delete(@PathVariable Long id) {
        useCase.delete(id);
        return ResponseEntity.ok(SuccessResponse.of(HttpStatus.OK.value(), "Préstamo eliminado correctamente"));
    }
}
