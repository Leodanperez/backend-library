package dev.leo.library.domain.port.input;

import dev.leo.library.application.dto.request.CategoryRequest;
import dev.leo.library.application.dto.response.CategorySelectResponse;
import dev.leo.library.infrastructure.adapter.output.persistence.entity.CategoryEntity;
import dev.leo.library.shared.dto.PaginatedResponse;
import java.util.List;

public interface CategoryUseCase {
    PaginatedResponse<CategoryEntity> findAll(String q, Boolean active, int page, int perPage);
    List<CategorySelectResponse> findAllActive();
    CategoryEntity findById(Long id);
    CategoryEntity save(CategoryRequest dto);
    CategoryEntity update(Long id, CategoryRequest dto);
    CategoryEntity activate(Long id);
    CategoryEntity deactivate(Long id);
    void delete(Long id);
}
