package dev.leo.library.infrastructure.adapter.output.persistence.repository;

import dev.leo.library.infrastructure.adapter.output.persistence.entity.AuthorEntity;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import java.util.List;
import java.util.Optional;

public interface AuthorJpaRepository extends JpaRepository<AuthorEntity, Long>, JpaSpecificationExecutor<AuthorEntity> {
    Optional<AuthorEntity> findByEmail(String email);
    boolean existsByEmail(String email);
    List<AuthorEntity> findByActiveTrue(Sort sort);
}
