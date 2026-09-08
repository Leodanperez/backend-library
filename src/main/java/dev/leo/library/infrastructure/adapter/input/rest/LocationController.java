package dev.leo.library.infrastructure.adapter.input.rest;

import dev.leo.library.application.dto.request.LocationRequest;
import dev.leo.library.domain.port.input.LocationUseCase;
import dev.leo.library.infrastructure.adapter.output.persistence.entity.LocationEntity;
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
@RequestMapping("/api/v1/locations")
@RequiredArgsConstructor
@Tag(name = "Ubicaciones", description = "Gestión de ubicaciones físicas de la biblioteca (salas, pisos, secciones). Acceso público, no requiere autenticación.")
public class LocationController {

    private final LocationUseCase useCase;

    @GetMapping
    @Operation(
        summary = "Listar ubicaciones",
        description = "Devuelve una lista paginada de ubicaciones. Se puede filtrar por nombre o descripción con `?q=` y por estado activo/inactivo con `?active=`. No requiere autenticación."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lista de ubicaciones obtenida correctamente")
    })
    public PaginatedResponse<LocationEntity> findAll(
            @Parameter(description = "Texto libre para buscar por nombre o descripción") @RequestParam(required = false) String q,
            @Parameter(description = "Filtrar por estado: true = activas, false = inactivas") @RequestParam(required = false) Boolean active,
            @Parameter(description = "Número de página (empieza en 1)") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "Cantidad de resultados por página") @RequestParam(defaultValue = "10") int perPage) {
        return useCase.findAll(q, active, page, perPage);
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "Obtener ubicación por ID",
        description = "Devuelve el detalle completo de una ubicación a partir de su ID. No requiere autenticación."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Ubicación encontrada"),
        @ApiResponse(responseCode = "404", description = "Ubicación no encontrada")
    })
    public LocationEntity findById(
            @Parameter(description = "ID de la ubicación") @PathVariable Long id) {
        return useCase.findById(id);
    }

    @PostMapping
    @Operation(
        summary = "Crear ubicación",
        description = "Registra una nueva ubicación física en el sistema. El nombre debe ser único. No requiere autenticación."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Ubicación creada correctamente"),
        @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
        @ApiResponse(responseCode = "409", description = "Ya existe una ubicación con ese nombre")
    })
    public ResponseEntity<SuccessResponse> save(@Valid @RequestBody LocationRequest dto) {
        LocationEntity saved = useCase.save(dto);
        var location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(saved.getId()).toUri();
        return ResponseEntity.created(location)
                .body(SuccessResponse.of(HttpStatus.CREATED.value(), "Ubicación creada correctamente"));
    }

    @PutMapping("/{id}")
    @Operation(
        summary = "Actualizar ubicación",
        description = "Actualiza los datos de una ubicación existente. El nombre debe seguir siendo único. No requiere autenticación."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Ubicación actualizada correctamente"),
        @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
        @ApiResponse(responseCode = "404", description = "Ubicación no encontrada"),
        @ApiResponse(responseCode = "409", description = "Ya existe otra ubicación con ese nombre")
    })
    public ResponseEntity<SuccessResponse> update(
            @Parameter(description = "ID de la ubicación a actualizar") @PathVariable Long id,
            @Valid @RequestBody LocationRequest dto) {
        useCase.update(id, dto);
        return ResponseEntity.ok(SuccessResponse.of(HttpStatus.OK.value(), "Ubicación actualizada correctamente"));
    }

    @PatchMapping("/{id}/activate")
    @Operation(
        summary = "Activar ubicación",
        description = "Cambia el estado de una ubicación a activo. No requiere autenticación."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Ubicación activada correctamente"),
        @ApiResponse(responseCode = "404", description = "Ubicación no encontrada")
    })
    public ResponseEntity<SuccessResponse> activate(
            @Parameter(description = "ID de la ubicación") @PathVariable Long id) {
        useCase.activate(id);
        return ResponseEntity.ok(SuccessResponse.of(HttpStatus.OK.value(), "Ubicación activada correctamente"));
    }

    @PatchMapping("/{id}/deactivate")
    @Operation(
        summary = "Desactivar ubicación",
        description = "Cambia el estado de una ubicación a inactivo. No elimina el registro. No requiere autenticación."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Ubicación desactivada correctamente"),
        @ApiResponse(responseCode = "404", description = "Ubicación no encontrada")
    })
    public ResponseEntity<SuccessResponse> deactivate(
            @Parameter(description = "ID de la ubicación") @PathVariable Long id) {
        useCase.deactivate(id);
        return ResponseEntity.ok(SuccessResponse.of(HttpStatus.OK.value(), "Ubicación desactivada correctamente"));
    }

    @DeleteMapping("/{id}")
    @Operation(
        summary = "Eliminar ubicación",
        description = "Elimina permanentemente una ubicación del sistema. No requiere autenticación."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Ubicación eliminada correctamente"),
        @ApiResponse(responseCode = "404", description = "Ubicación no encontrada")
    })
    public ResponseEntity<SuccessResponse> delete(
            @Parameter(description = "ID de la ubicación a eliminar") @PathVariable Long id) {
        useCase.delete(id);
        return ResponseEntity.ok(SuccessResponse.of(HttpStatus.OK.value(), "Ubicación eliminada correctamente"));
    }
}
