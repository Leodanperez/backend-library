package dev.leo.library.application.dto.response;

import dev.leo.library.infrastructure.adapter.output.persistence.entity.BookEntity;
import java.time.Instant;

public record BookResponse(
        Long id,
        String title,
        String isbn,
        String description,
        Integer publicationYear,
        Integer pages,
        String language,
        String publisher,
        String coverUrl,
        Long authorId,
        String authorName,
        Long categoryId,
        String categoryName,
        boolean active,
        Instant createdAt,
        Instant updatedAt
) {
    public static BookResponse from(BookEntity b) {
        return new BookResponse(
                b.getId(), b.getTitle(), b.getIsbn(), b.getDescription(),
                b.getPublicationYear(), b.getPages(), b.getLanguage(),
                b.getPublisher(), b.getCoverUrl(),
                b.getAuthor().getId(),
                b.getAuthor().getFirstName() + " " + b.getAuthor().getLastName(),
                b.getCategory().getId(),
                b.getCategory().getName(),
                b.isActive(), b.getCreatedAt(), b.getUpdatedAt()
        );
    }
}
