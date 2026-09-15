package dev.leo.library.application.service;

import dev.leo.library.application.dto.request.UserRequest;
import dev.leo.library.application.dto.request.UserUpdateRequest;
import dev.leo.library.application.dto.response.SelectOptionsResponse.SelectItem;
import dev.leo.library.application.dto.response.UserResponse;
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
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService implements UserUseCase {

    private final UserJpaRepository repository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public List<SelectItem> findAllForSelect() {
        return repository.findAll(Sort.by("lastName").ascending()).stream()
                .filter(UserEntity::isActive)
                .map(u -> new SelectItem(u.getId(), u.getFirstName() + " " + u.getLastName()))
                .toList();
    }

    @Override
    public PaginatedResponse<UserResponse> findAll(String q, UserRole role, Boolean active, int page, int perPage) {
        Page<UserEntity> result = repository.findAll(
                UserSpec.filter(q, role, active),
                PageRequest.of(page - 1, perPage, Sort.by("lastName").ascending())
        );
        return PaginatedResponse.of(result.getContent().stream().map(UserResponse::from).toList(), page, perPage, result.getTotalElements());
    }

    @Override
    public UserEntity findEntityById(Long id) {
        return repository.findById(id).orElseThrow(() -> new UserNotFoundException(id));
    }

    @Override
    public UserResponse findById(Long id) {
        return UserResponse.from(findEntityById(id));
    }

    @Override
    public UserEntity findByEmail(String email) {
        return repository.findByEmail(email).orElseThrow(() -> new UserNotFoundException(email));
    }

    @Override
    @Transactional
    public UserResponse save(UserRequest dto) {
        if (repository.existsByEmail(dto.email()))
            throw new IllegalStateException("El correo electrónico ya está registrado: " + dto.email());
        return UserResponse.from(repository.save(UserEntity.builder()
                .firstName(dto.firstName()).lastName(dto.lastName()).email(dto.email())
                .password(passwordEncoder.encode(dto.password()))
                .phone(dto.phone()).address(dto.address()).birthDate(dto.birthDate())
                .role(dto.role() != null ? dto.role() : UserRole.STUDENT)
                .active(true).build()));
    }

    @Override
    @Transactional
    public void update(Long id, UserUpdateRequest dto) {
        UserEntity user = findEntityById(id);
        if (!dto.email().equals(user.getEmail()) && repository.existsByEmail(dto.email()))
            throw new IllegalStateException("El correo electrónico ya está registrado: " + dto.email());
        user.setFirstName(dto.firstName()); user.setLastName(dto.lastName());
        user.setEmail(dto.email()); user.setPhone(dto.phone());
        user.setAddress(dto.address()); user.setBirthDate(dto.birthDate());
        if (dto.role() != null) user.setRole(dto.role());
        if (dto.active() != null) user.setActive(dto.active());
        repository.save(user);
    }

    @Override
    @Transactional
    public void activate(Long id) {
        UserEntity user = findEntityById(id);
        user.setActive(true);
        repository.save(user);
    }

    @Override
    @Transactional
    public void deactivate(Long id) {
        UserEntity user = findEntityById(id);
        user.setActive(false);
        repository.save(user);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        repository.delete(findEntityById(id));
    }
}
