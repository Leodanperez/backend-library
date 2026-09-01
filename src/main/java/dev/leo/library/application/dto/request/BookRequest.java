package dev.leo.library.application.dto.request;

import jakarta.validation.constraints.*;

public record BookRequest(
        @NotBlank(message = "El título es obligatorio")
        @Size(max = 200, message = "El título no puede superar los 200 caracteres")
        String title,

        @Pattern(
            regexp = "^(?:97[89]\\d{10}|\\d{9}[\\dX])$",
            message = "El ISBN no tiene un formato válido (ISBN-10 o ISBN-13)"
        )
        @Size(max = 20, message = "El ISBN no puede superar los 20 caracteres")
        String isbn,

        @Size(max = 2000, message = "La descripción no puede superar los 2000 caracteres")
        String description,

        @Min(value = 1000, message = "El año de publicación no puede ser anterior al año 1000")
        @Max(value = 9999, message = "El año de publicación no es válido")
        Integer publicationYear,

        @Positive(message = "El número de páginas debe ser mayor a cero")
        Integer pages,

        @Size(max = 50, message = "El idioma no puede superar los 50 caracteres")
        String language,

        @Size(max = 150, message = "La editorial no puede superar los 150 caracteres")
        String publisher,

        @Size(max = 500, message = "La URL de portada no puede superar los 500 caracteres")
        String coverUrl,

        @NotNull(message = "El autor es obligatorio")
        Long authorId,

        @NotNull(message = "La categoría es obligatoria")
        Long categoryId
) {}
