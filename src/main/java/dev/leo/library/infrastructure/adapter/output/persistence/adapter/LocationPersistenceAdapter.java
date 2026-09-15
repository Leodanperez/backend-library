package dev.leo.library.infrastructure.adapter.output.persistence.adapter;

import dev.leo.library.domain.port.output.LocationRepositoryPort;
import dev.leo.library.infrastructure.adapter.output.persistence.entity.LocationEntity;
import dev.leo.library.infrastructure.adapter.output.persistence.repository.LocationJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class LocationPersistenceAdapter implements LocationRepositoryPort {

    private final LocationJpaRepository locationRepository;

    @Override
    public Optional<LocationEntity> findById(Long id) {
        return locationRepository.findById(id);
    }

    @Override
    public Page<LocationEntity> search(String q, Boolean active, Pageable pageable) {
        return locationRepository.findAll(LocationSpec.filter(q, active), pageable);
    }

    @Override
    public boolean existsByName(String name) {
        return locationRepository.existsByName(name);
    }

    @Override
    public boolean existsByNameAndIdNot(String name, Long id) {
        return locationRepository.existsByNameAndIdNot(name, id);
    }

    @Override
    public LocationEntity save(LocationEntity location) {
        return locationRepository.save(location);
    }

    @Override
    public void delete(LocationEntity location) {
        locationRepository.delete(location);
    }
}
