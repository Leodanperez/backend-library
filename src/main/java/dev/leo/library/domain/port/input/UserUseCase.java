package dev.leo.library.domain.port.input;

import dev.leo.library.application.dto.request.UserRequest;
import dev.leo.library.domain.model.UserRole;
import dev.leo.library.infrastructure.adapter.output.persistence.entity.UserEntity;
import dev.leo.library.shared.dto.PaginatedResponse;

public interface UserUseCase {
    PaginatedResponse<UserEntity> findAll(String q, UserRole role, Boolean active, int page, int perPage);
    UserEntity findById(Long id);
    UserEntity findByEmail(String email);
    UserEntity save(UserRequest dto);
    UserEntity update(Long id, UserRequest dto);
    UserEntity activate(Long id);
    UserEntity deactivate(Long id);
    void delete(Long id);
}
