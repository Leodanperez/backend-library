package dev.leo.library.domain.exception;

public class LocationNotFoundException extends RuntimeException {
    public LocationNotFoundException(Long id) {
        super("Ubicación no encontrada con id: " + id);
    }
}
