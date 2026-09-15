package dev.leo.library.application.service;

import dev.leo.library.application.dto.request.LocationRequest;
import dev.leo.library.application.dto.response.LocationResponse;
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
    public PaginatedResponse<LocationResponse> findAll(String q, Boolean active, int page, int perPage) {
        Page<LocationEntity> result = repository.findAll(
                LocationSpec.filter(q, active),
                PageRequest.of(page - 1, perPage, Sort.by("name").ascending())
        );
        return PaginatedResponse.of(result.getContent().stream().map(LocationResponse::from).toList(), page, perPage, result.getTotalElements());
    }

    @Override
    public LocationEntity findEntityById(Long id) {
        return repository.findById(id).orElseThrow(() -> new LocationNotFoundException(id));
    }

    @Override
    public LocationResponse findById(Long id) {
        return LocationResponse.from(findEntityById(id));
    }

    @Override
    @Transactional
    public LocationResponse save(LocationRequest dto) {
        if (repository.existsByName(dto.name()))
            throw new IllegalStateException("El nombre de ubicación ya existe: " + dto.name());
        return LocationResponse.from(repository.save(LocationEntity.builder()
                .name(dto.name()).floor(dto.floor()).capacity(dto.capacity())
                .description(dto.description()).active(true).build()));
    }

    @Override
    @Transactional
    public void update(Long id, LocationRequest dto) {
        LocationEntity location = findEntityById(id);
        if (repository.existsByNameAndIdNot(dto.name(), id))
            throw new IllegalStateException("El nombre de ubicación ya existe: " + dto.name());
        location.setName(dto.name()); location.setFloor(dto.floor());
        location.setCapacity(dto.capacity()); location.setDescription(dto.description());
        repository.save(location);
    }

    @Override
    @Transactional
    public void activate(Long id) {
        LocationEntity location = findEntityById(id);
        location.setActive(true);
        repository.save(location);
    }

    @Override
    @Transactional
    public void deactivate(Long id) {
        LocationEntity location = findEntityById(id);
        location.setActive(false);
        repository.save(location);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        repository.delete(findEntityById(id));
    }
}
