package dev.leo.library.application.dto.request;

import dev.leo.library.domain.model.UserRole;
import jakarta.validation.constraints.*;
import java.time.LocalDate;

public record UserRequest(
        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 100, message = "El nombre no puede superar los 100 caracteres")
        String firstName,

        @NotBlank(message = "El apellido es obligatorio")
        @Size(max = 100, message = "El apellido no puede superar los 100 caracteres")
        String lastName,

        @NotBlank(message = "El correo electrónico es obligatorio")
        @Email(message = "El correo electrónico no tiene un formato válido")
        @Size(max = 150, message = "El correo electrónico no puede superar los 150 caracteres")
        String email,

        @NotBlank(message = "La contraseña es obligatoria")
        @Size(min = 6, max = 255, message = "La contraseña debe tener entre 6 y 255 caracteres")
        String password,

        @Pattern(regexp = "^[+]?[0-9\\s\\-().]{7,20}$", message = "El teléfono no tiene un formato válido")
        @Size(max = 20, message = "El teléfono no puede superar los 20 caracteres")
        String phone,

        @Size(max = 250, message = "La dirección no puede superar los 250 caracteres")
        String address,

        @Past(message = "La fecha de nacimiento debe ser una fecha pasada")
        LocalDate birthDate,

        UserRole role,
        Boolean active
) {}
