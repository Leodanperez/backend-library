package dev.leo.library.infrastructure.adapter.input.rest;

import dev.leo.library.application.dto.request.ExamRequest;
import dev.leo.library.application.dto.response.ExamResponse;
import dev.leo.library.domain.port.input.ExamUseCase;
import dev.leo.library.shared.dto.PaginatedResponse;
import dev.leo.library.shared.dto.SuccessResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/v1/exams")
@RequiredArgsConstructor
@Tag(name = "Exams", description = "Gestión de exámenes parciales")
public class ExamController {

    private final ExamUseCase useCase;

    @GetMapping
    @Operation(summary = "Listar exámenes", description = "Retorna una lista paginada de exámenes. Filtrá por materia/aula con `q` y por estado con `active`.")
    public PaginatedResponse<ExamResponse> findAll(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Boolean active,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int perPage) {
        return useCase.findAll(q, active, page, perPage);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener examen por ID")
    public ExamResponse findById(@PathVariable Long id) {
        return useCase.findById(id);
    }

    @PostMapping
    @Operation(summary = "Crear examen")
    public ResponseEntity<SuccessResponse> save(@Valid @RequestBody ExamRequest dto) {
        ExamResponse saved = useCase.save(dto);
        var location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(saved.id()).toUri();
        return ResponseEntity.created(location)
                .body(SuccessResponse.of(HttpStatus.CREATED.value(), "Examen creado correctamente"));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar examen")
    public ResponseEntity<SuccessResponse> update(@PathVariable Long id, @Valid @RequestBody ExamRequest dto) {
        useCase.update(id, dto);
        return ResponseEntity.ok(SuccessResponse.of(HttpStatus.OK.value(), "Examen actualizado correctamente"));
    }

    @PatchMapping("/{id}/activate")
    @Operation(summary = "Activar examen")
    public ResponseEntity<SuccessResponse> activate(@PathVariable Long id) {
        useCase.activate(id);
        return ResponseEntity.ok(SuccessResponse.of(HttpStatus.OK.value(), "Examen activado correctamente"));
    }

    @PatchMapping("/{id}/deactivate")
    @Operation(summary = "Desactivar examen")
    public ResponseEntity<SuccessResponse> deactivate(@PathVariable Long id) {
        useCase.deactivate(id);
        return ResponseEntity.ok(SuccessResponse.of(HttpStatus.OK.value(), "Examen desactivado correctamente"));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar examen")
    public ResponseEntity<SuccessResponse> delete(@PathVariable Long id) {
        useCase.delete(id);
        return ResponseEntity.ok(SuccessResponse.of(HttpStatus.OK.value(), "Examen eliminado correctamente"));
    }
}
