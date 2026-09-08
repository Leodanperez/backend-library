package dev.leo.library.application.dto.response;

import dev.leo.library.infrastructure.adapter.output.persistence.entity.LoanEntity;

import java.time.LocalDateTime;

public record PendingLoanResponse(
        Long id,
        String studentName,
        String studentEmail,
        String bookTitle,
        String bookCopyCode,
        LocalDateTime loanDate,
        LocalDateTime dueDate,
        String observations
) {
    public static PendingLoanResponse from(LoanEntity loan) {
        return new PendingLoanResponse(
                loan.getId(),
                loan.getUser().getFirstName() + " " + loan.getUser().getLastName(),
                loan.getUser().getEmail(),
                loan.getBookCopy().getBook().getTitle(),
                loan.getBookCopy().getCode(),
                loan.getLoanDate(),
                loan.getDueDate(),
                loan.getObservations()
        );
    }
}
