package dev.leo.library.domain.exception;

public class CategoryNotFoundException extends RuntimeException {
    public CategoryNotFoundException(Long id) {
        super("Categoría no encontrada con id: " + id);
    }
}
