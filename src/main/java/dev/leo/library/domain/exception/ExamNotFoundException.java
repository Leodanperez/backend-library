package dev.leo.library.domain.exception;

public class ExamNotFoundException extends RuntimeException {
    public ExamNotFoundException(Long id) {
        super("Examen no encontrado con id: " + id);
    }
}
