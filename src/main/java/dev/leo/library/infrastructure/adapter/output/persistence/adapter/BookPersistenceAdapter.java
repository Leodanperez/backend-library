package dev.leo.library.infrastructure.adapter.output.persistence.adapter;

import dev.leo.library.domain.port.output.BookRepositoryPort;
import dev.leo.library.infrastructure.adapter.output.persistence.entity.BookEntity;
import dev.leo.library.infrastructure.adapter.output.persistence.repository.BookJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class BookPersistenceAdapter implements BookRepositoryPort {

    private final BookJpaRepository bookRepository;

    @Override
    public Optional<BookEntity> findById(Long id) {
        return bookRepository.findById(id);
    }

    @Override
    public List<BookEntity> findAllSortedByTitle() {
        return bookRepository.findAll(Sort.by("title").ascending());
    }

    @Override
    public Page<BookEntity> search(String q, Long authorId, Long categoryId, String language, Boolean active, Pageable pageable) {
        return bookRepository.findAll(BookSpec.filter(q, authorId, categoryId, language, active), pageable);
    }

    @Override
    public boolean existsByIsbn(String isbn) {
        return bookRepository.existsByIsbn(isbn);
    }

    @Override
    public BookEntity save(BookEntity book) {
        return bookRepository.save(book);
    }

    @Override
    public void delete(BookEntity book) {
        bookRepository.delete(book);
    }

    @Override
    public Set<Long> findBookIdsWithAvailableCopies(List<Long> bookIds) {
        return bookRepository.findBookIdsWithAvailableCopies(bookIds);
    }

    @Override
    public long countActiveByAuthorId(Long authorId) {
        return bookRepository.countActiveByAuthorId(authorId);
    }

    @Override
    public long countActiveByCategoryId(Long categoryId) {
        return bookRepository.countActiveByCategoryId(categoryId);
    }
}
