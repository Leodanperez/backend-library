package dev.leo.library.infrastructure.adapter.input.rest;

import dev.leo.library.infrastructure.adapter.output.persistence.entity.LoanStatusEntity;
import dev.leo.library.infrastructure.adapter.output.persistence.repository.LoanStatusJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/loan-statuses")
@RequiredArgsConstructor
public class LoanStatusController {

    private final LoanStatusJpaRepository repository;

    @GetMapping
    public List<LoanStatusEntity> findAll() {
        return repository.findAll();
    }
}
