package dev.leo.library.application.service;

import dev.leo.library.application.dto.response.LoanStatusResponse;
import dev.leo.library.domain.port.input.LoanStatusUseCase;
import dev.leo.library.infrastructure.adapter.output.persistence.repository.LoanStatusJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LoanStatusService implements LoanStatusUseCase {

    private final LoanStatusJpaRepository repository;

    @Override
    public List<LoanStatusResponse> findAll() {
        return repository.findAll().stream().map(LoanStatusResponse::from).toList();
    }
}
