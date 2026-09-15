package dev.leo.library.application.dto.response;

import dev.leo.library.application.dto.response.BookCopyResponse;
import dev.leo.library.application.dto.response.BookResponse;
import dev.leo.library.domain.model.CopyCondition;
import dev.leo.library.domain.model.CopyStatus;

import java.util.List;
import java.util.Set;

public record BookDetailResponse(
        Long id,
        String title,
        String isbn,
        String description,
        Integer publicationYear,
        Integer pages,
        String language,
        String publisher,
        String coverUrl,
        String authorFullName,
        String category,
        int availableCopies,
        boolean available,
        List<CopyInfo> copies
) {
    public record CopyInfo(Long id, String code, CopyStatus status, CopyCondition condition, String location) {
        public static CopyInfo from(BookCopyResponse copy) {
            return new CopyInfo(copy.id(), copy.code(), copy.status(), copy.condition(), copy.location());
        }
    }

    public static BookDetailResponse from(BookResponse book, List<BookCopyResponse> copies, Set<Long> requestedCopyIds) {
        List<CopyInfo> copyInfos = copies.stream().map(CopyInfo::from).toList();
        long available = copies.stream()
                .filter(c -> c.status() == CopyStatus.AVAILABLE && !requestedCopyIds.contains(c.id()))
                .count();
        return new BookDetailResponse(
                book.id(), book.title(), book.isbn(), book.description(),
                book.publicationYear(), book.pages(), book.language(),
                book.publisher(), book.coverUrl(),
                book.authorName(),
                book.categoryName(),
                (int) available,
                available > 0,
                copyInfos
        );
    }
}
