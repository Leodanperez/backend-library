package dev.leo.library.domain.port.output;

import dev.leo.library.infrastructure.adapter.output.persistence.entity.PublisherEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.Optional;

public interface PublisherRepositoryPort {
    Optional<PublisherEntity> findById(Long id);
    Page<PublisherEntity> search(String q, Boolean active, Pageable pageable);
    boolean existsByName(String name);
    boolean existsByNameAndIdNot(String name, Long id);
    PublisherEntity save(PublisherEntity publisher);
    void delete(PublisherEntity publisher);
}
