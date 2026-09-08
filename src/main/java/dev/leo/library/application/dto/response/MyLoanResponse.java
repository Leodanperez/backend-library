package dev.leo.library.application.dto.response;

import dev.leo.library.infrastructure.adapter.output.persistence.entity.LoanEntity;

import java.time.LocalDateTime;

public record MyLoanResponse(
        Long id,
        String bookTitle,
        String bookCopyCode,
        String coverUrl,
        String status,
        LocalDateTime loanDate,
        LocalDateTime dueDate,
        LocalDateTime returnDate,
        Integer renewalCount,
        String observations
) {
    public static MyLoanResponse from(LoanEntity loan) {
        return new MyLoanResponse(
                loan.getId(),
                loan.getBookCopy().getBook().getTitle(),
                loan.getBookCopy().getCode(),
                loan.getBookCopy().getBook().getCoverUrl(),
                loan.getLoanStatus().getName(),
                loan.getLoanDate(),
                loan.getDueDate(),
                loan.getReturnDate(),
                loan.getRenewalCount(),
                loan.getObservations()
        );
    }
}
