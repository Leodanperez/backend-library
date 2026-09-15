package dev.leo.library.domain.port.output;

import dev.leo.library.domain.model.UserRole;
import dev.leo.library.infrastructure.adapter.output.persistence.entity.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;

public interface UserRepositoryPort {
    Optional<UserEntity> findById(Long id);
    Optional<UserEntity> findByEmail(String email);
    List<UserEntity> findAllSortedByLastName();
    Page<UserEntity> search(String q, UserRole role, Boolean active, Pageable pageable);
    boolean existsByEmail(String email);
    UserEntity save(UserEntity user);
    void delete(UserEntity user);
}
