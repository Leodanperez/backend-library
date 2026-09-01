package dev.leo.library.application.dto.response;

import dev.leo.library.infrastructure.adapter.output.persistence.entity.BookEntity;

public record BookCatalogResponse(
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
        String category
) {
    public static BookCatalogResponse from(BookEntity book) {
        return new BookCatalogResponse(
                book.getId(),
                book.getTitle(),
                book.getIsbn(),
                book.getDescription(),
                book.getPublicationYear(),
                book.getPages(),
                book.getLanguage(),
                book.getPublisher(),
                book.getCoverUrl(),
                book.getAuthor().getFirstName() + " " + book.getAuthor().getLastName(),
                book.getCategory().getName()
        );
    }
}
