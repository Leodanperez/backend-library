package dev.leo.library.application.service;

import dev.leo.library.application.dto.request.CategoryRequest;
import dev.leo.library.application.dto.response.CategoryResponse;
import dev.leo.library.application.dto.response.SelectOptionsResponse.SelectItem;
import dev.leo.library.domain.exception.CategoryNotFoundException;
import dev.leo.library.domain.port.input.CategoryUseCase;
import dev.leo.library.infrastructure.adapter.output.persistence.adapter.CategorySpec;
import dev.leo.library.infrastructure.adapter.output.persistence.entity.CategoryEntity;
import dev.leo.library.infrastructure.adapter.output.persistence.repository.BookJpaRepository;
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
    private final BookJpaRepository bookRepository;

    @Override
    public PaginatedResponse<CategoryResponse> findAll(String q, Boolean active, int page, int perPage) {
        Page<CategoryEntity> result = repository.findAll(
                CategorySpec.filter(q, active),
                PageRequest.of(page - 1, perPage, Sort.by("name").ascending())
        );
        var mapped = result.getContent().stream()
                .map(c -> CategoryResponse.from(c, bookRepository.countActiveByCategoryId(c.getId())))
                .toList();
        return PaginatedResponse.of(mapped, page, perPage, result.getTotalElements());
    }

    @Override
    public List<SelectItem> findAllActive() {
        return repository.findByActiveTrueOrderByNameAsc().stream()
                .map(c -> new SelectItem(c.getId(), c.getName()))
                .toList();
    }

    @Override
    public CategoryEntity findEntityById(Long id) {
        return repository.findById(id).orElseThrow(() -> new CategoryNotFoundException(id));
    }

    @Override
    public CategoryResponse findById(Long id) {
        return CategoryResponse.from(findEntityById(id), bookRepository.countActiveByCategoryId(id));
    }

    @Override
    @Transactional
    public CategoryResponse save(CategoryRequest dto) {
        if (repository.existsByName(dto.name()))
            throw new IllegalStateException("El nombre de categoría ya existe: " + dto.name());
        CategoryEntity saved = repository.save(CategoryEntity.builder()
                .name(dto.name()).description(dto.description()).active(true).build());
        return CategoryResponse.from(saved, 0);
    }

    @Override
    @Transactional
    public void update(Long id, CategoryRequest dto) {
        CategoryEntity category = findEntityById(id);
        if (repository.existsByNameAndIdNot(dto.name(), id))
            throw new IllegalStateException("El nombre de categoría ya existe: " + dto.name());
        category.setName(dto.name());
        category.setDescription(dto.description());
        repository.save(category);
    }

    @Override
    @Transactional
    public void activate(Long id) {
        CategoryEntity category = findEntityById(id);
        category.setActive(true);
        repository.save(category);
    }

    @Override
    @Transactional
    public void deactivate(Long id) {
        CategoryEntity category = findEntityById(id);
        category.setActive(false);
        repository.save(category);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        repository.delete(findEntityById(id));
    }
}
