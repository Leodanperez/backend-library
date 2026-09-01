package dev.leo.library.infrastructure.adapter.input.rest;

import dev.leo.library.application.dto.request.CategoryRequest;
import dev.leo.library.application.dto.response.CategorySelectResponse;
import dev.leo.library.domain.port.input.CategoryUseCase;
import dev.leo.library.infrastructure.adapter.output.persistence.entity.CategoryEntity;
import dev.leo.library.shared.dto.PaginatedResponse;
import dev.leo.library.shared.dto.SuccessResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import java.util.List;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryUseCase useCase;

    @GetMapping
    public PaginatedResponse<CategoryEntity> findAll(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Boolean active,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int perPage) {
        return useCase.findAll(q, active, page, perPage);
    }

    @GetMapping("/select")
    public List<CategorySelectResponse> findAllActive() {
        return useCase.findAllActive();
    }

    @GetMapping("/{id}")
    public CategoryEntity findById(@PathVariable Long id) {
        return useCase.findById(id);
    }

    @PostMapping
    public ResponseEntity<SuccessResponse> save(@Valid @RequestBody CategoryRequest dto) {
        CategoryEntity saved = useCase.save(dto);
        var location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(saved.getId()).toUri();
        return ResponseEntity.created(location)
                .body(SuccessResponse.of(HttpStatus.CREATED.value(), "Categoría creada correctamente"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SuccessResponse> update(@PathVariable Long id, @Valid @RequestBody CategoryRequest dto) {
        useCase.update(id, dto);
        return ResponseEntity.ok(SuccessResponse.of(HttpStatus.OK.value(), "Categoría actualizada correctamente"));
    }

    @PatchMapping("/{id}/activate")
    public ResponseEntity<SuccessResponse> activate(@PathVariable Long id) {
        useCase.activate(id);
        return ResponseEntity.ok(SuccessResponse.of(HttpStatus.OK.value(), "Categoría activada correctamente"));
    }

    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<SuccessResponse> deactivate(@PathVariable Long id) {
        useCase.deactivate(id);
        return ResponseEntity.ok(SuccessResponse.of(HttpStatus.OK.value(), "Categoría desactivada correctamente"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<SuccessResponse> delete(@PathVariable Long id) {
        useCase.delete(id);
        return ResponseEntity.ok(SuccessResponse.of(HttpStatus.OK.value(), "Categoría eliminada correctamente"));
    }
}
