package dev.leo.library.infrastructure.adapter.output.persistence.adapter;

import dev.leo.library.domain.port.output.CategoryRepositoryPort;
import dev.leo.library.infrastructure.adapter.output.persistence.entity.CategoryEntity;
import dev.leo.library.infrastructure.adapter.output.persistence.repository.BookJpaRepository;
import dev.leo.library.infrastructure.adapter.output.persistence.repository.CategoryJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CategoryPersistenceAdapter implements CategoryRepositoryPort {

    private final CategoryJpaRepository categoryRepository;
    private final BookJpaRepository bookRepository;

    @Override
    public Optional<CategoryEntity> findById(Long id) {
        return categoryRepository.findById(id);
    }

    @Override
    public List<CategoryEntity> findActiveOrderByName() {
        return categoryRepository.findByActiveTrueOrderByNameAsc();
    }

    @Override
    public Page<CategoryEntity> search(String q, Boolean active, Pageable pageable) {
        return categoryRepository.findAll(CategorySpec.filter(q, active), pageable);
    }

    @Override
    public boolean existsByName(String name) {
        return categoryRepository.existsByName(name);
    }

    @Override
    public boolean existsByNameAndIdNot(String name, Long id) {
        return categoryRepository.existsByNameAndIdNot(name, id);
    }

    @Override
    public CategoryEntity save(CategoryEntity category) {
        return categoryRepository.save(category);
    }

    @Override
    public void delete(CategoryEntity category) {
        categoryRepository.delete(category);
    }

    @Override
    public long countActiveByCategoryId(Long categoryId) {
        return bookRepository.countActiveByCategoryId(categoryId);
    }
}
