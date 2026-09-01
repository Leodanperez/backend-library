package dev.leo.library.domain.exception;

public class LoanStatusNotFoundException extends RuntimeException {
    public LoanStatusNotFoundException(String name) {
        super("Estado de préstamo no encontrado: " + name);
    }
}
