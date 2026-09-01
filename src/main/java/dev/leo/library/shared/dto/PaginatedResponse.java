package dev.leo.library.shared.dto;

import lombok.Builder;
import java.util.List;

@Builder
public record PaginatedResponse<T>(
        List<T> data,
        int page,
        int perPage,
        long total,
        int lastPage
) {
    public static <T> PaginatedResponse<T> of(List<T> data, int page, int perPage, long total) {
        int lastPage = Math.max(1, (int) Math.ceil((double) total / perPage));
        return PaginatedResponse.<T>builder()
                .data(data).page(page).perPage(perPage).total(total).lastPage(lastPage)
                .build();
    }
}
