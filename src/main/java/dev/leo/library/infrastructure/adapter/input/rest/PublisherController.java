package dev.leo.library.infrastructure.adapter.input.rest;

import dev.leo.library.application.dto.request.PublisherRequest;
import dev.leo.library.domain.port.input.PublisherUseCase;
import dev.leo.library.infrastructure.adapter.output.persistence.entity.PublisherEntity;
import dev.leo.library.shared.dto.PaginatedResponse;
import dev.leo.library.shared.dto.SuccessResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/v1/publishers")
@RequiredArgsConstructor
@Tag(name = "Editoriales", description = "Gestión de editoriales. Permite crear, consultar, actualizar y eliminar editoriales del sistema. Acceso público, no requiere autenticación.")
public class PublisherController {

    private final PublisherUseCase useCase;

    @GetMapping
    @Operation(
        summary = "Listar editoriales",
        description = "Devuelve una lista paginada de editoriales. Se puede filtrar por nombre o país con `?q=` y por estado activo/inactivo con `?active=`. No requiere autenticación."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lista de editoriales obtenida correctamente")
    })
    public PaginatedResponse<PublisherEntity> findAll(
            @Parameter(description = "Texto libre para buscar por nombre o país") @RequestParam(required = false) String q,
            @Parameter(description = "Filtrar por estado: true = activas, false = inactivas") @RequestParam(required = false) Boolean active,
            @Parameter(description = "Número de página (empieza en 1)") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "Cantidad de resultados por página") @RequestParam(defaultValue = "10") int perPage) {
        return useCase.findAll(q, active, page, perPage);
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "Obtener editorial por ID",
        description = "Devuelve el detalle completo de una editorial a partir de su ID. No requiere autenticación."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Editorial encontrada"),
        @ApiResponse(responseCode = "404", description = "Editorial no encontrada")
    })
    public PublisherEntity findById(
            @Parameter(description = "ID de la editorial") @PathVariable Long id) {
        return useCase.findById(id);
    }

    @PostMapping
    @Operation(
        summary = "Crear editorial",
        description = "Registra una nueva editorial en el sistema. El nombre debe ser único. No requiere autenticación."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Editorial creada correctamente"),
        @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
        @ApiResponse(responseCode = "409", description = "Ya existe una editorial con ese nombre")
    })
    public ResponseEntity<SuccessResponse> save(@Valid @RequestBody PublisherRequest dto) {
        PublisherEntity saved = useCase.save(dto);
        var location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(saved.getId()).toUri();
        return ResponseEntity.created(location)
                .body(SuccessResponse.of(HttpStatus.CREATED.value(), "Editorial creada correctamente"));
    }

    @PutMapping("/{id}")
    @Operation(
        summary = "Actualizar editorial",
        description = "Actualiza los datos de una editorial existente. El nombre debe seguir siendo único. No requiere autenticación."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Editorial actualizada correctamente"),
        @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
        @ApiResponse(responseCode = "404", description = "Editorial no encontrada"),
        @ApiResponse(responseCode = "409", description = "Ya existe otra editorial con ese nombre")
    })
    public ResponseEntity<SuccessResponse> update(
            @Parameter(description = "ID de la editorial a actualizar") @PathVariable Long id,
            @Valid @RequestBody PublisherRequest dto) {
        useCase.update(id, dto);
        return ResponseEntity.ok(SuccessResponse.of(HttpStatus.OK.value(), "Editorial actualizada correctamente"));
    }

    @PatchMapping("/{id}/activate")
    @Operation(
        summary = "Activar editorial",
        description = "Cambia el estado de una editorial a activo. No requiere autenticación."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Editorial activada correctamente"),
        @ApiResponse(responseCode = "404", description = "Editorial no encontrada")
    })
    public ResponseEntity<SuccessResponse> activate(
            @Parameter(description = "ID de la editorial") @PathVariable Long id) {
        useCase.activate(id);
        return ResponseEntity.ok(SuccessResponse.of(HttpStatus.OK.value(), "Editorial activada correctamente"));
    }

    @PatchMapping("/{id}/deactivate")
    @Operation(
        summary = "Desactivar editorial",
        description = "Cambia el estado de una editorial a inactivo. No elimina el registro. No requiere autenticación."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Editorial desactivada correctamente"),
        @ApiResponse(responseCode = "404", description = "Editorial no encontrada")
    })
    public ResponseEntity<SuccessResponse> deactivate(
            @Parameter(description = "ID de la editorial") @PathVariable Long id) {
        useCase.deactivate(id);
        return ResponseEntity.ok(SuccessResponse.of(HttpStatus.OK.value(), "Editorial desactivada correctamente"));
    }

    @DeleteMapping("/{id}")
    @Operation(
        summary = "Eliminar editorial",
        description = "Elimina permanentemente una editorial del sistema. No requiere autenticación."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Editorial eliminada correctamente"),
        @ApiResponse(responseCode = "404", description = "Editorial no encontrada")
    })
    public ResponseEntity<SuccessResponse> delete(
            @Parameter(description = "ID de la editorial a eliminar") @PathVariable Long id) {
        useCase.delete(id);
        return ResponseEntity.ok(SuccessResponse.of(HttpStatus.OK.value(), "Editorial eliminada correctamente"));
    }
}
