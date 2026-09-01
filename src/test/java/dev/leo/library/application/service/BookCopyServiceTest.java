package dev.leo.library.application.service;

import dev.leo.library.application.dto.request.BookCopyRequest;
import dev.leo.library.domain.exception.BookCopyNotFoundException;
import dev.leo.library.domain.model.CopyCondition;
import dev.leo.library.domain.model.CopyStatus;
import dev.leo.library.infrastructure.adapter.output.persistence.entity.BookCopyEntity;
import dev.leo.library.infrastructure.adapter.output.persistence.entity.BookEntity;
import dev.leo.library.infrastructure.adapter.output.persistence.repository.BookCopyJpaRepository;
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

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookCopyServiceTest {

    @Mock
    private BookCopyJpaRepository repository;

    @Mock
    private BookService bookService;

    @InjectMocks
    private BookCopyService service;

    private BookCopyEntity copy;
    private BookCopyRequest request;
    private BookEntity book;

    @BeforeEach
    void setUp() {
        book = BookEntity.builder().id(1L).title("Cien años de soledad").active(true).build();
        copy = BookCopyEntity.builder()
                .id(1L).book(book).code("COPY-001")
                .status(CopyStatus.AVAILABLE).condition(CopyCondition.GOOD).build();
        request = new BookCopyRequest(1L, "COPY-001", CopyStatus.AVAILABLE, CopyCondition.GOOD, null, null, "A1");
    }

    @Test
    void findAll_returnsPagedResponse() {
        when(repository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(copy)));

        PaginatedResponse<BookCopyEntity> result = service.findAll(null, null, null, null, 1, 10);

        assertThat(result.data()).hasSize(1);
        assertThat(result.total()).isEqualTo(1);
    }

    @Test
    void findById_returnsCopy_whenExists() {
        when(repository.findById(1L)).thenReturn(Optional.of(copy));

        BookCopyEntity result = service.findById(1L);

        assertThat(result.getCode()).isEqualTo("COPY-001");
    }

    @Test
    void findById_throwsBookCopyNotFoundException_whenNotFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(99L))
                .isInstanceOf(BookCopyNotFoundException.class);
    }

    @Test
    void save_createsCopy_whenCodeNotDuplicated() {
        when(repository.existsByCode("COPY-001")).thenReturn(false);
        when(bookService.findById(1L)).thenReturn(book);
        when(repository.save(any(BookCopyEntity.class))).thenReturn(copy);

        BookCopyEntity result = service.save(request);

        assertThat(result.getCode()).isEqualTo("COPY-001");
        verify(repository).save(any(BookCopyEntity.class));
    }

    @Test
    void save_throwsIllegalStateException_whenCodeAlreadyExists() {
        when(repository.existsByCode("COPY-001")).thenReturn(true);

        assertThatThrownBy(() -> service.save(request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("El código del ejemplar ya existe");
    }

    @Test
    void save_defaultsStatusToAvailable_whenStatusIsNull() {
        BookCopyRequest noStatus = new BookCopyRequest(1L, "COPY-002", null, CopyCondition.GOOD, null, null, null);
        when(repository.existsByCode("COPY-002")).thenReturn(false);
        when(bookService.findById(1L)).thenReturn(book);
        when(repository.save(any(BookCopyEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        BookCopyEntity result = service.save(noStatus);

        assertThat(result.getStatus()).isEqualTo(CopyStatus.AVAILABLE);
    }

    @Test
    void update_updatesCopy_whenCodeNotTaken() {
        when(repository.findById(1L)).thenReturn(Optional.of(copy));
        when(repository.save(any(BookCopyEntity.class))).thenReturn(copy);

        BookCopyEntity result = service.update(1L, request);

        assertThat(result).isNotNull();
        verify(repository).save(copy);
    }

    @Test
    void update_throwsIllegalStateException_whenNewCodeAlreadyTaken() {
        BookCopyRequest newCode = new BookCopyRequest(1L, "COPY-999", null, CopyCondition.GOOD, null, null, null);
        when(repository.findById(1L)).thenReturn(Optional.of(copy));
        when(repository.existsByCode("COPY-999")).thenReturn(true);

        assertThatThrownBy(() -> service.update(1L, newCode))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("El código del ejemplar ya existe");
    }

    @Test
    void markAsLost_setsStatusLost() {
        when(repository.findById(1L)).thenReturn(Optional.of(copy));
        when(repository.save(copy)).thenReturn(copy);

        BookCopyEntity result = service.markAsLost(1L);

        assertThat(result.getStatus()).isEqualTo(CopyStatus.LOST);
    }

    @Test
    void markAsLost_throwsIllegalStateException_whenAlreadyLost() {
        copy.setStatus(CopyStatus.LOST);
        when(repository.findById(1L)).thenReturn(Optional.of(copy));

        assertThatThrownBy(() -> service.markAsLost(1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("ya está marcado como perdido");
    }

    @Test
    void markAsDamaged_setsStatusDamaged_whenNotLoaned() {
        when(repository.findById(1L)).thenReturn(Optional.of(copy));
        when(repository.save(copy)).thenReturn(copy);

        BookCopyEntity result = service.markAsDamaged(1L, CopyCondition.DAMAGED);

        assertThat(result.getStatus()).isEqualTo(CopyStatus.DAMAGED);
        assertThat(result.getCondition()).isEqualTo(CopyCondition.DAMAGED);
    }

    @Test
    void markAsDamaged_throwsIllegalStateException_whenLoaned() {
        copy.setStatus(CopyStatus.LOANED);
        when(repository.findById(1L)).thenReturn(Optional.of(copy));

        assertThatThrownBy(() -> service.markAsDamaged(1L, CopyCondition.DAMAGED))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No se puede marcar como dañado");
    }

    @Test
    void restore_setsStatusAvailable_whenDamaged() {
        copy.setStatus(CopyStatus.DAMAGED);
        when(repository.findById(1L)).thenReturn(Optional.of(copy));
        when(repository.save(copy)).thenReturn(copy);

        BookCopyEntity result = service.restore(1L);

        assertThat(result.getStatus()).isEqualTo(CopyStatus.AVAILABLE);
        assertThat(result.getCondition()).isEqualTo(CopyCondition.FAIR);
    }

    @Test
    void restore_throwsIllegalStateException_whenAvailable() {
        when(repository.findById(1L)).thenReturn(Optional.of(copy));

        assertThatThrownBy(() -> service.restore(1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Solo se pueden restaurar");
    }

    @Test
    void delete_callsRepositoryDelete_whenNotLoaned() {
        when(repository.findById(1L)).thenReturn(Optional.of(copy));

        service.delete(1L);

        verify(repository).delete(copy);
    }

    @Test
    void delete_throwsIllegalStateException_whenLoaned() {
        copy.setStatus(CopyStatus.LOANED);
        when(repository.findById(1L)).thenReturn(Optional.of(copy));

        assertThatThrownBy(() -> service.delete(1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No se puede eliminar un ejemplar que está prestado");
    }
}
