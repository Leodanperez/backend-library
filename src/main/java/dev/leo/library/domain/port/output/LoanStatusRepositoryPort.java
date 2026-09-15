package dev.leo.library.domain.port.output;

import dev.leo.library.infrastructure.adapter.output.persistence.entity.LoanStatusEntity;
import java.util.List;
import java.util.Optional;

public interface LoanStatusRepositoryPort {
    Optional<LoanStatusEntity> findByName(String name);
    Optional<LoanStatusEntity> findById(Long id);
    List<LoanStatusEntity> findAll();
}
