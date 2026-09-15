package dev.leo.library.application.dto.response;

import dev.leo.library.infrastructure.adapter.output.persistence.entity.LoanEntity;

import java.time.LocalDateTime;

public record LoanSummaryResponse(
        Long id,
        String status,
        String bookTitle,
        String coverUrl,
        String bookCopyCode,
        String studentName,
        String studentEmail,
        LocalDateTime loanDate,
        LocalDateTime dueDate,
        LocalDateTime returnDate,
        int renewalCount,
        String observations
) {
    public static LoanSummaryResponse from(LoanEntity loan) {
        return new LoanSummaryResponse(
                loan.getId(),
                loan.getLoanStatus().getName(),
                loan.getBookCopy().getBook().getTitle(),
                loan.getBookCopy().getBook().getCoverUrl(),
                loan.getBookCopy().getCode(),
                loan.getUser().getFirstName() + " " + loan.getUser().getLastName(),
                loan.getUser().getEmail(),
                loan.getLoanDate(),
                loan.getDueDate(),
                loan.getReturnDate(),
                loan.getRenewalCount(),
                loan.getObservations()
        );
    }
}
