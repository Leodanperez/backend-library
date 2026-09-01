package dev.leo.library.domain.port.input;

import dev.leo.library.application.dto.request.LoanRequest;
import dev.leo.library.application.dto.request.LoanRequestDto;
import dev.leo.library.infrastructure.adapter.output.persistence.entity.LoanEntity;
import dev.leo.library.shared.dto.PaginatedResponse;

public interface LoanUseCase {
    PaginatedResponse<LoanEntity> findAll(Long userId, Long bookCopyId, Long loanStatusId, int page, int perPage);
    LoanEntity findById(Long id);
    LoanEntity requestLoan(LoanRequestDto dto, Long studentId);
    LoanEntity approveLoan(Long id, Long librarianId);
    LoanEntity save(LoanRequest dto);
    LoanEntity returnLoan(Long id);
    LoanEntity renewLoan(Long id, int days);
    LoanEntity cancelLoan(Long id);
    LoanEntity cancelLoanByStudent(Long id, Long studentId);
    LoanEntity update(Long id, LoanRequest dto);
    void delete(Long id);
}
