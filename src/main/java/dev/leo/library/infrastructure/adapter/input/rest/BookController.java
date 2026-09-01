package dev.leo.library.infrastructure.adapter.input.rest;

import dev.leo.library.application.dto.request.BookRequest;
import dev.leo.library.domain.port.input.BookUseCase;
import dev.leo.library.infrastructure.adapter.output.persistence.entity.BookEntity;
import dev.leo.library.shared.dto.PaginatedResponse;
import dev.leo.library.shared.dto.SuccessResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/v1/books")
@RequiredArgsConstructor
public class BookController {

    private final BookUseCase useCase;

    @GetMapping
    public PaginatedResponse<BookEntity> findAll(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Long authorId,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String language,
            @RequestParam(required = false) Boolean active,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int perPage) {
        return useCase.findAll(q, authorId, categoryId, language, active, page, perPage);
    }

    @GetMapping("/{id}")
    public BookEntity findById(@PathVariable Long id) {
        return useCase.findById(id);
    }

    @PostMapping
    public ResponseEntity<SuccessResponse> save(@Valid @RequestBody BookRequest dto) {
        BookEntity saved = useCase.save(dto);
        var location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(saved.getId()).toUri();
        return ResponseEntity.created(location)
                .body(SuccessResponse.of(HttpStatus.CREATED.value(), "Libro creado correctamente"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SuccessResponse> update(@PathVariable Long id, @Valid @RequestBody BookRequest dto) {
        useCase.update(id, dto);
        return ResponseEntity.ok(SuccessResponse.of(HttpStatus.OK.value(), "Libro actualizado correctamente"));
    }

    @PatchMapping("/{id}/activate")
    public ResponseEntity<SuccessResponse> activate(@PathVariable Long id) {
        useCase.activate(id);
        return ResponseEntity.ok(SuccessResponse.of(HttpStatus.OK.value(), "Libro activado correctamente"));
    }

    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<SuccessResponse> deactivate(@PathVariable Long id) {
        useCase.deactivate(id);
        return ResponseEntity.ok(SuccessResponse.of(HttpStatus.OK.value(), "Libro desactivado correctamente"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<SuccessResponse> delete(@PathVariable Long id) {
        useCase.delete(id);
        return ResponseEntity.ok(SuccessResponse.of(HttpStatus.OK.value(), "Libro eliminado correctamente"));
    }
}
