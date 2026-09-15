package dev.leo.library.infrastructure.adapter.output.persistence.adapter;

import dev.leo.library.domain.model.UserRole;
import dev.leo.library.domain.port.output.UserRepositoryPort;
import dev.leo.library.infrastructure.adapter.output.persistence.entity.UserEntity;
import dev.leo.library.infrastructure.adapter.output.persistence.repository.UserJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class UserPersistenceAdapter implements UserRepositoryPort {

    private final UserJpaRepository userRepository;

    @Override
    public Optional<UserEntity> findById(Long id) {
        return userRepository.findById(id);
    }

    @Override
    public Optional<UserEntity> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public List<UserEntity> findAllSortedByLastName() {
        return userRepository.findAll(Sort.by("lastName").ascending());
    }

    @Override
    public Page<UserEntity> search(String q, UserRole role, Boolean active, Pageable pageable) {
        return userRepository.findAll(UserSpec.filter(q, role, active), pageable);
    }

    @Override
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    public UserEntity save(UserEntity user) {
        return userRepository.save(user);
    }

    @Override
    public void delete(UserEntity user) {
        userRepository.delete(user);
    }
}
