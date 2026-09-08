package dev.leo.library.application.dto.response;

import dev.leo.library.infrastructure.adapter.output.persistence.entity.AuthorEntity;

public record AuthorSelectResponse(Long id, String firstName, String lastName) {
    public static AuthorSelectResponse from(AuthorEntity author) {
        return new AuthorSelectResponse(author.getId(), author.getFirstName(), author.getLastName());
    }
}
