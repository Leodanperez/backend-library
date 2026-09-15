package dev.leo.library.application.dto.response;

import dev.leo.library.infrastructure.adapter.output.persistence.entity.AuthorEntity;
import java.time.Instant;
import java.time.LocalDate;

public record AuthorResponse(
        Long id,
        String firstName,
        String lastName,
        String pseudonym,
        LocalDate birthDate,
        String nationality,
        String biography,
        String email,
        boolean active,
        long bookCount,
        Instant createdAt,
        Instant updatedAt
) {
    public static AuthorResponse from(AuthorEntity a, long bookCount) {
        return new AuthorResponse(a.getId(), a.getFirstName(), a.getLastName(), a.getPseudonym(),
                a.getBirthDate(), a.getNationality(), a.getBiography(), a.getEmail(),
                a.isActive(), bookCount, a.getCreatedAt(), a.getUpdatedAt());
    }
}
