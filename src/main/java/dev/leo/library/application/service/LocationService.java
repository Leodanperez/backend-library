package dev.leo.library.application.service;

import dev.leo.library.application.dto.request.LocationRequest;
import dev.leo.library.domain.exception.LocationNotFoundException;
import dev.leo.library.domain.port.input.LocationUseCase;
import dev.leo.library.infrastructure.adapter.output.persistence.adapter.LocationSpec;
import dev.leo.library.infrastructure.adapter.output.persistence.entity.LocationEntity;
import dev.leo.library.infrastructure.adapter.output.persistence.repository.LocationJpaRepository;
import dev.leo.library.shared.dto.PaginatedResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LocationService implements LocationUseCase {

    private final LocationJpaRepository repository;

    @Override
    public PaginatedResponse<LocationEntity> findAll(String q, Boolean active, int page, int perPage) {
        Page<LocationEntity> result = repository.findAll(
                LocationSpec.filter(q, active),
                PageRequest.of(page - 1, perPage, Sort.by("name").ascending())
        );
        return PaginatedResponse.of(result.getContent(), page, perPage, result.getTotalElements());
    }

    @Override
    public LocationEntity findById(Long id) {
        return repository.findById(id).orElseThrow(() -> new LocationNotFoundException(id));
    }

    @Override
    @Transactional
    public LocationEntity save(LocationRequest dto) {
        if (repository.existsByName(dto.name()))
            throw new IllegalStateException("El nombre de ubicación ya existe: " + dto.name());
        return repository.save(LocationEntity.builder()
                .name(dto.name())
                .floor(dto.floor())
                .capacity(dto.capacity())
                .description(dto.description())
                .active(true)
                .build());
    }

    @Override
    @Transactional
    public LocationEntity update(Long id, LocationRequest dto) {
        LocationEntity location = findById(id);
        if (repository.existsByNameAndIdNot(dto.name(), id))
            throw new IllegalStateException("El nombre de ubicación ya existe: " + dto.name());
        location.setName(dto.name());
        location.setFloor(dto.floor());
        location.setCapacity(dto.capacity());
        location.setDescription(dto.description());
        return repository.save(location);
    }

    @Override
    @Transactional
    public LocationEntity activate(Long id) {
        LocationEntity location = findById(id);
        location.setActive(true);
        return repository.save(location);
    }

    @Override
    @Transactional
    public LocationEntity deactivate(Long id) {
        LocationEntity location = findById(id);
        location.setActive(false);
        return repository.save(location);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        repository.delete(findById(id));
    }
}
