package dev.leo.library.domain.exception;

public class LoanNotFoundException extends RuntimeException {
    public LoanNotFoundException(Long id) {
        super("Préstamo no encontrado con id: " + id);
    }
}
