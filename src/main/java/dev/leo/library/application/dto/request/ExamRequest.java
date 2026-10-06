package dev.leo.library.application.dto.request;

import jakarta.validation.constraints.*;
import java.time.LocalDate;

public record ExamRequest(
        @NotBlank(message = "La materia es obligatoria")
        @Size(max = 100, message = "La materia no puede superar los 100 caracteres")
        String subject,

        @NotNull(message = "La fecha es obligatoria")
        LocalDate date,

        @NotBlank(message = "El aula es obligatoria")
        @Size(max = 50, message = "El aula no puede superar los 50 caracteres")
        String classroom,

        @NotNull(message = "La duración es obligatoria")
        @Min(value = 1, message = "La duración debe ser al menos 1 minuto")
        Integer durationMinutes
) {}
