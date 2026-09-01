package dev.leo.library.application.service;

import dev.leo.library.application.dto.request.AuthorRequest;
import dev.leo.library.domain.exception.AuthorNotFoundException;
import dev.leo.library.domain.port.input.AuthorUseCase;
import dev.leo.library.infrastructure.adapter.output.persistence.adapter.AuthorSpec;
import dev.leo.library.infrastructure.adapter.output.persistence.entity.AuthorEntity;
import dev.leo.library.infrastructure.adapter.output.persistence.repository.AuthorJpaRepository;
import dev.leo.library.shared.dto.PaginatedResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthorService implements AuthorUseCase {

    private final AuthorJpaRepository repository;

    @Override
    public PaginatedResponse<AuthorEntity> findAll(String q, String nationality, Boolean active, int page, int perPage) {
        Page<AuthorEntity> result = repository.findAll(
                AuthorSpec.filter(q, nationality, active),
                PageRequest.of(page - 1, perPage, Sort.by("lastName").ascending())
        );
        return PaginatedResponse.of(result.getContent(), page, perPage, result.getTotalElements());
    }

    @Override
    public AuthorEntity findById(Long id) {
        return repository.findById(id).orElseThrow(() -> new AuthorNotFoundException(id));
    }

    @Override
    @Transactional
    public AuthorEntity save(AuthorRequest dto) {
        if (dto.email() != null && repository.existsByEmail(dto.email()))
            throw new IllegalStateException("El correo electrónico ya está registrado: " + dto.email());
        return repository.save(AuthorEntity.builder()
                .firstName(dto.firstName()).lastName(dto.lastName())
                .pseudonym(dto.pseudonym()).birthDate(dto.birthDate())
                .nationality(dto.nationality()).biography(dto.biography())
                .email(dto.email()).active(true).build());
    }

    @Override
    @Transactional
    public AuthorEntity update(Long id, AuthorRequest dto) {
        AuthorEntity author = findById(id);
        if (dto.email() != null && !dto.email().equals(author.getEmail()) && repository.existsByEmail(dto.email()))
            throw new IllegalStateException("El correo electrónico ya está registrado: " + dto.email());
        author.setFirstName(dto.firstName()); author.setLastName(dto.lastName());
        author.setPseudonym(dto.pseudonym()); author.setBirthDate(dto.birthDate());
        author.setNationality(dto.nationality()); author.setBiography(dto.biography());
        author.setEmail(dto.email());
        return repository.save(author);
    }

    @Override
    @Transactional
    public AuthorEntity activate(Long id) {
        AuthorEntity author = findById(id);
        author.setActive(true);
        return repository.save(author);
    }

    @Override
    @Transactional
    public AuthorEntity deactivate(Long id) {
        AuthorEntity author = findById(id);
        author.setActive(false);
        return repository.save(author);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        repository.delete(findById(id));
    }
}
