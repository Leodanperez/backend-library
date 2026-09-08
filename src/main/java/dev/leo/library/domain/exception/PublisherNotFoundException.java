package dev.leo.library.domain.exception;

public class PublisherNotFoundException extends RuntimeException {
    public PublisherNotFoundException(Long id) {
        super("Editorial no encontrada con id: " + id);
    }
}
