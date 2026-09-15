package dev.leo.library.domain.port.input;

import dev.leo.library.application.dto.request.UserRequest;
import dev.leo.library.application.dto.request.UserUpdateRequest;
import dev.leo.library.application.dto.response.UserResponse;
import dev.leo.library.domain.model.UserRole;
import dev.leo.library.infrastructure.adapter.output.persistence.entity.UserEntity;
import dev.leo.library.shared.dto.PaginatedResponse;
import dev.leo.library.application.dto.response.SelectOptionsResponse.SelectItem;
import java.util.List;

public interface UserUseCase {
    List<SelectItem> findAllForSelect();
    PaginatedResponse<UserResponse> findAll(String q, UserRole role, Boolean active, int page, int perPage);
    UserResponse findById(Long id);
    UserResponse save(UserRequest dto);
    void update(Long id, UserUpdateRequest dto);
    void activate(Long id);
    void deactivate(Long id);
    void delete(Long id);
    UserEntity findEntityById(Long id);
    UserEntity findByEmail(String email);
}
