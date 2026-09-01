package dev.leo.library.domain.exception;

public class AuthorNotFoundException extends RuntimeException {
    public AuthorNotFoundException(Long id) {
        super("Autor no encontrado con id: " + id);
    }
}
