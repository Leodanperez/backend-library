package dev.leo.library.application.dto.response;

import dev.leo.library.shared.dto.PaginatedResponse;

public record LoanRequestSummaryResponse(
        long totalPending,
        long approvedToday,
        long rejectedToday,
        PaginatedResponse<LoanRequestItemResponse> requests
) {}
