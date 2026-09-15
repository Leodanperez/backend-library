package dev.leo.library.domain.port.input;

import dev.leo.library.application.dto.request.LoanRequest;
import dev.leo.library.application.dto.request.LoanRequestDto;
import dev.leo.library.application.dto.response.ActiveLoanResponse;
import dev.leo.library.application.dto.response.LoanRequestSummaryResponse;
import dev.leo.library.application.dto.response.LoanSummaryResponse;
import dev.leo.library.application.dto.response.MyLoanResponse;
import dev.leo.library.application.dto.response.MyLoanSummaryResponse;
import dev.leo.library.infrastructure.adapter.output.persistence.entity.LoanEntity;
import dev.leo.library.shared.dto.PaginatedResponse;
import java.util.List;

public interface LoanUseCase {
    PaginatedResponse<LoanSummaryResponse> findAll(Long userId, Long bookCopyId, String status, String q, int page, int perPage);
    LoanRequestSummaryResponse findRequests(String status, String q, int page, int perPage);
    PaginatedResponse<ActiveLoanResponse> findActiveLoans(String q, int page, int perPage);
    MyLoanSummaryResponse getSummary(Long userId);
    PaginatedResponse<MyLoanResponse> getMyLoans(Long userId, int page, int perPage);
    PaginatedResponse<MyLoanResponse> getHistory(Long userId, int page, int perPage);
    List<Long> findRequestedCopyIds(List<Long> copyIds);
    LoanSummaryResponse findById(Long id);
    void requestLoan(LoanRequestDto dto, Long studentId);
    void requestLoanFromCatalog(Long bookId, LoanRequestDto dto, Long studentId);
    void approveLoan(Long id, Long librarianId);
    void save(LoanRequest dto);
    void returnLoan(Long id, String observations);
    void renewLoan(Long id, int days);
    void cancelLoan(Long id);
    void cancelLoanByStudent(Long id, Long studentId);
    void update(Long id, LoanRequest dto);
    void delete(Long id);
    LoanEntity findEntityById(Long id);
}
