package dev.leo.library.application.dto.response;

import dev.leo.library.domain.model.CopyCondition;
import dev.leo.library.domain.model.CopyStatus;
import dev.leo.library.infrastructure.adapter.output.persistence.entity.BookCopyEntity;
import dev.leo.library.infrastructure.adapter.output.persistence.entity.BookEntity;

import java.util.List;

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
        List<CopyInfo> copies
) {
    public record CopyInfo(Long id, String code, CopyStatus status, CopyCondition condition, String location) {
        public static CopyInfo from(BookCopyEntity copy) {
            return new CopyInfo(copy.getId(), copy.getCode(), copy.getStatus(), copy.getCondition(), copy.getLocation());
        }
    }

    public static BookDetailResponse from(BookEntity book, List<BookCopyEntity> copies) {
        List<CopyInfo> copyInfos = copies.stream().map(CopyInfo::from).toList();
        long available = copies.stream().filter(c -> c.getStatus() == CopyStatus.AVAILABLE).count();
        return new BookDetailResponse(
                book.getId(), book.getTitle(), book.getIsbn(), book.getDescription(),
                book.getPublicationYear(), book.getPages(), book.getLanguage(),
                book.getPublisher(), book.getCoverUrl(),
                book.getAuthor().getFirstName() + " " + book.getAuthor().getLastName(),
                book.getCategory().getName(),
                (int) available,
                copyInfos
        );
    }
}
