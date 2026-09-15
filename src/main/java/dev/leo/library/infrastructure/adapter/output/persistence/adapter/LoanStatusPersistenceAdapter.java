package dev.leo.library.infrastructure.adapter.output.persistence.adapter;

import dev.leo.library.domain.port.output.LoanStatusRepositoryPort;
import dev.leo.library.infrastructure.adapter.output.persistence.entity.LoanStatusEntity;
import dev.leo.library.infrastructure.adapter.output.persistence.repository.LoanStatusJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class LoanStatusPersistenceAdapter implements LoanStatusRepositoryPort {

    private final LoanStatusJpaRepository loanStatusRepository;

    @Override
    public Optional<LoanStatusEntity> findByName(String name) {
        return loanStatusRepository.findByName(name);
    }

    @Override
    public Optional<LoanStatusEntity> findById(Long id) {
        return loanStatusRepository.findById(id);
    }

    @Override
    public List<LoanStatusEntity> findAll() {
        return loanStatusRepository.findAll();
    }
}
