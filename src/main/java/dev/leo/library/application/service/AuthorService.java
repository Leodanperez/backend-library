package dev.leo.library.application.service;

import dev.leo.library.application.dto.request.AuthorRequest;
import dev.leo.library.application.dto.response.AuthorResponse;
import dev.leo.library.application.dto.response.SelectOptionsResponse.SelectItem;
import dev.leo.library.domain.exception.AuthorNotFoundException;
import dev.leo.library.domain.port.input.AuthorUseCase;
import dev.leo.library.infrastructure.adapter.output.persistence.adapter.AuthorSpec;
import dev.leo.library.infrastructure.adapter.output.persistence.entity.AuthorEntity;
import dev.leo.library.infrastructure.adapter.output.persistence.repository.AuthorJpaRepository;
import dev.leo.library.infrastructure.adapter.output.persistence.repository.BookJpaRepository;
import dev.leo.library.shared.dto.PaginatedResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthorService implements AuthorUseCase {

    private final AuthorJpaRepository repository;
    private final BookJpaRepository bookRepository;

    @Override
    public List<SelectItem> findAllForSelect() {
        return repository.findByActiveTrue(Sort.by("lastName").ascending()).stream()
                .map(a -> new SelectItem(a.getId(), a.getFirstName() + " " + a.getLastName()))
                .toList();
    }

    @Override
    public PaginatedResponse<AuthorResponse> findAll(String q, String nationality, Boolean active, int page, int perPage) {
        Page<AuthorEntity> result = repository.findAll(
                AuthorSpec.filter(q, nationality, active),
                PageRequest.of(page - 1, perPage, Sort.by("lastName").ascending())
        );
        var mapped = result.getContent().stream()
                .map(a -> AuthorResponse.from(a, bookRepository.countActiveByAuthorId(a.getId())))
                .toList();
        return PaginatedResponse.of(mapped, page, perPage, result.getTotalElements());
    }

    public AuthorEntity findEntityById(Long id) {
        return repository.findById(id).orElseThrow(() -> new AuthorNotFoundException(id));
    }

    @Override
    public AuthorResponse findById(Long id) {
        return AuthorResponse.from(findEntityById(id), bookRepository.countActiveByAuthorId(id));
    }

    @Override
    @Transactional
    public AuthorResponse save(AuthorRequest dto) {
        if (dto.email() != null && repository.existsByEmail(dto.email()))
            throw new IllegalStateException("El correo electrónico ya está registrado: " + dto.email());
        AuthorEntity saved = repository.save(AuthorEntity.builder()
                .firstName(dto.firstName()).lastName(dto.lastName())
                .pseudonym(dto.pseudonym()).birthDate(dto.birthDate())
                .nationality(dto.nationality()).biography(dto.biography())
                .email(dto.email()).active(true).build());
        return AuthorResponse.from(saved, 0);
    }

    @Override
    @Transactional
    public void update(Long id, AuthorRequest dto) {
        AuthorEntity author = findEntityById(id);
        if (dto.email() != null && !dto.email().equals(author.getEmail()) && repository.existsByEmail(dto.email()))
            throw new IllegalStateException("El correo electrónico ya está registrado: " + dto.email());
        author.setFirstName(dto.firstName()); author.setLastName(dto.lastName());
        author.setPseudonym(dto.pseudonym()); author.setBirthDate(dto.birthDate());
        author.setNationality(dto.nationality()); author.setBiography(dto.biography());
        author.setEmail(dto.email());
        repository.save(author);
    }

    @Override
    @Transactional
    public void activate(Long id) {
        AuthorEntity author = findEntityById(id);
        author.setActive(true);
        repository.save(author);
    }

    @Override
    @Transactional
    public void deactivate(Long id) {
        AuthorEntity author = findEntityById(id);
        author.setActive(false);
        repository.save(author);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        repository.delete(findEntityById(id));
    }
}
