package dev.leo.library.application.dto.response;

import java.util.List;

public record MyLoanSummaryResponse(
        long pendingRequests,
        long activeLoans,
        long renewalsAvailable,
        long booksReadThisYear,
        List<MyLoanResponse> requests,
        List<MyLoanResponse> active
) {}
