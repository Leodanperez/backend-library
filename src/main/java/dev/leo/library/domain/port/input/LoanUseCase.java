package dev.leo.library.domain.port.input;

import dev.leo.library.application.dto.request.LoanRequest;
import dev.leo.library.application.dto.request.LoanRequestDto;
import dev.leo.library.application.dto.response.ActiveLoanResponse;
import dev.leo.library.application.dto.response.LoanRequestSummaryResponse;
import dev.leo.library.application.dto.response.MyLoanResponse;
import dev.leo.library.application.dto.response.MyLoanSummaryResponse;
import dev.leo.library.infrastructure.adapter.output.persistence.entity.LoanEntity;
import dev.leo.library.shared.dto.PaginatedResponse;

import java.util.List;

public interface LoanUseCase {
    PaginatedResponse<LoanEntity> findAll(Long userId, Long bookCopyId, Long loanStatusId, int page, int perPage);
    LoanRequestSummaryResponse findRequests(String status, String q, int page, int perPage);
    PaginatedResponse<ActiveLoanResponse> findActiveLoans(String q, int page, int perPage);
    MyLoanSummaryResponse getSummary(Long userId);
    PaginatedResponse<MyLoanResponse> getHistory(Long userId, int page, int perPage);
    List<Long> findRequestedCopyIds(List<Long> copyIds);
    LoanEntity findById(Long id);
    LoanEntity requestLoan(LoanRequestDto dto, Long studentId);
    LoanEntity approveLoan(Long id, Long librarianId);
    LoanEntity save(LoanRequest dto);
    LoanEntity returnLoan(Long id, String observations);
    LoanEntity renewLoan(Long id, int days);
    LoanEntity cancelLoan(Long id);
    LoanEntity cancelLoanByStudent(Long id, Long studentId);
    LoanEntity update(Long id, LoanRequest dto);
    void delete(Long id);
}
