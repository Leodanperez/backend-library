package dev.leo.library.application.service;

import dev.leo.library.application.dto.request.BookRequest;
import dev.leo.library.domain.exception.BookNotFoundException;
import dev.leo.library.infrastructure.adapter.output.persistence.entity.AuthorEntity;
import dev.leo.library.infrastructure.adapter.output.persistence.entity.BookEntity;
import dev.leo.library.infrastructure.adapter.output.persistence.entity.CategoryEntity;
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

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    @Mock
    private BookJpaRepository repository;

    @Mock
    private AuthorService authorService;

    @Mock
    private CategoryService categoryService;

    @InjectMocks
    private BookService service;

    private BookEntity book;
    private BookRequest request;
    private AuthorEntity author;
    private CategoryEntity category;

    @BeforeEach
    void setUp() {
        author = AuthorEntity.builder().id(1L).firstName("Gabriel").lastName("García Márquez").active(true).build();
        category = CategoryEntity.builder().id(1L).name("Fiction").active(true).build();
        book = BookEntity.builder()
                .id(1L).title("Cien años de soledad").isbn("978-0-06-088328-7")
                .author(author).category(category).active(true).build();
        request = new BookRequest("Cien años de soledad", "978-0-06-088328-7",
                "A novel", 1967, 417, "Spanish", "Editorial Sudamericana", null, 1L, 1L);
    }

    @Test
    void findAll_returnsPagedResponse() {
        when(repository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(book)));

        PaginatedResponse<BookEntity> result = service.findAll(null, null, null, null, null, 1, 10);

        assertThat(result.data()).hasSize(1);
        assertThat(result.total()).isEqualTo(1);
    }

    @Test
    void findById_returnsBook_whenExists() {
        when(repository.findById(1L)).thenReturn(Optional.of(book));

        BookEntity result = service.findById(1L);

        assertThat(result.getTitle()).isEqualTo("Cien años de soledad");
    }

    @Test
    void findById_throwsBookNotFoundException_whenNotFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(99L))
                .isInstanceOf(BookNotFoundException.class);
    }

    @Test
    void save_createsBook_whenIsbnNotDuplicated() {
        when(repository.existsByIsbn(request.isbn())).thenReturn(false);
        when(authorService.findById(1L)).thenReturn(author);
        when(categoryService.findById(1L)).thenReturn(category);
        when(repository.save(any(BookEntity.class))).thenReturn(book);

        BookEntity result = service.save(request);

        assertThat(result.getTitle()).isEqualTo("Cien años de soledad");
        verify(repository).save(any(BookEntity.class));
    }

    @Test
    void save_throwsIllegalStateException_whenIsbnAlreadyExists() {
        when(repository.existsByIsbn(request.isbn())).thenReturn(true);

        assertThatThrownBy(() -> service.save(request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("El ISBN ya está registrado");
    }

    @Test
    void save_skipsIsbnCheck_whenIsbnIsNull() {
        BookRequest noIsbn = new BookRequest("El Aleph", null, null, 1949, 125, "Spanish", null, null, 1L, 1L);
        when(authorService.findById(1L)).thenReturn(author);
        when(categoryService.findById(1L)).thenReturn(category);
        when(repository.save(any(BookEntity.class))).thenReturn(book);

        service.save(noIsbn);

        verify(repository, never()).existsByIsbn(any());
    }

    @Test
    void update_updatesBook_whenIsbnNotTaken() {
        when(repository.findById(1L)).thenReturn(Optional.of(book));
        when(authorService.findById(1L)).thenReturn(author);
        when(categoryService.findById(1L)).thenReturn(category);
        when(repository.save(any(BookEntity.class))).thenReturn(book);

        BookEntity result = service.update(1L, request);

        assertThat(result).isNotNull();
        verify(repository).save(book);
    }

    @Test
    void update_throwsIllegalStateException_whenNewIsbnAlreadyTaken() {
        BookRequest newIsbn = new BookRequest("Cien años", "978-NEW-ISBN",
                null, null, null, null, null, null, 1L, 1L);
        when(repository.findById(1L)).thenReturn(Optional.of(book));
        when(repository.existsByIsbn("978-NEW-ISBN")).thenReturn(true);

        assertThatThrownBy(() -> service.update(1L, newIsbn))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("El ISBN ya está registrado");
    }

    @Test
    void activate_setsActiveTrue() {
        book.setActive(false);
        when(repository.findById(1L)).thenReturn(Optional.of(book));
        when(repository.save(book)).thenReturn(book);

        BookEntity result = service.activate(1L);

        assertThat(result.isActive()).isTrue();
    }

    @Test
    void deactivate_setsActiveFalse() {
        when(repository.findById(1L)).thenReturn(Optional.of(book));
        when(repository.save(book)).thenReturn(book);

        BookEntity result = service.deactivate(1L);

        assertThat(result.isActive()).isFalse();
    }

    @Test
    void delete_callsRepositoryDelete_whenFound() {
        when(repository.findById(1L)).thenReturn(Optional.of(book));

        service.delete(1L);

        verify(repository).delete(book);
    }
}
