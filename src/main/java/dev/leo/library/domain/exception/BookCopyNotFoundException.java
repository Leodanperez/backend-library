package dev.leo.library.domain.exception;

public class BookCopyNotFoundException extends RuntimeException {
    public BookCopyNotFoundException(Long id) {
        super("Ejemplar no encontrado con id: " + id);
    }
}
