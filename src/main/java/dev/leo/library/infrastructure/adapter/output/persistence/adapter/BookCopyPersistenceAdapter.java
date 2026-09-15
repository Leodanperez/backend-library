package dev.leo.library.infrastructure.adapter.output.persistence.adapter;

import dev.leo.library.domain.model.CopyCondition;
import dev.leo.library.domain.model.CopyStatus;
import dev.leo.library.domain.port.output.BookCopyRepositoryPort;
import dev.leo.library.infrastructure.adapter.output.persistence.entity.BookCopyEntity;
import dev.leo.library.infrastructure.adapter.output.persistence.repository.BookCopyJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class BookCopyPersistenceAdapter implements BookCopyRepositoryPort {

    private final BookCopyJpaRepository bookCopyRepository;

    @Override
    public Optional<BookCopyEntity> findById(Long id) {
        return bookCopyRepository.findById(id);
    }

    @Override
    public List<BookCopyEntity> findByBookId(Long bookId) {
        return bookCopyRepository.findByBookId(bookId);
    }

    @Override
    public List<BookCopyEntity> findAllSortedByCode() {
        return bookCopyRepository.findAll(Sort.by("code").ascending());
    }

    @Override
    public Page<BookCopyEntity> search(String q, Long bookId, CopyStatus status, CopyCondition condition, Pageable pageable) {
        return bookCopyRepository.findAll(BookCopySpec.filter(q, bookId, status, condition), pageable);
    }

    @Override
    public boolean existsByCode(String code) {
        return bookCopyRepository.existsByCode(code);
    }

    @Override
    public BookCopyEntity save(BookCopyEntity copy) {
        return bookCopyRepository.save(copy);
    }

    @Override
    public void delete(BookCopyEntity copy) {
        bookCopyRepository.delete(copy);
    }
}
