package dev.leo.library.infrastructure.adapter.output.persistence.adapter;

import dev.leo.library.domain.port.output.AuthorRepositoryPort;
import dev.leo.library.infrastructure.adapter.output.persistence.entity.AuthorEntity;
import dev.leo.library.infrastructure.adapter.output.persistence.repository.AuthorJpaRepository;
import dev.leo.library.infrastructure.adapter.output.persistence.repository.BookJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class AuthorPersistenceAdapter implements AuthorRepositoryPort {

    private final AuthorJpaRepository authorRepository;
    private final BookJpaRepository bookRepository;

    @Override
    public Optional<AuthorEntity> findById(Long id) {
        return authorRepository.findById(id);
    }

    @Override
    public List<AuthorEntity> findActiveOrderByLastName() {
        return authorRepository.findByActiveTrue(Sort.by("lastName").ascending());
    }

    @Override
    public Page<AuthorEntity> search(String q, String nationality, Boolean active, Pageable pageable) {
        return authorRepository.findAll(AuthorSpec.filter(q, nationality, active), pageable);
    }

    @Override
    public boolean existsByEmail(String email) {
        return authorRepository.existsByEmail(email);
    }

    @Override
    public AuthorEntity save(AuthorEntity author) {
        return authorRepository.save(author);
    }

    @Override
    public void delete(AuthorEntity author) {
        authorRepository.delete(author);
    }

    @Override
    public long countActiveByAuthorId(Long authorId) {
        return bookRepository.countActiveByAuthorId(authorId);
    }
}
