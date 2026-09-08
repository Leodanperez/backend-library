package dev.leo.library.infrastructure.adapter.output.persistence.repository;

import dev.leo.library.infrastructure.adapter.output.persistence.entity.LocationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface LocationJpaRepository extends JpaRepository<LocationEntity, Long>, JpaSpecificationExecutor<LocationEntity> {
    boolean existsByName(String name);
    boolean existsByNameAndIdNot(String name, Long id);
}
