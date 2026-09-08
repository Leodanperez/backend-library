package dev.leo.library.application.dto.request;

import jakarta.validation.constraints.*;

public record PublisherRequest(
        @NotBlank(message = "El nombre de la editorial es obligatorio")
        @Size(max = 150, message = "El nombre no puede superar los 150 caracteres")
        String name,

        @Size(max = 100, message = "El país no puede superar los 100 caracteres")
        String country,

        @Min(value = 1400, message = "El año de fundación no puede ser anterior a 1400")
        @Max(value = 2100, message = "El año de fundación no es válido")
        Integer foundedYear,

        @Size(max = 255, message = "El sitio web no puede superar los 255 caracteres")
        String website
) {}
