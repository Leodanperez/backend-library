package dev.leo.library.application.service;

import dev.leo.library.application.dto.request.AuthorRequest;
import dev.leo.library.application.dto.response.AuthorResponse;
import dev.leo.library.application.dto.response.SelectOptionsResponse.SelectItem;
import dev.leo.library.domain.exception.AuthorNotFoundException;
import dev.leo.library.infrastructure.adapter.output.persistence.entity.AuthorEntity;
import dev.leo.library.infrastructure.adapter.output.persistence.repository.AuthorJpaRepository;
import dev.leo.library.infrastructure.adapter.output.persistence.repository.BookJpaRepository;
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

    @Mock private AuthorJpaRepository repository;
    @Mock private BookJpaRepository bookRepository;
    @InjectMocks private AuthorService service;

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
        when(bookRepository.countActiveByAuthorId(1L)).thenReturn(3L);

        PaginatedResponse<AuthorResponse> result = service.findAll(null, null, null, 1, 10);

        assertThat(result.data()).hasSize(1);
        assertThat(result.data().get(0).bookCount()).isEqualTo(3);
    }

    @Test
    void findEntityById_returnsAuthor_whenExists() {
        when(repository.findById(1L)).thenReturn(Optional.of(author));

        AuthorEntity result = service.findEntityById(1L);

        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    void findById_returnsAuthorResponse_whenExists() {
        when(repository.findById(1L)).thenReturn(Optional.of(author));
        when(bookRepository.countActiveByAuthorId(1L)).thenReturn(0L);

        AuthorResponse result = service.findById(1L);

        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.firstName()).isEqualTo("Gabriel");
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

        AuthorResponse result = service.save(request);

        assertThat(result.firstName()).isEqualTo("Gabriel");
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

        AuthorResponse result = service.save(noEmail);

        assertThat(result.firstName()).isEqualTo("Ana");
        verify(repository, never()).existsByEmail(any());
    }

    @Test
    void update_updatesAuthor_whenFound() {
        when(repository.findById(1L)).thenReturn(Optional.of(author));
        when(repository.save(any(AuthorEntity.class))).thenReturn(author);

        service.update(1L, request);

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

        service.activate(1L);

        assertThat(author.isActive()).isTrue();
    }

    @Test
    void deactivate_setsActiveFalse() {
        when(repository.findById(1L)).thenReturn(Optional.of(author));
        when(repository.save(author)).thenReturn(author);

        service.deactivate(1L);

        assertThat(author.isActive()).isFalse();
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
