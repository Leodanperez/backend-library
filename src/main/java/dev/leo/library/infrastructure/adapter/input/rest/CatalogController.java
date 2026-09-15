package dev.leo.library.infrastructure.adapter.input.rest;

import dev.leo.library.application.dto.request.LoanRequestDto;
import dev.leo.library.application.dto.response.BookCatalogResponse;
import dev.leo.library.application.dto.response.BookDetailResponse;
import dev.leo.library.domain.port.input.BookUseCase;
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
@RequestMapping("/api/v1/catalog")
@RequiredArgsConstructor
public class CatalogController {

    private final BookUseCase bookUseCase;
    private final LoanUseCase loanUseCase;

    @GetMapping
    public PaginatedResponse<BookCatalogResponse> search(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Long authorId,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String language,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int perPage) {
        return bookUseCase.searchCatalog(q, authorId, categoryId, language, page, perPage);
    }

    @GetMapping("/{id}")
    public BookDetailResponse detail(@PathVariable Long id) {
        return bookUseCase.detailCatalog(id);
    }

    @PostMapping("/{bookId}/request")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<SuccessResponse> requestLoan(
            @PathVariable Long bookId,
            @Valid @RequestBody LoanRequestDto dto,
            @AuthenticationPrincipal UserPrincipal principal) {
        loanUseCase.requestLoanFromCatalog(bookId, dto, principal.user().getId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(SuccessResponse.of(HttpStatus.CREATED.value(), "Solicitud de préstamo enviada correctamente"));
    }
}
