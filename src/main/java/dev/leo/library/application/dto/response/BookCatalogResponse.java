package dev.leo.library.application.dto.response;

import dev.leo.library.application.dto.response.BookResponse;

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
        String category,
        boolean available
) {
    public static BookCatalogResponse from(BookResponse book, boolean available) {
        return new BookCatalogResponse(
                book.id(), book.title(), book.isbn(), book.description(),
                book.publicationYear(), book.pages(), book.language(),
                book.publisher(), book.coverUrl(),
                book.authorName(),
                book.categoryName(),
                available
        );
    }
}
