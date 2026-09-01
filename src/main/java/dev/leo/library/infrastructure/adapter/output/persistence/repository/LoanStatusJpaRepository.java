package dev.leo.library.infrastructure.adapter.output.persistence.repository;

import dev.leo.library.infrastructure.adapter.output.persistence.entity.LoanStatusEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface LoanStatusJpaRepository extends JpaRepository<LoanStatusEntity, Long> {
    Optional<LoanStatusEntity> findByName(String name);
}
