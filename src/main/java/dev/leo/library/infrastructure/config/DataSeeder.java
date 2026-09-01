package dev.leo.library.infrastructure.config;

import dev.leo.library.domain.model.UserRole;
import dev.leo.library.infrastructure.adapter.output.persistence.entity.LoanStatusEntity;
import dev.leo.library.infrastructure.adapter.output.persistence.entity.UserEntity;
import dev.leo.library.infrastructure.adapter.output.persistence.repository.LoanStatusJpaRepository;
import dev.leo.library.infrastructure.adapter.output.persistence.repository.UserJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements ApplicationRunner {

    private final UserJpaRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final LoanStatusJpaRepository loanStatusRepository;

    @Override
    public void run(ApplicationArguments args) {
        seedLoanStatuses();
        seedUsers();
    }

    private void seedLoanStatuses() {
        List.of(
                new SeedStatus("REQUESTED", "Solicitud de préstamo pendiente de aprobación"),
                new SeedStatus("PENDING",   "Préstamo aprobado y activo"),
                new SeedStatus("RETURNED",  "Libro devuelto"),
                new SeedStatus("OVERDUE",   "Préstamo vencido"),
                new SeedStatus("CANCELLED", "Préstamo cancelado")
        ).forEach(seed -> {
            if (loanStatusRepository.findByName(seed.name()).isEmpty()) {
                loanStatusRepository.save(LoanStatusEntity.builder()
                        .name(seed.name()).description(seed.description()).active(true).build());
                log.info("Seeded loan status: {}", seed.name());
            }
        });
    }

    private void seedUsers() {
        List.of(
                new SeedUser("Admin",     "Library", "admin@library.com",     "admin123",     UserRole.ADMIN),
                new SeedUser("Librarian", "Library", "librarian@library.com", "librarian123", UserRole.LIBRARIAN),
                new SeedUser("Student",   "Library", "student@library.com",   "student123",   UserRole.STUDENT)
        ).forEach(seed -> {
            if (!userRepository.existsByEmail(seed.email())) {
                userRepository.save(UserEntity.builder()
                        .firstName(seed.firstName()).lastName(seed.lastName()).email(seed.email())
                        .password(passwordEncoder.encode(seed.password()))
                        .role(seed.role()).active(true).build());
                log.info("Seeded user: {} [{}]", seed.email(), seed.role());
            }
        });
    }

    private record SeedStatus(String name, String description) {}
    private record SeedUser(String firstName, String lastName, String email, String password, UserRole role) {}
}
