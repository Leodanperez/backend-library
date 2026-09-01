package dev.leo.library.infrastructure.adapter.input.rest;

import dev.leo.library.application.dto.request.BookCopyRequest;
import dev.leo.library.domain.model.CopyCondition;
import dev.leo.library.domain.model.CopyStatus;
import dev.leo.library.domain.port.input.BookCopyUseCase;
import dev.leo.library.infrastructure.adapter.output.persistence.entity.BookCopyEntity;
import dev.leo.library.shared.dto.PaginatedResponse;
import dev.leo.library.shared.dto.SuccessResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/v1/book-copies")
@RequiredArgsConstructor
public class BookCopyController {

    private final BookCopyUseCase useCase;

    @GetMapping
    public PaginatedResponse<BookCopyEntity> findAll(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Long bookId,
            @RequestParam(required = false) CopyStatus status,
            @RequestParam(required = false) CopyCondition condition,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int perPage) {
        return useCase.findAll(q, bookId, status, condition, page, perPage);
    }

    @GetMapping("/{id}")
    public BookCopyEntity findById(@PathVariable Long id) {
        return useCase.findById(id);
    }

    @PostMapping
    public ResponseEntity<SuccessResponse> save(@Valid @RequestBody BookCopyRequest dto) {
        BookCopyEntity saved = useCase.save(dto);
        var location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(saved.getId()).toUri();
        return ResponseEntity.created(location)
                .body(SuccessResponse.of(HttpStatus.CREATED.value(), "Ejemplar creado correctamente"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SuccessResponse> update(@PathVariable Long id, @Valid @RequestBody BookCopyRequest dto) {
        useCase.update(id, dto);
        return ResponseEntity.ok(SuccessResponse.of(HttpStatus.OK.value(), "Ejemplar actualizado correctamente"));
    }

    @PatchMapping("/{id}/lost")
    public ResponseEntity<SuccessResponse> markAsLost(@PathVariable Long id) {
        useCase.markAsLost(id);
        return ResponseEntity.ok(SuccessResponse.of(HttpStatus.OK.value(), "Ejemplar marcado como perdido"));
    }

    @PatchMapping("/{id}/damaged")
    public ResponseEntity<SuccessResponse> markAsDamaged(
            @PathVariable Long id,
            @RequestParam(required = false) CopyCondition condition) {
        useCase.markAsDamaged(id, condition);
        return ResponseEntity.ok(SuccessResponse.of(HttpStatus.OK.value(), "Ejemplar marcado como dañado"));
    }

    @PatchMapping("/{id}/restore")
    public ResponseEntity<SuccessResponse> restore(@PathVariable Long id) {
        useCase.restore(id);
        return ResponseEntity.ok(SuccessResponse.of(HttpStatus.OK.value(), "Ejemplar restaurado y disponible"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<SuccessResponse> delete(@PathVariable Long id) {
        useCase.delete(id);
        return ResponseEntity.ok(SuccessResponse.of(HttpStatus.OK.value(), "Ejemplar eliminado correctamente"));
    }
}
