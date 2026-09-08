package dev.leo.library.application.dto.response;

import dev.leo.library.infrastructure.adapter.output.persistence.entity.LoanEntity;

import java.time.LocalDateTime;

public record LoanRequestItemResponse(
        Long id,
        String status,
        // Libro
        String bookTitle,
        String authorFullName,
        String coverUrl,
        int availableCopies,
        // Ejemplar sugerido
        String bookCopyCode,
        String bookCopyLocation,
        // Estudiante
        String studentName,
        String studentEmail,
        long studentActiveLoans,
        long studentOverdueLoans,
        // Fechas y notas
        LocalDateTime loanDate,
        LocalDateTime dueDate,
        String observations
) {
    public static LoanRequestItemResponse from(LoanEntity loan, int availableCopies,
                                               long studentActiveLoans, long studentOverdueLoans) {
        return new LoanRequestItemResponse(
                loan.getId(),
                loan.getLoanStatus().getName(),
                loan.getBookCopy().getBook().getTitle(),
                loan.getBookCopy().getBook().getAuthor().getFirstName() + " "
                        + loan.getBookCopy().getBook().getAuthor().getLastName(),
                loan.getBookCopy().getBook().getCoverUrl(),
                availableCopies,
                loan.getBookCopy().getCode(),
                loan.getBookCopy().getLocation(),
                loan.getUser().getFirstName() + " " + loan.getUser().getLastName(),
                loan.getUser().getEmail(),
                studentActiveLoans,
                studentOverdueLoans,
                loan.getLoanDate(),
                loan.getDueDate(),
                loan.getObservations()
        );
    }
}
