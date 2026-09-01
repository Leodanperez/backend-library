package dev.leo.library.infrastructure.adapter.input.rest;

import dev.leo.library.application.dto.request.LoanRequestDto;
import dev.leo.library.application.dto.response.BookCatalogResponse;
import dev.leo.library.application.dto.response.BookDetailResponse;
import dev.leo.library.domain.model.CopyStatus;
import dev.leo.library.domain.port.input.BookCopyUseCase;
import dev.leo.library.domain.port.input.BookUseCase;
import dev.leo.library.domain.port.input.LoanUseCase;
import dev.leo.library.infrastructure.adapter.output.persistence.entity.BookCopyEntity;
import dev.leo.library.infrastructure.adapter.output.persistence.entity.BookEntity;
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

import java.util.List;

@RestController
@RequestMapping("/api/v1/catalog")
@RequiredArgsConstructor
public class CatalogController {

    private final BookUseCase bookUseCase;
    private final BookCopyUseCase bookCopyUseCase;
    private final LoanUseCase loanUseCase;

    // Buscar libros activos con filtros: título, autor, categoría, idioma, año
    @GetMapping
    public PaginatedResponse<BookCatalogResponse> search(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Long authorId,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String language,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int perPage) {
        PaginatedResponse<BookEntity> books = bookUseCase.findAll(q, authorId, categoryId, language, true, page, perPage);
        List<BookCatalogResponse> mapped = books.data().stream().map(BookCatalogResponse::from).toList();
        return PaginatedResponse.of(mapped, books.page(), books.perPage(), books.total());
    }

    // Ver detalle de un libro con sus ejemplares y disponibilidad
    @GetMapping("/{id}")
    public BookDetailResponse detail(@PathVariable Long id) {
        BookEntity book = bookUseCase.findById(id);
        List<BookCopyEntity> copies = bookCopyUseCase.findAll(null, id, null, null, 1, 100).data();
        return BookDetailResponse.from(book, copies);
    }

    // Solicitar préstamo de un ejemplar específico (solo estudiantes autenticados)
    @PostMapping("/{bookId}/request")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<SuccessResponse> requestLoan(
            @PathVariable Long bookId,
            @Valid @RequestBody LoanRequestDto dto,
            @AuthenticationPrincipal UserPrincipal principal) {

        // Validar que el ejemplar pertenece al libro indicado
        BookCopyEntity copy = bookCopyUseCase.findById(dto.bookCopyId());
        if (!copy.getBook().getId().equals(bookId))
            throw new IllegalArgumentException("El ejemplar no pertenece al libro indicado");
        if (copy.getStatus() != CopyStatus.AVAILABLE)
            throw new IllegalStateException("El ejemplar no está disponible para préstamo");

        var saved = loanUseCase.requestLoan(dto, principal.user().getId());
        var location = ServletUriComponentsBuilder.fromCurrentRequest()
                .replacePath("/api/v1/loans/{id}").buildAndExpand(saved.getId()).toUri();
        return ResponseEntity.created(location)
                .body(SuccessResponse.of(HttpStatus.CREATED.value(), "Solicitud de préstamo enviada correctamente"));
    }
}
