package dev.leo.library.application.dto.response;

import dev.leo.library.infrastructure.adapter.output.persistence.entity.LoanStatusEntity;

public record LoanStatusResponse(Long id, String name, String description, boolean active) {
    public static LoanStatusResponse from(LoanStatusEntity e) {
        return new LoanStatusResponse(e.getId(), e.getName(), e.getDescription(), e.isActive());
    }
}
