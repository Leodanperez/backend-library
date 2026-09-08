package dev.leo.library.application.dto.response;

import dev.leo.library.infrastructure.adapter.output.persistence.entity.LoanEntity;

import java.time.LocalDateTime;

public record ActiveLoanResponse(
        Long id,
        String bookTitle,
        String coverUrl,
        String bookCopyCode,
        String studentName,
        LocalDateTime loanDate,
        LocalDateTime dueDate,
        String status
) {
    public static ActiveLoanResponse from(LoanEntity loan) {
        return new ActiveLoanResponse(
                loan.getId(),
                loan.getBookCopy().getBook().getTitle(),
                loan.getBookCopy().getBook().getCoverUrl(),
                loan.getBookCopy().getCode(),
                loan.getUser().getFirstName() + " " + loan.getUser().getLastName(),
                loan.getLoanDate(),
                loan.getDueDate(),
                loan.getLoanStatus().getName()
        );
    }
}
