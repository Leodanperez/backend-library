package dev.leo.library.domain.port.output;

import dev.leo.library.infrastructure.adapter.output.persistence.entity.LocationEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.Optional;

public interface LocationRepositoryPort {
    Optional<LocationEntity> findById(Long id);
    Page<LocationEntity> search(String q, Boolean active, Pageable pageable);
    boolean existsByName(String name);
    boolean existsByNameAndIdNot(String name, Long id);
    LocationEntity save(LocationEntity location);
    void delete(LocationEntity location);
}
