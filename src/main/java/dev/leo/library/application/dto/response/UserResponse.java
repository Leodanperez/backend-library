package dev.leo.library.application.dto.response;

import dev.leo.library.domain.model.UserRole;
import dev.leo.library.infrastructure.adapter.output.persistence.entity.UserEntity;
import java.time.Instant;
import java.time.LocalDate;

public record UserResponse(
        Long id,
        String firstName,
        String lastName,
        String email,
        String phone,
        String address,
        LocalDate birthDate,
        UserRole role,
        boolean active,
        Instant registrationDate
) {
    public static UserResponse from(UserEntity user) {
        return new UserResponse(
                user.getId(), user.getFirstName(), user.getLastName(),
                user.getEmail(), user.getPhone(), user.getAddress(),
                user.getBirthDate(), user.getRole(), user.isActive(),
                user.getRegistrationDate()
        );
    }
}
