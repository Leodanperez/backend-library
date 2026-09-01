package dev.leo.library.application.service;

import dev.leo.library.application.dto.request.UserRequest;
import dev.leo.library.domain.exception.UserNotFoundException;
import dev.leo.library.domain.model.UserRole;
import dev.leo.library.domain.port.input.UserUseCase;
import dev.leo.library.infrastructure.adapter.output.persistence.adapter.UserSpec;
import dev.leo.library.infrastructure.adapter.output.persistence.entity.UserEntity;
import dev.leo.library.infrastructure.adapter.output.persistence.repository.UserJpaRepository;
import dev.leo.library.shared.dto.PaginatedResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService implements UserUseCase {

    private final UserJpaRepository repository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public PaginatedResponse<UserEntity> findAll(String q, UserRole role, Boolean active, int page, int perPage) {
        Page<UserEntity> result = repository.findAll(
                UserSpec.filter(q, role, active),
                PageRequest.of(page - 1, perPage, Sort.by("lastName").ascending())
        );
        return PaginatedResponse.of(result.getContent(), page, perPage, result.getTotalElements());
    }

    @Override
    public UserEntity findById(Long id) {
        return repository.findById(id).orElseThrow(() -> new UserNotFoundException(id));
    }

    @Override
    public UserEntity findByEmail(String email) {
        return repository.findByEmail(email).orElseThrow(() -> new UserNotFoundException(email));
    }

    @Override
    @Transactional
    public UserEntity save(UserRequest dto) {
        if (repository.existsByEmail(dto.email()))
            throw new IllegalStateException("El correo electrónico ya está registrado: " + dto.email());
        return repository.save(UserEntity.builder()
                .firstName(dto.firstName()).lastName(dto.lastName()).email(dto.email())
                .password(passwordEncoder.encode(dto.password()))
                .phone(dto.phone()).address(dto.address()).birthDate(dto.birthDate())
                .role(dto.role() != null ? dto.role() : UserRole.STUDENT)
                .active(true).build());
    }

    @Override
    @Transactional
    public UserEntity update(Long id, UserRequest dto) {
        UserEntity user = findById(id);
        if (!dto.email().equals(user.getEmail()) && repository.existsByEmail(dto.email()))
            throw new IllegalStateException("El correo electrónico ya está registrado: " + dto.email());
        user.setFirstName(dto.firstName()); user.setLastName(dto.lastName());
        user.setEmail(dto.email()); user.setPhone(dto.phone());
        user.setAddress(dto.address()); user.setBirthDate(dto.birthDate());
        if (dto.role() != null) user.setRole(dto.role());
        if (dto.active() != null) user.setActive(dto.active());
        return repository.save(user);
    }

    @Override
    @Transactional
    public UserEntity activate(Long id) {
        UserEntity user = findById(id);
        user.setActive(true);
        return repository.save(user);
    }

    @Override
    @Transactional
    public UserEntity deactivate(Long id) {
        UserEntity user = findById(id);
        user.setActive(false);
        return repository.save(user);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        repository.delete(findById(id));
    }
}
