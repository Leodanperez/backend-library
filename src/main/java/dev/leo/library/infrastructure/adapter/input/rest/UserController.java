package dev.leo.library.infrastructure.adapter.input.rest;

import dev.leo.library.application.dto.request.UserRequest;
import dev.leo.library.domain.model.UserRole;
import dev.leo.library.domain.port.input.UserUseCase;
import dev.leo.library.infrastructure.adapter.output.persistence.entity.UserEntity;
import dev.leo.library.shared.dto.PaginatedResponse;
import dev.leo.library.shared.dto.SuccessResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserUseCase useCase;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public PaginatedResponse<UserEntity> findAll(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) UserRole role,
            @RequestParam(required = false) Boolean active,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int perPage) {
        return useCase.findAll(q, role, active, page, perPage);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public UserEntity findById(@PathVariable Long id) {
        return useCase.findById(id);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SuccessResponse> save(@Valid @RequestBody UserRequest dto) {
        UserEntity saved = useCase.save(dto);
        var location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(saved.getId()).toUri();
        return ResponseEntity.created(location)
                .body(SuccessResponse.of(HttpStatus.CREATED.value(), "Usuario creado correctamente"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SuccessResponse> update(@PathVariable Long id, @Valid @RequestBody UserRequest dto) {
        useCase.update(id, dto);
        return ResponseEntity.ok(SuccessResponse.of(HttpStatus.OK.value(), "Usuario actualizado correctamente"));
    }

    @PatchMapping("/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SuccessResponse> activate(@PathVariable Long id) {
        useCase.activate(id);
        return ResponseEntity.ok(SuccessResponse.of(HttpStatus.OK.value(), "Usuario activado correctamente"));
    }

    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SuccessResponse> deactivate(@PathVariable Long id) {
        useCase.deactivate(id);
        return ResponseEntity.ok(SuccessResponse.of(HttpStatus.OK.value(), "Usuario desactivado correctamente"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SuccessResponse> delete(@PathVariable Long id) {
        useCase.delete(id);
        return ResponseEntity.ok(SuccessResponse.of(HttpStatus.OK.value(), "Usuario eliminado correctamente"));
    }
}
