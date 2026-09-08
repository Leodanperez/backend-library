package dev.leo.library.application.dto.request;

import jakarta.validation.constraints.*;

public record LocationRequest(
        @NotBlank(message = "El nombre de la ubicación es obligatorio")
        @Size(max = 150, message = "El nombre no puede superar los 150 caracteres")
        String name,

        @Min(value = 0, message = "El piso no puede ser negativo")
        Integer floor,

        @Min(value = 1, message = "La capacidad debe ser al menos 1")
        Integer capacity,

        String description
) {}
