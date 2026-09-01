package dev.leo.library.domain.exception;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(Long id) {
        super("Usuario no encontrado con id: " + id);
    }
    public UserNotFoundException(String email) {
        super("Usuario no encontrado con correo: " + email);
    }
}
