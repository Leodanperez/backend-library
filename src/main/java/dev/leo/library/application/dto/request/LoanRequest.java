package dev.leo.library.application.dto.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

public record LoanRequest(
        @NotNull(message = "El ejemplar del libro es obligatorio")
        Long bookCopyId,

        @NotNull(message = "El usuario es obligatorio")
        Long userId,

        LocalDateTime loanDate,

        @NotNull(message = "La fecha de vencimiento es obligatoria")
        @Future(message = "La fecha de vencimiento debe ser una fecha futura")
        LocalDateTime dueDate,

        LocalDateTime returnDate,
        Long loanStatusId,

        @Size(max = 500, message = "Las observaciones no pueden superar los 500 caracteres")
        String observations
) {}
