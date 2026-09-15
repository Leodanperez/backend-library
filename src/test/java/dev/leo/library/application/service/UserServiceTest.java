package dev.leo.library.application.service;

import dev.leo.library.application.dto.request.UserRequest;
import dev.leo.library.application.dto.request.UserUpdateRequest;
import dev.leo.library.application.dto.response.UserResponse;
import dev.leo.library.domain.exception.UserNotFoundException;
import dev.leo.library.domain.model.UserRole;
import dev.leo.library.infrastructure.adapter.output.persistence.entity.UserEntity;
import dev.leo.library.infrastructure.adapter.output.persistence.repository.UserJpaRepository;
import dev.leo.library.shared.dto.PaginatedResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock private UserJpaRepository repository;
    @Mock private PasswordEncoder passwordEncoder;
    @InjectMocks private UserService service;

    private UserEntity user;
    private UserRequest request;
    private UserUpdateRequest updateRequest;

    @BeforeEach
    void setUp() {
        user = UserEntity.builder()
                .id(1L).firstName("John").lastName("Doe")
                .email("john@example.com").password("encoded").role(UserRole.STUDENT).active(true).build();
        request = new UserRequest("John", "Doe", "john@example.com", "password123",
                "555-1234", "123 Main St", null, UserRole.STUDENT, null);
        updateRequest = new UserUpdateRequest("John", "Doe", "john@example.com",
                null, null, null, UserRole.STUDENT, null);
    }

    @Test
    void findAll_returnsPagedResponse() {
        when(repository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(user)));

        PaginatedResponse<UserResponse> result = service.findAll(null, null, null, 1, 10);

        assertThat(result.data()).hasSize(1);
        assertThat(result.total()).isEqualTo(1);
    }

    @Test
    void findEntityById_returnsUser_whenExists() {
        when(repository.findById(1L)).thenReturn(Optional.of(user));

        UserEntity result = service.findEntityById(1L);

        assertThat(result.getEmail()).isEqualTo("john@example.com");
    }

    @Test
    void findById_returnsUserResponse_whenExists() {
        when(repository.findById(1L)).thenReturn(Optional.of(user));

        UserResponse result = service.findById(1L);

        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.email()).isEqualTo("john@example.com");
    }

    @Test
    void findById_throwsUserNotFoundException_whenNotFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(99L))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void findByEmail_returnsUser_whenExists() {
        when(repository.findByEmail("john@example.com")).thenReturn(Optional.of(user));

        UserEntity result = service.findByEmail("john@example.com");

        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    void findByEmail_throwsUserNotFoundException_whenNotFound() {
        when(repository.findByEmail("missing@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findByEmail("missing@example.com"))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void save_createsUser_withEncodedPassword() {
        when(repository.existsByEmail(request.email())).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encoded");
        when(repository.save(any(UserEntity.class))).thenReturn(user);

        UserResponse result = service.save(request);

        assertThat(result.email()).isEqualTo("john@example.com");
        verify(passwordEncoder).encode("password123");
    }

    @Test
    void save_throwsIllegalStateException_whenEmailAlreadyExists() {
        when(repository.existsByEmail(request.email())).thenReturn(true);

        assertThatThrownBy(() -> service.save(request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("El correo electrónico ya está registrado");
    }

    @Test
    void save_defaultsRoleToStudent_whenRoleIsNull() {
        UserRequest noRole = new UserRequest("Ana", "Lopez", "ana@example.com", "pass123",
                null, null, null, null, null);
        UserEntity saved = UserEntity.builder().id(2L).email("ana@example.com").role(UserRole.STUDENT).active(true).build();
        when(repository.existsByEmail("ana@example.com")).thenReturn(false);
        when(passwordEncoder.encode("pass123")).thenReturn("encoded");
        when(repository.save(any(UserEntity.class))).thenReturn(saved);

        UserResponse result = service.save(noRole);

        assertThat(result.role()).isEqualTo(UserRole.STUDENT);
    }

    @Test
    void update_updatesUser_whenEmailNotTaken() {
        when(repository.findById(1L)).thenReturn(Optional.of(user));
        when(repository.save(any(UserEntity.class))).thenReturn(user);

        service.update(1L, updateRequest);

        verify(repository).save(user);
    }

    @Test
    void update_throwsIllegalStateException_whenNewEmailAlreadyTaken() {
        UserUpdateRequest newEmail = new UserUpdateRequest("John", "Doe", "taken@example.com",
                null, null, null, null, null);
        when(repository.findById(1L)).thenReturn(Optional.of(user));
        when(repository.existsByEmail("taken@example.com")).thenReturn(true);

        assertThatThrownBy(() -> service.update(1L, newEmail))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("El correo electrónico ya está registrado");
    }

    @Test
    void activate_setsActiveTrue() {
        user.setActive(false);
        when(repository.findById(1L)).thenReturn(Optional.of(user));
        when(repository.save(user)).thenReturn(user);

        service.activate(1L);

        assertThat(user.isActive()).isTrue();
    }

    @Test
    void deactivate_setsActiveFalse() {
        when(repository.findById(1L)).thenReturn(Optional.of(user));
        when(repository.save(user)).thenReturn(user);

        service.deactivate(1L);

        assertThat(user.isActive()).isFalse();
    }

    @Test
    void delete_callsRepositoryDelete_whenFound() {
        when(repository.findById(1L)).thenReturn(Optional.of(user));

        service.delete(1L);

        verify(repository).delete(user);
    }
}
