package dev.leo.library.infrastructure.adapter.input.rest;

import dev.leo.library.application.dto.request.AuthorRequest;
import dev.leo.library.domain.port.input.AuthorUseCase;
import dev.leo.library.infrastructure.adapter.output.persistence.entity.AuthorEntity;
import dev.leo.library.shared.dto.PaginatedResponse;
import dev.leo.library.shared.dto.SuccessResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/v1/authors")
@RequiredArgsConstructor
public class AuthorController {

    private final AuthorUseCase useCase;

    @GetMapping
    public PaginatedResponse<AuthorEntity> findAll(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String nationality,
            @RequestParam(required = false) Boolean active,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int perPage) {
        return useCase.findAll(q, nationality, active, page, perPage);
    }

    @GetMapping("/{id}")
    public AuthorEntity findById(@PathVariable Long id) {
        return useCase.findById(id);
    }

    @PostMapping
    public ResponseEntity<SuccessResponse> save(@Valid @RequestBody AuthorRequest dto) {
        AuthorEntity saved = useCase.save(dto);
        var location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(saved.getId()).toUri();
        return ResponseEntity.created(location)
                .body(SuccessResponse.of(HttpStatus.CREATED.value(), "Autor creado correctamente"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SuccessResponse> update(@PathVariable Long id, @Valid @RequestBody AuthorRequest dto) {
        useCase.update(id, dto);
        return ResponseEntity.ok(SuccessResponse.of(HttpStatus.OK.value(), "Autor actualizado correctamente"));
    }

    @PatchMapping("/{id}/activate")
    public ResponseEntity<SuccessResponse> activate(@PathVariable Long id) {
        useCase.activate(id);
        return ResponseEntity.ok(SuccessResponse.of(HttpStatus.OK.value(), "Autor activado correctamente"));
    }

    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<SuccessResponse> deactivate(@PathVariable Long id) {
        useCase.deactivate(id);
        return ResponseEntity.ok(SuccessResponse.of(HttpStatus.OK.value(), "Autor desactivado correctamente"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<SuccessResponse> delete(@PathVariable Long id) {
        useCase.delete(id);
        return ResponseEntity.ok(SuccessResponse.of(HttpStatus.OK.value(), "Autor eliminado correctamente"));
    }
}
