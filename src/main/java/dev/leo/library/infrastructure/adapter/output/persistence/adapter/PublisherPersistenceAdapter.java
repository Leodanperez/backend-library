package dev.leo.library.infrastructure.adapter.output.persistence.adapter;

import dev.leo.library.domain.port.output.PublisherRepositoryPort;
import dev.leo.library.infrastructure.adapter.output.persistence.entity.PublisherEntity;
import dev.leo.library.infrastructure.adapter.output.persistence.repository.PublisherJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class PublisherPersistenceAdapter implements PublisherRepositoryPort {

    private final PublisherJpaRepository publisherRepository;

    @Override
    public Optional<PublisherEntity> findById(Long id) {
        return publisherRepository.findById(id);
    }

    @Override
    public Page<PublisherEntity> search(String q, Boolean active, Pageable pageable) {
        return publisherRepository.findAll(PublisherSpec.filter(q, active), pageable);
    }

    @Override
    public boolean existsByName(String name) {
        return publisherRepository.existsByName(name);
    }

    @Override
    public boolean existsByNameAndIdNot(String name, Long id) {
        return publisherRepository.existsByNameAndIdNot(name, id);
    }

    @Override
    public PublisherEntity save(PublisherEntity publisher) {
        return publisherRepository.save(publisher);
    }

    @Override
    public void delete(PublisherEntity publisher) {
        publisherRepository.delete(publisher);
    }
}
