package dev.leo.library.domain.port.output;

import dev.leo.library.infrastructure.adapter.output.persistence.entity.AuthorEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;

public interface AuthorRepositoryPort {
    Optional<AuthorEntity> findById(Long id);
    List<AuthorEntity> findActiveOrderByLastName();
    Page<AuthorEntity> search(String q, String nationality, Boolean active, Pageable pageable);
    boolean existsByEmail(String email);
    AuthorEntity save(AuthorEntity author);
    void delete(AuthorEntity author);
    long countActiveByAuthorId(Long authorId);
}
