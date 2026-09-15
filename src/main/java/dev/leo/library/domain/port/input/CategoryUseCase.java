package dev.leo.library.domain.port.input;

import dev.leo.library.application.dto.request.CategoryRequest;
import dev.leo.library.application.dto.response.CategoryResponse;
import dev.leo.library.application.dto.response.SelectOptionsResponse.SelectItem;
import dev.leo.library.infrastructure.adapter.output.persistence.entity.CategoryEntity;
import dev.leo.library.shared.dto.PaginatedResponse;
import java.util.List;

public interface CategoryUseCase {
    PaginatedResponse<CategoryResponse> findAll(String q, Boolean active, int page, int perPage);
    List<SelectItem> findAllActive();
    CategoryResponse findById(Long id);
    CategoryResponse save(CategoryRequest dto);
    void update(Long id, CategoryRequest dto);
    void activate(Long id);
    void deactivate(Long id);
    void delete(Long id);
    CategoryEntity findEntityById(Long id);
}
