package dev.leo.library.application.service;

import dev.leo.library.application.dto.request.AuthorRequest;
import dev.leo.library.domain.exception.AuthorNotFoundException;
import dev.leo.library.infrastructure.adapter.output.persistence.entity.AuthorEntity;
import dev.leo.library.infrastructure.adapter.output.persistence.repository.AuthorJpaRepository;
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

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthorServiceTest {

    @Mock
    private AuthorJpaRepository repository;

    @InjectMocks
    private AuthorService service;

    private AuthorEntity author;
    private AuthorRequest request;

    @BeforeEach
    void setUp() {
        author = AuthorEntity.builder()
                .id(1L).firstName("Gabriel").lastName("García Márquez")
                .nationality("Colombian").email("gabriel@example.com").active(true).build();

        request = new AuthorRequest("Gabriel", "García Márquez", null,
                LocalDate.of(1927, 3, 6), "Colombian", "Nobel Prize author", "gabriel@example.com");
    }

    @Test
    void findAll_returnsPagedResponse() {
        when(repository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(author)));

        PaginatedResponse<AuthorEntity> result = service.findAll(null, null, null, 1, 10);

        assertThat(result.data()).hasSize(1);
        assertThat(result.total()).isEqualTo(1);
    }

    @Test
    void findById_returnsAuthor_whenExists() {
        when(repository.findById(1L)).thenReturn(Optional.of(author));

        AuthorEntity result = service.findById(1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getFirstName()).isEqualTo("Gabriel");
    }

    @Test
    void findById_throwsAuthorNotFoundException_whenNotFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(99L))
                .isInstanceOf(AuthorNotFoundException.class);
    }

    @Test
    void save_createsAuthor_whenEmailNotDuplicated() {
        when(repository.existsByEmail(request.email())).thenReturn(false);
        when(repository.save(any(AuthorEntity.class))).thenReturn(author);

        AuthorEntity result = service.save(request);

        assertThat(result.getFirstName()).isEqualTo("Gabriel");
        verify(repository).save(any(AuthorEntity.class));
    }

    @Test
    void save_throwsIllegalStateException_whenEmailAlreadyExists() {
        when(repository.existsByEmail(request.email())).thenReturn(true);

        assertThatThrownBy(() -> service.save(request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("El correo electrónico ya está registrado");
    }

    @Test
    void save_skipsEmailCheck_whenEmailIsNull() {
        AuthorRequest noEmail = new AuthorRequest("Ana", "Rojas", null, null, "Mexican", null, null);
        AuthorEntity saved = AuthorEntity.builder().id(2L).firstName("Ana").lastName("Rojas").active(true).build();
        when(repository.save(any(AuthorEntity.class))).thenReturn(saved);

        AuthorEntity result = service.save(noEmail);

        assertThat(result.getFirstName()).isEqualTo("Ana");
        verify(repository, never()).existsByEmail(any());
    }

    @Test
    void update_updatesAuthor_whenFound() {
        when(repository.findById(1L)).thenReturn(Optional.of(author));
        when(repository.save(any(AuthorEntity.class))).thenReturn(author);

        AuthorEntity result = service.update(1L, request);

        assertThat(result).isNotNull();
        verify(repository).save(author);
    }

    @Test
    void update_throwsIllegalStateException_whenNewEmailAlreadyTaken() {
        AuthorRequest newEmail = new AuthorRequest("Gabriel", "García Márquez", null,
                null, "Colombian", null, "other@example.com");
        when(repository.findById(1L)).thenReturn(Optional.of(author));
        when(repository.existsByEmail("other@example.com")).thenReturn(true);

        assertThatThrownBy(() -> service.update(1L, newEmail))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("El correo electrónico ya está registrado");
    }

    @Test
    void activate_setsActiveTrue() {
        author.setActive(false);
        when(repository.findById(1L)).thenReturn(Optional.of(author));
        when(repository.save(author)).thenReturn(author);

        AuthorEntity result = service.activate(1L);

        assertThat(result.isActive()).isTrue();
    }

    @Test
    void deactivate_setsActiveFalse() {
        when(repository.findById(1L)).thenReturn(Optional.of(author));
        when(repository.save(author)).thenReturn(author);

        AuthorEntity result = service.deactivate(1L);

        assertThat(result.isActive()).isFalse();
    }

    @Test
    void delete_callsRepositoryDelete_whenFound() {
        when(repository.findById(1L)).thenReturn(Optional.of(author));

        service.delete(1L);

        verify(repository).delete(author);
    }

    @Test
    void delete_throwsAuthorNotFoundException_whenNotFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.delete(99L))
                .isInstanceOf(AuthorNotFoundException.class);
    }
}
