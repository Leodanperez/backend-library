package dev.leo.library.infrastructure.adapter.output.persistence.repository;

import dev.leo.library.infrastructure.adapter.output.persistence.entity.PublisherEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface PublisherJpaRepository extends JpaRepository<PublisherEntity, Long>, JpaSpecificationExecutor<PublisherEntity> {
    boolean existsByName(String name);
    boolean existsByNameAndIdNot(String name, Long id);
}
