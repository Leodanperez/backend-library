package dev.leo.library.application.service;

import dev.leo.library.application.dto.request.CategoryRequest;
import dev.leo.library.application.dto.response.CategorySelectResponse;
import dev.leo.library.domain.exception.CategoryNotFoundException;
import dev.leo.library.domain.port.input.CategoryUseCase;
import dev.leo.library.infrastructure.adapter.output.persistence.adapter.CategorySpec;
import dev.leo.library.infrastructure.adapter.output.persistence.entity.CategoryEntity;
import dev.leo.library.infrastructure.adapter.output.persistence.repository.CategoryJpaRepository;
import dev.leo.library.shared.dto.PaginatedResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService implements CategoryUseCase {

    private final CategoryJpaRepository repository;

    @Override
    public PaginatedResponse<CategoryEntity> findAll(String q, Boolean active, int page, int perPage) {
        Page<CategoryEntity> result = repository.findAll(
                CategorySpec.filter(q, active),
                PageRequest.of(page - 1, perPage, Sort.by("name").ascending())
        );
        return PaginatedResponse.of(result.getContent(), page, perPage, result.getTotalElements());
    }

    @Override
    public List<CategorySelectResponse> findAllActive() {
        return repository.findByActiveTrueOrderByNameAsc().stream()
                .map(CategorySelectResponse::from)
                .toList();
    }

    @Override
    public CategoryEntity findById(Long id) {
        return repository.findById(id).orElseThrow(() -> new CategoryNotFoundException(id));
    }

    @Override
    @Transactional
    public CategoryEntity save(CategoryRequest dto) {
        if (repository.existsByName(dto.name()))
            throw new IllegalStateException("El nombre de categoría ya existe: " + dto.name());
        return repository.save(CategoryEntity.builder()
                .name(dto.name()).description(dto.description()).active(true).build());
    }

    @Override
    @Transactional
    public CategoryEntity update(Long id, CategoryRequest dto) {
        CategoryEntity category = findById(id);
        if (repository.existsByNameAndIdNot(dto.name(), id))
            throw new IllegalStateException("El nombre de categoría ya existe: " + dto.name());
        category.setName(dto.name());
        category.setDescription(dto.description());
        return repository.save(category);
    }

    @Override
    @Transactional
    public CategoryEntity activate(Long id) {
        CategoryEntity category = findById(id);
        category.setActive(true);
        return repository.save(category);
    }

    @Override
    @Transactional
    public CategoryEntity deactivate(Long id) {
        CategoryEntity category = findById(id);
        category.setActive(false);
        return repository.save(category);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        repository.delete(findById(id));
    }
}
