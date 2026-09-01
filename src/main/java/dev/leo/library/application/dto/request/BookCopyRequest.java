package dev.leo.library.application.dto.request;

import dev.leo.library.domain.model.CopyCondition;
import dev.leo.library.domain.model.CopyStatus;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.Instant;

public record BookCopyRequest(
        @NotNull(message = "El libro es obligatorio")
        Long bookId,

        @NotBlank(message = "El código del ejemplar es obligatorio")
        @Size(max = 50, message = "El código no puede superar los 50 caracteres")
        String code,

        CopyStatus status,

        @NotNull(message = "La condición del ejemplar es obligatoria")
        CopyCondition condition,

        @PastOrPresent(message = "La fecha de adquisición no puede ser una fecha futura")
        Instant acquisitionDate,

        @DecimalMin(value = "0.0", inclusive = false, message = "El precio debe ser mayor a cero")
        @Digits(integer = 8, fraction = 2, message = "El precio no tiene un formato válido (máximo 8 enteros y 2 decimales)")
        BigDecimal price,

        @Size(max = 100, message = "La ubicación no puede superar los 100 caracteres")
        String location
) {}
