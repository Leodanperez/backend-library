package dev.leo.library.application.dto.response;

import dev.leo.library.infrastructure.adapter.output.persistence.entity.CategoryEntity;

public record CategorySelectResponse(Long id, String name) {
    public static CategorySelectResponse from(CategoryEntity category) {
        return new CategorySelectResponse(category.getId(), category.getName());
    }
}
