package dev.leo.library.domain.port.output;

import dev.leo.library.infrastructure.adapter.output.persistence.entity.CategoryEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;

public interface CategoryRepositoryPort {
    Optional<CategoryEntity> findById(Long id);
    List<CategoryEntity> findActiveOrderByName();
    Page<CategoryEntity> search(String q, Boolean active, Pageable pageable);
    boolean existsByName(String name);
    boolean existsByNameAndIdNot(String name, Long id);
    CategoryEntity save(CategoryEntity category);
    void delete(CategoryEntity category);
    long countActiveByCategoryId(Long categoryId);
}
