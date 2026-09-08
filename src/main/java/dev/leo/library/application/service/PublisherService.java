package dev.leo.library.application.service;

import dev.leo.library.application.dto.request.PublisherRequest;
import dev.leo.library.domain.exception.PublisherNotFoundException;
import dev.leo.library.domain.port.input.PublisherUseCase;
import dev.leo.library.infrastructure.adapter.output.persistence.adapter.PublisherSpec;
import dev.leo.library.infrastructure.adapter.output.persistence.entity.PublisherEntity;
import dev.leo.library.infrastructure.adapter.output.persistence.repository.PublisherJpaRepository;
import dev.leo.library.shared.dto.PaginatedResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PublisherService implements PublisherUseCase {

    private final PublisherJpaRepository repository;

    @Override
    public PaginatedResponse<PublisherEntity> findAll(String q, Boolean active, int page, int perPage) {
        Page<PublisherEntity> result = repository.findAll(
                PublisherSpec.filter(q, active),
                PageRequest.of(page - 1, perPage, Sort.by("name").ascending())
        );
        return PaginatedResponse.of(result.getContent(), page, perPage, result.getTotalElements());
    }

    @Override
    public PublisherEntity findById(Long id) {
        return repository.findById(id).orElseThrow(() -> new PublisherNotFoundException(id));
    }

    @Override
    @Transactional
    public PublisherEntity save(PublisherRequest dto) {
        if (repository.existsByName(dto.name()))
            throw new IllegalStateException("El nombre de editorial ya existe: " + dto.name());
        return repository.save(PublisherEntity.builder()
                .name(dto.name())
                .country(dto.country())
                .foundedYear(dto.foundedYear())
                .website(dto.website())
                .active(true)
                .build());
    }

    @Override
    @Transactional
    public PublisherEntity update(Long id, PublisherRequest dto) {
        PublisherEntity publisher = findById(id);
        if (repository.existsByNameAndIdNot(dto.name(), id))
            throw new IllegalStateException("El nombre de editorial ya existe: " + dto.name());
        publisher.setName(dto.name());
        publisher.setCountry(dto.country());
        publisher.setFoundedYear(dto.foundedYear());
        publisher.setWebsite(dto.website());
        return repository.save(publisher);
    }

    @Override
    @Transactional
    public PublisherEntity activate(Long id) {
        PublisherEntity publisher = findById(id);
        publisher.setActive(true);
        return repository.save(publisher);
    }

    @Override
    @Transactional
    public PublisherEntity deactivate(Long id) {
        PublisherEntity publisher = findById(id);
        publisher.setActive(false);
        return repository.save(publisher);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        repository.delete(findById(id));
    }
}
